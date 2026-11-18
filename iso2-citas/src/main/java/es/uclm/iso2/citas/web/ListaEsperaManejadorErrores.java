package es.uclm.iso2.citas.web;

import es.uclm.iso2.common.catalogo.CatalogoNoEncontradoException;
import es.uclm.iso2.common.citas.CitaConflictoException;
import es.uclm.iso2.common.citas.CitaNoEncontradaException;
import es.uclm.iso2.common.citas.ListaEsperaConflictoException;
import es.uclm.iso2.common.citas.ListaEsperaNoEncontradaException;
import es.uclm.iso2.common.shared.ApiError;
import es.uclm.iso2.common.solicitudes.SolicitudNoEncontradaException;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Traduce los errores de la lista de espera a respuestas HTTP coherentes con el
 * contrato: 404 con el codigo de la referencia que falta (entrada, prueba del
 * catalogo o solicitud de origen), 409 con un codigo estable segun el conflicto
 * de la maquina de estados o el heredado del CDU12 al asignar, y 400 cuando
 * faltan datos o el {@code esperaId} no es un UUID legible.
 *
 * <p>El manejador de {@link DataIntegrityViolationException} no es un detalle
 * tecnico: es la concurrencia real del boton de asignar. Cuando dos administrativos
 * eligen el mismo hueco, la base de datos lo impide al violar el
 * {@code UNIQUE (profesional_id, inicio)} de {@code cita_hueco} y aqui se responde
 * el mismo 409 {@code CONFLICTO_HUECO_OCUPADO} que devuelve la comprobacion previa
 * del CDU12. El alta duplicada tambien choca contra el UNIQUE de
 * {@code clave_duplicada}, pero esa ya la traduce el servicio antes de llegar aqui.
 * La version optimista de la fila ({@code @Version} en {@code ListaEspera}) lanza
 * {@link ObjectOptimisticLockingFailureException} cuando dos hilos escriben sobre la
 * misma entrada: tambien se traduce a 409, nunca al 500 del catch-all.</p>
 *
 * <p>Como en {@link CitaManejadorErrores}, el advice esta acotado al controlador:
 * un metodo no declarado (POST sobre la ruta de auditoria, DELETE sobre la
 * coleccion) muere en el despacho de Spring y responde el 405 canonico sin cuerpo
 * {@link ApiError} propio (TC14-X METODO-NO-DECLARADO y RETIRAR-SIN-ID).</p>
 *
 * @since 1.2.0 (CFG-07, CDU14)
 */
@RestControllerAdvice(assignableTypes = ListaEsperaControlador.class)
public class ListaEsperaManejadorErrores {

    private static final Logger LOG = LoggerFactory.getLogger(ListaEsperaManejadorErrores.class);

    /**
     * Entrada de la cola inexistente: 404 con {@code LISTA_ESPERA_NO_ENCONTRADA}.
     *
     * @param e excepcion de contrato
     * @return respuesta 404
     */
    @ExceptionHandler(ListaEsperaNoEncontradaException.class)
    public ResponseEntity<ApiError> entradaNoEncontrada(ListaEsperaNoEncontradaException e) {
        return error(HttpStatus.NOT_FOUND, "LISTA_ESPERA_NO_ENCONTRADA", e.getMessage());
    }

    /**
     * Prueba fuera del catalogo en un alta: 404 con {@code PRUEBA_NO_ENCONTRADA}.
     * En los filtros de la cola la prueba inexistente es 400, no 404: el 404 se
     * reserva para la referencia que falta en el alta (tabla A de la spec).
     *
     * @param e excepcion del catalogo
     * @return respuesta 404
     */
    @ExceptionHandler(CatalogoNoEncontradoException.class)
    public ResponseEntity<ApiError> pruebaNoEncontrada(CatalogoNoEncontradoException e) {
        return error(HttpStatus.NOT_FOUND, "PRUEBA_NO_ENCONTRADA", e.getMessage());
    }

