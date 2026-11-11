package es.uclm.iso2.common.citas;

/**
 * Excepcion de contrato: la operacion sobre una cita choca con una regla del CDU12.
 * La aplicacion servidora la traduce a HTTP 409 con un codigo estable segun el tipo:
 * {@code CONFLICTO_HUECO_OCUPADO}, {@code CONFLICTO_FUERA_DE_FRANJA},
 * {@code CONFLICTO_TRANSICION_INVALIDA}, {@code CONFLICTO_SOLICITUD_NO_APTA} o
 * {@code CONFLICTO_PLAZO_CANCELACION}.
 *
 * <p>El tipo {@link Conflicto#HUECO_OCUPADO} es la senala de la concurrencia: dos
 * usuarios que reservan el mismo hueco, solo uno lo consigue (RF12, CFG-05).</p>
 *
 * @since 1.1.0 (CFG-05, CDU12)
 */
public class CitaConflictoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** Tipos de conflicto al gestionar una cita. */
    public enum Conflicto {
        /** El hueco ya esta bloqueado por otra cita activa: solo un hilo gana. */
        HUECO_OCUPADO,
        /** La cita cae fuera de una franja de disponibilidad o en dia festivo (CDU13). */
        FUERA_DE_FRANJA,
        /** La transicion de la maquina de estados de la cita no es valida. */
        TRANSICION_INVALIDA,
        /** La solicitud de origen no admite cita (no existe, no esta en creada o ya esta citada). */
        SOLICITUD_NO_APTA,
        /** La cancelacion llega fuera del plazo minimo de preaviso del catalogo. */
        PLAZO_CANCELACION
    }

    private final Conflicto tipo;

    /**
     * Crea el conflicto con su tipo y mensaje legible.
     *
     * @param tipo    clase de conflicto
     * @param mensaje detalle para el usuario final
     */
    public CitaConflictoException(Conflicto tipo, String mensaje) {
        super(mensaje);
        this.tipo = tipo;
    }

    /**
     * Tipo de conflicto detectado.
     *
     * @return el tipo de conflicto
     */
    public Conflicto tipo() {
        return tipo;
    }
}
