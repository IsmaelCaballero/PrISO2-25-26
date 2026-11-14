package es.uclm.iso2.citas.servicio;

import es.uclm.iso2.citas.dominio.AuditoriaCitas;
import es.uclm.iso2.citas.dominio.AuditoriaCitas.AccionAuditoria;
import es.uclm.iso2.citas.dominio.Cita;
import es.uclm.iso2.citas.dominio.CitaHueco;
import es.uclm.iso2.citas.repositorio.AuditoriaCitasRepositorio;
import es.uclm.iso2.citas.repositorio.CitaHuecoRepositorio;
import es.uclm.iso2.citas.repositorio.CitaRepositorio;
import es.uclm.iso2.common.agendas.AgendaFacade;
import es.uclm.iso2.common.agendas.AgendaFacade.FranjaDTO;
import es.uclm.iso2.common.agendas.AgendaNoEncontradoException;
import es.uclm.iso2.common.catalogo.PruebaCatalogoFacade;
import es.uclm.iso2.common.catalogo.PruebaCatalogoFacade.PruebaImagenDTO;
import es.uclm.iso2.common.citas.CitaConflictoException;
import es.uclm.iso2.common.citas.CitaConflictoException.Conflicto;
import es.uclm.iso2.common.citas.CitaFacade;
import es.uclm.iso2.common.citas.CitaNoEncontradaException;
import es.uclm.iso2.common.solicitudes.SolicitudConflictoException;
import es.uclm.iso2.common.solicitudes.SolicitudFacade;
import es.uclm.iso2.common.solicitudes.SolicitudFacade.SolicitudDTO;
import es.uclm.iso2.common.solicitudes.SolicitudNoEncontradaException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Motor de citas del CDU12 sobre las agendas del CDU13 y el catalogo del CDU16.
 *
 * <p>La regla que manda es que <strong>el hueco lo bloquea la base de datos</strong>. El
 * servicio comprueba antes de escribir dos cosas: que la cita cabe entera en una franja de
 * disponibilidad del profesional (los festivos ya quedan fuera de {@link
 * AgendaFacade#franjasLibres}, de modo que no caber equivale a festivo o a hueco fuera de
 * jornada) y que ningun hueco activo solapa el intervalo pedido. Pero dos recepcionistas
 * pueden comprobar a la vez y ver el hueco libre: la unica decision que vale es la del
 * {@code UNIQUE (profesional_id, inicio)} de {@link CitaHueco}, que aqui se fuerza con
 * {@code saveAndFlush}. El que llega tarde ve como la transaccion entera se echa abajo y
 * recibe un 409 {@code CONFLICTO_HUECO_OCUPADO}: un solo usuario consigue el hueco.</p>
 *
 * <p>Toda operacion que cambia el estado de una cita escribe <em>en una sola
 * transaccion</em> la cita, su hueco, su eslabon de auditoria y, si viene de una solicitud,
 * el estado de esa solicitud. Si algo falla a mitad, no queda ni cita ni hueco bloqueado ni
 * rastro: es lo que garantiza que la agenda no se llene de citas fantasmas.</p>
 *
 * <p>Las transiciones de la maquina de estados las rechaza el dominio con
 * {@link IllegalStateException}; el servicio las traduce a
 * {@code CitaConflictoException(TRANSICION_INVALIDA)} para que la API responda 409 en vez
 * de 500. Los datos obligatorios que faltan se detectan aqui y se dejan como
 * {@link IllegalArgumentException} (400), como fija la decision de contrato de CFG-04.</p>
 *
 * <p>El reloj es inyectable: el corte del lote de expiracion y el plazo de cancelacion del
 * catalogo se miden sobre el mismo reloj, asi que las pruebas los fijan.</p>
 *
 * @since 1.1.0 (CFG-05, CDU12)
 */
@Service
public class CitaServicio implements CitaFacade {

    private static final Logger LOG = LoggerFactory.getLogger(CitaServicio.class);

    /** Estados que siguen ocupando hueco y, por tanto, pueden vencer. */
    private static final List<Cita.Estado> ESTADOS_ACTIVOS =
            List.of(Cita.Estado.PROGRAMADA, Cita.Estado.CONFIRMADA);

    /** Usuario que escribe cuando no hay autenticacion: los lotes de expiracion. */
    private static final String USUARIO_SISTEMA = "sistema";

    private final CitaRepositorio citas;
    private final CitaHuecoRepositorio huecos;
    private final AuditoriaCitasRepositorio auditoria;
    private final AgendaFacade agendas;
    private final PruebaCatalogoFacade catalogo;
    private final SolicitudFacade solicitudes;
    private final Clock reloj;

    /**
     * Inyecta los repositorios del modulo y las fachadas de los subdominios con los que
     * conversa (agendas CDU13, catalogo CDU16 y solicitudes CDU06) y el reloj del sistema.
     *
     * @param citas      acceso JPA a las citas
     * @param huecos     acceso JPA a los huecos bloqueados
     * @param auditoria  acceso JPA al rastro encadenado
     * @param agendas    fachada de agendas para validar franjas y festivos
     * @param catalogo   fachada de catalogo para duracion y plazo de cancelacion
     * @param solicitudes fachada de solicitudes para marcarla citada y devolverla a creada
     */
    @Autowired
    public CitaServicio(CitaRepositorio citas, CitaHuecoRepositorio huecos,
            AuditoriaCitasRepositorio auditoria, AgendaFacade agendas, PruebaCatalogoFacade catalogo,
            SolicitudFacade solicitudes) {
        this(citas, huecos, auditoria, agendas, catalogo, solicitudes, Clock.systemUTC());
    }

    /**
     * Variante con reloj explicito para pruebas: corte de expiracion y plazo de cancelacion
     * deterministas.
     *
     * @param citas       acceso JPA a las citas
     * @param huecos      acceso JPA a los huecos bloqueados
     * @param auditoria   acceso JPA al rastro encadenado
     * @param agendas     fachada de agendas
     * @param catalogo    fachada de catalogo
     * @param solicitudes fachada de solicitudes
     * @param reloj       reloj de referencia para los cortes
     */
    public CitaServicio(CitaRepositorio citas, CitaHuecoRepositorio huecos,
            AuditoriaCitasRepositorio auditoria, AgendaFacade agendas, PruebaCatalogoFacade catalogo,
            SolicitudFacade solicitudes, Clock reloj) {
        this.citas = citas;
        this.huecos = huecos;
        this.auditoria = auditoria;
        this.agendas = agendas;
        this.catalogo = catalogo;
        this.solicitudes = solicitudes;
        this.reloj = reloj;
    }

    // ------------------------------------------------------------------
    // Alta y cambios de estado
    // ------------------------------------------------------------------

    /** {@inheritDoc} */
    @Override
    @Transactional
    public CitaDTO agendar(CitaNuevaDTO nueva) {
        if (nueva == null) {
            throw new IllegalArgumentException("hay que indicar que cita se agenda");
        }
        if (nueva.profesionalId() == null || nueva.profesionalId().isBlank()) {
            throw new IllegalArgumentException("la cita necesita un profesional");
        }
        if (nueva.inicio() == null) {
            throw new IllegalArgumentException("la cita necesita un inicio");
        }
        if (nueva.duracionMinutos() < 0) {
            throw new IllegalArgumentException("la duracion no puede ser negativa");
        }
        String solicitudId = (nueva.solicitudId() == null || nueva.solicitudId().isBlank())
                ? null : nueva.solicitudId().trim();

        // La duracion puede venir del catalogo (CDU16): si el cuerpo no la trae, se la
        // pregunta a la prueba de la solicitud. Una cita directa sin solicitud y sin
        // duracion no se puede agendar: no hay de donde sacarla.
        int duracion = nueva.duracionMinutos();
        if (solicitudId != null) {
            SolicitudDTO solicitud = solicitudO409(solicitudId);
            if (duracion <= 0) {
                duracion = duracionDeCatalogo(solicitud.codigoPrueba());
            }
        }
        if (duracion <= 0) {
            throw new IllegalArgumentException(
                    "la cita necesita duracion en minutos: no la trae el cuerpo y la prueba no la fija");
        }

        Cita cita = Cita.nueva(solicitudId, nueva.profesionalId(), nueva.inicio(), duracion);
        exigeCaberEnFranja(cita);
        exigeHuecoLibre(cita.getProfesionalId(), null, cita.getInicio(), cita.getFin());

        citas.save(cita);
        // saveAndFlush: sin este envia, la restriccion unica no se comprueba hasta el commit
        // y los dos hilos parecerian haber reservado el mismo hueco. Si la base de datos la
        // rechaza, la transaccion cae con un fallo de integridad que la capa web traduce en
        // 409 CONFLICTO_HUECO_OCUPADO: el perdedor no se queda con un hueco doble.
        huecos.saveAndFlush(new CitaHueco(cita.getCitaId(), cita.getProfesionalId(), cita.getInicio(),
                cita.getFin()));
        registrar(AccionAuditoria.AGENDAMIENTO, cita);
        if (solicitudId != null) {
            marcarCitada(solicitudId);
        }
        return aDTO(cita);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public CitaDTO confirmar(String citaId) {
        Cita cita = citaO404(citaId);
        try {
            cita.confirmar();
        } catch (IllegalStateException e) {
            throw transicionInvalida(e);
        }
        citas.save(cita);
        registrar(AccionAuditoria.CONFIRMACION, cita);
        return aDTO(cita);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public CitaDTO realizar(String citaId) {
        Cita cita = citaO404(citaId);
        try {
            cita.realizar();
        } catch (IllegalStateException e) {
            throw transicionInvalida(e);
        }
        citas.save(cita);
        // La prueba se hizo: el hueco deja de estar bloqueado y la solicitud sigue citada
        // (la trazabilidad de lo realizado la cierra CDU06 al registrar el resultado).
        huecos.deleteByCitaId(cita.getCitaId());
        registrar(AccionAuditoria.REALIZACION, cita);
        return aDTO(cita);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public CitaDTO reprogramar(String citaId, LocalDateTime inicio) {
        if (inicio == null) {
            throw new IllegalArgumentException("la reprogramacion necesita un inicio nuevo");
        }
        Cita cita = citaO404(citaId);
        try {
            cita.moverA(inicio, cita.duracionMinutos());
        } catch (IllegalStateException e) {
            throw transicionInvalida(e);
        }
        exigeCaberEnFranja(cita);
        // El hueco propio no cuenta como ocupacion: si no, una cita que se mueve a una hora
        // que solapa su antigua franja se bloquearia a si misma.
        exigeHuecoLibre(cita.getProfesionalId(), cita.getCitaId(), cita.getInicio(), cita.getFin());

        citas.save(cita);
        // Se mueve la misma fila del hueco en vez de borrarla y crear otra: asi el @Version
        // exigido por la matriz de calidad de CFG-05 hace su trabajo y dos reprogramaciones
        // simultaneas de la misma cita no se pisan. El saveAndFlush fuerza la restriccion
        // unica en la base de datos, que es la que decide si el destino estaba libre.
        CitaHueco hueco = huecos.findByCitaId(cita.getCitaId())
                .orElseGet(() -> new CitaHueco(cita.getCitaId(), cita.getProfesionalId(), cita.getInicio(),
                        cita.getFin()));
        hueco.moverA(cita.getInicio(), cita.getFin());
        huecos.saveAndFlush(hueco);
        registrar(AccionAuditoria.REPROGRAMACION, cita);
        return aDTO(cita);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public CitaDTO cancelar(String citaId, String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("la cancelacion necesita un motivo");
        }
        Cita cita = citaO404(citaId);
        try {
            cita.cancelar(motivo.trim());
        } catch (IllegalStateException e) {
            throw transicionInvalida(e);
        }
        exigePlazoDeCancelacion(cita);
        citas.save(cita);
        huecos.deleteByCitaId(cita.getCitaId());
        registrar(AccionAuditoria.CANCELACION, cita);
        if (cita.getSolicitudId() != null) {
            devolverACreada(cita.getSolicitudId());
        }
        return aDTO(cita);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public List<CitaDTO> expirarVencidas(LocalDateTime corte) {
        LocalDateTime referencia = corte == null ? LocalDateTime.now(reloj) : corte;
        List<CitaDTO> vencidas = new ArrayList<>();
        for (Cita cita : citas.findByEstadoInAndFinBefore(ESTADOS_ACTIVOS, referencia)) {
            try {
                cita.expirar();
            } catch (IllegalStateException e) {
                LOG.warn("la cita {} ya estaba en estado terminal al expirar: {}", cita.getCitaId(),
                        e.getMessage());
                continue;
            }
            citas.save(cita);
            huecos.deleteByCitaId(cita.getCitaId());
            registrar(AccionAuditoria.EXPIRACION, cita);
            if (cita.getSolicitudId() != null) {
                devolverACreadaTolerante(cita.getSolicitudId());
            }
            vencidas.add(aDTO(cita));
        }
        return vencidas;
    }

    // ------------------------------------------------------------------
    // Consultas
    // ------------------------------------------------------------------

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<DetalleCitaDTO> listar(LocalDateTime desde, LocalDateTime hasta, String profesionalId,
            String estado) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new IllegalArgumentException("el rango de fechas no es coherente: desde despues de hasta");
        }
        List<Cita> encontradas = citas.busca(desde, hasta,
                (profesionalId == null || profesionalId.isBlank()) ? null : profesionalId.trim(),
                (estado == null || estado.isBlank()) ? null : aEstado(estado));
        return encontradas.stream().map(this::aDetalle).toList();
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public DetalleCitaDTO detalle(String citaId) {
        return aDetalle(citaO404(citaId));
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<HuecoDTO> huecosLibres(String profesionalId, LocalDate desde, LocalDate hasta,
            Integer duracionMinutos) {
        if (profesionalId == null || profesionalId.isBlank()) {
            throw new IllegalArgumentException("hay que indicar el profesional de la agenda");
        }
        if (desde == null || hasta == null) {
            throw new IllegalArgumentException("el rango de dias es obligatorio");
        }
        if (desde.isAfter(hasta)) {
            throw new IllegalArgumentException("el rango de dias no es coherente: desde despues de hasta");
        }
        if (duracionMinutos != null && duracionMinutos <= 0) {
            throw new IllegalArgumentException("la duracion pedida debe ser positiva");
        }
        List<HuecoDTO> libres = new ArrayList<>();
        // franjasLibres ya descarta los dias festivos y viene ordenado por dia e inicio.
        for (FranjaDTO franja : franjasDelProfesional(profesionalId.trim(), desde, hasta)) {
            LocalDateTime apertura = LocalDateTime.of(franja.dia(), franja.inicio());
            LocalDateTime cierre = LocalDateTime.of(franja.dia(), franja.fin());
            List<CitaHueco> bloqueados = huecosQueSolapan(profesionalId.trim(), apertura, cierre);
            LocalDateTime cursor = apertura;
            for (CitaHueco bloqueado : bloqueados) {
                if (bloqueado.getInicio().isAfter(cursor)) {
                    anyadirHueco(libres, profesionalId.trim(), cursor, bloqueado.getInicio(), duracionMinutos);
                }
                if (bloqueado.getFin().isAfter(cursor)) {
                    cursor = bloqueado.getFin();
                }
            }
            if (cursor.isBefore(cierre)) {
                anyadirHueco(libres, profesionalId.trim(), cursor, cierre, duracionMinutos);
            }
        }
        return libres;
    }

    /**
     * Comprueba que la cadena de auditoria de las citas sigue siendo verificable: cada
     * registro enlaza con el hash recalculado del anterior. Usada por las pruebas de CFG-05
     * (espejo de la auditoria de catalogo de CFG-01 y de agendas de CFG-03).
     *
     * @return true si ningun registro fue alterado despues de su escritura
     */
    @Transactional(readOnly = true)
    public boolean auditoriaVerificable() {
        return AuditoriaCitas.cadenaIntegra(auditoria.findAllByOrderByIdAsc());
    }

    // ------------------------------------------------------------------
    // Comprobaciones de las reglas del CDU12
    // ------------------------------------------------------------------

    /**
     * Exige que la cita quepa entera en una franja de disponibilidad del profesional. Una
     * franja nunca cruza la medianoche, asi que la contencion se comprueba en una sola
     * franja; los dias festivos no aparecen en {@link AgendaFacade#franjasLibres}, de modo
     * que no caber cubre tambien agendar en festivo.
     *
     * @param cita cita con el intervalo ya calculado
     * @throws CitaConflictoException con tipo FUERA_DE_FRANJA si no cabe
     */
    private void exigeCaberEnFranja(Cita cita) {
        List<FranjaDTO> franjas = franjasDelProfesional(cita.getProfesionalId(),
                cita.getInicio().toLocalDate(), cita.getFin().toLocalDate());
        boolean cabe = franjas.stream().anyMatch(franja -> cabeEn(franja, cita.getInicio(), cita.getFin()));
        if (!cabe) {
            throw new CitaConflictoException(Conflicto.FUERA_DE_FRANJA,
                    "el hueco " + cita.getInicio() + " - " + cita.getFin() + " no cabe en ninguna franja de "
                            + cita.getProfesionalId() + " (el dia puede ser festivo o estar fuera de jornada)");
        }
    }

    /**
     * Franjas laborables del profesional en el rango de dias pedido. Si el profesional no
     * tiene agenda, CDU13 responde con su propia excepcion: aqui se traduce al conflicto del
     * CDU12 porque sin agenda simplemente no hay ninguna franja donde quepa el hueco, y el
     * cliente tiene que recibir 409 CONFLICTO_FUERA_DE_FRANJA, no un error interno.
     *
     * @param profesionalId profesional cuya agenda se consulta
     * @param desde         primer dia del rango
     * @param hasta         ultimo dia del rango
     * @return franjas libres ordenadas por dia e inicio
     * @throws CitaConflictoException con tipo FUERA_DE_FRANJA si el profesional no tiene agenda
     */
    private List<FranjaDTO> franjasDelProfesional(String profesionalId, LocalDate desde, LocalDate hasta) {
        try {
            return agendas.franjasLibres(profesionalId, desde, hasta);
        } catch (AgendaNoEncontradoException e) {
            throw new CitaConflictoException(Conflicto.FUERA_DE_FRANJA,
                    "no hay agenda de " + profesionalId + ", asi que no hay franja donde quepa el hueco");
        }
    }

    private static boolean cabeEn(FranjaDTO franja, LocalDateTime inicio, LocalDateTime fin) {
        LocalDateTime apertura = LocalDateTime.of(franja.dia(), franja.inicio());
        LocalDateTime cierre = LocalDateTime.of(franja.dia(), franja.fin());
        return !inicio.isBefore(apertura) && !fin.isAfter(cierre);
    }

    /**
     * Exige que ningun hueco activo ocupe el intervalo pedido. Primero se comprueba el
     * inicio exacto, que es la columna de la restriccion unica y el caso de la concurrencia;
     * despues el solape, para que no se cuele una cita que empieza a mitad de otra. En la
     * reprogramacion se pasa el id de la propia cita en {@code citaIdPropia} para que su
     * hueco antiguo no se cuente como ocupacion.
     *
     * @param profesionalId profesional cuya agenda se comprueba
     * @param citaIdPropia  cita que ya tiene hueco y no debe bloquearse a si misma,
     *                      {@code null} al agendar
     * @param inicio        inicio del intervalo pedido
     * @param fin           fin del intervalo pedido
     * @throws CitaConflictoException con tipo HUECO_OCUPADO si alguien ya lo tiene
     */
    private void exigeHuecoLibre(String profesionalId, String citaIdPropia, LocalDateTime inicio,
            LocalDateTime fin) {
        huecos.findByProfesionalIdAndInicio(profesionalId, inicio)
                .filter(ocupado -> !ocupado.getCitaId().equals(citaIdPropia))
                .ifPresent(ocupado -> {
                    throw new CitaConflictoException(Conflicto.HUECO_OCUPADO,
                            "el hueco de " + profesionalId + " a las " + inicio + " ya lo ocupa la cita "
                                    + ocupado.getCitaId());
                });
        huecosQueSolapan(profesionalId, inicio, fin).stream()
                .filter(solapado -> !solapado.getCitaId().equals(citaIdPropia))
                .findFirst().ifPresent(solapado -> {
                    throw new CitaConflictoException(Conflicto.HUECO_OCUPADO,
                            "el hueco de " + profesionalId + " " + inicio + " - " + fin
                                    + " solapa con la cita " + solapado.getCitaId() + " ("
                                    + solapado.getInicio() + " - " + solapado.getFin() + ")");
                });
    }

    /**
     * Huecos activos del profesional que solapan el intervalo. Se buscan con una ventana de
     * un dia a cada lado porque la consulta filtra por el inicio del hueco, y un hueco que
     * empieza antes del intervalo pedido puede solaparlo; como las franjas no cruzan la
     * medianoche, con un dia de margen basta.
     *
     * @param profesionalId profesional cuya agenda se consulta
     * @param inicio        inicio del intervalo
     * @param fin           fin del intervalo
     * @return huecos que solapan, ordenados por inicio
     */
    private List<CitaHueco> huecosQueSolapan(String profesionalId, LocalDateTime inicio, LocalDateTime fin) {
        return huecos.findByProfesionalIdAndInicioBetween(profesionalId, inicio.minusDays(1), fin.plusDays(1))
                .stream()
                .filter(hueco -> hueco.solapaCon(inicio, fin))
                .sorted(Comparator.comparing(CitaHueco::getInicio))
                .toList();
    }

    /**
     * Exige el preaviso minimo de cancelacion que fija la politica de la prueba en el
     * catalogo (CDU16). Solo aplica a las citas que vienen de una solicitud con prueba
     * catalogada: una cita directa no tiene politica que exigir.
     *
     * @param cita cita ya cancelada en memoria
     * @throws CitaConflictoException con tipo PLAZO_CANCELACION si llega tarde
     */
    private void exigePlazoDeCancelacion(Cita cita) {
        if (cita.getSolicitudId() == null) {
            return;
        }
        SolicitudDTO solicitud = solicitudes.buscar(cita.getSolicitudId()).orElse(null);
        if (solicitud == null) {
            return;
        }
        PruebaImagenDTO prueba = catalogo.buscarPorCodigo(solicitud.codigoPrueba()).orElse(null);
        if (prueba == null || prueba.politica() == null || prueba.politica().plazoCancelacionHoras() <= 0) {
            return;
        }
        int horas = prueba.politica().plazoCancelacionHoras();
        LocalDateTime limite = cita.getInicio().minusHours(horas);
        if (LocalDateTime.now(reloj).isAfter(limite)) {
            throw new CitaConflictoException(Conflicto.PLAZO_CANCELACION,
                    "la cita " + cita.getCitaId() + " solo puede cancelarse con " + horas
                            + " horas de antelacion y el limite era " + limite);
        }
    }

    private int duracionDeCatalogo(String codigoPrueba) {
        return catalogo.buscarPorCodigo(codigoPrueba)
                .map(PruebaImagenDTO::duracionMinutos)
                .orElse(0);
    }

    // ------------------------------------------------------------------
    // Acoplamiento con CDU06 y auditoria encadenada
    // ------------------------------------------------------------------

    private SolicitudDTO solicitudO409(String solicitudId) {
        SolicitudDTO solicitud = solicitudes.buscar(solicitudId)
                .orElseThrow(() -> new CitaConflictoException(Conflicto.SOLICITUD_NO_APTA,
                        "no hay solicitud con id " + solicitudId));
        if (!"creada".equalsIgnoreCase(solicitud.estado())) {
            throw new CitaConflictoException(Conflicto.SOLICITUD_NO_APTA,
                    "la solicitud " + solicitudId + " esta en estado " + solicitud.estado()
                            + " y solo se agenda sobre una creada");
        }
        return solicitud;
    }

    /**
     * Marca la solicitud como citada en CDU06. La fachada es la que decide si el estado de
     * la solicitud lo permite: si se opone, aqui se traduce al conflicto del CDU12 para que
     * el codigo de error no dependa del orden de los consejos de excepcion.
     *
     * @param solicitudId solicitud que pasa a citada
     */
    private void marcarCitada(String solicitudId) {
        try {
            solicitudes.marcarCitada(solicitudId);
        } catch (SolicitudConflictoException | SolicitudNoEncontradaException e) {
            throw new CitaConflictoException(Conflicto.SOLICITUD_NO_APTA,
                    "la solicitud " + solicitudId + " no admite cita: " + e.getMessage());
        }
    }

    private void devolverACreada(String solicitudId) {
        try {
            solicitudes.devolverACreada(solicitudId);
        } catch (SolicitudConflictoException | SolicitudNoEncontradaException e) {
            throw new CitaConflictoException(Conflicto.SOLICITUD_NO_APTA,
                    "la solicitud " + solicitudId + " no puede volver a creada: " + e.getMessage());
        }
    }

    /**
     * Devuelve la solicitud a creada en el lote de expiracion. Aqui el fallo no rompe el
     * lote: una cita vencida tiene que liberar su hueco aunque su solicitud ya este cerrada
     * por otro subdominio, y lo que no puede quedar es un hueco bloqueado para siempre.
     *
     * @param solicitudId solicitud que vuelve a creada
     */
    private void devolverACreadaTolerante(String solicitudId) {
        try {
            solicitudes.devolverACreada(solicitudId);
        } catch (SolicitudConflictoException | SolicitudNoEncontradaException e) {
            LOG.warn("la solicitud {} no pudo volver a creada al expirar su cita: {}", solicitudId,
                    e.getMessage());
        }
    }

    /**
     * Escribe el eslabon de auditoria de un cambio de estado. El material canonico es
     * {@code accion|citaId|profesionalId|usuario|instante|hashAnterior}: el rastro responde
     * por que cita se hizo que y quien la hizo.
     *
     * @param accion accion realizada
     * @param cita   cita afectada
     */
    private void registrar(AccionAuditoria accion, Cita cita) {
        String hashAnterior = auditoria.findTopByOrderByIdDesc()
                .map(AuditoriaCitas::getHash)
                .orElse(null);
        Instant ahora = Instant.now(reloj);
        String usuario = usuarioEnSeguridad();
        String hash = AuditoriaCitas.hashDe(accion, cita.getCitaId(), cita.getProfesionalId(), usuario,
                ahora, hashAnterior);
        auditoria.save(new AuditoriaCitas(accion, cita.getCitaId(), cita.getProfesionalId(), usuario,
                ahora, hashAnterior, hash));
    }

    private static String usuarioEnSeguridad() {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        return autenticacion == null ? USUARIO_SISTEMA : autenticacion.getName();
    }

    // ------------------------------------------------------------------
    // Vuelta a DTO y ayudas de error
    // ------------------------------------------------------------------

    private Cita citaO404(String citaId) {
        if (citaId == null || citaId.isBlank()) {
            throw new CitaNoEncontradaException("no hay cita con el id " + citaId);
        }
        return citas.findByCitaId(citaId.trim())
                .orElseThrow(() -> new CitaNoEncontradaException("no hay cita con el id " + citaId));
    }

    private DetalleCitaDTO aDetalle(Cita cita) {
        long version = huecos.findByCitaId(cita.getCitaId())
                .map(hueco -> (long) hueco.getVersion())
                .orElse(0L);
        return new DetalleCitaDTO(cita.getCitaId(), cita.getSolicitudId(), cita.getProfesionalId(),
                cita.getInicio(), cita.getFin(), estadoEnMinusculas(cita.getEstado()),
                cita.getMotivoCancelacion(), version);
    }

    private static CitaDTO aDTO(Cita cita) {
        return new CitaDTO(cita.getCitaId(), cita.getSolicitudId(), cita.getInicio(), cita.getFin(),
                estadoEnMinusculas(cita.getEstado()));
    }

    private static String estadoEnMinusculas(Cita.Estado estado) {
        return estado.name().toLowerCase(Locale.ROOT);
    }

    private static Cita.Estado aEstado(String estado) {
        try {
            return Cita.Estado.valueOf(estado.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("el estado " + estado + " no es de la maquina de citas: "
                    + "programada, confirmada, realizada, cancelada o expirada");
        }
    }

    private static CitaConflictoException transicionInvalida(IllegalStateException causa) {
        return new CitaConflictoException(Conflicto.TRANSICION_INVALIDA, causa.getMessage());
    }

    private static void anyadirHueco(List<HuecoDTO> libres, String profesionalId, LocalDateTime desde,
            LocalDateTime hasta, Integer duracionPedida) {
        int minutosLibres = (int) Duration.between(desde, hasta).toMinutes();
        if (minutosLibres <= 0) {
            return;
        }
        if (duracionPedida != null && minutosLibres < duracionPedida) {
            return;
        }
        libres.add(new HuecoDTO(profesionalId, desde, hasta, duracionPedida == null ? null : minutosLibres));
    }
}
