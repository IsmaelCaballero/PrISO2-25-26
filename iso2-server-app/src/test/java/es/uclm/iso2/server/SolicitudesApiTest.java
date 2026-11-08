package es.uclm.iso2.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Puerta de calidad CFG-04 (Iter4/CDU06): RBAC de la matriz sobre /api/solicitudes y
 * las reglas del CDU (no repudio, anti-duplicado, prueba disponible) por HTTP contra la
 * aplicacion servidora completa (H2 en memoria con la semilla de la migracion V1.4.0:
 * pruebas de catalogo activas SEM-*).
 *
 * <p>Los pacientes que se usan aqui son ids propios (api-*) para no chocar con la
 * semilla ni entre tests: el contexto H2 se comparte entre clases.</p>
 *
 * <p>No sustituye los TC funcionales de {@code docs/pruebas/} (D28): cubre la
 * contractura de permisos y reglas exigida por CFG-04.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
class SolicitudesApiTest {

    private static final String RUTA_SOLICITUDES = "/api/solicitudes";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

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

    private static Stream<Arguments> rolesConSolicitudLeer() {
        return semillas().stream()
                .filter(s -> "ADMINISTRADOR".equals(s.rol()) || "MEDICO".equals(s.rol())
                        || "RECEPCIONISTA".equals(s.rol()))
                .map(Arguments::of);
    }

    private static Stream<Arguments> rolesSinSolicitudLeer() {
        return semillas().stream()
                .filter(s -> !("ADMINISTRADOR".equals(s.rol()) || "MEDICO".equals(s.rol())
                        || "RECEPCIONISTA".equals(s.rol())))
                .map(Arguments::of);
    }

    private static Stream<Arguments> rolesSinSolicitudEscribir() {
        return semillas().stream()
                .filter(s -> !("ADMINISTRADOR".equals(s.rol()) || "MEDICO".equals(s.rol())))
                .map(Arguments::of);
    }

