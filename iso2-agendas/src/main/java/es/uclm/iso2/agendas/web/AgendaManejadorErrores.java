package es.uclm.iso2.agendas.web;

import es.uclm.iso2.common.agendas.AgendaConflictoException;
import es.uclm.iso2.common.agendas.AgendaNoEncontradoException;
import es.uclm.iso2.common.shared.ApiError;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Traduce los errores de las agendas a respuestas HTTP coherentes con el contrato
 * (404 agenda/franja/festivo inexistentes, 409 solape/festivo/agenda duplicada,
 * 400 validacion).
 *
 * @since 1.1.0 (CFG-03)
 */
@RestControllerAdvice(assignableTypes = AgendaControlador.class)
public class AgendaManejadorErrores {

    private static final Logger LOG = LoggerFactory.getLogger(AgendaManejadorErrores.class);

    /**
     * Agenda, franja o festivo inexistentes: 404 con {@code AGENDA_NO_ENCONTRADA}.
     *
     * @param e excepcion de contrato
     * @return respuesta 404
     */
    @ExceptionHandler(AgendaNoEncontradoException.class)
    public ResponseEntity<ApiError> noEncontrado(AgendaNoEncontradoException e) {
        return error(HttpStatus.NOT_FOUND, "AGENDA_NO_ENCONTRADA", e.getMessage());
    }

    /**
     * Conflicto de regla de negocio: 409 con codigo estable segun el tipo de conflicto.
     *
     * @param e excepcion de contrato con el tipo de conflicto
     * @return respuesta 409
     */
    @ExceptionHandler(AgendaConflictoException.class)
    public ResponseEntity<ApiError> conflicto(AgendaConflictoException e) {
        String codigo = switch (e.tipo()) {
            case SOLAPE -> "CONFLICTO_SOLAPE";
            case FESTIVO -> "CONFLICTO_FESTIVO";
            case AGENDA_DUPLICADA -> "CONFLICTO_AGENDA_EXISTENTE";
        };
        return error(HttpStatus.CONFLICT, codigo, e.getMessage());
    }

    /**
     * Datos invalidos (franja con inicio posterior o igual al fin, festivo sin dia...):
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
        LOG.error("fallo inesperado en agendas", e);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO", "fallo interno de agendas");
    }

    private static ResponseEntity<ApiError> error(HttpStatus estado, String codigo, String mensaje) {
        return ResponseEntity.status(estado)
                .body(new ApiError(codigo, mensaje, Instant.now()));
    }
}