    /**
     * Solicitud de origen inexistente en un alta: 404 con
     * {@code SOLICITUD_NO_ENCONTRADA}.
     *
     * @param e excepcion del CDU06
     * @return respuesta 404
     */
    @ExceptionHandler(SolicitudNoEncontradaException.class)
    public ResponseEntity<ApiError> solicitudNoEncontrada(SolicitudNoEncontradaException e) {
        return error(HttpStatus.NOT_FOUND, "SOLICITUD_NO_ENCONTRADA", e.getMessage());
    }

    /**
     * Cita inexistente: 404 con {@code CITA_NO_ENCONTRADA}, codigo heredado del
     * CDU12 porque la cola termina tocando su contrato al asignar.
     *
     * @param e excepcion de contrato del CDU12
     * @return respuesta 404
     */
    @ExceptionHandler(CitaNoEncontradaException.class)
    public ResponseEntity<ApiError> citaNoEncontrada(CitaNoEncontradaException e) {
        return error(HttpStatus.NOT_FOUND, "CITA_NO_ENCONTRADA", e.getMessage());
    }

    /**
     * Conflicto de la maquina de la cola: 409 con codigo estable segun el tipo.
     *
     * @param e excepcion de contrato con el tipo de conflicto
     * @return respuesta 409
     */
    @ExceptionHandler(ListaEsperaConflictoException.class)
    public ResponseEntity<ApiError> conflictoDeCola(ListaEsperaConflictoException e) {
        String codigo = switch (e.codigo()) {
            case ALTA_DUPLICADA -> "CONFLICTO_ALTA_DUPLICADA";
            case REUBICACION_REDUNDANTE -> "CONFLICTO_REUBICACION_REDUNDANTE";
            case ENTRADA_NO_ACTIVA -> "CONFLICTO_ENTRADA_NO_ACTIVA";
        };
        return error(HttpStatus.CONFLICT, codigo, e.getMessage());
    }

