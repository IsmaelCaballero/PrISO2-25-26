package es.uclm.iso2.integracion.web;

import es.uclm.iso2.integracion.dominio.AuditoriaIntegracion;
import es.uclm.iso2.integracion.dominio.EslabonAuditoria;
import es.uclm.iso2.integracion.dominio.ModoSimulacion;
import es.uclm.iso2.integracion.dominio.PoliticaSimulacion;
import es.uclm.iso2.integracion.dominio.RegistroSimulacion;
import es.uclm.iso2.integracion.dominio.SistemaExterno;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas tecnicas del controlador de simulacion (CDU19): es la pieza que permite
 * ejecutar los TCs degradados del CSV TDOP contra el servidor vivo, asi que se
 * comprueba con el dominio real (registro y auditoria de verdad, sin mofas) que la
 * politica queda instalada, que los errores de entrada cierran en 400/404 y que cada
 * cambio deja su eslabon SIMULACION con el token del setup.
 */
class SimulacionControladorTest {

    private RegistroSimulacion registro;
    private AuditoriaIntegracion auditoria;
    private MockMvc mvc;

    @BeforeEach
    void montar() {
        registro = new RegistroSimulacion(Map.of());
        auditoria = new AuditoriaIntegracion();
        JsonMapper mapaFechas = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
        mvc = MockMvcBuilders.standaloneSetup(new SimulacionControlador(registro, auditoria))
                .setControllerAdvice(new IntegracionManejadorErrores())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(mapaFechas))
                .build();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void desmontar() {
        SecurityContextHolder.clearContext();
    }

    // ------------------------------------------------------------------
    // Estado del registro
    // ------------------------------------------------------------------

    @Test
    void elRegistroArrancaEnModoNormalParaLosCuatroSistemas() throws Exception {
        mvc.perform(get("/api/integracion/simulacion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.SSO.modo", is("NORMAL")))
                .andExpect(jsonPath("$.HCE.modo", is("NORMAL")))
                .andExpect(jsonPath("$.RIS.modo", is("NORMAL")))
                .andExpect(jsonPath("$.PACS.modo", is("NORMAL")))
                .andExpect(jsonPath("$.HCE.reintentos", is(0)));
    }

    @Test
    void elRegistroPintaLasPoliticasConLasQueNacio() throws Exception {
        registro = new RegistroSimulacion(Map.of(SistemaExterno.RIS,
                new PoliticaSimulacion(ModoSimulacion.LENTO_TIMEOUT, 30, 10, 2)));
        mvc = MockMvcBuilders.standaloneSetup(new SimulacionControlador(registro, auditoria))
                .setControllerAdvice(new IntegracionManejadorErrores())
                .build();

        mvc.perform(get("/api/integracion/simulacion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.RIS.modo", is("LENTO_TIMEOUT")))
                .andExpect(jsonPath("$.RIS.latenciaMs", is(30)))
                .andExpect(jsonPath("$.RIS.timeoutMs", is(10)))
                .andExpect(jsonPath("$.RIS.reintentos", is(2)));
    }

    // ------------------------------------------------------------------
    // Cambio de politica en caliente
    // ------------------------------------------------------------------

    @Test
    void cambiarDejaLaPoliticaInstaladaParaLaSiguienteLlamada() throws Exception {
        mvc.perform(post("/api/integracion/simulacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sistema\":\"hce\",\"modo\":\"CAIDO\","
                                + "\"latenciaMs\":5,\"timeoutMs\":50,\"reintentos\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modo", is("CAIDO")))
                .andExpect(jsonPath("$.latenciaMs", is(5)))
                .andExpect(jsonPath("$.timeoutMs", is(50)));

        assertEquals(ModoSimulacion.CAIDO, registro.de(SistemaExterno.HCE).modo());
        assertEquals(ModoSimulacion.NORMAL, registro.de(SistemaExterno.RIS).modo());
    }

    @Test
    void cadaCambioDejaUnEslabonSIMULACIONConElTokenDelSetup() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin-iso2", "x"));

