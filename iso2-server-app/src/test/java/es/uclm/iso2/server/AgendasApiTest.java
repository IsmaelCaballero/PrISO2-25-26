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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Puerta de calidad CFG-03 (Iter3/CDU13): RBAC de la matriz sobre /api/agendas y las
 * reglas de solape/festivo por HTTP contra la aplicacion servidora completa (H2 en
 * memoria con la semilla de la migracion V1.3.0: agenda de medico y tecnico).
 *
 * <p>Los profesionales que se crean aqui usan ids propios (api-*) para no chocar con la
 * semilla ni entre tests: el contexto H2 se comparte entre clases.</p>
 *
 * <p>No sustituye los TC funcionales de {@code docs/pruebas/} (D28): cubre la
 * contractura de solapes/festivos y permisos exigida por CFG-03.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
class AgendasApiTest {

    private static final String RUTA_AGENDAS = "/api/agendas";

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

    private static Stream<Arguments> rolesConAgendaLeer() {
        return semillas().stream()
                .filter(s -> !"CELADOR".equals(s.rol()))
                .map(Arguments::of);
    }

    private static Stream<Arguments> rolesConAgendaEscribir() {
        return semillas().stream()
                .filter(s -> "ADMINISTRADOR".equals(s.rol()) || "RECEPCIONISTA".equals(s.rol()))
                .map(Arguments::of);
    }

    private static Stream<Arguments> rolesSinAgendaEscribir() {
        return semillas().stream()
                .filter(s -> !("ADMINISTRADOR".equals(s.rol()) || "RECEPCIONISTA".equals(s.rol())))
                .map(Arguments::of);
    }

