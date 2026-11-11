package es.uclm.iso2.server.persistencia;

import javax.sql.DataSource;

/**
 * Agente para H2 2.x (base por defecto de la aplicacion y de todos los tests).
 *
 * <p>H2 2.x ya no acepta {@code SELECT SCHEMA()} con parentesis (es palabra clave al
 * estilo SQL:2016), por eso la consulta de esquema es {@code SELECT SCHEMA}.</p>
 */
public class AgenteH2 extends AgenteSobreDataSource {

    /**
     * Crea el agente H2 sobre el pool de la aplicacion.
     *
     * @param dataSource pool de conexiones unico del servidor
     */
    public AgenteH2(DataSource dataSource) {
        super("H2", dataSource);
    }

    /**
     * {@inheritDoc}
     *
     * <p>H2 2.x ya no acepta {@code SELECT SCHEMA()} (lo toma como columna) ni
     * {@code SELECT SCHEMA}: la funcion estandar es {@code CURRENT_SCHEMA()}, que
     * devuelve el esquema por defecto ({@code PUBLIC} en memorias y ficheros sin
     * esquema fijo).</p>
     */
    @Override
    protected String consultaEsquema() {
        return "SELECT CURRENT_SCHEMA()";
    }
}
