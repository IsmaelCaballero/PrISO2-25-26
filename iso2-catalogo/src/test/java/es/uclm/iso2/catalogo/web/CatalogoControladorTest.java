package es.uclm.iso2.catalogo.web;

import es.uclm.iso2.common.catalogo.CatalogoConflictoException;
import es.uclm.iso2.common.catalogo.CatalogoNoEncontradoException;
import es.uclm.iso2.common.catalogo.PruebaCatalogoFacade;
import es.uclm.iso2.common.catalogo.PruebaCatalogoFacade.PoliticaDTO;
import es.uclm.iso2.common.catalogo.PruebaCatalogoFacade.PruebaImagenDTO;
import es.uclm.iso2.common.catalogo.PruebaCatalogoFacade.ProtocoloDTO;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas tecnicas del endpoint REST de catalogo (CDU16) con MockMvc standalone:
 * codigos de estado y cuerpo de error segun contrato (201/200/204/400/404/409).
 * El RBAC (401/403) se prueba en iso2-server-app, que es donde se configura la seguridad.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CatalogoControladorTest {

    @Mock
    private PruebaCatalogoFacade catalogo;

    private MockMvc mvc;

    @BeforeEach
    void montarControlador() {
        mvc = MockMvcBuilders.standaloneSetup(new CatalogoControlador(catalogo))
                .setControllerAdvice(new CatalogoManejadorErrores())
                .build();
    }

    private static PruebaImagenDTO rmCervical(boolean activa) {
        return new PruebaImagenDTO("RM-CERVICAL", "RM cervical", "RM", true, 25, activa,
                new ProtocoloDTO("ayunar 4 h", 4, "marcapasos"),
                new PoliticaDTO(24, 3));
    }

    private static final String JSON_VALIDO = """
            {"codigo":"RM-CERVICAL","nombre":"RM cervical","categoria":"RM",
             "requiereCita":true,"duracionMinutos":25,"activa":true,
             "protocolo":{"preparacion":"ayunar 4 h","ayunoHoras":4,"contraindicaciones":"marcapasos"},
             "politica":{"plazoCancelacionHoras":24,"maxCancelaciones":3}}
            """;

    @Test
    void listarDevuelve200ConElArrayDePruebas() throws Exception {
        when(catalogo.listar(any())).thenReturn(List.of(rmCervical(true)));

        mvc.perform(get("/api/catalogo/pruebas").param("pagina", "0").param("tamano", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("RM-CERVICAL"))
                .andExpect(jsonPath("$[0].protocolo.ayunoHoras").value(4));
    }

    @Test
    void listarConPaginacionInvalidaDevuelve400() throws Exception {
        mvc.perform(get("/api/catalogo/pruebas").param("tamano", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DATOS_INVALIDOS"));
    }

    @Test
    void detalleDevuelve200O404() throws Exception {
        when(catalogo.buscarPorCodigo("RM-CERVICAL")).thenReturn(Optional.of(rmCervical(true)));
        when(catalogo.buscarPorCodigo("FANTASMA")).thenReturn(Optional.empty());

        mvc.perform(get("/api/catalogo/pruebas/RM-CERVICAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activa").value(true));
        mvc.perform(get("/api/catalogo/pruebas/FANTASMA"))
                .andExpect(status().isNotFound());
    }

    @Test
    void altaValidaDevuelve201() throws Exception {
        when(catalogo.crear(any())).thenReturn(rmCervical(true));

        mvc.perform(post("/api/catalogo/pruebas")
                        .contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value("RM-CERVICAL"));
    }

    @Test
    void altaConCodigoRepetidoDevuelve409() throws Exception {
        when(catalogo.crear(any())).thenThrow(new CatalogoConflictoException("duplicado"));

        mvc.perform(post("/api/catalogo/pruebas")
                        .contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CODIGO_DUPLICADO"));
    }

    @Test
    void altaConCuerpoInvalidoDevuelve400() throws Exception {
        String jsonInvalido = JSON_VALIDO.replace("\"duracionMinutos\":25", "\"duracionMinutos\":0");

        mvc.perform(post("/api/catalogo/pruebas")
                        .contentType(MediaType.APPLICATION_JSON).content(jsonInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DATOS_INVALIDOS"));
    }

    @Test
    void modificacionDevuelve200Y404CuandoProcede() throws Exception {
        when(catalogo.actualizar(anyString(), any())).thenThrow(new CatalogoNoEncontradoException("X"));
        when(catalogo.actualizar(ArgumentMatchers.eq("RM-CERVICAL"), any())).thenReturn(rmCervical(true));

        mvc.perform(put("/api/catalogo/pruebas/RM-CERVICAL")
                        .contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
                .andExpect(status().isOk());
        mvc.perform(put("/api/catalogo/pruebas/NO-VALE")
                        .contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("PRUEBA_NO_ENCONTRADA"));
    }

    @Test
    void bajaLogicaDevuelve204() throws Exception {
        when(catalogo.desactivar("RM-CERVICAL")).thenReturn(rmCervical(false));

        mvc.perform(delete("/api/catalogo/pruebas/RM-CERVICAL"))
                .andExpect(status().isNoContent());
    }

    @Test
    void reactivacionDevuelve200() throws Exception {
        when(catalogo.reactivar("RM-CERVICAL")).thenReturn(rmCervical(true));

        mvc.perform(put("/api/catalogo/pruebas/RM-CERVICAL/reactivar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activa").value(true));
    }
}