    /**
     * Matriz CFG-03: los cuatro roles con agenda:leer leen las agendas (200); el
     * CELADOR, que no tiene el permiso, recibe 403 con el codigo estable del contrato.
     */
    @ParameterizedTest
    @MethodSource("rolesConAgendaLeer")
    void losRolesConPermisoLeenAgendas(Semilla semilla) throws Exception {
        String token = tokenDe(semilla.usuario(), semilla.password());
        mvc.perform(get(RUTA_AGENDAS).header(AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
        mvc.perform(get(RUTA_AGENDAS + "/medico").header(AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profesionalId").value("medico"));
    }

    @Test
    void elCeladorNoPuedeNiLeerAgendas() throws Exception {
        String token = tokenDe("celador", "iso2-ses");
        mvc.perform(get(RUTA_AGENDAS).header(AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("FORBIDDEN"));
    }

    /**
     * Semilla V1.3.0 visible por API: la agenda del medico tiene sus tres franjas de
     * noviembre y el contrato baseline franjasLibres responde con el rango pedido.
     */
    @Test
    void laSemillaDeDesarrolloSePuedeConsultar() throws Exception {
        String token = tokenDe("medico", "iso2-medico");
        mvc.perform(get(RUTA_AGENDAS + "/medico").header(AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.franjas.length()").value(3))
                .andExpect(jsonPath("$.festivos.length()").value(1));
        mvc.perform(get(RUTA_AGENDAS + "/medico/libres")
                        .param("desde", "2026-11-09").param("hasta", "2026-11-13")
                        .header(AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    /**
     * Actor Administrativo del CDU13: ADMINISTRADOR y RECEPCIONISTA pueden dar alta de
     * agenda, franja y festivo (201) y eliminar la franja (204).
     */
    @ParameterizedTest
    @MethodSource("rolesConAgendaEscribir")
    void administracionYRecepcionGestionanAgendas(Semilla semilla) throws Exception {
        String token = tokenDe(semilla.usuario(), semilla.password());
        String profesional = "api-alta-" + semilla.rol().toLowerCase();

        mvc.perform(post(RUTA_AGENDAS).header(AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"profesionalId\":\"" + profesional + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.profesionalId").value(profesional));

        MvcResult franja = mvc.perform(post(RUTA_AGENDAS + "/" + profesional + "/franjas")
                        .header(AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(franja("2026-12-01", "10:00", "12:00")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();
        long franjaId = objectMapper.readTree(franja.getResponse().getContentAsString()).get("id").asLong();

        mvc.perform(post(RUTA_AGENDAS + "/" + profesional + "/festivos")
                        .header(AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dia\":\"2026-12-02\",\"motivo\":\"asunto propio\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.motivo").value("asunto propio"));

        mvc.perform(delete(RUTA_AGENDAS + "/" + profesional + "/franjas/" + franjaId)
                        .header(AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    /**
     * MEDICO, TECNICO y CELADOR no pueden escribir en agendas: 403 en alta de agenda y
     * en alta de franja.
     */
    @ParameterizedTest
    @MethodSource("rolesSinAgendaEscribir")
    void losDemasRolesNoEscribenAgendas(Semilla semilla) throws Exception {
        String token = tokenDe(semilla.usuario(), semilla.password());
        mvc.perform(post(RUTA_AGENDAS).header(AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"profesionalId\":\"api-prohibido-" + semilla.rol().toLowerCase() + "\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("FORBIDDEN"));
        mvc.perform(post(RUTA_AGENDAS + "/medico/franjas").header(AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(franja("2026-12-03", "09:00", "11:00")))
                .andExpect(status().isForbidden());
    }

    /**
     * Sin token: 401 con el codigo estable del contrato (tambien en la lectura).
     */
    @Test
    void anonimoRecibe401EnAgendas() throws Exception {
        mvc.perform(get(RUTA_AGENDAS))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("AUTENTICACION_REQUERIDA"));
        mvc.perform(post(RUTA_AGENDAS).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"profesionalId\":\"api-anonimo\"}"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Regla CFG-03 "TC solapes": franja que pisa a otra del mismo dia y hora → 409 con
     * codigo CONFLICTO_SOLAPE (los bordes que se tocan se crean sin problema).
     */
    @Test
    void elSolapeDevuelve409ConCodigoEstable() throws Exception {
        String token = tokenDe("recepcionista", "iso2-rec");
        String profesional = "api-solape";
        crearAgenda(token, profesional);

        mvc.perform(post(RUTA_AGENDAS + "/" + profesional + "/franjas").header(AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(franja("2026-12-07", "10:00", "12:00")))
                .andExpect(status().isCreated());
        mvc.perform(post(RUTA_AGENDAS + "/" + profesional + "/franjas").header(AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(franja("2026-12-07", "12:00", "14:00")))
                .andExpect(status().isCreated());
        mvc.perform(post(RUTA_AGENDAS + "/" + profesional + "/franjas").header(AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(franja("2026-12-07", "11:30", "13:00")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CONFLICTO_SOLAPE"));
    }

    /**
     * Regla CFG-03 "TC festivos": franja en dia festivo → 409 CONFLICTO_FESTIVO; y a la
     * inversa, declarar festivo un dia con franjas también es CONFLICTO_FESTIVO.
     */
    @Test
    void losFestivosMandanSobreLasFranjas() throws Exception {
        String token = tokenDe("recepcionista", "iso2-rec");
        crearAgenda(token, "api-festivo-manda");

        mvc.perform(post(RUTA_AGENDAS + "/api-festivo-manda/festivos").header(AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dia\":\"2026-12-08\",\"motivo\":\"dia natural\"}"))
                .andExpect(status().isCreated());
        mvc.perform(post(RUTA_AGENDAS + "/api-festivo-manda/franjas").header(AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(franja("2026-12-08", "08:00", "15:00")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CONFLICTO_FESTIVO"));

        mvc.perform(post(RUTA_AGENDAS + "/api-festivo-manda/franjas").header(AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(franja("2026-12-09", "08:00", "15:00")))
                .andExpect(status().isCreated());
        mvc.perform(post(RUTA_AGENDAS + "/api-festivo-manda/festivos").header(AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dia\":\"2026-12-09\",\"motivo\":\"vacaciones\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CONFLICTO_FESTIVO"));
    }

    /**
     * Validaciones del CDU: franja con inicio posterior o igual al fin → 400
     * DATOS_INVALIDOS; agenda ya existente → 409 CONFLICTO_AGENDA_EXISTENTE.
     */
    @Test
    void datosInvalidosYAgendaDuplicadaTienenCodigoEstable() throws Exception {
        String token = tokenDe("admin", "iso2-admin");
        crearAgenda(token, "api-datos-malos");

        mvc.perform(post(RUTA_AGENDAS + "/api-datos-malos/franjas").header(AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(franja("2026-12-10", "12:00", "12:00")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DATOS_INVALIDOS"));
        mvc.perform(post(RUTA_AGENDAS + "/api-datos-malos/franjas").header(AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(franja("2026-12-10", "15:00", "08:00")))
                .andExpect(status().isBadRequest());

        mvc.perform(post(RUTA_AGENDAS).header(AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"profesionalId\":\"api-datos-malos\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CONFLICTO_AGENDA_EXISTENTE"));
    }

    /**
     * Profesional sin agenda: el detalle responde 404 y el alta de franja 404 con el
     * codigo AGENDA_NO_ENCONTRADA del contrato.
     */
    @Test
    void elProfesionalSinAgendaDevuelve404() throws Exception {
        String token = tokenDe("recepcionista", "iso2-rec");
        mvc.perform(get(RUTA_AGENDAS + "/api-inexistente").header(AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNotFound());
        mvc.perform(post(RUTA_AGENDAS + "/api-inexistente/franjas").header(AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(franja("2026-12-11", "08:00", "15:00")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("AGENDA_NO_ENCONTRADA"));
    }

    private void crearAgenda(String token, String profesional) throws Exception {
        mvc.perform(post(RUTA_AGENDAS).header(AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"profesionalId\":\"" + profesional + "\"}"))
                .andExpect(status().isCreated());
    }

    private static String franja(String dia, String inicio, String fin) {
        return "{\"dia\":\"%s\",\"inicio\":\"%s\",\"fin\":\"%s\"}".formatted(dia, inicio, fin);
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
