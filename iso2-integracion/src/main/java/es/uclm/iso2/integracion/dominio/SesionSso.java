package es.uclm.iso2.integracion.dominio;

import java.time.Instant;

/**
 * Sesion que emite el IdP externo tras un login SSO correcto. Hallazgo del spike
 * CDU19: esta sesion NO sustituye al JWT de CDU17; la API de ISO2 sigue
 * autenticandose con su propio token.
 *
 * @param sujeto     identidad externa autenticada
 * @param rolExterno rol que el IdP asigna al sujeto
 * @param caducaEn   instante de caducidad de la sesion
 * @since 1.1.0 (CFG-06, CDU19)
 */
public record SesionSso(String sujeto, String rolExterno, Instant caducaEn) {
}
