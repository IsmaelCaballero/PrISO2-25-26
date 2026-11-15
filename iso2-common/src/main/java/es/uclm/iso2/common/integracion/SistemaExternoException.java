package es.uclm.iso2.common.integracion;

/**
 * Excepcion de contrato: el sistema externo esta caido o no respondio dentro del
 * tiempo de espera del adaptador. La aplicacion servidora la traduce a HTTP 503
 * con un codigo estable segun el tipo: {@code SISTEMA_NO_DISPONIBLE} o
 * {@code SISTEMA_TIEMPO_SUPERADO}.
 *
 * <p>No aplica al ping de monitorizacion: el ping degradado de CDU19 responde
 * siempre 200 con el estado. Esta excepcion es para el login SSO, la
 * sincronizacion de historia y la importacion de estudios, donde el fallo del
 * sistema externo si rompe la operacion y gana a cualquier otro error de
 * entrada (precedencia 503 &gt; 401 &gt; 400).</p>
 *
 * @since 1.1.0 (CFG-06, CDU19)
 */
public class SistemaExternoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** Motivos por los que un sistema externo no pudo ser atendido. */
    public enum Fallo {
        /** El sistema esta caido o en modo no-disponible. */
        NO_DISPONIBLE,
        /** El sistema tardo mas que el tiempo de espera tras agotar los reintentos. */
        TIEMPO_SUPERADO
    }

    private final Fallo tipo;

    /**
     * Crea el fallo con su tipo y mensaje legible.
     *
     * @param tipo    clase de fallo del sistema externo
     * @param mensaje detalle para el usuario final
     */
    public SistemaExternoException(Fallo tipo, String mensaje) {
        super(mensaje);
        this.tipo = tipo;
    }

    /**
     * Tipo de fallo detectado.
     *
     * @return el tipo de fallo
     */
    public Fallo tipo() {
        return tipo;
    }
}
