package es.uclm.iso2.integracion.servicio;

import es.uclm.iso2.common.integracion.CredencialesInvalidasException;
import es.uclm.iso2.common.integracion.InteropFacade;
import es.uclm.iso2.common.integracion.SistemaExternoException;
import es.uclm.iso2.common.integracion.SistemaNoEncontradoException;
import es.uclm.iso2.integracion.dominio.AuditoriaIntegracion;
import es.uclm.iso2.integracion.dominio.EslabonAuditoria;
import es.uclm.iso2.integracion.dominio.EstadoSistema;
import es.uclm.iso2.integracion.dominio.EstudioExterno;
import es.uclm.iso2.integracion.dominio.HistoriaExterna;
import es.uclm.iso2.integracion.dominio.HuellaContenido;
import es.uclm.iso2.integracion.dominio.RegistroHistoria;
import es.uclm.iso2.integracion.dominio.SesionSso;
import es.uclm.iso2.integracion.dominio.SistemaExterno;
import es.uclm.iso2.integracion.puerto.HcePort;
import es.uclm.iso2.integracion.puerto.PacsPort;
import es.uclm.iso2.integracion.puerto.RisPort;
import es.uclm.iso2.integracion.puerto.SsoPort;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Implementacion del contrato {@link InteropFacade} para el spike CDU19. Conoce solo
 * los puertos de salida: los adaptadores (simulados hoy, reales manana) se inyectan
 * detras. Regla ArchUnit: jamas depende de la capa adaptador ni de web.
 *
 * <p>Aqui viven las dos reglas de negocio del spike:</p>
 * <ul>
 *   <li><b>Idempotencia de la sincronizacion</b>: la huella SHA-256 de la forma
 *       canonica de la historia decide si se reutiliza la instantanea previa.</li>
 *   <li><b>Ping degradado</b>: el estado de los sistemas nunca lanza
 *       {@link SistemaExternoException}; un sistema caido se pinta con
 *       {@code disponible=false}.</li>
 * </ul>
 *
 * <p>La auditoria encadenada registra SSO_LOGIN, HCE_SINCRONIZAR y RIS_IMPORTAR con
 * su resultado; los pings de monitorizacion no se auditan para no llenar la cadena de
 * lecturas periodicas.</p>
 *
 * @since 1.1.0 (CFG-06, CDU19)
 */
@Service
public class IntegracionServicio implements InteropFacade {

    /** Actor cuando no hay autenticacion (llamadas internas o tests sin contexto). */
    private static final String USUARIO_SISTEMA = "USUARIO_SISTEMA";

    private final SsoPort sso;
    private final HcePort hce;
    private final RisPort ris;
    private final PacsPort pacs;
    private final AuditoriaIntegracion auditoria;

    /** Instantaneas por paciente para la idempotencia (en memoria: el spike no persiste). */
    private final ConcurrentHashMap<String, InstantaneaHistoria> snapshots = new ConcurrentHashMap<>();

