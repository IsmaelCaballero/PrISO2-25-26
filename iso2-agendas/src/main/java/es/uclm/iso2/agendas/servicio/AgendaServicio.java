package es.uclm.iso2.agendas.servicio;

import es.uclm.iso2.agendas.dominio.Agenda;
import es.uclm.iso2.agendas.dominio.AuditoriaAgendas;
import es.uclm.iso2.agendas.dominio.Festivo;
import es.uclm.iso2.agendas.dominio.Franja;
import es.uclm.iso2.agendas.repositorio.AgendaRepositorio;
import es.uclm.iso2.agendas.repositorio.AuditoriaAgendasRepositorio;
import es.uclm.iso2.agendas.repositorio.FestivoRepositorio;
import es.uclm.iso2.agendas.repositorio.FranjaRepositorio;
import es.uclm.iso2.common.agendas.AgendaConflictoException;
import es.uclm.iso2.common.agendas.AgendaFacade;
import es.uclm.iso2.common.agendas.AgendaNoEncontradoException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementacion de las agendas de medicos y tecnicos (CDU13, RF13): alta de agenda,
 * gestion de franjas y festivos con auditoria encadenada por SHA-256.
 *
 * <p>Reglas autoritativas del CDU aplicadas aqui:</p>
 * <ul>
 *   <li><strong>no solape</strong>: una franja nueva no puede pisar a otra del mismo
 *       profesional el mismo dia (bordes que se tocan no cuentan);</li>
 *   <li><strong>festivo manda</strong>: no hay franja en dia festivo y no se declara
 *       festivo un dia con franjas (409 {@code FESTIVO} en ambos casos);</li>
 *   <li>un profesional tiene una unica agenda (409 {@code AGENDA_DUPLICADA}).</li>
 * </ul>
 *
 * <p>El usuario de auditoria sale del contexto de seguridad (JWT desde CDU17) y se
 * registra como {@code sistema} si no hay autenticacion.</p>
 *
 * @since 1.1.0 (CFG-03)
 */
@Service
@Transactional(readOnly = true)
public class AgendaServicio implements AgendaFacade {

    private static final Comparator<Franja> FRANJAS_ORDEN =
            Comparator.comparing(Franja::getDia).thenComparing(Franja::getInicio);
    private static final Comparator<Festivo> FESTIVOS_ORDEN = Comparator.comparing(Festivo::getDia);

    private final AgendaRepositorio agendas;
    private final FranjaRepositorio franjas;
    private final FestivoRepositorio festivos;
    private final AuditoriaAgendasRepositorio auditoria;

    /**
     * Inyecta los repositorios JPA del modulo.
     *
     * @param agendas   acceso a las agendas de profesional
     * @param franjas   acceso a las franjas (alta directa: el id debe volver al DTO)
     * @param festivos  acceso a los festivos (alta directa por el mismo motivo)
     * @param auditoria acceso a la cadena de auditoria
     */
    public AgendaServicio(AgendaRepositorio agendas, FranjaRepositorio franjas,
                          FestivoRepositorio festivos, AuditoriaAgendasRepositorio auditoria) {
        this.agendas = agendas;
        this.franjas = franjas;
        this.festivos = festivos;
        this.auditoria = auditoria;
    }

    /** {@inheritDoc} */
    @Override
    public List<FranjaDTO> franjasLibres(String profesionalId, LocalDate desde, LocalDate hasta) {
        Optional<Agenda> agenda = agendas.findByProfesionalId(profesionalId);
        if (agenda.isEmpty() || desde == null || hasta == null) {
            return List.of();
        }
        return agenda.get().getFranjas().stream()
                .filter(f -> !f.getDia().isBefore(desde) && !f.getDia().isAfter(hasta))
                .filter(f -> !agenda.get().esFestivo(f.getDia()))
                .sorted(FRANJAS_ORDEN)
                .map(f -> new FranjaDTO(profesionalId, f.getDia(), f.getInicio(), f.getFin()))
                .toList();
    }

