package es.uclm.iso2.citas.web;

import es.uclm.iso2.common.citas.CitaFacade;
import es.uclm.iso2.common.citas.CitaFacade.CitaDTO;
import es.uclm.iso2.common.citas.CitaFacade.CitaNuevaDTO;
import es.uclm.iso2.common.citas.CitaFacade.DetalleCitaDTO;
import es.uclm.iso2.common.citas.CitaFacade.HuecoDTO;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * API REST de citas del CDU12 sobre la que trabaja la pantalla de gestion de citas de
 * recepcionista (UC12.2 a UC12.8). El contrato de errores lo traduce
 * {@link CitaManejadorErrores}: 409 cuando el hueco ya esta ocupado, la franja no vale o
 * la transicion de estado no es valida, 404 cuando la cita no existe y 400 cuando faltan
 * datos obligatorios.
 *
 * <p>No hay endpoints de borrado: una cita no se borra, se cancela con motivo, que es lo
 * que mantiene el rastro de auditoria y la trazabilidad del subdominio (UC12.5).</p>
 *
 * @since 1.1.0 (CFG-05, CDU12)
 */
@RestController
@RequestMapping("/api/citas")
public class CitaControlador {

    private final CitaFacade citas;

    /**
     * Inyecta la fachada del dominio.
     *
     * @param citas implementacion de {@code CitaFacade}
     */
    public CitaControlador(CitaFacade citas) {
        this.citas = citas;
    }

    /**
     * Pide reprogramar la cita a un inicio nuevo.
     *
     * @param inicio nuevo inicio de la cita
     */
    record ReprogramacionRequest(LocalDateTime inicio) {
    }

    /**
     * Pide cancelar una cita; el motivo es obligatorio (UC12.5).
     *
     * @param motivo motivo de la cancelacion
     */
    record CancelacionRequest(String motivo) {
    }

    /**
     * Momento de corte del lote de expiracion.
     *
     * @param corte instante de referencia; si viene nulo se usa el reloj del servidor
     */
    record ExpiracionRequest(LocalDateTime corte) {
    }

    /**
     * Agenda una cita sobre una franja libre de la agenda de un profesional. El hueco se
     * bloquea en la misma transaccion: si otro usuario lo reservo antes, la operacion
     * termina en 409 {@code CONFLICTO_HUECO_OCUPADO} (RF12, CFG-05).
     *
     * @param nueva solicitud opcional, profesional, inicio y duracion
     * @return 201 con la cita creada en estado programada
     */
    @PostMapping
    public ResponseEntity<CitaDTO> agendar(@RequestBody CitaNuevaDTO nueva) {
        return ResponseEntity.status(HttpStatus.CREATED).body(citas.agendar(nueva));
    }

    /**
     * Lista las citas con los filtros de la pantalla de recepcion (UC12.8).
     *
     * @param desde         inicio inclusivo del rango, opcional
     * @param hasta         fin inclusivo del rango, opcional
     * @param profesionalId profesional a filtrar, opcional
     * @param estado        estado en minusculas, opcional
     * @return 200 con las citas que cumplen los filtros
     */
    @GetMapping
    public List<DetalleCitaDTO> listar(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @RequestParam(required = false) String profesionalId,
            @RequestParam(required = false) String estado) {
        return citas.listar(desde, hasta, profesionalId, estado);
    }

    /**
     * Motor de asignacion de huecos: huecos libres de un profesional descontando los que
     * ya estan bloqueados (UC12.2). Los festivos del CDU13 nunca aparecen.
     *
     * @param profesionalId   profesional cuya agenda se consulta, obligatorio
     * @param desde           primer dia del rango, obligatorio
     * @param hasta           ultimo dia del rango, obligatorio
     * @param duracionMinutos duracion que debe caber en el hueco, opcional
     * @return 200 con los huecos libres ordenados por inicio
     */
    @GetMapping("/huecos")
    public List<HuecoDTO> huecosLibres(@RequestParam String profesionalId,
                                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                                       @RequestParam(required = false) Integer duracionMinutos) {
        return citas.huecosLibres(profesionalId, desde, hasta, duracionMinutos);
    }

    /**
     * Detalle completo de una cita.
     *
     * @param id identificador funcional (UUID) de la cita
     * @return 200 con el detalle, o 404 si no existe
     */
    @GetMapping("/{id}")
    public DetalleCitaDTO detalle(@PathVariable("id") String id) {
        return citas.detalle(id);
    }

    /**
     * Confirma la asistencia de una cita programada (UC12.3).
     *
     * @param id identificador funcional de la cita
     * @return 200 con la cita confirmada, 404 si no existe o 409 si la transicion no vale
     */
    @PostMapping("/{id}/confirmar")
    public CitaDTO confirmar(@PathVariable("id") String id) {
        return citas.confirmar(id);
    }

    /**
     * Registra la prueba como realizada y libera el hueco (UC12.6).
     *
     * @param id identificador funcional de la cita
     * @return 200 con la cita realizada, 404 si no existe o 409 si la transicion no vale
     */
    @PostMapping("/{id}/realizar")
    public CitaDTO realizar(@PathVariable("id") String id) {
        return citas.realizar(id);
    }

    /**
     * Reprograma la cita a un hueco nuevo: valida de nuevo franja y festivo, libera el
     * hueco antiguo y bloquea el de destino (UC12.4). Si el destino ya esta ocupado, la
     * cita conserva su hueco anterior y la respuesta es 409.
     *
     * @param id      identificador funcional de la cita
     * @param request nuevo inicio
     * @return 200 con la cita en su nueva hora, 404 si no existe o 409 si el hueco no vale
     */
    @PostMapping("/{id}/reprogramar")
    public CitaDTO reprogramar(@PathVariable("id") String id, @RequestBody ReprogramacionRequest request) {
        return citas.reprogramar(id, request.inicio());
    }

    /**
     * Cancela la cita con motivo y libera su hueco (UC12.5).
     *
     * @param id      identificador funcional de la cita
     * @param request motivo de la cancelacion
     * @return 200 con la cita cancelada, 404 si no existe o 409 si la transicion no vale
     *         o si no se respeta el plazo de cancelacion del catalogo
     */
    @PostMapping("/{id}/cancelar")
    public CitaDTO cancelar(@PathVariable("id") String id, @RequestBody CancelacionRequest request) {
        return citas.cancelar(id, request.motivo());
    }

    /**
     * Lote de mantenimiento que vence las citas a las que no se atendio: las pasa a
     * expirada y libera sus huecos (UC12.7). Lo lanza recepcion o el sistema.
     *
     * @param request corte de referencia, opcional
     * @return 200 con las citas que pasaron a expirada
     */
    @PostMapping("/expirar-vencidas")
    public List<CitaDTO> expirarVencidas(@RequestBody(required = false) ExpiracionRequest request) {
        return citas.expirarVencidas(request == null ? null : request.corte());
    }
}
