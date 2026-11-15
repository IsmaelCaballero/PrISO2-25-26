package es.uclm.iso2.integracion.web;

import es.uclm.iso2.common.integracion.CredencialesInvalidasException;
import es.uclm.iso2.common.integracion.EstudioNoEncontradoException;
import es.uclm.iso2.common.integracion.HistoriaNoEncontradaException;
import es.uclm.iso2.common.integracion.SistemaExternoException;
import es.uclm.iso2.common.integracion.SistemaNoEncontradoException;
import es.uclm.iso2.common.shared.ApiError;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Traduce los errores del CDU19 a las respuestas HTTP que fija el contrato: 401 para
 * credenciales rechazadas por el IdP, 404 para historia/estudio/sistema inexistentes y
 * 503 con codigo estable segun el tipo de fallo del sistema externo. La precedencia
 * 503 &gt; 401 &gt; 400 no se decide aqui: la garantiza el adaptador comprobando la
 * politica antes que la peticion; este advice solo la pinta.
 *
 * <p>Limitado a los dos controladores del modulo (como el resto de advice del
 * proyecto) para no solaparse con los de catalogos, agendas, citas o solicitudes.</p>
 *
 * @since 1.1.0 (CFG-06, CDU19)
 */
@RestControllerAdvice(assignableTypes = {IntegracionControlador.class, SimulacionControlador.class})
public class IntegracionManejadorErrores {

    private static final Logger LOG = LoggerFactory.getLogger(IntegracionManejadorErrores.class);

    /**
     * El IdP rechazo las credenciales: 401 con {@code CREDENCIALES_INVALIDAS}.
     *
     * @param e excepcion de contrato
     * @return respuesta 401
     */
    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<ApiError> credenciales(CredencialesInvalidasException e) {
        return error(HttpStatus.UNAUTHORIZED, "CREDENCIALES_INVALIDAS", e.getMessage());
    }

    /**
     * Paciente inexistente en la HCE externa: 404 con {@code HISTORIA_NO_ENCONTRADA}.
     *
     * @param e excepcion de contrato
     * @return respuesta 404
     */
    @ExceptionHandler(HistoriaNoEncontradaException.class)
    public ResponseEntity<ApiError> historiaNoEncontrada(HistoriaNoEncontradaException e) {
        return error(HttpStatus.NOT_FOUND, "HISTORIA_NO_ENCONTRADA", e.getMessage());
    }

    /**
     * Estudio inexistente en el RIS: 404 con {@code ESTUDIO_NO_ENCONTRADO}.
     *
     * @param e excepcion de contrato
     * @return respuesta 404
     */
    @ExceptionHandler(EstudioNoEncontradoException.class)
    public ResponseEntity<ApiError> estudioNoEncontrado(EstudioNoEncontradoException e) {
        return error(HttpStatus.NOT_FOUND, "ESTUDIO_NO_ENCONTRADO", e.getMessage());
    }

    /**
     * Nombre de sistema desconocido en el ping o en la simulacion: 404 con
     * {@code SISTEMA_NO_ENCONTRADO}.
     *
     * @param e excepcion de contrato
     * @return respuesta 404
     */
    @ExceptionHandler(SistemaNoEncontradoException.class)
    public ResponseEntity<ApiError> sistemaNoEncontrado(SistemaNoEncontradoException e) {
        return error(HttpStatus.NOT_FOUND, "SISTEMA_NO_ENCONTRADO", e.getMessage());
    }

    /**
     * El sistema externo esta caido o agoto el tiempo de espera: 503 con
     * {@code SISTEMA_NO_DISPONIBLE} o {@code SISTEMA_TIEMPO_SUPERADO}.
     *
     * @param e excepcion de contrato con el tipo de fallo
     * @return respuesta 503
     */
    @ExceptionHandler(SistemaExternoException.class)
    public ResponseEntity<ApiError> sistemaExterno(SistemaExternoException e) {
        String codigo = switch (e.tipo()) {
            case NO_DISPONIBLE -> "SISTEMA_NO_DISPONIBLE";
            case TIEMPO_SUPERADO -> "SISTEMA_TIEMPO_SUPERADO";
        };
        return error(HttpStatus.SERVICE_UNAVAILABLE, codigo, e.getMessage());
    }

    /**
     * Datos invalidos (identificadores vacios, modo desconocido, cifras ausentes...):
     * 400 con {@code DATOS_INVALIDOS}.
     *
     * @param e excepcion de validacion
     * @return respuesta 400
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> invalidos(IllegalArgumentException e) {
        return error(HttpStatus.BAD_REQUEST, "DATOS_INVALIDOS", e.getMessage());
    }

    /**
     * Cuerpo JSON mal formado: 400 con {@code DATOS_INVALIDOS}.
     *
     * @param e error de lectura del mensaje HTTP
     * @return respuesta 400
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> cuerpoIlegible(HttpMessageNotReadableException e) {
        return error(HttpStatus.BAD_REQUEST, "DATOS_INVALIDOS", "el cuerpo JSON no es legible");
    }

    /**
     * Content-Type no soportado: 415 con {@code CONTENIDO_NO_ACEPTADO} (D35, como en
     * catalogos).
     *
     * @param e error de medio de contenido
     * @return respuesta 415
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiError> contenidoNoAceptado(HttpMediaTypeNotSupportedException e) {
        return error(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "CONTENIDO_NO_ACEPTADO",
                "el Content-Type enviado no esta soportado");
    }

    /**
     * Cualquier fallo inesperado: 500 con {@code ERROR_INTERNO} (el detalle va al log).
     *
     * @param e excepcion no esperada
     * @return respuesta 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> inesperado(Exception e) {
        LOG.error("fallo inesperado en integracion", e);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO", "fallo interno de integracion");
    }

    private static ResponseEntity<ApiError> error(HttpStatus estado, String codigo, String mensaje) {
        return ResponseEntity.status(estado)
                .body(new ApiError(codigo, mensaje, Instant.now()));
    }
}
