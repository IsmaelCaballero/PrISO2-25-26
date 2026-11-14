/**
 * Modulo {@code iso2-citas}: agendamiento de citas sobre las agendas del CDU13 (CDU12).
 *
 * <p>En Iter0 (CFG-00) el modulo quedo como <strong>baseline contractual 1.0.0</strong>:
 * solo definia el paquete y su Javadoc. Desde la Iter5 (CFG-05) esta
 * <strong>IMPLEMENTADA a 1.1.0</strong>: motor de asignacion de huecos anti-concurrencia,
 * maquina de estados de cita y auditoria encadenada, con las vistas de recepcionista del
 * cliente.</p>
 *
 * <p>Mapa de incrementos (autoritativo en docs/planificacion-iteraciones.csv):
 * CDU12 (Iter5) -&gt; 1.1.0; CDU14 (Iter7) -&gt; 1.2.0; CDU01 (Iter9) -&gt; 1.3.0.</p>
 *
 * <p>Los DTO e interfaces compartidos con el cliente viven en {@code es.uclm.iso2.common}.
 * Las capas internas no se cruzan hacia arriba: lo verifica ArchUnit en
 * {@code ArquitecturaCitasTest}.</p>
 */
package es.uclm.iso2.citas;
