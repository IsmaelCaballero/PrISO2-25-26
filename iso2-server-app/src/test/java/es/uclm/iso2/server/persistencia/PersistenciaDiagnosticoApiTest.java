package es.uclm.iso2.server.persistencia;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Puerta de calidad D34 (Iter5/CFG-05) sobre H2: RBAC de {@code GET /api/persistencia/diagnostico}
 * (solo ADMINISTRADOR), deteccion del agente H2 y lectura de {@code INFORMATION_SCHEMA}
 * contra la semilla real de las migraciones V1.1.0-V1.4.0.
 *
 * <p>Contractura de permisos al estilo CFG-04 (no sustituye los TC humanos de
 * {@code docs/pruebas/}, D28). La variante MySQL se cubre aparte en
 * {@code PersistenciaMysqlTestcontainersTest}.</p>
 *
 * <p>Las comparaciones de tablas van en minusculas porque H2 2.x guarda los nombres
 * en mayusculas en {@code INFORMATION_SCHEMA} y MySQL los deja como se crearon.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
class PersistenciaDiagnosticoApiTest {

    private static final String RUTA_DIAGNOSTICO = "/api/persistencia/diagnostico";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DiagnosticoPersistenciaDao dao;

    private record Semilla(String usuario, String password, String rol) {
    }

    private static List<Semilla> semillas() {
        return List.of(
                new Semilla("admin", "iso2-admin", "ADMINISTRADOR"),
                new Semilla("medico", "iso2-medico", "MEDICO"),
                new Semilla("tecnico", "iso2-tecnico", "TECNICO"),
                new Semilla("celador", "iso2-ses", "CELADOR"),
                new Semilla("recepcionista", "iso2-rec", "RECEPCIONISTA"));
    }

    private static Stream<Arguments> rolesDistintosDeAdministrador() {
        return semillas().stream()
                .filter(s -> !"ADMINISTRADOR".equals(s.rol()))
                .map(Arguments::of);
    }

    @Test
    @DisplayName("TC-D34-H2-01: el admin pide diagnostico y ve H2 con las tablas versionadas por Flyway")
    void elAdministradorObtieneElDiagnosticoDeH2() throws Exception {
        JsonNode cuerpo = diagnosticoComoAdmin();
        assertEquals("H2", cuerpo.get("sgbd").asText());
        assertEquals("PUBLIC", cuerpo.get("esquema").asText());
        List<String> tablas = nombresDe(cuerpo.get("tablas"));
        assertTrue(tablas.contains("catalogo_prueba"), "falta catalogo_prueba en " + tablas);
        assertTrue(tablas.contains("seguridad_usuario"), "falta seguridad_usuario en " + tablas);
        assertTrue(tablas.contains("solicitud"), "falta solicitud en " + tablas);
        assertTrue(tablas.contains("flyway_schema_history"),
                "la tabla tecnica de Flyway si debe salir en tablas");
        assertTrue(conteo(cuerpo, "seguridad_usuario") >= 5,
                "la semilla de seguridad deja al menos 5 usuarios");
    }

    @Test
    @DisplayName("TC-D34-H2-02: el diagnostico excluye flyway_schema_history de los conteos de negocio")
    void elDiagnosticoOcultaLaTablaTecnicaDeFlyway() throws Exception {
        JsonNode cuerpo = diagnosticoComoAdmin();
        assertFalse(nombresDe(cuerpo.get("conteosPorTabla")).contains("flyway_schema_history"),
                "flyway_schema_history no debe aparecer en conteosPorTabla");
    }

    @ParameterizedTest
    @MethodSource("rolesDistintosDeAdministrador")
    @DisplayName("TC-D34-H2-03/04/05/06: los otros cuatro roles reciben 403 FORBIDDEN")
    void losRolesNoAdminNoVenElDiagnostico(Semilla semilla) throws Exception {
        String token = tokenDe(semilla.usuario(), semilla.password());
        mvc.perform(get(RUTA_DIAGNOSTICO).header(AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("FORBIDDEN"));
    }

    @Test
    @DisplayName("TC-D34-H2-07: sin token el diagnostico responde 401 AUTENTICACION_REQUERIDA")
    void sinTokenNoHayDiagnostico() throws Exception {
        mvc.perform(get(RUTA_DIAGNOSTICO))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("AUTENTICACION_REQUERIDA"));
    }

    @Test
    @DisplayName("TC-D34-H2-08: el DAO JDBC rechaza nombres de tabla que no son identificadores simples")
    void elDaoRechazaIdentificadoresPeligrosos() {
        // El post-procesador de Traduccion de excepciones de Spring (por el @Repository)
        // envuelve el IllegalArgumentException en un DataAccessException: se comprueba la causa.
        DataAccessException ex = assertThrows(DataAccessException.class,
                () -> dao.cuentaRegistros("SEGURIDAD_USUARIO; DROP TABLE solicitud"));
        assertEquals(true, ex.getCause() instanceof IllegalArgumentException);
        assertThrows(DataAccessException.class, () -> dao.cuentaRegistros(""));
        assertThrows(DataAccessException.class, () -> dao.cuentaRegistros(null));
    }

    @Test
    @DisplayName("TC-D34-H2-09: el DAO lee el esquema real y cuenta una tabla sembrada por Flyway")
    void elAgenteYelDaoLeenElEsquemaReal() throws SQLException {
        List<String> tablas = dao.tablasDelEsquema();
        assertTrue(tablas.stream().anyMatch("catalogo_prueba"::equalsIgnoreCase));
        assertTrue(tablas.stream().anyMatch("agenda_franja"::equalsIgnoreCase));
        assertTrue(dao.cuentaRegistros("agenda_franja") > 0,
                "V1.3.0 siembra franjas: el recuento no puede ser 0");
    }

    private JsonNode diagnosticoComoAdmin() throws Exception {
        String token = tokenDe("admin", "iso2-admin");
        MvcResult resultado = mvc.perform(get(RUTA_DIAGNOSTICO).header(AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(resultado.getResponse().getContentAsString());
    }

    private static List<String> nombresDe(JsonNode array) {
        List<String> nombres = new ArrayList<>();
        if (array != null && array.isArray()) {
            for (JsonNode elemento : array) {
                JsonNode tabla = elemento.isObject() ? elemento.get("tabla") : elemento;
                nombres.add(tabla.asText().toLowerCase(Locale.ROOT));
            }
        }
        return nombres;
    }

    private static long conteo(JsonNode cuerpo, String tabla) {
        for (JsonNode entrada : cuerpo.get("conteosPorTabla")) {
            if (tabla.equalsIgnoreCase(entrada.get("tabla").asText())) {
                return entrada.get("registros").asLong();
            }
        }
        throw new AssertionError("sin conteo para la tabla " + tabla);
    }

    private String tokenDe(String usuario, String password) throws Exception {
        MvcResult resultado = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"%s\",\"password\":\"%s\"}".formatted(usuario, password)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(resultado.getResponse().getContentAsString()).get("token").asText();
    }
}
