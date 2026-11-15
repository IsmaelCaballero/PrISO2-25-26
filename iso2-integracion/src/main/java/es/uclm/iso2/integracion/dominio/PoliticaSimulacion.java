package es.uclm.iso2.integracion.dominio;

/**
 * Como se comporta un adaptador simulado en cada llamada: modo de fallo, latencia
 * simulada, tiempo de espera y reintentos. Es un valor inmutable; el registro vivo de
 * las politicas es {@link RegistroSimulacion}.
 *
 * @param modo       modo de simulacion activo
 * @param latenciaMs milisegundos que tarda simuladamente cada llamada
 * @param timeoutMs  milisegundos maximos que el adaptador esta dispuesto a esperar
 * @param reintentos reintentos tras el primer intento antes de rendirse
 * @since 1.1.0 (CFG-06, CDU19)
 */
public record PoliticaSimulacion(ModoSimulacion modo, long latenciaMs, long timeoutMs, int reintentos) {

    /**
     * Crea la politica comprobando que las cifras tienen sentido.
     */
    public PoliticaSimulacion {
        if (modo == null) {
            throw new IllegalArgumentException("el modo de simulacion es obligatorio");
        }
        if (latenciaMs < 0 || timeoutMs < 0) {
            throw new IllegalArgumentException("la latencia y el tiempo de espera no pueden ser negativos");
        }
        if (reintentos < 0) {
            throw new IllegalArgumentException("los reintentos no pueden ser negativos");
        }
    }

    /**
     * Politica normal con la latencia, el tiempo de espera y los reintentos por defecto
     * que arranca el modulo segun {@code iso2.integracion.*}.
     *
     * @param latenciaMs latencia simulada en milisegundos
     * @param timeoutMs  tiempo de espera en milisegundos
     * @param reintentos reintentos por llamada
     * @return la politica NORMAL
     */
    public static PoliticaSimulacion normal(long latenciaMs, long timeoutMs, int reintentos) {
        return new PoliticaSimulacion(ModoSimulacion.NORMAL, latenciaMs, timeoutMs, reintentos);
    }

    /**
     * La latencia simulada desborda el tiempo de espera: la llamada terminara en
     * {@code TIEMPO_SUPERADO} tras agotar los reintentos.
     *
     * @return true si el modo agotara el tiempo de espera
     */
    public boolean excedeTiempoEspera() {
        return modo == ModoSimulacion.LENTO_TIMEOUT && latenciaMs > timeoutMs;
    }
}
