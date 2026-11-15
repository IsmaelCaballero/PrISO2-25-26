package es.uclm.iso2.integracion.adaptador;

import es.uclm.iso2.common.integracion.SistemaExternoException;
import es.uclm.iso2.integracion.dominio.EstadoSistema;
import es.uclm.iso2.integracion.dominio.ModoSimulacion;
import es.uclm.iso2.integracion.dominio.PoliticaSimulacion;
import es.uclm.iso2.integracion.dominio.RegistroSimulacion;
import es.uclm.iso2.integracion.dominio.SistemaExterno;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Supplier;

/**
 * Base de los cuatro adaptadores simulados. {@link #proteger(Supplier)} aplica la
 * politica de simulacion vigente antes de ejecutar la llamada:
 *
 * <ul>
 *   <li>CAIDO: falla con {@code NO_DISPONIBLE} sin ni siquiera mirar la peticion. Por
 *       eso el fallo de sistema gana a la credencial invalida o al id inexistente
 *       (precedencia 503 &gt; 401 &gt; 400 que fija el contrato).</li>
 *   <li>LENTO_TIMEOUT con latencia mayor que el tiempo de espera: duerme la latencia
 *       {@code 1 + reintentos} veces y falla con {@code TIEMPO_SUPERADO}; la llamada
 *       real jamas se ejecuta.</li>
 *   <li>LENTO dentro de plazo o NORMAL: duerme la latencia simulada y ejecuta.</li>
 * </ul>
 *
 * Las latencias de cada intento se acumulan en una media movil corta para el ping de
 * monitorizacion.
 *
 * @since 1.1.0 (CFG-06, CDU19)
 */
public abstract class AdaptadorSimulado {

    /** Ventana de latencias recordadas para la media movil del ping. */
    private static final int VENTANA_LATENCIAS = 10;

    /** Registro compartido de politicas: la API lo cambia sin reiniciar el servidor. */
    protected final RegistroSimulacion registro;

    /** Sistema que representa este adaptador. */
    private final SistemaExterno sistema;

    private final Deque<Long> latencias = new ArrayDeque<>();

    /**
     * Asocia el adaptador a su sistema y al registro compartido de politicas.
     *
     * @param sistema  sistema externo simulado
     * @param registro registro vivo de politicas de simulacion
     */
    protected AdaptadorSimulado(SistemaExterno sistema, RegistroSimulacion registro) {
        this.sistema = sistema;
        this.registro = registro;
    }

    /**
     * Ejecuta la llamada aplicando la politica vigente del sistema.
     *
     * @param llamada contenido real de la llamada al sistema externo
     * @param <T>     tipo del resultado
     * @return el resultado de la llamada
     * @throws SistemaExternoException si la politica dice que el sistema esta caido o
     *                                 agota el tiempo de espera tras los reintentos
     */
    protected <T> T proteger(Supplier<T> llamada) {
        PoliticaSimulacion politica = registro.de(sistema);
        if (politica.modo() == ModoSimulacion.CAIDO) {
            throw new SistemaExternoException(SistemaExternoException.Fallo.NO_DISPONIBLE,
                    "el sistema " + sistema.codigo() + " esta caido");
        }
        if (politica.excedeTiempoEspera()) {
            int intentos = 1 + politica.reintentos();
            for (int intento = 0; intento < intentos; intento++) {
                dormir(politica.latenciaMs());
                anadirLatenciaObservada(politica.latenciaMs());
            }
            throw new SistemaExternoException(SistemaExternoException.Fallo.TIEMPO_SUPERADO,
                    "el sistema " + sistema.codigo() + " no respondio en "
                            + politica.timeoutMs() + " ms tras " + intentos + " intentos");
        }
        dormir(politica.latenciaMs());
        T valor = llamada.get();
        anadirLatenciaObservada(politica.latenciaMs());
        return valor;
    }

    /**
     * Estado actual del sistema para el ping degradado: jamas lanza excepcion. Un
     * sistema caido o con latencia fuera de plazo se reporta como no disponible con su
     * motivo, con la latencia media observada (o la de la politica si aun no ha
     * habido llamadas).
     *
     * @return estado del sistema
     */
    public EstadoSistema estado() {
        PoliticaSimulacion politica = registro.de(sistema);
        long media = mediaMovil(politica.latenciaMs());
        if (politica.modo() == ModoSimulacion.CAIDO) {
            return new EstadoSistema(sistema, false, "CAIDO", media);
        }
        if (politica.excedeTiempoEspera()) {
            return new EstadoSistema(sistema, false, "TIEMPO_SUPERADO", media);
        }
        return new EstadoSistema(sistema, true, null, media);
    }

    /**
     * Recorta y normaliza el identificador recibido antes de buscarlo, la misma
     * leccion de CDU12: si el cliente manda espacios, la busco igualmente.
     *
     * @param identificador id tal y como llego de la API
     * @return el id recortado
     */
    protected static String recortar(String identificador) {
        return identificador == null ? "" : identificador.trim();
    }

    private void dormir(long milisegundos) {
        if (milisegundos <= 0) {
            return;
        }
        try {
            Thread.sleep(milisegundos);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SistemaExternoException(SistemaExternoException.Fallo.NO_DISPONIBLE,
                    "interrumpida la espera del sistema " + sistema.codigo());
        }
    }

    private synchronized void anadirLatenciaObservada(long latencia) {
        latencias.addLast(latencia);
        while (latencias.size() > VENTANA_LATENCIAS) {
            latencias.removeFirst();
        }
    }

    private synchronized long mediaMovil(long latenciaPolitica) {
        if (latencias.isEmpty()) {
            return latenciaPolitica;
        }
        long suma = 0;
        for (long latencia : latencias) {
            suma += latencia;
        }
        return suma / latencias.size();
    }
}
