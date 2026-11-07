/**
 * Modulo {@code iso2-comunicacion}: notificaciones al paciente.
 *
 * <p>En Iter0 (CFG-00) el modulo queda como <strong>baseline contractual 1.0.0</strong>:
 * solo define el paquete y su Javadoc. La implementacion y los contratos de fachada
 * se anaden con el CDU asociado, subiendo la version MINOR en cada CDU (SemVer + D22/D23).</p>
 *
 * <p>Mapa de incrementos (autoritativo en docs/planificacion-iteraciones.csv):
 * CDU02 (Iter10) -> 1.1.0; CDU04 (Iter18) -> 1.2.0.</p>
 *
 * <p>Los DTO e interfaces compartidos con el cliente viven en {@code es.uclm.iso2.common}.
 * Este modulo no debe exponer clases de framework (Spring/JavaFX): el CI lo verifica
 * con ArchUnit en {@code iso2-common}.</p>
 */
package es.uclm.iso2.comunicacion;