package es.uclm.iso2.common.catalogo;

/**
 * Excepcion de contrato: la operacion de catalogo choca con un codigo de prueba ya existente
 * o con un estado que impide la operacion (ej. reutilizar un codigo dado de baja).
 * La aplicacion servidora la traduce a HTTP 409; el cliente la muestra como aviso de conflicto.
 *
 * @since 1.1.0 (CFG-01, CDU16)
 */
public class CatalogoConflictoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Crea el conflicto con mensaje legible.
     *
     * @param mensaje detalle del conflicto
     */
    public CatalogoConflictoException(String mensaje) {
        super(mensaje);
    }

    /**
     * Crea el conflicto encadenando la causa original.
     *
     * @param mensaje detalle del conflicto
     * @param causa   excepcion original
     */
    public CatalogoConflictoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
