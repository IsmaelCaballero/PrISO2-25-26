/**
 * Servicio del CDU19: implementa {@code InteropFacade} sobre los puertos de salida,
 * con idempotencia de sincronizacion por huella SHA-256 y ping degradado. Depende de
 * dominio, contrato comun y puertos; jamas de adaptadores ni de web (ArchUnit).
 *
 * @since 1.1.0 (CFG-06, CDU19)
 */
package es.uclm.iso2.integracion.servicio;
