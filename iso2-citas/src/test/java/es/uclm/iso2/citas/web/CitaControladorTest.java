package es.uclm.iso2.citas.web;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import es.uclm.iso2.citas.dominio.CitaHueco;
import es.uclm.iso2.citas.web.CitaControlador.CancelacionRequest;
import es.uclm.iso2.citas.web.CitaControlador.ReprogramacionRequest;
import es.uclm.iso2.common.citas.CitaConflictoException;
import es.uclm.iso2.common.citas.CitaConflictoException.Conflicto;
import es.uclm.iso2.common.citas.CitaFacade;
import es.uclm.iso2.common.citas.CitaFacade.CitaDTO;
import es.uclm.iso2.common.citas.CitaFacade.CitaNuevaDTO;
import es.uclm.iso2.common.citas.CitaFacade.DetalleCitaDTO;
import es.uclm.iso2.common.citas.CitaFacade.HuecoDTO;
import es.uclm.iso2.common.citas.CitaNoEncontradaException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas tecnicas de la capa web de citas (MockMvc en solitario, sin subir la app
 * servidora): contrato de errores con codigos HTTP estables segun la matriz de calidad
 * de CFG-05, enlace de los filtros de la pantalla de recepcion y cuerpos opcionales del
 * lote de expiracion.
 */
class CitaControladorTest {

    private static final LocalDateTime INICIO = LocalDateTime.parse("2026-11-13T09:00:00");
    private static final LocalDateTime FIN = LocalDateTime.parse("2026-11-13T09:30:00");

    private CitaFacade fachada;
    private MockMvc mvc;

