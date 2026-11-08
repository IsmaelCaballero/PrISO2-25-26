package es.uclm.iso2.common.agendas;

/**
 * Excepcion de contrato: la agenda pedida (o una de sus franjas/festivos) no existe.
 * La aplicacion servidora la traduce a HTTP 404 con codigo {@code AGENDA_NO_ENCONTRADA}.
 *
 * @since 1.1.0 (CFG-03, CDU13)
 */
public class AgendaNoEncontradoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Crea el error con mensaje legible.
     *
     * @param mensaje detalle del error
     */
    public AgendaNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
