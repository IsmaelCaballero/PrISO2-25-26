package es.uclm.iso2.integracion.puerto;

import es.uclm.iso2.integracion.dominio.EstadoSistema;
import es.uclm.iso2.integracion.dominio.SesionSso;

/**
 * Salida hacia el proveedor de identidad externo (SSO). El servicio solo conoce esta
 * interfaz: el adaptador simulado se sustituye por uno real en la version definitiva
 * sin tocar el servicio (hallazgo del spike CDU19).
 *
 * @since 1.1.0 (CFG-06, CDU19)
 */
public interface SsoPort {

    /**
     * Autentica contra el IdP externo.
     *
     * @param usuario    usuario en el IdP
     * @param contrasena contrasena aceptada por el IdP
     * @return sesion emitida por el IdP
     */
    SesionSso autenticar(String usuario, String contrasena);

    /**
     * Estado del IdP para monitorizacion (ping degradado, nunca lanza fallo).
     *
     * @return estado actual del sistema SSO
     */
    EstadoSistema estado();
}
