package es.uclm.iso2.catalogo.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Entidad JPA del catalogo de pruebas de imagen (CDU16, tabla {@code catalogo_prueba}).
 *
 * <p>Reglas de negocio encarnadas (RF16):</p>
 * <ul>
 *   <li>{@code codigo} unico e inmutable: la unicidad la garantiza la base de datos
 *       (restriccion {@code uk_catalogo_prueba_codigo}), no una comprobacion en memoria.</li>
 *   <li>Nunca hay borrado fisico: la baja es logica cambiando {@code activa = false},
 *       para que solicitudes y citas historicas sigan referenciando la prueba.</li>
 * </ul>
 *
 * @since 1.1.0 (CFG-01)
 */
@Entity
@Table(name = "catalogo_prueba",
        uniqueConstraints = @UniqueConstraint(name = "uk_catalogo_prueba_codigo", columnNames = "codigo"))
public class TipoPrueba {

    /** Clave tecnica sustituta; el negocio se referencia por {@code codigo}. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Codigo unico e inmutable de la prueba (ej. {@code RM-CERVICAL}). */
    @Column(nullable = false, length = 40)
    private String codigo;

    /** Nombre legible para profesionales. */
    @Column(nullable = false, length = 120)
    private String nombre;

    /** Modalidad de imagen: RX, TAC, RM, ECO, MED-NUC, DENS, OTRO. */
    @Column(nullable = false, length = 20)
    private String categoria;

    /** Si la prueba necesita cita previa con recursos asignables. */
    @Column(nullable = false)
    private boolean requiereCita;

    /** Duracion estimada de ejecucion en minutos (encaje de agendas). */
    @Column(nullable = false)
    private int duracionMinutos;

    /** Baja logica: una prueba desactivada no es seleccionable pero conserva su historico. */
    @Column(nullable = false)
    private boolean activa;

    /** Parametros de preparacion del paciente. */
    @Embedded
    private Protocolo protocolo;

    /** Parametros administrativos de cancelacion. */
    @Embedded
    private Politica politica;

    /**
     * Constructor JPA sin argumentos (requerido por Hibernate).
     */
    protected TipoPrueba() {
    }

    /**
     * Crea una prueba del catalogo con todos sus parametros de negocio.
     *
     * @param codigo          codigo unico e inmutable
     * @param nombre          nombre legible
     * @param categoria       modalidad de imagen
     * @param requiereCita    si necesita cita previa
     * @param duracionMinutos duracion estimada en minutos (&gt; 0)
     * @param activa          estado de baja logica (el alta siempre nace activa)
     * @param protocolo       preparacion del paciente
     * @param politica        reglas administrativas
     */
    public TipoPrueba(String codigo, String nombre, String categoria, boolean requiereCita,
                      int duracionMinutos, boolean activa, Protocolo protocolo, Politica politica) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.categoria = categoria;
        this.requiereCita = requiereCita;
        this.duracionMinutos = duracionMinutos;
        this.activa = activa;
        this.protocolo = protocolo;
        this.politica = politica;
    }

    /**
     * Actualiza los parametros de gestion (no toca id, codigo ni estado de baja logica).
     *
     * @param nombre          nuevo nombre
     * @param categoria       nueva modalidad
     * @param requiereCita    si necesita cita previa
     * @param duracionMinutos nueva duracion en minutos
     * @param protocolo       nueva preparacion
     * @param politica        nuevas reglas administrativas
     */
    public void actualizarDatos(String nombre, String categoria, boolean requiereCita,
                                int duracionMinutos, Protocolo protocolo, Politica politica) {
        this.nombre = nombre;
        this.categoria = categoria;
        this.requiereCita = requiereCita;
        this.duracionMinutos = duracionMinutos;
        this.protocolo = protocolo;
        this.politica = politica;
    }

    /**
     * Cambia el estado de baja logica de la prueba (baja o reactivacion).
     *
     * @param activo nuevo estado
     */
    public void cambiarEstado(boolean activo) {
        this.activa = activo;
    }

    /**
     * Devuelve la clave tecnica sustituta.
     *
     * @return id interno
     */
    public Long getId() {
        return id;
    }

    /**
     * Devuelve el codigo unico de la prueba.
     *
     * @return codigo
     */
    public String getCodigo() {
        return codigo;
    }

    /**
     * Devuelve el nombre legible.
     *
     * @return nombre
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Devuelve la modalidad de imagen.
     *
     * @return categoria
     */
    public String getCategoria() {
        return categoria;
    }

    /**
     * Devuelve si la prueba requiere cita previa.
     *
     * @return requiere cita
     */
    public boolean isRequiereCita() {
        return requiereCita;
    }

    /**
     * Devuelve la duracion estimada en minutos.
     *
     * @return duracion
     */
    public int getDuracionMinutos() {
        return duracionMinutos;
    }

    /**
     * Devuelve el estado de baja logica.
     *
     * @return activa
     */
    public boolean isActiva() {
        return activa;
    }

    /**
     * Devuelve los parametros de preparacion.
     *
     * @return protocolo
     */
    public Protocolo getProtocolo() {
        return protocolo;
    }

    /**
     * Devuelve los parametros administrativos.
     *
     * @return politica
     */
    public Politica getPolitica() {
        return politica;
    }
}
