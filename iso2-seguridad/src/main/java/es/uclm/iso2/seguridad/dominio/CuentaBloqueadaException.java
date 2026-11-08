package es.uclm.iso2.seguridad.dominio;

import java.time.Instant;

/**
 * La cuenta esta bloqueada temporalmente por exceso de intentos fallidos (CDU17,
 * mitigacion de fuerza bruta RF17). El transporte la traduce a 401 con codigo
 * {@code CUENTA_BLOQUEADA} indicando cuando se levantara el bloqueo.
 *
 * @since 1.1.0 (CFG-02)
 */
public class CuentaBloqueadaException extends RuntimeException {

    private final Instant hasta;

    /**
     * Bloqueo en curso.
     *
     * @param hasta momento en que se levantara el bloqueo
     */
    public CuentaBloqueadaException(Instant hasta) {
        super("La cuenta esta bloqueada hasta " + hasta);
        this.hasta = hasta;
    }

    /**
     * @return momento de desbloqueo
     */
    public Instant getHasta() {
        return hasta;
    }
}
