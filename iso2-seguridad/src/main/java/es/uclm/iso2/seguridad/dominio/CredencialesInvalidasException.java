package es.uclm.iso2.seguridad.dominio;

/**
 * Credenciales incorrectas o usuario inexistente/inactivo (CDU17). No distingue si el
 * usuario existe a proposito: revelar esa diferencia facilita ataques de enumeracion.
 *
 * @since 1.1.0 (CFG-02)
 */
public class CredencialesInvalidasException extends RuntimeException {

    /**
     * Error estandar de credenciales (mensaje unico e intencionadamente vago).
     */
    public CredencialesInvalidasException() {
        super("Usuario o contrasena incorrectos");
    }
}
