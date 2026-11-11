/**
 * Capa de persistencia de bajo nivel del servidor (D34, Iter5/CFG-05, issue #34).
 *
 * <p>Los modulos de dominio ({@code iso2-catalogo}, {@code iso2-seguridad},
 * {@code iso2-agendas}, {@code iso2-solicitudes}) siguen usando JPA/Spring Data como
 * acceso primario a los datos: este paquete NO los sustituye, es una incorporacion
 * nueva que aporta dos cosas:</p>
 *
 * <ul>
 *   <li>El patron <em>Agente</em> ({@link es.uclm.iso2.server.persistencia.Agente}),
 *       que oculta al resto de la aplicacion que el SGBD sea H2 o MySQL. La eleccion
 *       la hace {@link es.uclm.iso2.server.persistencia.ConfigPersistencia}
 *       preguntando al conductor JDBC real ({@code DatabaseMetaData}), no mirando
 *       perfiles ni URLs: asi el bean nunca se desincroniza de la base auténtica.</li>
 *   <li>El primer DAO JDBC puro
 *       ({@link es.uclm.iso2.server.persistencia.DiagnosticoPersistenciaDao}), que
 *       consulta {@code INFORMATION_SCHEMA} para diagnosticar el esquema y el numero
 *       de registros por tabla. Sirve de patron replicable para futuros DAOs sobre
 *       vistas del sistema donde JPA no aplica.</li>
 * </ul>
 *
 * <p>La base de datos puede ser H2 (por defecto y en tests) o MySQL 8.4 Levss
 * (perfil {@code mysql} + {@code docker/}), con migraciones Flyway separadas por
 * SGBD en {@code db/migration/h2} y {@code db/migration/mysql} y usuarios distintos
 * para DDL ({@code iso2_flyway}) y DML ({@code gestoroperaciones}).</p>
 */
package es.uclm.iso2.server.persistencia;
