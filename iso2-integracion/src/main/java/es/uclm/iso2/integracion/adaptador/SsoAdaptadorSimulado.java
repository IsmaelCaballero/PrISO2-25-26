package es.uclm.iso2.integracion.adaptador;

import es.uclm.iso2.common.integracion.CredencialesInvalidasException;
import es.uclm.iso2.integracion.dominio.RegistroSimulacion;
import es.uclm.iso2.integracion.dominio.SesionSso;
import es.uclm.iso2.integracion.dominio.SistemaExterno;
import es.uclm.iso2.integracion.puerto.SsoPort;
import java.time.Instant;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Adaptador simulado del IdP externo (SSO). No hay IdP real en develop, asi que el
 * spike fija un pequeno empadronamiento: {@code idp-admin} con contrasena
 * {@code idp-pass} autentica, cualquier otro usuario la rechaza con 401.
 *
 * <p>Precedencia de fallos: el modo CAIDO de la politica se comprueba en
 * {@link AdaptadorSimulado#proteger} antes de mirar la credencial, de modo que un IdP
 * caido responde 503 aunque las credenciales sean invalidas (503 &gt; 401).</p>
 *
 * @since 1.1.0 (CFG-06, CDU19)
 */
@Component
public class SsoAdaptadorSimulado extends AdaptadorSimulado implements SsoPort {

    /** Empadronamiento simulado del IdP: usuario -> contrasena. */
    private static final Map<String, String> USUARIOS = Map.of("idp-admin", "idp-pass");

    /** Rol que el IdP asigna a cualquier sujeto autentificado en el spike. */
    private static final String ROL_EXTERNO = "radiologia";

    /**
     * Registra el adaptador del IdP sobre el sistema SSO.
     *
     * @param registro registro compartido de politicas de simulacion
     */
    public SsoAdaptadorSimulado(RegistroSimulacion registro) {
        super(SistemaExterno.SSO, registro);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SesionSso autenticar(String usuario, String contrasena) {
        return proteger(() -> {
            String id = recortar(usuario);
            if (id.isEmpty() || contrasena == null || contrasena.isBlank()) {
                throw new IllegalArgumentException("usuario y contrasena son obligatorios");
            }
            String esperada = USUARIOS.get(id);
            if (esperada == null || !esperada.equals(contrasena)) {
                throw new CredencialesInvalidasException("el IdP ha rechazado las credenciales");
            }
            return new SesionSso(id, ROL_EXTERNO, Instant.now().plusSeconds(3600));
        });
    }
}
