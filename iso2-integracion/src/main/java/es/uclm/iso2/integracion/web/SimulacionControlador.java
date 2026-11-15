package es.uclm.iso2.integracion.web;

import es.uclm.iso2.common.integracion.SistemaNoEncontradoException;
import es.uclm.iso2.integracion.dominio.AuditoriaIntegracion;
import es.uclm.iso2.integracion.dominio.ModoSimulacion;
import es.uclm.iso2.integracion.dominio.PoliticaSimulacion;
import es.uclm.iso2.integracion.dominio.RegistroSimulacion;
import es.uclm.iso2.integracion.dominio.SetupSimulacion;
import es.uclm.iso2.integracion.dominio.SistemaExterno;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Control de la simulacion de modos de fallo (CAIDO/LENTO/TIEMPO SUPERADO). Vive aparte
 * del resto de la API porque toca directamente el {@link RegistroSimulacion} del
 * dominio: es la pieza que permite ejecutar los TCs degradados del CSV TDOP sin
 * reiniciar el servidor. Audita cada cambio como accion SIMULACION con el token del
 * setup, de modo que la matriz quede trazable en la cadena.
 *
 * <p>Regla ArchUnit: web jamas depende de la capa adaptador; aqui solo se escribe la
 * politica y el adaptador la lee en su siguiente llamada.</p>
 *
 * @since 1.1.0 (CFG-06, CDU19)
 */
@RestController
@RequestMapping("/api/integracion/simulacion")
public class SimulacionControlador {

    /** Usuario auditado cuando no hay autenticacion. */
    private static final String USUARIO_SISTEMA = "USUARIO_SISTEMA";

    private final RegistroSimulacion registro;
    private final AuditoriaIntegracion auditoria;

    /**
     * Crea el controlador sobre el registro de politicas y la auditoria.
     *
     * @param registro  registro vivo de politicas de simulacion
     * @param auditoria cadena de auditoria en memoria del subdominio
     */
    public SimulacionControlador(RegistroSimulacion registro, AuditoriaIntegracion auditoria) {
        this.registro = registro;
        this.auditoria = auditoria;
    }

    /**
     * Peticion de cambio de politica: sistema en minusculas y los cuatro valores de la
     * politica. Los campos numericos son envoltorios para poder distinguir "no enviado"
     * de cero.
     *
     * @param sistema    codigo del sistema (sso, hce, ris o pacs)
     * @param modo       modo de simulacion (NORMAL, LENTO, LENTO_TIMEOUT o CAIDO)
     * @param latenciaMs latencia simulada en milisegundos
     * @param timeoutMs  tiempo de espera en milisegundos
     * @param reintentos reintentos tras el primer intento
     */
    public record CambiarSimulacionRequest(String sistema, String modo, Long latenciaMs,
                                           Long timeoutMs, Integer reintentos) {
    }

    /**
     * Politicas vigentes de los cuatro sistemas, en el orden SSO, HCE, RIS, PACS.
     *
     * @return 200 con el estado del registro de simulacion
     */
    @GetMapping
    public ResponseEntity<Map<SistemaExterno, PoliticaSimulacion>> estado() {
        return ResponseEntity.ok(registro.todas());
    }

    /**
     * Instala una politica nueva para un sistema. A partir de la siguiente llamada el
     * adaptador correspondiente se comporta como diga la politica.
     *
     * @param request politica a instalar
     * @return 200 con la politica ya vigente para ese sistema
     */
    @PostMapping
    public ResponseEntity<PoliticaSimulacion> cambiar(@RequestBody CambiarSimulacionRequest request) {
        if (request == null || request.sistema() == null || request.sistema().isBlank()) {
            throw new IllegalArgumentException("el sistema es obligatorio");
        }
        SistemaExterno sistema = SistemaExterno.desdeCodigo(request.sistema())
                .orElseThrow(() -> new SistemaNoEncontradoException(
                        "el sistema " + request.sistema() + " no es un sistema externo conocido"));
        if (request.modo() == null || request.modo().isBlank()) {
            throw new IllegalArgumentException("el modo de simulacion es obligatorio");
        }
        ModoSimulacion modo;
        try {
            modo = ModoSimulacion.valueOf(request.modo().trim().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("modo de simulacion desconocido: " + request.modo(), e);
        }
        if (request.latenciaMs() == null || request.timeoutMs() == null || request.reintentos() == null) {
            throw new IllegalArgumentException("latenciaMs, timeoutMs y reintentos son obligatorios");
        }
        SetupSimulacion setup = new SetupSimulacion(sistema, new PoliticaSimulacion(
                modo, request.latenciaMs(), request.timeoutMs(), request.reintentos()));
        registro.cambiar(sistema, setup.politica());
        auditoria.registrar("SIMULACION", sistema.codigo(), setup.toString(), actorActual());
        return ResponseEntity.ok(registro.de(sistema));
    }

    private static String actorActual() {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        return autenticacion == null ? USUARIO_SISTEMA : autenticacion.getName();
    }
}
