package es.uclm.iso2.catalogo.servicio;

import java.util.Set;

/**
 * Cotas que vigila la capa de dominio del catalogo antes de escribir en la base de datos
 * (CDU16, decision D35).
 *
 * <p>Son el espejo del esquema autoritativo {@code V1.1.0__cdu16_catalogo.sql}. El contrato
 * comun ({@code PruebaCatalogoFacade}) ya rechaza lo que va vacio o es negativo, pero no
 * mira longitudes ni el dominio de {@code categoria}: sin estas cotas, un texto que no cabe
 * en el {@code VARCHAR} llega hasta el INSERT y Hibernate lo convierte en un 500.</p>
 *
 * <p>No son cifras negociables por codigo. Cambiar una obliga a cambiar antes la migracion
 * y el diagrama de clases de diseno de iter01 (D31), y a reejecutar la matriz TDOP de
 * {@code docs/test/iter01}.</p>
 *
 * <p>Nota: el PUD de analisis habla de codigos de 2 a 20 caracteres y el esquema reserva
 * {@code VARCHAR(40)}. Se valida contra la cota de almacenamiento; el desajuste esta
 * anotado en el diagrama y pendiente de formalizar, no se inventa aqui una regla de negocio
 * que el PUD no cifra.</p>
 *
 * @since 1.1.0 (CFG-01, D35)
 */
public final class LimitesCatalogo {

    /** Caracteres maximos de {@code codigo} ({@code VARCHAR(40)}). */
    public static final int CODIGO_MAX = 40;

    /** Caracteres maximos de {@code nombre} ({@code VARCHAR(120)}). */
    public static final int NOMBRE_MAX = 120;

    /** Caracteres maximos de {@code categoria} ({@code VARCHAR(20)}). */
    public static final int CATEGORIA_MAX = 20;

    /** Caracteres maximos de los textos libres del protocolo ({@code VARCHAR(1000)}). */
    public static final int TEXTO_PROTOCOLO_MAX = 1000;

    /** Minutos maximos de duracion estimada de una prueba (24 h). */
    public static final int DURACION_MAX = 1440;

    /** Horas de ayuno maximas admisibles (30 dias). */
    public static final int AYUNO_HORAS_MAX = 720;

    /** Horas maximas de plazo de cancelacion (un ano). */
    public static final int PLAZO_CANCELACION_MAX = 8760;

    /** Maximo de cancelaciones admitido por prueba. */
    public static final int MAX_CANCELACIONES_MAX = 100;

    /**
     * Categorias de prueba admitidas. Es el mismo dominio que muestra el formulario del
     * cliente JavaFX ({@code VistaAdminCatalogo}), que hasta ahora no estaba defendido en
     * el servidor: cualquier cadena se aceptaba y se guardaba.
     */
    public static final Set<String> CATEGORIAS =
            Set.of("RX", "TAC", "RM", "ECO", "MED-NUC", "DENS", "OTRO");

    /**
     * Campos por los que se puede ordenar el listado. Lista blanca: evita que un valor
     * inventado acabe en {@code Sort.by} y provoque un 500.
     */
    public static final Set<String> ORDENES_VALIDOS =
            Set.of("codigo", "nombre", "categoria", "duracionMinutos");

    /** Utilidad sin instancia. */
    private LimitesCatalogo() {
    }
}
