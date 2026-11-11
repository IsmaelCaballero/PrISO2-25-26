package es.uclm.iso2.server.persistencia;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Patron Agente (D34): representa el SGBD concreto sobre el que esta corriendo la
 * aplicacion sin que el resto del codigo pregunte por perfiles ni por URLs de conexion.
 *
 * <p>Un {@code Agente} sabe tres cosas de su base: como se llama ({@link #nombre()}),
 * como prestar una conexion JDBC viva ({@link #conexion()}) y cual es el esquema actual
 * ({@link #esquemaActual()}). Con esas tres cosas, los DAOs que necesiten SQL nativo o
 * vistas de sistema ({@code INFORMATION_SCHEMA}) pueden escribir consultas validas
 * tanto en H2 como en MySQL.</p>
 *
 * <p>La instancia la crea {@link ConfigPersistencia} a partir del producto que declara
 * el conductor JDBC, nunca a mano desde el codigo de negocio.</p>
 */
public interface Agente {

    /**
     * Nombre comercial del SGBD, para diagnostico y mensajes ({@code "H2"},
     * {@code "MySQL"}...).
     *
     * @return nombre legible del SGBD, jamas nulo ni vacio
     */
    String nombre();

    /**
     * Presta una conexion JDBC del conjunto de conexiones de la aplicacion.
     *
     * <p>La conexion pedida debe cerrarse por quien la usa (try-with-resources); si
     * vive dentro de una transaccion de Spring, cerrar devuelve la conexion al pool
     * sin romperla.</p>
     *
     * @return conexion lista para usar
     * @throws SQLException si el SGBD no puede dar conexion
     */
    Connection conexion() throws SQLException;

    /**
     * Esquema (base de datos) actual del SGBD: {@code PUBLIC} en H2 en memoria,
     * {@code iso2} en MySQL.
     *
     * @return nombre del esquema segun lo declara el propio SGBD
     * @throws SQLException si el SGBD no responde a la consulta de esquema
     */
    String esquemaActual() throws SQLException;
}
