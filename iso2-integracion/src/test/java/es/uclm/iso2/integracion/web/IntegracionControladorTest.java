package es.uclm.iso2.integracion.web;

import es.uclm.iso2.common.integracion.CredencialesInvalidasException;
import es.uclm.iso2.common.integracion.EstudioNoEncontradoException;
import es.uclm.iso2.common.integracion.HistoriaNoEncontradaException;
import es.uclm.iso2.common.integracion.InteropFacade;
import es.uclm.iso2.common.integracion.InteropFacade.AuditoriaIntegracionDTO;
import es.uclm.iso2.common.integracion.InteropFacade.CredencialesSsoDTO;
import es.uclm.iso2.common.integracion.InteropFacade.EslabonAuditoriaDTO;
import es.uclm.iso2.common.integracion.InteropFacade.EstadoSistemaDTO;
import es.uclm.iso2.common.integracion.InteropFacade.EstudioDTO;
import es.uclm.iso2.common.integracion.InteropFacade.HistoriaSincronizadaDTO;
import es.uclm.iso2.common.integracion.InteropFacade.RegistroHistoriaDTO;
import es.uclm.iso2.common.integracion.InteropFacade.SesionSsoDTO;
import es.uclm.iso2.common.integracion.SistemaExternoException;
import es.uclm.iso2.common.integracion.SistemaExternoException.Fallo;
import es.uclm.iso2.common.integracion.SistemaNoEncontradoException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas tecnicas del controlador REST del spike CDU19 (CDU12/CDU13 como espejo):
 * matriz de codigos D35 con fachada simulada, para comprobar que el advice traduce
 * cada excepcion de contrato a su estado y a su {@code ApiError.codigo}. El ping de
 * un sistema caido tiene que cerrar en 200, nunca en 5xx.
 */
class IntegracionControladorTest {

    private InteropFacade fachada;
    private MockMvc mvc;

