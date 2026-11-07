package es.uclm.iso2.common.catalogo;

/**
 * Excepcion de contrato: la operacion de catalogo se dirige a un codigo de prueba que no
 * existe en el catalogo. La aplicacion servidora la traduce a HTTP 404.
 *
 * @since 1.1.0 (CFG-01, CDU16)
 */
public class CatalogoNoEncontradoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Crea el error indicando el codigo buscado.
     *
     * @param codigo codigo de prueba no encontrado
     */
    public CatalogoNoEncontradoException(String codigo) {
        super("prueba de catalogo no encontrada: " + codigo);
    }

    /**
     * Crea el error con mensaje personalizado.
     *
     * @param mensaje detalle del error
     */
    public CatalogoNoEncontradoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
