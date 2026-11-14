package es.uclm.iso2.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
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
 * Puerta de calidad CFG-05 (Iter5/CDU12): RBAC de la matriz sobre /api/citas y las reglas
 * de hueco/franja/transicion/plazo por HTTP contra la aplicacion servidora completa (H2
 * en memoria con la semilla de la migracion V1.5.0: cinco citas CITA-SEM-*).
 *
 * <p>Como el contexto H2 se comparte entre clases de esta aplicacion, los profesionales,
 * solicitudes y citas que se crean aqui usan ids propios (api-*) y franjas lejanas en el
 * tiempo (junio de 2027). Las franjas lejanas son lo que hace determinista el plazo de
 * cancelacion del catalogo (48 h para SEM-RM-RODILLA) contra el reloj real, y los ids
 * con prefijo api evitan tocar la semilla SEM y CITA-SEM que otras clases de prueba leen.</p>
 *
 * <p>El lote {@code POST /api/citas/expirar-vencidas} no se prueba por HTTP aqui: cualquier
 * corte posterior a noviembre de 2026 expira las citas semilla del contexto compartido y
 * envenena al resto de clases de prueba. Esa regla va cubierta en el modulo iso2-citas por
 * {@code CitaServicioTest} y {@code MaquinaEstadosCitasTest}.</p>
 *
 * <p>No sustituye los TC funcionales de {@code docs/pruebas/} (D28): cubre la contractura
 * de RBAC, hueco/franja/transiciones y codigos estables exigida por CFG-05.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
class CitasApiTest {

    private static final String RUTA_CITAS = "/api/citas";

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

    /** Roles con cita:leer en la matriz de la decision D29/CFG-05: admin, medico y recepcion. */
    private static Stream<Arguments> rolesConCitaLeer() {
        return semillas().stream()
                .filter(s -> !("CELADOR".equals(s.rol()) || "TECNICO".equals(s.rol())))
                .map(Arguments::of);
    }

    /** Roles con cita:escribir: el actor Administrativo del CDU12 (recepcion) y el admin. */
    private static Stream<Arguments> rolesConCitaEscribir() {
        return semillas().stream()
                .filter(s -> "ADMINISTRADOR".equals(s.rol()) || "RECEPCIONISTA".equals(s.rol()))
                .map(Arguments::of);
    }

    /** MEDICO (solo lee citas), TECNICO y CELADOR quedan fuera de la escritura de citas. */
    private static Stream<Arguments> rolesSinCitaEscribir() {
        return semillas().stream()
                .filter(s -> !("ADMINISTRADOR".equals(s.rol()) || "RECEPCIONISTA".equals(s.rol())))
                .map(Arguments::of);
    }

