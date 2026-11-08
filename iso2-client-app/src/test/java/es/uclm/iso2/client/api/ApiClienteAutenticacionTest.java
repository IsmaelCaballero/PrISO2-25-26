package es.uclm.iso2.client.api;

import com.sun.net.httpserver.HttpServer;
import es.uclm.iso2.common.seguridad.AutenticacionFacade.SesionDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueba del cliente de autenticacion (CDU17, CFG-02): stub HTTP del JDK que simula
 * {@code POST /api/auth/login}. Verifica la serializacion de credenciales, la
 * lectura del {@code SesionDTO} (incluida la caducidad Instant via jsr310) y la
 * traduccion de los errores de contrato (401 bloqueo, error no JSON).
 */
class ApiClienteAutenticacionTest {

    private static final String SESION_JSON = """
            {"usuario":"celador","rol":"CELADOR","token":"eyJhbGciOiJIUzI1NiJ0.firma",
             "caducidad":1893456000.000000000}
            """;

    private HttpServer servidor;
    private ApiClienteAutenticacion api;

    private volatile String ultimoMetodo;
    private volatile String ultimaRuta;
    private volatile String ultimoCuerpoRecibido;
    private volatile int estadoPorResponder = 200;
    private volatile String cuerpoPorResponder = SESION_JSON;

    /**
     * Arranca el stub HTTP en puerto efimero y el cliente contra el.
     *
     * @throws IOException si no se puede abrir el puerto
     */
    @BeforeEach
    void arrancarServidorStub() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.createContext("/", peticion -> {
            ultimoMetodo = peticion.getRequestMethod();
            ultimaRuta = peticion.getRequestURI().toString();
            ultimoCuerpoRecibido = new String(peticion.getRequestBody().readAllBytes(),
                    StandardCharsets.UTF_8);
            byte[] cuerpo = cuerpoPorResponder.getBytes(StandardCharsets.UTF_8);
            peticion.sendResponseHeaders(estadoPorResponder, cuerpo.length);
            try (var salida = peticion.getResponseBody()) {
                salida.write(cuerpo);
            }
        });
        servidor.start();
        api = new ApiClienteAutenticacion("http://127.0.0.1:" + servidor.getAddress().getPort());
    }

    /**
     * Para el stub al terminar cada test.
     */
    @AfterEach
    void pararServidorStub() {
        servidor.stop(0);
    }

    /**
     * Login correcto: POST a la ruta del contrato, credenciales serializadas y
     * sesion leida con token, rol y caducidad Instant.
     *
     * @throws Exception si falla la llamada
     */
    @Test
    void loginCorrectoDevuelveSesionCompleta() throws Exception {
        SesionDTO sesion = api.iniciarSesion("celador", "iso2-ses");

        assertEquals("POST", ultimoMetodo);
        assertEquals("/api/auth/login", ultimaRuta);
        assertTrue(ultimoCuerpoRecibido.contains("\"usuario\":\"celador\""));
        assertTrue(ultimoCuerpoRecibido.contains("\"password\":\"iso2-ses\""));

        assertEquals("celador", sesion.usuario());
        assertEquals("CELADOR", sesion.rol());
        assertNotNull(sesion.token());
        assertEquals(Instant.ofEpochSecond(1893456000L), sesion.caducidad());
    }

    /**
     * Credenciales incorrectas: el 401 con ApiError se traduce a ApiException con el
     * codigo estable (la UI lo mostrara tal cual).
     *
     * @throws Exception si falla la llamada
     */
    @Test
    void credencialesInvalidasSeTraducenAApiException() throws Exception {
        estadoPorResponder = 401;
        cuerpoPorResponder = """
                {"codigo":"CREDENCIALES_INVALIDAS","mensaje":"Usuario o contrasena incorrectos"}
                """;

        ApiException error = assertThrows(ApiException.class, () -> api.iniciarSesion("celador", "no"));

        assertEquals(401, error.status());
        assertEquals("CREDENCIALES_INVALIDAS", error.codigo());
    }

    /**
     * Cuenta bloqueada: el codigo CUENTA_BLOQUEADA llega intacto al cliente (es la
     * senal de que el login debe esperar al desbloqueo).
     *
     * @throws Exception si falla la llamada
     */
    @Test
    void cuentaBloqueadaConservaSuCodigo() throws Exception {
        estadoPorResponder = 401;
        cuerpoPorResponder = """
                {"codigo":"CUENTA_BLOQUEADA","mensaje":"cuenta bloqueada hasta 2026-11-08T12:15:00Z"}
                """;

        ApiException error = assertThrows(ApiException.class, () -> api.iniciarSesion("bloqueable", "iso2-bloq"));

        assertEquals("CUENTA_BLOQUEADA", error.codigo());
        assertTrue(error.getMessage().contains("bloqueada"));
    }

    /**
     * Un portal cautivo o proxy que responde HTML en el login tampoco deja al cliente
     * sin mensaje: se envuelve como ERROR_HTTP.
     *
     * @throws Exception si falla la llamada
     */
    @Test
    void respuestaNoJsonSeEnvuelveComoErrorHttp() throws Exception {
        estadoPorResponder = 502;
        cuerpoPorResponder = "<html>proxy caido</html>";

        ApiException error = assertThrows(ApiException.class, () -> api.iniciarSesion("celador", "iso2-ses"));

        assertEquals(502, error.status());
        assertEquals("ERROR_HTTP", error.codigo());
    }
}
