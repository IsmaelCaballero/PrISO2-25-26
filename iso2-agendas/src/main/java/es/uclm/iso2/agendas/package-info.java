/**
 * Modulo {@code iso2-agendas}: agendas de medicos y tecnicos (CDU13).
 *
 * <p>Desde Iter3 (CFG-03) el modulo esta implementado en su version <strong>1.1.0</strong>:
 * dominio Agenda/Franja/Festivo con auditoria de hash encadenado, servicio que implementa
 * {@code AgendaFacade} con las reglas del CDU (sin solapes entre franjas; los festivos
 * mandan sobre las franjas) y API REST {@code /api/agendas}. Las capas siguen la frontera
 * dominio &lt;- repositorio &lt;- servicio &lt;- web verificada por ArchUnit.</p>
 *
 * <p>Mapa de incrementos (autoritativo en docs/planificacion-iteraciones.csv):
 * CDU13 (Iter3) -> 1.1.0; CDU08 (Iter12) -> 1.2.0.</p>
 *
 * <p>Los DTO e interfaces compartidos con el cliente viven en {@code es.uclm.iso2.common}.
 * Este modulo no debe exponer clases de framework (Spring/JavaFX): el CI lo verifica
 * con ArchUnit en {@code iso2-common}.</p>
 */
package es.uclm.iso2.agendas;