    /**
     * Cablea el servicio con los cuatro puertos y la auditoria compartida.
     *
     * @param sso       puerto del IdP externo
     * @param hce       puerto de la historia clinica externa
     * @param ris       puerto del RIS
     * @param pacs      puerto del PACS
     * @param auditoria cadena de auditoria en memoria del subdominio
     */
    public IntegracionServicio(SsoPort sso, HcePort hce, RisPort ris, PacsPort pacs,
                               AuditoriaIntegracion auditoria) {
        this.sso = sso;
        this.hce = hce;
        this.ris = ris;
        this.pacs = pacs;
        this.auditoria = auditoria;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EstudioDTO importarEstudio(String accessionNumber) {
        try {
            EstudioExterno estudio = ris.buscarEstudio(accessionNumber);
            auditoria.registrar("RIS_IMPORTAR", "ris", "OK", actorActual());
            return new EstudioDTO(estudio.accessionNumber(), estudio.pacienteId(),
                    estudio.fechaEstudio(), estudio.numeroSeries());
        } catch (SistemaExternoException e) {
            auditoria.registrar("RIS_IMPORTAR", "ris", "FALLO_SISTEMA", actorActual());
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SesionSsoDTO autenticarSso(CredencialesSsoDTO credenciales) {
        if (credenciales == null) {
            throw new IllegalArgumentException("las credenciales son obligatorias");
        }
        try {
            SesionSso sesion = sso.autenticar(credenciales.usuario(), credenciales.contrasena());
            auditoria.registrar("SSO_LOGIN", "sso", "OK", actorActual());
            return new SesionSsoDTO(sesion.sujeto(), sesion.rolExterno(), sesion.caducaEn());
        } catch (SistemaExternoException e) {
            auditoria.registrar("SSO_LOGIN", "sso", "FALLO_SISTEMA", actorActual());
            throw e;
        } catch (CredencialesInvalidasException e) {
            auditoria.registrar("SSO_LOGIN", "sso", "FALLO_CREDENCIALES", actorActual());
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HistoriaSincronizadaDTO sincronizarHistoriaClinica(String pacienteExternoId) {
        HistoriaExterna historia = hce.buscarHistoria(pacienteExternoId);
        String huella = HuellaContenido.sha256Hex(historia.canonica());
        String idRecortado = pacienteExternoId == null ? "" : pacienteExternoId.trim();
        InstantaneaHistoria anterior = snapshots.get(idRecortado);
        if (anterior != null && anterior.huella().equals(huella)) {
            auditoria.registrar("HCE_SINCRONIZAR", "hce", "OK_REUTILIZADA", actorActual());
            return anterior.aDTOReutilizada(idRecortado);
        }
        List<RegistroHistoriaDTO> registros = new ArrayList<>();
        for (RegistroHistoria registro : historia.registros()) {
            registros.add(new RegistroHistoriaDTO(registro.codigo(), registro.fecha(), registro.detalle()));
        }
        InstantaneaHistoria instantanea = new InstantaneaHistoria(huella, registros, Instant.now());
        snapshots.put(idRecortado, instantanea);
        auditoria.registrar("HCE_SINCRONIZAR", "hce", "OK_ACTUALIZADA", actorActual());
        return new HistoriaSincronizadaDTO(idRecortado, false, huella,
                instantanea.sincronizadaEn(), instantanea.registros());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EstadoSistemaDTO pingRis() {
        return aDTO(ris.estado());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EstadoSistemaDTO pingPacs() {
        return aDTO(pacs.estado());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EstadoSistemaDTO pingSistema(String nombre) {
        return aDTO(estadoDeNombre(nombre));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<EstadoSistemaDTO> estadoSistemas() {
        List<EstadoSistemaDTO> estados = new ArrayList<>();
        for (SistemaExterno sistema : SistemaExterno.values()) {
            estados.add(aDTO(estadoDe(sistema)));
        }
        return estados;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AuditoriaIntegracionDTO estadoAuditoriaIntegracion() {
        List<EslabonAuditoriaDTO> eslabones = new ArrayList<>();
        for (EslabonAuditoria eslabon : auditoria.listar()) {
            eslabones.add(new EslabonAuditoriaDTO(eslabon.secuencia(), eslabon.accion(),
                    eslabon.sistema(), eslabon.resultado(), eslabon.actor(), eslabon.instante(),
                    eslabon.hashAnterior(), eslabon.hash()));
        }
        return new AuditoriaIntegracionDTO(auditoria.cadenaIntegra(), eslabones.size(),
                auditoria.ultimoHash(), eslabones);
    }

    // ------------------------------------------------------------------
    // Estados compartidos y actor
    // ------------------------------------------------------------------

    private EstadoSistema estadoDeNombre(String nombre) {
        SistemaExterno sistema = SistemaExterno.desdeCodigo(nombre)
                .orElseThrow(() -> new SistemaNoEncontradoException(
                        "el sistema " + nombre + " no es un sistema externo conocido"));
        return estadoDe(sistema);
    }

    private EstadoSistema estadoDe(SistemaExterno sistema) {
        return switch (sistema) {
            case SSO -> sso.estado();
            case HCE -> hce.estado();
            case RIS -> ris.estado();
            case PACS -> pacs.estado();
        };
    }

    private static EstadoSistemaDTO aDTO(EstadoSistema estado) {
        return new EstadoSistemaDTO(estado.sistema().codigo(), estado.disponible(),
                estado.motivo(), estado.latenciaMediaMs());
    }

    private static String actorActual() {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        return autenticacion == null ? USUARIO_SISTEMA : autenticacion.getName();
    }
}
