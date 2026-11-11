package es.uclm.iso2.server.persistencia;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Ensamblado de la capa de persistencia (D34): decide que {@link Agente} encaja con el
 * SGBD real al que apunta el {@link DataSource} unico de la aplicacion.
 *
 * <p>La decision se toma preguntando al conductor JDBC
 * ({@link DatabaseMetaData#getDatabaseProductName()}) en el arranque, NO mirando
 * perfiles ni cadenas de conexion: si manana la aplicacion apunta a otra base, el bean
 * se equivoca imposible de quedar desincronizado.</p>
 *
 * <p>Productos reconocidos: {@code H2} y {@code MySQL} (los dos soportados por la
 * D34). Cualquier otro producto deja un agente generico que responde con el nombre
 * declarado por la base y una advertencia en el log: la aplicacion arranca igual y
 * JPA sigue funcionando, solo queda sin diagnostico especifico.</p>
 */
@Configuration
public class ConfigPersistencia {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigPersistencia.class);

    /**
     * Registra el {@link Agente} que corresponde al SGBD detectado.
     *
     * @param dataSource pool de conexiones unico del servidor (lo aporta Boot)
     * @return agente H2, agente MySQL o agente generico con aviso en log
     * @throws SQLException si ni siquiera se puede abrir una conexion para detectar
     *                      el producto: la aplicacion no tiene sentido sin base
     */
    @Bean
    public Agente agente(DataSource dataSource) throws SQLException {
        String producto;
        try (Connection c = dataSource.getConnection()) {
            DatabaseMetaData meta = c.getMetaData();
            producto = meta.getDatabaseProductName();
        }
        LOG.info("SGBD detectado en el arranque: {}", producto);
        return switch (producto) {
            case "H2" -> new AgenteH2(dataSource);
            case "MySQL" -> new AgenteMySQL(dataSource);
            default -> {
                LOG.warn("SGBD no reconocido ({}): se usa un agente generico; los DAOs "
                        + "especificos de H2/MySQL pueden no funcionar.", producto);
                yield new AgenteGenerico(producto, dataSource);
            }
        };
    }

    /**
     * Agente de respaldo para productos no reconocidos: presta conexiones y lee el
     * esquema con la funcion estandar {@code CURRENT_SCHEMA} (SQL:2016), que H2 y
     * algunos otros SGBD entienden; si la base no la soporta, {@link #esquemaActual()}
     * lanza y el DAO de diagnostico lo reporta.
     */
    static class AgenteGenerico extends AgenteSobreDataSource {

        /**
         * Crea el agente generico con el nombre declarado por la base.
         *
         * @param nombre producto informado por el conductor JDBC
         * @param dataSource pool de conexiones unico del servidor
         */
        AgenteGenerico(String nombre, DataSource dataSource) {
            super(nombre, dataSource);
        }

        /**
         * {@inheritDoc}
         *
         * <p>Usa la sintaxis estandar SQL:2016; puede fallar en SGBD antiguos.</p>
         */
        @Override
        protected String consultaEsquema() {
            return "SELECT CURRENT_SCHEMA";
        }
    }
}
