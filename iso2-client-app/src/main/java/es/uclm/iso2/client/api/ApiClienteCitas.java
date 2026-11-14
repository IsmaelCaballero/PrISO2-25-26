package es.uclm.iso2.client.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import es.uclm.iso2.common.citas.CitaFacade.CitaDTO;
import es.uclm.iso2.common.citas.CitaFacade.CitaNuevaDTO;
import es.uclm.iso2.common.citas.CitaFacade.DetalleCitaDTO;
import es.uclm.iso2.common.citas.CitaFacade.HuecoDTO;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Cliente HTTP de citas sobre agendas (CDU12, Iter5/CFG-05).
 *
 * <p>Habla contra {@code /api/citas} con el token Bearer emitido por
 * {@link ApiClienteAutenticacion} y (de)serializa los DTOs del contrato comun
 * {@code CitaFacade}. El inicio y el fin viajan como cadenas ISO con segundos
 * ({@code 2027-06-01T08:00:00}): el mapper registra {@code JavaTimeModule} y
 * desactiva las marcas de tiempo para coincidir con el Jackson de Spring Boot del
 * servidor; los filtros de listado van por query con {@code LocalDateTime} y las
 * fechas del motor de huecos con {@code LocalDate}.</p>
 *
 * <p>Los errores del servidor se traducen a {@link ApiException} con el codigo
 * estable del contrato: {@code CITA_NO_ENCONTRADA} (404),
 * {@code CONFLICTO_HUECO_OCUPADO} / {@code CONFLICTO_FUERA_DE_FRANJA} /
 * {@code TRANSICION_INVALIDA} / {@code PLAZO_CANCELACION} /
 * {@code NO_APTA_PARA_CITA} (409, D36) y {@code DATOS_INVALIDOS} (400), para que
 * la interfaz muestre el mensaje del servidor tal cual. La lectura exige
 * {@code cita:leer} (ADMINISTRADOR, MEDICO y RECEPCIONISTA) y la escritura
 * {@code cita:escribir} (solo ADMINISTRADOR y RECEPCIONISTA, matriz RBAC).</p>
 *
 * <p>No depende de JavaFX: es verificable en CI headless y reutilizada por los
 * tests de integracion cliente-servidor de {@code iso2-server-app}.</p>
 *
 * @since 0.1.5 (CFG-05)
 */
public class ApiClienteCitas {

