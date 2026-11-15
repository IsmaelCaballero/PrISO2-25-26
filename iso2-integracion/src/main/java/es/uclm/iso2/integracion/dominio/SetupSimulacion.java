package es.uclm.iso2.integracion.dominio;

import java.util.Locale;

/**
 * Setup de un TC de la matriz TDOP: sistema afectado y politica a instalar. El token
 * canónico tiene la forma {@code SIM-HCE-CAIDO-5-50-0} (prefijo, sistema, modo,
 * latencia ms, tiempo de espera ms y reintentos) y es lo que guarda la columna
 * {@code setup} de los CSV generados por {@code tools/pairwise.py}.
 *
 * @param sistema  sistema externo cuya politica se cambia
 * @param politica politica que queda instalada tras aplicar el setup
 * @since 1.1.0 (CFG-06, CDU19)
 */
public record SetupSimulacion(SistemaExterno sistema, PoliticaSimulacion politica) {

    /** Prefijo comun de todos los tokens de setup. */
    private static final String PREFIJO = "SIM";

    /**
     * Analiza un token de setup del CSV TDOP.
     *
     * @param token token tipo {@code SIM-HCE-CAIDO-5-50-0}
     * @return el sistema y la politica que describe
     * @throws IllegalArgumentException si el token no tiene la forma esperada
     */
    public static SetupSimulacion desdeToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("el token de setup esta vacio");
        }
        String[] partes = token.trim().toUpperCase(Locale.ROOT).split("-");
        if (partes.length != 6 || !PREFIJO.equals(partes[0])) {
            throw new IllegalArgumentException("token de setup invalido: " + token);
        }
        SistemaExterno sistema = SistemaExterno.desdeCodigo(partes[1])
                .orElseThrow(() -> new IllegalArgumentException("sistema desconocido en el token: " + token));
        ModoSimulacion modo;
        try {
            modo = ModoSimulacion.valueOf(partes[2]);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("modo desconocido en el token: " + token, e);
        }
        try {
            return new SetupSimulacion(sistema, new PoliticaSimulacion(modo,
                    Long.parseLong(partes[3]), Long.parseLong(partes[4]), Integer.parseInt(partes[5])));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("cifras invalidas en el token: " + token, e);
        }
    }

    /**
     * Reconstruye el token canónico. Los CSV de TDOP y los logs de los TCs quedan
     * legibles en la matriz esperada.
     *
     * @return el token {@code SIM-<sistema>-<modo>-<lat>-<timeout>-<reintentos>}
     */
    @Override
    public String toString() {
        return PREFIJO + "-" + sistema.name() + "-" + politica.modo().name() + "-"
                + politica.latenciaMs() + "-" + politica.timeoutMs() + "-" + politica.reintentos();
    }
}