        mvc.perform(post("/api/integracion/simulacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sistema\":\"ris\",\"modo\":\"LENTO_TIMEOUT\","
                                + "\"latenciaMs\":30,\"timeoutMs\":10,\"reintentos\":2}"))
                .andExpect(status().isOk());
        mvc.perform(post("/api/integracion/simulacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sistema\":\"SSO\",\"modo\":\"normal\",\"latenciaMs\":0,"
                                + "\"timeoutMs\":50,\"reintentos\":0}"))
                .andExpect(status().isOk());

        List<EslabonAuditoria> cadena = auditoria.listar();
        assertEquals(2, cadena.size());
        assertEquals("SIMULACION", cadena.get(0).accion());
        assertEquals("ris", cadena.get(0).sistema());
        assertEquals("SIM-RIS-LENTO_TIMEOUT-30-10-2", cadena.get(0).resultado());
        assertEquals("admin-iso2", cadena.get(0).actor());
        assertEquals("sso", cadena.get(1).sistema());
        assertEquals("SIM-SSO-NORMAL-0-50-0", cadena.get(1).resultado());
        assertEquals(cadena.get(0).hash(), cadena.get(1).hashAnterior());
        assertTrue(auditoria.cadenaIntegra());
    }

    @Test
    void sinAutenticacionElActorAuditadoEsElSistema() throws Exception {
        mvc.perform(post("/api/integracion/simulacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sistema\":\"pacs\",\"modo\":\"CAIDO\","
                                + "\"latenciaMs\":0,\"timeoutMs\":50,\"reintentos\":0}"))
                .andExpect(status().isOk());

        assertEquals("USUARIO_SISTEMA", auditoria.listar().get(0).actor());
    }

    @Test
    void cambiarValeMayusculasYMinusculasEnSistemaYModo() throws Exception {
        mvc.perform(post("/api/integracion/simulacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sistema\":\" HcE \",\"modo\":\"  caido \","
                                + "\"latenciaMs\":1,\"timeoutMs\":5,\"reintentos\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modo", is("CAIDO")));
    }

    // ------------------------------------------------------------------
    // Errores de entrada
    // ------------------------------------------------------------------

    @Test
    void unSistemaQueNoExisteCierraEn404SinTocarElRegistro() throws Exception {
        mvc.perform(post("/api/integracion/simulacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sistema\":\"sap\",\"modo\":\"CAIDO\","
                                + "\"latenciaMs\":5,\"timeoutMs\":50,\"reintentos\":0}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo", is("SISTEMA_NO_ENCONTRADO")));

        assertEquals(ModoSimulacion.NORMAL, registro.de(SistemaExterno.HCE).modo());
        assertEquals(0, auditoria.listar().size());
    }

    @Test
    void unModoDesconocidoOCifrasAusentesSon400() throws Exception {
        mvc.perform(post("/api/integracion/simulacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sistema\":\"hce\",\"modo\":\"FLOJO\","
                                + "\"latenciaMs\":5,\"timeoutMs\":50,\"reintentos\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo", is("DATOS_INVALIDOS")));

        mvc.perform(post("/api/integracion/simulacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sistema\":\"hce\",\"modo\":\"CAIDO\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo", is("DATOS_INVALIDOS")));

        mvc.perform(post("/api/integracion/simulacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"modo\":\"CAIDO\",\"latenciaMs\":5,\"timeoutMs\":50,\"reintentos\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo", is("DATOS_INVALIDOS")));

        mvc.perform(post("/api/integracion/simulacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sistema\":\"hce\",\"modo\":\"CAIDO\","
                                + "\"latenciaMs\":-5,\"timeoutMs\":50,\"reintentos\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo", is("DATOS_INVALIDOS")));

        mvc.perform(post("/api/integracion/simulacion")
                        .contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo", is("DATOS_INVALIDOS")));

        assertEquals(0, auditoria.listar().size());
    }
}
