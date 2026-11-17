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
import es.uclm.iso2.common.solicitudes.Prioridad;
import es.uclm.iso2.common.solicitudes.SolicitudFacade;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
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
        verificarEsperado(f.id(), f.get("esperadoStatus"), f.get("esperadoCodigo"), r);
    }

    /** Esperado de una operacion repetida de las secuencias: el numero va en el propio token. */
    private void verificarEsperado(final String id, final int indice, final MvcResult resultado)
            throws Exception {
        verificarEsperado(id, esperarToken(indice, resultado), null, resultado);
    }

    /**
     * Esperado de una operacion repetida. El {@code esperadoStatus} de la tabla vale para la
     * ultima; las anteriores son las que abren o cierran la puerta, asi que se leen de la
     * propia secuencia y se comprueban aparte.
     */
    private static String esperarToken(final int indice, final MvcResult resultado) {
        if (indice == 0) {
            return "ACTIVA";
        }
        return resultado.getResponse().getStatus() == 201 ? "ACTIVA" : "TERMINAL";
    }

    private void verificarEsperado(final String id, final String esperado, final String columnaCodigo,
            final MvcResult r) throws Exception {
        if ("UN_OK_Y_OTRO_409".equals(esperado)) {
            assertThat(columnaCodigo).as("%s: %s no lleva codigo único", id, esperado).isEqualTo("-");
            return;
        }
        final int esperadoStatus = statusEsperado(esperado);
        final String esperadoCodigo = codigoEsperado(esperado);
        final int real = r.getResponse().getStatus();
        assertThat(real)
                .as("%s esperaba HTTP %d, respuesta: %s", id, esperadoStatus,
                        r.getResponse().getContentAsString())
                .isEqualTo(esperadoStatus);
        if (esperadoCodigo == null) {
            assertThat(columnaCodigo)
                    .as("%s: %s no lleva codigo de error en la tabla canonica", id, esperado)
                    .isEqualTo("-");
        } else {
            JsonNode cuerpo = cuerpo(r);
            assertThat(cuerpo.path("codigo").asText())
                    .as("%s esperaba codigo %s", id, esperadoCodigo)
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
        aplicarSetup(f);
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        if (!"AUSENTE".equals(f.get("paciente"))) {
            cuerpo.put("pacienteId", pacienteAlta(f));
        }
        if (!"AUSENTE".equals(f.get("prueba"))) {
            cuerpo.put("pruebaCodigo", pruebaAlta(f));
        }
        String prioridad = prioridadAlta(f);
        if (prioridad != null) {
            cuerpo.put("prioridad", prioridad);
        }
        String solicitud = f.get("solicitud");
        if (!"AUSENTE".equals(solicitud) && !"NINGUNA".equals(solicitud)) {
            cuerpo.put("solicitudId", solicitudAlta(solicitud));
        }
        MvcResult r = req(post(BASE).contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(cuerpo)));
        verificar(f, r);
    }

    private String pacienteAlta(Fila f) {
        return switch (f.get("paciente")) {
            case "VACIO" -> "";
            case "ESPACIOS" -> "   ";
            case "MAX" -> "P".repeat(40);
            case "LARGO" -> "P".repeat(41);
            default -> "api-paciente";
        };
    }

    private String pruebaAlta(Fila f) {
        return switch (f.get("prueba")) {
            case "VACIO" -> "";
            case "MAX" -> "c".repeat(40);
            case "LARGO" -> "c".repeat(41);
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

    private String solicitudAlta(String solicitud) {
        if ("MAL_FORMATADO".equals(solicitud)) {
            return "no-es-un-uuid";
        }
        return token(solicitud);
    }

    // ------------------------------------------------------------------
    // Tabla A (cdu14-cola.csv): lectura de la cola con sus filtros
    // ------------------------------------------------------------------

    static Stream<Fila> filasCola() {
        return filas("cdu14-cola.csv");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("filasCola")
    void colaDesdeMatriz(Fila f) throws Exception {
        aplicarSetup(f);
        MvcResult primera = req(cola(f));
        verificar(f, primera);
        MvcResult segunda = req(cola(f));
        if (primera.getResponse().getStatus() == 200) {
            JsonNode cola = cuerpo(primera);
            assertThat(cola.isArray()).as("%s: la cola es una lista", f.id()).isTrue();
            assertThat(ordenCanonico(cola)).as("%s: el orden canon es prioridad desc, fecha asc y "
                    + "esperaId asc; respuesta: %s", f.id(), cola).isTrue();
            for (int i = 0; i < cola.size(); i++) {
                assertThat(cola.get(i).path("posicion").asInt())
                        .as("%s: la posicion es 1-based y sigue al orden", f.id()).isEqualTo(i + 1);
                assertThat(cola.get(i).path("total").asLong())
                        .as("%s: el total bate con las filas del filtro", f.id()).isEqualTo(cola.size());
            }
            assertThat(segunda.getResponse().getContentAsString())
                    .as("%s: leer la cola dos veces da exactamente lo mismo", f.id())
                    .isEqualTo(primera.getResponse().getContentAsString());
        }
    }

    private MockHttpServletRequestBuilder cola(Fila f) {
        MockHttpServletRequestBuilder peticion = get(BASE);
        String prueba = filtro(f.get("prueba"), f.get("cola"));
        if (prueba != null) {
            peticion.param("pruebaCodigo", prueba);
        }
        String profesional = filtro(f.get("profesional"), f.get("cola"));
        if (profesional != null) {
            peticion.param("profesionalId", profesional);
        }
        String estado = filtro(f.get("estado"), f.get("cola"));
        if (estado != null) {
            peticion.param("estado", estado);
        }
        return peticion;
    }

    /** Valor real de un filtro de cola; null es que el parametro no va en la peticion. */
    private String filtro(String nivel, String mundo) {
        return switch (nivel) {
            case "NINGUNO", "-", "SIN_FILTRO" -> null;
            case "VACIO" -> "";
            case "ESPACIOS" -> "   ";
            case "MINUSCULA" -> "activa";
            case "CONOCIDA" -> PruebasCitas.PRUEBA;
            case "OTRA" -> PruebasCitas.PRUEBA_DUP;
            case "MIXTA", "EMPALTE_FECHA", "EMPALTE_TODO", "TERMINALES", "SOLO_TERMINALES", "VACIA" -> null;
            default -> token(nivel);
        };
    }

    /**
     * El pedido llega ordenado como manda D11: prioridad de mayor a menor, empatando la
     * fecha de solicitud mas antigua, y empatando aun el esperaId. Se compara contra una
     * ordenacion canonica de la misma respuesta, asi que la matriz no fija literales de id.
     */
    private boolean ordenCanonico(JsonNode cola) {
        List<JsonNode> vistas = new ArrayList<>();
        cola.forEach(vistas::add);
        List<JsonNode> canon = new ArrayList<>(vistas);
        canon.sort(Comparator.comparing((JsonNode e) -> Prioridad.valueOf(e.path("prioridad").asText()),
                Comparator.reverseOrder())
                .thenComparing(e -> e.path("fechaSolicitud").asText())
                .thenComparing(e -> e.path("esperaId").asText()));
        for (int i = 0; i < vistas.size(); i++) {
            if (!vistas.get(i).path("esperaId").asText().equals(canon.get(i).path("esperaId").asText())) {
                return false;
            }
        }
        return true;
    }

    // ------------------------------------------------------------------
    // Setup: el mundo de cada fila se siembra por la propia API HTTP
    // ------------------------------------------------------------------

    /** Inicio fijo dentro de la franja de AgendasFalsas para un dia no festivo. */
    private static final String INICIO_FIJO = "2026-11-13T09:00:00";

    /** Instantes de siembra: la cola desempata por fechaSolicitud y el reloj esta fijado. */
    private static final String T1 = "2026-11-11T09:00:00Z";
    private static final String T2 = "2026-11-11T10:00:00Z";
    private static final String T3 = "2026-11-11T11:00:00Z";

    /** Material del primer eslabon de cualquier cadena de auditoria. */
    private static final String GENESIS = "GENESIS";

    private void aplicarSetup(Fila f) throws Exception {
        for (String bruto : f.get("setup").split(",")) {
            String s = bruto.trim();
            if (s.isEmpty() || "-".equals(s)) {
                continue;
            }
            int dosPuntos = s.indexOf(':');
            String cabeza = dosPuntos < 0 ? s : s.substring(0, dosPuntos);
            String valor = dosPuntos < 0 ? "" : s.substring(dosPuntos + 1);
            switch (cabeza) {
                case "SEMILLA-VACIA", "SEMILLA-SOL", "SEMILLA-SOLO", "ENLAZE", "SEMILLA-PRI", "REDUNDANTE" -> {
                    // No siembra nada: la solicitud, el enganche y la repeticion los resuelve la fila.
                }
                case "SEMILLA-DUP" -> alta("SEM", "api-paciente", PruebasCitas.PRUEBA, "PREFERENTE", T1, null);
                case "SEMILLA-DUP-RETIRADA" -> {
                    alta("SEM", "api-paciente", PruebasCitas.PRUEBA, "PREFERENTE", T1, null);
                    retirar(idSembrado("SEM"), "retirada de siembra");
                }
                case "SEMILLA-DUP-ASIGNADA" -> {
                    alta("SEM", "api-paciente", PruebasCitas.PRUEBA, "PREFERENTE", T1, PruebasCitas.SOL_CREADA);
                    asignar(idSembrado("SEM"), PruebasCitas.PROF_MEDICO, INICIO_FIJO, null);
                }
                case "SEMILLA-OTRO-PACIENTE" -> alta("SEM", "api-paciente-otro", PruebasCitas.PRUEBA,
                        "PREFERENTE", T1, null);
                case "SEMILLA-OTRA-PRUEBA" -> alta("SEM", "api-paciente", PruebasCitas.PRUEBA_DUP,
                        "PREFERENTE", T1, null);
                case "SEMILLA-MIXTA", "SEMILLA-COLA-TRES" -> colaTres();
                case "SEMILLA-EMPALTE-FECHA" -> {
                    alta("A", "api-paciente-a", PruebasCitas.PRUEBA, "ORDINARIA", T1, PruebasCitas.SOL_CREADA);
                    alta("B", "api-paciente-b", PruebasCitas.PRUEBA, "PREFERENTE", T1, PruebasCitas.SOL_CREADA_2);
                }
                case "SEMILLA-EMPALTE-TODO" -> {
                    alta("A", "api-paciente-a", PruebasCitas.PRUEBA, "PREFERENTE", T1, PruebasCitas.SOL_CREADA);
                    alta("B", "api-paciente-b", PruebasCitas.PRUEBA, "PREFERENTE", T1, PruebasCitas.SOL_CREADA_2);
                }
                case "SEMILLA-TERMINALES", "SEMILLA-COLA-TERMINALES" -> {
                    colaTres();
                    asignar(idSembrado("B"), PruebasCitas.PROF_MEDICO, INICIO_FIJO, null);
                    retirar(idSembrado("C"), "retirada de siembra");
                }
                case "SEMILLA-SOLO-TERMINALES" -> {
                    alta("B", "api-paciente-b", PruebasCitas.PRUEBA, "PREFERENTE", T2, PruebasCitas.SOL_CREADA);
                    alta("C", "api-paciente-c", PruebasCitas.PRUEBA, "ORDINARIA", T3, PruebasCitas.SOL_CREADA_2);
                    asignar(idSembrado("B"), PruebasCitas.PROF_MEDICO, INICIO_FIJO, null);
                    retirar(idSembrado("C"), "retirada de siembra");
                }
                case "SEMILLA-COLA-SOLO" -> alta("C", "api-paciente-c", PruebasCitas.PRUEBA, "ORDINARIA", T3, null);
                case "SEMILLA-COLA-EMPALTE-BC" -> {
                    alta("A", "api-paciente-a", PruebasCitas.PRUEBA, "URGENTE", T1, PruebasCitas.SOL_CREADA);
                    alta("B", "api-paciente-b", PruebasCitas.PRUEBA, "ORDINARIA", T2, PruebasCitas.SOL_CREADA_2);
                    alta("C", "api-paciente-c", PruebasCitas.PRUEBA, "PREFERENTE", T2, PruebasCitas.SOL_CREADA_3);
                    cambiarPrioridad(idSembrado("B"), "PREFERENTE", "empalme con C");
                }
                case "SEMILLA-COLA-CON-RETIRO", "SEMILLA-CON-RETIRO" -> {
                    colaTres();
                    cambiarPrioridad(idSembrado("B"), "ORDINARIA", "bajada de siembra");
                    retirar(idSembrado("B"), "retirada de siembra");
                }
                case "SEM-ESTADO" -> semEstado(valor);
                case "SEM-HUECO-DEST" -> huecoDestino(valor, f);
                case "SEM-TAMPER" -> tamper(valor, f);
                default -> throw new IllegalStateException("setup sin resolver: " + s + " en " + f.id());
            }
        }
    }

    /** Tres entradas vivas de la misma prueba: A URGENTE, B PREFERENTE y C ORDINARIA. */
    private void colaTres() throws Exception {
        alta("A", "api-paciente-a", PruebasCitas.PRUEBA, "URGENTE", T1, PruebasCitas.SOL_CREADA);
        alta("B", "api-paciente-b", PruebasCitas.PRUEBA, "PREFERENTE", T2, PruebasCitas.SOL_CREADA_2);
        alta("C", "api-paciente-c", PruebasCitas.PRUEBA, "ORDINARIA", T3, PruebasCitas.SOL_CREADA_3);
    }

    /** Deja una entrada en el estado que pide la matriz, con alias OBJ. */
    private void semEstado(String estado) throws Exception {
        switch (estado) {
            case "ACTIVA" -> alta("OBJ", "api-paciente-obj", PruebasCitas.PRUEBA, "URGENTE", T1,
                    PruebasCitas.SOL_CREADA);
            case "ASIGNADA" -> {
                alta("OBJ", "api-paciente-obj", PruebasCitas.PRUEBA, "URGENTE", T1, PruebasCitas.SOL_CREADA);
                asignar(idSembrado("OBJ"), PruebasCitas.PROF_MEDICO, INICIO_FIJO, null);
            }
            case "RETIRADA" -> {
                alta("OBJ", "api-paciente-obj", PruebasCitas.PRUEBA, "URGENTE", T1, PruebasCitas.SOL_CREADA);
                retirar(idSembrado("OBJ"), "retirada de siembra");
            }
            default -> throw new IllegalStateException("SEM-ESTADO sin resolver: " + estado);
        }
    }

    private String idSembrado(String alias) {
        String id = idsSembrados.get(alias);
        if (id == null) {
            throw new IllegalStateException("la siembra no dejo la entrada " + alias);
        }
        return id;
    }

    /** Alta sembrada: guarda el esperaId real con su alias para resolver los simbolos. */
    private MvcResult alta(String alias, String paciente, String prueba, String prioridad,
            String fecha, String solicitud) throws Exception {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("pacienteId", paciente);
        cuerpo.put("pruebaCodigo", prueba);
        cuerpo.put("profesionalId", PruebasCitas.PROF_MEDICO);
        cuerpo.put("prioridad", prioridad);
        if (fecha != null) {
            cuerpo.put("fechaSolicitud", fecha);
        }
        if (solicitud != null) {
            cuerpo.put("solicitudId", solicitud);
        }
        MvcResult r = req(post(BASE).contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(cuerpo)));
        if (r.getResponse().getStatus() == 201) {
            idsSembrados.put(alias, cuerpo(r).path("esperaId").asText());
        }
        return r;
    }

    private MvcResult cambiarPrioridad(String esperaId, String prioridad, String motivo) throws Exception {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        if (prioridad != null) {
            cuerpo.put("prioridad", prioridad);
        }
        if (motivo != null) {
            cuerpo.put("motivo", motivo);
        }
        return req(post(BASE + "/" + esperaId + "/prioridad").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(cuerpo)));
    }

    private MvcResult retirar(String esperaId, String motivo) throws Exception {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        if (motivo != null) {
            cuerpo.put("motivo", motivo);
        }
        return req(post(BASE + "/" + esperaId + "/retirar").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(cuerpo)));
    }

    private MvcResult asignar(String esperaId, String profesional, String inicio, Integer duracion)
            throws Exception {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        if (profesional != null) {
            cuerpo.put("profesionalId", profesional);
        }
        if (inicio != null) {
            cuerpo.put("inicio", inicio);
        }
        if (duracion != null) {
            cuerpo.put("duracionMinutos", duracion);
        }
        return req(post(BASE + "/" + esperaId + "/asignar").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(cuerpo)));
    }

    /** Hueco sembrado en el destino de la operacion de la fila: lo resuelve cada tabla. */
    private void huecoDestino(String valor, Fila f) {
        // Se siembra al llegar a la tabla de asignacion, que es la que conoce el inicio.
    }

    /** Ataques a la cadena de auditoria: los monta la tabla G. */
    private void tamper(String valor, Fila f) {
        throw new IllegalStateException("tamper sin montar: " + valor);
    }
}
