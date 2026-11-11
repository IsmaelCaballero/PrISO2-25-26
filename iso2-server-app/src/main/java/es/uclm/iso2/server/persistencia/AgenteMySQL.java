package es.uclm.iso2.server.persistencia;

import javax.sql.DataSource;

/**
 * Agente para MySQL 8.4 Levss (perfil {@code mysql} con la base de {@code docker/}).
 *
 * <p>En MySQL el esquema actual se lee con la funcion {@code DATABASE()}, que
 * devuelve la base seleccionada en la cadena de conexion ({@code iso2}).</p>
 */
public class AgenteMySQL extends AgenteSobreDataSource {

    /**
     * Crea el agente MySQL sobre el pool de la aplicacion.
     *
     * @param dataSource pool de conexiones unico del servidor
     */
    public AgenteMySQL(DataSource dataSource) {
        super("MySQL", dataSource);
    }

    /**
     * {@inheritDoc}
     *
     * <p>{@code SELECT DATABASE()} devuelve la base activa de la sesion; con la URL
     * del perfil {@code mysql} eso es {@code iso2}.</p>
     */
    @Override
    protected String consultaEsquema() {
        return "SELECT DATABASE()";
    }
}
