package es.uclm.iso2.common.integracion;

/**
 * Excepcion de contrato: el nombre recibido no corresponde a ninguno de los
 * sistemas externos conocidos ({@code sso}, {@code hce}, {@code ris}, {@code pacs}).
 * La aplicacion servidora la traduce a HTTP 404 con el codigo estable
 * {@code SISTEMA_NO_ENCONTRADO}.
 *
 * @since 1.1.0 (CFG-06, CDU19)
 */
public class SistemaNoEncontradoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Crea la excepcion con mensaje legible.
     *
     * @param mensaje detalle para el usuario final
     */
    public SistemaNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
