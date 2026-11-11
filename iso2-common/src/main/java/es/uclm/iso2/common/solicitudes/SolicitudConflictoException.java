package es.uclm.iso2.common.solicitudes;

/**
 * Excepcion de contrato: la prescripcion choca con una regla del CDU06. La
 * aplicacion servidora la traduce a HTTP 409 con un codigo estable segun el tipo:
 * {@code CONFLICTO_SOLICITUD_DUPLICADA}, {@code CONFLICTO_PRUEBA_NO_DISPONIBLE}
 * o {@code CONFLICTO_SOLICITUD_NO_APTA}.
 *
 * @since 1.1.0 (CFG-04, CDU06); anadido {@code NO_APTA_PARA_CITA} en 1.2.0 (CFG-05, CDU12)
 */
public class SolicitudConflictoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** Tipos de conflicto al prescribir una prueba. */
    public enum Conflicto {
        /** El medico ya tiene una solicitud activa del mismo paciente y prueba (RF06.3). */
        DUPLICADA,
        /** La prueba no existe en el catalogo (CDU16) o esta desactivada. */
        PRUEBA_NO_DISPONIBLE,
        /** La solicitud no admite cita o no se puede reabrir: no existe, no esta en
         * creada o ya fue citada (la usa el CDU12 al agendar, cancelar o expirar).
         * @since 1.2.0 (CFG-05, CDU12) */
        NO_APTA_PARA_CITA
    }

    private final Conflicto tipo;

    /**
     * Crea el conflicto con su tipo y mensaje legible.
     *
     * @param tipo    clase de conflicto
     * @param mensaje detalle para el usuario final
     */
    public SolicitudConflictoException(Conflicto tipo, String mensaje) {
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
