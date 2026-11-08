package es.uclm.iso2.client.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import es.uclm.iso2.common.seguridad.AutenticacionFacade.CredencialesDTO;
import es.uclm.iso2.common.seguridad.AutenticacionFacade.SesionDTO;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/**
 * Cliente HTTP de autenticacion (CDU17, Iter2/CFG-02): consume {@code POST /api/auth/login}
 * y devuelve la {@link SesionDTO} del contrato comun (token JWT + rol + caducidad).
 *
 * <p>Es la puerta de entrada del cliente: el token devuelto se pasa despues a
 * {@link ApiClienteCatalogo} como cabecera Bearer. El rol de la sesion decide que
 * vista abre la aplicacion (ver {@code NavegacionPorRol}).</p>
 *
 * <p>Los errores del servidor ({@code CREDENCIALES_INVALIDAS}, {@code CUENTA_BLOQUEADA},
 * {@code DATOS_INVALIDOS}) se traducen a {@link ApiException} igual que en el resto
 * de clientes. No depende de JavaFX: es verificable en CI headless.</p>
 *
 * @since 0.1.2 (CFG-02)
 */
public class ApiClienteAutenticacion {

    private final String baseUrl;
    private final HttpClient http;
    private final ObjectMapper json;

    /**
     * Crea el cliente contra la raiz de la API del servidor (sin credenciales: el
     * login es quien las pide al usuario).
     *
     * @param baseUrl raiz de la API (ej. {@code http://localhost:8080})
     */
    public ApiClienteAutenticacion(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.http = HttpClient.newHttpClient();
        // JavaTimeModule: el campo caducidad (Instant) del SesionDTO necesita jsr310.
        this.json = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(DeserializationFeature.FAIL_ON_TRAILING_TOKENS, true);
    }

    /**
     * Inicia sesion en el servidor. No lanza excepcion por credenciales incorrectas:
     * lanza {@link ApiException} con el codigo del {@code ApiError} recibido
     * ({@code CREDENCIALES_INVALIDAS} o {@code CUENTA_BLOQUEADA}).
     *
     * @param usuario  usuario de la cuenta
     * @param password contrasena en claro (solo viaja en esta peticion HTTPS/HTTP)
     * @return sesion con token JWT, rol y caducidad
     * @throws IOException          si el servidor no responde
     * @throws InterruptedException si se interrumpe la espera
     * @throws ApiException         si el servidor devuelve un error de contrato
     */
    public SesionDTO iniciarSesion(String usuario, String password)
            throws IOException, InterruptedException {
        String cuerpo = json.writeValueAsString(new CredencialesDTO(usuario, password));
        HttpRequest peticion = HttpRequest.newBuilder(URI.create(baseUrl + "/api/auth/login"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(cuerpo, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> respuesta =
                http.send(peticion, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (respuesta.statusCode() >= 400) {
            throw traducirError(respuesta);
        }
        return json.readValue(respuesta.body(), SesionDTO.class);
    }

    private ApiException traducirError(HttpResponse<String> respuesta) {
        try {
            JsonNode raiz = json.readTree(respuesta.body());
            return new ApiException(respuesta.statusCode(),
                    raiz.path("codigo").asText("ERROR_DESCONOCIDO"),
                    raiz.path("mensaje").asText(respuesta.body()));
        } catch (IOException cuerpoNoJson) {
            return new ApiException(respuesta.statusCode(), "ERROR_HTTP", respuesta.body());
        }
    }
}
