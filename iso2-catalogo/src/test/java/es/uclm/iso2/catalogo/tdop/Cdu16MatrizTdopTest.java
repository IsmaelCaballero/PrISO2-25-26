package es.uclm.iso2.catalogo.tdop;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.uclm.iso2.catalogo.dominio.AuditoriaCatalogo;
import es.uclm.iso2.catalogo.dominio.TipoPrueba;
import es.uclm.iso2.catalogo.repositorio.AuditoriaCatalogoRepositorio;
import es.uclm.iso2.catalogo.repositorio.TipoPruebaRepositorio;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Ejecucion de las matrices TDOP de CDU16 (D33): los cinco CSVs generados desde
 * {@code docs/test/iter01/tdop-cdu16-espec.json} (copia en {@code src/test/resources/tdop})
 * son la fuente de casos; cada fila es un parametrizado que dispara la peticion real contra
 * el contexto Spring completo del modulo (MockMvc + H2) y compara con el mapa de estados
 * {@code cdu16-esperados.csv}.
 *
 * <p>No hay heuristica aqui: el esperado de cada fila viene del CSV, y el CSV viene de la
 * spec. Si el codigo contradice a la spec, la fila falla en rojo y se corrige la spec
 * (y se regeneran los CSVs), no el test.</p>
 */
@SpringBootTest(classes = Cdu16MatrizTdopTest.ConfigTdopWeb.class)
@AutoConfigureMockMvc
class Cdu16MatrizTdopTest {

    /**
     * Arranque completo del modulo: el modulo no tiene {@code @SpringBootApplication}
     * (vive en iso2-server-app), asi que se declara explicitamente con escaneo de los
     * paquetes reales de web/servicio/dominio/repositorio.
     */
    @SpringBootConfiguration
    @EnableAutoConfiguration
    @ComponentScan(basePackages = "es.uclm.iso2.catalogo")
    @EntityScan(basePackages = "es.uclm.iso2.catalogo.dominio")
    @EnableJpaRepositories(basePackages = "es.uclm.iso2.catalogo.repositorio")
    static class ConfigTdopWeb {
    }

    private static final String URL_ALTAS = "/api/catalogo/pruebas";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Pattern REP = Pattern.compile("^REP:(\\d+):(.*)$");

    /** Separador de lineas compatible con CSV generados en Windows o Linux. */
    private static final String SALTOS = "\\r\\n|\\n|\\r";

    /** Mapa estado-esperado: nombre de estado -> (http, codigo de error), del CSV espejo. */
    private static final Map<String, Esperado> ESPERADOS = cargarEsperados();

    /** Un estado de la tabla de estados esperados. */
    private record Esperado(int http, String codigoError) {
    }

    /** Una fila de matriz: id + el resto de columnas. */
    private record Fila(String id, Map<String, String> columnas) {
        String get(String columna) {
            String valor = columnas.get(columna);
            return valor == null ? "" : valor;
        }
    }

    private static Map<String, Esperado> cargarEsperados() {
        Map<String, Esperado> mapa = new LinkedHashMap<>();
        List<String> lineas = lineasCsv("tdop/cdu16-esperados.csv");
        for (String linea : lineas.subList(1, lineas.size())) {
            String[] c = linea.split(";", -1);
            mapa.put(c[0], new Esperado(Integer.parseInt(c[1]), c[2]));
        }
        return mapa;
    }

    /** Lee un CSV de recursos de test y devuelve sus lineas (sin BOM ni saltos en blanco). */
    private static List<String> lineasCsv(String recurso) {
        try (InputStream in = Cdu16MatrizTdopTest.class.getClassLoader().getResourceAsStream(recurso)) {
            Objects.requireNonNull(in, "falta el recurso de test " + recurso);
            String texto = new String(in.readAllBytes(), StandardCharsets.UTF_8).replace("\uFEFF", "");
            List<String> lineas = new ArrayList<>();
            for (String linea : texto.split(SALTOS)) {
                if (!linea.isBlank()) {
                    lineas.add(linea);
                }
            }
            return lineas;
        } catch (java.io.IOException e) {
            throw new IllegalStateException("no se pudo leer " + recurso, e);
        }
    }

