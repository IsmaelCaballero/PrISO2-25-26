package es.uclm.iso2.common.agendas;

/**
 * Excepcion de contrato: la operacion de agenda choca con una regla del CDU13. La
 * aplicacion servidora la traduce a HTTP 409 con un codigo estable segun el tipo:
 * {@code CONFLICTO_SOLAPE}, {@code CONFLICTO_FESTIVO} o {@code CONFLICTO_AGENDA_EXISTENTE}.
 *
 * @since 1.1.0 (CFG-03, CDU13)
 */
public class AgendaConflictoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** Tipos de conflicto entre franjas y festivos de una agenda. */
    public enum Conflicto {
        /** La franja se solapa con otra franja del mismo profesional. */
        SOLAPE,
        /** La franja cae en un dia festivo, o el festivo choca con franjas del dia. */
        FESTIVO,
        /** El profesional ya tenia agenda dada de alta. */
        AGENDA_DUPLICADA
    }

    private final Conflicto tipo;

    /**
     * Crea el conflicto con su tipo y mensaje legible.
     *
     * @param tipo    clase de conflicto (determina el codigo HTTP 409 enviado)
     * @param mensaje detalle del conflicto
     */
    public AgendaConflictoException(Conflicto tipo, String mensaje) {
        super(mensaje);
        this.tipo = tipo;
    }

    /**
     * Tipo de conflicto detectado.
     *
     * @return el tipo
     */
    public Conflicto tipo() {
        return tipo;
    }
}
