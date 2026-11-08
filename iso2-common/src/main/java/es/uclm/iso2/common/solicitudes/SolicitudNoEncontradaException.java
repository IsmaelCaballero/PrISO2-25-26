package es.uclm.iso2.common.solicitudes;

/**
 * Excepcion de contrato: la solicitud consultada no existe. La aplicacion servidora
 * la traduce a HTTP 404 con el codigo estable {@code SOLICITUD_NO_ENCONTRADA}.
 *
 * @since 1.1.0 (CFG-04, CDU06)
 */
public class SolicitudNoEncontradaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Crea la excepcion con mensaje legible.
     *
     * @param mensaje detalle para el usuario final
     */
    public SolicitudNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}
