/**
 * Modulo {@code iso2-solicitudes}: solicitudes de prueba.
 *
 * <p>En Iter0 (CFG-00) el modulo queda como <strong>baseline contractual 1.0.0</strong>:
 * solo define el paquete y su Javadoc. La implementacion y los contratos de fachada
 * se anaden con el CDU asociado, subiendo la version MINOR en cada CDU (SemVer + D22/D23).</p>
 *
 * <p>Mapa de incrementos (autoritativo en docs/planificacion-iteraciones.csv):
 * CDU06 (Iter4) -> 1.1.0; CDU07 (Iter11) -> 1.2.0; CDU08 (Iter12) -> 1.3.0; CDU05 (Iter14) -> 1.4.0.</p>
 *
 * <p>Los DTO e interfaces compartidos con el cliente viven en {@code es.uclm.iso2.common}.
 * Este modulo no debe exponer clases de framework (Spring/JavaFX): el CI lo verifica
 * con ArchUnit en {@code iso2-common}.</p>
 */
package es.uclm.iso2.solicitudes;