package es.uclm.iso2.server.persistencia;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;

/**
 * Base comun de los agentes que viven sobre el {@link DataSource} unico de la
 * aplicacion (D34). Guarda la fuente de datos y resuelve el patron de plantilla
 * <em>metodo plantilla</em>: {@link #conexion()} y {@link #esquemaActual()} son
 * identicas para cualquier SGBD; solo cambian las dos preguntas que hace cada base,
 * que dejan los subtipos concretos ({@link AgenteH2}, {@link AgenteMySQL}).
 */
public abstract class AgenteSobreDataSource implements Agente {

    /** Nombre comercial con el que este agente se presenta (H2, MySQL...). */
    private final String nombreComercial;

    /** Fuente de datos (pool HikariCP) compartida con JPA y Flyway. */
    protected final DataSource dataSource;

    /**
     * Crea el agente sobre la fuente de datos de la aplicacion.
     *
     * @param nombreComercial nombre con el que se presenta este SGBD (no vacio)
     * @param dataSource pool de conexiones unico del servidor; no nulo
     */
    protected AgenteSobreDataSource(String nombreComercial, DataSource dataSource) {
        this.nombreComercial = nombreComercial;
        this.dataSource = dataSource;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Devuelve el nombre comercial con el que el subtipo se construyo.</p>
     */
    @Override
    public String nombre() {
        return nombreComercial;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Devuelve siempre una conexion del pool {@code iso2}: la misma base que usa
     * JPA, para que el diagnostico refleje los datos reales de la aplicacion.</p>
     */
    @Override
    public Connection conexion() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Ejecuta la consulta que declara {@link #consultaEsquema()} y devuelve su
     * primer valor. Nunca abre conexiones propias fuera del pool.</p>
     */
    @Override
    public String esquemaActual() throws SQLException {
        try (Connection c = conexion();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery(consultaEsquema())) {
            if (!rs.next()) {
                throw new SQLException("El SGBD no devolvio esquema para la consulta " + consultaEsquema());
            }
            return rs.getString(1);
        }
    }

    /**
     * Sentencia SQL que, ejecutada sin parametros, devuelve el nombre del esquema
     * actual en el dialecto de este SGBD (p. ej. {@code SELECT SCHEMA} en H2 o
     * {@code SELECT DATABASE()} en MySQL).
     *
     * @return consulta de esquema propia del SGBD
     */
    protected abstract String consultaEsquema();
}
