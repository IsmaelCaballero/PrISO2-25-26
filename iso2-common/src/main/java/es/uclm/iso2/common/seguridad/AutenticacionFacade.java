package es.uclm.iso2.common.seguridad;

import java.time.Instant;

/**
 * Fachada de contratos del subdominio {@code seguridad}: Autenticacion y permisos por rol (CDU17; SSO en CDU18).
 * Solo contrato: la implementacion vive en el modulo iso2-seguridad (baseline 1.0.0, D22).
 *
 * @since 1.0.0 (CFG-00)
 */
public interface AutenticacionFacade {

    /**
     * Verifica unas credenciales locales y devuelve la sesion con su token.
     *
     * @param credenciales usuario y contrasena
     * @return sesion iniciada con token de acceso
     */
    SesionDTO iniciarSesion(CredencialesDTO credenciales);

    /**
     * Comprueba si un rol tiene un permiso concreto (RBAC).
     *
     * @param rol           identificador del rol
     * @param codigoPermiso codigo del permiso a comprobar
     * @return true si el rol posee el permiso
     */
    boolean tienePermiso(String rol, String codigoPermiso);

    /**
     * Credenciales de acceso.
     *
     * @param usuario    nombre de usuario
     * @param password   contrasena en claro solo en el transporte; nunca se persiste
     */
    record CredencialesDTO(String usuario, String password) {
    }

    /**
     * Sesion iniciada para una persona con un unico rol.
     *
     * @param usuario      usuario autenticado
     * @param rol          rol unico activo en la sesion
     * @param token        token de acceso (JWT en el servidor)
     * @param caducidad momento de expiracion del token
     */
    record SesionDTO(String usuario, String rol, String token, Instant caducidad) {
    }
}
