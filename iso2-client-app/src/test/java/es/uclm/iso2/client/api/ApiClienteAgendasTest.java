package es.uclm.iso2.client.api;

import com.sun.net.httpserver.HttpServer;
import es.uclm.iso2.common.agendas.AgendaFacade.AgendaDTO;
import es.uclm.iso2.common.agendas.AgendaFacade.FestivoDTO;
import es.uclm.iso2.common.agendas.AgendaFacade.FranjaDTO;
import es.uclm.iso2.common.agendas.AgendaFacade.FranjaGestionDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueba de humo de la capa API de agendas del cliente (CDU13, Iter3/CFG-03): stub
 * HTTP del JDK (headless, sin JavaFX) para verificar el Bearer, las fechas y horas
 * ISO en ambos sentidos ({@code JavaTimeModule}), la (de)serializacion de los DTOs
 * de {@code AgendaFacade} y la traduccion de los codigos de error del contrato
 * (solape, festivo, agenda existente, no encontrada).
 */
class ApiClienteAgendasTest {

    private static final String AGENDA_JSON = """
            {"profesionalId":"medico-1",
             "franjas":[{"id":7,"dia":"2026-11-09","inicio":"08:00","fin":"15:00"}],
             "festivos":[{"id":3,"dia":"2026-11-10","motivo":"festivo local"}]}
            """;

    private HttpServer servidor;
    private ApiClienteAgendas api;

    private static final String TOKEN_PRUEBA = "eyJhbGciOiJIUzI1NiJ0.prueba.firma";

    private volatile int estadoPorResponder = 200;
    private volatile String cuerpoPorResponder = "[]";
    private volatile String ultimaRuta;
    private volatile String ultimaAutorizacion;
    private volatile String ultimoCuerpoRecibido;

