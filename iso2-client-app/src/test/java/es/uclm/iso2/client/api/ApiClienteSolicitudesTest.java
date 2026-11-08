package es.uclm.iso2.client.api;

import com.sun.net.httpserver.HttpServer;
import es.uclm.iso2.common.solicitudes.Prioridad;
import es.uclm.iso2.common.solicitudes.SolicitudFacade.SolicitudDTO;
import es.uclm.iso2.common.solicitudes.SolicitudFacade.SolicitudPrescripcionDTO;
import es.uclm.iso2.common.solicitudes.SolicitudFacade.TrazabilidadSolicitudDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueba de humo de la capa API de solicitudes del cliente (CDU06, Iter4/CFG-04):
 * stub HTTP del JDK (headless, sin JavaFX) para verificar el Bearer, el cuerpo del
 * alta con los cinco campos del controlador (el {@code medicoId} NO viaja: RF06.2 de
 * no repudio lo fija el servidor desde el token), la prioridad como nombre de enum,
 * las fechas ISO de la trazabilidad y la traduccion de los codigos de error del
 * contrato (duplicada, prueba no disponible, no encontrada, datos invalidos).
 */
class ApiClienteSolicitudesTest {

    private static final String SOLICITUD_JSON = """
            {"solicitudId":"3f2a1b6c-0000-4cde-8a11-000000000006",
             "pacienteId":"HC-12345678",
             "codigoPrueba":"RX-TORAX",
             "fechaSolicitada":"2026-11-08",
             "estado":"creada"}
            """;

    private static final String TRAZABILIDAD_JSON = """
            {"solicitudId":"3f2a1b6c-0000-4cde-8a11-000000000006",
             "autor":"medico-1",
             "creadaEn":"2026-11-08T10:15:30.123456Z",
             "huella":"9c1a4e0b6d2f4a3e8b7c5d1a0f6e2b4c3d5a7e9f1b0c2d4e6f8a0b1c3d5e7f9a",
             "integridadOk":true}
            """;

    private HttpServer servidor;
    private ApiClienteSolicitudes api;

    private static final String TOKEN_PRUEBA = "eyJhbGciOiJIUzI1NiJ0.prueba.firma";

