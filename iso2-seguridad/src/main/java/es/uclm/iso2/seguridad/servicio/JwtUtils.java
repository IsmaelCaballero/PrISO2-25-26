package es.uclm.iso2.seguridad.servicio;

import es.uclm.iso2.common.seguridad.AutenticacionFacade.SesionDTO;
import es.uclm.iso2.seguridad.dominio.Rol;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

/**
 * Emision y verificacion de tokens JWT (RFC 7519, CDU17, RF17). Algoritmo HS256 con
 * clave simetrica: la misma clave firma y verifica. La caducidad es obligatoria
 * ({@code exp}) y el rol viaja en el claim {@code rol} para que el servidor autorice
 * sin consultar la base de datos en cada peticion.
 *
 * <p><strong>Secreto fuera del repositorio (CFG-02):</strong> la clave se lee de la
 * propiedad {@code iso2.seguridad.jwt.secret}, que en produccion se alimenta solo de
 * la variable de entorno {@code ISO2_JWT_SECRET}. Si falta o es corta (&lt; 32 bytes,
 * minimo de HS256), la aplicacion falla al arrancar: mejor no levantar que arrancar
 * con un secreto debil o compartido.</p>
 *
 * @since 1.1.0 (CFG-02)
 */
@Component
public class JwtUtils {

    /** Claim que transporta el rol unico de la sesion. */
    public static final String CLAIM_ROL = "rol";

    /** Minimo de bytes exigido a la clave para HS256 (RFC 7518). */
    static final int MINIMO_BYTES_CLAVE = 32;

    private final SecretKey clave;
    private final Duration duracion;

    /**
     * Construye el emisor/verificador con el secreto y la caducidad configurados.
     *
     * @param secreto         clave HMAC (solo desde {@code ISO2_JWT_SECRET}; jamas en el repo)
     * @param duracionMinutos minutos de validez de los tokens emitidos
     * @throws IllegalStateException si el secreto esta vacío o no alcanza los 32 bytes
     *                               exigidos por HS256
     */
    public JwtUtils(@Value("${iso2.seguridad.jwt.secret:}") String secreto,
                    @Value("${iso2.seguridad.jwt.duracion-minutos:60}") long duracionMinutos) {
        if (secreto == null || secreto.isBlank()) {
            throw new IllegalStateException(
                    "Falta el secreto JWT: define la variable de entorno ISO2_JWT_SECRET "
                            + "(los secretos no viven en el repositorio, CFG-02)");
        }
        if (secreto.getBytes(StandardCharsets.UTF_8).length < MINIMO_BYTES_CLAVE) {
            throw new IllegalStateException(
                    "ISO2_JWT_SECRET debe tener al menos " + MINIMO_BYTES_CLAVE
                            + " bytes para firmar con HS256");
        }
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.duracion = Duration.ofMinutes(duracionMinutos);
    }

    /**
     * Emite un token firmado para un usuario con su rol unico.
     *
     * @param usuario nombre de inicio de sesion (claim {@code sub})
     * @param rol     rol unico activo (claim {@code rol})
     * @return sesion con el token y su momento de expiracion
     */
    public SesionDTO generar(String usuario, Rol rol) {
        Instant ahora = Instant.now();
        Instant expiracion = ahora.plus(duracion);
        String token = Jwts.builder()
                .subject(usuario)
                .claim(CLAIM_ROL, rol.name())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(expiracion))
                .signWith(clave, Jwts.SIG.HS256)
                .compact();
        return new SesionDTO(usuario, rol.name(), token, expiracion);
    }

    /**
     * Verifica firma, caducidad y estructura de un token recibido.
     *
     * @param token JWT compacto
     * @return la sesion que transporta; vacio si el token es invalido, caducado o firmado
     *         con otra clave (el filtro lo traduce a 401)
     */
    public Optional<SesionDTO> leerSesion(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(clave)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            String rolClaim = claims.get(CLAIM_ROL, String.class);
            if (Rol.desdeCodigo(rolClaim).isEmpty()) {
                return Optional.empty();
            }
            Instant expira = claims.getExpiration().toInstant();
            return Optional.of(new SesionDTO(claims.getSubject(), rolClaim, token, expira));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
