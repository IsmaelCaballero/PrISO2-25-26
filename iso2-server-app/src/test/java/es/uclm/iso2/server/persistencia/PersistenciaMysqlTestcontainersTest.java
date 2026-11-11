package es.uclm.iso2.server.persistencia;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Puerta de calidad D34 (Iter5/CFG-05) sobre MySQL 8.4 real: arranca la aplicacion
 * completa contra un contenedor {@code mysql:8.4} con la misma separacion de usuarios
 * que {@code docker/init/01-usuarios.sql} ({@code gestoroperaciones} solo DML para la
 * app, {@code iso2_flyway} con DDL para Flyway) y verifica que:
 *
 * <ul>
 *   <li>el agente detectado es {@code MySQL} y el esquema es {@code iso2};</li>
 *   <li>las migraciones de {@code db/migration/mysql} se aplican y siembran datos;</li>
 *   <li>el usuario de aplicacion NO puede ejecutar DDL (error 1142 de MySQL).</li>
 * </ul>
 *
 * <p>Si no hay demonio de Docker, el test se ignora ({@code disabledWithoutDocker}):
 * la compuerta {@code mvn verify} no depende de Docker, pero en las maquinas con
 * Docker (la de Ismael y la que sera CI) si se ejecuta de verdad.</p>
 */
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class PersistenciaMysqlTestcontainersTest {

    /** Contrasenas SOLO de pruebas: las mismas que documenta docker/init/01-usuarios.sql. */
    private static final String USUARIO_APP = "gestoroperaciones";
    private static final String PASSWORD_APP = "Iso2Curso25-26";
    private static final String USUARIO_FLYWAY = "iso2_flyway";
    private static final String PASSWORD_FLYWAY = "Iso2Flyway25-26";
    private static final String BASE = "iso2";

    @Container
    private static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4")
            .withDatabaseName("test")
            .withUsername("root")
            .withPassword("root-test")
            .withInitScript("db/testcontainers/01-usuarios-mysql.sql");

    /**
     * Reescribe las propiedades de la aplicacion para apuntar al contenedor: la app
     * entra como usuario DML y Flyway como usuario DDL, con las migraciones mysql.
     *
     * @param registry propiedades dinamicas del contexto de Spring
     */
    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        // getJdbcUrl() del contenedor plano termina en /test sin parametros (1.21.x);
        // se cambia la base a /iso2 y se anaden los parametros del driver con el
        // separador correcto (? si no los trae ya, & si los trajera).
        String baseIso2 = MYSQL.getJdbcUrl()
                .replaceFirst("/test(\\?.*)?$", "/" + BASE + "$1");
        String sep = baseIso2.contains("?") ? "&" : "?";
        String urlIso2 = baseIso2 + sep + "useSSL=false&allowPublicKeyRetrieval=true"
                + "&connectionTimeZone=UTC&forceConnectionTimeZoneToSession=true"
                + "&preserveInstants=true&characterEncoding=UTF-8";
        registry.add("spring.datasource.url", () -> urlIso2);
        registry.add("spring.datasource.username", () -> USUARIO_APP);
        registry.add("spring.datasource.password", () -> PASSWORD_APP);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        registry.add("spring.flyway.url", () -> urlIso2);
        registry.add("spring.flyway.user", () -> USUARIO_FLYWAY);
        registry.add("spring.flyway.password", () -> PASSWORD_FLYWAY);
        registry.add("spring.flyway.locations", () -> "classpath:db/migration/mysql");
    }

    @Autowired
    private Agente agente;

    @Autowired
    private DiagnosticoPersistenciaDao dao;

    @Autowired
    private DataSource dataSource;

    @Test
    @DisplayName("TC-D34-MY-01: con el perfil de contenedor, el agente detectado es MySQL y el esquema iso2")
    void elAgenteDetectaMysqlYLBaseEsIso2() throws SQLException {
        assertEquals("MySQL", agente.nombre());
        assertEquals(BASE, agente.esquemaActual());
    }

    @Test
    @DisplayName("TC-D34-MY-02: las migraciones mysql crearon las tablas y la semilla llego")
    void lasMigracionesMysqlDejanTablasConDatos() throws SQLException {
        var tablas = dao.tablasDelEsquema();
        assertTrue(tablas.contains("catalogo_prueba"), "falta catalogo_prueba en " + tablas);
        assertTrue(tablas.contains("seguridad_usuario"), "falta seguridad_usuario en " + tablas);
        assertTrue(tablas.contains("flyway_schema_history"),
                "flyway_schema_history debe existir en MySQL tambien");
        assertTrue(dao.cuentaRegistros("seguridad_usuario") >= 5,
                "V1.2.0 siembra 5 usuarios de seguridad");
        assertTrue(dao.conteoPorTabla().stream()
                        .noneMatch(c -> "flyway_schema_history".equalsIgnoreCase(c.tabla())),
                "la tabla tecnica de Flyway no va en los conteos de negocio");
    }

    @Test
    @DisplayName("TC-D34-MY-03: el usuario de aplicacion (solo DML) no puede crear tablas: error 1142")
    void elUsuarioDmlNoPuedeHacerDdl() {
        SQLException ex = assertThrows(SQLException.class, () -> {
            try (Connection c = dataSource.getConnection();
                 Statement s = c.createStatement()) {
                s.execute("CREATE TABLE intentos_ddl_prohibido (id INT)");
            }
        });
        assertEquals(1142, ex.getErrorCode(),
                "MySQL debe responder 1142 (command denied): " + ex.getMessage());
        assertTrue(ex.getMessage().toLowerCase().contains("command denied"),
                "el mensaje real deberia hablar de permiso denegado: " + ex.getMessage());
    }
}
