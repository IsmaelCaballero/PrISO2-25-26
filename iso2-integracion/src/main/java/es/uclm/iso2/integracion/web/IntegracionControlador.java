package es.uclm.iso2.integracion.web;

import es.uclm.iso2.common.integracion.InteropFacade;
import es.uclm.iso2.common.integracion.InteropFacade.AuditoriaIntegracionDTO;
import es.uclm.iso2.common.integracion.InteropFacade.CredencialesSsoDTO;
import es.uclm.iso2.common.integracion.InteropFacade.EstadoSistemaDTO;
import es.uclm.iso2.common.integracion.InteropFacade.EstudioDTO;
import es.uclm.iso2.common.integracion.InteropFacade.HistoriaSincronizadaDTO;
import es.uclm.iso2.common.integracion.InteropFacade.SesionSsoDTO;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API REST del spike CDU19 sobre la fachada {@link InteropFacade}: login SSO contra el
 * IdP simulado, sincronizacion idempotente de la historia externa, ping de sistemas,
 * importacion del baseline y auditoria encadenada. El controlador solo conoce el
 * contrato comun (regla ArchUnit): nunca puertos, adaptadores ni dominio.
 *
 * @since 1.1.0 (CFG-06, CDU19)
 */
@RestController
@RequestMapping("/api/integracion")
public class IntegracionControlador {

    private final InteropFacade fachada;

    /**
     * Crea el controlador sobre la fachada de integracion.
     *
     * @param fachada implementacion del contrato comun
     */
    public IntegracionControlador(InteropFacade fachada) {
        this.fachada = fachada;
    }

    /**
     * Login contra el IdP externo simulado. La sesion devuelta es informativa: la API
     * de ISO2 sigue autenticandose con su JWT de CDU17 (hallazgo del spike).
     *
     * @param credenciales usuario y contrasena del IdP
     * @return 200 con la sesion SSO
     */
    @PostMapping("/sso/login")
    public ResponseEntity<SesionSsoDTO> loginSso(@RequestBody CredencialesSsoDTO credenciales) {
        return ResponseEntity.ok(fachada.autenticarSso(credenciales));
    }

    /**
     * Estado de los cuatro sistemas externos monitorizados.
     *
     * @return 200 con los estados en el orden SSO, HCE, RIS, PACS
     */
    @GetMapping("/sistemas")
    public ResponseEntity<List<EstadoSistemaDTO>> sistemas() {
        return ResponseEntity.ok(fachada.estadoSistemas());
    }

    /**
     * Ping degradado de un sistema: un sistema caido se responde 200 con
     * {@code disponible=false}, jamas un 5xx.
     *
     * @param sistema codigo del sistema (sso, hce, ris o pacs)
     * @return 200 con el estado, o 404 si el nombre no es un sistema conocido
     */
    @GetMapping("/sistemas/{sistema}/ping")
    public ResponseEntity<EstadoSistemaDTO> ping(@PathVariable String sistema) {
        return ResponseEntity.ok(fachada.pingSistema(sistema));
    }

    /**
     * Sincroniza la historia externa de un paciente. Idempotente: si el contenido no
     * cambio desde la ultima sincronizacion, {@code reutilizado=true}.
     *
     * @param pacienteExternoId identificador del paciente en la HCE externa
     * @return 200 con la historia sincronizada y su huella
     */
    @PostMapping("/historias/{pacienteExternoId}/sincronizar")
    public ResponseEntity<HistoriaSincronizadaDTO> sincronizar(
            @PathVariable String pacienteExternoId) {
        return ResponseEntity.ok(fachada.sincronizarHistoriaClinica(pacienteExternoId));
    }

    /**
     * Importa la cabecera de un estudio del RIS (baseline 1.0.0 de CFG-00).
     *
     * @param accessionNumber numero de acceso del estudio
     * @return 200 con la cabecera importada
     */
    @GetMapping("/estudios/{accessionNumber}")
    public ResponseEntity<EstudioDTO> estudio(@PathVariable String accessionNumber) {
        return ResponseEntity.ok(fachada.importarEstudio(accessionNumber));
    }

    /**
     * Cadena de auditoria del subdominio con el hash encadenado y su verificacion.
     *
     * @return 200 con la cadena completa y la marca de integridad
     */
    @GetMapping("/auditoria")
    public ResponseEntity<AuditoriaIntegracionDTO> auditoria() {
        return ResponseEntity.ok(fachada.estadoAuditoriaIntegracion());
    }
}