    @BeforeEach
    void montar() {
        fachada = mock(CitaFacade.class);
        // El conversor explicito fija el mismo formato de fechas que usa la aplicacion
        // servidora (ISO-8601, no arrays de numeros): sin el, MockMvc en solitario saca
        // [2026,11,13,9,0] y el contrato de la pantalla de recepcion se comprueba mal.
        JsonMapper mapaFechas = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
        mvc = MockMvcBuilders.standaloneSetup(new CitaControlador(fachada))
                .setControllerAdvice(new CitaManejadorErrores())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(mapaFechas))
                .build();
    }

    private static CitaDTO cita(String estado) {
        return new CitaDTO("c-1", null, INICIO, FIN, estado);
    }

    private static DetalleCitaDTO detalle() {
        return new DetalleCitaDTO("c-1", "s-1", "api-medico", INICIO, FIN, "programada", null, 0L);
    }

    // ------------------------------------------------------------------
    // Alta y conflictos
    // ------------------------------------------------------------------

    @Test
    void agendarDevuelve201ConLaCitaProgramada() throws Exception {
        when(fachada.agendar(any())).thenReturn(cita("programada"));

        mvc.perform(post("/api/citas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"solicitudId":null,"profesionalId":"api-medico",
                                 "inicio":"2026-11-13T09:00:00","duracionMinutos":30}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.citaId", is("c-1")))
                .andExpect(jsonPath("$.estado", is("programada")))
                .andExpect(jsonPath("$.inicio", is("2026-11-13T09:00:00")));

        verify(fachada).agendar(new CitaNuevaDTO(null, "api-medico", INICIO, 30));
    }

    @Test
    void cadaConflictoDelCdu12TieneSuCodigo409Estable() throws Exception {
        Map<Conflicto, String> codigos = new EnumMap<>(Conflicto.class);
        codigos.put(Conflicto.HUECO_OCUPADO, "CONFLICTO_HUECO_OCUPADO");
        codigos.put(Conflicto.FUERA_DE_FRANJA, "CONFLICTO_FUERA_DE_FRANJA");
        codigos.put(Conflicto.TRANSICION_INVALIDA, "CONFLICTO_TRANSICION_INVALIDA");
        codigos.put(Conflicto.SOLICITUD_NO_APTA, "CONFLICTO_SOLICITUD_NO_APTA");
        codigos.put(Conflicto.PLAZO_CANCELACION, "CONFLICTO_PLAZO_CANCELACION");

        // Se recorre el enum entero: si manana se anade un conflicto, el test lo reclama.
        // doThrow evita que el stub vuelva a invocar agendar() sobre el mock del turno
        // anterior y salga disparada la excepcion del bucle en el propio when().
        assertEquals(5, Conflicto.values().length);
        for (Map.Entry<Conflicto, String> entrada : codigos.entrySet()) {
            doThrow(new CitaConflictoException(entrada.getKey(), "conflicto de prueba"))
                    .when(fachada).agendar(any());

            mvc.perform(post("/api/citas")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"profesionalId":"api-medico","inicio":"2026-11-13T09:00:00",
                                     "duracionMinutos":30}"""))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.codigo", is(entrada.getValue())));
        }
    }

    // ------------------------------------------------------------------
    // Errores del contrato
    // ------------------------------------------------------------------

    @Test
    void citaInexistenteEs404EnCualquierOperacion() throws Exception {
        when(fachada.detalle("roto")).thenThrow(new CitaNoEncontradaException("no existe c-9"));
        when(fachada.confirmar("roto")).thenThrow(new CitaNoEncontradaException("no existe c-9"));

        mvc.perform(get("/api/citas/roto"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo", is("CITA_NO_ENCONTRADA")));

        mvc.perform(post("/api/citas/roto/confirmar"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo", is("CITA_NO_ENCONTRADA")));
    }

    @Test
    void datosInvalidosYJsonIlegibleSon400() throws Exception {
        when(fachada.cancelar(eq("c-1"), any()))
                .thenThrow(new IllegalArgumentException("la cancelacion necesita un motivo"));

        mvc.perform(post("/api/citas/c-1/cancelar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo", is("DATOS_INVALIDOS")));

        // Cuerpo imposible de leer: el manejador de HttpMessageNotReadable lo traduce igual.
        mvc.perform(post("/api/citas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ no es json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo", is("DATOS_INVALIDOS")));
    }

    @Test
    void concurrenciaEnLaBaseDeDatosTambienEs409DeHueco() throws Exception {
        // La decision final la toma la base de datos (D36): si el fallo de integridad o de
        // version llega aqui sin transaccion que lo envuelva, recepcion tiene que ver el
        // mismo CONFLICTO_HUECO_OCUPADO que con la comprobacion previa.
        when(fachada.agendar(any()))
                .thenThrow(new DataIntegrityViolationException("uniq (profesional_id, inicio)"));
        mvc.perform(post("/api/citas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"profesionalId":"api-medico","inicio":"2026-11-13T09:00:00",
                                 "duracionMinutos":30}"""))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo", is("CONFLICTO_HUECO_OCUPADO")));

        when(fachada.reprogramar(eq("c-1"), any()))
                .thenThrow(new ObjectOptimisticLockingFailureException(CitaHueco.class, 1L));
        mvc.perform(post("/api/citas/c-1/reprogramar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"inicio\":\"2026-11-13T13:00:00\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo", is("CONFLICTO_HUECO_OCUPADO")));
    }

    @Test
    void falloInesperadoTerminaEn500ConCodigoInterno() throws Exception {
        when(fachada.detalle("x1")).thenThrow(new IllegalStateException("bdb caida"));

        mvc.perform(get("/api/citas/x1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.codigo", is("ERROR_INTERNO")));
    }

    // ------------------------------------------------------------------
    // Bindings de la pantalla de recepcion
    // ------------------------------------------------------------------

    @Test
    void listarEnlazaLosFiltrosIsoDeRecepcion() throws Exception {
        when(fachada.listar(any(), any(), any(), any())).thenReturn(List.of(detalle()));

        mvc.perform(get("/api/citas")
                        .param("desde", "2026-11-12T00:00:00")
                        .param("hasta", "2026-11-14T00:00:00")
                        .param("profesionalId", "api-medico")
                        .param("estado", "programada"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].citaId", is("c-1")))
                .andExpect(jsonPath("$[0].version", is(0)));

        verify(fachada).listar(
                LocalDateTime.parse("2026-11-12T00:00:00"),
                LocalDateTime.parse("2026-11-14T00:00:00"),
                "api-medico", "programada");

        // Sin filtros: la fachada recibe cuatro nulos y el servicio decide su ventana.
        mvc.perform(get("/api/citas")).andExpect(status().isOk());
        verify(fachada).listar(null, null, null, null);
    }

    @Test
    void huecosEnlazaRangoIsoDeDiasYLaDuracionOpcional() throws Exception {
        when(fachada.huecosLibres(any(), any(), any(), any()))
                .thenReturn(List.of(new HuecoDTO("api-medico", INICIO, FIN, null)));

        mvc.perform(get("/api/citas/huecos")
                        .param("profesionalId", "api-medico")
                        .param("desde", "2026-11-13")
                        .param("hasta", "2026-11-17"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].inicio", is("2026-11-13T09:00:00")));

        verify(fachada).huecosLibres("api-medico", LocalDate.parse("2026-11-13"),
                LocalDate.parse("2026-11-17"), null);

        mvc.perform(get("/api/citas/huecos")
                        .param("profesionalId", "api-medico")
                        .param("desde", "2026-11-13")
                        .param("hasta", "2026-11-17")
                        .param("duracionMinutos", "240"))
                .andExpect(status().isOk());

        verify(fachada).huecosLibres("api-medico", LocalDate.parse("2026-11-13"),
                LocalDate.parse("2026-11-17"), 240);
    }

    // ------------------------------------------------------------------
    // Transiciones y lote
    // ------------------------------------------------------------------

    @Test
    void confirmarYRealizarNoNecesitanCuerpo() throws Exception {
        when(fachada.confirmar("c-1")).thenReturn(cita("confirmada"));
        when(fachada.realizar("c-1")).thenReturn(cita("realizada"));

        mvc.perform(post("/api/citas/c-1/confirmar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("confirmada")));
        mvc.perform(post("/api/citas/c-1/realizar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("realizada")));

        verify(fachada).confirmar("c-1");
        verify(fachada).realizar("c-1");
    }

    @Test
    void reprogramarYCancelarTomanSuCuerpoDeRecord() throws Exception {
        when(fachada.reprogramar(any(), any())).thenReturn(cita("programada"));
        when(fachada.cancelar(any(), any())).thenReturn(cita("cancelada"));

        mvc.perform(post("/api/citas/c-1/reprogramar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"inicio\":\"2026-11-13T13:00:00\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("programada")));

        mvc.perform(post("/api/citas/c-1/cancelar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"el paciente avisa\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("cancelada")));

        verify(fachada).reprogramar("c-1", LocalDateTime.parse("2026-11-13T13:00:00"));
        verify(fachada).cancelar("c-1", "el paciente avisa");
    }

    @Test
    void expirarVencidasAceptaCorteCuerpoVacioOSinCuerpo() throws Exception {
        when(fachada.expirarVencidas(any())).thenReturn(List.of(cita("expirada")));

        mvc.perform(post("/api/citas/expirar-vencidas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"corte\":\"2026-11-13T12:00:00\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estado", is("expirada")));

        mvc.perform(post("/api/citas/expirar-vencidas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());

        mvc.perform(post("/api/citas/expirar-vencidas"))
                .andExpect(status().isOk());

        ArgumentCaptor<LocalDateTime> corte = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(fachada, times(3)).expirarVencidas(corte.capture());
        // Arrays.asList porque List.of no admite nulos y dos de los tres cortes son nulos.
        assertEquals(Arrays.asList(LocalDateTime.parse("2026-11-13T12:00:00"), null, null),
                corte.getAllValues());
    }
}
