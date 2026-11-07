/**
 * Modulo {@code iso2-catalogo}: catalogo clinico y administrativo de pruebas de imagen (CDU16).
 *
 * <p>Desde Iter1 (CFG-01) el modulo esta implementado en <strong>version 1.1.0</strong>
 * con capas claras:</p>
 * <ul>
 *   <li>{@code dominio}: entidades JPA ({@code TipoPrueba} con embebidos {@code Protocolo} y
 *       {@code Politica}, y {@code AuditoriaCatalogo} con hash encadenado).</li>
 *   <li>{@code repositorio}: repositorios Spring Data JPA.</li>
 *   <li>{@code servicio}: {@code CatalogoServicio}, implementacion de la fachada de contrato
 *       con auditoria SHA-256 encadenada.</li>
 *   <li>{@code web}: {@code CatalogoControlador} (REST {@code /api/catalogo/pruebas}) y su
 *       manejador de errores.</li>
 * </ul>
 *
 * <p>Aqui si se permiten dependencias de framework (Spring Data/JPA/MVC); la restriccion de
 * agnosticidad aplica solo a {@code iso2-common}. El modulo se ensambla en {@code iso2-server-app}.</p>
 *
 * <p>Mapa de incrementos (autoritativo en docs/planificacion-iteraciones.csv):
 * CDU16 (Iter1) -> 1.1.0.</p>
 *
 * <p>Los DTO e interfaces compartidos con el cliente viven en {@code es.uclm.iso2.common}.</p>
 */
package es.uclm.iso2.catalogo;
