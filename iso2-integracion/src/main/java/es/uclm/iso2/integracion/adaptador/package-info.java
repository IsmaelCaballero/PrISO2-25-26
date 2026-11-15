/**
 * Adaptadores simulados de los puertos de salida del CDU19. Simulan IdP, HCE, RIS y
 * PACS con politicas de fallo mutables desde la API; no hay sistemas reales en develop
 * (spike). Regla ArchUnit: nunca dependen de servicio ni web.
 *
 * @since 1.1.0 (CFG-06, CDU19)
 */
package es.uclm.iso2.integracion.adaptador;
