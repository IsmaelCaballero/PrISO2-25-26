package es.uclm.iso2.seguridad.web;

import es.uclm.iso2.common.seguridad.AutenticacionFacade.CredencialesDTO;
import es.uclm.iso2.common.seguridad.AutenticacionFacade.SesionDTO;
import es.uclm.iso2.seguridad.dominio.CredencialesInvalidasException;
import es.uclm.iso2.seguridad.dominio.CuentaBloqueadaException;
import es.uclm.iso2.seguridad.servicio.SeguridadServicio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba de la capa web de autenticacion (CDU17) con MockMvc aislado: el servicio se
 * simula y se comprueba la traduccion excepcion -> HTTP que exige CFG-02
 * (200 sesion, 400 DATOS_INVALIDOS, 401 CREDENCIALES_INVALIDAS, 401 CUENTA_BLOQUEADA).
 */
class ControladorAutenticacionTest {

    private SeguridadServicio servicio;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        servicio = mock(SeguridadServicio.class);
        mvc = MockMvcBuilders.standaloneSetup(new ControladorAutenticacion(servicio)).build();
    }

    /**
     * Login correcto: 200 con la sesion serializada (usuario, rol, token, caducidad).
     */
    @Test
    void loginCorrectoDevuelveSesion() throws Exception {
        when(servicio.iniciarSesion(any())).thenReturn(
                new SesionDTO("medico", "MEDICO", "token.firmado.abc", Instant.parse("2026-11-08T12:00:00Z")));
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"medico\",\"password\":\"iso2-medico\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario").value("medico"))
                .andExpect(jsonPath("$.rol").value("MEDICO"))
                .andExpect(jsonPath("$.token").value("token.firmado.abc"));
    }

    /**
     * Credenciales incorrectas: 401 con codigo estable (sin revelar la causa).
     */
    @Test
    void credencialesInvalidasDevuelven401() throws Exception {
        when(servicio.iniciarSesion(any())).thenThrow(new CredencialesInvalidasException());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"medico\",\"password\":\"no\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("CREDENCIALES_INVALIDAS"));
    }

    /**
     * Cuenta bloqueada: 401 con codigo CUENTA_BLOQUEADA y la fecha en el mensaje.
     */
    @Test
    void cuentaBloqueadaDevuelve401ConCodigo() throws Exception {
        when(servicio.iniciarSesion(any()))
                .thenThrow(new CuentaBloqueadaException(Instant.parse("2026-11-08T12:15:00Z")));
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"bloqueable\",\"password\":\"iso2-bloq\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("CUENTA_BLOQUEADA"))
                .andExpect(jsonPath("$.mensaje").value(org.hamcrest.Matchers.containsString("2026-11-08T12:15")));
    }

    /**
     * Faltan credenciales: 400 con codigo DATOS_INVALIDOS.
     */
    @Test
    void credencialesVaciasDevuelven400() throws Exception {
        when(servicio.iniciarSesion(any())).thenThrow(new IllegalArgumentException("Usuario y contrasena son obligatorios"));
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DATOS_INVALIDOS"));
    }

    /**
     * El controlador delega en el servicio con las credenciales leidas del cuerpo.
     */
    @Test
    void delegaLasCredencialesEnElServicio() throws Exception {
        when(servicio.iniciarSesion(new CredencialesDTO("admin", "iso2-admin"))).thenReturn(
                new SesionDTO("admin", "ADMINISTRADOR", "tok", Instant.now()));
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"admin\",\"password\":\"iso2-admin\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("ADMINISTRADOR"));
    }
}
