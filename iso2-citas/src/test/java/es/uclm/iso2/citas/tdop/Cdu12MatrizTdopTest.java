package es.uclm.iso2.citas.tdop;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.uclm.iso2.citas.dominio.Cita;
import es.uclm.iso2.citas.dominio.CitaHueco;
import es.uclm.iso2.citas.repositorio.AuditoriaCitasRepositorio;
import es.uclm.iso2.citas.repositorio.CitaHuecoRepositorio;
import es.uclm.iso2.citas.repositorio.CitaRepositorio;
import es.uclm.iso2.citas.servicio.PruebasCitas;
import es.uclm.iso2.common.solicitudes.SolicitudFacade;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * Matriz TDOP de la CDU12 (Iteracion 5, CFG-05): 172 casos sobre el API HTTP de citas leidos
 * de los seis CSV generados por {@code tools/pairwise.py} desde
 * {@code docs/test/iter05/tdop-cdu12-espec.json}. Si un caso falla, se corrige la especificacion
 * y se regeneran los CSV (D33); nunca se retocan a mano los CSV ni este test.
 *
 * <p>Las tablas cubren: alta (agendar), reprogramacion por POST /api/citas/{id}/reprogramar
 * (la ruta real; el PATCH del contrato D36 no esta implementado y es hallazgo ya anotado),
 * huecos libres, listado con
 * filtros, transiciones de estado (confirmar/realizar/reprogramar/cancelar/expirar) y la tabla
 * de conjeturas, que son comportamientos leidos de la implementacion actual y del contrato
 * D36: duracion 0 suplida por el catalogo, profesionalId sin recortar, Content-Type incorrecto
 * y subrecursos inexistentes. Las celdas marcadas HALLAZGO en el CSV son defectos confirmados
 * que se aceptan como comportamiento esperado hasta que la matriz lo diga (D16).</p>
 *
 * <p>El contexto arranca con {@link ConfigTdopWeb}: solo se escanean los controladores web y
 * las capas de dominio y repositorio. El servicio lo aporta {@link PruebasCitas} con reloj fijo,
 * y escanear el paquete de servicio al tiempo crearia un segundo {@code CitaServicio}
 * enfrentado al bean de las pruebas.</p>
 *
 * @since 1.1.0 (CFG-05, CDU12)
 */
@SpringBootTest(classes = Cdu12MatrizTdopTest.ConfigTdopWeb.class)
@AutoConfigureMockMvc
class Cdu12MatrizTdopTest {

    private static final String URI_BASE = "/api/citas";
    private static final String URI_HUECOS = URI_BASE + "/huecos";
    private static final String ID_FALSA = "api-cita-que-no-existe";

    private static final String CUERPO_REPRO = "{\"inicio\":\"2026-11-13T13:00:00\"}";
    private static final String CUERPO_REPRO_PROPIO = "{\"inicio\":\"2026-11-13T09:00:00\"}";
    private static final String CUERPO_CANCEL = "{\"motivo\":\"cancelacion de prueba\"}";
    private static final String CUERPO_CANCEL_VACIO = "{\"motivo\":\"  \"}";
    private static final String CUERPO_EXPIRA = "{\"corte\":\"2026-11-13T00:00:00\"}";

    /** Inicio sin solape con la siembra (09:00) para los alta de la tabla de conjeturas. */
    private static final LocalDateTime INICIO_FLEX = LocalDateTime.parse("2026-11-13T12:00:00");

    /** Cita vencida para el lote: su fin queda antes del corte 2026-11-13T00:00. */
    private static final LocalDateTime INICIO_VENCIDA = LocalDateTime.parse("2026-11-12T09:00:00");

    /** Cita proxima al reloj fijo (2026-11-11T08:00): cancelarla fuera de plazo da 409. */
    private static final LocalDateTime INICIO_CERCANO = LocalDateTime.parse("2026-11-11T09:00:00");

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String SALTOS = "\\R";
    private static final Map<String, Esperado> ESPERADOS = cargarEsperados();

    /** Estado de respuesta pedido por una fila: HTTP esperado mas codigo de error esperado. */
    private record Esperado(int http, String codigoError) {
    }

    private static Map<String, Esperado> cargarEsperados() {
        Map<String, Esperado> esperado = new LinkedHashMap<>();
        List<String> lineas = lineasCsv("tdop/cdu12-esperados.csv");
        for (String linea : lineas.subList(1, lineas.size())) {
            String[] c = linea.split(";", -1);
            esperado.put(c[0], new Esperado(Integer.parseInt(c[1].trim()), c[2]));
        }
        return esperado;
    }