    /**
     * Arranca el stub HTTP en un puerto efimero y el cliente contra el.
     *
     * @throws IOException si no se puede abrir el puerto
     */
    @BeforeEach
    void arrancarServidorStub() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.createContext("/", peticion -> {
            ultimaRuta = peticion.getRequestURI().toString();
            ultimaAutorizacion = peticion.getRequestHeaders().getFirst("Authorization");
            ultimoCuerpoRecibido = new String(peticion.getRequestBody().readAllBytes(),
                    StandardCharsets.UTF_8);
            byte[] cuerpo = cuerpoPorResponder.getBytes(StandardCharsets.UTF_8);
            if (estadoPorResponder == 204) {
                peticion.sendResponseHeaders(204, -1);
            } else {
                peticion.sendResponseHeaders(estadoPorResponder, cuerpo.length);
                try (var salida = peticion.getResponseBody()) {
                    salida.write(cuerpo);
                }
            }
        });
        servidor.start();
        api = new ApiClienteAgendas("http://127.0.0.1:" + servidor.getAddress().getPort(), TOKEN_PRUEBA);
    }

    /**
     * Para el stub HTTP al terminar cada test.
     */
    @AfterEach
    void pararServidorStub() {
        servidor.stop(0);
    }

    /**
     * GET listar: autentica con Bearer y decodifica las agendas con franjas y
     * festivos, convirtiendo las cadenas ISO en LocalDate/LocalTime.
     *
     * @throws Exception si falla la llamada
     */
    @Test
    void listarDecodificaFranjasYFestivosConFechasISO() throws Exception {
        cuerpoPorResponder = "[" + AGENDA_JSON + "]";

        List<AgendaDTO> agendas = api.listar();

        assertEquals(1, agendas.size());
        AgendaDTO agenda = agendas.get(0);
        assertEquals("medico-1", agenda.profesionalId());
        assertEquals(LocalDate.parse("2026-11-09"), agenda.franjas().get(0).dia());
        assertEquals(LocalTime.parse("15:00"), agenda.franjas().get(0).fin());
        assertEquals("festivo local", agenda.festivos().get(0).motivo());
        assertEquals("Bearer " + TOKEN_PRUEBA, ultimaAutorizacion);
        assertTrue(ultimaRuta.endsWith("/api/agendas"));
    }

    /**
     * GET detalle: devuelve el estado completo de la agenda de un profesional.
     *
     * @throws Exception si falla la llamada
     */
    @Test
    void detalleDevoluelveLaAgendaDelProfesional() throws Exception {
        cuerpoPorResponder = AGENDA_JSON;

        AgendaDTO agenda = api.detalle("medico-1");

        assertTrue(ultimaRuta.endsWith("/api/agendas/medico-1"));
        assertEquals(1, agenda.franjas().size());
        assertEquals(7L, agenda.franjas().get(0).id());
    }

    /**
     * GET franjas libres (contrato baseline CDU12): manda el rango como fechas ISO
     * por query y decodifica la lista de {@code FranjaDTO}.
     *
     * @throws Exception si falla la llamada
     */
    @Test
    void franjasLibresMandaRangoISOYDecodifica() throws Exception {
        cuerpoPorResponder = """
                [{"profesionalId":"medico-1","dia":"2026-11-09","inicio":"08:00","fin":"15:00"}]
                """;

        List<FranjaDTO> libres = api.franjasLibres("medico-1",
                LocalDate.parse("2026-11-09"), LocalDate.parse("2026-11-13"));

        assertTrue(ultimaRuta.contains("/libres?desde=2026-11-09&hasta=2026-11-13"));
        assertEquals(1, libres.size());
        assertEquals(LocalDate.parse("2026-11-09"), libres.get(0).dia());
    }

    /**
     * POST alta de agenda: serializa el profesional con listas vacias.
     *
     * @throws Exception si falla la llamada
     */
    @Test
    void crearAgendaSerializaProfesional() throws Exception {
        cuerpoPorResponder = "{\"profesionalId\":\"it-nueva\",\"franjas\":[],\"festivos\":[]}";

        AgendaDTO creada = api.crearAgenda("it-nueva");

        assertTrue(ultimoCuerpoRecibido.contains("\"profesionalId\":\"it-nueva\""));
        assertEquals("it-nueva", creada.profesionalId());
        assertTrue(creada.franjas().isEmpty());
    }

    /**
     * POST anadir franja: la fecha y las horas viajan como cadenas ISO (igual que
     * espera el Jackson de Spring Boot del servidor), no como marcas de tiempo.
     *
     * @throws Exception si falla la llamada
     */
    @Test
    void anadirFranjaSerializaFechasISO() throws Exception {
        cuerpoPorResponder = """
                {"id":9,"dia":"2026-11-16","inicio":"08:00","fin":"15:00"}
                """;

        FranjaGestionDTO creada = api.anadirFranja("medico-1", new FranjaGestionDTO(
                null, LocalDate.parse("2026-11-16"), LocalTime.parse("08:00"), LocalTime.parse("15:00")));

        assertTrue(ultimoCuerpoRecibido.contains("\"dia\":\"2026-11-16\""));
        assertTrue(ultimoCuerpoRecibido.contains("\"inicio\":\"08:00:00\""),
                "Jackson del cliente escribe la hora ISO completa, igual que espera Spring Boot");
        assertTrue(ultimoCuerpoRecibido.contains("\"fin\":\"15:00:00\""));
        assertEquals(9L, creada.id());
    }

    /**
     * POST anadir festivo: dia y motivo serializados y festivo devuelto con id.
     *
     * @throws Exception si falla la llamada
     */
    @Test
    void anadirFestivoSerializaYDevuelveId() throws Exception {
        cuerpoPorResponder = """
                {"id":5,"dia":"2026-12-25","motivo":"Navidad"}
                """;

        FestivoDTO creado = api.anadirFestivo("medico-1", new FestivoDTO(
                null, LocalDate.parse("2026-12-25"), "Navidad"));

        assertTrue(ultimoCuerpoRecibido.contains("\"dia\":\"2026-12-25\""));
        assertTrue(ultimoCuerpoRecibido.contains("Navidad"));
        assertEquals(5L, creado.id());
        assertEquals(LocalDate.parse("2026-12-25"), creado.dia());
    }

    /**
     * DELETE eliminar franja: con 204 sin cuerpo la operacion se da por buena.
     *
     * @throws Exception si falla la llamada
     */
    @Test
    void eliminarFranjaAcepta204SinCuerpo() throws Exception {
        estadoPorResponder = 204;
        api.eliminarFranja("medico-1", 7L);
        assertTrue(ultimaRuta.endsWith("/api/agendas/medico-1/franjas/7"));
    }

    /**
     * DELETE eliminar festivo: con 204 sin cuerpo la operacion se da por buena.
     *
     * @throws Exception si falla la llamada
     */
    @Test
    void eliminarFestivoAcepta204SinCuerpo() throws Exception {
        estadoPorResponder = 204;
        api.eliminarFestivo("medico-1", 3L);
        assertTrue(ultimaRuta.endsWith("/api/agendas/medico-1/festivos/3"));
    }

    /**
     * Un conflicto de solape del servidor (409 CONFLICTO_SOLAPE, regla CFG-03) se
     * traduce a {@link ApiException} con codigo, estado y mensaje legibles.
     *
     * @throws Exception si falla la llamada
     */
    @Test
    void conflictoSolapeSeTraduceAApiException() throws Exception {
        estadoPorResponder = 409;
        cuerpoPorResponder = """
                {"codigo":"CONFLICTO_SOLAPE",
                 "mensaje":"la franja 09:00-10:00 se solapa con la franja 08:00-15:00 del 2026-11-09",
                 "instante":1794000000.0}
                """;

        ApiException error = assertThrows(ApiException.class, () -> api.anadirFranja("medico-1",
                new FranjaGestionDTO(null, LocalDate.parse("2026-11-09"),
                        LocalTime.parse("09:00"), LocalTime.parse("10:00"))));

        assertEquals(409, error.status());
        assertEquals("CONFLICTO_SOLAPE", error.codigo());
        assertTrue(error.mensaje().contains("se solapa"));
    }

    /**
     * Una agenda inexistente (404 AGENDA_NO_ENCONTRADA) se traduce con su codigo de
     * contrato.
     *
     * @throws Exception si falla la llamada
     */
    @Test
    void agendaInexistenteSeTraduceAApiException() {
        estadoPorResponder = 404;
        cuerpoPorResponder = """
                {"codigo":"AGENDA_NO_ENCONTRADA","mensaje":"el profesional nadie no tiene agenda",
                 "instante":1794000000.0}
                """;

        ApiException error = assertThrows(ApiException.class, () -> api.detalle("nadie"));

        assertEquals(404, error.status());
        assertEquals("AGENDA_NO_ENCONTRADA", error.codigo());
    }

    /**
     * Una respuesta de error que no es JSON (proxy, portal cautivo etc.) no deja al
     * cliente sin mensaje: se envuelve como ERROR_HTTP.
     *
     * @throws Exception si falla la llamada
     */
    @Test
    void respuestaDeErrorNoJsonSeEnvuelve() {
        estadoPorResponder = 502;
        cuerpoPorResponder = "<html>proxy caido</html>";

        ApiException error = assertThrows(ApiException.class, this::listarAgendas);

        assertEquals(502, error.status());
        assertEquals("ERROR_HTTP", error.codigo());
    }

    private List<AgendaDTO> listarAgendas() throws IOException, InterruptedException {
        return api.listar();
    }
}