    /** Lee el CSV y devuelve sus filas como (id, mapa de columnas) para el parametrizado. */
    private static Stream<Fila> filas(String recurso) {
        try (InputStream in = Cdu16MatrizTdopTest.class.getClassLoader().getResourceAsStream(recurso)) {
            Objects.requireNonNull(in, "falta el recurso de test " + recurso);
            String texto = new String(in.readAllBytes(), StandardCharsets.UTF_8)
                    .replace("\uFEFF", "");
            List<String> lineas = new ArrayList<>();
            for (String linea : texto.split(SALTOS)) {
                if (!linea.isBlank()) {
                    lineas.add(linea);
                }
            }
            String[] cabecera = lineas.get(0).split(";", -1);
            return lineas.subList(1, lineas.size()).stream().map(linea -> {
                String[] c = linea.split(";", -1);
                Map<String, String> columnas = new LinkedHashMap<>();
                for (int i = 0; i < cabecera.length && i < c.length; i++) {
                    columnas.put(cabecera[i], c[i]);
                }
                return new Fila(columnas.get("id"), columnas);
            });
        } catch (java.io.IOException e) {
            throw new IllegalStateException("no se pudo leer " + recurso, e);
        }
    }

    @org.springframework.beans.factory.annotation.Autowired
    private MockMvc mockMvc;

    @org.springframework.beans.factory.annotation.Autowired
    private TipoPruebaRepositorio pruebas;

    @org.springframework.beans.factory.annotation.Autowired
    private AuditoriaCatalogoRepositorio auditoria;

    /** Cada fila arranca con el almacen vacio: los setups se sembraran por HTTP. */
    @BeforeEach
    void limpiarAlmacen() {
        auditoria.deleteAll();
        pruebas.deleteAll();
    }

    // ------------------------------------------------------------------
    // Tabla A: altas (POST /api/catalogo/pruebas)
    // ------------------------------------------------------------------

    @ParameterizedTest(name = "{0}")
    @MethodSource("filasAlta")
    void matrizAlta(Fila f) throws Exception {
        String raiz = codigoBase(f);
        sembrar(f.get("setup"), raiz);

        var peticion = post(URL_ALTAS);
        String cuerpo = cuerpoAlta(f, raiz);
        if ("TEXT_PLANO".equals(f.get("transporte"))) {
            peticion.contentType(MediaType.TEXT_PLAIN)
                    .content(cuerpo == null ? "texto suelto" : cuerpo);
        } else if (cuerpo != null) {
            peticion.contentType(MediaType.APPLICATION_JSON).content(cuerpo);
        }
        // cuerpo VACIO: sin content en absoluto (HttpMessageNotReadable / body ausente)

        verificar(f, mockMvc.perform(peticion).andReturn());
    }

    private static Stream<Fila> filasAlta() {
        return filas("tdop/cdu16-alta.csv");
    }

    /** Construye el cuerpo del alta segun la columna cuerpo, y el nivel de limites. */
    private static String cuerpoAlta(Fila f, String raiz) {
        String cuerpo = f.get("cuerpo");
        String limites = f.get("limites");
        String nombre = segunNombreCategoria(f.get("nombreCategoria"));
        String categoria = "NO_ADMITIDA".equals(f.get("nombreCategoria")) ? "USG" : "RX";

        switch (cuerpo) {
            case "VACIO":
                return null;
            case "JSON_MALFORMADO":
                return "{\"codigo\":\"" + raiz + "\",\"nombre\":";
            case "TIPO_ERRONEO":
                return altaBase(raiz, nombre, categoria)
                        + "\"requiereCita\":true,\"activa\":true,"
                        + bloqueLimitesComaSiAplica(limites)
                        + "\"politica\":true}";
            case "CONTRATO_INCOMPLETO":
                return altaBase(raiz, nombre, categoria)
                        + "\"requiereCita\":true,\"activa\":true,"
                        + "\"duracionMinutos\":60}";
            default:
                break;
        }
        // VALIDO (con la variante COMA en limites)
        StringBuilder sb = new StringBuilder(altaBase(raiz, nombre, categoria));
        sb.append("\"requiereCita\":true,\"activa\":true,");
        sb.append(bloqueLimitesComaSiAplica(limites));
        sb.append('}');
        return sb.toString();
    }

    private static String altaBase(String codigo, String nombre, String categoria) {
        return "{\"codigo\":\"" + codigo + "\",\"nombre\":\"" + nombre
                + "\",\"categoria\":\"" + categoria + "\",";
    }

