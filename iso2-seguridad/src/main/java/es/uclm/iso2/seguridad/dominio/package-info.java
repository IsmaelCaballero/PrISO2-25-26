/**
 * Capa de dominio de la seguridad: usuario persistido (BCrypt + bloqueo por intentos),
 * roles, permisos y matriz RBAC autoritativa. No depende de las capas de servicio,
 * repositorio ni web (regla vigilada por ArchUnit en {@code ArquitecturaSeguridadTest}).
 *
 * @since 1.1.0 (CFG-02)
 */
package es.uclm.iso2.seguridad.dominio;