    @BeforeEach
    void montar() {
        fachada = mock(InteropFacade.class);
        // Mismo formato de fechas que la aplicacion servidora: sin el conversor ISO-8601
        // MockMvc en solitario saca arrays de numeros y el contrato se comprueba mal.
        JsonMapper mapaFechas = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
        mvc = MockMvcBuilders.standaloneSetup(new IntegracionControlador(fachada))
                .setControllerAdvice(new IntegracionManejadorErrores())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(mapaFechas))
                .build();
    }

    private static SesionSsoDTO sesion() {
        return new SesionSsoDTO("idp-admin", "radiologia",
                Instant.parse("2026-11-15T12:00:00Z"));
    }

    private static HistoriaSincronizadaDTO historia(boolean reutilizado) {
        return new HistoriaSincronizadaDTO("api-paciente-ok", reutilizado, "a".repeat(64),
                Instant.parse("2026-11-15T10:00:00Z"),
                List.of(new RegistroHistoriaDTO("ENF-HTA", LocalDate.of(2024, 3, 12),
                        "Hipertension arterial diagnostica")));
    }

    // ------------------------------------------------------------------
    // Login SSO
    // ------------------------------------------------------------------

    @Test
    void elLoginSsoDevuelve200ConLaSesionDelIdp() throws Exception {
        when(fachada.autenticarSso(any())).thenReturn(sesion());

        mvc.perform(post("/api/integracion/sso/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"idp-admin\",\"contrasena\":\"idp-pass\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sujeto", is("idp-admin")))
                .andExpect(jsonPath("$.rolExterno", is("radiologia")))
                .andExpect(jsonPath("$.caducaEn", is("2026-11-15T12:00:00Z")));

        ArgumentCaptor<CredencialesSsoDTO> captor = ArgumentCaptor.forClass(CredencialesSsoDTO.class);
        verify(fachada).autenticarSso(captor.capture());
        assertEquals("idp-admin", captor.getValue().usuario());
        assertEquals("idp-pass", captor.getValue().contrasena());
    }

    @Test
    void elLoginRechazadoCierraEn401ConSuCodigo() throws Exception {
        when(fachada.autenticarSso(any()))
                .thenThrow(new CredencialesInvalidasException("el IdP rechaza las credenciales"));

        mvc.perform(post("/api/integracion/sso/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"nadie\",\"contrasena\":\"fallida\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo", is("CREDENCIALES_INVALIDAS")));
    }

    @Test
    void elIdpCaidoCierraEn503YElTiempoSuperadoTambien() throws Exception {
        when(fachada.autenticarSso(any()))
                .thenThrow(new SistemaExternoException(Fallo.NO_DISPONIBLE, "el IdP esta caido"));
        mvc.perform(post("/api/integracion/sso/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"idp-admin\",\"contrasena\":\"idp-pass\"}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.codigo", is("SISTEMA_NO_DISPONIBLE")));

        reset(fachada);
        when(fachada.autenticarSso(any()))
                .thenThrow(new SistemaExternoException(Fallo.TIEMPO_SUPERADO, "el IdP tardo demasiado"));
        mvc.perform(post("/api/integracion/sso/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"idp-admin\",\"contrasena\":\"idp-pass\"}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.codigo", is("SISTEMA_TIEMPO_SUPERADO")));
    }

    @Test
    void elLoginSinCuerpoEs400YUnContent_TypeRaroEs415() throws Exception {
        mvc.perform(post("/api/integracion/sso/login")
                        .contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo", is("DATOS_INVALIDOS")));

        mvc.perform(post("/api/integracion/sso/login")
                        .contentType(MediaType.TEXT_PLAIN).content("idp-admin"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.codigo", is("CONTENIDO_NO_ACEPTADO")));

        verify(fachada, never()).autenticarSso(any());
    }

    @Test
    void unFalloInesperadoDelServicioCierraEn500ConERROR_INTERNO() throws Exception {
        when(fachada.autenticarSso(any())).thenThrow(new IllegalStateException("no deberia pasar"));

        mvc.perform(post("/api/integracion/sso/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"idp-admin\",\"contrasena\":\"idp-pass\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.codigo", is("ERROR_INTERNO")));
    }

    // ------------------------------------------------------------------
    // Ping de sistemas
    // ------------------------------------------------------------------

    @Test
    void losCuatroSistemasSePintanEnSuOrden() throws Exception {
        when(fachada.estadoSistemas()).thenReturn(List.of(
                new EstadoSistemaDTO("sso", true, null, 5),
                new EstadoSistemaDTO("hce", true, null, 5),
                new EstadoSistemaDTO("ris", false, "CAIDO", 40),
                new EstadoSistemaDTO("pacs", true, null, 0)));

        mvc.perform(get("/api/integracion/sistemas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(4)))
                .andExpect(jsonPath("$[0].nombre", is("sso")))
                .andExpect(jsonPath("$[2].disponible", is(false)))
                .andExpect(jsonPath("$[2].motivo", is("CAIDO")));
    }

    @Test
    void elPingDeUnSistemaCaidoCierraEn200NuncaEn5xx() throws Exception {
        when(fachada.pingSistema("ris"))
                .thenReturn(new EstadoSistemaDTO("ris", false, "CAIDO", 40));

        mvc.perform(get("/api/integracion/sistemas/ris/ping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre", is("ris")))
                .andExpect(jsonPath("$.disponible", is(false)))
                .andExpect(jsonPath("$.motivo", is("CAIDO")))
                .andExpect(jsonPath("$.latenciaMediaMs", is(40)));

        verify(fachada).pingSistema(eq("ris"));
    }

    @Test
    void unSistemaQueNoExisteCierraEn404() throws Exception {
        when(fachada.pingSistema("sap"))
                .thenThrow(new SistemaNoEncontradoException("el sistema sap no es un sistema externo conocido"));

        mvc.perform(get("/api/integracion/sistemas/sap/ping"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo", is("SISTEMA_NO_ENCONTRADO")));
    }

    // ------------------------------------------------------------------
    // Sincronizacion de historia (idempotencia)
    // ------------------------------------------------------------------

    @Test
    void sincronizarDevuelve200ConLaHuellaYElMarcajeDeReutilizacion() throws Exception {
        when(fachada.sincronizarHistoriaClinica("api-paciente-ok")).thenReturn(historia(false));

        mvc.perform(post("/api/integracion/historias/api-paciente-ok/sincronizar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pacienteExternoId", is("api-paciente-ok")))
                .andExpect(jsonPath("$.reutilizado", is(false)))
                .andExpect(jsonPath("$.registros[0].codigo", is("ENF-HTA")));

        when(fachada.sincronizarHistoriaClinica("api-paciente-ok")).thenReturn(historia(true));
        mvc.perform(post("/api/integracion/historias/api-paciente-ok/sincronizar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reutilizado", is(true)));
    }

    @Test
    void elPacienteInexistenteCierraEn404YElIdentificadorVacioEn400() throws Exception {
        when(fachada.sincronizarHistoriaClinica("quien-esos"))
                .thenThrow(new HistoriaNoEncontradaException("el paciente no consta en la HCE externa"));
        mvc.perform(post("/api/integracion/historias/quien-esos/sincronizar"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo", is("HISTORIA_NO_ENCONTRADA")));

        when(fachada.sincronizarHistoriaClinica("sin-id"))
                .thenThrow(new IllegalArgumentException("el paciente externo es obligatorio"));
        mvc.perform(post("/api/integracion/historias/sin-id/sincronizar"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo", is("DATOS_INVALIDOS")));
    }

    // ------------------------------------------------------------------
    // Baseline de CFG-00 y auditoria
    // ------------------------------------------------------------------

    @Test
    void importarEstudioMantieneElContratoDelBaseline() throws Exception {
        when(fachada.importarEstudio("ACC-0002")).thenReturn(
                new EstudioDTO("ACC-0002", "HC-1002", LocalDate.of(2025, 10, 2), 1));

        mvc.perform(get("/api/integracion/estudios/ACC-0002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessionNumber", is("ACC-0002")))
                .andExpect(jsonPath("$.pacienteId", is("HC-1002")))
                .andExpect(jsonPath("$.fechaEstudio", is("2025-10-02")))
                .andExpect(jsonPath("$.numeroSeries", is(1)));
    }

    @Test
    void elEstudioInexistenteCierraEn404() throws Exception {
        when(fachada.importarEstudio("ACC-9999"))
                .thenThrow(new EstudioNoEncontradoException("el RIS no conoce el estudio ACC-9999"));

        mvc.perform(get("/api/integracion/estudios/ACC-9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo", is("ESTUDIO_NO_ENCONTRADO")));
    }

    @Test
    void laAuditoriaDevuelveLaCadenaConSuMarcaDeIntegridad() throws Exception {
        when(fachada.estadoAuditoriaIntegracion()).thenReturn(new AuditoriaIntegracionDTO(true, 1,
                "b".repeat(64),
                List.of(new EslabonAuditoriaDTO(1L, "SSO_LOGIN", "sso", "OK", "admin",
                        Instant.parse("2026-11-15T10:00:00Z"), "c".repeat(64), "b".repeat(64)))));

        mvc.perform(get("/api/integracion/auditoria"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cadenaIntegra", is(true)))
                .andExpect(jsonPath("$.totalRegistros", is(1)))
                .andExpect(jsonPath("$.eslabones[0].secuencia", is(1)))
                .andExpect(jsonPath("$.eslabones[0].accion", is("SSO_LOGIN")))
                .andExpect(jsonPath("$.eslabones[0].instante", is("2026-11-15T10:00:00Z")));
    }
}
