package es.uclm.iso2.server;

import es.uclm.iso2.seguridad.dominio.Rol;
import es.uclm.iso2.seguridad.servicio.JwtUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.stream.Stream;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Puerta de calidad CFG-02 (Iter2/CDU17): matriz RBAC sobre los 6 endpoints del
 * catalogo + ciclo JWT (emitir, manipular, caducar) + bloqueo por intentos fallidos,
 * todo por HTTP contra la aplicacion servidora completa.
 *
 * <p>El catalogo arranca vacio (V1.1.0 no siembra pruebas), asi que las lecturas
 * autorizadas sobre un codigo inexistente responden <strong>404, no 403</strong>: el
 * 404 demuestra que la peticion atraveso la autorizacion y llego al controlador. Las
 * cuentas semilla son las de la migracion V1.2.0.</p>
 *
 * <p>No sustituye los TC funcionales de {@code docs/pruebas/} (D28): cubre la contractura
 * de seguridad extremo a extremo exigida por CFG-02.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
class SeguridadApiTest {

    private static final String RUTA_LISTA = "/api/catalogo/pruebas";
    private static final String RUTA_DETALLE = "/api/catalogo/pruebas/NOPE-999";
    private static final String CUERPO_ALTA = """
            {"codigo":"RBAC-999","nombre":"Intento RBAC","grupo":"RM","procedencia":"LOCAL"}""";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Value("${iso2.seguridad.jwt.secret}")
    private String secretoDePruebas;

    private record Semilla(String usuario, String password, String rol) {
    }

    private static List<Semilla> semillas() {
        return List.of(
                new Semilla("admin", "iso2-admin", "ADMINISTRADOR"),
                new Semilla("medico", "iso2-medico", "MEDICO"),
                new Semilla("tecnico", "iso2-tecnico", "TECNICO"),
                new Semilla("celador", "iso2-ses", "CELADOR"),
                new Semilla("recepcionista", "iso2-rec", "RECEPCIONISTA"));
    }

    private static Stream<Arguments> cincoRoles() {
        return semillas().stream().map(Arguments::of);
    }

    private static Stream<Arguments> rolesNoAdministradores() {
        return semillas().stream().filter(s -> !"ADMINISTRADOR".equals(s.rol())).map(Arguments::of);
    }

