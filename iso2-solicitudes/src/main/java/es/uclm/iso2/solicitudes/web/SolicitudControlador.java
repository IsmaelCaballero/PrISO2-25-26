package es.uclm.iso2.solicitudes.web;

import es.uclm.iso2.common.solicitudes.Prioridad;
import es.uclm.iso2.common.solicitudes.SolicitudFacade;
import es.uclm.iso2.common.solicitudes.SolicitudFacade.SolicitudDTO;
import es.uclm.iso2.common.solicitudes.SolicitudFacade.TrazabilidadSolicitudDTO;
import es.uclm.iso2.common.solicitudes.SolicitudFacade.SolicitudPrescripcionDTO;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API REST de solicitudes de prueba (CDU06). El autor de la prescripcion NO se lee del
 * cuerpo JSON: se toma del claim del token (RF06.2, no repudio), de modo que un medico
 * no puede fingir que otro prescribio. Los codigos de error estables los traduce
 * {@link SolicitudManejadorErrores}.
 *
 * @since 1.1.0 (CFG-04)
 */
@RestController
@RequestMapping("/api/solicitudes")
public class SolicitudControlador {

    private final SolicitudFacade solicitudes;

    /**
     * Inyecta la fachada del dominio.
     *
     * @param solicitudes implementacion de {@code SolicitudFacade}
     */
    public SolicitudControlador(SolicitudFacade solicitudes) {
        this.solicitudes = solicitudes;
    }

    /**
     * Cuerpo del alta de una prescripcion. La prioridad viaja como texto y se valida
     * aqui ({@link Prioridad#desdeNombre}); un nombre desconocido es un 400.
     *
     * @param pacienteId        historia clinica del paciente
     * @param codigoPrueba      codigo de prueba del catalogo
     * @param indicacionClinica motivo clinico de la peticion
     * @param prioridad         ORDINARIA, PREFERENTE o URGENTE
     * @param origen            origen de la solicitud (AP, hospital, urgencias...)
     */
    record PrescripcionRequest(String pacienteId, String codigoPrueba, String indicacionClinica,
                               String prioridad, String origen) {
    }

    /**
     * Prescribe una prueba a nombre del medico autenticado.
     *
     * @param request   datos de la prescripcion (sin medicoId)
     * @param principal usuario del token JWT (claim sub = medicoId)
     * @return 201 con la solicitud creada
     */
    @PostMapping
    public ResponseEntity<SolicitudDTO> prescribir(@RequestBody PrescripcionRequest request,
                                                   Principal principal) {
        Prioridad prioridad = Prioridad.desdeNombre(request.prioridad())
                .orElseThrow(() -> new IllegalArgumentException(
                        "la prioridad " + request.prioridad() + " no es valida (ORDINARIA, PREFERENTE, URGENTE)"));
        SolicitudDTO creada = solicitudes.prescribir(new SolicitudPrescripcionDTO(
                request.pacienteId(), request.codigoPrueba(), request.indicacionClinica(),
                prioridad, request.origen(), principal.getName()));
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    /**
     * Lista las solicitudes prescritas por el medico autenticado, de la mas reciente
     * a la mas antigua.
     *
     * @param principal usuario del token JWT
     * @return 200 con la lista de solicitudes
     */
    @GetMapping("/mias")
    public List<SolicitudDTO> mias(Principal principal) {
        return solicitudes.listarPorMedico(principal.getName());
    }

    /**
     * Detalle completo de una solicitud.
     *
     * @param id identificador funcional (UUID) de la solicitud
     * @return 200 con la solicitud, o 404 si no existe
     */
    @GetMapping("/{id}")
    public SolicitudDTO detalle(@PathVariable("id") String id) {
        return solicitudes.detalle(id);
    }

    /**
     * Trazabilidad de no repudio de una solicitud (RF06.2): autor, instante, huella
     * e integridad del recalculo.
     *
     * @param id identificador funcional (UUID) de la solicitud
     * @return 200 con la trazabilidad, o 404 si no existe
     */
    @GetMapping("/{id}/integridad")
    public TrazabilidadSolicitudDTO integridad(@PathVariable("id") String id) {
        return solicitudes.trazable(id);
    }
}
