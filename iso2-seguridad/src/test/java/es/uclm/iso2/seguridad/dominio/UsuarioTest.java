package es.uclm.iso2.seguridad.dominio;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests unitarios de las reglas del usuario persistido (CDU17): bloqueo por intentos,
 * reinicio, baja logica y acceso al rol como enum.
 */
class UsuarioTest {

    private static Usuario medico() {
        return new Usuario("medico", "$2a$10$hash", Rol.MEDICO, true);
    }

    /**
     * Un usuario de alta nace sin intentos ni bloqueos y con su rol.
     */
    @Test
    void altaNaceLimpia() {
        Usuario u = medico();
        assertEquals(0, u.getIntentosFallidos());
        assertFalse(u.estaBloqueado());
        assertNull(u.getBloqueadoHasta());
        assertEquals(Rol.MEDICO, u.rolEnum().orElseThrow());
        assertEquals("MEDICO", u.getRol());
        assertTrue(u.isActivo());
    }

    /**
     * Antes del maximo solo cuenta; al alcanzarlo bloquea con fecha de desbloqueo.
     */
    @Test
    void elQuintoIntentoBloqueaQuinceMinutos() {
        Usuario u = medico();
        for (int i = 1; i < 5; i++) {
            u.registrarIntentoFallido(5, Duration.ofMinutes(15));
            assertFalse(u.estaBloqueado(), "con " + i + " intentos no debe estar bloqueado");
        }
        Instant antes = Instant.now();
        u.registrarIntentoFallido(5, Duration.ofMinutes(15));
        assertEquals(5, u.getIntentosFallidos());
        assertTrue(u.estaBloqueado());
        assertNotNull(u.getBloqueadoHasta());
        assertTrue(u.getBloqueadoHasta().isAfter(antes.plus(Duration.ofMinutes(14))),
                "el desbloqueo debe quedar a ~15 minutos");
    }

    /**
     * Un bloqueo ya expirado no bloquea (y el contador se puede reiniciar).
     */
    @Test
    void reinicioDejaLibreDeBloqueos() {
        Usuario u = medico();
        for (int i = 0; i < 5; i++) {
            u.registrarIntentoFallido(5, Duration.ofMillis(-1)); // bloqueo ya caducado
        }
        assertFalse(u.estaBloqueado(), "un bloqueo pasado no bloquea");
        u.reiniciarIntentos();
        assertEquals(0, u.getIntentosFallidos());
        assertNull(u.getBloqueadoHasta());
    }
}
