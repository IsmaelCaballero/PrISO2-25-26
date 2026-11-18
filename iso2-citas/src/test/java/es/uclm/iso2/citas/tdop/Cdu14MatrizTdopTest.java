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
import es.uclm.iso2.citas.dominio.CitaHueco;
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
import java.util.Arrays;
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
import java.util.concurrent.CountDownLatch;
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

    /** Prefijo de las citas fantasmas que siembran huecos en destinos (reglas 21 y 22). */
    private static final String HUECO_FANTASMA = "cdu14-hueco-fantasma-";

    /** Cuantas citas fantasma se han sembrado: la columna cita_id es UNIQUE. */
    private int huecosSembrados;

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

    /**
     * Esperado de una operacion intermedia de la secuencia. La columna {@code esperadoStatus}
     * solo describe la ultima, asi que los pasos previos se afirman con el token que les toca;
     * el resultado se devuelve para poder encadenar la secuencia.
     */
    private MvcResult paso(final String id, final MvcResult r, final String esperado) throws Exception {
        verificarEsperado(id, esperado, "-", r);
        return r;
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
    // Tabla D (cdu14-reubicacion.csv): reubicacion POST /{esperaId}/prioridad
    // ------------------------------------------------------------------

    static Stream<Fila> filasReubicacion() {
        return filas("cdu14-reubicacion.csv");
    }

    /**
     * Las 27 filas de reubicacion. Ademas del status y del codigo canonico la fila afirma dos
     * cosas: que la cadena de auditoria solo crece cuando la reubicacion es legitima (D03, D16,
     * D18 y D26 no escriben un segundo eslabon) y que la cola ACTIVA sale en el orden canonico
     * de D11 con las alias de la siembra. Las cadenas no se comparan contra un literal fijo:
     * se lee la cadena antes de operar y se exige que crezca exactamente un eslabon, porque las
     * filas de terminales ya traen eslabones de siembra.
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("filasReubicacion")
    void reubicacionDesdeMatriz(Fila f) throws Exception {
        aplicarSetup(f);
        String esperaId = esperaIdDe(f, "objetivo");
        boolean objetivoVivo = idsSembrados.containsValue(esperaId);
        String cadenaAntes = objetivoVivo ? cadenaDe(esperaId) : null;
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        if (!"AUSENTE".equals(f.get("destino"))) {
            cuerpo.put("prioridad", f.get("destino"));
        }
        String motivo = motivoDe(f);
        if (motivo != null) {
            cuerpo.put("motivo", motivo);
        }
        MvcResult r = req(post(BASE + "/" + esperaId + "/prioridad")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(cuerpo)));
        verificar(f, r);
        if (objetivoVivo) {
            String cadenaDespues = cadenaDe(esperaId);
            if (statusEsperado(f.get("esperadoStatus")) == 200) {
                assertThat(cadenaDespues).as("%s: la reubicacion legitima anade un REUBICACION", f.id())
                        .isEqualTo(cadenaAntes + ",REUBICACION");
            } else {
                assertThat(cadenaDespues).as("%s: una reubicacion rechazada no escribe eslabones", f.id())
                        .isEqualTo(cadenaAntes);
            }
        }
        verificarColaCanon(f, f.get("esperadoCola"));
    }

    /**
     * Motivo de la reubicacion. VACIO es cadena en blanco y AUSENTE es quitar la clave del
     * cuerpo; MAX300 y LARGO301 son las longitudes limite de la columna motivo_fin.
     */
    private String motivoDe(Fila f) {
        return switch (f.get("motivo")) {
            case "TEXTO" -> "reubicacion de la matriz " + f.id();
            case "VACIO" -> "";
            case "AUSENTE", "-" -> null;
            case "MAX300", "LARGO301" -> token(f.get("motivo"));
            default -> throw new IllegalStateException(
                    "motivo sin resolver: " + f.get("motivo") + " en " + f.id());
        };
    }

    /**
     * El esperaId real de la fila. {@code AUT} es la entrada que nombra la columna objetivo;
     * INEXISTENTE es un UUID que nunca se sembro; MAL_FORMATADO es texto donde va un UUID, que
     * el PathVariable corta antes de llegar al servicio; y un objetivo "-" pide una entrada que
     * no existe, como anuncia D19.
     */
    private String esperaIdDe(Fila f, String columnaObjetivo) {
        String simbolo = f.get("esperaId");
        if (simbolo.isBlank()) {
            simbolo = "AUT";
        }
        return switch (simbolo) {
            case "INEXISTENTE" -> UUID.randomUUID().toString();
            case "MAL_FORMATADO" -> "no-es-un-uuid";
            default -> {
                String alias = f.get(columnaObjetivo);
                yield alias.isBlank() || "-".equals(alias)
                        ? UUID.randomUUID().toString() : idSembrado(alias);
            }
        };
    }

    /**
     * La columna esperadoCola escribe las entradas ACTIVA en orden canonico como
     * alias:PRIORIDAD separadas por barras verticales; las alias que empatan en escalon y marca de
     * tiempo se agrupan en un mismo bloque separadas por comas ("B,C:PREFERENTE"), porque dentro
     * del empate el orden lo decide el esperaId y nadie afirma ese orden. Las alias salen de la
     * siembra, asi que la matriz no fija ni un solo UUID. Se lee la cola filtrada por ACTIVA y,
     * ademas del orden, se reafirman la posicion y el total. La comparacion se hace por bloques
     * de empate, como anuncia la nota de D11 al prometer el bloque {B,C} y la lectura repetida.
     */
    private void verificarColaCanon(Fila f, String esperado) throws Exception {
        if ("-".equals(esperado) || esperado.isBlank()) {
            return;
        }
        List<List<String>> esperados = new ArrayList<>();
        List<String> prioridadesPosicion = new ArrayList<>();
        for (String bloque : esperado.split("\\|")) {
            String trozo = bloque.trim();
            String alias = trozo.substring(0, trozo.indexOf(':'));
            String prioridad = trozo.substring(trozo.indexOf(':') + 1);
            for (String a : alias.split(",")) {
                prioridadesPosicion.add(prioridad);
            }
            esperados.add(ordenados(Arrays.asList(alias.split(","))));
        }
        MvcResult lectura = req(get(BASE).param("pruebaCodigo", PruebasCitas.PRUEBA)
                .param("estado", "ACTIVA"));
        assertThat(lectura.getResponse().getStatus()).as("%s: la cola canon se lee con 200", f.id())
                .isEqualTo(200);
        JsonNode cola = cuerpo(lectura);
        List<String> aliasReales = new ArrayList<>();
        for (int i = 0; i < cola.size(); i++) {
            JsonNode e = cola.get(i);
            aliasReales.add(aliasDe(e.path("esperaId").asText()));
            assertThat(e.path("posicion").asInt()).as("%s: la posicion es 1-based", f.id())
                    .isEqualTo(i + 1);
            assertThat(e.path("total").asLong()).as("%s: el total acompana a la cola servida", f.id())
                    .isEqualTo(cola.size());
        }
        assertThat(ordenCanonico(cola)).as("%s: el orden canon es prioridad desc, fecha asc y "
                + "esperaId asc; respuesta: %s", f.id(), cola).isTrue();
        assertThat(aliasReales).as("%s: la cola trae exactamente las entradas sembradas", f.id())
                .hasSameSizeAs(prioridadesPosicion);
        for (int i = 0; i < aliasReales.size(); i++) {
            assertThat(cola.get(i).path("prioridad").asText())
                    .as("%s: el escalon de la posicion %d", f.id(), i + 1)
                    .isEqualTo(prioridadesPosicion.get(i));
        }
        assertThat(bloquesDeEmpate(cola, aliasReales)).as("%s: las alias de la cola, por bloques", f.id())
                .isEqualTo(esperados);
    }

    /** Bloques de alias de la respuesta: dentro de un empate el orden no es afirmable. */
    private List<List<String>> bloquesDeEmpate(JsonNode cola, List<String> aliasReales) {
        List<List<String>> bloques = new ArrayList<>();
        int i = 0;
        while (i < aliasReales.size()) {
            int j = i + 1;
            while (j < aliasReales.size() && empatan(cola.get(i), cola.get(j))) {
                j++;
            }
            bloques.add(ordenados(aliasReales.subList(i, j)));
            i = j;
        }
        return bloques;
    }

    /** Dos entradas empatan cuando escalon y marca de tiempo son identicos. */
    private boolean empatan(JsonNode una, JsonNode otra) {
        return una.path("prioridad").asText().equals(otra.path("prioridad").asText())
                && una.path("fechaSolicitud").asText().equals(otra.path("fechaSolicitud").asText());
    }

    private List<String> ordenados(List<String> valores) {
        List<String> copia = new ArrayList<>(valores);
        copia.sort(Comparator.naturalOrder());
        return copia;
    }

    /** Alias con la que la siembra conoce una entrada; "?" es una entrada que nadie sembro. */
    private String aliasDe(String esperaId) {
        for (Map.Entry<String, String> sembrada : idsSembrados.entrySet()) {
            if (sembrada.getValue().equals(esperaId)) {
                return sembrada.getKey();
            }
        }
        return "?";
    }

    /**
     * Cadena de auditoria de una entrada: el genesis material mas la accion de cada eslabon.
     * No hay ningun eslabon GENESIS guardado en la tabla: el genesis es el hash anterior del
     * primer eslabon, igual que en la cadena de citas de CDU12.
     */
    private String cadenaDe(String esperaId) throws Exception {
        JsonNode asientos = cuerpo(req(get(BASE + "/" + esperaId + "/auditoria")));
        StringBuilder cadena = new StringBuilder(GENESIS);
        for (JsonNode asiento : asientos) {
            cadena.append(',').append(asiento.path("accion").asText());
        }
        return cadena.toString();
    }

    // ------------------------------------------------------------------
    // Tabla E (cdu14-secuencias.csv): doble clic secuencial y concurrencia
    // ------------------------------------------------------------------

    static Stream<Fila> filasSecuencia() {
        return filas("cdu14-secuencias.csv");
    }

    /**
     * Las 12 secuencias. La columna esperadoStatus describe la ultima operacion, asi que los
     * pasos previos se afirman con el token que les toca (paso). Despues se comprueban las tres
     * salidas del modulo: las cadenas de las entradas tocadas, como multiset; la cola, como
     * recuento de estados sin filtrar; y el numero de citas. El invariante de la puerta SEG es
     * que el segundo golpe no escribe nada, y solo se ve comparando las tres.
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("filasSecuencia")
    void secuenciaDesdeMatriz(Fila f) throws Exception {
        aplicarSetup(f);
        String id = f.id();
        List<String> tocados = new ArrayList<>();
        MvcResult ultimo;
        switch (f.get("secuencia")) {
            case "ALTA_ALTA" -> {
                tocados.add("P1");
                ultimo = paso(id, alta("P1", "api-paciente-a", PruebasCitas.PRUEBA, "URGENTE", T1, null),
                        "CREADO");
                ultimo = paso(id, alta("P2", "api-paciente-a", PruebasCitas.PRUEBA, "URGENTE", T2, null),
                        "ALTA_DUPLICADA");
            }
            case "ALTA_RETIRAR_ALTA" -> {
                tocados.add("P1");
                ultimo = paso(id, alta("P1", "api-paciente-a", PruebasCitas.PRUEBA, "URGENTE", T1, null),
                        "CREADO");
                ultimo = paso(id, retirar(idSembrado("P1"), "retirada de la secuencia"), "OK");
                tocados.add("P2");
                ultimo = paso(id, alta("P2", "api-paciente-a", PruebasCitas.PRUEBA, "URGENTE", T2, null),
                        "CREADO");
            }
            case "ALTA_ASIGNAR_ALTA" -> {
                tocados.add("P1");
                ultimo = paso(id, alta("P1", "api-paciente-a", PruebasCitas.PRUEBA, "URGENTE", T1,
                        PruebasCitas.SOL_CREADA), "CREADO");
                ultimo = paso(id, asignar(idSembrado("P1"), PruebasCitas.PROF_MEDICO, INICIO_FIJO, null),
                        "ASIGNADO");
                tocados.add("P2");
                ultimo = paso(id, alta("P2", "api-paciente-a", PruebasCitas.PRUEBA, "URGENTE", T2, null),
                        "CREADO");
            }
            case "ALTA_RETIRAR_RETIRAR" -> {
                tocados.add("P1");
                ultimo = paso(id, alta("P1", "api-paciente-a", PruebasCitas.PRUEBA, "URGENTE", T1, null),
                        "CREADO");
                ultimo = paso(id, retirar(idSembrado("P1"), "retirada de la secuencia"), "OK");
                ultimo = paso(id, retirar(idSembrado("P1"), "segunda retirada"), "NO_ACTIVA");
            }
            case "ALTA_PRIORIDAD_VUELTA" -> {
                tocados.add("P1");
                ultimo = paso(id, alta("P1", "api-paciente-a", PruebasCitas.PRUEBA, "URGENTE", T1, null),
                        "CREADO");
                ultimo = paso(id, cambiarPrioridad(idSembrado("P1"), "ORDINARIA", "la bajada"), "OK");
                ultimo = paso(id, cambiarPrioridad(idSembrado("P1"), "URGENTE", "la vuelta"), "OK");
            }
            case "ALTA_PRIORIDAD_IGUAL_IGUAL" -> {
                tocados.add("P1");
                ultimo = paso(id, alta("P1", "api-paciente-a", PruebasCitas.PRUEBA, "ORDINARIA", T1, null),
                        "CREADO");
                ultimo = paso(id, cambiarPrioridad(idSembrado("P1"), "URGENTE", "primera subida"), "OK");
                ultimo = paso(id, cambiarPrioridad(idSembrado("P1"), "URGENTE", "doble clic"), "REDUNDANTE");
            }
            case "ALTA_ASIGNAR_ASIGNAR" -> {
                tocados.add("P1");
                ultimo = paso(id, alta("P1", "api-paciente-a", PruebasCitas.PRUEBA, "URGENTE", T1,
                        PruebasCitas.SOL_CREADA), "CREADO");
                ultimo = paso(id, asignar(idSembrado("P1"), PruebasCitas.PROF_MEDICO, INICIO_FIJO, null),
                        "ASIGNADO");
                ultimo = paso(id, asignar(idSembrado("P1"), PruebasCitas.PROF_MEDICO, INICIO_FIJO, null),
                        "NO_ACTIVA");
            }
            case "ASIGNAR_CONCURRENTES" -> {
                tocados.add("P1");
                ultimo = paso(id, alta("P1", "api-paciente-a", PruebasCitas.PRUEBA, "URGENTE", T1,
                        PruebasCitas.SOL_CREADA), "CREADO");
                List<MvcResult> dos = alMismoTiempo(
                        () -> asignar(idSembrado("P1"), PruebasCitas.PROF_MEDICO, INICIO_FIJO, null),
                        () -> asignar(idSembrado("P1"), PruebasCitas.PROF_MEDICO, INICIO_FIJO, null));
                afirmarUnOkYOtroConflicto(f, dos);
                ultimo = ganadora(dos);
            }
            case "ALTA_ALTA_CONCURRENTES" -> {
                List<MvcResult> dos = alMismoTiempo(
                        () -> alta("P1", "api-paciente-a", PruebasCitas.PRUEBA, "URGENTE", T1, null),
                        () -> alta("P2", "api-paciente-a", PruebasCitas.PRUEBA, "URGENTE", T1, null));
                afirmarUnOkYOtroConflicto(f, dos);
                tocados.add(ganadoraDe(dos, "P1", "P2"));
                ultimo = ganadora(dos);
            }
            case "RETIRAR_CONCURRENTES" -> {
                tocados.add("P1");
                ultimo = paso(id, alta("P1", "api-paciente-a", PruebasCitas.PRUEBA, "URGENTE", T1, null),
                        "CREADO");
                List<MvcResult> dos = alMismoTiempo(
                        () -> retirar(idSembrado("P1"), "retirada A"),
                        () -> retirar(idSembrado("P1"), "retirada B"));
                afirmarUnOkYOtroConflicto(f, dos);
                ultimo = ganadora(dos);
            }
            case "ASIGNAR_MISMO_HUECO_ENTRADAS_DISTINTAS" -> {
                tocados.add("B");
                tocados.add("C");
                ultimo = paso(id, asignar(idSembrado("B"), PruebasCitas.PROF_MEDICO, INICIO_FIJO, null),
                        "ASIGNADO");
                ultimo = paso(id, asignar(idSembrado("C"), PruebasCitas.PROF_MEDICO, INICIO_FIJO, null),
                        "HUECO_OCUPADO");
            }
            case "ALTA_ALTA_DISTINTOS" -> {
                tocados.add("P1");
                ultimo = paso(id, alta("P1", "api-paciente-a", PruebasCitas.PRUEBA, "URGENTE", T1, null),
                        "CREADO");
                tocados.add("P2");
                ultimo = paso(id, alta("P2", "api-paciente-b", PruebasCitas.PRUEBA, "URGENTE", T2, null),
                        "CREADO");
            }
            default -> throw new IllegalStateException(
                    "secuencia sin montar: " + f.get("secuencia") + " en " + id);
        }
        verificar(f, ultimo);
        verificarEslabones(f, tocados);
        verificarRecuentos(f, f.get("esperadoCola"));
        verificarCitas(f, f.get("esperadoCitas"));
    }

    /**
     * Eslabones de las entradas que la secuencia toco, comparados como multiset ordenado. La
     * matriz los escribe separados por " / ", en el orden que le da igual: lo que se afirma es el
     * conjunto de cadenas, porque el identificador de cada entrada no esta en la matriz.
     */
    private void verificarEslabones(Fila f, List<String> aliasTocadas) throws Exception {
        String esperado = f.get("esperadoEslabones");
        if ("-".equals(esperado) || esperado.isBlank()) {
            return;
        }
        List<String> reales = new ArrayList<>();
        for (String alias : aliasTocadas) {
            reales.add(cadenaDe(idSembrado(alias)));
        }
        assertThat(ordenados(reales)).as("%s: cadenas de las entradas tocadas", f.id())
                .isEqualTo(ordenados(List.of(esperado.split(" / "))));
    }

    /**
     * Recuento de estados de la cola. La columna esperadoCola cuenta entradas por estado sobre la
     * cola SIN filtrar por estado, y por eso la lectura pide solo la prueba: el repositorio
     * entiende el filtro de estado ausente como todos los estados, y ahi es donde se ve la
     * terminal que el doble clic dejo atras.
     */
    private void verificarRecuentos(Fila f, String esperado) throws Exception {
        if ("-".equals(esperado) || esperado.isBlank()) {
            return;
        }
        Map<String, Integer> esperados = new HashMap<>();
        for (String trozo : esperado.split("\\s*\\+\\s*")) {
            String[] partes = trozo.trim().split("\\s+");
            esperados.merge(partes[1], Integer.parseInt(partes[0]), Integer::sum);
        }
        Map<String, Integer> reales = new HashMap<>();
        for (JsonNode e : cuerpo(req(get(BASE).param("pruebaCodigo", PruebasCitas.PRUEBA)))) {
            reales.merge(e.path("estado").asText().toUpperCase(Locale.ROOT), 1, Integer::sum);
        }
        assertThat(reales).as("%s: la cola de %s, sin filtro de estado", f.id(), PruebasCitas.PRUEBA)
                .isEqualTo(esperados);
    }

    /** Las citas que hay en el sistema: la puerta SEG promete que el segundo golpe no crea. */
    private void verificarCitas(Fila f, String esperado) throws Exception {
        if ("-".equals(esperado) || esperado.isBlank()) {
            return;
        }
        int reales = cuerpo(req(get("/api/citas"))).size();
        assertThat(reales).as("%s: citas creadas por la secuencia", f.id())
                .isEqualTo(Integer.parseInt(esperado));
    }

    /**
     * Dos operaciones lanzadas a la vez con dos barreras: ninguna arranca hasta que las dos
     * estan listas, y las dos sueltan la barrera en el mismo instante. Sin esto la concurrencia
     * de las filas E08, E09 y E10 seria una letra bonita que en realidad corre de una en una.
     */
    private List<MvcResult> alMismoTiempo(Callable<MvcResult> una, Callable<MvcResult> otra)
            throws Exception {
        ExecutorService hilo = Executors.newFixedThreadPool(2);
        try {
            CountDownLatch listas = new CountDownLatch(2);
            CountDownLatch salida = new CountDownLatch(1);
            Future<MvcResult> primera = hilo.submit(() -> {
                listas.countDown();
                salida.await();
                return una.call();
            });
            Future<MvcResult> segunda = hilo.submit(() -> {
                listas.countDown();
                salida.await();
                return otra.call();
            });
            assertThat(listas.await(10, TimeUnit.SECONDS)).as("los dos hilos no llegaron a estar listos")
                    .isTrue();
            salida.countDown();
            return List.of(primera.get(30, TimeUnit.SECONDS), segunda.get(30, TimeUnit.SECONDS));
        } finally {
            hilo.shutdownNow();
        }
    }

    /**
     * El invariante UN_OK_Y_OTRO_409: exactamente una operacion termina en exito y la otra en
     * conflicto. La matriz no fija codigo porque el perdedor puede acabar en 409 de maquina de
     * estados, en 409 de hueco por UNIQUE o en conflicto optimista por @Version.
     */
    private void afirmarUnOkYOtroConflicto(Fila f, List<MvcResult> resultados) {
        List<Integer> codigos = new ArrayList<>();
        resultados.forEach(r -> codigos.add(r.getResponse().getStatus()));
        assertThat(codigos.stream().filter(c -> c >= 200 && c < 300).count())
                .as("%s: exactamente una operacion termina en exito, codigos: %s", f.id(), codigos)
                .isEqualTo(1L);
        assertThat(codigos.stream().filter(c -> c >= 409).count())
                .as("%s: la otra termina en conflicto, codigos: %s", f.id(), codigos).isEqualTo(1L);
    }

    private MvcResult ganadora(List<MvcResult> dos) {
        return dos.stream()
                .filter(r -> r.getResponse().getStatus() >= 200 && r.getResponse().getStatus() < 300)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("ninguna operacion termino en exito"));
    }

    /** Que alias se guardo el alta que gano la carrera. */
    private String ganadoraDe(List<MvcResult> dos, String aliasPrimera, String aliasSegunda) {
        return dos.get(0).getResponse().getStatus() == 201 ? aliasPrimera : aliasSegunda;
    }

    // ------------------------------------------------------------------
    // Tabla F (cdu14-asignar.csv): asignacion desde la cola
    // ------------------------------------------------------------------

    static Stream<Fila> filasAsignacion() {
        return filas("cdu14-asignar.csv");
    }

    /**
     * Una fila de la tabla F: POST /{esperaId}/asignar con el cuerpo armado por los tokens.
     * La precedencia de guardas es la que se prueba fila a fila: la sintaxis del cuerpo (400)
     * corre antes de mirar si la entrada existe (404), el estado de la cola (409) antes que la
     * solicitud, y la solicitud antes que las guardas del CDU12 (franja antes que hueco).
     * La cadena de auditoria solo crece con el ASIGNACION de una asignacion legitima.
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("filasAsignacion")
    void asignacionDesdeMatriz(Fila f) throws Exception {
        aplicarSetup(f);
        String esperaId = switch (f.get("esperaId")) {
            case "INEXISTENTE" -> UUID.randomUUID().toString();
            case "MAL_FORMATADO" -> "no-es-un-uuid";
            default -> idSembrado("OBJ");
        };
        boolean objVivo = idsSembrados.containsValue(esperaId);
        String cadenaAntes = objVivo ? cadenaDe(esperaId) : null;
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        String profesional = profesionalDe(f);
        if (profesional != null) {
            cuerpo.put("profesionalId", profesional);
        }
        String inicio = inicioDe(f);
        if (inicio != null) {
            cuerpo.put("inicio", inicio);
        }
        Integer duracion = duracionDe(f);
        if (duracion != null) {
            cuerpo.put("duracionMinutos", duracion);
        }
        MvcResult r = req(post(BASE + "/" + esperaId + "/asignar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(cuerpo)));
        verificar(f, r);
        if ("ASIGNADO".equals(f.get("esperadoStatus"))) {
            JsonNode cita = cuerpo(r).path("cita");
            assertThat(cita.path("citaId").asText()).as("%s: la asignacion pare una cita con id", f.id())
                    .isNotBlank();
            assertThat(LocalDateTime.parse(cita.path("inicio").asText()))
                    .as("%s: la cita nace en la hora pedida", f.id())
                    .isEqualTo(LocalDateTime.parse(inicio));
            if (objVivo) {
                assertThat(cadenaDe(esperaId)).as("%s: la asignacion legitima anade un ASIGNACION", f.id())
                        .isEqualTo(cadenaAntes + ",ASIGNACION");
            }
        } else if (objVivo) {
            assertThat(cadenaDe(esperaId)).as("%s: una asignacion rechazada no escribe eslabones", f.id())
                    .isEqualTo(cadenaAntes);
        }
    }

    /** El profesional de la fila: VACIO es cadena en blanco, AUSENTE quita la clave. */
    private String profesionalDe(Fila f) {
        return switch (f.get("profesional")) {
            case "MEDICO" -> PruebasCitas.PROF_MEDICO;
            case "SIN_AGENDA" -> PruebasCitas.PROF_SIN_AGENDA;
            case "DESCONOCIDO" -> "api-profesional-que-no-existe";
            case "VACIO" -> "";
            case "AUSENTE" -> null;
            default -> throw new IllegalStateException(
                    "profesional sin resolver: " + f.get("profesional") + " en " + f.id());
        };
    }

    /**
     * El inicio de la fila como texto JSON: AUSENTE quita la clave y MAL_FORMATADO manda texto
     * donde va un LocalDateTime, que la deserializacion corta con 400 DATOS_INVALIDOS.
     */
    private String inicioDe(Fila f) {
        return switch (f.get("inicio")) {
            case "AUT" -> INICIO_FIJO;
            case "APERTURA_JUSTA" -> "2026-11-13T08:00:00";
            case "CIERRE_LIMITRO" -> "2026-11-13T14:30:00";
            case "PASADA_CIERRE" -> "2026-11-13T15:00:00";
            case "FESTIVO" -> "2026-11-10T09:00:00";
            case "ANTERIOR_APERTURA" -> "2026-11-13T07:00:00";
            case "DOMINGO" -> "2026-11-15T09:00:00";
            case "MAL_FORMATADO" -> "manana al mediodia";
            case "AUSENTE" -> null;
            default -> throw new IllegalStateException(
                    "inicio sin resolver: " + f.get("inicio") + " en " + f.id());
        };
    }

    /** La duracion en minutos; AUSENTE quita la clave y deja que la fije el catalogo. */
    private Integer duracionDe(Fila f) {
        return switch (f.get("duracion")) {
            case "AUT", "AUSENTE" -> null;
            case "D30" -> 30;
            case "D1" -> 1;
            case "D0" -> 0;
            case "D_NEGATIVA" -> -5;
            case "EXCESIVA" -> 600;
            default -> throw new IllegalStateException(
                    "duracion sin resolver: " + f.get("duracion") + " en " + f.id());
        };
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
                case "SEMILLA-MIXTA", "SEMILLA-COLA-TRES" -> colaTres(f);
                case "SEMILLA-EMPALTE-FECHA" -> {
                    alta("A", "api-paciente-a", PruebasCitas.PRUEBA, "ORDINARIA", T1, PruebasCitas.SOL_CREADA);
                    alta("B", "api-paciente-b", PruebasCitas.PRUEBA, "PREFERENTE", T1, PruebasCitas.SOL_CREADA_2);
                }
                case "SEMILLA-EMPALTE-TODO" -> {
                    alta("A", "api-paciente-a", PruebasCitas.PRUEBA, "PREFERENTE", T1, PruebasCitas.SOL_CREADA);
                    alta("B", "api-paciente-b", PruebasCitas.PRUEBA, "PREFERENTE", T1, PruebasCitas.SOL_CREADA_2);
                }
                case "SEMILLA-TERMINALES", "SEMILLA-COLA-TERMINALES" -> {
                    colaTres(f);
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
                    alta("B", "api-paciente-b", PruebasCitas.PRUEBA, "PREFERENTE", T2, PruebasCitas.SOL_CREADA_2);
                    alta("C", "api-paciente-c", PruebasCitas.PRUEBA, "ORDINARIA", T2, PruebasCitas.SOL_CREADA_3);
                }
                case "SEMILLA-COLA-CON-RETIRO" -> {
                    colaTres(f);
                    retirar(idSembrado("A"), "retirada de siembra");
                }
                case "SEMILLA-CON-RETIRO" -> {
                    alta("SEM", "api-paciente", PruebasCitas.PRUEBA, "ORDINARIA", T1, null);
                    cambiarPrioridad(idSembrado("SEM"), "URGENTE", "reubicacion de siembra");
                    retirar(idSembrado("SEM"), "retirada de siembra");
                }
                case "SEMILLA-CITA-OCUPA" -> {
                    alta("SEM", "api-paciente", PruebasCitas.PRUEBA, "URGENTE", T1, PruebasCitas.SOL_CREADA);
                    asignar(idSembrado("SEM"), PruebasCitas.PROF_MEDICO, INICIO_FIJO, null);
                }
                case "SEM-ESTADO" -> semEstado(valor, f);
                case "SEM-HUECO-DEST" -> huecoDestino(valor, f);
                case "SEM-TAMPER" -> tamper(valor, f);
                default -> throw new IllegalStateException("setup sin resolver: " + s + " en " + f.id());
            }
        }
    }

    /** Tres entradas vivas de la misma prueba: A URGENTE, B PREFERENTE y C ORDINARIA. */
    private void colaTres(Fila f) throws Exception {
        String solA = PruebasCitas.SOL_CREADA;
        String solB = PruebasCitas.SOL_CREADA_2;
        String solC = PruebasCitas.SOL_CREADA_3;
        if ("NINGUNA".equals(enlaceDe(f))) {
            solA = null;
            solB = null;
            solC = null;
        }
        alta("A", "api-paciente-a", PruebasCitas.PRUEBA, "URGENTE", T1, solA);
        alta("B", "api-paciente-b", PruebasCitas.PRUEBA, "PREFERENTE", T2, solB);
        alta("C", "api-paciente-c", PruebasCitas.PRUEBA, "ORDINARIA", T3, solC);
    }

    /** Valor del token ENLAZE del setup: CITADA o NINGUNA; null es que la fila no lo lleva. */
    private String enlaceDe(Fila f) {
        for (String bruto : f.get("setup").split(",")) {
            String s = bruto.trim();
            if (s.startsWith("ENLAZE:")) {
                return s.substring("ENLAZE:".length());
            }
        }
        return null;
    }

    /** Deja una entrada en el estado que pide la matriz, con alias OBJ. */
    private void semEstado(String estado, Fila f) throws Exception {
        String solicitud = solicitudDeSiembra(f);
        switch (estado) {
            case "ACTIVA" -> alta("OBJ", "api-paciente-obj", PruebasCitas.PRUEBA, "URGENTE", T1, solicitud);
            case "ASIGNADA" -> {
                alta("OBJ", "api-paciente-obj", PruebasCitas.PRUEBA, "URGENTE", T1, solicitud);
                asignar(idSembrado("OBJ"), PruebasCitas.PROF_MEDICO, INICIO_FIJO, null);
            }
            case "RETIRADA" -> {
                alta("OBJ", "api-paciente-obj", PruebasCitas.PRUEBA, "URGENTE", T1, solicitud);
                retirar(idSembrado("OBJ"), "retirada de siembra");
            }
            default -> throw new IllegalStateException("SEM-ESTADO sin resolver: " + estado);
        }
    }

    /**
     * Solicitud que se enlaza al sembrar con SEM-ESTADO: la fija el token ENLAZE de la fila,
     * como pide la regla 19 de la especificacion. Sin token se usa la creada de siempre, que
     * es la que necesitan las filas de tablas que no conocen ENLAZE.
     */
    private String solicitudDeSiembra(Fila f) {
        String enlace = enlaceDe(f);
        if (enlace == null) {
            return PruebasCitas.SOL_CREADA;
        }
        return token(enlace);
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
        if (prioridad != null) {
            cuerpo.put("prioridad", prioridad);
        }
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

    /** Alias de la agenda fantasma que usa FANTASMA_EXACTO: no es un profesional del mundo API. */
    private static final String PROF_FANTASMA = "api-otro-profesional";

    /**
     * Hueco sembrado en el destino de la operacion de la fila: lo resuelve cada tabla. El tipo
     * se toma de la columna {@code hueco} cuando la tabla la tiene (es el factor que se prueba)
     * y del token del setup en las tablas que no la describen.
     */
    private void huecoDestino(String valor, Fila f) {
        String tipo = f.celdas().containsKey("hueco") ? f.get("hueco") : valor;
        if ("LIBRE".equals(tipo) || !f.celdas().containsKey("inicio")) {
            // LIBRE no siembra nada, y las tablas que no describen un destino no tienen hueco que llenar
            return;
        }
        LocalDateTime inicio;
        try {
            inicio = LocalDateTime.parse(inicioDe(f));
        } catch (RuntimeException sinInicio) {
            // sin un inicio interpretable no hay tramo que sembrar: la fila se corta antes por 400
            return;
        }
        String profesional = f.celdas().containsKey("profesional") ? profesionalDe(f)
                : PruebasCitas.PROF_MEDICO;
        if (profesional == null || profesional.isBlank()) {
            // el hueco es solo decoracion cuando la fila se va a rechazar por falta de profesional
            profesional = PruebasCitas.PROF_MEDICO;
        }
        if (tipo.startsWith("FANTASMA")) {
            // el hueco debe estar en OTRA agenda: si estuviera en la pedida la asignacion no seria
            // posible y la regla 22 no tendria sentido, porque la prueba exige seguir ASIGNADO
            profesional = PROF_FANTASMA;
        }
        if ("SOLAPE".equals(tipo)) {
            // tramo que cae dentro del pedido sin coincidir exactamente con el pedido
            inicio = inicio.plusMinutes(15);
        }
        LocalDateTime fin = inicio.plusMinutes(PruebasCitas.DURACION);
        huecos.saveAndFlush(new CitaHueco(HUECO_FANTASMA + (++huecosSembrados),
                profesional, inicio, fin));
    }

    /** Ataques a la cadena de auditoria: los monta la tabla G. */
    private void tamper(String valor, Fila f) {
        throw new IllegalStateException("tamper sin montar: " + valor);
    }
}
