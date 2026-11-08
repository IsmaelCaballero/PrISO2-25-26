package es.uclm.iso2.seguridad.servicio;

import es.uclm.iso2.common.seguridad.AutenticacionFacade.SesionDTO;
import es.uclm.iso2.seguridad.dominio.Rol;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests unitarios de la emision/verificacion JWT (CDU17). Cubren el gate CFG-02:
 * JwtUtils >= 90 % de linea. Ronda: ida y vuelta, firma ajena, manipulacion, caducidad
 * y fail-fast del secreto (CFG-02: secretos fuera del repo).
 */
class JwtUtilsTest {

    private static final String SECRETO = "secreto-de-pruebas-para-jwt-de-mas-de-32-bytes-seguro";
    private static final String OTRA_CLAVE = "otra-clave-de-prueba-tambien-muy-larga-de-32-b";

    private final JwtUtils jwt = new JwtUtils(SECRETO, 30);

    /**
     * Emitir y verificar la misma sesion: usuario, rol y caducidad sobreviven al viaje.
     */
    @Test
    void idaYVueltaConservaUsuarioRolYCanucidad() {
        SesionDTO emitida = jwt.generar("medico", Rol.MEDICO);
        assertNotNull(emitida.token());
        assertEquals("MEDICO", emitida.rol());
        assertTrue(emitida.caducidad().isAfter(Instant.now()), "la caducidad debe ser futura");

        Optional<SesionDTO> leida = jwt.leerSesion(emitida.token());
        assertTrue(leida.isPresent(), "un token propio valido debe verificarse");
        assertEquals("medico", leida.get().usuario());
        assertEquals("MEDICO", leida.get().rol());
        assertEquals(emitida.token(), leida.get().token());
    }

    /**
     * Un token bien firmado pero con un rol desconocido no es una sesion valida
     * (cierre por defecto ante claims corruptos).
     */
    @Test
    void rolDesconocidoEnClaimInvalidaSesion() {
        String forjado = Jwts.builder()
                .subject("medico")
                .claim(JwtUtils.CLAIM_ROL, "PIRATA")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor(SECRETO.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS256)
                .compact();
        assertTrue(jwt.leerSesion(forjado).isEmpty(), "el claim de rol debe ser un rol conocido");
    }

    /**
     * Tokens firmados con otra clave, alterados o directamente invalidos: todos vacio.
     */
    @Test
    void firmasInvalidasDevuelvenVacio() {
        String ajeno = new JwtUtils(OTRA_CLAVE, 30).generar("medico", Rol.MEDICO).token();
        assertTrue(jwt.leerSesion(ajeno).isEmpty(), "la firma debe verificarse con la clave propia");

        String propio = jwt.generar("medico", Rol.MEDICO).token();
        assertTrue(jwt.leerSesion(propio + "x").isEmpty(), "un token manipulado debe rechazarse");
        assertTrue(jwt.leerSesion("esto-no-es-un-jwt").isEmpty());
    }

    /**
     * La caducidad se comprueba siempre: un token nacido expirado no autentica.
     */
    @Test
    void tokenCaducadoDevuelveVacio() {
        String caducado = new JwtUtils(SECRETO, -5).generar("medico", Rol.MEDICO).token();
        assertTrue(jwt.leerSesion(caducado).isEmpty(), "el claim exp debe respetarse");
    }

    /**
     * Entradas degeneradas (nulo/vacio) no lanzan: devuelven vacio.
     */
    @Test
    void nullOVacioDevuelvenVacio() {
        assertTrue(jwt.leerSesion(null).isEmpty());
        assertTrue(jwt.leerSesion("   ").isEmpty());
    }

    /**
     * Fail-fast del secreto (CFG-02): sin secreto o con uno de menos de 32 bytes, la
     * aplicacion no arranca. Es la prueba del gate "secretos fuera del repo": si alguien
     * pusiera un secreto debil por defecto, estas aserciones lo impedirian.
     */
    @Test
    void secretoAusenteODebilHaceFallaElArranque() {
        assertThrows(IllegalStateException.class, () -> new JwtUtils("", 30));
        assertThrows(IllegalStateException.class, () -> new JwtUtils(null, 30));
        assertThrows(IllegalStateException.class, () -> new JwtUtils("corto", 30));
        assertEquals(32, JwtUtils.MINIMO_BYTES_CLAVE);
    }

    /**
     * El claim del rol es el contrato estable "rol" (lo consumen filtro y cliente).
     */
    @Test
    void elClaimDelRolEsEstable() {
        assertEquals("rol", JwtUtils.CLAIM_ROL);
        assertEquals(Duration.ofMinutes(30).getSeconds(),
                jwt.generar("medico", Rol.MEDICO).caducidad().getEpochSecond()
                        - Instant.now().getEpochSecond(), 5);
    }
}
