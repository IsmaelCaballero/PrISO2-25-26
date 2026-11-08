/**
 * Capa de servicio de la seguridad: implementacion de {@code AutenticacionFacade}
 * (login BCrypt con bloqueo) y emision/verificacion de JWT. El secreto solo entra por
 * configuracion externa ({@code ISO2_JWT_SECRET}); nunca se hardcodea.
 *
 * @since 1.1.0 (CFG-02)
 */
package es.uclm.iso2.seguridad.servicio;
