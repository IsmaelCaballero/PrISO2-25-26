package es.uclm.iso2.integracion.dominio;

/**
 * Modo en que se comporta un adaptador simulado al ejecutar una llamada. Es el
 * concepto que hace repetibles los TCs de modo degradado: el CSV de TDOP fija el modo
 * por fila con setups tipo {@code SIM-HCE-CAIDO-5-50-0}.
 *
 * @since 1.1.0 (CFG-06, CDU19)
 */
public enum ModoSimulacion {

    /** El sistema responde: latencia pequena y exito. */
    NORMAL,
    /** El sistema responde tarde pero dentro del tiempo de espera. */
    LENTO,
    /** El sistema tarda mas que el tiempo de espera: falla tras agotar los reintentos. */
    LENTO_TIMEOUT,
    /** El sistema esta caido: falla sin llegar a mirar la peticion. */
    CAIDO
}