    /** Una fila de la matriz: id de TC12 y sus columnas por nombre. */
    private record Fila(String id, Map<String, String> columnas) {

        String get(String columna) {
            String valor = columnas.get(columna);
            return valor == null ? "" : valor;
        }
    }

    private static List<String> lineasCsv(String recurso) {
        try (InputStream in = Cdu12MatrizTdopTest.class.getClassLoader().getResourceAsStream(recurso)) {
            if (in == null) {
                throw new IllegalStateException("falta el recurso " + recurso);
            }
            String texto = new String(in.readAllBytes(), StandardCharsets.UTF_8)
                    .replace("\uFEFF", "");
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

    private static Stream<Fila> filas(String recurso) {
        List<String> lineas = lineasCsv(recurso);
        String[] cabecera = lineas.get(0).split(";", -1);
        return lineas.subList(1, lineas.size()).stream().map(linea -> {
            String[] c = linea.split(";", -1);
            Map<String, String> columnas = new LinkedHashMap<>();
            for (int i = 0; i < cabecera.length && i < c.length; i++) {
                columnas.put(cabecera[i], c[i]);
            }
            return new Fila(columnas.get("id"), columnas);
        });
    }

    private static Stream<Fila> filasAgendar() {
        return filas("tdop/cdu12-agendar.csv");
    }

    private static Stream<Fila> filasReprogramar() {
        return filas("tdop/cdu12-reprogramar.csv");
    }

    private static Stream<Fila> filasHuecos() {
        return filas("tdop/cdu12-huecos.csv");
    }

    private static Stream<Fila> filasListado() {
        return filas("tdop/cdu12-listado.csv");
    }

    private static Stream<Fila> filasOperaciones() {
        return filas("tdop/cdu12-operaciones.csv");
    }

    private static Stream<Fila> filasConjeturas() {
        return filas("tdop/cdu12-conjetura.csv");
    }

    @Autowired
    private MockMvc mvc;

    @Autowired
    private CitaRepositorio citas;

    @Autowired
    private CitaHuecoRepositorio huecos;

    @Autowired
    private AuditoriaCitasRepositorio auditoria;

    @Autowired
    private SolicitudFacade solicitudes;

    /** Fila que pasa = mundo limpio: borra citas, huecos y auditoria y reinicia los dobles. */
    @BeforeEach
    void limpiar() {
        auditoria.deleteAllInBatch();
        huecos.deleteAllInBatch();
        citas.deleteAllInBatch();
        ((PruebasCitas.SolicitudesFalsas) solicitudes).reiniciar();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("filasAgendar")
    void agendarDesdeMatriz(Fila f) throws Exception {
        aplicarSetup(f.get("setup"), inicioDe(f.get("inicio")));
        String cuerpo = cuerpoAgendar(f);
        MockHttpServletRequestBuilder peticion = post(URI_BASE)
                .contentType(MediaType.APPLICATION_JSON);
        if (cuerpo != null) {
            peticion.content(cuerpo);
        }
        verificar(f, mvc.perform(peticion));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("filasReprogramar")
    void reprogramarDesdeMatriz(Fila f) throws Exception {
        String id = aplicarSetup(f.get("setup"), null);
        String url = URI_BASE + "/" + (id != null ? id : ID_FALSA);
        String cuerpo = cuerpoReprogramacion(f);
        MockHttpServletRequestBuilder peticion = post(url + "/reprogramar")
                .contentType(MediaType.APPLICATION_JSON);
        if (cuerpo != null) {
            peticion.content(cuerpo);
        }
        verificar(f, mvc.perform(peticion));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("filasHuecos")
    void huecosDesdeMatriz(Fila f) throws Exception {
        aplicarSetup(f.get("setup"), null);
        String[] rango = rangoFecha(f.get("rango"), false);
        MockHttpServletRequestBuilder peticion = get(URI_HUECOS)
                .param("profesionalId", valorProfesional(f.get("profesionalId")))
                .contentType(MediaType.APPLICATION_JSON);
        if (rango[0] != null) {
            peticion.param("desde", rango[0]);
        }
        if (rango[1] != null) {
            peticion.param("hasta", rango[1]);
        }
        String duracion = duracionHueco(f.get("duracion"));
        if (duracion != null) {
            peticion.param("duracionMinutos", duracion);
        }
        verificar(f, mvc.perform(peticion));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("filasListado")
    void listadoDesdeMatriz(Fila f) throws Exception {
        aplicarSetup(f.get("setup"), null);
        String[] rango = rangoFecha(f.get("rango"), true);
        MockHttpServletRequestBuilder peticion = get(URI_BASE)
                .contentType(MediaType.APPLICATION_JSON);
        if (rango[0] != null) {
            peticion.param("desde", rango[0]);
        }
        if (rango[1] != null) {
            peticion.param("hasta", rango[1]);
        }
        String estado = valorEstado(f.get("estado"));
        if (estado != null) {
            peticion.param("estado", estado);
        }
        String filtroProfesional = filtroProfesional(f.get("profFiltro"));
        if (filtroProfesional != null) {
            peticion.param("profesionalId", filtroProfesional);
        }
        verificar(f, mvc.perform(peticion));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("filasOperaciones")
    void operacionesDesdeMatriz(Fila f) throws Exception {
        String id = aplicarSetup(f.get("setup"), null);
        String url = URI_BASE + "/" + (id != null ? id : ID_FALSA);
        ResultActions resultado = switch (f.get("operacion")) {
            case "CONFIRMAR" -> mvc.perform(post(url + "/confirmar"));
            case "REALIZAR" -> mvc.perform(post(url + "/realizar"));
            case "REPROGRAMAR" -> mvc.perform(post(url + "/reprogramar")
                    .contentType(MediaType.APPLICATION_JSON).content(CUERPO_REPRO));
            case "CANCELAR" -> mvc.perform(post(url + "/cancelar")
                    .contentType(MediaType.APPLICATION_JSON).content(CUERPO_CANCEL));
            case "DETALLE" -> mvc.perform(get(url));
            case "EXPIRAR_LOTE" -> mvc.perform(post(URI_BASE + "/expirar-vencidas")
                    .contentType(MediaType.APPLICATION_JSON).content(CUERPO_EXPIRA));
            default -> throw new IllegalStateException(
                    f.id() + ": operacion desconocida " + f.get("operacion"));
        };
        verificar(f, resultado);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("filasConjeturas")
    void conjeturasDesdeMatriz(Fila f) throws Exception {
        String id = aplicarSetup(f.get("setup"), null);
        String url = URI_BASE + "/" + (id != null ? id : ID_FALSA);
        ResultActions resultado = switch (f.get("operacion")) {
            case "AGENDAR" -> {
                MockHttpServletRequestBuilder peticion = post(URI_BASE);
                if ("TEXT_PLANO".equals(f.get("cuerpo"))) {
                    peticion.contentType(MediaType.TEXT_PLAIN).content("esto no es json");
                } else {
                    peticion.contentType(MediaType.APPLICATION_JSON);
                    String cuerpo = cuerpoConjetura(f.get("cuerpo"));
                    if (cuerpo != null) {
                        peticion.content(cuerpo);
                    }
                }
                yield mvc.perform(peticion);
            }
            case "REPROGRAMAR" -> mvc.perform(post(url + "/reprogramar")
                    .contentType(MediaType.APPLICATION_JSON).content(CUERPO_REPRO_PROPIO));
            case "CANCELAR" -> mvc.perform(post(url + "/cancelar")
                    .contentType(MediaType.APPLICATION_JSON).content(CUERPO_CANCEL_VACIO));
            case "AUDITORIA" -> mvc.perform(get(url + "/auditoria"));
            case "BAJA" -> mvc.perform(delete(url));
            default -> throw new IllegalStateException(
                    f.id() + ": operacion desconocida " + f.get("operacion"));
        };
        verificar(f, resultado);
    }
    /** Cuerpo con tipos que no caben en el record: la conversion revienta antes del servicio. */
    private static final String CUERPO_TIPO_ERRONEO =
            "{\"profesionalId\":\"" + PruebasCitas.PROF_MEDICO
                    + "\",\"inicio\":\"no-fecha\",\"duracionMinutos\":\"treinta\"}";

    /** El codigo esperado solo se comprueba si la fila lo pide; el HTTP siempre. */
    private void verificar(Fila f, ResultActions resultado) throws Exception {
        Esperado esperado = ESPERADOS.get(f.get("esperadoStatus"));
        assertThat(esperado)
                .as("estado %s desconocido en cdu12-esperados.csv", f.get("esperadoStatus"))
                .isNotNull();
        resultado.andExpect(status().is(esperado.http()));
        if (!"-".equals(f.get("esperadoCodigo"))) {
            assertThat(codigoApiError(resultado))
                    .as("%s: codigo de error", f.id())
                    .isEqualTo(f.get("esperadoCodigo"));
        }
    }

    /** Devuelve el codigo del ApiError, o null si la respuesta no trae cuerpo con codigo. */
    private static String codigoApiError(ResultActions resultado) throws Exception {
        String cuerpo = resultado.andReturn().getResponse().getContentAsString();
        if (cuerpo == null || cuerpo.isBlank()) {
            return null;
        }
        JsonNode raiz = MAPPER.readTree(cuerpo);
        JsonNode codigo = raiz.get("codigo");
        return codigo == null ? null : codigo.asText();
    }

    // ------------------------------------------------------------------
    // Preparacion del mundo de la fila (columna setup de los CSV)
    // ------------------------------------------------------------------

    /**
     * Interpreta la columna setup y devuelve el id tecnico de la cita sembrada (SEM-CITA...)
     * o null si la fila no siembra ninguna cita. SEM-SOL llega ya de PruebasCitas.reiniciar,
     * BODY y NINGUNO no preparan nada, y SEM-HUECO-* planta huecos sin cita detrás (fantasma)
     * para comprobar que exigeHuecoLibre solo mira la tabla de huecos.
     */
    private String aplicarSetup(String setup, LocalDateTime baseHueco) {
        LocalDateTime base = baseHueco != null ? baseHueco : PruebasCitas.INICIO;
        String idSembrado = null;
        for (String trozo : setup.split(",")) {
            String tok = trozo.trim();
            if (tok.startsWith("SEM-CITA-VENCIDA:")) {
                idSembrado = sembrarCita(tok.substring(17), INICIO_VENCIDA);
            } else if (tok.startsWith("SEM-CITA-CERCANO:")) {
                idSembrado = sembrarCita(tok.substring(17), INICIO_CERCANO);
            } else if (tok.startsWith("SEM-CITA:")) {
                idSembrado = sembrarCita(tok.substring(9), PruebasCitas.INICIO);
            } else if (tok.startsWith("SEM-HUECO-SOL:")) {
                sembrarHuecoFantasma("api-fantasma-sol", tok.substring(14), base);
            } else if (tok.startsWith("SEM-HUECO-DEST:")) {
                sembrarHuecoFantasma("api-fantasma-dest", tok.substring(15),
                        PruebasCitas.INICIO_TARDE);
            } else if (tok.equals("SEM-HUECO-FIJO")) {
                huecos.save(new CitaHueco("api-fantasma-fijo", PruebasCitas.PROF_MEDICO,
                        PruebasCitas.DIA.atTime(10, 0), PruebasCitas.DIA.atTime(10, 30)));
            }
        }
        return idSembrado;
    }

    /** Siembra una cita de prueba en el estado pedido y devuelve su id para montar la URL. */
    private String sembrarCita(String estado, LocalDateTime inicio) {
        Cita cita = Cita.nueva(PruebasCitas.SOL_CITADA, PruebasCitas.PROF_MEDICO, inicio,
                PruebasCitas.DURACION);
        switch (estado) {
            case "CONFIRMADA":
                cita.confirmar();
                break;
            case "REALIZADA":
                cita.confirmar();
                cita.realizar();
                break;
            case "EXPIRADA":
                cita.expirar();
                break;
            case "CANCELADA":
                cita.confirmar();
                cita.cancelar("cancelada en la siembra");
                break;
            default:
                break; // PROGRAMADA: no necesita ninguna transicion
        }
        return citas.save(cita).getCitaId();
    }

    /** Hueco activo sin cita detras: bloquea la franja aunque ningun Cita la ocupe. */
    private void sembrarHuecoFantasma(String prefijo, String tipo, LocalDateTime base) {
        if ("EXACTO".equals(tipo)) {
            huecos.save(new CitaHueco(prefijo + "-exacto", PruebasCitas.PROF_MEDICO, base,
                    base.plusMinutes(PruebasCitas.DURACION)));
        } else if ("SOLAPE".equals(tipo)) {
            huecos.save(new CitaHueco(prefijo + "-solape", PruebasCitas.PROF_MEDICO,
                    base.plusMinutes(15), base.plusMinutes(45)));
        }
        // LIBRE: no se siembra nada, el tramo pedido debe estar vacio
    }

    // ------------------------------------------------------------------
    // Tokens de las columnas -> valores reales del contrato
    // ------------------------------------------------------------------

    private static LocalDateTime inicioDe(String tok) {
        switch (tok) {
            case "AUT":
                return PruebasCitas.INICIO;
            case "APERTURA_JUSTA":
                return PruebasCitas.DIA.atTime(8, 0);
            case "JUSTO_CIERRE":
                return PruebasCitas.DIA.atTime(14, 30);
            case "PASADA_CIERRE":
                return PruebasCitas.DIA.atTime(14, 45);
            case "ANTERIOR_APERTURA":
                return PruebasCitas.DIA.atTime(7, 0);
            case "FESTIVO":
                return PruebasCitas.INICIO_FESTIVO;
            default:
                return null; // AUSENTE: el JSON no trae la clave
        }
    }

    private static Integer duracionDe(String tok) {
        switch (tok) {
            case "TREINTA":
                return PruebasCitas.DURACION;
            case "NEGATIVA":
                return -5;
            case "EXCESIVA":
                return 1441;
            default:
                return null; // AUSENTE: el record rellena 0, el catalogo la suple
        }
    }

    private static String solicitudDe(String tok) {
        switch (tok) {
            case "CREADA":
                return PruebasCitas.SOL_CREADA;
            case "CITADA":
                return PruebasCitas.SOL_CITADA;
            case "PRESCRITA":
                return PruebasCitas.SOL_PRESCRITA;
            case "INEXISTENTE":
                return PruebasCitas.SOL_INEXISTENTE;
            default:
                return null; // AUSENTE y NULA: sin clave o con valor null, mismo efecto
        }
    }

    private static LocalDateTime nuevoInicioDe(String tok) {
        switch (tok) {
            case "AUT":
                return PruebasCitas.INICIO;
            case "OCUPADO_EXACTO":
            case "OCUPADO_SOLAPE":
                return PruebasCitas.INICIO_TARDE;
            case "FUERA_FRANJA":
                return PruebasCitas.DIA.atTime(19, 0);
            default:
                return null; // AUSENTE: cuerpo legitimo sin la clave inicio
        }
    }

    private static String valorProfesional(String tok) {
        switch (tok) {
            case "MEDICO":
                return PruebasCitas.PROF_MEDICO;
            case "ESPACIOS":
                return "  " + PruebasCitas.PROF_MEDICO + "  ";
            case "VACIO":
                return "";
            case "SIN_AGENDA":
                return PruebasCitas.PROF_SIN_AGENDA;
            case "DESCONOCIDO":
                return "api-otro-profesional";
            default:
                return null; // AUSENTE: se omite la clave del JSON
        }
    }

    private static String duracionHueco(String tok) {
        switch (tok) {
            case "D30":
                return "30";
            case "D60":
                return "60";
            case "CERO":
                return "0";
            case "NEGATIVA":
                return "-5";
            default:
                return null; // AUSENTE: sin parametro, el catalogo fija la duracion
        }
    }

    /** Rango de la columna rango; fechaHora=true usa el formato ISO.DATE_TIME del listado. */
    private static String[] rangoFecha(String tok, boolean fechaHora) {
        String hora = fechaHora ? "T00:00:00" : "";
        switch (tok) {
            case "COHERENTE":
                return new String[] {"2026-11-12" + hora, "2026-11-14" + hora};
            case "IGUAL_DIA":
                return new String[] {"2026-11-13" + hora, "2026-11-13" + hora};
            case "SOLO_FESTIVO":
                return new String[] {"2026-11-10" + hora, "2026-11-10" + hora};
            case "INVERTIDO":
                return new String[] {"2026-11-14" + hora, "2026-11-12" + hora};
            case "TEXTO_ILEGAL":
                return new String[] {"hola", "2026-11-14" + hora};
            case "FECHA_CORTA":
                return new String[] {"2026-11", "2026-11-14" + hora};
            case "AUSENTE":
                if (fechaHora) {
                    return new String[] {null, null}; // listado: sin filtros de rango
                }
                return new String[] {"2026-11-12", null}; // huecos: hasta obligatorio ausente
            default:
                throw new IllegalStateException("token de rango desconocido: " + tok);
        }
    }

    private static String valorEstado(String tok) {
        switch (tok) {
            case "PROGRAMADA":
                return "PROGRAMADA";
            case "CONFIRMADA":
                return "CONFIRMADA";
            case "DESCONOCIDO":
                return "volando";
            default:
                return null; // AUSENTE: sin parametro estado
        }
    }

    private static String filtroProfesional(String tok) {
        switch (tok) {
            case "API_MEDICO":
                return PruebasCitas.PROF_MEDICO;
            case "ESPACIOS":
                return "  " + PruebasCitas.PROF_MEDICO + "  ";
            case "VACIO":
                return "";
            case "DESCONOCIDO":
                return "api-otro-profesional";
            default:
                return null; // AUSENTE: sin parametro profesionalId
        }
    }

    // ------------------------------------------------------------------
    // Cuerpos JSON de cada tabla
    // ------------------------------------------------------------------

    /** Armado del CitaNuevaDTO desde los tokens; null significa peticion sin contenido. */
    private static String cuerpoAgendar(Fila f) throws Exception {
        switch (f.get("cuerpo")) {
            case "VACIO":
                return null;
            case "JSON_MALFORMADO":
                return "{no-json";
            case "JSON_NULO":
                return "null";
            case "TIPO_ERRONEO":
                return CUERPO_TIPO_ERRONEO;
            default:
                return mapaCuerpo(valorProfesional(f.get("profesionalId")),
                        inicioDe(f.get("inicio")), duracionDe(f.get("duracion")),
                        solicitudDe(f.get("estadoSolicitud")));
        }
    }

    /** Cuerpo del ReprogramacionRequest desde los tokens de la tabla B. */
    private static String cuerpoReprogramacion(Fila f) throws Exception {
        switch (f.get("cuerpo")) {
            case "VACIO":
                return null;
            case "JSON_MALFORMADO":
                return "{no-json";
            case "JSON_NULO":
                return "null";
            case "JSON_VACIO":
                return "{}";
            case "TIPO_ERRONEO":
                return CUERPO_TIPO_ERRONEO;
            default:
                return mapaCuerpo(null, nuevoInicioDe(f.get("nuevoInicio")), null, null);
        }
    }

    /** Cuerpos fijos de la tabla de conjeturas (F), leidos del codigo actual. */
    private static String cuerpoConjetura(String tok) throws Exception {
        switch (tok) {
            case "VACIO":
                return null;
            case "JSON_NULO":
                return "null";
            case "JSON_SIN_DURACION":
                return mapaCuerpo(PruebasCitas.PROF_MEDICO, INICIO_FLEX, null,
                        PruebasCitas.SOL_CREADA);
            case "JSON_DUR_CERO":
                return mapaCuerpo(PruebasCitas.PROF_MEDICO, INICIO_FLEX, 0,
                        PruebasCitas.SOL_CREADA);
            case "JSON_DUR_NEGATIVA":
                return mapaCuerpo(PruebasCitas.PROF_MEDICO, INICIO_FLEX, -5, null);
            case "JSON_SIN_SOL":
                return mapaCuerpo(PruebasCitas.PROF_MEDICO, INICIO_FLEX, null, null);
            case "JSON_PROF_ESPACIOS":
                return mapaCuerpo("  " + PruebasCitas.PROF_MEDICO + "  ", INICIO_FLEX,
                        PruebasCitas.DURACION, null);
            case "JSON_SOL_ESPACIOS":
                return mapaCuerpo(PruebasCitas.PROF_MEDICO, INICIO_FLEX, PruebasCitas.DURACION,
                        "  ");
            default:
                throw new IllegalStateException("token de cuerpo de conjetura desconocido: " + tok);
        }
    }

    /** Serializa solo las claves con valor: una clave ausente deja el componente a null/0. */
    private static String mapaCuerpo(String profesional, LocalDateTime inicio, Integer duracion,
            String solicitudId) throws Exception {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        if (profesional != null) {
            cuerpo.put("profesionalId", profesional);
        }
        if (inicio != null) {
            cuerpo.put("inicio", inicio.toString());
        }
        if (duracion != null) {
            cuerpo.put("duracionMinutos", duracion);
        }
        if (solicitudId != null) {
            cuerpo.put("solicitudId", solicitudId);
        }
        return MAPPER.writeValueAsString(cuerpo);
    }

    /**
     * Corte web minimo: solo se escanean los controladores (y su consejo de errores) para que
     * el servicio lo aporte PruebasCitas con reloj fijo. Escanear el paquete de servicio al
     * tiempo crearia un segundo CitaServicio enfrentado al bean de las pruebas.
     */
    @SpringBootConfiguration
    @EnableAutoConfiguration
    @ComponentScan(basePackages = "es.uclm.iso2.citas.web")
    @EntityScan("es.uclm.iso2.citas.dominio")
    @EnableJpaRepositories("es.uclm.iso2.citas.repositorio")
    @Import(PruebasCitas.class)
    static class ConfigTdopWeb {
    }
}