    /** Bloque de numeros + protocolo + politica segun nivel de limites; COMA rompe el JSON. */
    private static String bloqueLimites(String nivel) {
        switch (nivel) {
            case "AUT":
                return "\"duracionMinutos\":60,"
                        + "\"protocolo\":{\"preparacion\":\"REPOSO Y AYUNO\",\"ayunoHoras\":12,"
                        + "\"contraindicaciones\":\"NINGUNA\"},"
                        + "\"politica\":{\"plazoCancelacionHoras\":24,\"maxCancelaciones\":3}";
            case "JUSTO":
                return "\"duracionMinutos\":1440,"
                        + "\"protocolo\":{\"preparacion\":\"" + repetir('P', 1000) + "\",\"ayunoHoras\":720,"
                        + "\"contraindicaciones\":\"NINGUNA\"},"
                        + "\"politica\":{\"plazoCancelacionHoras\":8760,\"maxCancelaciones\":100}";
            case "CERO":
                return "\"duracionMinutos\":0,"
                        + "\"protocolo\":{\"preparacion\":\"REPOSO\",\"ayunoHoras\":0,"
                        + "\"contraindicaciones\":\"NINGUNA\"},"
                        + "\"politica\":{\"plazoCancelacionHoras\":0,\"maxCancelaciones\":0}";
            case "NEGATIVA":
                return "\"duracionMinutos\":-1,"
                        + "\"protocolo\":{\"preparacion\":\"REPOSO\",\"ayunoHoras\":-1,"
                        + "\"contraindicaciones\":\"NINGUNA\"},"
                        + "\"politica\":{\"plazoCancelacionHoras\":-1,\"maxCancelaciones\":-1}";
            case "EXCESIVA":
                return "\"duracionMinutos\":1441,"
                        + "\"protocolo\":{\"preparacion\":\"" + repetir('P', 1001) + "\",\"ayunoHoras\":721,"
                        + "\"contraindicaciones\":\"NINGUNA\"},"
                        + "\"politica\":{\"plazoCancelacionHoras\":8761,\"maxCancelaciones\":101}";
            case "AUSENTE":
                // campos numericos ausentes: Jackson los deja a 0 y las cotas del DTO deciden
                return "\"protocolo\":{\"preparacion\":\"REPOSO\",\"contraindicaciones\":\"NINGUNA\"},"
                        + "\"politica\":{}";
            default:
                throw new IllegalStateException("nivel de limites desconocido: " + nivel);
        }
    }

    private static String bloqueLimitesComaSiAplica(String nivel) {
        if ("COMA".equals(nivel)) {
            // 36,5: Jackson corta el numero en la coma y el resto revienta el parseo
            return "\"duracionMinutos\":36,5,"
                    + "\"protocolo\":{\"preparacion\":\"REPOSO\",\"ayunoHoras\":12,"
                    + "\"contraindicaciones\":\"NINGUNA\"},"
                    + "\"politica\":{\"plazoCancelacionHoras\":24,\"maxCancelaciones\":3}";
        }
        return bloqueLimites(nivel);
    }

    private static String segunNombreCategoria(String nivel) {
        switch (nivel) {
            case "AUT":
                return "RX DE RODILLA";
            case "VACIO":
                return "";
            case "ESPACIOS":
                return "   ";
            case "LARGO":
                return repetir('N', 121);
            default:
                return "RX DE RODILLA";
        }
    }

    // ------------------------------------------------------------------
    // Tabla B: modificaciones (PUT /api/catalogo/pruebas/{ruta})
    // ------------------------------------------------------------------

    @ParameterizedTest(name = "{0}")
    @MethodSource("filasModificacion")
    void matrizModificacion(Fila f) throws Exception {
        String raiz = codigoBase(f);
        sembrar(f.get("setup"), raiz);

        String ruta = segunCodigoRuta(f.get("codigoRuta"), raiz);
        String cuerpo = cuerpoModificacion(f, raiz);

        var peticion = put(URL_ALTAS + "/" + ruta).contentType(MediaType.APPLICATION_JSON).content(cuerpo);
        verificar(f, mockMvc.perform(peticion).andReturn());
    }

    private static Stream<Fila> filasModificacion() {
        return filas("tdop/cdu16-modificacion.csv");
    }

    private static String segunCodigoRuta(String codigoRuta, String raiz) {
        switch (codigoRuta) {
            case "IGUAL":
                return raiz;
            case "DISTINTO_INVENTADO":
                return raiz + "-RUTA";
            case "DISTINTO_EXISTENTE":
                return raiz + "-OTRA";
            default:
                throw new IllegalStateException("codigoRuta desconocido: " + codigoRuta);
        }
    }

