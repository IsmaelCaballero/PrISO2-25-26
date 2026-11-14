package es.uclm.iso2.client.api;

import com.sun.net.httpserver.HttpServer;
import es.uclm.iso2.common.citas.CitaFacade.CitaDTO;
import es.uclm.iso2.common.citas.CitaFacade.CitaNuevaDTO;
import es.uclm.iso2.common.citas.CitaFacade.DetalleCitaDTO;
import es.uclm.iso2.common.citas.CitaFacade.HuecoDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueba de humo de la capa API de citas del cliente (CDU12, Iter5/CFG-05): stub
 * HTTP del JDK (headless, sin JavaFX) para verificar el Bearer, las fechas y horas
 * ISO con segundos en ambos sentidos ({@code JavaTimeModule}), la (de)serializacion
 * de los DTOs de {@code CitaFacade}, los filtros opcionales del listado y del motor
 * de huecos, y la traduccion de los codigos de error del contrato (hueco ocupado,
 * fuera de franja, no encontrada) a {@link ApiException}.
 */
class ApiClienteCitasTest {

    private static final String CITA_JSON = """
            {"citaId":"cita-1","solicitudId":"sol-1","profesionalId":"medico-1",
             "inicio":"2027-06-01T08:00:00","fin":"2027-06-01T08:30:00",
             "estado":"programada","motivoCancelacion":null,"version":0}
            """;

