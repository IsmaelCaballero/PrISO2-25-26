package es.uclm.iso2.integracion.tdop;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesRegex;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import es.uclm.iso2.integracion.adaptador.AdaptadorSimulado;
import es.uclm.iso2.integracion.adaptador.HceAdaptadorSimulado;
import es.uclm.iso2.integracion.adaptador.PacsAdaptadorSimulado;
import es.uclm.iso2.integracion.adaptador.RisAdaptadorSimulado;
import es.uclm.iso2.integracion.adaptador.SsoAdaptadorSimulado;
import es.uclm.iso2.integracion.configuracion.IntegracionProperties;
import es.uclm.iso2.integracion.dominio.AuditoriaIntegracion;
import es.uclm.iso2.integracion.dominio.RegistroSimulacion;
import es.uclm.iso2.integracion.dominio.SetupSimulacion;
import es.uclm.iso2.integracion.dominio.SistemaExterno;
import es.uclm.iso2.integracion.servicio.IntegracionServicio;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * Matriz TDOP del CDU19 (Iter6, CFG-06) ejecutada contra la API REST real del spike con MockMvc.
 * Las siete tablas de la matriz viven en CSV bajo {@code src/test/resources/tdop/}, exportadas de
 * {@code docs/test/iter06/tdop-cdu19-espec.json}, unica fuente de verdad: si un caso falla se
 * corrige el JSON y se regeneran los CSV, nunca se retoca el CSV a mano. Cada tabla es un
 * {@code @ParameterizedTest} y el ARBITRO comun de {@link #verificar} comprueba el estatus que
 * declara {@code esperadoStatus} y, cuando {@code esperadoCodigo} no es "-", el {@code $.codigo}
 * del {@code ApiError}. Las comprobaciones propias de cada tabla ({@code esperadoReutilizado} en
 * la cadena de auditoria, {@code esperadoExtra} en ping y auditoria, el eco del modo y las cifras
 * en simulacion) salen del mismo CSV. El setup {@code SIM-*} no viaja por HTTP: se instala en el
 * {@link RegistroSimulacion} del contexto, que es el mismo registro que consultan los cuatro
 * adaptadores simulados al servir la peticion.
 *
 * <p>La matriz documenta y fija cuatro hallazgos del spike: (1) {@code proteger()} consulta la
 * politica ANTES que la peticion, asi que el 503 gana a 400/401/404 (filas CAIDO y LENTO_TIMEOUT
 * de las tablas A, B, C, D y E); (2) las rutas fuera del modulo o con segmento de ruta suprimido
 * mueren en el 404 o el 405 de Spring sin cuerpo {@code ApiError}, porque el consejo del modulo
 * esta acotado por {@code assignableTypes} a sus dos controladores (A13-A15, E12, G01/G02 y
 * G10-G14); (3) sincronizar no lleva {@code @RequestBody} y un {@code Content-Type} text/plain no
 * lo rechaza (G03); (4) Jackson coercea escalares numericos a cadena y el IdP lo responde con el
 * 401 de siempre, sin 400 por tipo (G07). Y un quinto que condiciona la tabla B:
 * {@code sincronizarHistoriaClinica} llama a la HCE antes de mirar los snapshots, asi que con el
 * adaptador caido no se sirve la copia local (B04 y B05 acaban en 503 pese al cache).</p>
 *
 * <p>Mundo limpio por fila sin reconstruir el grafo: los controladores y los beans son singletos,
 * asi que cada fila devuelve las politicas al valor de {@link IntegracionProperties} y vacia los
 * snapshots de {@link IntegracionServicio}, la cadena de auditoria con su hash previo, el rotador
 * de la HCE y las colas de latencia de los adaptadores. La cadena no tiene metodo de reset en el
 * producto porque el spike es de memoria, asi que el test usa reflexion sobre los campos privados,
 * la misma tecnica de {@code AuditoriaIntegracionTest} y {@code CatalogoServicioTest}.</p>
 *
 * @since 1.1.0 (CFG-06, CDU19)
 */
@DisplayName("CDU19 matriz TDOP (CFG-06)")
@SpringBootTest(classes = Cdu19MatrizTdopTest.ConfigTdopWeb.class)
@AutoConfigureMockMvc
class Cdu19MatrizTdopTest {

    /** Web minima: solo el paquete del modulo, sin la app servidora ni la seguridad de CDU17. */
    @SpringBootConfiguration
    @EnableAutoConfiguration
    @ComponentScan(basePackages = "es.uclm.iso2.integracion")
    static class ConfigTdopWeb {
    }

    /** Hash previo del primer eslabon: TC19-E16 lo espera en una cadena reciende empezar. */
    private static final String GENESIS = AuditoriaIntegracion.GENESIS;

    /** Login valido del IdP simulado, para las llamadas previas de TC19-E15. */
    private static final String SSO_OK = "{\"usuario\":\"idp-admin\",\"contrasena\":\"idp-pass\"}";

    /**
     * El mapa de estados del contrato CDU19 a estatus HTTP es el que declara
     * {@code tdop/cdu19-esperados.csv}, exportado del bloque {@code esperados} de la spec: el
     * arbitro lo lee y no puede inventarse una traduccion. La columna {@code codigoError} de ese
     * CSV no interviene aqui porque cada fila pide el suyo por {@code esperadoCodigo}.
     */
    private static final Map<String, HttpStatus> ESPERADOS = estadosEsperados();

    /** Carga el mapa estado-&gt;estatus HTTP desde el CSV de estados esperados de la matriz. */
    private static Map<String, HttpStatus> estadosEsperados() {
        Map<String, HttpStatus> mapa = new LinkedHashMap<>();
        for (Fila f : filas("tdop/cdu19-esperados.csv", 1, 13)) {
            mapa.put(f.columnas().get("estado"),
                    HttpStatus.valueOf(Integer.parseInt(f.columnas().get("http"))));
        }
        return Collections.unmodifiableMap(mapa);
    }

    /** Tramos de cifras del cuerpo de simulacion; clave = alias de la columna {@code cifras}. */
    private static final Map<String, String> CIFRAS = Map.of(
            "COMPLETAS", "\"latenciaMs\":7,\"timeoutMs\":400,\"reintentos\":1",
            "CERO", "\"latenciaMs\":0,\"timeoutMs\":0,\"reintentos\":0",
            "MAX", "\"latenciaMs\":9223372036854775807,\"timeoutMs\":9223372036854775807,"
                    + "\"reintentos\":2147483647",
            "NEG_LAT", "\"latenciaMs\":-1,\"timeoutMs\":300,\"reintentos\":0",
            "NEG_TIMEOUT", "\"latenciaMs\":5,\"timeoutMs\":-1,\"reintentos\":0",
            "NEG_REINT", "\"latenciaMs\":5,\"timeoutMs\":300,\"reintentos\":-1",
            "TIPO_ERRONEO", "\"latenciaMs\":\"cinco\",\"timeoutMs\":300,\"reintentos\":0",
            "AUSENTE", "");

    @Autowired
    private MockMvc mvc;

    @Autowired
    private IntegracionProperties propiedades;

    @Autowired
    private RegistroSimulacion registro;

    @Autowired
    private AuditoriaIntegracion auditoria;

    @Autowired
    private IntegracionServicio servicio;

    @Autowired
    private HceAdaptadorSimulado adaptadorHce;

    @Autowired
    private RisAdaptadorSimulado adaptadorRis;

    @Autowired
    private SsoAdaptadorSimulado adaptadorSso;

    @Autowired
    private PacsAdaptadorSimulado adaptadorPacs;

    /**
     * Fila que pasa = mundo limpio. Los beans son singletos y el spike no persiste, asi que no hay
     * tablas que borrar: se devuelven las politicas al valor de arranque de
     * {@link IntegracionProperties}, se vacian los snapshots de historias (la tabla B decide
     * SI/NO por ellos), la cadena de auditoria con su hash previo (TC19-E16 exige
     * {@code totalRegistros=0} y el genesis) y el estado acumulado de los adaptadores: el rotador
     * de la HCE (TC19-B03) y las colas de latencia que alimenta {@code
     * AdaptadorSimulado#registrarIntento} y lee la media movil del ping. Sin eso la matriz no
     * seria reproducible entre filas.
     */
    @BeforeEach
    void limpiarMundo() {
        for (SistemaExterno sistema : SistemaExterno.values()) {
            registro.cambiar(sistema, propiedades.politicaDe(sistema));
        }
        vaciar(servicio, "snapshots");
        vaciar(auditoria, "eslabones");
        ReflectionTestUtils.setField(auditoria, "ultimoHash", GENESIS);
        ReflectionTestUtils.setField(adaptadorHce, "rotador", new AtomicInteger());
        for (AdaptadorSimulado adaptador : List.of(adaptadorHce, adaptadorRis, adaptadorSso,
                adaptadorPacs)) {
            vaciar(adaptador, "latencias");
        }
    }

    // ---------- tabla A: sincronizar historia ----------

    @ParameterizedTest(name = "{0}")
    @MethodSource("filasSincronizar")
    void sincronizarDesdeMatriz(Fila f) throws Exception {
        aplicarSetup(f.columnas().get("setup"));
        String paciente = f.columnas().get("paciente");
        ResultActions res = mvc.perform(post(urlHistorias(paciente), aliasPaciente(paciente))
                .accept(MediaType.APPLICATION_JSON));
        verificar(f, res);
    }

    // ---------- tabla B: idempotencia y reutilizacion de snapshot ----------

    @ParameterizedTest(name = "{0}")
    @MethodSource("filasIdempotencia")
    void idempotenciaDesdeMatriz(Fila f) throws Exception {
        String paciente = aliasPaciente(f.columnas().get("paciente"));
        String ruta = "/api/integracion/historias/{paciente}/sincronizar";
        List<String> llamadas = List.of(f.columnas().get("llamadas").split(","));
        aplicarSetup(f.columnas().get("setup"));
        ResultActions ultima = null;
        for (int i = 0; i < llamadas.size(); i++) {
            if (i == 1) {
                aplicarSetup(f.columnas().get("setupEntre"));
            }
            ResultActions res = mvc.perform(post(ruta, paciente).accept(MediaType.APPLICATION_JSON));
            if (i < llamadas.size() - 1) {
                res.andExpect(status().isOk());
            }
            ultima = res;
        }
        verificar(f, ultima);
        String reutilizado = f.columnas().get("esperadoReutilizado");
        if ("-".equals(reutilizado)) {
            return;
        }
        // Reutilizar la copia local se comprueba donde el spike deja rastro de verdad: el resultado
        // del ultimo eslabon, OK_REUTILIZADA en vez de OK_ACTUALIZADA.
        mvc.perform(get("/api/integracion/auditoria").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cadenaIntegra").value(true))
                .andExpect(jsonPath("$.totalRegistros").value(llamadas.size()))
                .andExpect(jsonPath("$.eslabones[" + (llamadas.size() - 1) + "].accion")
                        .value("HCE_SINCRONIZAR"))
                .andExpect(jsonPath("$.eslabones[" + (llamadas.size() - 1) + "].resultado")
                        .value("SI".equals(reutilizado) ? "OK_REUTILIZADA" : "OK_ACTUALIZADA"));
    }

    // ---------- tabla C: login SSO ----------

    @ParameterizedTest(name = "{0}")
    @MethodSource("filasSso")
    void ssoDesdeMatriz(Fila f) throws Exception {
        aplicarSetup(f.columnas().get("setup"));
        ResultActions res = mvc.perform(post("/api/integracion/sso/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(cuerpoSso(f.columnas().get("usuario"), f.columnas().get("contrasena")))
                .accept(MediaType.APPLICATION_JSON));
        verificar(f, res);
    }

    // ---------- tabla D: estudios del RIS ----------

    @ParameterizedTest(name = "{0}")
    @MethodSource("filasEstudios")
    void estudiosDesdeMatriz(Fila f) throws Exception {
        aplicarSetup(f.columnas().get("setup"));
        ResultActions res = mvc.perform(get("/api/integracion/estudios/{acceso}",
                aliasAcceso(f.columnas().get("acceso"))).accept(MediaType.APPLICATION_JSON));
        verificar(f, res);
        if ("OK".equals(f.columnas().get("esperadoStatus"))) {
            res.andExpect(jsonPath("$.accessionNumber").value("ACC-0001"))
                    .andExpect(jsonPath("$.pacienteId").value("HC-1001"))
                    .andExpect(jsonPath("$.numeroSeries").value(3));
        }
    }

    // ---------- tabla E: ping, listado de sistemas y auditoria ----------

    @ParameterizedTest(name = "{0}")
    @MethodSource("filasPing")
    void pingDesdeMatriz(Fila f) throws Exception {
        aplicarSetup(f.columnas().get("setup"));
        if ("sso,sincronizar".equals(f.columnas().get("previas"))) {
            mvc.perform(post("/api/integracion/sso/login").contentType(MediaType.APPLICATION_JSON)
                    .content(SSO_OK).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
            mvc.perform(post("/api/integracion/historias/{paciente}/sincronizar", "api-paciente-ok")
                    .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
        }
        String operacion = f.columnas().get("operacion");
        String sistema = f.columnas().get("sistema");
        ResultActions res;
        if ("PING".equals(operacion)) {
            res = mvc.perform(get(urlPing(sistema), aliasSistema(sistema))
                    .accept(MediaType.APPLICATION_JSON));
        } else if ("SISTEMAS".equals(operacion)) {
            res = mvc.perform(get("/api/integracion/sistemas").accept(MediaType.APPLICATION_JSON));
        } else {
            res = mvc.perform(get("/api/integracion/auditoria").accept(MediaType.APPLICATION_JSON));
        }
        verificar(f, res);
        String extra = f.columnas().get("esperadoExtra");
        switch (extra == null ? "-" : extra) {
            case "DISPONIBLE_SI" -> res.andExpect(jsonPath("$.nombre")
                            .value(aliasSistema(sistema).trim().toLowerCase(Locale.ROOT)))
                    .andExpect(jsonPath("$.disponible").value(true));
            case "DISPONIBLE_NO_CAIDO" -> res.andExpect(jsonPath("$.disponible").value(false))
                    .andExpect(jsonPath("$.motivo").value("CAIDO"));
            case "DISPONIBLE_NO_TIEMPO_SUPERADO" ->
                    res.andExpect(jsonPath("$.disponible").value(false))
                            .andExpect(jsonPath("$.motivo").value("TIEMPO_SUPERADO"));
            case "TODOS_DISPONIBLES" -> res.andExpect(jsonPath("$.length()").value(4))
                    .andExpect(jsonPath("$[0].nombre").value("sso"))
                    .andExpect(jsonPath("$[1].nombre").value("hce"))
                    .andExpect(jsonPath("$[2].nombre").value("ris"))
                    .andExpect(jsonPath("$[3].nombre").value("pacs"))
                    .andExpect(jsonPath("$[*].disponible").value(everyItem(is(true))));
            case "MIXTO" -> res.andExpect(jsonPath("$[0].disponible").value(true))
                    .andExpect(jsonPath("$[1].disponible").value(false))
                    .andExpect(jsonPath("$[1].motivo").value("CAIDO"))
                    .andExpect(jsonPath("$[2].disponible").value(true))
                    .andExpect(jsonPath("$[3].disponible").value(true));
            case "CADENA_INTEGRA" -> res.andExpect(jsonPath("$.cadenaIntegra").value(true))
                    .andExpect(jsonPath("$.totalRegistros").value(2))
                    .andExpect(jsonPath("$.eslabones.length()").value(2))
                    .andExpect(jsonPath("$.eslabones[0].accion").value("SSO_LOGIN"))
                    .andExpect(jsonPath("$.eslabones[1].accion").value("HCE_SINCRONIZAR"))
                    .andExpect(jsonPath("$.ultimoHash").value(matchesRegex("[0-9a-f]{64}")));
            case "CADENA_VACIA" -> res.andExpect(jsonPath("$.cadenaIntegra").value(true))
                    .andExpect(jsonPath("$.totalRegistros").value(0))
                    .andExpect(jsonPath("$.eslabones.length()").value(0))
                    .andExpect(jsonPath("$.ultimoHash").value(GENESIS));
            default -> {
                // "-": las filas de ping con error o de lista no tienen comprobacion extra.
            }
        }
    }

    // ---------- tabla F: control de la simulacion ----------

    @ParameterizedTest(name = "{0}")
    @MethodSource("filasSimulacion")
    void simulacionDesdeMatriz(Fila f) throws Exception {
        aplicarSetup(f.columnas().get("setup"));
        ResultActions res = mvc.perform(post("/api/integracion/simulacion")
                .contentType(MediaType.APPLICATION_JSON)
                .content(cuerpoSimulacion(f.columnas().get("sistema"), f.columnas().get("modo"),
                        f.columnas().get("cifras")))
                .accept(MediaType.APPLICATION_JSON));
        verificar(f, res);
        if ("OK".equals(f.columnas().get("esperadoStatus"))) {
            boolean completas = "COMPLETAS".equals(f.columnas().get("cifras"));
            res.andExpect(jsonPath("$.modo").value(modoNormalizado(f.columnas().get("modo"))))
                    .andExpect(jsonPath("$.latenciaMs").value(completas ? 7 : 0))
                    .andExpect(jsonPath("$.timeoutMs").value(completas ? 400 : 0))
                    .andExpect(jsonPath("$.reintentos").value(completas ? 1 : 0));
        }
    }

    // ---------- tabla G: transporte, metodos y cuerpos raros ----------

    @ParameterizedTest(name = "{0}")
    @MethodSource("filasTransporte")
    void transporteDesdeMatriz(Fila f) throws Exception {
        aplicarSetup(f.columnas().get("setup"));
        String ruta = f.columnas().get("ruta");
        MockHttpServletRequestBuilder req = switch (f.columnas().get("metodo")) {
            case "GET" -> get(ruta);
            case "PATCH" -> patch(ruta);
            default -> post(ruta);
        };
        String tipo = f.columnas().get("contentTipo");
        if (!"NINGUNO".equals(tipo)) {
            req.contentType("TEXT_PLANO".equals(tipo) ? MediaType.TEXT_PLAIN
                    : MediaType.APPLICATION_JSON);
        }
        String cuerpo = f.columnas().get("cuerpo");
        if (!"NINGUNO".equals(cuerpo)) {
            req.content(cuerpoTransporte(cuerpo));
        }
        verificar(f, mvc.perform(req.accept(MediaType.APPLICATION_JSON)));
    }

    // ---------- arbitro comun ----------

    /**
     * Comprueba lo que la matriz espera de verdad. Primero el estatus HTTP, SIEMPRE: lo que el
     * contrato promete es el estatus. El codigo solo se exige si el CSV lo fija, porque hay
     * estados que el advice del modulo nunca escribe: los 404 y 405 de rutas fuera de sus
     * controladores llevan "-" en {@code esperadoCodigo} y al arbitro les basta el codigo de
     * estado (A13-A15, E12, G01, G02 y G10-G14). Un estado de exito tampoco lleva codigo.
     *
     * @param f   la fila de la matriz
     * @param res la peticion ya ejecutada con su respuesta
     * @throws Exception si MockMvc no puede montar la comprobacion
     */
    private void verificar(Fila f, ResultActions res) throws Exception {
        String estado = f.columnas().get("esperadoStatus");
        HttpStatus esperado = ESPERADOS.get(estado);
        if (esperado == null) {
            throw new IllegalArgumentException("estado desconocido en la matriz: " + estado);
        }
        res.andExpect(status().is(esperado.value()));
        String codigo = f.columnas().get("esperadoCodigo");
        if (codigo != null && !"-".equals(codigo)) {
            res.andExpect(jsonPath("$.codigo").value(codigo));
        }
    }

    // ---------- cargadores de las siete tablas ----------

    private static Stream<Fila> filasSincronizar() {
        return filas("tdop/cdu19-sincronizar.csv", 1, 21).stream();
    }

    private static Stream<Fila> filasIdempotencia() {
        return filas("tdop/cdu19-idempotencia.csv", 1, 7).stream();
    }

    private static Stream<Fila> filasSso() {
        return filas("tdop/cdu19-sso.csv", 1, 15).stream();
    }

    private static Stream<Fila> filasEstudios() {
        return filas("tdop/cdu19-estudios.csv", 1, 12).stream();
    }

    private static Stream<Fila> filasPing() {
        return filas("tdop/cdu19-ping.csv", 1, 17).stream();
    }

    private static Stream<Fila> filasSimulacion() {
        return filas("tdop/cdu19-simulacion.csv", 1, 40).stream();
    }

    private static Stream<Fila> filasTransporte() {
        return filas("tdop/cdu19-transporte.csv", 1, 14).stream();
    }

    /**
     * Fila leida del CSV: id de la columna 0 y mapa cabecera-&gt;valor con las demas.
     *
     * @param id       el TC19-xx
     * @param columnas los valores por cabecera
     */
    private record Fila(String id, Map<String, String> columnas) {
    }

    /**
     * El CSV exportado de la spec es la fuente unica: aqui no se reescriben casos, si uno falla se
     * corrige {@code tdop-cdu19-espec.json} y se regeneran los CSV. El rango de filas permite
     * aislar un bloque durante el depurador sin tocar nada.
     */
    private static List<Fila> filas(String csv, int desde, int hasta) {
        List<String> lineas = lineasCsv(csv);
        if (lineas.size() < 2) {
            throw new IllegalStateException("el CSV de la matriz " + csv + " esta vacio");
        }
        String[] cabecera = lineas.get(0).split(";", -1);
        List<Fila> filas = new ArrayList<>();
        for (int i = desde; i <= hasta && i < lineas.size(); i++) {
            String linea = lineas.get(i);
            if (linea.isBlank()) {
                continue;
            }
            String[] campos = linea.split(";", -1);
            Map<String, String> fila = new LinkedHashMap<>();
            for (int c = 0; c < cabecera.length; c++) {
                fila.put(cabecera[c], c < campos.length ? campos[c] : "");
            }
            filas.add(new Fila(campos[0], fila));
        }
        return filas;
    }

    private static List<String> lineasCsv(String classpath) {
        try (InputStream in = Cdu19MatrizTdopTest.class.getClassLoader()
                .getResourceAsStream(classpath)) {
            if (in == null) {
                throw new IllegalStateException("falta el CSV de la matriz: " + classpath);
            }
            List<String> lineas = new ArrayList<>();
            try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                StringBuilder sb = new StringBuilder();
                int ch;
                while ((ch = reader.read()) != -1) {
                    if (ch == '\n') {
                        lineas.add(sb.toString().replace("\r", ""));
                        sb.setLength(0);
                    } else {
                        sb.append((char) ch);
                    }
                }
                if (!sb.isEmpty()) {
                    lineas.add(sb.toString());
                }
            }
            return lineas;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // ---------- montaje de peticiones y mundo ----------

    /** Instala el setup de la fila en el registro vivo; NINGUNO y "-" dejan la politica inicial. */
    private void aplicarSetup(String token) {
        if (token == null || token.isBlank() || "NINGUNO".equals(token) || "-".equals(token)) {
            return;
        }
        SetupSimulacion setup = SetupSimulacion.desdeToken(token);
        registro.cambiar(setup.sistema(), setup.politica());
    }

    /**
     * Ruta de sincronizacion. Con el token AUSENTE se suprime el segmento, tal y como lo describe
     * la matriz: la peticion queda en {@code /historias/sincronizar} y la resuelve Spring, no el
     * consejo del modulo, de ahi el 404 sin {@code ApiError} de A13-A15.
     */
    private static String urlHistorias(String alias) {
        return "AUSENTE".equals(alias) ? "/api/integracion/historias/sincronizar"
                : "/api/integracion/historias/{paciente}/sincronizar";
    }

    /** Idem con el segmento del sistema en el ping: TC19-E12 deja la ruta en /sistemas/ping. */
    private static String urlPing(String alias) {
        return "AUSENTE".equals(alias) ? "/api/integracion/sistemas/ping"
                : "/api/integracion/sistemas/{sistema}/ping";
    }

    /** Identidad externa de la HCE simulada. RODEADO viaja con espacios y VACIO llega vaciable. */
    private static String aliasPaciente(String alias) {
        return switch (alias) {
            case "AUT" -> "api-paciente-ok";
            case "ROTATIVO" -> "api-paciente-rotativo";
            case "RODEADO" -> " api-paciente-ok ";
            case "VACIO" -> "   ";
            case "DESCONOCIDO", "NEGATIVO" -> "sap";
            case "LARGO" -> "p".repeat(80);
            default -> alias;
        };
    }

    /** Codigo de sistema para la ruta: MAYUSCULAS y RODEADO son de la clase valida y se resuelven. */
    private static String aliasSistema(String alias) {
        return switch (alias) {
            case "AUT", "HCE_MIN" -> "hce";
            case "RIS_MIN" -> "ris";
            case "PACS_MIN" -> "pacs";
            case "SSO_MIN" -> "sso";
            case "RODEADO" -> " hce ";
            case "MAYUSCULAS" -> "HCE";
            case "VACIO" -> "";
            case "ESPACIOS" -> "   ";
            case "DESCONOCIDO" -> "sap";
            default -> alias;
        };
    }

    /** Numero de acceso del RIS: MINUSCULA y RODEADO siguen dentro de clase tras recortar y subir. */
    private static String aliasAcceso(String alias) {
        return switch (alias) {
            case "AUT" -> "ACC-0001";
            case "MINUSCULA" -> "acc-0001";
            case "RODEADO" -> " ACC-0001 ";
            case "INEXISTENTE" -> "ACC-9999";
            default -> alias;
        };
    }

    /** Cuerpo del login: AUSENTE omite la contrasena, el resto rellena la plantilla. */
    private static String cuerpoSso(String usuario, String contrasena) {
        String u = switch (usuario) {
            case "AUT" -> "idp-admin";
            case "MALO" -> "sap";
            case "VACIO" -> "";
            case "ESPACIOS" -> "   ";
            case "LARGO" -> "u".repeat(80);
            default -> usuario;
        };
        String p = switch (contrasena) {
            case "AUT" -> "idp-pass";
            case "MALA" -> "idp-mala";
            case "VACIO", "VACIA" -> "";
            case "ESPACIOS" -> "   ";
            case "LARGO" -> "p".repeat(80);
            default -> contrasena;
        };
        if ("AUSENTE".equals(contrasena)) {
            return "{\"usuario\":\"" + u + "\"}";
        }
        return "{\"usuario\":\"" + u + "\",\"contrasena\":\"" + p + "\"}";
    }

    /** Modo del cuerpo de simulacion: NORMAL_MINUS baja a minusculas, MAX y AUT suben a NORMAL. */
    private static String aliasModo(String alias) {
        return switch (alias) {
            case "NORMAL", "MAX", "AUT" -> "NORMAL";
            case "NORMAL_MINUS" -> "normal";
            case "DESCONOCIDO" -> "FUERA_SERVICIO";
            default -> alias;
        };
    }

    /**
     * Modo normalizado que el endpoint devuelve en el JSON: el controlador lo reconstruye con
     * {@code valueOf(trim().toUpperCase(ROOT))}, asi que TC19-F01 responde NORMAL pese al normal
     * del cuerpo.
     */
    private static String modoNormalizado(String alias) {
        return "NORMAL_MINUS".equals(alias) ? "NORMAL" : aliasModo(alias);
    }

    /** Cuerpo de la tabla F: AUSENTE omite la clave y en el servidor llega null, no la cadena. */
    private static String cuerpoSimulacion(String sistema, String modo, String cifras) {
        StringBuilder sb = new StringBuilder("{");
        if (!"AUSENTE".equals(sistema)) {
            sb.append("\"sistema\":\"").append(aliasSistema(sistema)).append('"');
        }
        if (!"AUSENTE".equals(modo)) {
            if (!sb.isEmpty()) {
                sb.append(',');
            }
            sb.append("\"modo\":\"").append(aliasModo(modo)).append('"');
        }
        String cifrasJson = CIFRAS.get(cifras);
        if (cifrasJson == null) {
            throw new IllegalArgumentException("cifras desconocidas en la matriz: " + cifras);
        }
        if (!cifrasJson.isEmpty()) {
            if (!sb.isEmpty()) {
                sb.append(',');
            }
            sb.append(cifrasJson);
        }
        return sb.append('}').toString();
    }

    /** Cuerpos literales de la tabla G: aqui lo que se manda tal cual es lo que se quiere probar. */
    private static String cuerpoTransporte(String alias) {
        return switch (alias) {
            case "SSO_COMPLETO" -> SSO_OK;
            case "NO_JSON" -> "esto no es json";
            case "NULL" -> "null";
            case "USUARIO_NUMERICO" -> "{\"usuario\":123,\"contrasena\":\"idp-pass\"}";
            case "CIFRAS_TEXTO" -> "{\"sistema\":\"hce\",\"modo\":\"NORMAL\",\"latenciaMs\":\"cinco\","
                    + "\"timeoutMs\":300,\"reintentos\":0}";
            case "VACIO" -> "{}";
            default -> throw new IllegalArgumentException("cuerpo desconocido en la matriz: " + alias);
        };
    }

    /**
     * Vacia un mapa o una coleccion privada de los singletos por reflexion: snapshots, eslabones y
     * colas de latencia, que no tienen reset porque el spike en memoria no lo plantea. La alternativa
     * era reconstruir el contexto por fila, y con cadenas de latencias simuladas eso multiplicaba
     * el tiempo de la matriz sin ganar nada.
     */
    @SuppressWarnings("unchecked")
    private static void vaciar(Object destino, String campo) {
        Object valor = ReflectionTestUtils.getField(destino, campo);
        if (valor instanceof Map<?, ?> mapa) {
            ((Map<Object, Object>) mapa).clear();
        } else if (valor instanceof Collection<?> coleccion) {
            ((Collection<Object>) coleccion).clear();
        } else {
            throw new IllegalStateException("el campo " + campo + " no es vaciable");
        }
    }
}