    /** El cuerpo siempre lleva el codigo raiz; el campo invalido lo marca la columna. */
    private static String cuerpoModificacion(Fila f, String raiz) {
        String nombre = "RX DE RODILLA";
        String categoria = "RX";
        String preparacion = "REPOSO Y AYUNO";
        int maxCancelaciones = 3;
        switch (f.get("validezCuerpo")) {
            case "VALIDO":
                break;
            case "NOMBRE_LARGO121":
                nombre = expandir(f.get("valorInvalido"));
                break;
            case "CATEGORIA_FUERA":
                categoria = f.get("valorInvalido");
                break;
            case "MAXCANCEL_101":
                maxCancelaciones = Integer.parseInt(f.get("valorInvalido"));
                break;
            case "PROTO_LOMAX1001":
                preparacion = expandir(f.get("valorInvalido"));
                break;
            default:
                throw new IllegalStateException("validezCuerpo desconocido: " + f.get("validezCuerpo"));
        }
        return altaBase(raiz, nombre, categoria)
                + "\"requiereCita\":true,\"activa\":true,"
                + "\"duracionMinutos\":60,"
                + "\"protocolo\":{\"preparacion\":\"" + preparacion + "\",\"ayunoHoras\":12,"
                + "\"contraindicaciones\":\"NINGUNA\"},"
                + "\"politica\":{\"plazoCancelacionHoras\":24,\"maxCancelaciones\":" + maxCancelaciones + "}}";
    }

    // ------------------------------------------------------------------
    // Tabla C: listado (GET /api/catalogo/pruebas)
    // ------------------------------------------------------------------

    @ParameterizedTest(name = "{0}")
    @MethodSource("filasListado")
    void matrizListado(Fila f) throws Exception {
        String raiz = codigoBase(f);
        sembrar(f.get("setup"), raiz);

        var peticion = get(URL_ALTAS).param("pagina", expandir(f.get("pagina")))
                .param("tamano", expandir(f.get("tamano")));
        if (!"VACIO".equals(f.get("orden"))) {
            peticion.param("orden", f.get("orden"));
        }
        verificar(f, mockMvc.perform(peticion).andReturn());
    }

    private static Stream<Fila> filasListado() {
        return filas("tdop/cdu16-listado.csv");
    }

    // ------------------------------------------------------------------
    // Tabla D: estados (DETALLE / BAJA / REACTIVAR sobre la fila raiz)
    // ------------------------------------------------------------------

    @ParameterizedTest(name = "{0}")
    @MethodSource("filasEstados")
    void matrizEstados(Fila f) throws Exception {
        String raiz = codigoBase(f);
        sembrar(f.get("setup"), raiz);

        MvcResult resultado;
        switch (f.get("operacion")) {
            case "DETALLE":
                resultado = mockMvc.perform(get(URL_ALTAS + "/" + raiz)).andReturn();
                break;
            case "BAJA":
                resultado = mockMvc.perform(delete(URL_ALTAS + "/" + raiz)).andReturn();
                break;
            case "REACTIVAR":
                resultado = mockMvc.perform(put(URL_ALTAS + "/" + raiz + "/reactivar")).andReturn();
                break;
            default:
                throw new IllegalStateException("operacion desconocida: " + f.get("operacion"));
        }
        verificar(f, resultado);
    }

    private static Stream<Fila> filasEstados() {
        return filas("tdop/cdu16-estados.csv");
    }

    // ------------------------------------------------------------------
    // Tabla E: conjeturas (secuencias completas con tramos de auditoria)
    // ------------------------------------------------------------------

