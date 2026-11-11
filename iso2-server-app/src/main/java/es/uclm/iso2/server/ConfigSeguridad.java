package es.uclm.iso2.server;

import es.uclm.iso2.common.shared.ApiError;
import es.uclm.iso2.seguridad.servicio.JwtUtils;
import es.uclm.iso2.seguridad.web.JwtAutenticacionFiltro;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

/**
 * Cadena de seguridad definitiva de CDU17 (CFG-02): sustituye el Basic/in-memory
 * transitorio de CFG-01 por JWT stateless sobre usuarios persistidos.
 *
 * <p>Reglas derivadas de la matriz RBAC de {@code iso2-seguridad} (5 roles):</p>
 * <ul>
 *   <li>{@code POST /api/auth/login} y {@code /actuator/health}: publicos.</li>
 *   <li>GET de catalogo: cualquier rol autenticado (los cuatro roles de trabajo consultan).</li>
 *   <li>Escritura de catalogo (POST/PUT/DELETE): solo {@code ADMINISTRADOR}.</li>
 *   <li>GET de agendas (CDU13): roles con {@code agenda:leer} (ADMINISTRADOR, MEDICO,
 *       TECNICO, RECEPCIONISTA); CELADOR queda fuera por matriz.</li>
 *   <li>Escritura de agendas: solo {@code ADMINISTRADOR} y {@code RECEPCIONISTA}
 *       ({@code agenda:escribir}, actor Administrativo del CDU13).</li>
 *   <li>GET de solicitudes (CDU06): roles con {@code solicitud:leer} (ADMINISTRADOR,
 *       MEDICO, RECEPCIONISTA); TECNICO y CELADOR quedan fuera por matriz.</li>
 *   <li>Alta de solicitudes: solo {@code ADMINISTRADOR} y {@code MEDICO}
 *       ({@code solicitud:escribir}, actor Medico del CDU06).</li>
 *   <li>{@code /h2-console/**}: solo {@code ADMINISTRADOR}. Como el navegador no puede
 *       enviar la cabecera {@code Authorization} en la navegación HTML, la consola se
 *       usa en desarrollo con herramientas que sí pueden (curl/scripts) adjuntando el
 *       token del login: se acepta perder la comoda ventana de navegador a cambio de
 *       una unica via de autenticacion (JWT) y cadena sin estado.</li>
 *   <li>Resto: autenticado.</li>
 * </ul>
 *
 * <p>Sin sesion HTTP ni Basic: la autorizacion se apoya en el claim {@code rol} del
 * token, verificado por {@link JwtAutenticacionFiltro} antes que el filtro de usuario
 * y clave. Las respuestas de autenticacion usan el cuerpo {@link ApiError} del contrato.</p>
 */
@Configuration
@EnableWebSecurity
public class ConfigSeguridad {

    /**
     * Cadena de filtros stateless con JWT. Se conserva frameOptions sameOrigin para la
     * consola H2 (uso en desarrollo con token adjunto).
     *
     * @param http    configurador de la cadena de filtros
     * @param jwtUtils emisor/verificador de tokens del dominio de seguridad
     * @return la cadena de filtros construida
     * @throws Exception si la configuracion es invalida
     */
    @Bean
    public SecurityFilterChain filtroHttp(HttpSecurity http, JwtUtils jwtUtils) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(reglas -> reglas
                        .requestMatchers("/api/auth/login", "/actuator/health").permitAll()
                        .requestMatchers("/h2-console/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/catalogo/**").authenticated()
                        .requestMatchers("/api/catalogo/**").hasRole("ADMINISTRADOR")
                        // CDU13: lectura para roles con agenda:leer (celador, sin ella, fuera);
                        // escritura solo para ADMINISTRADOR y RECEPCIONISTA (agenda:escribir).
                        .requestMatchers(HttpMethod.GET, "/api/agendas/**").hasAnyRole("ADMINISTRADOR", "MEDICO", "TECNICO", "RECEPCIONISTA")
                        .requestMatchers("/api/agendas/**").hasAnyRole("ADMINISTRADOR", "RECEPCIONISTA")
                        // CDU06: lectura para roles con solicitud:leer (tecnico y celador, sin
                        // el permiso, fuera); prescribir solo ADMINISTRADOR y MEDICO
                        // (solicitud:escribir, actor Medico del CDU06).
                        .requestMatchers(HttpMethod.GET, "/api/solicitudes/**").hasAnyRole("ADMINISTRADOR", "MEDICO", "RECEPCIONISTA")
                        .requestMatchers("/api/solicitudes/**").hasAnyRole("ADMINISTRADOR", "MEDICO")
                        // D34 (Iter5): el diagnostico de persistencia revela estructura interna
                        // de la base (tablas y registros), solo para ADMINISTRADOR.
                        .requestMatchers("/api/persistencia/**").hasRole("ADMINISTRADOR")
                        .anyRequest().authenticated())
                .exceptionHandling(manejo -> manejo
                        .authenticationEntryPoint((peticion, respuesta, causa) ->
                                escribirError(respuesta, HttpStatus.UNAUTHORIZED, "AUTENTICACION_REQUERIDA",
                                        "Hace falta un token JWT valido en la cabecera Authorization (Bearer ...)"))
                        .accessDeniedHandler((peticion, respuesta, causa) ->
                                escribirError(respuesta, HttpStatus.FORBIDDEN, "FORBIDDEN",
                                        "El rol del token no tiene permiso para este recurso")))
                .addFilterBefore(new JwtAutenticacionFiltro(jwtUtils),
                        UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /**
     * Codificador de contrasenas: BCrypt, el mismo que verifican el servicio de seguridad
     * y los hashes de la migracion Flyway V1.2.0.
     *
     * @return el codificador BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private static void escribirError(HttpServletResponse respuesta, HttpStatus estado,
                                      String codigo, String mensaje) throws IOException {
        respuesta.setStatus(estado.value());
        respuesta.setContentType("application/json;charset=UTF-8");
        respuesta.setCharacterEncoding(StandardCharsets.UTF_8.name());
        // Cuerpo ApiError del contrato, serializado a mano para no acoplar la cadena
        // de seguridad a un ObjectMapper: los valores son constantes controladas.
        ApiError error = new ApiError(codigo, mensaje, Instant.now());
        respuesta.getWriter().print("{\"codigo\":\"" + error.codigo() + "\",\"mensaje\":\""
                + error.mensaje() + "\",\"instante\":\"" + error.instante() + "\"}");
    }
}
