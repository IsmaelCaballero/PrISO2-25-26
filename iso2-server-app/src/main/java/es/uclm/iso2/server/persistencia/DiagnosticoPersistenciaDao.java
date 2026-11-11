package es.uclm.iso2.server.persistencia;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Repository;

/**
 * DAO JDBC puro de diagnostico (D34): el primer acceso por JDBC nativo del proyecto,
 * complemento al JPA de los modulos de dominio (decisión D34: JPA arriba, DAO como
 * capa nueva). Lee {@code INFORMATION_SCHEMA}, una vista del propio SGBD que JPA no
 * mapea, por eso va con {@link PreparedStatement} sobre la conexion del {@link Agente}.
 *
 * <p>Las dos consultas de tablas usan la forma comun a H2 2.x y MySQL 8.4
 * ({@code INFORMATION_SCHEMA.TABLES} filtrando {@code TABLE_SCHEMA} y
 * {@code TABLE_TYPE = 'BASE TABLE'}), asi que el mismo codigo vale para ambas bases.</p>
 */
@Repository
public class DiagnosticoPersistenciaDao {

    private final Agente agente;

    /**
     * Inyecta el agente del SGBD activo (lo registra {@link ConfigPersistencia}).
     *
     * @param agente agente H2/MySQL/generico segun se detectase en el arranque
     */
    public DiagnosticoPersistenciaDao(Agente agente) {
        this.agente = agente;
    }

    /**
     * Lista las tablas de datos del esquema actual (incluye {@code flyway_schema_history},
     * que tambien es una tabla del esquema).
     *
     * @return nombres de tabla en el orden que los da el SGBD
     * @throws SQLException si el SGBD no responde a la vista de sistema
     */
    public List<String> tablasDelEsquema() throws SQLException {
        String sql = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES"
                + " WHERE TABLE_SCHEMA = ? AND TABLE_TYPE = 'BASE TABLE'";
        List<String> tablas = new ArrayList<>();
        try (var c = agente.conexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, agente.esquemaActual());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tablas.add(rs.getString(1));
                }
            }
        }
        return tablas;
    }

    /**
     * Cuenta los registros de una tabla concreta.
     *
     * <p>El nombre de tabla NO viene de un usuario: sale de {@link #tablasDelEsquema()},
     * que a su vez consulta el catalogo del SGBD. Aun asi se valida que sea un
     * identificador simple ([A-Za-z0-9_]+) antes de incrustarlo en el SQL, para que
     * este patron de JDBC puro sea seguro aunque se copie en otros DAOs.</p>
     *
     * @param tabla nombre de tabla del esquema actual
     * @return numero de filas de la tabla
     * @throws SQLException si la tabla no existe o el SGBD falla
     * @throws IllegalArgumentException si {@code tabla} no es un identificador simple
     */
    public long cuentaRegistros(String tabla) throws SQLException {
        if (tabla == null || !tabla.matches("[A-Za-z0-9_]+")) {
            throw new IllegalArgumentException("Nombre de tabla no valido: " + tabla);
        }
        try (var c = agente.conexion();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM " + tabla)) {
            return rs.next() ? rs.getLong(1) : 0L;
        }
    }

    /**
     * Recuento completo: cada tabla del esquema con sus registros, excluyendo la tabla
     * tecnica {@code flyway_schema_history} que solo interesa a Flyway.
     *
     * @return conteos por tabla de negocio, en el orden del esquema
     * @throws SQLException si falla alguna lectura del catalogo o un recuento
     */
    public List<ConteoTabla> conteoPorTabla() throws SQLException {
        List<ConteoTabla> conteos = new ArrayList<>();
        for (String tabla : tablasDelEsquema()) {
            if ("flyway_schema_history".equalsIgnoreCase(tabla)) {
                continue;
            }
            conteos.add(new ConteoTabla(tabla, cuentaRegistros(tabla)));
        }
        return conteos;
    }
}