    @ParameterizedTest(name = "{0}")
    @MethodSource("filasConjetura")
    void matrizConjetura(Fila f) throws Exception {
        String raiz = codigoBase(f);
        sembrar(f.get("setup"), raiz);

        MvcResult resultado;
        switch (f.get("operacion")) {
            case "ALTA":
                resultado = mockMvc.perform(post(URL_ALTAS).contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoConjetura(f, raiz))).andReturn();
                break;
            case "MODIFICACION":
                String ruta = "OTRA_COD".equals(f.get("rutaExtra")) ? raiz + "-OTRA" : raiz;
                resultado = mockMvc.perform(put(URL_ALTAS + "/" + ruta)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoConjetura(f, raiz))).andReturn();
                break;
            case "BAJA":
                resultado = mockMvc.perform(delete(URL_ALTAS + "/" + raiz)).andReturn();
                break;
            case "REACTIVAR":
                resultado = mockMvc.perform(put(URL_ALTAS + "/" + raiz + "/reactivar")).andReturn();
                break;
            case "DETALLE":
                String url = "AUDITORIA".equals(f.get("rutaExtra"))
                        ? URL_ALTAS + "/" + raiz + "/auditoria"
                        : URL_ALTAS + "/" + raiz;
                resultado = mockMvc.perform(get(url)).andReturn();
                break;
            case "LISTADO":
                resultado = mockMvc.perform(get(URL_ALTAS)
                        .param("pagina", "0").param("tamano", "200")).andReturn();
                break;
            default:
                throw new IllegalStateException("operacion desconocida: " + f.get("operacion"));
        }
        verificar(f, resultado);

        if (!"-".equals(f.get("asientos"))) {
            long asientos = asientosDe(raiz);
            assertThat(asientos).as("%s: asientos de auditoria de %s", f.id(), raiz)
                    .isEqualTo(Long.parseLong(f.get("asientos")));
        }
        if (!"-".equals(f.get("estadoFinal"))) {
            boolean activa = pruebas.findByCodigo(raiz)
                    .orElseThrow(() -> new AssertionError(f.id() + ": la fila raiz deberia existir"))
                    .isActiva();
            assertThat(activa).as("%s: estado final de %s", f.id(), raiz)
                    .isEqualTo(Boolean.parseBoolean(f.get("estadoFinal")));
        }
        if ("LISTADO".equals(f.get("operacion"))) {
            String cuerpo = resultado.getResponse().getContentAsString(StandardCharsets.UTF_8);
            assertThat(cuerpo).as("%s: el listado incluye la prueba inactiva", f.id())
                    .contains(raiz).contains(raiz + "-B");
        }
    }

    private static Stream<Fila> filasConjetura() {
        return filas("tdop/cdu16-conjetura.csv");
    }

    /** Cuerpos de conjetura: variantes del contrato sobre el codigo raiz. */
    private static String cuerpoConjetura(Fila f, String raiz) {
        String cuerpo = f.get("cuerpo");
        String codigo = raiz;
        String nombre = "RX DE CONJETURA";
        String categoria = "RX";
        String duracion = "\"duracionMinutos\":60";
        String ayuno = "12";
        String plazo = "24";
        String maxCancelaciones = "3";
        String extra = "";
        boolean activa = true;
        switch (cuerpo) {
            case "VALIDO":
                break;
            case "CODIGO_PERTENECIENTE":
                codigo = raiz; // el codigo del cuerpo es el de la fila sembrada; la ruta lleva otra
                break;
            case "ACTIVA_FALSA":
                activa = false;
                break;
            case "ACTIVA_VERDADERA":
                activa = true;
                break;
            case "CODIGO_21_CARACTERES":
                codigo = raiz + "X".repeat(21 - raiz.length());
                break;
            case "CODIGO_CON_ESPACIOS":
                codigo = " " + raiz + " ";
                break;
            case "DURACION_COMO_TEXTO":
                duracion = "\"duracionMinutos\":\"60\"";
                break;
            case "CAMPO_DESCONOCIDO":
                extra = ",\"campoQueNoExiste\":\"nada\"";
                break;
            case "CATEGORIA_MINUSCULA":
                categoria = "rx";
                break;
            case "LIMITES_CERO":
                // TC16-E22: cuerpo duplicado pero con cotas violadas (0 no admitido)
                duracion = "\"duracionMinutos\":0";
                ayuno = "0";
                plazo = "0";
                maxCancelaciones = "0";
                break;
            default:
                throw new IllegalStateException("cuerpo de conjetura desconocido: " + cuerpo);
        }
        String json = "{\"codigo\":\"" + codigo + "\",\"nombre\":\"" + nombre + "\","
                + "\"categoria\":\"" + categoria + "\","
                + "\"requiereCita\":true,\"activa\":" + activa + ","
                + duracion + ","
                + "\"protocolo\":{\"preparacion\":\"REPOSO Y AYUNO\",\"ayunoHoras\":" + ayuno + ","
                + "\"contraindicaciones\":\"NINGUNA\"},"
                + "\"politica\":{\"plazoCancelacionHoras\":" + plazo + ",\"maxCancelaciones\":"
                + maxCancelaciones + "}"
                + extra + "}";
        return json;
    }

    private long asientosDe(String codigo) {
        return auditoria.findAll().stream()
                .filter(asiento -> codigo.equals(asiento.getCodigoPrueba()))
                .count();
    }

    // ------------------------------------------------------------------
    // Setups: se siembran por HTTP para que los asientos de auditoria sean reales
    // ------------------------------------------------------------------

    private void sembrar(String setup, String raiz) throws Exception {
        for (String token : setup.split(",")) {
            switch (token.trim()) {
                case "CREA_COD_ACTIVA" -> esperarAlta(raiz);
                case "CREA_COD_INACTIVA" -> {
                    esperarAlta(raiz);
                    mockMvc.perform(delete(URL_ALTAS + "/" + raiz)).andExpect(status().isNoContent());
                }
                case "CREA_OTRA_ACTIVA" -> esperarAlta(raiz + "-OTRA");
                case "CREA_MIXTO" -> {
                    esperarAlta(raiz);
                    esperarAlta(raiz + "-B");
                    mockMvc.perform(delete(URL_ALTAS + "/" + raiz + "-B")).andExpect(status().isNoContent());
                }
                default -> {
                    // NINGUNO y COD_AUTO: no hay que sembrar nada (el codigo de la fila es libre)
                }
            }
        }
    }

    private void esperarAlta(String codigo) throws Exception {
        String json = altaBase(codigo, "PRUEBA DE SIEMBRA", "RX")
                + "\"requiereCita\":true,\"activa\":true,"
                + bloqueLimites("AUT") + "}";
        mockMvc.perform(post(URL_ALTAS).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated());
    }

    // ------------------------------------------------------------------
    // Verificacion comun: status y, si procede, el codigo de ApiError
    // ------------------------------------------------------------------

    private void verificar(Fila f, MvcResult resultado) throws Exception {
        String estado = f.get("esperadoStatus");
        Esperado esperado = ESPERADOS.get(estado);
        assertThat(esperado).as("%s: estado %s no esta en cdu16-esperados.csv", f.id(), estado).isNotNull();

        assertThat(resultado.getResponse().getStatus())
                .as("%s: estado HTTP esperado %s", f.id(), estado)
                .isEqualTo(esperado.http());

        String codigoEsperado = f.get("esperadoCodigo");
        if (!"-".equals(codigoEsperado)) {
            assertThat(codigoApiError(resultado))
                    .as("%s: cuerpo ApiError con codigo esperado", f.id())
                    .isEqualTo(codigoEsperado);
        } else if ("RUTA_INEXISTENTE".equals(estado)) {
            assertThat(codigoApiError(resultado))
                    .as("%s: %s no debe dejar cuerpo ApiError", f.id(), estado)
                    .isNull();
        }
    }

    /** El campo "codigo" del cuerpo ApiError, o null si el cuerpo no es un ApiError. */
    private static String codigoApiError(MvcResult resultado) {
        String cuerpo;
        try {
            cuerpo = resultado.getResponse().getContentAsString();
        } catch (java.io.UnsupportedEncodingException e) {
            return null;
        }
        if (cuerpo == null || cuerpo.isBlank()) {
            return null;
        }
        try {
            JsonNode raiz = MAPPER.readTree(cuerpo);
            JsonNode codigo = raiz.path("codigo");
            return codigo.isMissingNode() || codigo.isNull() ? null : codigo.asText();
        } catch (java.io.IOException e) {
            return null;
        }
    }

    // ------------------------------------------------------------------
    // Utilidades de fila
    // ------------------------------------------------------------------

    /** Codigo raiz determinista por fila: TC16-A03 -> TDOP-A03. */
    private static String codigoBase(Fila f) {
        return "TDOP-" + f.id().substring("TC16-".length());
    }

    /** Expande la notacion REP:n:c de los CSVs (p. ej. REP:121:N -> 121 equises). */
    private static String expandir(String token) {
        Matcher m = REP.matcher(token);
        if (m.matches()) {
            return repetir(m.group(2).charAt(0), Integer.parseInt(m.group(1)));
        }
        return token;
    }

    private static String repetir(char caracter, int veces) {
        return String.valueOf(caracter).repeat(veces);
    }

}
