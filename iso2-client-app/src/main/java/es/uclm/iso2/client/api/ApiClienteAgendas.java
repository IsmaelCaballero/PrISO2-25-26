package es.uclm.iso2.client.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import es.uclm.iso2.common.agendas.AgendaFacade.AgendaDTO;
import es.uclm.iso2.common.agendas.AgendaFacade.FestivoDTO;
import es.uclm.iso2.common.agendas.AgendaFacade.FranjaDTO;
import es.uclm.iso2.common.agendas.AgendaFacade.FranjaGestionDTO;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

/**
 * Cliente HTTP de gestion de agendas de medicos y tecnicos (CDU13, Iter3/CFG-03).
 *
 * <p>Habla contra {@code /api/agendas} con el token Bearer emitido por
 * {@link ApiClienteAutenticacion} y (de)serializa los DTOs del contrato comun
 * {@code AgendaFacade}. La fecha y la hora viajan como cadenas ISO
 * ({@code 2026-11-16}, {@code 08:00}): el mapper registra {@code JavaTimeModule} y
 * desactiva las marcas de tiempo para coincidir con el Jackson de Spring Boot del
 * servidor.</p>
 *
 * <p>Los errores del servidor se traducen a {@link ApiException} con el codigo
 * estable del contrato: {@code AGENDA_NO_ENCONTRADA} (404),
 * {@code CONFLICTO_SOLAPE} / {@code CONFLICTO_FESTIVO} /
 * {@code CONFLICTO_AGENDA_EXISTENTE} (409) y {@code DATOS_INVALIDOS} (400), para
 * que la interfaz muestre el mensaje del servidor tal cual. La escritura esta
 * restringida en el servidor a ADMINISTRADOR y RECEPCIONISTA (matriz RBAC).</p>
 *
 * <p>No depende de JavaFX: es verificable en CI headless y reutilizada por los
 * tests de integracion cliente-servidor de {@code iso2-server-app}.</p>
 *
 * @since 0.1.3 (CFG-03)
 */
