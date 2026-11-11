package es.uclm.iso2.common.citas;

/**
 * Excepcion de contrato: la cita consultada no existe. La aplicacion servidora la
 * traduce a HTTP 404 con el codigo estable {@code CITA_NO_ENCONTRADA}.
 *
 * @since 1.1.0 (CFG-05, CDU12)
 */
public class CitaNoEncontradaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Crea la excepcion con mensaje legible.
     *
     * @param mensaje detalle para el usuario final
     */
    public CitaNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}
