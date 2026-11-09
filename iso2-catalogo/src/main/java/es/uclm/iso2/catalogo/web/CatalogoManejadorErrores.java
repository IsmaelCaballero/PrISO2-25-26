package es.uclm.iso2.catalogo.web;

import es.uclm.iso2.common.catalogo.CatalogoConflictoException;
import es.uclm.iso2.common.catalogo.CatalogoNoEncontradoException;
import es.uclm.iso2.common.shared.ApiError;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Traduce los errores del catalogo a respuestas HTTP coherentes con el contrato
 * (404 prueba inexistente, 409 conflicto de codigo, 400 validacion de cuerpo o de
 * parametros de consulta, 415 medio de peticion no soportado).
 *
 * <p>El orden de los capturadores importa: los especificos ganan al de {@link Exception},
 * que es la red de seguridad y por eso registra en el log. Desde D35 ningun fallo de
 * validacion ni de binding debe acabar en ese ultimo.</p>
 *
 * @since 1.1.0 (CFG-01)
 */
@RestControllerAdvice(assignableTypes = CatalogoControlador.class)
public class CatalogoManejadorErrores {

    private static final Logger LOG = LoggerFactory.getLogger(CatalogoManejadorErrores.class);

    /**
     * Prueba inexistente: 404 con codigo estable {@code PRUEBA_NO_ENCONTRADA}.
     *
     * @param e excepcion de contrato
     * @return respuesta 404
     */
    @ExceptionHandler(CatalogoNoEncontradoException.class)
    public ResponseEntity<ApiError> noEncontrado(CatalogoNoEncontradoException e) {
        return error(HttpStatus.NOT_FOUND, "PRUEBA_NO_ENCONTRADA", e.getMessage());
    }

    /**
     * Conflicto de negocio (codigo duplicado, estado que impide la operacion): 409 con
     * codigo estable {@code CODIGO_DUPLICADO}.
     *
     * @param e excepcion de contrato
     * @return respuesta 409
     */
    @ExceptionHandler(CatalogoConflictoException.class)
    public ResponseEntity<ApiError> conflicto(CatalogoConflictoException e) {
        return error(HttpStatus.CONFLICT, "CODIGO_DUPLICADO", e.getMessage());
    }

    /**
     * Validacion de parametros o cuerpo invalido: 400 con {@code DATOS_INVALIDOS}.
     *
     * @param e excepcion de validacion (o fallo de lectura del JSON)
     * @return respuesta 400
     */
    @ExceptionHandler({IllegalArgumentException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ApiError> validacion(Exception e) {
        Throwable causa = e.getCause() instanceof IllegalArgumentException iae ? iae : e;
        return error(HttpStatus.BAD_REQUEST, "DATOS_INVALIDOS", causa.getMessage());
    }

    /**
     * Parametros de consulta con un tipo o un nombre imposibles (por ejemplo
     * {@code ?pagina=abc}, {@code ?tamano=abc} o un {@code tamano} obligatorio ausente):
     * 400 con {@code DATOS_INVALIDOS}. Sin este capturador el fallo de binding caia en el
     * generico y la API respondia 500 (D35).
     *
     * @param e fallo de binding del parametro de consulta
     * @return respuesta 400
     */
    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<ApiError> parametroMalTipado(Exception e) {
        String detalle = e instanceof MethodArgumentTypeMismatchException tipado
                ? "el parametro de consulta '" + tipado.getName() + "' no es del tipo esperado"
                : "falta un parametro de consulta obligatorio";
        return error(HttpStatus.BAD_REQUEST, "DATOS_INVALIDOS", detalle);
    }

    /**
     * Medio de peticion no soportado (por ejemplo un cuerpo {@code text/plain} en el alta o
     * la modificacion): 415 con {@code CONTENIDO_NO_ACEPTADO}. Sin este capturador el fallo
     * caia en el generico y la API respondia 500 con {@code ERROR_INTERNO} (D35).
     *
     * @param e excepcion de negociacion de contenido
     * @return respuesta 415
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiError> medioNoSoportado(HttpMediaTypeNotSupportedException e) {
        return error(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "CONTENIDO_NO_ACEPTADO",
                "el Content-Type de la peticion no es compatible con el contrato de la API");
    }

    /**
     * Cualquier fallo inesperado: 500 con {@code ERROR_INTERNO} (el detalle va al log).
     *
     * @param e excepcion no esperada
     * @return respuesta 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> inesperado(Exception e) {
        LOG.error("fallo inesperado en catalogo", e);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO", "fallo interno del catalogo");
    }

    private static ResponseEntity<ApiError> error(HttpStatus estado, String codigo, String mensaje) {
        return ResponseEntity.status(estado)
                .body(new ApiError(codigo, mensaje, Instant.now()));
    }
}
