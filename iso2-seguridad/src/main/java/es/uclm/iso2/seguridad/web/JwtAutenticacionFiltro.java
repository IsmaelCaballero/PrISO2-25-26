package es.uclm.iso2.seguridad.web;

import es.uclm.iso2.common.seguridad.AutenticacionFacade.SesionDTO;
import es.uclm.iso2.seguridad.servicio.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtro stateless de la cadena de seguridad (CDU17): si la peticion trae la cabecera
 * {@code Authorization: Bearer <jwt>}, verifica firma y caducidad con {@link JwtUtils}
 * y rellena el {@code SecurityContext} con la autoridad {@code ROLE_<ROL>} del claim.
 *
 * <p>Si no hay token o es invalido, el filtro no autentica y deja pasar: quien responde
 * (401 o 403) es la cadena de {@code iso2-server-app}, no este filtro. Autorizar por
 * el claim del token evita consultar la base de datos en cada peticion (el token ya
 * fue emitido tras verificar la contrasena).</p>
 *
 * <p>No es un {@code @Component}: lo instancia la configuracion de seguridad para
 * controlar su posicion en la cadena (antes de {@code UsernamePasswordAuthenticationFilter}).</p>
 *
 * @since 1.1.0 (CFG-02)
 */
public class JwtAutenticacionFiltro extends OncePerRequestFilter {

    /** Prefijo de la cabecera Authorization definido por RFC 6750. */
    static final String PREFIJO_BEARER = "Bearer ";

    private final JwtUtils jwt;

    /**
     * Filtro verificador con el emisor/verificador de tokens del dominio.
     *
     * @param jwt utilidades JWT (bean unico de la aplicacion)
     */
    public JwtAutenticacionFiltro(JwtUtils jwt) {
        this.jwt = jwt;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest peticion, HttpServletResponse respuesta,
                                    FilterChain cadena) throws ServletException, IOException {
        String cabecera = peticion.getHeader("Authorization");
        if (cabecera != null && cabecera.startsWith(PREFIJO_BEARER)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            jwt.leerSesion(cabecera.substring(PREFIJO_BEARER.length()).trim())
                    .ifPresent(sesion -> autenticar(sesion));
        }
        cadena.doFilter(peticion, respuesta);
    }

    private static void autenticar(SesionDTO sesion) {
        var autoridad = new SimpleGrantedAuthority("ROLE_" + sesion.rol());
        var autenticacion = new UsernamePasswordAuthenticationToken(sesion.usuario(), sesion.token(),
                List.of(autoridad));
        SecurityContextHolder.getContext().setAuthentication(autenticacion);
    }
}