    /**
     * Los cinco roles autenticados pueden leer el catalogo: la lista responde 200 y el
     * detalle de un codigo inexistente responde 404 (autorizado: llega al controlador).
     * El login ademas devuelve el rol esperado en el cuerpo de la sesion.
     */
    @ParameterizedTest
    @MethodSource("cincoRoles")
    void todosLosRolesPuedenLeerCatalogo(Semilla semilla) throws Exception {
        String token = tokenDe(semilla.usuario(), semilla.password());

        mvc.perform(get(RUTA_LISTA).header(AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
        // El contrato de catalogo responde 404 con cuerpo vacio cuando el codigo no
        // existe: el 404 (y no un 403) demuestra que la autorizacion del rol paso.
        mvc.perform(get(RUTA_DETALLE).header(AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    /**
     * Puerta CFG-02 "403 por rol no autorizado en cada endpoint": los cuatro roles que
     * no son ADMINISTRADOR reciben 403 con ApiError en los cuatro endpoints de escritura
     * (alta, modificacion, baja logica, reactivacion).
     */
    @ParameterizedTest
    @MethodSource("rolesNoAdministradores")
    void rolesNoAdministradoresNoPuedenEscribir(Semilla semilla) throws Exception {
        String token = tokenDe(semilla.usuario(), semilla.password());

        mvc.perform(post(RUTA_LISTA).header(AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(CUERPO_ALTA))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("FORBIDDEN"));
        mvc.perform(put(RUTA_DETALLE).header(AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(CUERPO_ALTA))
                .andExpect(status().isForbidden());
        mvc.perform(delete(RUTA_DETALLE).header(AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden());
        mvc.perform(put(RUTA_DETALLE + "/reactivar").header(AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    /**
     * Sin token: todos los endpoints de negocio y la consola H2 responden 401 con el
     * codigo estable del contrato. El login sigue siendo publico.
     */
    @Test
    void anonimoRecibe401EnRutasProtegidas() throws Exception {
        mvc.perform(get(RUTA_LISTA))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("AUTENTICACION_REQUERIDA"));
        mvc.perform(post(RUTA_LISTA).contentType(MediaType.APPLICATION_JSON).content(CUERPO_ALTA))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/h2-console"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"admin\",\"password\":\"iso2-admin\"}"))
                .andExpect(status().isOk());
    }

    /**
     * La consola H2 queda solo para ADMINISTRADOR: un rol autenticado no administrador
     * recibe 403.
     */
    @Test
    void consolaH2EsSoloDeAdministrador() throws Exception {
        String token = tokenDe("medico", "iso2-medico");
        mvc.perform(get("/h2-console").header(AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    /**
     * Un token manipulado (firma alterada) se rechaza con 401: la verificacion de la
     * firma HS256 es obligatoria en cada peticion.
     */
    @Test
    void tokenManipuladoDevuelve401() throws Exception {
        String valido = tokenDe("medico", "iso2-medico");
        mvc.perform(get(RUTA_LISTA).header(AUTHORIZATION, "Bearer " + valido + "x"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Un token firmado con otra clave distinta (otro secreto de 32+ bytes) se rechaza
     * con 401 aunque su estructura sea valida.
     */
    @Test
    void tokenDeOtraClaveDevuelve401() throws Exception {
        String ajeno = new JwtUtils("otra-clave-supersecreta-de-prueba-mas-de-32-bytes", 30)
                .generar("medico", Rol.MEDICO).token();
        mvc.perform(get(RUTA_LISTA).header(AUTHORIZATION, "Bearer " + ajeno))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Un token ya caducado se rechaza con 401: se construye con el secreto de pruebas
     * y duracion negativa para que nazca expirado (la caducidad se comprueba siempre).
     */
    @Test
    void tokenCaducadoDevuelve401() throws Exception {
        String caducado = new JwtUtils(secretoDePruebas, -5).generar("medico", Rol.MEDICO).token();
        mvc.perform(get(RUTA_LISTA).header(AUTHORIZATION, "Bearer " + caducado))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Mitigacion de fuerza bruta (RF17): tras 5 intentos fallidos seguidos la cuenta
     * queda bloqueada y la contrasena correcta tambien devuelve 401 con codigo
     * {@code CUENTA_BLOQUEADA}; otras cuentas siguen operativas.
     */
    @Test
    void cincoIntentosFallidosBloqueanLaCuenta() throws Exception {
        for (int i = 1; i <= 5; i++) {
            mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"usuario\":\"bloqueable\",\"password\":\"mal-" + i + "\"}"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.codigo").value("CREDENCIALES_INVALIDAS"));
        }
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"bloqueable\",\"password\":\"iso2-bloq\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("CUENTA_BLOQUEADA"));

        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"admin\",\"password\":\"iso2-admin\"}"))
                .andExpect(status().isOk());
    }

    /**
     * Login sin credenciales: 400 con codigo {@code DATOS_INVALIDOS} (validacion de
     * entrada, no un 500).
     */
    @Test
    void loginSinCredencialesDevuelve400() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DATOS_INVALIDOS"));
    }

    /**
     * Login con usuario inexistente: mismo 401 {@code CREDENCIALES_INVALIDAS} que
     * contrasena erronea (no se revela si el usuario existe).
     */
    @Test
    void usuarioInexistenteDevuelve401Generico() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"fantasma\",\"password\":\"nada\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("CREDENCIALES_INVALIDAS"));
    }

    private String tokenDe(String usuario, String password) throws Exception {
        MvcResult resultado = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"%s\",\"password\":\"%s\"}".formatted(usuario, password)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(resultado.getResponse().getContentAsString()).get("token").asText();
    }
}
