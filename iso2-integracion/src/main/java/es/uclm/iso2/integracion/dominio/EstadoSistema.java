package es.uclm.iso2.integracion.dominio;

import java.util.Objects;

/**
 * Estado que devuelve el ping de un sistema externo. El spike decide que el ping
 * degradado nunca es un 5xx: un sistema caido se pinta como {@code disponible=false}
 * con su motivo, porque el monitor necesita pintar el estado, no fallar.
 *
 * @param sistema        sistema evaluado
 * @param disponible     si respondio correctamente al ping
 * @param motivo         porque no esta disponible (nulo si si lo esta)
 * @param latenciaMediaMs media movil de latencias observadas en llamadas recientes
 * @since 1.1.0 (CFG-06, CDU19)
 */
public record EstadoSistema(SistemaExterno sistema, boolean disponible, String motivo,
                            long latenciaMediaMs) {

    /**
     * Crea el estado comprobando el sistema.
     */
    public EstadoSistema {
        Objects.requireNonNull(sistema, "el sistema es obligatorio");
    }
}
