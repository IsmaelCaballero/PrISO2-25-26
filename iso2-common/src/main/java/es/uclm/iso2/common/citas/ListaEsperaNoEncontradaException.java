package es.uclm.iso2.common.citas;

/**
 * Excepcion de contrato: la entrada de la lista de espera consultada no existe.
 * La aplicacion servidora la traduce a HTTP 404 con el codigo estable
 * {@code LISTA_ESPERA_NO_ENCONTRADA} (CDU14, CFG-07).
 *
 * @since 1.2.0 (CFG-07, CDU14)
 */
public class ListaEsperaNoEncontradaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Crea la excepcion con mensaje legible.
     *
     * @param mensaje detalle para el usuario final
     */
    public ListaEsperaNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}
