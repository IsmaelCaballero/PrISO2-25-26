package es.uclm.iso2.citas.web;

import es.uclm.iso2.common.shared.ApiError;
import es.uclm.iso2.common.citas.CitaConflictoException;
import es.uclm.iso2.common.citas.CitaNoEncontradaException;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Traduce los errores de las citas a respuestas HTTP coherentes con el contrato: 409 con
 * un codigo estable segun el conflicto, 404 si la cita no existe y 400 si faltan datos.
 *
 * <p>Los dos manejadores de fallos de acceso a datos no son un detalle tecnico: son la
 * senala de la concurrencia real. Cuando dos usuarios reservan el mismo hueco, la decision
 * la toma la base de datos al violar {@code UNIQUE (profesional_id, inicio)} de la tabla
 * de huecos, y ese fallo llega hasta aqui sin transaccion que lo envuelva. Tambien llega
 * el {@code ObjectOptimisticLockingFailureException} cuando dos reprogramaciones pretenden
 * mover la misma fila de hueco. Los dos se responden como 409
 * {@code CONFLICTO_HUECO_OCUPADO}, que es lo que espera la pantalla de recepcion para
 * avisar de que el hueco ya no esta libre.</p>
 *
 * @since 1.1.0 (CFG-05, CDU12)
 */
@RestControllerAdvice(assignableTypes = CitaControlador.class)
public class CitaManejadorErrores {

    private static final Logger LOG = LoggerFactory.getLogger(CitaManejadorErrores.class);

    /**
     * Cita inexistente: 404 con {@code CITA_NO_ENCONTRADA}.
     *
     * @param e excepcion de contrato
     * @return respuesta 404
     */
    @ExceptionHandler(CitaNoEncontradaException.class)
    public ResponseEntity<ApiError> noEncontrada(CitaNoEncontradaException e) {
        return error(HttpStatus.NOT_FOUND, "CITA_NO_ENCONTRADA", e.getMessage());
    }

    /**
     * Conflicto de regla del CDU12: 409 con codigo estable segun el tipo.
     *
     * @param e excepcion de contrato con el tipo de conflicto
     * @return respuesta 409
     */
    @ExceptionHandler(CitaConflictoException.class)
    public ResponseEntity<ApiError> conflicto(CitaConflictoException e) {
        String codigo = switch (e.tipo()) {
            case HUECO_OCUPADO -> "CONFLICTO_HUECO_OCUPADO";
            case FUERA_DE_FRANJA -> "CONFLICTO_FUERA_DE_FRANJA";
            case TRANSICION_INVALIDA -> "CONFLICTO_TRANSICION_INVALIDA";
            case SOLICITUD_NO_APTA -> "CONFLICTO_SOLICITUD_NO_APTA";
            case PLAZO_CANCELACION -> "CONFLICTO_PLAZO_CANCELACION";
        };
        return error(HttpStatus.CONFLICT, codigo, e.getMessage());
    }

    /**
     * El hueco quedo doblemente reservado o dos usuarios movieron a la vez la misma cita:
     * la base de datos lo impide y aqui se traduce al mismo 409 que devuelve la
     * comprobacion previa. El hueco sigue bloqueado para la primera cita, que es la
     * garantia que exige la matriz de calidad de CFG-05.
     *
     * @param e fallo de integridad o de control optimista
     * @return respuesta 409 con {@code CONFLICTO_HUECO_OCUPADO}
     */
    @ExceptionHandler({DataIntegrityViolationException.class,
            ObjectOptimisticLockingFailureException.class})
    public ResponseEntity<ApiError> huecoEnDisputa(Exception e) {
        LOG.warn("hueco en disputa al reservar o reprogramar una cita: {}", e.getMessage());
        return error(HttpStatus.CONFLICT, "CONFLICTO_HUECO_OCUPADO",
                "el hueco pedido ya no esta libre u otra operacion lo esta moviendo a la vez");
    }

    /**
     * Datos invalidos (profesional o inicio ausentes, duracion no positiva, estado
     * desconocido, rango incoherente...): 400 con {@code DATOS_INVALIDOS}.
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
        LOG.error("fallo inesperado en citas", e);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO", "fallo interno de citas");
    }

    private static ResponseEntity<ApiError> error(HttpStatus estado, String codigo, String mensaje) {
        return ResponseEntity.status(estado)
                .body(new ApiError(codigo, mensaje, Instant.now()));
    }
}
