package es.uclm.iso2.client.api;

import com.sun.net.httpserver.HttpServer;
import es.uclm.iso2.common.catalogo.PruebaCatalogoFacade.PoliticaDTO;
import es.uclm.iso2.common.catalogo.PruebaCatalogoFacade.PruebaImagenDTO;
import es.uclm.iso2.common.catalogo.PruebaCatalogoFacade.ProtocoloDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueba de humo de la capa API del cliente (CFG-01): se levanta un servidor HTTP
 * stub del JDK (headless, sin JavaFX) y se verifica que el cliente autentica con
 * Basic, (de)serializa los DTOs del contrato y traduce los errores ApiError.
 */
class ApiClienteCatalogoTest {

    private static final String DTO_JSON = """
            {"codigo":"RM-CERVICAL","nombre":"Resonancia magnetica cervical","categoria":"RM",
             "requiereCita":true,"duracionMinutos":45,"activa":true,
             "protocolo":{"preparacion":"sin metal","ayunoHoras":0,"contraindicaciones":"marcapasos"},
             "politica":{"plazoCancelacionHoras":24,"maxCancelaciones":3}}
            """;

    private HttpServer servidor;
    private ApiClienteCatalogo api;

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
        api = new ApiClienteCatalogo("http://127.0.0.1:" + servidor.getAddress().getPort(),
                "admin", "iso2-admin");
    }

    /**
     * Para el stub HTTP al terminar cada test.
     */
    @AfterEach
    void pararServidorStub() {
        servidor.stop(0);
    }

    /**
     * GET listar: manda la paginacion por query, autentica con Basic y decodifica la
     * lista de DTOs del contrato.
     *
     * @throws Exception si falla la llamada
     */
    @Test
    void listarPaginaYDecodificaLista() throws Exception {
        cuerpoPorResponder = "[" + DTO_JSON + "]";

        List<PruebaImagenDTO> pruebas = api.listar(0, 10);

        assertEquals(1, pruebas.size());
        assertEquals("RM-CERVICAL", pruebas.get(0).codigo());
        assertEquals(45, pruebas.get(0).duracionMinutos());
        assertEquals("sin metal", pruebas.get(0).protocolo().preparacion());
        assertTrue(ultimaRuta.contains("pagina=0&tamano=10"));
        assertEquals("Basic " + Base64.getEncoder().encodeToString(
                        "admin:iso2-admin".getBytes(StandardCharsets.UTF_8)),
                ultimaAutorizacion);
    }

    /**
     * POST crear: serializa el DTO (incluidos protocolo/politica anidados) y devuelve
     * el alta real del servidor.
     *
     * @throws Exception si falla la llamada
     */
    @Test
    void crearSerializaYDevuelveAltaReal() throws Exception {
        cuerpoPorResponder = DTO_JSON;
        PruebaImagenDTO alta = PruebaImagenDTO.sinEstado("RM-CERVICAL",
                "Resonancia magnetica cervical", "RM", true, 45,
                new ProtocoloDTO("sin metal", 0, "marcapasos"),
                new PoliticaDTO(24, 3));

        PruebaImagenDTO creada = api.crear(alta);

        assertTrue(ultimoCuerpoRecibido.contains("\"protocolo\""));
        assertTrue(ultimoCuerpoRecibido.contains("sin metal"));
        assertEquals("RM-CERVICAL", creada.codigo());
        assertTrue(creada.activa());
    }

    /**
     * DELETE desactivar: con 204 sin cuerpo la operacion se da por buena.
     *
     * @throws Exception si falla la llamada
     */
    @Test
    void desactivarAcepta204SinCuerpo() throws Exception {
        estadoPorResponder = 204;
        api.desactivar("RM-CERVICAL");
        assertTrue(ultimaRuta.endsWith("/api/catalogo/pruebas/RM-CERVICAL"));
    }

    /**
     * Un error de contrato del servidor (409 con cuerpo ApiError) se traduce a
     * {@link ApiException} con el codigo y el estado originales.
     *
     * @throws Exception si falla la llamada
     */
    @Test
    void errorDeContratoSeTraduceAApiException() {
        estadoPorResponder = 409;
        cuerpoPorResponder = """
                {"codigo":"CODIGO_EN_USO","mensaje":"el codigo RM-CERVICAL ya existe",
                 "instante":1794000000.0}
                """;
        PruebaImagenDTO alta = PruebaImagenDTO.sinEstado("RM-CERVICAL",
                "Resonancia magnetica cervical", "RM", true, 45,
                new ProtocoloDTO("sin metal", 0, "marcapasos"),
                new PoliticaDTO(24, 3));

        ApiException error = assertThrows(ApiException.class, () -> api.crear(alta));

        assertEquals(409, error.status());
        assertEquals("CODIGO_EN_USO", error.codigo());
        assertTrue(error.getMessage().contains("ya existe"));
    }

    /**
     * Una respuesta de error que no es JSON (proxy, portal cautivo etc.) no deja al
     * cliente sin mensaje: se envuelve como ERROR_HTTP.
     *
     * @throws Exception si falla la llamada
     */
    @Test
    void respuestaDeErrorNoJsonSeEnvuelve() throws Exception {
        estadoPorResponder = 502;
        cuerpoPorResponder = "<html>proxy caido</html>";

        ApiException error = assertThrows(ApiException.class, () -> api.listar(0, 10));

        assertEquals(502, error.status());
        assertEquals("ERROR_HTTP", error.codigo());
    }
}