    /**
     * Matriz CFG-04: los tres roles con solicitud:leer consultan su historial (200).
     */
    @ParameterizedTest
    @MethodSource("rolesConSolicitudLeer")
    void losRolesConPermisoLeenSolicitudes(Semilla semilla) throws Exception {
        String token = tokenDe(semilla.usuario(), semilla.password());
        mvc.perform(get(RUTA_SOLICITUDES + "/mias").header(AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
    }

    /**
     * TECNICO y CELADOR no tienen solicitud:leer: 403 con el codigo estable del contrato.
     */
    @ParameterizedTest
    @MethodSource("rolesSinSolicitudLeer")
    void losRolesSinPermisoNoLeenSolicitudes(Semilla semilla) throws Exception {
        String token = tokenDe(semilla.usuario(), semilla.password());
        mvc.perform(get(RUTA_SOLICITUDES + "/mias").header(AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("FORBIDDEN"));
    }

    /**
     * Los roles sin solicitud:escribir (recepcion, tecnico, celador) no pueden prescribir.
     */
    @ParameterizedTest
    @MethodSource("rolesSinSolicitudEscribir")
    void losRolesSinPermisoNoPrescriben(Semilla semilla) throws Exception {
        String token = tokenDe(semilla.usuario(), semilla.password());
        mvc.perform(post(RUTA_SOLICITUDES).header(AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(prescripcion("api-prohibido-" + semilla.rol().toLowerCase(),
                                "SEM-RX-TORAX", "tos persistente", "ORDINARIA", "AP")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("FORBIDDEN"));
    }

    /**
     * Regla CFG-04 "RF06.2 no repudio": el alta toma el autor del token (no del cuerpo),
     * sella huella SHA-256 y el endpoint de integridad la recalcula correcta.
     */
    @Test
    void elMedicoPrescribeYLaTrazabilidadSellaAutorYHuella() throws Exception {
        String token = tokenDe("medico", "iso2-medico");
        MvcResult creada = mvc.perform(post(RUTA_SOLICITUDES).header(AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(prescripcion("api-norepudio", "SEM-RM-RODILLA",
                                "dolor de rodilla tras caida", "PREFERENTE", "AP")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.solicitudId").exists())
                .andExpect(jsonPath("$.pacienteId").value("api-norepudio"))
                .andExpect(jsonPath("$.codigoPrueba").value("SEM-RM-RODILLA"))
                .andExpect(jsonPath("$.estado").value("creada"))
                .andExpect(jsonPath("$.fechaSolicitada").exists())
                .andReturn();
        String id = objectMapper.readTree(creada.getResponse().getContentAsString()).get("solicitudId").asText();

        mvc.perform(get(RUTA_SOLICITUDES + "/" + id + "/integridad").header(AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.autor").value("medico"))
                .andExpect(jsonPath("$.huella").value(org.hamcrest.Matchers.matchesPattern("^[0-9a-f]{64}$")))
                .andExpect(jsonPath("$.integridadOk").value(true));

        mvc.perform(get(RUTA_SOLICITUDES + "/mias").header(AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.solicitudId == '" + id + "')]").exists());
    }

    /**
     * Regla CFG-04 "TC duplicados" (RF06.4): el mismo medico no puede repetir la misma
     * prueba al mismo paciente mientras la anterior esta activa: 409 con codigo estable.
     */
    @Test
    void laSolicitudDuplicadaDevuelve409ConCodigoEstable() throws Exception {
        String token = tokenDe("medico", "iso2-medico");
        mvc.perform(post(RUTA_SOLICITUDES).header(AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(prescripcion("api-dup", "SEM-RX-TORAX",
                                "control de neumonia", "ORDINARIA", "AP")))
                .andExpect(status().isCreated());
        mvc.perform(post(RUTA_SOLICITUDES).header(AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(prescripcion("api-dup", "SEM-RX-TORAX",
                                "mismo control repetido", "URGENTE", "URGENCIAS")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CONFLICTO_SOLICITUD_DUPLICADA"));
    }

    /**
     * Prueba inexistente o inactiva en el catalogo: 409 CONFLICTO_PRUEBA_NO_DISPONIBLE.
     */
    @Test
    void laPruebaNoDisponibleDevuelve409ConCodigoEstable() throws Exception {
        String token = tokenDe("medico", "iso2-medico");
        mvc.perform(post(RUTA_SOLICITUDES).header(AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(prescripcion("api-sin-prueba", "PRUEBA-QUE-NO-EXISTE",
                                "torticolis", "ORDINARIA", "AP")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CONFLICTO_PRUEBA_NO_DISPONIBLE"));
    }

    /**
     * Validaciones RF06.1 por HTTP: prioridad desconocida o indicacion en blanco son 400
     * con el codigo DATOS_INVALIDOS.
     */
    @Test
    void losDatosInvalidosTienenCodigoEstable() throws Exception {
        String token = tokenDe("medico", "iso2-medico");
        mvc.perform(post(RUTA_SOLICITUDES).header(AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(prescripcion("api-malos", "SEM-RX-TORAX",
                                "dolor", "MAXIMA", "AP")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DATOS_INVALIDOS"));
        mvc.perform(post(RUTA_SOLICITUDES).header(AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(prescripcion("api-malos", "SEM-RX-TORAX", "   ", "ORDINARIA", "AP")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DATOS_INVALIDOS"));
    }

    /**
     * Solicitud inexistente: detalle e integridad responden 404 con el codigo
     * SOLICITUD_NO_ENCONTRADA del contrato.
     */
    @Test
    void laSolicitudInexistenteDevuelve404() throws Exception {
        String token = tokenDe("recepcionista", "iso2-rec");
        mvc.perform(get(RUTA_SOLICITUDES + "/00000000-0000-0000-0000-000000000000")
                        .header(AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("SOLICITUD_NO_ENCONTRADA"));
        mvc.perform(get(RUTA_SOLICITUDES + "/00000000-0000-0000-0000-000000000000/integridad")
                        .header(AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("SOLICITUD_NO_ENCONTRADA"));
    }

    /**
     * Sin token: 401 con el codigo estable del contrato (tambien en la lectura).
     */
    @Test
    void anonimoRecibe401EnSolicitudes() throws Exception {
        mvc.perform(get(RUTA_SOLICITUDES + "/mias"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("AUTENTICACION_REQUERIDA"));
        mvc.perform(post(RUTA_SOLICITUDES).contentType(MediaType.APPLICATION_JSON)
                        .content(prescripcion("api-anonimo", "SEM-RX-TORAX", "tos", "ORDINARIA", "AP")))
                .andExpect(status().isUnauthorized());
    }

    private static String prescripcion(String paciente, String prueba, String indicacion,
                                       String prioridad, String origen) {
        return """
                {"pacienteId":"%s","codigoPrueba":"%s","indicacionClinica":"%s",
                 "prioridad":"%s","origen":"%s"}
                """.formatted(paciente, prueba, indicacion, prioridad, origen);
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
