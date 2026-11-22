package es.uclm.iso2.common.citas;

/**
 * Excepcion de contrato: la operacion sobre una entrada de la lista de espera
 * choca con una regla del CDU14. La aplicacion servidora la traduce a HTTP 409
 * con un codigo estable segun el tipo: {@code CONFLICTO_ALTA_DUPLICADA},
 * {@code CONFLICTO_REUBICACION_REDUNDANTE} o {@code CONFLICTO_ENTRADA_NO_ACTIVA}.
 *
 * <p>Los nombres del enum van desnudos y el manejador de errores anade el
 * prefijo {@code CONFLICTO_}, el mismo canon que {@link CitaConflictoException}.</p>
 *
 * @since 1.2.0 (CFG-07, CDU14)
 */
public class ListaEsperaConflictoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** Tipos de conflicto al gestionar una entrada de la lista de espera. */
    public enum Conflicto {
        /** El paciente ya tiene una entrada activa para la misma prueba. */
        ALTA_DUPLICADA,
        /** La reubicacion pide la prioridad que la entrada ya tiene. */
        REUBICACION_REDUNDANTE,
        /** La entrada no esta activa: fue asignada o retirada. */
        ENTRADA_NO_ACTIVA
    }

    private final Conflicto codigo;

    /**
     * Crea el conflicto con su codigo y mensaje legible.
     *
     * @param codigo  clase de conflicto
     * @param mensaje detalle para el usuario final
     */
    public ListaEsperaConflictoException(Conflicto codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    /**
     * Codigo de conflicto detectado.
     *
     * @return el codigo de conflicto
     */
    public Conflicto codigo() {
        return codigo;
    }
}
