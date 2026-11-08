package es.uclm.iso2.solicitudes.web;

import es.uclm.iso2.common.solicitudes.Prioridad;
import es.uclm.iso2.common.solicitudes.SolicitudConflictoException;
import es.uclm.iso2.common.solicitudes.SolicitudFacade;
import es.uclm.iso2.common.solicitudes.SolicitudFacade.SolicitudDTO;
import es.uclm.iso2.common.solicitudes.SolicitudFacade.TrazabilidadSolicitudDTO;
import es.uclm.iso2.common.solicitudes.SolicitudNoEncontradaException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas tecnicas de la capa web de solicitudes (MockMvc en solitario, sin subir el
 * contexto de {@code iso2-server-app}: ahi ya existe la integracion completa). Se
 * comprueba la traduccion de excepciones a codigos HTTP estables y que el autor de la
 * prescripcion sale del token, nunca del cuerpo JSON (RF06.2).
 */
class SolicitudControladorTest {

    private SolicitudFacade fachada;
    private MockMvc mvc;

    @BeforeEach
    void montar() {
        fachada = mock(SolicitudFacade.class);
        mvc = MockMvcBuilders.standaloneSetup(new SolicitudControlador(fachada))
                .setControllerAdvice(new SolicitudManejadorErrores())
                .build();
    }

    /** Usuario simulado en la peticion (el claim sub del JWT en la app servidora). */
    private static RequestPostProcessor usuario(String nombre) {
        return request -> {
            request.setUserPrincipal(() -> nombre);
            return request;
        };
    }

    private static SolicitudDTO dto(String id) {
        return new SolicitudDTO(id, "P1", "RX01", LocalDate.of(2026, 3, 1), "creada");
    }

    @Test
    void prescribirDevuelve201ConElAutorDelToken() throws Exception {
        when(fachada.prescribir(any())).thenReturn(dto("s-1"));

        mvc.perform(post("/api/solicitudes")
                        .with(usuario("medico1"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"pacienteId":"P1","codigoPrueba":"RX01",
                                 "indicacionClinica":"dolor","prioridad":"URGENTE","origen":"AP"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.solicitudId", is("s-1")))
                .andExpect(jsonPath("$.estado", is("creada")));

        verify(fachada).prescribir(org.mockito.ArgumentMatchers.argThat(p ->
                p.medicoId().equals("medico1") && p.prioridad() == Prioridad.URGENTE));
    }

    @Test
    void prioridadDesconocidaEs400SinLlegarAlServicio() throws Exception {
        mvc.perform(post("/api/solicitudes")
                        .with(usuario("medico1"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"pacienteId":"P1","codigoPrueba":"RX01",
                                 "indicacionClinica":"dolor","prioridad":"MAXIMA","origen":"AP"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo", is("DATOS_INVALIDOS")));
    }

    @Test
    void camposEnBlancoSeTraducenA400OMalFormado() throws Exception {
        when(fachada.prescribir(any()))
                .thenThrow(new IllegalArgumentException("el campo indicacionClinica es obligatorio"));

        mvc.perform(post("/api/solicitudes")
                        .with(usuario("medico1"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"pacienteId":"P1","codigoPrueba":"RX01",
                                 "indicacionClinica":" ","prioridad":"ORDINARIA","origen":"AP"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo", is("DATOS_INVALIDOS")));

        mvc.perform(post("/api/solicitudes").with(usuario("medico1"))
                        .contentType(MediaType.APPLICATION_JSON).content("{ no es json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo", is("DATOS_INVALIDOS")));
    }

    @Test
    void conflictosDelCdu06Devuelven409ConCodigoEstable() throws Exception {
        when(fachada.prescribir(any()))
                .thenThrow(new SolicitudConflictoException(
                        SolicitudConflictoException.Conflicto.DUPLICADA, "duplicada"))
                .thenThrow(new SolicitudConflictoException(
                        SolicitudConflictoException.Conflicto.PRUEBA_NO_DISPONIBLE, "no disponible"));

        String cuerpo = """
                {"pacienteId":"P1","codigoPrueba":"RX01",
                 "indicacionClinica":"dolor","prioridad":"ORDINARIA","origen":"AP"}""";
        mvc.perform(post("/api/solicitudes").with(usuario("medico1"))
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo", is("CONFLICTO_SOLICITUD_DUPLICADA")));

        mvc.perform(post("/api/solicitudes").with(usuario("medico1"))
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo", is("CONFLICTO_PRUEBA_NO_DISPONIBLE")));
    }

    @Test
    void listadoMiasUsaAlMedicoDelToken() throws Exception {
        when(fachada.listarPorMedico("medico1")).thenReturn(List.of(dto("s-1"), dto("s-2")));

        mvc.perform(get("/api/solicitudes/mias").with(usuario("medico1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(2)))
                .andExpect(jsonPath("$[0].solicitudId", is("s-1")));

        verify(fachada).listarPorMedico(eq("medico1"));
    }

    @Test
    void detalleDevuelve200O404() throws Exception {
        when(fachada.detalle("s-1")).thenReturn(dto("s-1"));
        when(fachada.detalle("roto")).thenThrow(new SolicitudNoEncontradaException("no existe"));

        mvc.perform(get("/api/solicitudes/s-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigoPrueba", is("RX01")));

        mvc.perform(get("/api/solicitudes/roto"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo", is("SOLICITUD_NO_ENCONTRADA")));
    }

    @Test
    void integridadDevuelveTrazabilidadConBandera() throws Exception {
        when(fachada.trazable("s-1")).thenReturn(new TrazabilidadSolicitudDTO(
                "s-1", "medico1", Instant.parse("2026-03-01T09:00:00Z"), "ab".repeat(32), false));

        mvc.perform(get("/api/solicitudes/s-1/integridad"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.autor", is("medico1"))
                )
                .andExpect(jsonPath("$.integridadOk", is(false)));
    }

    @Test
    void unFalloInesperadoTerminaEn500ConCodigoInterno() throws Exception {
        when(fachada.detalle("x1")).thenThrow(new IllegalStateException("bdb caida"));

        mvc.perform(get("/api/solicitudes/x1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.codigo", is("ERROR_INTERNO")));
    }
}
