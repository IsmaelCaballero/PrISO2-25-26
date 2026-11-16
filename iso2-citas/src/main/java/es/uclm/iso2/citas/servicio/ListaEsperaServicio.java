package es.uclm.iso2.citas.servicio;

import es.uclm.iso2.citas.dominio.AuditoriaEspera;
import es.uclm.iso2.citas.dominio.AuditoriaEspera.Accion;
import es.uclm.iso2.citas.dominio.ListaEspera;
import es.uclm.iso2.citas.dominio.ListaEspera.Estado;
import es.uclm.iso2.citas.repositorio.AuditoriaEsperaRepositorio;
import es.uclm.iso2.citas.repositorio.ListaEsperaRepositorio;
import es.uclm.iso2.common.catalogo.CatalogoNoEncontradoException;
import es.uclm.iso2.common.catalogo.PruebaCatalogoFacade;
import es.uclm.iso2.common.catalogo.PruebaCatalogoFacade.PruebaImagenDTO;
import es.uclm.iso2.common.citas.CitaConflictoException;
import es.uclm.iso2.common.citas.CitaConflictoException.Conflicto;
import es.uclm.iso2.common.citas.CitaFacade;
import es.uclm.iso2.common.citas.CitaFacade.CitaDTO;
import es.uclm.iso2.common.citas.CitaFacade.CitaNuevaDTO;
import es.uclm.iso2.common.citas.ListaEsperaConflictoException;
import es.uclm.iso2.common.citas.ListaEsperaFacade;
import es.uclm.iso2.common.citas.ListaEsperaNoEncontradaException;
import es.uclm.iso2.common.solicitudes.Prioridad;
import es.uclm.iso2.common.solicitudes.SolicitudFacade;
import es.uclm.iso2.common.solicitudes.SolicitudFacade.SolicitudDTO;
import es.uclm.iso2.common.solicitudes.SolicitudNoEncontradaException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementacion del contrato de listas de espera (CDU14, CFG-07). Es el motor de la
 * cola: guarda de sintaxis antes de tocar base de datos, anti-reincidencia por
 * {@code claveDuplicada}, reubicacion y retiro con motivo obligatorio, y asignacion
 * que delega el hueco en {@link CitaServicio} sin tocar el contrato congelado del
 * CDU12 (D23: CDU14 invierte la dependencia con CDU01 al usar el motor aqui).
 *
 * <p>La posicion nunca se persiste: se calcula sobre el contexto de la propia entrada
 * (su prueba y su estado) con {@link ListaEspera#porOrdenDeEspera()}, el mismo orden
 * del {@code @Query} del repositorio (RF14.3). Cada cambio de estado escribe su
 * eslabon en la cadena de {@link AuditoriaEspera} dentro de la misma transaccion,
 * con la misma tecnica de genesis encadenado que {@code CitaServicio.registrar}.</p>
 *
 * @since 1.2.0 (CFG-07, CDU14)
 */
@Service
public class ListaEsperaServicio implements ListaEsperaFacade {

    private static final Logger LOG = LoggerFactory.getLogger(ListaEsperaServicio.class);

    /** Usuario que firma los asientos cuando no hay autenticacion (expiracion, tests). */
    private static final String USUARIO_SISTEMA = "sistema";

    /** Ultima longitud que cabe en paciente_id y codigo_prueba (TDOP alta, MAX). */
    private static final int MAX_LONGITUD_CODIGO = 40;

    /** Ultima longitud que cabe en motivo_fin y en el eslabon de auditoria (MAX300). */
    private static final int MAX_LONGITUD_MOTIVO = 300;

    /** Hash reportado con cadena vacia: no hay eslabon del que dar hash (TC14-G09). */
    private static final String HASH_SIN_CADENA = "";

    private static final String INTEGRAL = "CADENA_INTEGRAL";
    private static final String MANIPULADA = "CADENA_MANIPULADA";

    private final ListaEsperaRepositorio esperas;
    private final AuditoriaEsperaRepositorio auditoria;
    private final PruebaCatalogoFacade catalogo;
    private final SolicitudFacade solicitudes;
    private final CitaServicio citaServicio;
    private final Clock reloj;

    /**
     * Crea el servicio con las dependencias de produccion.
     *
     * @param esperas      acceso JPA a las entradas de la cola
     * @param auditoria    acceso JPA al rastro encadenado de la cola
     * @param catalogo     fachada de catalogo de pruebas (CDU16)
     * @param solicitudes fachada de solicitudes (CDU06)
     * @param citaServicio motor de citas del CDU12 para la asignacion desde la cola
     */
    @Autowired
    public ListaEsperaServicio(ListaEsperaRepositorio esperas, AuditoriaEsperaRepositorio auditoria,
            PruebaCatalogoFacade catalogo, SolicitudFacade solicitudes, CitaServicio citaServicio) {
        this(esperas, auditoria, catalogo, solicitudes, citaServicio, Clock.systemUTC());
    }

    /**
     * Variante con reloj explicito para pruebas: {@code fechaSolicitud} e instante de
     * los asientos deterministas.
     *
     * @param esperas      acceso JPA a las entradas de la cola
     * @param auditoria    acceso JPA al rastro encadenado de la cola
     * @param catalogo     fachada de catalogo de pruebas
     * @param solicitudes fachada de solicitudes
     * @param citaServicio motor de citas del CDU12
     * @param reloj        reloj de referencia
     */
    public ListaEsperaServicio(ListaEsperaRepositorio esperas, AuditoriaEsperaRepositorio auditoria,
            PruebaCatalogoFacade catalogo, SolicitudFacade solicitudes, CitaServicio citaServicio,
            Clock reloj) {
        this.esperas = esperas;
        this.auditoria = auditoria;
        this.catalogo = catalogo;
        this.solicitudes = solicitudes;
        this.citaServicio = citaServicio;
        this.reloj = reloj;
    }

    // ------------------------------------------------------------------
    // Alta (UC14.1)
    // ------------------------------------------------------------------

    @Override
    @Transactional
    public EntradaEsperaDTO darDeAlta(AltaEsperaDTO alta) {
        // Sintaxis primero: la guarda del servicio precede a cualquier consulta (TDOP alta).
        String pacienteId = alta.pacienteId().trim();
        String pruebaCodigo = alta.pruebaCodigo().trim();
        exigeLongitud(pacienteId, MAX_LONGITUD_CODIGO, "pacienteId");
        exigeLongitud(pruebaCodigo, MAX_LONGITUD_CODIGO, "pruebaCodigo");
        String profesionalId = recortarOpcional(alta.profesionalId());
        exigeLongitud(profesionalId, MAX_LONGITUD_CODIGO, "profesionalId");
        String solicitudId;
        if (alta.solicitudId() == null) {
            solicitudId = null;
        } else {
            String recortada = alta.solicitudId().trim();
            if (recortada.isEmpty()) {
                throw new IllegalArgumentException(
                        "solicitudId llega en blanco: un opcional mal formado es error del cliente, no una ausencia");
            }
            solicitudId = recortada;
        }
        Prioridad prioridad = Prioridad.desdeNombre(alta.prioridad())
                .orElseThrow(() -> new IllegalArgumentException("prioridad no entendible: " + alta.prioridad()));

        // La prueba tiene que estar en el catalogo (404 PRUEBA_NO_ENCONTRADA); de la
        // solicitud solo se comprueba que exista: el alta no mira su estado (TDOP alta).
        if (catalogo.buscarPorCodigo(pruebaCodigo).isEmpty()) {
            throw new CatalogoNoEncontradoException(pruebaCodigo);
        }
        if (solicitudId != null && solicitudes.buscar(solicitudId).isEmpty()) {
            throw new SolicitudNoEncontradaException("no hay solicitud con id " + solicitudId);
        }

        // Anti-reincidencia por claveDuplicada viva; el UNIQUE de la tabla es la red
        // contra la carrera: el segundo insert pierde y also es alta duplicada.
        String clave = pacienteId + "|" + pruebaCodigo;
        esperas.findByClaveDuplicada(clave).ifPresent(viva -> {
            throw new ListaEsperaConflictoException(
                    ListaEsperaConflictoException.Conflicto.ALTA_DUPLICADA,
                    "el paciente " + pacienteId + " ya esta en la cola de " + pruebaCodigo
                            + " con la entrada " + viva.getEsperaId());
        });

        Instant fechaSolicitud = alta.fechaSolicitud() == null ? Instant.now(reloj) : alta.fechaSolicitud();
        ListaEspera entrada = ListaEspera.nueva(solicitudId, pacienteId, pruebaCodigo, profesionalId,
                prioridad, fechaSolicitud);
        try {
            esperas.saveAndFlush(entrada);
        } catch (DataIntegrityViolationException e) {
            LOG.warn("la base de datos rechazo el alta duplicada de {} en {}: {}", pacienteId,
                    pruebaCodigo, e.getMessage());
            throw new ListaEsperaConflictoException(
                    ListaEsperaConflictoException.Conflicto.ALTA_DUPLICADA,
                    "el paciente " + pacienteId + " ya esta en la cola de " + pruebaCodigo);
        }
        asentar(Accion.ALTA, entrada.getEsperaId(), null, Estado.ACTIVA.name(), null, usuarioEnSeguridad());
        return aDTOConContexto(entrada);
    }

    // ------------------------------------------------------------------
    // Lecturas: cola y detalle (RF14.3)
    // ------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<EntradaEsperaDTO> cola(String pruebaCodigo, String profesionalId, String estado) {
        String prueba = null;
        if (pruebaCodigo != null) {
            prueba = pruebaCodigo.trim();
            if (prueba.isEmpty()) {
                throw new IllegalArgumentException("el filtro de prueba llega en blanco: o filtra o no filtra");
            }
            if (catalogo.buscarPorCodigo(prueba).isEmpty()) {
                // En la cola el filtro desconocido es sintaxis (400); el 404 queda
                // reservado al alta, que es donde la prueba es un hecho del negocio.
                throw new IllegalArgumentException("la prueba " + prueba + " no existe en el catalogo");
            }
        }
        String profesional = null;
        if (profesionalId != null) {
            profesional = profesionalId.trim();
            if (profesional.isEmpty()) {
                throw new IllegalArgumentException("el filtro de profesional llega en blanco: o filtra o no filtra");
            }
            // El profesional del filtro no se valida contra agendas: SIN_AGENDA o
            // inexistente simplemente recorta a 0 filas (TDOP cola).
        }
        Estado estadoFiltro = estado == null ? null : aEstado(estado);

        List<ListaEspera> filas = new ArrayList<>(esperas.cola(prueba, profesional, estadoFiltro));
        filas.sort(ListaEspera.porOrdenDeEspera());
        List<EntradaEsperaDTO> salida = new ArrayList<>(filas.size());
        for (int posicion = 0; posicion < filas.size(); posicion++) {
            salida.add(aDTO(filas.get(posicion), posicion + 1, filas.size()));
        }
        return salida;
    }

    @Override
    @Transactional(readOnly = true)
    public EntradaEsperaDTO detalle(String esperaId) {
        return aDTOConContexto(entradaO404(esperaId));
    }

    // ------------------------------------------------------------------
    // Reubicacion (UC14.3) y retiro (UC14.4)
    // ------------------------------------------------------------------

    @Override
    @Transactional
    public EntradaEsperaDTO cambiarPrioridad(String esperaId, String prioridad, String motivo) {
        return cambiarPrioridadConUsuario(esperaId, prioridad, motivo, usuarioEnSeguridad());
    }

    /**
     * Variante con usuario explicito (bloque de servicio del PUML de diseno): el
     * eslabon REUBICACION firma con ese usuario en lugar del de la autenticacion.
     *
     * @param esperaId  entrada a reubicar
     * @param prioridad nueva prioridad de la escala CDU06
     * @param motivo    motivo obligatorio de la reubicacion
     * @param usuario   firmante del eslabon
     * @return la entrada reubicada con su posicion nueva
     */
    @Transactional
    public EntradaEsperaDTO cambiarPrioridad(String esperaId, String prioridad, String motivo, String usuario) {
        return cambiarPrioridadConUsuario(esperaId, prioridad, motivo, usuario);
    }

    private EntradaEsperaDTO cambiarPrioridadConUsuario(String esperaId, String prioridad, String motivo,
            String usuario) {
        // Sintaxis antes de cargar: un motivo en blanco o largo es 400 incluso si la
        // reubicacion tambien fuera redundante o inexistente (TDOP reubicar).
        Prioridad nueva = Prioridad.desdeNombre(prioridad)
                .orElseThrow(() -> new IllegalArgumentException("prioridad no entendible: " + prioridad));
        String motivoFinal = motivoObligatorio(motivo, "reubicar una entrada de la cola");

        ListaEspera entrada = entradaO404(esperaId);
        if (!entrada.estaActiva()) {
            throw new ListaEsperaConflictoException(
                    ListaEsperaConflictoException.Conflicto.ENTRADA_NO_ACTIVA,
                    "la entrada " + esperaId + " esta en estado " + estadoEnMinusculas(entrada.getEstado())
                            + " y ya no se reubica");
        }
        if (entrada.esMismaUbicacionQue(nueva)) {
            throw new ListaEsperaConflictoException(
                    ListaEsperaConflictoException.Conflicto.REUBICACION_REDUNDANTE,
                    "la entrada " + esperaId + " ya esta en " + nueva.name() + ": la reubicacion no moveria nada");
        }
        Prioridad anterior;
        try {
            anterior = entrada.cambiarPrioridad(nueva);
        } catch (IllegalStateException e) {
            throw new ListaEsperaConflictoException(
                    ListaEsperaConflictoException.Conflicto.ENTRADA_NO_ACTIVA,
                    e.getMessage());
        }
        asentar(Accion.REUBICACION, entrada.getEsperaId(), anterior.name(), nueva.name(), motivoFinal, usuario);
        return aDTOConContexto(entrada);
    }

    @Override
    @Transactional
    public EntradaEsperaDTO retirar(String esperaId, String motivo) {
        String motivoFinal = motivoObligatorio(motivo, "retirar una entrada de la cola");
        ListaEspera entrada = entradaO404(esperaId);
        if (!entrada.estaActiva()) {
            throw new ListaEsperaConflictoException(
                    ListaEsperaConflictoException.Conflicto.ENTRADA_NO_ACTIVA,
                    "la entrada " + esperaId + " esta en estado " + estadoEnMinusculas(entrada.getEstado())
                            + " y ya no se puede retirar");
        }
        try {
            entrada.retirar(motivoFinal);
        } catch (IllegalStateException e) {
            throw new ListaEsperaConflictoException(
                    ListaEsperaConflictoException.Conflicto.ENTRADA_NO_ACTIVA,
                    e.getMessage());
        }
        asentar(Accion.RETIRADA, entrada.getEsperaId(), Estado.ACTIVA.name(), Estado.RETIRADA.name(),
                motivoFinal, usuarioEnSeguridad());
        return aDTOConContexto(entrada);
    }

    // ------------------------------------------------------------------
    // Asignacion desde la cola (UC14.2): motor de huecos del CDU12
    // ------------------------------------------------------------------

    @Override
    @Transactional
    public AsignacionResultadoDTO asignarDesdeCola(String esperaId, String profesionalId, LocalDateTime inicio,
            Integer duracionMinutos) {
        // Sintaxis antes de cargar: el cuerpo trae lo que la cita del CDU12 necesita
        // (profesional en el cuerpo por la enmienda de contrato del #47).
        if (profesionalId == null || profesionalId.isBlank()) {
            throw new IllegalArgumentException("profesionalId es obligatorio para asignar desde la cola");
        }
        if (inicio == null) {
            throw new IllegalArgumentException("inicio es obligatorio para asignar desde la cola");
        }
        if (duracionMinutos != null && duracionMinutos < 0) {
            throw new IllegalArgumentException("la duracion en minutos no puede ser negativa: " + duracionMinutos);
        }
        String profesional = profesionalId.trim();

        ListaEspera entrada = entradaO404(esperaId);
        if (!entrada.estaActiva()) {
            throw new ListaEsperaConflictoException(
                    ListaEsperaConflictoException.Conflicto.ENTRADA_NO_ACTIVA,
                    "la entrada " + esperaId + " esta en estado " + estadoEnMinusculas(entrada.getEstado())
                            + " y ya no se asigna");
        }
        // La solicitud de origen tiene que estar creada (fail-fast antes del hueco,
        // igual que CitaServicio.solicitudO409): sin solicitud no hay cita que citar.
        if (entrada.getSolicitudId() == null) {
            throw new CitaConflictoException(Conflicto.SOLICITUD_NO_APTA,
                    "la entrada " + esperaId + " nacio sin solicitud: no hay solicitud que citar");
        }
        SolicitudDTO solicitud = solicitudes.buscar(entrada.getSolicitudId())
                .orElseThrow(() -> new CitaConflictoException(Conflicto.SOLICITUD_NO_APTA,
                        "no hay solicitud con id " + entrada.getSolicitudId()));
        if (!"creada".equalsIgnoreCase(solicitud.estado())) {
            throw new CitaConflictoException(Conflicto.SOLICITUD_NO_APTA,
                    "la solicitud " + solicitud.solicitudId() + " esta en estado " + solicitud.estado()
                            + " y solo se agenda sobre una creada");
        }

        // D1: duracion del cuerpo; D0/AUSENTE la fija el catalogo de la prueba (CDU16).
        int duracion = duracionMinutos != null && duracionMinutos > 0
                ? duracionMinutos
                : duracionDeCatalogo(entrada.getPruebaCodigo());

        // La posicion que se informa es la que ocupaba la entrada al asignar, sobre su
        // contexto (su prueba y ACTIVA), calculada antes de crear la cita.
        List<ListaEspera> contexto = contextoDe(entrada);
        int posicion = posicionEn(entrada, contexto);
        long total = contexto.size();

        CitaDTO cita = citaServicio.agendar(new CitaNuevaDTO(entrada.getSolicitudId(), profesional, inicio,
                duracion));

        try {
            entrada.asignarA(cita.citaId());
        } catch (IllegalStateException e) {
            throw new ListaEsperaConflictoException(
                    ListaEsperaConflictoException.Conflicto.ENTRADA_NO_ACTIVA,
                    e.getMessage());
        }
        asentar(Accion.ASIGNACION, entrada.getEsperaId(), Estado.ACTIVA.name(), Estado.ASIGNADA.name(), null,
                usuarioEnSeguridad());
        return new AsignacionResultadoDTO(cita, entrada.getEsperaId(), posicion, total);
    }

    // ------------------------------------------------------------------
    // Verificacion de la cadena (RF14.4)
    // ------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public VerificacionAuditoriaDTO verificarAuditoria() {
        List<AuditoriaEspera> cadena = auditoria.findAllByOrderByIdAsc();
        boolean integra = AuditoriaEspera.cadenaIntegra(cadena);
        String hashUltimo = cadena.isEmpty() ? HASH_SIN_CADENA : cadena.get(cadena.size() - 1).getHash();
        return new VerificacionAuditoriaDTO(integra ? INTEGRAL : MANIPULADA, cadena.size(), hashUltimo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditoriaEsperaDTO> auditoriaDe(String esperaId) {
        // auditar una entrada inexistente es 404 propio, no una lista vacia: la
        // guarda corre antes de leer la cadena (tabla G, fila NINGUNA de la spec)
        entradaO404(esperaId);
        return auditoria.findByEsperaIdOrderByIdAsc(esperaId.trim()).stream()
                .map(registro -> new AuditoriaEsperaDTO(registro.getEsperaId(),
                        registro.getAccion().name(), registro.getAntes(), registro.getDespues(),
                        registro.getUsuario(), registro.getMotivo(), registro.getInstante(),
                        registro.getHash(), registro.getHashAnterior()))
                .toList();
    }

    // ------------------------------------------------------------------
    // Auditoria encadenada y ayudas
    // ------------------------------------------------------------------

    /**
     * Escribe el eslabon de auditoria de un cambio sobre la cola. El material canonico
     * es {@code accion|esperaId|antes->despues|usuario|motivo|instante|hashAnterior};
     * el primero enlaza con el genesis, igual que {@code CitaServicio.registrar}.
     *
     * @param accion   accion registrada
     * @param esperaId entrada afectada
     * @param antes    prioridad o estado previo, {@code null} en el alta
     * @param despues  prioridad o estado nuevo
     * @param motivo   motivo de la accion, {@code null} en alta y asignacion
     * @param usuario  firmante del eslabon
     */
    private void asentar(Accion accion, String esperaId, String antes, String despues, String motivo,
            String usuario) {
        String hashAnterior = auditoria.findTopByOrderByIdDesc()
                .map(AuditoriaEspera::getHash)
                .orElse(null);
        Instant ahora = Instant.now(reloj);
        String hash = AuditoriaEspera.hashDe(accion, esperaId, antes, despues, usuario, motivo, ahora,
                hashAnterior);
        auditoria.save(new AuditoriaEspera(accion, esperaId, antes, despues, usuario, motivo, ahora,
                hashAnterior, hash));
    }

    /** Contexto de posicion de la entrada: su prueba y su estado, sin filtro de profesional. */
    private List<ListaEspera> contextoDe(ListaEspera entrada) {
        List<ListaEspera> contexto = new ArrayList<>(
                esperas.cola(entrada.getPruebaCodigo(), null, entrada.getEstado()));
        contexto.sort(ListaEspera.porOrdenDeEspera());
        return contexto;
    }

    /** Posicion 1-based de la entrada dentro de su contexto ordenado. */
    private static int posicionEn(ListaEspera entrada, List<ListaEspera> contexto) {
        for (int i = 0; i < contexto.size(); i++) {
            if (contexto.get(i).getEsperaId().equals(entrada.getEsperaId())) {
                return i + 1;
            }
        }
        throw new IllegalStateException("la entrada " + entrada.getEsperaId()
                + " no aparece en su propio contexto de cola");
    }

    private EntradaEsperaDTO aDTOConContexto(ListaEspera entrada) {
        List<ListaEspera> contexto = contextoDe(entrada);
        return aDTO(entrada, posicionEn(entrada, contexto), contexto.size());
    }

    private static EntradaEsperaDTO aDTO(ListaEspera entrada, int posicion, long total) {
        return new EntradaEsperaDTO(entrada.getEsperaId(), entrada.getSolicitudId(), entrada.getPacienteId(),
                entrada.getPruebaCodigo(), entrada.getProfesionalId(), entrada.getPrioridad().name(),
                entrada.getFechaSolicitud(), estadoEnMinusculas(entrada.getEstado()), entrada.getCitaId(),
                entrada.getMotivoFin(), posicion, total);
    }

    private static String estadoEnMinusculas(Estado estado) {
        return estado.name().toLowerCase(Locale.ROOT);
    }

    private static Estado aEstado(String estado) {
        try {
            return Estado.valueOf(estado.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("el estado " + estado + " no es de la maquina de la cola: "
                    + "activa, asignada o retirada");
        }
    }

    private ListaEspera entradaO404(String esperaId) {
        if (esperaId == null || esperaId.isBlank()) {
            throw new ListaEsperaNoEncontradaException("no hay entrada de la cola con el id " + esperaId);
        }
        return esperas.findByEsperaId(esperaId.trim())
                .orElseThrow(() -> new ListaEsperaNoEncontradaException("no hay entrada de la cola con el id "
                        + esperaId));
    }

    private static String recortarOpcional(String valor) {
        if (valor == null) {
            return null;
        }
        String recortado = valor.trim();
        return recortado.isEmpty() ? null : recortado;
    }

    private static void exigeLongitud(String valor, int maximo, String campo) {
        if (valor != null && valor.length() > maximo) {
            throw new IllegalArgumentException(campo + " pasa de " + maximo + " caracteres: no cabe en la columna");
        }
    }

    private static String motivoObligatorio(String motivo, String operacion) {
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("el motivo es obligatorio al " + operacion);
        }
        String recortado = motivo.trim();
        if (recortado.length() > MAX_LONGITUD_MOTIVO) {
            throw new IllegalArgumentException(
                    "el motivo pasa de " + MAX_LONGITUD_MOTIVO + " caracteres: no cabe en la columna ni en el eslabon");
        }
        return recortado;
    }

    private int duracionDeCatalogo(String pruebaCodigo) {
        return catalogo.buscarPorCodigo(pruebaCodigo)
                .map(PruebaImagenDTO::duracionMinutos)
                .orElseThrow(() -> new IllegalStateException(
                        "la prueba " + pruebaCodigo + " de la entrada ya no esta en el catalogo"));
    }

    private static String usuarioEnSeguridad() {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        return autenticacion == null ? USUARIO_SISTEMA : autenticacion.getName();
    }
}