    private volatile int estadoPorResponder = 200;
    private volatile String cuerpoPorResponder = "[]";
    private volatile String ultimaRuta;
    private volatile String ultimoMetodo;
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
            ultimoMetodo = peticion.getRequestMethod();
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
        api = new ApiClienteSolicitudes("http://127.0.0.1:" + servidor.getAddress().getPort(), TOKEN_PRUEBA);
    }

    /**
     * Para el stub HTTP al terminar cada test.
     */
    @AfterEach
    void pararServidorStub() {
        servidor.stop(0);
    }

    /**
     * POST prescribir: autentica con Bearer y manda los cinco campos que acepta el
     * controlador, con la prioridad como nombre del enum (igual que espera el
     * {@code desdeNombre} del servidor). El {@code medicoId} del dto solo sirve para
     * validar RF06.1 en el cliente: NO se serializa (RF06.2, el autor es el claim).
     *
     * @throws Exception si falla la llamada
     */
    @Test
    void prescribirMandaCincoCamposSinMedicoIdYDecodificaLaSolicitud() throws Exception {
        estadoPorResponder = 201;
        cuerpoPorResponder = SOLICITUD_JSON;

        SolicitudDTO creada = api.prescribir(new SolicitudPrescripcionDTO("HC-12345678", "RX-TORAX",
                "sospecha de neumonia", Prioridad.PREFERENTE, "AP", "medico-1"));

        assertTrue(ultimaRuta.endsWith("/api/solicitudes"));
        assertEquals("POST", ultimoMetodo);
        assertEquals("Bearer " + TOKEN_PRUEBA, ultimaAutorizacion);
        assertTrue(ultimoCuerpoRecibido.contains("\"pacienteId\":\"HC-12345678\""));
        assertTrue(ultimoCuerpoRecibido.contains("\"codigoPrueba\":\"RX-TORAX\""));
        assertTrue(ultimoCuerpoRecibido.contains("\"indicacionClinica\":\"sospecha de neumonia\""));
        assertTrue(ultimoCuerpoRecibido.contains("\"prioridad\":\"PREFERENTE\""));
        assertTrue(ultimoCuerpoRecibido.contains("\"origen\":\"AP\""));
        assertFalse(ultimoCuerpoRecibido.contains("medicoId"),
                "no repudio (RF06.2): el autor lo fija el servidor desde el token");
        assertEquals("3f2a1b6c-0000-4cde-8a11-000000000006", creada.solicitudId());
        assertEquals(LocalDate.parse("2026-11-08"), creada.fechaSolicitada());
        assertEquals("creada", creada.estado());
    }

    /**
     * RF06.1 en el cliente: un dto con un campo obligatorio en blanco se rechaza en el
     * propio contrato antes de gastar red, y el stub comprueba que no llego peticion.
     *
     * @throws Exception si falla la llamada
     */
    @Test
    void prescripcionSinIndicacionSeRechazaEnElClienteSinLlamarALaRed() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> api.prescribir(new SolicitudPrescripcionDTO("HC-12345678", "RX-TORAX", "  ",
                        Prioridad.ORDINARIA, "AP", "medico-1")));

        assertTrue(error.getMessage().contains("indicacionClinica"));
        assertNull(ultimaRuta, "no se debe haber emitido peticion HTTP");
    }

    /**
     * GET misSolicitudes: la ruta no lleva el identificador del medico (lo toma el
     * servidor del token) y decodifica la lista con su estado en minusculas.
     *
     * @throws Exception si falla la llamada
     */
    @Test
    void misSolicitudesRutaSinMedicoIdDecodificaLista() throws Exception {
        cuerpoPorResponder = "[" + SOLICITUD_JSON + "]";

        List<SolicitudDTO> mias = api.misSolicitudes();

        assertTrue(ultimaRuta.endsWith("/api/solicitudes/mias"));
        assertEquals("GET", ultimoMetodo);
        assertEquals(1, mias.size());
        assertEquals("RX-TORAX", mias.get(0).codigoPrueba());
        assertEquals("creada", mias.get(0).estado());
    }

    /**
     * GET detalle: ruta con el identificador funcional (UUID) y solicitud decodificada.
     *
     * @throws Exception si falla la llamada
     */
    @Test
    void detalleDecodificaLaSolicitud() throws Exception {
        cuerpoPorResponder = SOLICITUD_JSON;

        SolicitudDTO detalle = api.detalle("3f2a1b6c-0000-4cde-8a11-000000000006");

        assertTrue(ultimaRuta.endsWith("/api/solicitudes/3f2a1b6c-0000-4cde-8a11-000000000006"));
        assertEquals("HC-12345678", detalle.pacienteId());
    }

    /**
     * GET integridad: el {@code Instant} de la trazabilidad viaja como cadena ISO con
     * microsegundos ({@code JavaTimeModule}) y se reconstruye sin perder precision, que
     * es justo la invariante de la huella de RF06.2.
     *
     * @throws Exception si falla la llamada
     */
    @Test
    void integridadDecodificaTrazabilidadConInstantISO() throws Exception {
        cuerpoPorResponder = TRAZABILIDAD_JSON;

        TrazabilidadSolicitudDTO trazas = api.integridad("3f2a1b6c-0000-4cde-8a11-000000000006");

        assertTrue(ultimaRuta.endsWith("/api/solicitudes/3f2a1b6c-0000-4cde-8a11-000000000006/integridad"));
        assertEquals("medico-1", trazas.autor());
        assertEquals(Instant.parse("2026-11-08T10:15:30.123456Z"), trazas.creadaEn());
        assertEquals(64, trazas.huella().length());
        assertTrue(trazas.integridadOk());
    }

    /**
     * Un duplicado del mismo medico (409 CONFLICTO_SOLICITUD_DUPLICADA, RF06.3) se
     * traduce a {@link ApiException} con codigo, estado y mensaje legibles.
     *
     * @throws Exception si falla la llamada
     */
    @Test
    void conflictoDuplicadaSeTraduceAApiException() throws Exception {
        estadoPorResponder = 409;
        cuerpoPorResponder = """
                {"codigo":"CONFLICTO_SOLICITUD_DUPLICADA",
                 "mensaje":"el medico medico-1 ya tiene una solicitud activa de RX-TORAX para HC-12345678",
                 "instante":1794000000.0}
                """;

        ApiException error = assertThrows(ApiException.class, () -> api.prescribir(new SolicitudPrescripcionDTO(
                "HC-12345678", "RX-TORAX", "sospecha de neumonia", Prioridad.ORDINARIA, "AP", "medico-1")));

        assertEquals(409, error.status());
        assertEquals("CONFLICTO_SOLICITUD_DUPLICADA", error.codigo());
        assertTrue(error.mensaje().contains("solicitud activa"));
    }

    /**
     * Una prueba inexistente o de baja logica (409 CONFLICTO_PRUEBA_NO_DISPONIBLE,
     * validacion contra el catalogo de CDU16) se traduce con su codigo de contrato.
     *
     * @throws Exception si falla la llamada
     */
    @Test
    void conflictoPruebaNoDisponibleSeTraduceAApiException() throws Exception {
        estadoPorResponder = 409;
        cuerpoPorResponder = """
                {"codigo":"CONFLICTO_PRUEBA_NO_DISPONIBLE",
                 "mensaje":"la prueba TAC-CABEZA no existe o esta de baja en el catalogo",
                 "instante":1794000000.0}
                """;

        ApiException error = assertThrows(ApiException.class, () -> api.prescribir(new SolicitudPrescripcionDTO(
                "HC-12345678", "TAC-CABEZA", "cefalea", Prioridad.URGENTE, "URGENCIAS", "medico-1")));

        assertEquals(409, error.status());
        assertEquals("CONFLICTO_PRUEBA_NO_DISPONIBLE", error.codigo());
    }

    /**
     * Una solicitud inexistente (404 SOLICITUD_NO_ENCONTRADA) se traduce con su codigo
     * de contrato tanto en el detalle como en la trazabilidad.
     *
     * @throws Exception si falla la llamada
     */
    @Test
    void solicitudInexistenteSeTraduceAApiException() {
        estadoPorResponder = 404;
        cuerpoPorResponder = """
                {"codigo":"SOLICITUD_NO_ENCONTRADA","mensaje":"la solicitud aaa-1 no existe",
                 "instante":1794000000.0}
                """;

        ApiException detalle = assertThrows(ApiException.class, () -> api.detalle("aaa-1"));
        ApiException trazas = assertThrows(ApiException.class, () -> api.integridad("aaa-1"));

        assertEquals(404, detalle.status());
        assertEquals("SOLICITUD_NO_ENCONTRADA", detalle.codigo());
        assertEquals("SOLICITUD_NO_ENCONTRADA", trazas.codigo());
    }

    /**
     * Un 400 del servidor (campo ausente o prioridad desconocida) se traduce con el
     * codigo estable DATOS_INVALIDOS.
     *
     * @throws Exception si falla la llamada
     */
    @Test
    void datosInvalidosSeTraduceAApiException() {
        estadoPorResponder = 400;
        cuerpoPorResponder = """
                {"codigo":"DATOS_INVALIDOS","mensaje":"la prioridad SUPERNOVA no es un valor valido",
                 "instante":1794000000.0}
                """;

        ApiException error = assertThrows(ApiException.class, this::consultarMisSolicitudes);

        assertEquals(400, error.status());
        assertEquals("DATOS_INVALIDOS", error.codigo());
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

        ApiException error = assertThrows(ApiException.class, this::consultarMisSolicitudes);

        assertEquals(502, error.status());
        assertEquals("ERROR_HTTP", error.codigo());
    }

    private List<SolicitudDTO> consultarMisSolicitudes() throws IOException, InterruptedException {
        return api.misSolicitudes();
    }
}