public class ApiClienteAgendas {

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
    public ApiClienteAgendas(String baseUrl, String tokenJwt) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.http = HttpClient.newHttpClient();
        // LocalDate/LocalTime de los DTOs viajan como cadenas ISO, igual que en el
        // servidor: JavaTimeModule + WRITE_DATES_AS_TIMESTAMPS desactivado.
        this.json = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(DeserializationFeature.FAIL_ON_TRAILING_TOKENS, true);
        this.cabeceraAutorizacion = "Bearer " + tokenJwt;
    }

    /**
     * Lista todas las agendas dadas de alta con sus franjas y festivos
     * (GET /api/agendas).
     *
     * @return agendas ordenadas por profesional
     * @throws IOException          si el servidor no responde
     * @throws InterruptedException si se interrumpe la espera
     * @throws ApiException         si el servidor devuelve un error de contrato
     */
    public List<AgendaDTO> listar() throws IOException, InterruptedException {
        HttpResponse<String> respuesta = enviar(peticion("GET", "/api/agendas").GET().build());
        return json.readValue(respuesta.body(), new TypeReference<List<AgendaDTO>>() {
        });
    }

    /**
     * Estado completo de la agenda de un profesional (GET /api/agendas/{profesionalId}).
     *
     * @param profesionalId identificador del profesional
     * @return la agenda del profesional
     * @throws IOException          si el servidor no responde
     * @throws InterruptedException si se interrumpe la espera
     * @throws ApiException         con codigo {@code AGENDA_NO_ENCONTRADA} si no existe
     */
    public AgendaDTO detalle(String profesionalId) throws IOException, InterruptedException {
        HttpResponse<String> respuesta = enviar(peticion("GET",
                "/api/agendas/" + profesionalId).GET().build());
        return json.readValue(respuesta.body(), AgendaDTO.class);
    }

    /**
     * Franjas libres de un profesional en un rango de fechas (contrato baseline
     * CDU12: GET /api/agendas/{profesionalId}/libres; los festivos quedan fuera).
     *
     * @param profesionalId identificador del profesional
     * @param desde         fecha inicial del rango (incluida)
     * @param hasta         fecha final del rango (incluida)
     * @return franjas disponibles ordenadas por fecha
     * @throws IOException          si el servidor no responde
     * @throws InterruptedException si se interrumpe la espera
     * @throws ApiException         si el servidor devuelve un error de contrato
     */
    public List<FranjaDTO> franjasLibres(String profesionalId, LocalDate desde, LocalDate hasta)
            throws IOException, InterruptedException {
        HttpResponse<String> respuesta = enviar(peticion("GET",
                "/api/agendas/" + profesionalId + "/libres?desde=" + desde + "&hasta=" + hasta)
                .GET().build());
        return json.readValue(respuesta.body(), new TypeReference<List<FranjaDTO>>() {
        });
    }

    /**
     * Da de alta una agenda vacia para un profesional (POST /api/agendas).
     *
     * @param profesionalId identificador del profesional
     * @return la agenda creada por el servidor
     * @throws IOException          si el servidor no responde
     * @throws InterruptedException si se interrumpe la espera
     * @throws ApiException         con codigo {@code CONFLICTO_AGENDA_EXISTENTE} (409)
     *                              o {@code DATOS_INVALIDOS} (400) si falla la regla
     */
    public AgendaDTO crearAgenda(String profesionalId) throws IOException, InterruptedException {
        HttpResponse<String> respuesta = enviar(peticion("POST", "/api/agendas")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        json.writeValueAsString(new AgendaDTO(profesionalId, List.of(), List.of())),
                        StandardCharsets.UTF_8))
                .build());
        return json.readValue(respuesta.body(), AgendaDTO.class);
    }

    /**
     * Anade una franja de disponibilidad (POST /api/agendas/{profesionalId}/franjas).
     *
     * @param profesionalId identificador del profesional
     * @param franja        dia, inicio y fin (el id se ignora en el alta)
     * @return la franja creada con su id asignado
     * @throws IOException          si el servidor no responde
     * @throws InterruptedException si se interrumpe la espera
     * @throws ApiException         con codigo {@code CONFLICTO_SOLAPE} o
     *                              {@code CONFLICTO_FESTIVO} (409),
     *                              {@code AGENDA_NO_ENCONTRADA} (404) o
     *                              {@code DATOS_INVALIDOS} (400) segun la regla violada
     */
    public FranjaGestionDTO anadirFranja(String profesionalId, FranjaGestionDTO franja)
            throws IOException, InterruptedException {
        HttpResponse<String> respuesta = enviar(peticion("POST",
                "/api/agendas/" + profesionalId + "/franjas")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(franja),
                        StandardCharsets.UTF_8))
                .build());
        return json.readValue(respuesta.body(), FranjaGestionDTO.class);
    }

    /**
     * Elimina una franja de la agenda (DELETE .../franjas/{franjaId}, 204 sin cuerpo).
     *
     * @param profesionalId identificador del profesional
     * @param franjaId      id de la franja a eliminar
     * @throws IOException          si el servidor no responde
     * @throws InterruptedException si se interrumpe la espera
     * @throws ApiException         con codigo {@code AGENDA_NO_ENCONTRADA} (404)
     */
    public void eliminarFranja(String profesionalId, Long franjaId)
            throws IOException, InterruptedException {
        enviar(peticion("DELETE", "/api/agendas/" + profesionalId + "/franjas/" + franjaId)
                .DELETE().build());
    }

    /**
     * Anade un festivo a la agenda (POST .../festivos). El festivo manda sobre las
     * franjas: si ese dia ya tiene franjas el servidor responde 409.
     *
     * @param profesionalId identificador del profesional
     * @param festivo       dia y motivo (el id se ignora en el alta)
     * @return el festivo creado con su id asignado
     * @throws IOException          si el servidor no responde
     * @throws InterruptedException si se interrumpe la espera
     * @throws ApiException         con codigo {@code CONFLICTO_FESTIVO} (409),
     *                              {@code AGENDA_NO_ENCONTRADA} (404) o
     *                              {@code DATOS_INVALIDOS} (400) segun la regla violada
     */
    public FestivoDTO anadirFestivo(String profesionalId, FestivoDTO festivo)
            throws IOException, InterruptedException {
        HttpResponse<String> respuesta = enviar(peticion("POST",
                "/api/agendas/" + profesionalId + "/festivos")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(festivo),
                        StandardCharsets.UTF_8))
                .build());
        return json.readValue(respuesta.body(), FestivoDTO.class);
    }

    /**
     * Elimina un festivo de la agenda (DELETE .../festivos/{festivoId}, 204 sin cuerpo).
     *
     * @param profesionalId identificador del profesional
     * @param festivoId     id del festivo a eliminar
     * @throws IOException          si el servidor no responde
     * @throws InterruptedException si se interrumpe la espera
     * @throws ApiException         con codigo {@code AGENDA_NO_ENCONTRADA} (404)
     */
    public void eliminarFestivo(String profesionalId, Long festivoId)
            throws IOException, InterruptedException {
        enviar(peticion("DELETE", "/api/agendas/" + profesionalId + "/festivos/" + festivoId)
                .DELETE().build());
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
