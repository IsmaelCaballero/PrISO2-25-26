package es.uclm.iso2.server;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba de humo extremo a extremo de CFG-01 (Iter1/CDU16): arranca la aplicacion
 * servidora completa (seguridad + MVC + servicio + JPA + esquema Flyway sobre H2 en
 * memoria) y comprueba la puerta RBAC del catalogo y el alta REST real.
 *
 * <p>No sustituye a los TC de CDU16 (decision D28: se disenan sobre el codigo ya
 * funcional); es la verificacion tecnica minima del ensamblado.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
class CatalogoApiSmokeTest {

    private static final String ALTA_SMOKE = """
            {"codigo":"SMOKE-RX-99","nombre":"Radiografia de prueba smoke",
             "categoria":"RX","requiereCita":false,"duracionMinutos":10,
             "protocolo":{"preparacion":"sin preparacion","ayunoHoras":0,
                          "contraindicaciones":"ninguna"},
             "politica":{"plazoCancelacionHoras":24,"maxCancelaciones":3}}
            """;

    @Autowired
    private MockMvc mvc;

    /**
     * Sin credenciales el catalogo responde 401 (regla RBAC de CFG-01).
     *
     * @throws Exception si falla la peticion simulada
     */
    @Test
    void accesoAnonimoDevuelve401() throws Exception {
        mvc.perform(get("/api/catalogo/pruebas"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Un usuario autenticado sin rol ADMIN recibe 403 (RBAC por rol, no solo login).
     *
     * @throws Exception si falla la peticion simulada
     */
    @Test
    void rolSesSinPermisoDevuelve403() throws Exception {
        mvc.perform(get("/api/catalogo/pruebas").with(httpBasic("celador", "iso2-ses")))
                .andExpect(status().isForbidden());
    }

    /**
     * ADMIN puede listar el catalogo (200) usando las credenciales reales de
     * {@link ConfigSeguridad}, lo que ademas valida el encoder BCrypt en memoria.
     *
     * @throws Exception si falla la peticion simulada
     */
    @Test
    void adminPuedeListarCon200() throws Exception {
        mvc.perform(get("/api/catalogo/pruebas").param("pagina", "0").param("tamano", "10")
                        .with(httpBasic("admin", "iso2-admin")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    /**
     * Alta REST real extremo a extremo: POST validado por el DTO, persistido por JPA
     * sobre el esquema de Flyway y devuelto por GET. Si algo del ensamblado (scan de
     * componentes, -parameters, migraciones) se rompe, este test lo pilla.
     *
     * @throws Exception si falla la peticion simulada
     */
    @Test
    void altaRestYConsultaDevuelvenElAltaReal() throws Exception {
        mvc.perform(post("/api/catalogo/pruebas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ALTA_SMOKE)
                        .with(httpBasic("admin", "iso2-admin")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value("SMOKE-RX-99"))
                .andExpect(jsonPath("$.activa").value(true));

        mvc.perform(get("/api/catalogo/pruebas/SMOKE-RX-99")
                        .with(httpBasic("admin", "iso2-admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Radiografia de prueba smoke"));
    }

    /**
     * Un alta con datos invalidos revienta contra la validacion del DTO y el consejo
     * de errores la traduce a 400 con cuerpo {@code ApiError}.
     *
     * @throws Exception si falla la peticion simulada
     */
    @Test
    void altaInvalidaDevuelve400ConApiError() throws Exception {
        mvc.perform(post("/api/catalogo/pruebas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"codigo":"","nombre":"","categoria":"RX","requiereCita":false,
                                 "duracionMinutos":10,
                                 "protocolo":{"preparacion":"x","ayunoHoras":0,"contraindicaciones":"y"},
                                 "politica":{"plazoCancelacionHoras":24,"maxCancelaciones":3}}
                                """)
                        .with(httpBasic("admin", "iso2-admin")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DATOS_INVALIDOS"));
    }
}
