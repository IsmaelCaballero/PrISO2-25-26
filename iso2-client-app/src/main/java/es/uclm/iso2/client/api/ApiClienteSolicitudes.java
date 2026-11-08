package es.uclm.iso2.client.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import es.uclm.iso2.common.solicitudes.SolicitudFacade.SolicitudDTO;
import es.uclm.iso2.common.solicitudes.SolicitudFacade.SolicitudPrescripcionDTO;
import es.uclm.iso2.common.solicitudes.SolicitudFacade.TrazabilidadSolicitudDTO;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Cliente HTTP de solicitudes de prueba (CDU06, Iter4/CFG-04).
 *
 * <p>Habla contra {@code /api/solicitudes} con el token de la sesion como
 * {@code Authorization: Bearer} y (de)serializa los DTOs de
 * {@code es.uclm.iso2.common.solicitudes.SolicitudFacade}. El {@code Instant} de la
 * trazabilidad viaja como cadena ISO ({@code JavaTimeModule} +
 * {@code WRITE_DATES_AS_TIMESTAMPS} desactivado), igual que en el Jackson de Spring
 * Boot del servidor.</p>
 *
 * <p>No repudio (RF06.2): el cuerpo del alta lleva solo paciente, prueba, indicacion,
 * prioridad y origen. El {@code medicoId} del DTO del contrato se usa unicamente para
 * validar en el cliente los campos obligatorios de la RF06.1 antes de llamar a la red;
 * NO se serializa: el servidor fija el autor con el claim del token, de modo que el
 * cliente no puede fingir que otro medico prescribio.</p>
 *
 * <p>Los errores del servidor se traducen a {@link ApiException} con el codigo estable
 * del contrato: {@code SOLICITUD_NO_ENCONTRADA} (404),
 * {@code CONFLICTO_SOLICITUD_DUPLICADA} y {@code CONFLICTO_PRUEBA_NO_DISPONIBLE} (409)
 * y {@code DATOS_INVALIDOS} (400). Escribir exige {@code solicitud:escribir} en la
 * matriz RBAC (ADMINISTRADOR y MEDICO).</p>
 *
 * <p>No depende de JavaFX: es verificable en CI headless.</p>
 *
 * @since 0.1.4 (CFG-04)
 */
public class ApiClienteSolicitudes {

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
    public ApiClienteSolicitudes(String baseUrl, String tokenJwt) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.http = HttpClient.newHttpClient();
        // El Instant de la trazabilidad viaja como texto ISO, no como marca numerica.
        this.json = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(DeserializationFeature.FAIL_ON_TRAILING_TOKENS, true);
        this.cabeceraAutorizacion = "Bearer " + tokenJwt;
    }

    /**
     * Cuerpo real del alta: los cinco campos que acepta el controlador. Sin
     * {@code medicoId} a proposito (RF06.2: el autor es el claim del token).
     */
    private record PrescripcionRequest(String pacienteId, String codigoPrueba, String indicacionClinica,
                                       String prioridad, String origen) {
    }

    /**
     * Prescribe una prueba a nombre del medico autenticado (POST /api/solicitudes).
     *
     * @param prescripcion datos de la prescripcion; el {@code medicoId} del DTO solo
     *                     sirve para validar RF06.1 en el cliente y no viaja en el cuerpo
     * @return la solicitud creada por el servidor, con su {@code solicitudId} y su fecha
     * @throws IOException          si el servidor no responde
     * @throws InterruptedException si se interrumpe la espera
     * @throws ApiException         con codigo {@code CONFLICTO_SOLICITUD_DUPLICADA} o
     *                              {@code CONFLICTO_PRUEBA_NO_DISPONIBLE} (409) o
     *                              {@code DATOS_INVALIDOS} (400) segun la regla violada
     */
    public SolicitudDTO prescribir(SolicitudPrescripcionDTO prescripcion)
            throws IOException, InterruptedException {
        PrescripcionRequest cuerpo = new PrescripcionRequest(
                prescripcion.pacienteId(), prescripcion.codigoPrueba(), prescripcion.indicacionClinica(),
                prescripcion.prioridad().name(), prescripcion.origen());
        HttpResponse<String> respuesta = enviar(peticion("POST", "/api/solicitudes")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(cuerpo),
                        StandardCharsets.UTF_8))
                .build());
        return json.readValue(respuesta.body(), SolicitudDTO.class);
    }

    /**
     * Solicitudes prescritas por el usuario autenticado, de la mas reciente a la mas
     * antigua (GET /api/solicitudes/mias). El identificador del medico lo toma el
     * servidor del token, por eso la llamada no lo lleva.
     *
     * @return solicitudes del medico autenticado
     * @throws IOException          si el servidor no responde
     * @throws InterruptedException si se interrumpe la espera
     * @throws ApiException         si el servidor devuelve un error de contrato
     */
    public List<SolicitudDTO> misSolicitudes() throws IOException, InterruptedException {
        HttpResponse<String> respuesta = enviar(peticion("GET", "/api/solicitudes/mias").GET().build());
        return json.readValue(respuesta.body(), new TypeReference<List<SolicitudDTO>>() {
        });
    }

    /**
     * Detalle de una solicitud (GET /api/solicitudes/{id}).
     *
     * @param solicitudId identificador funcional (UUID) de la solicitud
     * @return la solicitud
     * @throws IOException          si el servidor no responde
     * @throws InterruptedException si se interrumpe la espera
     * @throws ApiException         con codigo {@code SOLICITUD_NO_ENCONTRADA} si no existe
     */
    public SolicitudDTO detalle(String solicitudId) throws IOException, InterruptedException {
        HttpResponse<String> respuesta = enviar(peticion("GET",
                "/api/solicitudes/" + solicitudId).GET().build());
        return json.readValue(respuesta.body(), SolicitudDTO.class);
    }

    /**
     * Trazabilidad de no repudio de una solicitud (GET /api/solicitudes/{id}/integridad):
     * autor, instante, huella SHA-256 y el resultado de recalcularla sobre lo guardado.
     *
     * @param solicitudId identificador funcional (UUID) de la solicitud
     * @return la trazabilidad; si {@code integridadOk} es {@code false}, la fila de la
     *         base de datos fue manipulada fuera de la aplicacion
     * @throws IOException          si el servidor no responde
     * @throws InterruptedException si se interrumpe la espera
     * @throws ApiException         con codigo {@code SOLICITUD_NO_ENCONTRADA} si no existe
     */
    public TrazabilidadSolicitudDTO integridad(String solicitudId)
            throws IOException, InterruptedException {
        HttpResponse<String> respuesta = enviar(peticion("GET",
                "/api/solicitudes/" + solicitudId + "/integridad").GET().build());
        return json.readValue(respuesta.body(), TrazabilidadSolicitudDTO.class);
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
