package es.uclm.iso2.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba de humo extremo a extremo de CDU16 + CDU17 (Iter2): arranca la aplicacion
 * servidora completa (seguridad JWT + MVC + servicio + JPA + esquema Flyway sobre H2 en
 * memoria) y comprueba la puerta RBAC del catalogo y el alta REST real con la
 * autenticacion definitiva del producto: login por API y token Bearer.
 *
 * <p>No sustituye a los TC de CDU16/CDU17 (decision D28: se disenan sobre el codigo ya
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

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Sin token el catalogo responde 401 con el codigo estable del contrato
     * (regla RBAC de CDU17: la cadena es stateless JWT).
     */
    @Test
    void accesoAnonimoDevuelve401() throws Exception {
        mvc.perform(get("/api/catalogo/pruebas"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("AUTENTICACION_REQUERIDA"));
    }

    /**
     * ADMINISTRADOR puede listar el catalogo (200) usando su cuenta real de la
     * migracion V1.2.0, lo que valida de paso login BCrypt, emision JWT y filtro.
     */
    @Test
    void adminPuedeListarCon200() throws Exception {
        mvc.perform(get("/api/catalogo/pruebas").param("pagina", "0").param("tamano", "10")
                        .header(AUTHORIZATION, "Bearer " + tokenDeAdmin()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    /**
     * Alta REST real extremo a extremo con JWT: POST validado por el DTO, persistido
     * por JPA sobre el esquema de Flyway y devuelto por GET. Si algo del ensamblado
     * (scan de componentes, -parameters, migraciones, filtro JWT) se rompe, aqui se ve.
     */
    @Test
    void altaRestYConsultaDevuelvenElAltaReal() throws Exception {
        String token = tokenDeAdmin();
        mvc.perform(post("/api/catalogo/pruebas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ALTA_SMOKE)
                        .header(AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value("SMOKE-RX-99"))
                .andExpect(jsonPath("$.activa").value(true));

        mvc.perform(get("/api/catalogo/pruebas/SMOKE-RX-99")
                        .header(AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Radiografia de prueba smoke"));
    }

    /**
     * Un alta con datos invalidos revienta contra la validacion del DTO y el consejo
     * de errores la traduce a 400 con cuerpo {@code ApiError}.
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
                        .header(AUTHORIZATION, "Bearer " + tokenDeAdmin()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DATOS_INVALIDOS"));
    }

    /**
     * Cambio de comportamiento aprobado en Iter2 (matriz RBAC de CDU17): un rol no
     * administrador (CELADOR) SI puede leer el catalogo (200) pero NO puede escribir:
     * el alta responde 403 con ApiError {@code FORBIDDEN}.
     */
    @Test
    void celadorLeePeroNoEscribe() throws Exception {
        MvcResult login = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"celador\",\"password\":\"iso2-ses\"}"))
                .andExpect(status().isOk())
                .andReturn();
        String token = objectMapper.readTree(login.getResponse().getContentAsString()).get("token").asText();

        mvc.perform(get("/api/catalogo/pruebas").header(AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());

        mvc.perform(post("/api/catalogo/pruebas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ALTA_SMOKE)
                        .header(AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("FORBIDDEN"));
    }

    /**
     * Login de la cuenta semilla ADMINISTRADOR y extraction del token Bearer.
     *
     * @return token JWT valido emitido por la aplicacion
     */
    private String tokenDeAdmin() throws Exception {
        MvcResult login = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"admin\",\"password\":\"iso2-admin\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(login.getResponse().getContentAsString()).get("token").asText();
    }
}
