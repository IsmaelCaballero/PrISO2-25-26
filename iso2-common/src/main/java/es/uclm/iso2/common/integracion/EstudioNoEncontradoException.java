package es.uclm.iso2.common.integracion;

/**
 * Excepcion de contrato: el RIS no conoce ese numero de acceso. La aplicacion
 * servidora la traduce a HTTP 404 con el codigo estable
 * {@code ESTUDIO_NO_ENCONTRADO}.
 *
 * <p>No rompe el baseline: {@link InteropFacade#importarEstudio(String)} siempre
 * pudo fallar, el baseline 1.0.0 simplemente no fijo como. Es un anadido aditivo
 * (MINOR, D23).</p>
 *
 * @since 1.1.0 (CFG-06, CDU19)
 */
public class EstudioNoEncontradoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Crea la excepcion con mensaje legible.
     *
     * @param mensaje detalle para el usuario final
     */
    public EstudioNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
