package es.uclm.iso2.server.persistencia;

import java.util.List;

/**
 * Resultado completo del diagnostico de persistencia (DTO inmutable que devuelve
 * {@code GET /api/persistencia/diagnostico}, D34).
 *
 * @param sgbd nombre del SGBD declarado por el agente activo
 * @param esquema esquema (base) actual segun el propio SGBD
 * @param tablas tablas del esquema, incluida {@code flyway_schema_history}
 * @param conteosPorTabla recuento por tabla de negocio (sin la tabla de Flyway)
 */
public record DiagnosticoSgbD(String sgbd, String esquema, List<String> tablas,
                              List<ConteoTabla> conteosPorTabla) {

    /**
     * Fabrica el diagnostico a partir del agente y de los conteos ya calculados.
     *
     * @param agente agente del SGBD activo
     * @param tablas tablas leidas del catalogo
     * @param conteos recuentos por tabla de negocio
     * @param esquema esquema actual ya resuelto por el agente
     * @return DTO listo para serializar
     */
    public static DiagnosticoSgbD de(Agente agente, List<String> tablas,
                                     List<ConteoTabla> conteos, String esquema) {
        return new DiagnosticoSgbD(agente.nombre(), esquema, List.copyOf(tablas), List.copyOf(conteos));
    }
}
