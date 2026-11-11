package es.uclm.iso2.solicitudes.servicio;

import es.uclm.iso2.common.catalogo.PruebaCatalogoFacade;
import es.uclm.iso2.common.solicitudes.Prioridad;
import es.uclm.iso2.common.solicitudes.SolicitudConflictoException;
import es.uclm.iso2.common.solicitudes.SolicitudFacade;
import es.uclm.iso2.common.solicitudes.SolicitudNoEncontradaException;
import es.uclm.iso2.solicitudes.dominio.Solicitud;
import es.uclm.iso2.solicitudes.repositorio.SolicitudRepositorio;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementacion de las solicitudes de prueba desde Atencion Primaria (CDU06, RF06).
 *
 * <p>Reglas autoritativas del CDU aplicadas aqui (ver {@code docs/modelo/iter04}):</p>
 * <ul>
 *   <li><strong>campos obligatorios</strong> (RF06.1): los valida el contrato
 *       {@link SolicitudPrescripcionDTO} antes de llegar al servicio;</li>
 *   <li><strong>prueba disponible</strong>: el codigo debe existir y estar activo en
 *       el catalogo de CDU16 (409 {@code PRUEBA_NO_DISPONIBLE});</li>
 *   <li><strong>anti-duplicado</strong> (RF06.3): el mismo medico no puede tener otra
 *       solicitud activa (no cancelada) del mismo paciente y prueba (409
 *       {@code DUPLICADA});</li>
 *   <li><strong>no repudio</strong> (RF06.2): el autor lo fija el controlador desde el
 *       token y la huella SHA-256 la sella el dominio en la creacion.</li>
 * </ul>
 *
 * <p>El baseline de CFG-00 ({@link #crear} y {@link #buscar}) se conserva: {@code crear}
 * delega en {@link #prescribir} con defaults (autor {@code SISTEMA}, prioridad
 * {@code ORDINARIA}) para que el alta minima tambien quede sellada.</p>
 *
 * <p>El reloj es inyectable para poder fijar el instante de creacion en los tests,
 * igual que en la auditoria de CDU13.</p>
 *
 * @since 1.1.0 (CFG-04)
 */
@Service
@Transactional(readOnly = true)
public class SolicitudServicio implements SolicitudFacade {

    /** Estados que siguen en curso y por tanto bloquean una prescripcion duplicada. */
    private static final EnumSet<Solicitud.Estado> ESTADOS_ACTIVOS =
            EnumSet.of(Solicitud.Estado.CREADA, Solicitud.Estado.CITADA,
                    Solicitud.Estado.REALIZADA, Solicitud.Estado.INFORMADA);

    private final SolicitudRepositorio solicitudes;
    private final PruebaCatalogoFacade catalogo;
    private final Clock clock;

    /**
     * Inyecta el repositorio del modulo y la fachada del catalogo (CDU16) con el
     * reloj del sistema.
     *
     * @param solicitudes acceso JPA a las solicitudes
     * @param catalogo    catalogo de pruebas para validar el codigo (CDU16)
     */
    @Autowired
    public SolicitudServicio(SolicitudRepositorio solicitudes, PruebaCatalogoFacade catalogo) {
        this(solicitudes, catalogo, Clock.systemUTC());
    }

    /**
     * Variante con reloj explicito para pruebas (instante determinista en la huella).
     *
     * @param solicitudes acceso JPA a las solicitudes
     * @param catalogo    catalogo de pruebas para validar el codigo (CDU16)
     * @param clock       reloj de creacion
     */
    public SolicitudServicio(SolicitudRepositorio solicitudes, PruebaCatalogoFacade catalogo, Clock clock) {
        this.solicitudes = solicitudes;
        this.catalogo = catalogo;
        this.clock = clock;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public SolicitudDTO crear(SolicitudNuevaDTO nueva) {
        if (nueva == null || nueva.pacienteId() == null || nueva.pacienteId().isBlank()
                || nueva.codigoPrueba() == null || nueva.codigoPrueba().isBlank()) {
            throw new IllegalArgumentException("la solicitud necesita paciente y prueba");
        }
        String origen = nueva.origen() == null || nueva.origen().isBlank() ? "AP" : nueva.origen();
        // El alta baseline tambien queda sellada: se delega en prescribir con defaults.
        return prescribir(new SolicitudPrescripcionDTO(nueva.pacienteId(), nueva.codigoPrueba(),
                "Alta baseline sin indicacion clinica", Prioridad.ORDINARIA, origen, "SISTEMA"));
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public SolicitudDTO prescribir(SolicitudPrescripcionDTO prescripcion) {
        PruebaCatalogoFacade.PruebaImagenDTO prueba = catalogo.buscarPorCodigo(prescripcion.codigoPrueba())
                .orElseThrow(() -> new SolicitudConflictoException(
                        SolicitudConflictoException.Conflicto.PRUEBA_NO_DISPONIBLE,
                        "la prueba " + prescripcion.codigoPrueba() + " no existe en el catalogo"));
        if (!prueba.activa()) {
            throw new SolicitudConflictoException(
                    SolicitudConflictoException.Conflicto.PRUEBA_NO_DISPONIBLE,
                    "la prueba " + prescripcion.codigoPrueba() + " esta desactivada en el catalogo");
        }
        if (solicitudes.existsByMedicoIdAndPacienteIdAndCodigoPruebaAndEstadoIn(
                prescripcion.medicoId(), prescripcion.pacienteId(), prescripcion.codigoPrueba(), ESTADOS_ACTIVOS)) {
            throw new SolicitudConflictoException(
                    SolicitudConflictoException.Conflicto.DUPLICADA,
                    "el medico ya tiene una solicitud activa de " + prescripcion.codigoPrueba()
                            + " para el paciente " + prescripcion.pacienteId());
        }
        Solicitud creada = solicitudes.save(new Solicitud(
                prescripcion.pacienteId(), prescripcion.codigoPrueba(), prescripcion.indicacionClinica(),
                prescripcion.prioridad(), prescripcion.origen(), prescripcion.medicoId(), clock.instant()));
        return aDTO(creada);
    }

    /** {@inheritDoc} */
    @Override
    public List<SolicitudDTO> listarPorMedico(String medicoId) {
        if (medicoId == null || medicoId.isBlank()) {
            throw new IllegalArgumentException("se necesita el identificador del medico");
        }
        return solicitudes.findByMedicoIdOrderByCreadaEnDesc(medicoId).stream()
                .map(SolicitudServicio::aDTO)
                .toList();
    }

    /** {@inheritDoc} */
    @Override
    public SolicitudDTO detalle(String solicitudId) {
        return aDTO(buscarEntidad(solicitudId));
    }

    /** {@inheritDoc} */
    @Override
    public TrazabilidadSolicitudDTO trazable(String solicitudId) {
        Solicitud s = buscarEntidad(solicitudId);
        return new TrazabilidadSolicitudDTO(s.getSolicitudId(), s.getMedicoId(), s.getCreadaEn(),
                s.getHuella(), s.trazaIntegra());
    }

    /** {@inheritDoc} */
    @Override
    public Optional<SolicitudDTO> buscar(String solicitudId) {
        return solicitudes.findBySolicitudId(solicitudId).map(SolicitudServicio::aDTO);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public SolicitudDTO marcarCitada(String solicitudId) {
        Solicitud s = buscarEntidad(solicitudId);
        if (s.getEstado() != Solicitud.Estado.CREADA) {
            throw new SolicitudConflictoException(
                    SolicitudConflictoException.Conflicto.NO_APTA_PARA_CITA,
                    "la solicitud " + solicitudId + " no admite cita: esta en estado "
                            + s.getEstado().name().toLowerCase(Locale.ROOT));
        }
        s.marcarComoCitada();
        return aDTO(s);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public SolicitudDTO devolverACreada(String solicitudId) {
        Solicitud s = buscarEntidad(solicitudId);
        if (s.getEstado() != Solicitud.Estado.CITADA) {
            throw new SolicitudConflictoException(
                    SolicitudConflictoException.Conflicto.NO_APTA_PARA_CITA,
                    "la solicitud " + solicitudId + " no se puede reabrir: esta en estado "
                            + s.getEstado().name().toLowerCase(Locale.ROOT));
        }
        s.volverACreada();
        return aDTO(s);
    }

    private Solicitud buscarEntidad(String solicitudId) {
        return solicitudes.findBySolicitudId(solicitudId)
                .orElseThrow(() -> new SolicitudNoEncontradaException("la solicitud " + solicitudId + " no existe"));
    }

    private static SolicitudDTO aDTO(Solicitud s) {
        return new SolicitudDTO(s.getSolicitudId(), s.getPacienteId(), s.getCodigoPrueba(),
                s.getCreadaEn().atZone(ZoneId.systemDefault()).toLocalDate(),
                s.getEstado().name().toLowerCase(Locale.ROOT));
    }
}
