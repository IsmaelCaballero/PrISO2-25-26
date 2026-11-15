package es.uclm.iso2.common.integracion;

/**
 * Excepcion de contrato: el IdP externo rechaza las credenciales presentadas.
 * La aplicacion servidora la traduce a HTTP 401 con el codigo estable
 * {@code CREDENCIALES_INVALIDAS}.
 *
 * <p>Precendencia fijada por el spike CDU19: si ademas el adaptador del IdP esta
 * en modo caido, gana {@link SistemaExternoException} (503) sobre esta 401: la
 * credencial ni se mira.</p>
 *
 * @since 1.1.0 (CFG-06, CDU19)
 */
public class CredencialesInvalidasException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Crea la excepcion con mensaje legible.
     *
     * @param mensaje detalle para el usuario final
     */
    public CredencialesInvalidasException(String mensaje) {
        super(mensaje);
    }
}