    /**
     * Matriz CFG-05: los tres roles con cita:leer listan y ven el detalle (200, con la
     * semilla CITA-SEM-001 en estado confirmada); TECNICO y CELADOR, sin el permiso,
     * reciben 403 con el codigo estable del contrato.
     */
    @ParameterizedTest
    @MethodSource("rolesConCitaLeer")
    void losRolesConPermisoLeenCitas(Semilla semilla) throws Exception {
        String token = tokenDe(semilla.usuario(), semilla.password());
        mvc.perform(get(RUTA_CITAS).header(AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
        mvc.perform(get(RUTA_CITAS + "/CITA-SEM-001").header(AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.citaId").value("CITA-SEM-001"))
                .andExpect(jsonPath("$.estado").value("confirmada"))
                .andExpect(jsonPath("$.profesionalId").value("medico"));
    }

    @ParameterizedTest
    @CsvSource({"TECNICO, tecnico, iso2-tecnico", "CELADOR, celador, iso2-ses"})
    void losRolesSinCitaLeerReciben403(String rol, String usuario, String password) throws Exception {
        String token = tokenDe(usuario, password);
        mvc.perform(get(RUTA_CITAS).header(AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("FORBIDDEN"));
        mvc.perform(get(RUTA_CITAS + "/CITA-SEM-001").header(AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    /**
     * Sin token: 401 con el codigo estable del contrato, tambien en el motor de huecos.
     */
    @Test
    void anonimoRecibe401EnCitas() throws Exception {
        mvc.perform(get(RUTA_CITAS))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("AUTENTICACION_REQUERIDA"));
        mvc.perform(get(RUTA_CITAS + "/huecos").param("profesionalId", "medico")
                        .param("desde", "2027-06-01").param("hasta", "2027-06-05"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post(RUTA_CITAS).contentType(MediaType.APPLICATION_JSON)
                        .content(cita("medico", "2027-06-01T09:00:00", 30)))
                .andExpect(status().isUnauthorized());
    }

    /**
     * UC12.2 a UC12.6 por HTTP: agendar sobre solicitud creada (201, la solicitud queda
     * citada), confirmar, reprogramar a otro hueco de la misma franja (200 con el inicio
     * nuevo), re-confirmar contra la maquina de estados (409 CONFLICTO_TRANSICION_INVALIDA)
     * y realizar (200). El ciclo completo lo ejecutan los dos roles con cita:escribir.
     */
    @ParameterizedTest
    @MethodSource("rolesConCitaEscribir")
    void elCicloCompletoDeCitaFuncionaPorHttp(Semilla semilla) throws Exception {
        String rol = semilla.rol().toLowerCase();
        String profesional = "api-citas-ciclo-" + rol;
        String tokenGestor = tokenDe(semilla.usuario(), semilla.password());
        // La prescripcion la hace un medico (solicitud:escribir), igual que en produccion.
        String tokenMedico = tokenDe("medico", "iso2-medico");
        String solicitudId = crearSolicitud(tokenMedico, "api-paciente-" + rol);

        crearAgenda(tokenGestor, profesional);
        anyadirFranja(tokenGestor, profesional, "2027-06-01", "08:00", "12:00");

        MvcResult creada = mvc.perform(post(RUTA_CITAS).header(AUTHORIZATION, "Bearer " + tokenGestor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(citaConSolicitud(solicitudId, profesional, "2027-06-01T09:00:00", 30)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("programada"))
                .andExpect(jsonPath("$.solicitudId").value(solicitudId))
                .andExpect(jsonPath("$.fin").value("2027-06-01T09:30:00"))
                .andReturn();
        String citaId = objectMapper.readTree(creada.getResponse().getContentAsString()).get("citaId").asText();

        mvc.perform(post(RUTA_CITAS + "/" + citaId + "/confirmar").header(AUTHORIZATION, "Bearer " + tokenGestor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("confirmada"));

        mvc.perform(post(RUTA_CITAS + "/" + citaId + "/reprogramar").header(AUTHORIZATION, "Bearer " + tokenGestor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"inicio\":\"2027-06-01T10:30:00\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inicio").value("2027-06-01T10:30:00"))
                .andExpect(jsonPath("$.fin").value("2027-06-01T11:00:00"));

        mvc.perform(post(RUTA_CITAS + "/" + citaId + "/confirmar").header(AUTHORIZATION, "Bearer " + tokenGestor))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CONFLICTO_TRANSICION_INVALIDA"));

        mvc.perform(post(RUTA_CITAS + "/" + citaId + "/realizar").header(AUTHORIZATION, "Bearer " + tokenGestor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("realizada"));

        // El hueco de la cita realizada queda libre (UC12.6): la franja vuelve entera.
        mvc.perform(get(RUTA_CITAS + "/huecos").header(AUTHORIZATION, "Bearer " + tokenGestor)
                        .param("profesionalId", profesional).param("desde", "2027-06-01").param("hasta", "2027-06-01")
                        .param("duracionMinutos", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.inicio == '2027-06-01T08:00:00')]").exists())
                .andExpect(jsonPath("$[?(@.inicio == '2027-06-01T10:30:00')]").doesNotExist());
    }

    /**
     * UC12.5 por HTTP: cancelar con motivo libera el hueco (200 con el motivo recogido);
     * reintentar la cancelacion devuelve 409 y, como fija D36, gana la transicion invalida
     * sobre el plazo de cancelacion aunque la cita cancelada ya este dentro del plazo de
     * 48 h de SEM-RM-RODILLA. Sin motivo (en blanco), 400 DATOS_INVALIDOS.
     */
    @Test
    void cancelarConMotivoYLuegoLaTransicionInvalidaGanaAlPlazo() throws Exception {
        String token = tokenDe("recepcionista", "iso2-rec");
        String profesional = "api-citas-cancela";
        String solicitudId = crearSolicitud(tokenDe("medico", "iso2-medico"), "api-paciente-cancela");
        crearAgenda(token, profesional);
        anyadirFranja(token, profesional, "2027-06-02", "08:00", "12:00");

        MvcResult creada = mvc.perform(post(RUTA_CITAS).header(AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(citaConSolicitud(solicitudId, profesional, "2027-06-02T09:00:00", 30)))
                .andExpect(status().isCreated())
                .andReturn();
        String citaId = objectMapper.readTree(creada.getResponse().getContentAsString()).get("citaId").asText();

        mvc.perform(post(RUTA_CITAS + "/" + citaId + "/cancelar").header(AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"el paciente aviso de ausencia\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("cancelada"));
        mvc.perform(get(RUTA_CITAS + "/" + citaId).header(AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.motivoCancelacion").value("el paciente aviso de ausencia"));

        // D36: sobre una cita ya cancelada responde TRANSICION_INVALIDA, no PLAZO_CANCELACION.
        mvc.perform(post(RUTA_CITAS + "/" + citaId + "/cancelar").header(AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"segundo intento\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CONFLICTO_TRANSICION_INVALIDA"));

        mvc.perform(post(RUTA_CITAS + "/" + citaId + "/cancelar").header(AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DATOS_INVALIDOS"));

        // El hueco cancelado vuelve a la bolsa: la franja aparece de nuevo entera.
        mvc.perform(get(RUTA_CITAS + "/huecos").header(AUTHORIZATION, "Bearer " + token)
                        .param("profesionalId", profesional).param("desde", "2027-06-02").param("hasta", "2027-06-02")
                        .param("duracionMinutos", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].inicio").value("2027-06-02T08:00:00"))
                .andExpect(jsonPath("$[0].duracionMinutos").value(240));
    }

    /**
     * Regla anti-concurrencia por HTTP (D36): el mismo hueco pedido dos veces se rechaza
     * con 409 CONFLICTO_HUECO_OCUPADO, y tambien una cita que solapa a otra aunque el
     * inicio no coincida.
     */
    @Test
    void elMismoHuecoYUnSolapeDevuelven409ConCodigoEstable() throws Exception {
        String token = tokenDe("recepcionista", "iso2-rec");
        String profesional = "api-citas-dup";
        crearAgenda(token, profesional);
        anyadirFranja(token, profesional, "2027-06-03", "08:00", "12:00");

        mvc.perform(post(RUTA_CITAS).header(AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cita(profesional, "2027-06-03T10:00:00", 30)))
                .andExpect(status().isCreated());
        mvc.perform(post(RUTA_CITAS).header(AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cita(profesional, "2027-06-03T10:00:00", 45)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CONFLICTO_HUECO_OCUPADO"));
        mvc.perform(post(RUTA_CITAS).header(AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cita(profesional, "2027-06-03T10:15:00", 30)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CONFLICTO_HUECO_OCUPADO"));
    }

    /**
     * D36: un profesional sin agenda y una agenda sin franja ese dia dan el mismo codigo
     * CONFLICTO_FUERA_DE_FRANJA (el profesional sin agenda responde FUERA_DE_FRANJA, no
     * 404: aqui manda la regla de la cita).
     */
    @Test
    void elProfesionalSinAgendaYEseDiaSinFranjaQuedanFueraDeFranja() throws Exception {
        String token = tokenDe("recepcionista", "iso2-rec");
        mvc.perform(post(RUTA_CITAS).header(AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cita("api-citas-sin-agenda", "2027-06-01T09:00:00", 30)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CONFLICTO_FUERA_DE_FRANJA"));

        String profesional = "api-citas-sin-franja";
        crearAgenda(token, profesional);
        mvc.perform(post(RUTA_CITAS).header(AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cita(profesional, "2027-06-01T09:00:00", 30)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CONFLICTO_FUERA_DE_FRANJA"));
    }

    /**
     * Reglas sobre la solicitud de origen (D36): una solicitud desconocida o ya citada se
     * rechaza con 409 CONFLICTO_SOLICITUD_NO_APTA; al cancelar la cita la solicitud vuelve
     * a creada y vuelve a ser apta; y una prueba con requiereCita=false no bloquea la cita
     * (el CDU12 no impone su politica, manda el catalogo).
     */
    @Test
    void laSolicitudNoAptaSeRechazaYAlCancelarLaCitaReabreLaSolicitud() throws Exception {
        String token = tokenDe("recepcionista", "iso2-rec");
        String tokenMedico = tokenDe("medico", "iso2-medico");
        String profesional = "api-citas-solicitud";
        crearAgenda(token, profesional);
        anyadirFranja(token, profesional, "2027-06-04", "08:00", "12:00");

        mvc.perform(post(RUTA_CITAS).header(AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(citaConSolicitud("api-solicitud-que-no-existe", profesional,
                                "2027-06-04T09:00:00", 30)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CONFLICTO_SOLICITUD_NO_APTA"));

        String solicitudId = crearSolicitud(tokenMedico, "api-paciente-ya-citada");
        MvcResult primera = mvc.perform(post(RUTA_CITAS).header(AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(citaConSolicitud(solicitudId, profesional, "2027-06-04T09:00:00", 30)))
                .andExpect(status().isCreated())
                .andReturn();
        String citaId = objectMapper.readTree(primera.getResponse().getContentAsString()).get("citaId").asText();

        mvc.perform(post(RUTA_CITAS).header(AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(citaConSolicitud(solicitudId, profesional, "2027-06-04T10:00:00", 30)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CONFLICTO_SOLICITUD_NO_APTA"));

        mvc.perform(post(RUTA_CITAS + "/" + citaId + "/cancelar").header(AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"se reorganiza la agenda\"}"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/solicitudes/" + solicitudId).header(AUTHORIZATION, "Bearer " + tokenMedico))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("creada"));
        mvc.perform(post(RUTA_CITAS).header(AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(citaConSolicitud(solicitudId, profesional, "2027-06-04T10:00:00", 30)))
                .andExpect(status().isCreated());

        // Prueba del catalogo con requiereCita=false: agendar una cita directa no se bloquea.
        mvc.perform(post(RUTA_CITAS).header(AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cita(profesional, "2027-06-04T11:00:00", 10)))
                .andExpect(status().isCreated());
    }

    /**
     * Cita directa (sin solicitud) sin duracion en el cuerpo: no hay catalogo del que
     * sacarla, asi que 400 DATOS_INVALIDOS.
     */
    @Test
    void laCitaDirectaSinDuracionSeRechazaCon400() throws Exception {
        String token = tokenDe("recepcionista", "iso2-rec");
        mvc.perform(post(RUTA_CITAS).header(AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cita("api-citas-sin-duracion", "2027-06-05T09:00:00", 0)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DATOS_INVALIDOS"));
    }

    /**
     * Filtros de la pantalla de recepcion (UC12.8): un estado que no es de la maquina o un
     * rango invertido devuelven 400 DATOS_INVALIDOS; y una cita inexistente 404 con
     * CITA_NO_ENCONTRADA.
     */
    @Test
    void elListadoValidaEstadoIRangoYElDetalle404TieneCodigo() throws Exception {
        String token = tokenDe("recepcionista", "iso2-rec");
        mvc.perform(get(RUTA_CITAS).header(AUTHORIZATION, "Bearer " + token).param("estado", "noexiste"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DATOS_INVALIDOS"));
        mvc.perform(get(RUTA_CITAS).header(AUTHORIZATION, "Bearer " + token)
                        .param("desde", "2027-06-10T00:00:00").param("hasta", "2027-06-01T00:00:00"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DATOS_INVALIDOS"));
        mvc.perform(get(RUTA_CITAS + "/api-cita-que-no-existe").header(AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("CITA_NO_ENCONTRADA"));

        mvc.perform(get(RUTA_CITAS).header(AUTHORIZATION, "Bearer " + token)
                        .param("profesionalId", "medico").param("estado", "confirmada")
                        .param("desde", "2026-11-01T00:00:00").param("hasta", "2026-12-01T00:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.citaId == 'CITA-SEM-001')]").exists());
    }

    /**
     * RBAC de escritura CFG-05: MEDICO, TECNICO y CELADOR reciben 403 al agendar,
     * confirmar, reprogramar o cancelar (cita:escribir es solo de ADMINISTRADOR y
     * RECEPCIONISTA). El medico, con cita:leer, si puede listar.
     */
    @ParameterizedTest
    @CsvSource({"MEDICO, medico, iso2-medico", "TECNICO, tecnico, iso2-tecnico", "CELADOR, celador, iso2-ses"})
    void losRolesSinCitaEscribirNoGestionanCitas(String rol, String usuario, String password) throws Exception {
        String token = tokenDe(usuario, password);
        mvc.perform(post(RUTA_CITAS).header(AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cita("medico", "2027-06-01T09:00:00", 30)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("FORBIDDEN"));
        mvc.perform(post(RUTA_CITAS + "/CITA-SEM-001/confirmar").header(AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden());
        mvc.perform(post(RUTA_CITAS + "/CITA-SEM-002/reprogramar").header(AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"inicio\":\"2027-06-01T09:00:00\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(post(RUTA_CITAS + "/CITA-SEM-002/cancelar").header(AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"sin permiso\"}"))
                .andExpect(status().isForbidden());
        if ("MEDICO".equals(rol)) {
            mvc.perform(get(RUTA_CITAS).header(AUTHORIZATION, "Bearer " + token))
                    .andExpect(status().isOk());
        } else {
            mvc.perform(get(RUTA_CITAS).header(AUTHORIZATION, "Bearer " + token))
                    .andExpect(status().isForbidden());
        }
    }

    /**
     * Motor de asignacion de huecos (UC12.2) por HTTP: la franja libre aparece entera; al
     * reservar una cita la franja se parte en dos tramos descontando el hueco bloqueado; y
     * el filtro duracionMinutos descarta el tramo que no cabe.
     */
    @Test
    void elMotorDeHuecosLibresDescuentaLosHuecosBloqueados() throws Exception {
        String token = tokenDe("recepcionista", "iso2-rec");
        String profesional = "api-citas-huecos";
        crearAgenda(token, profesional);
        anyadirFranja(token, profesional, "2027-06-07", "08:00", "12:00");

        mvc.perform(get(RUTA_CITAS + "/huecos").header(AUTHORIZATION, "Bearer " + token)
                        .param("profesionalId", profesional).param("desde", "2027-06-07").param("hasta", "2027-06-07")
                        .param("duracionMinutos", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].inicio").value("2027-06-07T08:00:00"))
                .andExpect(jsonPath("$[0].duracionMinutos").value(240));

        mvc.perform(post(RUTA_CITAS).header(AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cita(profesional, "2027-06-07T09:00:00", 30)))
                .andExpect(status().isCreated());

        mvc.perform(get(RUTA_CITAS + "/huecos").header(AUTHORIZATION, "Bearer " + token)
                        .param("profesionalId", profesional).param("desde", "2027-06-07").param("hasta", "2027-06-07")
                        .param("duracionMinutos", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].inicio").value("2027-06-07T08:00:00"))
                .andExpect(jsonPath("$[0].duracionMinutos").value(60))
                .andExpect(jsonPath("$[1].inicio").value("2027-06-07T09:30:00"))
                .andExpect(jsonPath("$[1].duracionMinutos").value(150));

        // Con 4 horas de duracion pedida no queda ningun tramo que la albergue (60 y 150 min).
        mvc.perform(get(RUTA_CITAS + "/huecos").header(AUTHORIZATION, "Bearer " + token)
                        .param("profesionalId", profesional).param("desde", "2027-06-07").param("hasta", "2027-06-07")
                        .param("duracionMinutos", "240"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ------------------------------------------------------------------
    // Semillas de prueba propias (api-*) y ayudantes
    // ------------------------------------------------------------------

    private void crearAgenda(String token, String profesional) throws Exception {
        mvc.perform(post("/api/agendas").header(AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"profesionalId\":\"" + profesional + "\"}"))
                .andExpect(status().isCreated());
    }

    private void anyadirFranja(String token, String profesional, String dia, String inicio, String fin)
            throws Exception {
        mvc.perform(post("/api/agendas/" + profesional + "/franjas").header(AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dia\":\"%s\",\"inicio\":\"%s\",\"fin\":\"%s\"}".formatted(dia, inicio, fin)))
                .andExpect(status().isCreated());
    }

    /** Receta una solicitud del medico en estado creada y devuelve su identificador. */
    private String crearSolicitud(String tokenMedico, String paciente) throws Exception {
        MvcResult resultado = mvc.perform(post("/api/solicitudes").header(AUTHORIZATION, "Bearer " + tokenMedico)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"pacienteId":"%s","codigoPrueba":"SEM-RM-RODILLA","indicacionClinica":"dolor",
                                 "prioridad":"ORDINARIA","origen":"AP"}
                                """.formatted(paciente)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("creada"))
                .andReturn();
        return objectMapper.readTree(resultado.getResponse().getContentAsString()).get("solicitudId").asText();
    }

    private static String cita(String profesionalId, String inicio, int duracionMinutos) {
        return """
                {"solicitudId":null,"profesionalId":"%s","inicio":"%s","duracionMinutos":%d}
                """.formatted(profesionalId, inicio, duracionMinutos);
    }

    private static String citaConSolicitud(String solicitudId, String profesionalId, String inicio,
            int duracionMinutos) {
        return """
                {"solicitudId":"%s","profesionalId":"%s","inicio":"%s","duracionMinutos":%d}
                """.formatted(solicitudId, profesionalId, inicio, duracionMinutos);
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
