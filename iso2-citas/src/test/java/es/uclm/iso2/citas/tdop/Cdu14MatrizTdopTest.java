package es.uclm.iso2.citas.tdop;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.uclm.iso2.citas.dominio.AuditoriaEspera;
import es.uclm.iso2.citas.dominio.ListaEspera;
import es.uclm.iso2.citas.repositorio.AuditoriaCitasRepositorio;
import es.uclm.iso2.citas.repositorio.AuditoriaEsperaRepositorio;
import es.uclm.iso2.citas.repositorio.CitaHuecoRepositorio;
import es.uclm.iso2.citas.repositorio.CitaRepositorio;
import es.uclm.iso2.citas.repositorio.ListaEsperaRepositorio;
import es.uclm.iso2.citas.servicio.PruebasCitas;
import es.uclm.iso2.common.solicitudes.SolicitudFacade;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * Ejecutor 1:1 de las matrices TDOP de CDU14 (CFG-07, Iteracion 7). Lee los nueve CSVs
 * de {@code src/test/resources/tdop/} generados por {@code tools/pairwise.py} desde la
 * spec canonica {@code docs/test/iter07/tdop-cdu14-espec.json} (D33: los CSV no se tocan
 * a mano; si un caso pone rojo se arregla el codigo o la spec, nunca la matriz).
 *
 * <p>Montura igual que {@code Cdu12MatrizTdopTest}: Spring Boot con MockMvc contra el
 * H2 en memoria, sin security (el RBAC de la puerta CFG-06 se comprueba en iso2-server).
 * Cada fila empieza con el mundo limpio; el sembrado entra por la propia API HTTP, y los
 * simbolos A/B/C/INEXISTENTE/MAL_FORMATADO del CSV se resuelven a los ids reales de esa
 * corrida. Los esperados simbolicos (CREADO, ALTA_DUPLICADA...) se traducen a status y
 * codigo con {@code cdu14-esperados.csv}.</p>
 *
 * @since Iter7 (CFG-07, CDU14)
 */
