package es.uclm.iso2.server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * RBAC transitorio de la aplicacion servidora (CFG-01). El RBAC definitivo con JWT y
 * usuarios persistidos llega con CDU17 (Iter2, decision de plan): mientras tanto se
 * protegen los endpoints con HTTP Basic y dos usuarios en memoria.
 *
 * <p>Roles demostrables (puerta CFG-01: 401 sin credenciales, 403 con rol incorrecto):</p>
 * <ul>
 *   <li>{@code admin/ADMIN}: puede gestionar el catalogo (CDU16).</li>
 *   <li>{@code celador/SES}: autenticado pero sin permiso de catalogo (forraje del RBAC).</li>
 * </ul>
 *
 * <p>La consola H2 queda reservada a ADMIN. El CSRF se desactiva porque la API es
 * sin estado y consumida por un cliente nativo (JavaFX), no por formularios web.</p>
 */
@Configuration
@EnableWebSecurity
public class ConfigSeguridad {

    /**
     * Cadena de filtros: Basic auth y reglas por URL. Se conserva la cabecera de la
     * consola H2 con frameOptions sameOrigin para poder usarla en desarrollo.
     *
     * @param http configurador de la cadena de filtros
     * @return la cadena de filtros construida
     * @throws Exception si la configuracion es invalida
     */
    @Bean
    public SecurityFilterChain filtroHttp(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .authorizeHttpRequests(reglas -> reglas
                        .requestMatchers("/h2-console/**").hasRole("ADMIN")
                        .requestMatchers("/api/catalogo/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults())
                .build();
    }

    /**
     * Codificador de contrasenas: BCrypt, el mismo que usara el RBAC definitivo (CDU17).
     *
     * @return el codificador BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Usuarios provisionales en memoria (no hay tabla de usuarios hasta CDU17).
     *
     * @param passwordEncoder codificador para guardar las claves con hash
     * @return el gestor de usuarios en memoria
     */
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails administrador = User.withUsername("admin")
                .password(passwordEncoder.encode("iso2-admin"))
                .roles("ADMIN")
                .build();
        UserDetails ses = User.withUsername("celador")
                .password(passwordEncoder.encode("iso2-ses"))
                .roles("SES")
                .build();
        return new InMemoryUserDetailsManager(administrador, ses);
    }
}