    /**
     * Conflicto del motor de citas al asignar desde la cola: 409 con los codigos
     * heredados del CDU12, que viajan tal cual porque el contrato de
     * {@code CitaFacade} no se toca (baseline CDU12 congelado).
     *
     * @param e excepcion de contrato del CDU12 con el tipo de conflicto
     * @return respuesta 409
     */
    @ExceptionHandler(CitaConflictoException.class)
    public ResponseEntity<ApiError> conflictoDeCita(CitaConflictoException e) {
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
     * Dos entradas de la cola peleandose por el mismo hueco: la base de datos lo
     * impide al violar el UNIQUE de {@code cita_hueco} y aqui se traduce al mismo
     * 409 que devuelve la comprobacion previa del CDU12. El hueco sigue bloqueado
     * para la primera cita, que es la garantia de no duplicar agenda (D140).
     *
     * @param e fallo de integridad de la reserva del hueco
     * @return respuesta 409 con {@code CONFLICTO_HUECO_OCUPADO}
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> huecoEnDisputa(DataIntegrityViolationException e) {
        LOG.warn("hueco en disputa al asignar desde la cola: {}", e.getMessage());
        return error(HttpStatus.CONFLICT, "CONFLICTO_HUECO_OCUPADO",
                "el hueco pedido ya no esta libre u otra operacion lo esta ocupando a la vez");
    }

    /**
     * Dos pulsaciones escribiendo sobre la misma entrada de la cola (TC14-E10):
     * Hibernate detecta que la columna {@code @Version} de {@code lista_espera}
     * cambio mientras el segundo hilo tenia la copia abierta y descarta su UPDATE.
     * Sin este manejo acabaria en el 500 del catch-all disfrazando un conflicto
     * normal de doble clic; aqui se responde el mismo 409
     * {@code CONFLICTO_ENTRADA_NO_ACTIVA} que recibe el hilo que llega tarde por la
     * maquina de estados, que es la misma situacion vista desde la base de datos.
     * Va despues del de {@link DataIntegrityViolationException} y no lo pisa: ese
     * es la pelea por un hueco de CDU12, este es la pelea por la misma fila.
     *
     * @param e fallo de control optimista sobre la fila de la cola
     * @return respuesta 409 con {@code CONFLICTO_ENTRADA_NO_ACTIVA}
     */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiError> entradaEnDisputa(ObjectOptimisticLockingFailureException e) {
        LOG.warn("entrada de la cola en disputa por control optimista: {}", e.getMessage());
        return error(HttpStatus.CONFLICT, "CONFLICTO_ENTRADA_NO_ACTIVA",
                "otra operacion ha modificado esta entrada de la cola a la vez");
    }

    /**
     * El path variable {@code esperaId} no es un UUID legible: 400 con
     * {@code DATOS_INVALIDOS}, nunca un 404 disfrazado (HALLAZGO 3 de CDU19
     * corregido en CDU12 y heredado aqui con el tipado UUID del path variable).
     *
     * @param e fallo de conversion del conversor de Spring
     * @return respuesta 400
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> idIlegible(MethodArgumentTypeMismatchException e) {
        return error(HttpStatus.BAD_REQUEST, "DATOS_INVALIDOS",
                "el esperaId no es un identificador legible: " + e.getValue());
    }

    /**
     * Datos invalidos (faltan obligatorios, prioridad fuera de escala, motivo
     * ausente, estado de filtro imposible...): 400 con {@code DATOS_INVALIDOS}.
     *
     * @param e excepcion de validacion
     * @return respuesta 400
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> invalidos(IllegalArgumentException e) {
        return error(HttpStatus.BAD_REQUEST, "DATOS_INVALIDOS", e.getMessage());
    }

    /**
     * Cuerpo JSON mal formado o ausente: 400 con {@code DATOS_INVALIDOS}. Es la
     * puerta de los intentos de colar los obligatorios por query: el controlador
     * solo lee el cuerpo y aqui acaba el aviso vacio (TC14-X
     * QUERY-CON-PARAMS-EN-ALTA y ASIGNAR-CON-PARAMS-EN-QUERY).
     *
     * @param e error de lectura del mensaje HTTP
     * @return respuesta 400
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> cuerpoIlegible(HttpMessageNotReadableException e) {
        return error(HttpStatus.BAD_REQUEST, "DATOS_INVALIDOS", "el cuerpo JSON no es legible");
    }

    /**
     * Contenido con un medio que el contrato no admite (texto plano donde va JSON): 415
     * {@code CONTENIDO} con respuesta vacia. El contrato no define cuerpo para el 415, y aqui el
     * consejo lo sirve igual que lo servia Spring por defecto: sin {@link ApiError}. Hace falta un
     * manejador especifico porque el catch-all de {@link Exception} de esta clase lo tragaba y lo
     * disfrazaba de 500 {@code ERROR_INTERNO}. El 405 de un metodo no declarado sigue sirriendose
     * por Spring sin mas: cuando el metodo no existe, este advice acotado al controlador ni
     * interviene (TC14-H02 y TC14-H10).
     *
     * @param e medio no soportado declarado en la peticion
     * @return respuesta 415 sin cuerpo
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Void> medioNoSoportado(HttpMediaTypeNotSupportedException e) {
        LOG.debug("medio no soportado en la lista de espera: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).build();
    }

    /**
     * Cualquier fallo inesperado: 500 con {@code ERROR_INTERNO} (el detalle va al log).
     *
     * @param e excepcion no esperada
     * @return respuesta 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> inesperado(Exception e) {
        LOG.error("fallo inesperado en la lista de espera", e);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO",
                "fallo interno de la lista de espera");
    }

    private static ResponseEntity<ApiError> error(HttpStatus estado, String codigo, String mensaje) {
        return ResponseEntity.status(estado)
                .body(new ApiError(codigo, mensaje, Instant.now()));
    }
}
