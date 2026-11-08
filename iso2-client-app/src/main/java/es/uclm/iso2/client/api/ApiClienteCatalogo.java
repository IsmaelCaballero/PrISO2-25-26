package es.uclm.iso2.client.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.uclm.iso2.common.catalogo.PruebaCatalogoFacade.PruebaImagenDTO;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Cliente HTTP del catalogo de pruebas (CDU16, Iter1/CFG-01).
 *
 * <p>Habla contra {@code /api/catalogo/pruebas} con el token Bearer emitido por
 * {@link ApiClienteAutenticacion} (autenticacion JWT definitiva de CDU17, Iter2) y
 * (de)serializa los DTOs del contrato comun. No depende de JavaFX: es verificable
 * en CI headless.</p>
 *
 * <p>Los errores del servidor ({@code ApiError}) se traducen a {@link ApiException}
 * para que la interfaz pueda mostrar un mensaje legible. Los roles no administrador
 * reciben 403 en los endpoints de escritura (matriz RBAC de CDU17); la interfaz
 * decide que vista mostrar segun el rol ({@code NavegacionPorRol}).</p>
 */
public class ApiClienteCatalogo {

    private final String baseUrl;
    private final HttpClient http;
    private final ObjectMapper json;
    private final String cabeceraAutorizacion;

    /**
     * Crea el cliente contra la raiz de la API del servidor.
     *
     * @param baseUrl  raiz de la API (ej. {@code http://localhost:8080})
     * @param tokenJwt token JWT (claim {@code rol}) obtenido de
     *                 {@link ApiClienteAutenticacion#iniciarSesion(String, String)}
     */
    public ApiClienteCatalogo(String baseUrl, String tokenJwt) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.http = HttpClient.newHttpClient();
        this.json = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_TRAILING_TOKENS, true);
        this.cabeceraAutorizacion = "Bearer " + tokenJwt;
    }

    /**
     * Lista el catalogo con paginacion.
     *
     * @param pagina pagina a pedir (0-based)
     * @param tamano tamano de pagina (1..200)
     * @return la pagina de pruebas devuelta por el servidor
     * @throws IOException          si el servidor no responde
     * @throws InterruptedException si se interrumpe la espera
     * @throws ApiException         si el servidor devuelve un error de contrato
     */
    public List<PruebaImagenDTO> listar(int pagina, int tamano)
            throws IOException, InterruptedException {
        HttpResponse<String> respuesta = enviar(peticion("GET",
                "/api/catalogo/pruebas?pagina=" + pagina + "&tamano=" + tamano).GET().build());
        return json.readValue(respuesta.body(), new TypeReference<List<PruebaImagenDTO>>() {
        });
    }

    /**
     * Da de alta una prueba (POST). El servidor responde 201 con el alta real.
     *
     * @param prueba datos del alta
     * @return la prueba tal y como quedo persistida
     * @throws IOException          si el servidor no responde
     * @throws InterruptedException si se interrumpe la espera
     * @throws ApiException         si el servidor devuelve un error de contrato
     */
    public PruebaImagenDTO crear(PruebaImagenDTO prueba) throws IOException, InterruptedException {
        HttpResponse<String> respuesta = enviar(peticion("POST", "/api/catalogo/pruebas")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(prueba),
                        StandardCharsets.UTF_8))
                .build());
        return json.readValue(respuesta.body(), PruebaImagenDTO.class);
    }

    /**
     * Modifica una prueba existente (PUT).
     *
     * @param codigo codigo de la prueba a modificar
     * @param prueba nuevos datos
     * @return la prueba actualizada
     * @throws IOException          si el servidor no responde
     * @throws InterruptedException si se interrumpe la espera
     * @throws ApiException         si el servidor devuelve un error de contrato
     */
    public PruebaImagenDTO actualizar(String codigo, PruebaImagenDTO prueba)
            throws IOException, InterruptedException {
        HttpResponse<String> respuesta = enviar(peticion("PUT", "/api/catalogo/pruebas/" + codigo)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(prueba),
                        StandardCharsets.UTF_8))
                .build());
        return json.readValue(respuesta.body(), PruebaImagenDTO.class);
    }

    /**
     * Baja logica de una prueba (DELETE, 204 sin cuerpo).
     *
     * @param codigo codigo de la prueba a desactivar
     * @throws IOException          si el servidor no responde
     * @throws InterruptedException si se interrumpe la espera
     * @throws ApiException         si el servidor devuelve un error de contrato
     */
    public void desactivar(String codigo) throws IOException, InterruptedException {
        enviar(peticion("DELETE", "/api/catalogo/pruebas/" + codigo).DELETE().build());
    }

    /**
     * Reactiva una prueba desactivada (PUT /{codigo}/reactivar).
     *
     * @param codigo codigo de la prueba a reactivar
     * @return la prueba reactivada
     * @throws IOException          si el servidor no responde
     * @throws InterruptedException si se interrumpe la espera
     * @throws ApiException         si el servidor devuelve un error de contrato
     */
    public PruebaImagenDTO reactivar(String codigo) throws IOException, InterruptedException {
        HttpResponse<String> respuesta = enviar(peticion("PUT",
                "/api/catalogo/pruebas/" + codigo + "/reactivar")
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build());
        return json.readValue(respuesta.body(), PruebaImagenDTO.class);
    }

    private HttpRequest.Builder peticion(String metodo, String rutaYConsulta) {
        return HttpRequest.newBuilder(URI.create(baseUrl + rutaYConsulta))
                .header("Authorization", cabeceraAutorizacion)
                .header("Accept", "application/json");
    }

    private HttpResponse<String> enviar(HttpRequest peticion) throws IOException, InterruptedException {
        HttpResponse<String> respuesta =
                http.send(peticion, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (respuesta.statusCode() >= 400) {
            throw traducirError(respuesta);
        }
        return respuesta;
    }

    private ApiException traducirError(HttpResponse<String> respuesta) {
        try {
            JsonNode cuerpo = json.readTree(respuesta.body());
            return new ApiException(respuesta.statusCode(),
                    cuerpo.path("codigo").asText("ERROR_DESCONOCIDO"),
                    cuerpo.path("mensaje").asText(respuesta.body()));
        } catch (IOException cuerpoNoJson) {
            return new ApiException(respuesta.statusCode(), "ERROR_HTTP", respuesta.body());
        }
    }
}
