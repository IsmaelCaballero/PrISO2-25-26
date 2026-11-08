package es.uclm.iso2.seguridad.web;

import es.uclm.iso2.seguridad.dominio.Rol;
import es.uclm.iso2.seguridad.servicio.JwtUtils;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests unitarios del filtro JWT (CDU17): con token valido rellena el contexto con la
 * autoridad ROLE_&lt;ROL&gt;; sin token, con token invalido o con otro esquema (Basic)
 * no autentica y deja continuar a la cadena (quien responde es la configuracion web).
 */
class JwtAutenticacionFiltroTest {

    private final JwtUtils jwt = new JwtUtils("secreto-de-pruebas-para-jwt-de-mas-de-32-bytes-seguro", 30);
    private final JwtAutenticacionFiltro filtro = new JwtAutenticacionFiltro(jwt);

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    /**
     * Token valido: el contexto queda autenticado con la autoridad del rol del claim.
     */
    @Test
    void tokenValidoAutenticaConLaAutoridadDelRol() throws Exception {
        String token = jwt.generar("celador", Rol.CELADOR).token();
        MockHttpServletRequest peticion = new MockHttpServletRequest("GET", "/api/catalogo/pruebas");
        peticion.addHeader("Authorization", "Bearer " + token);

        filtro.doFilter(peticion, new MockHttpServletResponse(), new MockFilterChain());

        var autenticacion = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(autenticacion, "el filtro debe autenticar con token valido");
        assertEquals("celador", autenticacion.getName());
        assertTrue(autenticacion.getAuthorities().stream()
                .anyMatch(a -> "ROLE_CELADOR".equals(a.getAuthority())));
    }

    /**
     * Sin cabecera, con cabecera que no es Bearer o con token invalido: no se pone
     * autenticacion (la cadena decidira despues 401/permitir) y la peticion continua.
     */
    @Test
    void sinTokenOTokenInvalidoNoAutenticaYPasa() throws Exception {
        AtomicBoolean cadenaInvocada = new AtomicBoolean();
        var cadena = new MockFilterChain() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response) {
                cadenaInvocada.set(true);
            }
        };

        filtro.doFilter(new MockHttpServletRequest("GET", "/api/catalogo/pruebas"),
                new MockHttpServletResponse(), cadena);
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        MockHttpServletRequest basica = new MockHttpServletRequest("GET", "/x");
        basica.addHeader("Authorization", "Basic dXNlcjpwYXNz");
        filtro.doFilter(basica, new MockHttpServletResponse(), cadena);
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        MockHttpServletRequest roto = new MockHttpServletRequest("GET", "/x");
        roto.addHeader("Authorization", "Bearer token-no-valido");
        filtro.doFilter(roto, new MockHttpServletResponse(), cadena);
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        assertTrue(cadenaInvocada.get(), "el filtro siempre debe dejar continuar la cadena");
    }

    /**
     * El prefijo Bearer se define una sola vez y es el del RFC 6750 (lo comparten
     * cliente y servidor).
     */
    @Test
    void elPrefijoBearerEsElContractual() {
        assertEquals("Bearer ", JwtAutenticacionFiltro.PREFIJO_BEARER);
    }
}