    /** {@inheritDoc} */
    @Override
    public List<AgendaDTO> listarAgendas() {
        return agendas.findAll().stream()
                .sorted(Comparator.comparing(Agenda::getProfesionalId))
                .map(AgendaServicio::aDTO)
                .toList();
    }

    /** {@inheritDoc} */
    @Override
    public Optional<AgendaDTO> buscarPorProfesional(String profesionalId) {
        return agendas.findByProfesionalId(profesionalId).map(AgendaServicio::aDTO);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public AgendaDTO crearAgenda(String profesionalId) {
        if (profesionalId == null || profesionalId.isBlank()) {
            throw new IllegalArgumentException("la agenda necesita un profesional");
        }
        if (agendas.existsByProfesionalId(profesionalId.trim())) {
            throw new AgendaConflictoException(AgendaConflictoException.Conflicto.AGENDA_DUPLICADA,
                    "el profesional " + profesionalId + " ya tiene agenda");
        }
        Agenda agenda = agendas.save(new Agenda(profesionalId));
        registrar(AuditoriaAgendas.AccionAuditoria.ALTA_AGENDA, agenda.getProfesionalId());
        return aDTO(agenda);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public FranjaGestionDTO anadirFranja(String profesionalId, FranjaGestionDTO franja) {
        Agenda agenda = agendaO404(profesionalId);
        if (franja == null || franja.dia() == null || franja.inicio() == null || franja.fin() == null) {
            throw new IllegalArgumentException("la franja necesita dia, inicio y fin");
        }
        if (agenda.esFestivo(franja.dia())) {
            throw new AgendaConflictoException(AgendaConflictoException.Conflicto.FESTIVO,
                    "el " + franja.dia() + " es festivo para " + profesionalId);
        }
        if (agenda.haySolape(franja.dia(), franja.inicio(), franja.fin())) {
            throw new AgendaConflictoException(AgendaConflictoException.Conflicto.SOLAPE,
                    "la franja " + franja.inicio() + "-" + franja.fin() + " del "
                            + franja.dia() + " solapa con otra de " + profesionalId);
        }
        Franja creada = agenda.anadirFranja(franja.dia(), franja.inicio(), franja.fin());
        // Alta directa: el persist en cascada desde la coleccion no reasigna el id
        // @IDENTITY en la instancia en memoria y el DTO viajaria con id nulo.
        franjas.save(creada);
        registrar(AuditoriaAgendas.AccionAuditoria.ALTA_FRANJA, agenda.getProfesionalId());
        return new FranjaGestionDTO(creada.getId(), creada.getDia(), creada.getInicio(), creada.getFin());
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void eliminarFranja(String profesionalId, Long franjaId) {
        Agenda agenda = agendaO404(profesionalId);
        Franja franja = agenda.buscarFranja(franjaId)
                .orElseThrow(() -> new AgendaNoEncontradoException(
                        "la agenda de " + profesionalId + " no tiene la franja " + franjaId));
        agenda.getFranjas().remove(franja);
        agendas.save(agenda);
        registrar(AuditoriaAgendas.AccionAuditoria.BAJA_FRANJA, agenda.getProfesionalId());
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public FestivoDTO anadirFestivo(String profesionalId, FestivoDTO festivo) {
        Agenda agenda = agendaO404(profesionalId);
        if (festivo == null || festivo.dia() == null) {
            throw new IllegalArgumentException("el festivo necesita un dia");
        }
        if (agenda.esFestivo(festivo.dia())) {
            throw new AgendaConflictoException(AgendaConflictoException.Conflicto.FESTIVO,
                    "el " + festivo.dia() + " ya es festivo para " + profesionalId);
        }
        if (agenda.hayFranjaElDia(festivo.dia())) {
            throw new AgendaConflictoException(AgendaConflictoException.Conflicto.FESTIVO,
                    "el " + festivo.dia() + " tiene franjas asignadas: quitalas antes "
                            + "de declarar festivo en la agenda de " + profesionalId);
        }
        Festivo creado = agenda.anadirFestivo(festivo.dia(), festivo.motivo());
        festivos.save(creado);
        registrar(AuditoriaAgendas.AccionAuditoria.ALTA_FESTIVO, agenda.getProfesionalId());
        return new FestivoDTO(creado.getId(), creado.getDia(), creado.getMotivo());
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void eliminarFestivo(String profesionalId, Long festivoId) {
        Agenda agenda = agendaO404(profesionalId);
        Festivo festivo = agenda.buscarFestivo(festivoId)
                .orElseThrow(() -> new AgendaNoEncontradoException(
                        "la agenda de " + profesionalId + " no tiene el festivo " + festivoId));
        agenda.getFestivos().remove(festivo);
        agendas.save(agenda);
        registrar(AuditoriaAgendas.AccionAuditoria.BAJA_FESTIVO, agenda.getProfesionalId());
    }

    /**
     * Comprueba que la cadena de auditoria del modulo sigue siendo verifiable: cada
     * registro enlaza con el hash recalculado del anterior. Usada por los tests de
     * CFG-03 (espejo de la auditoria de catalogo de CFG-01).
     *
     * @return true si ningun registro fue alterado tras su escritura
     */
    public boolean auditoriaVerificable() {
        String anterior = null;
        for (AuditoriaAgendas registro : auditoria.findAll()) {
            String esperado = hashDe(registro.getAccion(), registro.getProfesionalId(),
                    registro.getUsuario(), registro.getInstante(), anterior);
            if (!esperado.equals(registro.getHash())
                    || !java.util.Objects.equals(anterior, registro.getHashAnterior())) {
                return false;
            }
            anterior = registro.getHash();
        }
        return true;
    }

    private Agenda agendaO404(String profesionalId) {
        return agendas.findByProfesionalId(profesionalId)
                .orElseThrow(() -> new AgendaNoEncontradoException(
                        "no hay agenda para el profesional " + profesionalId));
    }

    private void registrar(AuditoriaAgendas.AccionAuditoria accion, String profesionalId) {
        String hashAnterior = auditoria.findTopByOrderByIdDesc()
                .map(AuditoriaAgendas::getHash)
                .orElse(null);
        Instant ahora = Instant.now();
        String usuario = usuarioEnSeguridad();
        String hash = hashDe(accion, profesionalId, usuario, ahora, hashAnterior);
        auditoria.save(new AuditoriaAgendas(accion, profesionalId, usuario, ahora, hash, hashAnterior));
    }

    private static String usuarioEnSeguridad() {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        return autenticacion == null ? "sistema" : autenticacion.getName();
    }

    /**
     * Calcula el hash SHA-256 (hex) de un registro de auditoria de agendas.
     *
     * @param accion        accion
     * @param profesionalId profesional afectado
     * @param usuario       usuario responsable
     * @param instante      momento UTC
     * @param hashAnterior  hash del registro anterior (null en el primero)
     * @return hash hex de 64 digitos
     */
    static String hashDe(AuditoriaAgendas.AccionAuditoria accion, String profesionalId,
                         String usuario, Instant instante, String hashAnterior) {
        String material = accion.name() + "|" + profesionalId + "|" + usuario + "|" + instante
                + "|" + (hashAnterior == null ? "GENESIS" : hashAnterior);
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(sha.digest(material.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible en esta JVM", e);
        }
    }

    private static AgendaDTO aDTO(Agenda agenda) {
        List<FranjaGestionDTO> franjas = agenda.getFranjas().stream()
                .sorted(FRANJAS_ORDEN)
                .map(f -> new FranjaGestionDTO(f.getId(), f.getDia(), f.getInicio(), f.getFin()))
                .toList();
        List<FestivoDTO> festivos = agenda.getFestivos().stream()
                .sorted(FESTIVOS_ORDEN)
                .map(f -> new FestivoDTO(f.getId(), f.getDia(), f.getMotivo()))
                .toList();
        return new AgendaDTO(agenda.getProfesionalId(), franjas, festivos);
    }
}