    private HttpServer servidor;
    private ApiClienteCitas api;

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
            peticion.sendResponseHeaders(estadoPorResponder, cuerpo.length);
            try (var salida = peticion.getResponseBody()) {
                salida.write(cuerpo);
            }
        });
        servidor.start();
        api = new ApiClienteCitas("http://127.0.0.1:" + servidor.getAddress().getPort(), TOKEN_PRUEBA);
    }

    /**
     * Para el stub HTTP al terminar cada test.
     */
    @AfterEach
    void pararServidorStub() {
        servidor.stop(0);
    }

    /**
     * GET listar con los cuatro filtros rellenos: viajan por query con el formato
     * ISO.DATE_TIME que acepta el servidor y decodifica {@code DetalleCitaDTO} con
     * {@code LocalDateTime} y el motivo nulo.
     *
     * @throws Exception si falla la llamada
     */
    @Test
    void listarMandaFiltrosISOyDecodificaDetalle() throws Exception {
        cuerpoPorResponder = "[" + CITA_JSON + "]";

        List<DetalleCitaDTO> citas = api.listar(
                LocalDateTime.parse("2027-06-01T00:00:00"),
                LocalDateTime.parse("2027-06-30T23:59:59"),
                "medico-1", "programada");

        assertTrue(ultimaRuta.contains("desde=2027-06-01T00:00:00"));
        assertTrue(ultimaRuta.contains("hasta=2027-06-30T23:59:59"));
        assertTrue(ultimaRuta.contains("profesionalId=medico-1"));
        assertTrue(ultimaRuta.contains("estado=programada"));
        assertEquals(1, citas.size());
        DetalleCitaDTO cita = citas.get(0);
        assertEquals("cita-1", cita.citaId());
        assertEquals(LocalDateTime.parse("2027-06-01T08:00:00"), cita.inicio());
        assertEquals("medico-1", cita.profesionalId());
        assertNull(cita.motivoCancelacion());
        assertEquals("Bearer " + TOKEN_PRUEBA, ultimaAutorizacion);
    }

    /**
     * GET listar sin filtros: ni signo de interrogacion ni parametros vacios, para no
     * provocar 400 por {@code estado} en blanco en el servidor.
     *
     * @throws Exception si falla la llamada
     */
    @Test
    void listarSinFiltrosNoMandaConsulta() throws Exception {
        cuerpoPorResponder = "[]";

        List<DetalleCitaDTO> citas = api.listar(null, null, null, "  ");

        assertEquals("/api/citas", ultimaRuta);
        assertTrue(citas.isEmpty());
    }

    /**
     * GET detalle: ruta con el id funcional y decodificacion del detalle completo.
     *
     * @throws Exception si falla la llamada
     */
    @Test
    void detalleDecodificaProfesionalYEstado() throws Exception {
        cuerpoPorResponder = CITA_JSON;

        DetalleCitaDTO cita = api.detalle("cita-1");

        assertTrue(ultimaRuta.endsWith("/api/citas/cita-1"));
        assertEquals("programada", cita.estado());
        assertEquals(0L, cita.version());
    }

    /**
     * GET huecos con duracion: fechas ISO sin hora y el filtro de duracion incluido
     * (motor de asignacion UC12.2).
     *
     * @throws Exception si falla la llamada
     */
    @Test
    void huecosLibresMandaRangoYduracion() throws Exception {
        cuerpoPorResponder = """
                [{"profesionalId":"medico-1","inicio":"2027-06-01T09:00:00",
                  "fin":"2027-06-01T15:00:00","duracionMinutos":360}]
                """;

        List<HuecoDTO> huecos = api.huecosLibres("medico-1",
                LocalDate.parse("2027-06-01"), LocalDate.parse("2027-06-05"), 30);

        assertTrue(ultimaRuta.contains("/api/citas/huecos?profesionalId=medico-1"));
        assertTrue(ultimaRuta.contains("desde=2027-06-01"));
        assertTrue(ultimaRuta.contains("hasta=2027-06-05"));
        assertTrue(ultimaRuta.contains("duracionMinutos=30"));
        assertEquals(1, huecos.size());
        assertEquals(LocalDateTime.parse("2027-06-01T09:00:00"), huecos.get(0).inicio());
        assertEquals(360, huecos.get(0).duracionMinutos());
    }

    /**
     * GET huecos sin duracion: el parametro no aparece y el servidor devuelve las
     * franjas libres enteras ({@code duracionMinutos} nulo en el DTO).
     *
     * @throws Exception si falla la llamada
     */
    @Test
    void huecosLibresSinDuracionOmiteElParametro() throws Exception {
        cuerpoPorResponder = """
                [{"profesionalId":"medico-1","inicio":"2027-06-01T08:00:00",
                  "fin":"2027-06-01T15:00:00","duracionMinutos":null}]
                """;

        List<HuecoDTO> huecos = api.huecosLibres("medico-1",
                LocalDate.parse("2027-06-01"), LocalDate.parse("2027-06-01"), null);

        assertTrue(!ultimaRuta.contains("duracionMinutos"));
        assertNull(huecos.get(0).duracionMinutos());
    }

    /**
     * POST agendar: el inicio y la duracion viajan como espera el Jackson del
     * servidor (ISO con segundos) y la cita creada se decodifica con su id.
     *
     * @throws Exception si falla la llamada
     */
    @Test
    void agendarSerializaInicioISOyDevuelveCitaCreada() throws Exception {
        estadoPorResponder = 201;
        cuerpoPorResponder = """
                {"citaId":"cita-nueva","solicitudId":"sol-1",
                 "inicio":"2027-06-01T08:00:00","fin":"2027-06-01T08:30:00","estado":"programada"}
                """;

        CitaDTO creada = api.agendar(new CitaNuevaDTO("sol-1", "medico-1",
                LocalDateTime.parse("2027-06-01T08:00:00"), 30));

        assertTrue(ultimoCuerpoRecibido.contains("\"inicio\":\"2027-06-01T08:00:00\""),
                "Jackson del cliente escribe la fecha ISO completa, igual que espera Spring Boot");
        assertTrue(ultimoCuerpoRecibido.contains("\"duracionMinutos\":30"));
        assertEquals("cita-nueva", creada.citaId());
        assertEquals("programada", creada.estado());
    }

    /**
     * POST reprogramar: el cuerpo lleva solo el nuevo inicio ISO
     * ({@code ReprogramacionRequest} del controlador).
     *
     * @throws Exception si falla la llamada
     */
    @Test
    void reprogramarSerializaNuevoInicio() throws Exception {
        cuerpoPorResponder = """
                {"citaId":"cita-1","solicitudId":"sol-1",
                 "inicio":"2027-06-02T10:00:00","fin":"2027-06-02T10:30:00","estado":"programada"}
                """;

        CitaDTO movida = api.reprogramar("cita-1", LocalDateTime.parse("2027-06-02T10:00:00"));

        assertTrue(ultimaRuta.endsWith("/api/citas/cita-1/reprogramar"));
        assertTrue(ultimoCuerpoRecibido.contains("\"inicio\":\"2027-06-02T10:00:00\""));
        assertEquals(LocalDateTime.parse("2027-06-02T10:00:00"), movida.inicio());
    }

    /**
     * POST cancelar: el motivo obligatorio viaja en el cuerpo y la respuesta es la
     * cita cancelada (el motivo solo se ve despues en el detalle).
     *
     * @throws Exception si falla la llamada
     */
    @Test
    void cancelarMandaMotivoEnElCuerpo() throws Exception {
        cuerpoPorResponder = """
                {"citaId":"cita-1","solicitudId":"sol-1",
                 "inicio":"2027-06-01T08:00:00","fin":"2027-06-01T08:30:00","estado":"cancelada"}
                """;

        CitaDTO cancelada = api.cancelar("cita-1", "paciente hospitalizado");

        assertTrue(ultimaRuta.endsWith("/api/citas/cita-1/cancelar"));
        assertTrue(ultimoCuerpoRecibido.contains("paciente hospitalizado"));
        assertEquals("cancelada", cancelada.estado());
    }

    /**
     * POST confirmar: accion sin cuerpo sobre la ruta de la maquina de estados.
     *
     * @throws Exception si falla la llamada
     */
    @Test
    void confirmarSinCuerpoSobreLaRutaDeAccion() throws Exception {
        cuerpoPorResponder = """
                {"citaId":"cita-1","solicitudId":"sol-1",
                 "inicio":"2027-06-01T08:00:00","fin":"2027-06-01T08:30:00","estado":"confirmada"}
                """;

        CitaDTO confirmada = api.confirmar("cita-1");

        assertTrue(ultimaRuta.endsWith("/api/citas/cita-1/confirmar"));
        assertTrue(ultimoCuerpoRecibido.isEmpty());
        assertEquals("confirmada", confirmada.estado());
    }

    /**
     * POST expirar-vencidas con corte ISO en el cuerpo (lote UC12.7).
     *
     * @throws Exception si falla la llamada
     */
    @Test
    void expirarVencidasMandaCorteISO() throws Exception {
        cuerpoPorResponder = """
                [{"citaId":"cita-vieja","solicitudId":null,
                  "inicio":"2026-11-01T08:00:00","fin":"2026-11-01T08:30:00","estado":"expirada"}]
                """;

        List<CitaDTO> expiradas = api.expirarVencidas(LocalDateTime.parse("2026-11-14T08:00:00"));

        assertTrue(ultimaRuta.endsWith("/api/citas/expirar-vencidas"));
        assertTrue(ultimoCuerpoRecibido.contains("\"corte\":\"2026-11-14T08:00:00\""));
        assertEquals(1, expiradas.size());
        assertEquals("expirada", expiradas.get(0).estado());
    }

    /**
     * El duelo anti-concurrencia del hueco (409 CONFLICTO_HUECO_OCUPADO, RF12 y D36)
     * se traduce a {@link ApiException} con codigo, estado y mensaje legibles.
     *
     * @throws Exception si falla la llamada
     */
    @Test
    void huecoOcupadoSeTraduceAApiException() throws Exception {
        estadoPorResponder = 409;
        cuerpoPorResponder = """
                {"codigo":"CONFLICTO_HUECO_OCUPADO",
                 "mensaje":"el hueco 2027-06-01T08:00 del profesional medico-1 ya esta ocupado",
                 "instante":1794000000.0}
                """;

        ApiException error = assertThrows(ApiException.class, () -> api.agendar(new CitaNuevaDTO(
                null, "medico-1", LocalDateTime.parse("2027-06-01T08:00:00"), 30)));

        assertEquals(409, error.status());
        assertEquals("CONFLICTO_HUECO_OCUPADO", error.codigo());
        assertTrue(error.mensaje().contains("ya esta ocupado"));
    }

    /**
     * Una cita inexistente (404 CITA_NO_ENCONTRADA) se traduce con su codigo de
     * contrato.
     *
     * @throws Exception si falla la llamada
     */
    @Test
    void citaInexistenteSeTraduceAApiException() {
        estadoPorResponder = 404;
        cuerpoPorResponder = """
                {"codigo":"CITA_NO_ENCONTRADA","mensaje":"la cita cita-fantasma no existe",
                 "instante":1794000000.0}
                """;

        ApiException error = assertThrows(ApiException.class, () -> api.detalle("cita-fantasma"));

        assertEquals(404, error.status());
        assertEquals("CITA_NO_ENCONTRADA", error.codigo());
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

        ApiException error = assertThrows(ApiException.class,
                () -> api.listar(null, null, null, null));

        assertEquals(502, error.status());
        assertEquals("ERROR_HTTP", error.codigo());
    }
}
