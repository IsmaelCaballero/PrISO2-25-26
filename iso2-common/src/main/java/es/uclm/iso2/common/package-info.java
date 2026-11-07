/**
 * Contratos compartidos entre el servidor y el cliente JavaFX (baseline 1.0.0, CFG-00).
 *
 * <p>Son el <strong>pegamento contractual</strong> del sistema: DTOs (records) y fachadas
 * de interfaz por subdominio. No contienen implementacion ni dependencias de framework
 * (Spring, JavaFX, JPA): un test de ArchUnit en este modulo lo garantiza en el CI.</p>
 *
 * <p><strong>Politica de version (D22 + ADR de versionado de common):</strong>
 * 1.0.0 es la baseline de Iter0. Cada CDU que anada contratos nuevos sube common a
 * 1.1.0-SNAPSHOT en las ramas feature, 1.1.0 en CFG-06/ALFA (R1), 1.2.0 en CFG-12 (R2),
 * 1.3.0 en CFG-18/BETA (R3), 1.4.0 en CFG-20 (RC) y 1.5.0 en CFG-21 (R5).
 * Solo rupturas reales obligarian a subir el mayor.</p>
 */
package es.uclm.iso2.common;