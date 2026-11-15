package es.uclm.iso2.common.integracion;

/**
 * Excepcion de contrato: el paciente consultado no tiene historia en el HCE
 * externo. La aplicacion servidora la traduce a HTTP 404 con el codigo estable
 * {@code HISTORIA_NO_ENCONTRADA}.
 *
 * @since 1.1.0 (CFG-06, CDU19)
 */
public class HistoriaNoEncontradaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Crea la excepcion con mensaje legible.
     *
     * @param mensaje detalle para el usuario final
     */
    public HistoriaNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}
