package es.uclm.iso2.agendas.web;

import es.uclm.iso2.common.agendas.AgendaFacade;
import es.uclm.iso2.common.agendas.AgendaFacade.AgendaDTO;
import es.uclm.iso2.common.agendas.AgendaNoEncontradoException;
import es.uclm.iso2.common.agendas.AgendaFacade.FestivoDTO;
import es.uclm.iso2.common.agendas.AgendaFacade.FranjaDTO;
import es.uclm.iso2.common.agendas.AgendaFacade.FranjaGestionDTO;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * API REST de gestion de agendas de medicos y tecnicos (CDU13, RF13). Protegida por
 * RBAC en {@code iso2-server-app}: lectura para cualquier rol con {@code agenda:leer};
 * escritura solo para ADMINISTRADOR y RECEPCIONISTA (actor Administrativo del CDU).
 *
 * <p>Codigos de estado: 201 alta de agenda/franja/festivo; 200 lectura; 204 baja;
 * 400 franja o festivo invalidos; 404 agenda/franja/festivo inexistentes;
 * 409 conflicto de solape, festivo o agenda duplicada.</p>
 *
 * @since 1.1.0 (CFG-03)
 */
@RestController
@RequestMapping("/api/agendas")
public class AgendaControlador {

    private final AgendaFacade agendas;

    /**
     * Inyecta la fachada de agendas.
     *
     * @param agendas implementacion del servicio de agendas
     */
    public AgendaControlador(AgendaFacade agendas) {
        this.agendas = agendas;
    }

    /**
     * Lista todas las agendas con sus franjas y festivos.
     *
     * @return lista de agendas ordenadas por profesional
     */
    @GetMapping
    public List<AgendaDTO> listar() {
        return agendas.listarAgendas();
    }

    /**
     * Alta de una agenda vacia para un profesional.
     *
     * @param agenda cuerpo con el profesionalId del profesional
     * @return 201 con la agenda creada (409 si ya existia)
     */
    @PostMapping
    public ResponseEntity<AgendaDTO> crear(@RequestBody AgendaDTO agenda) {
        return ResponseEntity.status(HttpStatus.CREATED).body(agendas.crearAgenda(agenda.profesionalId()));
    }

    /**
     * Estado completo de la agenda de un profesional.
     *
     * @param profesionalId identificador del profesional
     * @return 200 con la agenda o 404 con {@code AGENDA_NO_ENCONTRADA} si no existe
     */
    @GetMapping("/{profesionalId}")
    public ResponseEntity<AgendaDTO> detalle(@PathVariable String profesionalId) {
        return ResponseEntity.ok(agendas.buscarPorProfesional(profesionalId)
                .orElseThrow(() -> new AgendaNoEncontradoException(
                        "no hay agenda para el profesional " + profesionalId)));
    }

    /**
     * Franjas libres de un profesional en un rango (contrato baseline de CFG-00,
     * consumidor futuro por CDU12 al citar).
     *
     * @param profesionalId identificador del profesional
     * @param desde         fecha inicial del rango (inclusive, ISO)
     * @param hasta         fecha final del rango (inclusive, ISO)
     * @return franjas disponibles ordenadas por fecha
     */
    @GetMapping("/{profesionalId}/libres")
    public List<FranjaDTO> libres(@PathVariable String profesionalId,
                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return agendas.franjasLibres(profesionalId, desde, hasta);
    }

    /**
     * Anade una franja de disponibilidad a la agenda de un profesional.
     *
     * @param profesionalId identificador del profesional
     * @param franja        dia, inicio y fin (sin id)
     * @return 201 con la franja creada (409 solape/festivo, 400 tramo invalido)
     */
    @PostMapping("/{profesionalId}/franjas")
    public ResponseEntity<FranjaGestionDTO> anadirFranja(@PathVariable String profesionalId,
                                                         @RequestBody FranjaGestionDTO franja) {
        return ResponseEntity.status(HttpStatus.CREATED).body(agendas.anadirFranja(profesionalId, franja));
    }

    /**
     * Elimina una franja de la agenda de un profesional.
     *
     * @param profesionalId identificador del profesional
     * @param franjaId      id de la franja
     * @return 204 sin cuerpo (404 si la agenda o la franja no existen)
     */
    @DeleteMapping("/{profesionalId}/franjas/{franjaId}")
    public ResponseEntity<Void> eliminarFranja(@PathVariable String profesionalId,
                                               @PathVariable Long franjaId) {
        agendas.eliminarFranja(profesionalId, franjaId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Anade un festivo a la agenda de un profesional.
     *
     * @param profesionalId identificador del profesional
     * @param festivo       dia y motivo (sin id)
     * @return 201 con el festivo creado (409 si choca con franjas o ya era festivo)
     */
    @PostMapping("/{profesionalId}/festivos")
    public ResponseEntity<FestivoDTO> anadirFestivo(@PathVariable String profesionalId,
                                                    @RequestBody FestivoDTO festivo) {
        return ResponseEntity.status(HttpStatus.CREATED).body(agendas.anadirFestivo(profesionalId, festivo));
    }

    /**
     * Elimina un festivo de la agenda de un profesional.
     *
     * @param profesionalId identificador del profesional
     * @param festivoId     id del festivo
     * @return 204 sin cuerpo (404 si la agenda o el festivo no existen)
     */
    @DeleteMapping("/{profesionalId}/festivos/{festivoId}")
    public ResponseEntity<Void> eliminarFestivo(@PathVariable String profesionalId,
                                                @PathVariable Long festivoId) {
        agendas.eliminarFestivo(profesionalId, festivoId);
        return ResponseEntity.noContent().build();
    }
}