    /** Formato de query aceptado por {@code @DateTimeFormat(ISO.DATE_TIME)} del servidor. */
    private static final DateTimeFormatter FECHA_HORA_ISO =
            DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss");

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
    public ApiClienteCitas(String baseUrl, String tokenJwt) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.http = HttpClient.newHttpClient();
        // LocalDateTime/LocalDate de los DTOs viajan como cadenas ISO, igual que en
        // el servidor: JavaTimeModule + WRITE_DATES_AS_TIMESTAMPS desactivado.
        this.json = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(DeserializationFeature.FAIL_ON_TRAILING_TOKENS, true);
        this.cabeceraAutorizacion = "Bearer " + tokenJwt;
    }

    /**
     * Lista las citas con los filtros de la pantalla de recepcion (UC12.8):
     * GET /api/citas con los filtros opcionales que vengan rellenos.
     *
     * @param desde         inicio inclusivo del rango, opcional ({@code null} sin filtro)
     * @param hasta         fin inclusivo del rango, opcional ({@code null} sin filtro)
     * @param profesionalId filtro por profesional, opcional ({@code null}/vacio sin filtro)
     * @param estado        estado en minusculas, opcional ({@code null}/vacio sin filtro)
     * @return citas que cumplen los filtros, ordenadas por inicio
     * @throws IOException          si el servidor no responde
     * @throws InterruptedException si se interrumpe la espera
     * @throws ApiException         con codigo {@code DATOS_INVALIDOS} (400) si el rango
     *                              no es coherente o el estado no es de la maquina de estados
     */
    public List<DetalleCitaDTO> listar(LocalDateTime desde, LocalDateTime hasta,
                                       String profesionalId, String estado)
            throws IOException, InterruptedException {
        List<String> filtros = new ArrayList<>();
        if (desde != null) {
            filtros.add("desde=" + FECHA_HORA_ISO.format(desde));
        }
        if (hasta != null) {
            filtros.add("hasta=" + FECHA_HORA_ISO.format(hasta));
        }
        if (profesionalId != null && !profesionalId.isBlank()) {
            filtros.add("profesionalId=" + profesionalId);
        }
        if (estado != null && !estado.isBlank()) {
            filtros.add("estado=" + estado);
        }
        String consulta = filtros.isEmpty() ? "" : "?" + String.join("&", filtros);
        HttpResponse<String> respuesta = enviar(peticion("GET", "/api/citas" + consulta).GET().build());
        return json.readValue(respuesta.body(), new TypeReference<List<DetalleCitaDTO>>() {
        });
    }

    /**
     * Detalle completo de una cita, con profesional y motivo de cancelacion
     * (GET /api/citas/{citaId}).
     *
     * @param citaId identificador funcional (UUID) de la cita
     * @return el detalle de la cita
     * @throws IOException          si el servidor no responde
     * @throws InterruptedException si se interrumpe la espera
     * @throws ApiException         con codigo {@code CITA_NO_ENCONTRADA} si no existe
     */
    public DetalleCitaDTO detalle(String citaId) throws IOException, InterruptedException {
        HttpResponse<String> respuesta = enviar(peticion("GET",
                "/api/citas/" + citaId).GET().build());
        return json.readValue(respuesta.body(), DetalleCitaDTO.class);
    }

    /**
     * Motor de asignacion de huecos (UC12.2): huecos libres de un profesional en un
     * rango de dias descontando los bloqueados por citas activas
     * (GET /api/citas/huecos; los festivos del CDU13 nunca aparecen).
     *
     * @param profesionalId   profesional cuya agenda se consulta
     * @param desde           primer dia del rango (inclusive)
     * @param hasta           ultimo dia del rango (inclusive)
     * @param duracionMinutos duracion que debe caber en el hueco; {@code null} pide
     *                        las franjas libres enteras
     * @return huecos libres ordenados por inicio
     * @throws IOException          si el servidor no responde
     * @throws InterruptedException si se interrumpe la espera
     * @throws ApiException         con codigo {@code DATOS_INVALIDOS} (400) o
     *                              {@code AGENDA_NO_ENCONTRADA} (404) segun la regla violada
     */
    public List<HuecoDTO> huecosLibres(String profesionalId, LocalDate desde, LocalDate hasta,
                                       Integer duracionMinutos)
            throws IOException, InterruptedException {
        String duracion = duracionMinutos == null ? "" : "&duracionMinutos=" + duracionMinutos;
        HttpResponse<String> respuesta = enviar(peticion("GET",
                "/api/citas/huecos?profesionalId=" + profesionalId
                        + "&desde=" + desde + "&hasta=" + hasta + duracion)
                .GET().build());
        return json.readValue(respuesta.body(), new TypeReference<List<HuecoDTO>>() {
        });
    }

    /**
     * Agenda una cita sobre un hueco libre y lo bloquea en la misma transaccion
     * (POST /api/citas, UC12.1/UC12.2).
     *
     * @param nueva solicitud (opcional), profesional, inicio y duracion
     * @return la cita creada en estado programada con su identificador
     * @throws IOException          si el servidor no responde
     * @throws InterruptedException si se interrumpe la espera
     * @throws ApiException         con codigo {@code CONFLICTO_HUECO_OCUPADO} o
     *                              {@code CONFLICTO_FUERA_DE_FRANJA} (409, D36),
     *                              {@code NO_APTA_PARA_CITA} (409 si la solicitud no esta
     *                              en {@code creada}) o {@code DATOS_INVALIDOS} (400)
     */
    public CitaDTO agendar(CitaNuevaDTO nueva) throws IOException, InterruptedException {
        HttpResponse<String> respuesta = enviar(peticion("POST", "/api/citas")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(nueva),
                        StandardCharsets.UTF_8))
                .build());
        return json.readValue(respuesta.body(), CitaDTO.class);
    }

    /**
     * Confirma la asistencia de una cita programada (POST /api/citas/{citaId}/confirmar,
     * UC12.3).
     *
     * @param citaId identificador funcional de la cita
     * @return la cita confirmada
     * @throws IOException          si el servidor no responde
     * @throws InterruptedException si se interrumpe la espera
     * @throws ApiException         con codigo {@code CITA_NO_ENCONTRADA} (404) o
     *                              {@code TRANSICION_INVALIDA} (409) segun la maquina de estados
     */
    public CitaDTO confirmar(String citaId) throws IOException, InterruptedException {
        return accionSobreCita("confirmar", citaId);
    }

    /**
     * Registra la prueba como realizada y libera el hueco
     * (POST /api/citas/{citaId}/realizar, UC12.6).
     *
     * @param citaId identificador funcional de la cita
     * @return la cita realizada
     * @throws IOException          si el servidor no responde
     * @throws InterruptedException si se interrumpe la espera
     * @throws ApiException         con codigo {@code CITA_NO_ENCONTRADA} (404) o
     *                              {@code TRANSICION_INVALIDA} (409) segun la maquina de estados
     */
    public CitaDTO realizar(String citaId) throws IOException, InterruptedException {
        return accionSobreCita("realizar", citaId);
    }

    /**
     * Reprograma la cita a un hueco nuevo: libera el antiguo, bloquea el de destino y
     * valida de nuevo franja y festivo (POST /api/citas/{citaId}/reprogramar, UC12.4).
     *
     * @param citaId identificador funcional de la cita
     * @param inicio nuevo inicio de la cita
     * @return la cita con su nueva franja horaria
     * @throws IOException          si el servidor no responde
     * @throws InterruptedException si se interrumpe la espera
     * @throws ApiException         con codigo {@code CONFLICTO_HUECO_OCUPADO} o
     *                              {@code CONFLICTO_FUERA_DE_FRANJA} (409, la cita conserva
     *                              su hueco anterior) o {@code TRANSICION_INVALIDA} (409)
     */
    public CitaDTO reprogramar(String citaId, LocalDateTime inicio)
            throws IOException, InterruptedException {
        HttpResponse<String> respuesta = enviar(peticion("POST",
                "/api/citas/" + citaId + "/reprogramar")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        json.writeValueAsString(new ReprogramacionBody(inicio)),
                        StandardCharsets.UTF_8))
                .build());
        return json.readValue(respuesta.body(), CitaDTO.class);
    }

    /**
     * Cancela la cita con motivo obligatorio y libera su hueco
     * (POST /api/citas/{citaId}/cancelar, UC12.5).
     *
     * @param citaId identificador funcional de la cita
     * @param motivo motivo de la cancelacion (obligatorio, RF12.5)
     * @return la cita cancelada (el motivo solo se ve despues en el detalle)
     * @throws IOException          si el servidor no responde
     * @throws InterruptedException si se interrumpe la espera
     * @throws ApiException         con codigo {@code TRANSICION_INVALIDA} (409, gana sobre
     *                              el plazo, D36), {@code PLAZO_CANCELACION} (409) o
     *                              {@code DATOS_INVALIDOS} (400, motivo en blanco)
     */
    public CitaDTO cancelar(String citaId, String motivo) throws IOException, InterruptedException {
        HttpResponse<String> respuesta = enviar(peticion("POST",
                "/api/citas/" + citaId + "/cancelar")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        json.writeValueAsString(new CancelacionBody(motivo)),
                        StandardCharsets.UTF_8))
                .build());
        return json.readValue(respuesta.body(), CitaDTO.class);
    }

    /**
     * Lote de mantenimiento que expira las citas no atendidas y libera sus huecos
     * (POST /api/citas/expirar-vencidas, UC12.7). Lo lanza recepcion o el sistema.
     *
     * @param corte instante de referencia; {@code null} usa el reloj del servidor
     * @return citas que han pasado a expirada
     * @throws IOException          si el servidor no responde
     * @throws InterruptedException si se interrumpe la espera
     * @throws ApiException         si el servidor devuelve un error de contrato
     */
    public List<CitaDTO> expirarVencidas(LocalDateTime corte) throws IOException, InterruptedException {
        String cuerpo = corte == null ? "{}"
                : json.writeValueAsString(new ExpiracionBody(corte));
        HttpResponse<String> respuesta = enviar(peticion("POST", "/api/citas/expirar-vencidas")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(cuerpo, StandardCharsets.UTF_8))
                .build());
        return json.readValue(respuesta.body(), new TypeReference<List<CitaDTO>>() {
        });
    }

    /**
     * Cuerpo del alta de reprogramacion: {"inicio": "2027-06-01T08:00:00"}.
     */
    record ReprogramacionBody(LocalDateTime inicio) {
    }

    /**
     * Cuerpo del alta de cancelacion: {"motivo": "paciente hospitalizado"}.
     */
    record CancelacionBody(String motivo) {
    }

    /**
     * Cuerpo del lote de expiracion: {"corte": "2026-11-14T08:00:00"}.
     */
    record ExpiracionBody(LocalDateTime corte) {
    }

    private CitaDTO accionSobreCita(String accion, String citaId)
            throws IOException, InterruptedException {
        HttpResponse<String> respuesta = enviar(peticion("POST",
                "/api/citas/" + citaId + "/" + accion)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build());
        return json.readValue(respuesta.body(), CitaDTO.class);
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
