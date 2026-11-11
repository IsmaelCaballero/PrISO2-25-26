package es.uclm.iso2.solicitudes.web;

import es.uclm.iso2.common.shared.ApiError;
import es.uclm.iso2.common.solicitudes.SolicitudConflictoException;
import es.uclm.iso2.common.solicitudes.SolicitudNoEncontradaException;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Traduce los errores de las solicitudes a respuestas HTTP coherentes con el contrato
 * (404 solicitud inexistente, 409 duplicada/prueba no disponible, 400 validacion).
 *
 * @since 1.1.0 (CFG-04)
 */
@RestControllerAdvice(assignableTypes = SolicitudControlador.class)
public class SolicitudManejadorErrores {

    private static final Logger LOG = LoggerFactory.getLogger(SolicitudManejadorErrores.class);

    /**
     * Solicitud inexistente: 404 con {@code SOLICITUD_NO_ENCONTRADA}.
     *
     * @param e excepcion de contrato
     * @return respuesta 404
     */
    @ExceptionHandler(SolicitudNoEncontradaException.class)
    public ResponseEntity<ApiError> noEncontrado(SolicitudNoEncontradaException e) {
        return error(HttpStatus.NOT_FOUND, "SOLICITUD_NO_ENCONTRADA", e.getMessage());
    }

    /**
     * Conflicto de regla del CDU06: 409 con codigo estable segun el tipo.
     *
     * @param e excepcion de contrato con el tipo de conflicto
     * @return respuesta 409
     */
    @ExceptionHandler(SolicitudConflictoException.class)
    public ResponseEntity<ApiError> conflicto(SolicitudConflictoException e) {
        String codigo = switch (e.tipo()) {
            case DUPLICADA -> "CONFLICTO_SOLICITUD_DUPLICADA";
            case PRUEBA_NO_DISPONIBLE -> "CONFLICTO_PRUEBA_NO_DISPONIBLE";
            case NO_APTA_PARA_CITA -> "CONFLICTO_SOLICITUD_NO_APTA";
        };
        return error(HttpStatus.CONFLICT, codigo, e.getMessage());
    }

    /**
     * Datos invalidos (campo obligatorio en blanco, prioridad desconocida...):
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
     * Cualquier fallo inesperado: 500 con {@code ERROR_INTERNO} (el detalle va al log).
     *
     * @param e excepcion no esperada
     * @return respuesta 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> inesperado(Exception e) {
        LOG.error("fallo inesperado en solicitudes", e);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO", "fallo interno de solicitudes");
    }

    private static ResponseEntity<ApiError> error(HttpStatus estado, String codigo, String mensaje) {
        return ResponseEntity.status(estado)
                .body(new ApiError(codigo, mensaje, Instant.now()));
    }
}