@SpringBootTest(classes = Cdu14MatrizTdopTest.ConfigTdopWeb.class)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class Cdu14MatrizTdopTest {

    private static final String BASE = "/api/listas-espera";
    private static final String SALTOS = "\r\n|\n|\r";

    // ------------------------------------------------------------------
    // Montura: solo el mundo citas, sin security (igual que CDU12)
    // ------------------------------------------------------------------

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @ComponentScan(basePackages = "es.uclm.iso2.citas.web")
    @EntityScan(basePackages = "es.uclm.iso2.citas.dominio")
    @EnableJpaRepositories(basePackages = "es.uclm.iso2.citas.repositorio")
    @Import(PruebasCitas.class)
    static class ConfigTdopWeb {
    }

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;
    @Autowired private ListaEsperaRepositorio esperas;
    @Autowired private AuditoriaEsperaRepositorio auditoriasEspera;
    @Autowired private AuditoriaCitasRepositorio auditoriasCitas;
    @Autowired private CitaRepositorio citas;
    @Autowired private CitaHuecoRepositorio huecos;
    @Autowired private SolicitudFacade solicitudes;

    /** Simbolo -> esperaId real sembrado en la corrida de la fila actual. */
    private final Map<String, String> idsSembrados = new LinkedHashMap<>();

    /** Tabla canonico -> (HTTP, codigo) leida de cdu14-esperados.csv. */
    private static final Map<String, Esperado> ESPERADOS = leerEsperados();

    @BeforeEach
    void mundoLimpio() {
        auditoriasEspera.deleteAllInBatch();
        auditoriasCitas.deleteAllInBatch();
        citas.deleteAllInBatch();
        huecos.deleteAllInBatch();
        esperas.deleteAllInBatch();
        idsSembrados.clear();
        ((PruebasCitas.SolicitudesFalsas) solicitudes).reiniciar();
    }

    // ------------------------------------------------------------------
    // Lectura de matrices (mismo patron que Cdu12MatrizTdopTest)
    // ------------------------------------------------------------------

    private record Fila(Map<String, String> celdas) {

        String get(String columna) {
            String v = celdas.get(columna);
            return v == null ? "" : v;
        }

        String id() {
            return celdas.getOrDefault("id", "?");
        }
    }

    private static List<String> lineasCsv(String recurso) {
        try (InputStream in = Cdu14MatrizTdopTest.class.getClassLoader()
                .getResourceAsStream("tdop/" + recurso)) {
            if (in == null) {
                throw new IllegalStateException("falta la matriz tdop/" + recurso);
            }
            String texto = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            List<String> lineas = new ArrayList<>();
            for (String linea : texto.split(SALTOS)) {
                if (!linea.isBlank()) {
                    lineas.add(linea);
                }
            }
            return lineas;
        } catch (java.io.IOException e) {
            throw new IllegalStateException("no se pudo leer tdop/" + recurso, e);
        }
    }

    private static Stream<Fila> filas(String recurso) {
        List<String> lineas = lineasCsv(recurso);
        String[] cabecera = lineas.get(0).split(";", -1);
        return lineas.subList(1, lineas.size()).stream().map(linea -> {
            String[] c = linea.split(";", -1);
            Map<String, String> m = new LinkedHashMap<>();
            for (int i = 0; i < cabecera.length; i++) {
                m.put(cabecera[i].trim(), i < c.length ? c[i].trim() : "");
            }
            return new Fila(m);
        });
    }

    /** Un renglon de cdu14-esperados.csv: estado simbolico -> HTTP + codigo RFC 7807. */
    private record Esperado(int http, String codigoError) {
    }

    private static Map<String, Esperado> leerEsperados() {
        Map<String, Esperado> mapa = new LinkedHashMap<>();
        List<String> lineas = lineasCsv("cdu14-esperados.csv");
        for (String linea : lineas.subList(1, lineas.size())) {
            String[] c = linea.split(";", -1);
            String codigo = c[2].trim();
            mapa.put(c[0].trim(), new Esperado(Integer.parseInt(c[1].trim()),
                    "-".equals(codigo) ? null : codigo));
        }
        return mapa;
    }

    /** Codigo RFC 7807 que la tabla canonica asigna al esperado simbolico (null si no lo hay). */
    private static String codigoEsperado(String esperado) {
        Esperado e = ESPERADOS.get(esperado);
        return e == null ? null : e.codigoError();
    }

    /** Status HTTP que la tabla canonica asigna al esperado simbolico. */
    private static int statusEsperado(String esperado) {
        Esperado e = ESPERADOS.get(esperado);
        if (e == null) {
            throw new IllegalStateException("esperado sin mapa de status: " + esperado);
        }
        return e.http();
    }

    private void verificar(Fila f, MvcResult r) throws Exception {
        int esperadoStatus = statusEsperado(f.get("esperadoStatus"));
        String esperadoCodigo = codigoEsperado(f.get("esperadoStatus"));
        int real = r.getResponse().getStatus();
        assertThat(real)
                .as("%s esperaba HTTP %d, respuesta: %s", f.id(), esperadoStatus,
                        r.getResponse().getContentAsString())
                .isEqualTo(esperadoStatus);
        if (esperadoCodigo == null) {
            assertThat(f.get("esperadoCodigo"))
                    .as("%s: %s no lleva codigo de error en la tabla canonica", f.id(),
                            f.get("esperadoStatus"))
                    .isEqualTo("-");
        } else {
            JsonNode cuerpo = cuerpo(r);
            assertThat(cuerpo.path("codigo").asText())
                    .as("%s esperaba codigo %s", f.id(), esperadoCodigo)
                    .isEqualTo(esperadoCodigo);
        }
    }

    private JsonNode cuerpo(MvcResult r) throws Exception {
        return mapper.readTree(r.getResponse().getContentAsString());
    }

    private MvcResult req(MockHttpServletRequestBuilder peticion) throws Exception {
        return mvc.perform(peticion).andReturn();
    }

    // ------------------------------------------------------------------
    // Tokens de la matriz -> valores reales del mundo de pruebas
    // ------------------------------------------------------------------

    private String token(String nivel) {
        return switch (nivel) {
            case "CONOCIDA", "PRUEBA", "PRUEBA_CONOCIDA" -> PruebasCitas.PRUEBA;
            case "OTRA", "OTRA_PRUEBA", "PRUEBA_DUP" -> PruebasCitas.PRUEBA_DUP;
            case "NO_EXISTE", "DESCONOCIDA", "NO_ENCONTRADA", "INVENTADA" -> "api-prueba-que-no-existe";
            case "MEDICO", "CON_AGENDA", "PROF_MEDICO" -> PruebasCitas.PROF_MEDICO;
            case "SIN_AGENDA", "SIN_CITA", "OTRO_PROF" -> PruebasCitas.PROF_SIN_AGENDA;
            case "CREADA" -> PruebasCitas.SOL_CREADA;
            case "CITADA" -> PruebasCitas.SOL_CITADA;
            case "PRESCRITA" -> PruebasCitas.SOL_PRESCRITA;
            case "INEXISTENTE", "NO_ENCONTRADA_SOL" -> PruebasCitas.SOL_INEXISTENTE;
            case "MAX" -> "X".repeat(40);
            case "LARGO" -> "X".repeat(41);
            case "MAX300" -> "m".repeat(300);
            case "LARGO301" -> "m".repeat(301);
            case "ESPACIOS" -> "   ";
            case "VACIO", "VACIA" -> "";
            case "NINGUNA", "AUSENTE", "IGUAL", "-", "SIN_HUECO", "SIN_ESTADO", "SIN_FILTRO" -> null;
            case "INVENTADO", "INVENTADA_ESTADO" -> "esperando";
            case "MINUSCULA" -> null; // se resuelve en cada tabla (activa/urgente)
            case "DOMINGO" -> "2026-11-15";
            case "APERTURA_JUSTA" -> "2026-11-13T08:00";
            case "CIERRE_LIMITRO" -> "2026-11-13T14:30";
            case "PASADA_CIERRE" -> "2026-11-13T15:00";
            case "AUT", "AUTORIZADO" -> null; // el token de autorizacion no va en el cuerpo
            default -> nivel;
        };
    }

    // ------------------------------------------------------------------
    // Tabla B (cdu14-alta.csv): alta en la lista de espera
    // ------------------------------------------------------------------

    static java.util.stream.Stream<Fila> filasAlta() {
        return filas("cdu14-alta.csv");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("filasAlta")
    void altaDesdeMatriz(Fila f) throws Exception {
        aplicarSetup(f.get("setup"));
        String paciente = pacienteAlta(f);
        String prueba = pruebaAlta(f);
        String prioridad = prioridadAlta(f);
        String solicitud = solicitudAlta(f);
        MockHttpServletRequestBuilder peticion = post(BASE).contentType(MediaType.APPLICATION_JSON);
        if (!"AUSENTE".equals(f.get("cuerpo"))) {
            Map<String, Object> cuerpo = new LinkedHashMap<>();
            if (solicitud != null) {
                cuerpo.put("solicitudId", solicitud);
            }
            cuerpo.put("pacienteId", paciente);
            cuerpo.put("pruebaCodigo", prueba);
            if (prioridad != null) {
                cuerpo.put("prioridad", prioridad);
            }
            peticion.content(mapper.writeValueAsString(cuerpo));
        }
        MvcResult r = req(peticion);
        verificar(f, r);
    }

    private String pacienteAlta(Fila f) {
        return switch (f.get("paciente")) {
            case "VACIO" -> "";
            case "ESPACIOS" -> "   ";
            case "MAX" -> "P".repeat(60);
            case "LARGO" -> "P".repeat(61);
            default -> "api-paciente";
        };
    }

    private String pruebaAlta(Fila f) {
        return switch (f.get("prueba")) {
            case "VACIO" -> "";
            case "MAX" -> "c".repeat(60);
            case "LARGO" -> "c".repeat(61);
            default -> token(f.get("prueba"));
        };
    }

    private String prioridadAlta(Fila f) {
        String p = f.get("prioridad");
        if ("AUSENTE".equals(p)) {
            return null;
        }
        return "MINUSCULA".equals(p) ? "urgente" : p;
    }

    private String solicitudAlta(Fila f) {
        String s = f.get("solicitud");
        if ("NINGUNA".equals(s)) {
            return null;
        }
        if ("MAL_FORMATADO".equals(s)) {
            return "no-es-un-uuid";
        }
        return token(s);
    }
}
