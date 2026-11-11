package es.uclm.iso2.server.persistencia;

/**
 * Recuento de filas de una tabla del esquema (DTO inmutable del diagnostico D34).
 *
 * @param tabla nombre de la tabla segun el catalogo del SGBD
 * @param registros numero de filas de esa tabla en este momento
 */
public record ConteoTabla(String tabla, long registros) {
}
