package es.uclm.iso2.citas.dominio;

import es.uclm.iso2.common.solicitudes.Prioridad;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.Comparator;
import java.util.UUID;

/**
 * Entrada de la lista de espera de una prueba (CDU14, CFG-07). El alta nace
 * {@link Estado#ACTIVA} y solo admite dos salidas terminales: {@link #asignarA(String)
 * asignar} una cita desde la cola o {@link #retirar(String) retirar} con motivo.
 *
 * <p>La posicion en la cola NO se persiste: se calcula ordenando en memoria con
 * {@link #porOrdenDeEspera()}, que es el orden determinista que exige RF14.3
 * (prioridad descendente con la escala CDU06 y, a igualdad de prioridad,
 * antiguedad ascendente con la fecha de solicitud, que es inmutable tras el alta).
 * El desempate final por {@code esperaId} garantiza que dos lecturas del mismo
 * estado devuelven exactamente el mismo orden.</p>
 *
 * <p>La anti-reincidencia usa la misma tecnica ya probada en CDU12/CDU13/CDU16:
 * {@code claveDuplicada = pacienteId + "|" + pruebaCodigo} mientras la entrada esta
 * ACTIVA y pasa a {@code null} al retirar o asignar, de forma que el UNIQUE de la
 * tabla solo permite una fila no nula por paciente y prueba.</p>
 *
 * @since 1.2.0 (CFG-07, CDU14)
 */
@Entity
@Table(name = "lista_espera")
public class ListaEspera {

    /**
     * Estados de la entrada de la cola. En la API REST se serializan en minusculas
     * ({@code activa}, {@code asignada}, {@code retirada}).
     */
    public enum Estado {
        /** En la cola, a la espera de hueco. */
        ACTIVA,
        /** Se le asigno una cita desde la cola: terminal, sale de la cola activa. */
        ASIGNADA,
        /** Retirada con motivo: terminal, deja libre la plaza de duplicado. */
        RETIRADA;

        /**
         * Indica si el estado ya no admite ninguna transicion.
         *
         * @return true si el estado es terminal
         */
        public boolean esTerminal() {
            return this == ASIGNADA || this == RETIRADA;
        }
    }

    /** Clave tecnica; la de negocio es {@link #getEsperaId()}. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Identificador funcional de la entrada, unico. */
    @Column(name = "espera_id", nullable = false, unique = true, length = 36)
    private String esperaId;

    /** Solicitud de origen (CDU06); {@code null} en altas directas del Administrativo. */
    @Column(name = "solicitud_id", length = 36)
    private String solicitudId;

    /** Paciente en espera. */
    @Column(name = "paciente_id", nullable = false, length = 60)
    private String pacienteId;

    /** Prueba del catalogo que se espera. */
    @Column(name = "prueba_codigo", nullable = false, length = 60)
    private String pruebaCodigo;

    /** Profesional deseado; {@code null} si vale cualquiera. */
    @Column(name = "profesional_id", length = 60)
    private String profesionalId;

    /** Prioridad CDU06 que manda en el orden de la cola. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Prioridad prioridad;

    /** Instante del alta: la clave de desempate determinista, inmutable tras el alta. */
    @Column(name = "fecha_solicitud", nullable = false)
    private Instant fechaSolicitud;

    /** Estado de la entrada. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Estado estado;

    /** Cita creada al asignar desde la cola; {@code null} mientras no se asigna. */
    @Column(name = "cita_id", length = 36)
    private String citaId;

    /** Motivo del retiro (UC14.4); {@code null} mientras no se retira. */
    @Column(name = "motivo_fin", length = 300)
    private String motivoFin;

    /**
     * Clave anti-reincidencia: {@code pacienteId|pruebaCodigo} mientras esta ACTIVA y
     * {@code null} al retirar o asignar; el UNIQUE de la tabla no cuenta los nulos.
     */
    @Column(name = "clave_duplicada", unique = true, length = 121)
    private String claveDuplicada;

    /**
     * Control optimista (TC14-E10): dos retiradas concurrentes sobre la misma entrada
     * no pueden escribir dos eslabones RETIRADA en la auditoria. El segundo UPDATE
     * encuentra version desfasada y Hibernate lanza ObjectOptimisticLockingFailureException,
     * que el consejo de errores traduce en 409 CONFLICTO_ENTRADA_NO_ACTIVA (el mismo
     * codigo que recibe el hilo que llega tarde por la maquina de estados); mismo
     * criterio que {@link CitaHueco} en CDU12.
     */
    @Version
    @Column(nullable = false)
    private int version;

    /** Reconstructora de JPA: no fija estado, Hibernate lo escribe despues. */
    protected ListaEspera() {
    }

    private ListaEspera(String esperaId, String solicitudId, String pacienteId,
            String pruebaCodigo, String profesionalId, Prioridad prioridad,
            Instant fechaSolicitud) {
        this.esperaId = esperaId;
        this.solicitudId = (solicitudId == null || solicitudId.isBlank()) ? null : solicitudId;
        this.pacienteId = pacienteId;
        this.pruebaCodigo = pruebaCodigo;
        this.profesionalId = (profesionalId == null || profesionalId.isBlank()) ? null : profesionalId;
        this.prioridad = prioridad;
        this.fechaSolicitud = fechaSolicitud;
        this.estado = Estado.ACTIVA;
        this.claveDuplicada = pacienteId + "|" + pruebaCodigo;
    }

    /**
     * Crea una entrada sin persistir, con identificador funcional nuevo y en estado
     * {@link Estado#ACTIVA}: dar de alta es la unica forma de nacer una entrada (UC14.1).
     *
     * @param solicitudId    solicitud de origen, {@code null} o vacia en alta directa
     * @param pacienteId     paciente en espera, obligatorio
     * @param pruebaCodigo   codigo de la prueba esperada, obligatorio
     * @param profesionalId  profesional deseado, {@code null} si vale cualquiera
     * @param prioridad      prioridad de la escala CDU06, obligatoria
     * @param fechaSolicitud instante del alta (reloj inyectable del servicio)
     * @return la entrada nacida, lista para entrar en la cola
     */
    public static ListaEspera nueva(String solicitudId, String pacienteId, String pruebaCodigo,
            String profesionalId, Prioridad prioridad, Instant fechaSolicitud) {
        return new ListaEspera(UUID.randomUUID().toString(), solicitudId, pacienteId,
                pruebaCodigo, profesionalId, prioridad, fechaSolicitud);
    }

    /**
     * Cambia la prioridad de la entrada (reubicacion, UC14.3). La
     * {@code fechaSolicitud} no se toca: la reubicacion manda por prioridad, no
     * reinicia la antiguedad.
     *
     * @param nueva prioridad nueva de la escala CDU06
     * @return la prioridad anterior, para el eslabon REUBICACION de la auditoria
     * @throws IllegalStateException si la entrada ya esta en estado terminal
     */
    public Prioridad cambiarPrioridad(Prioridad nueva) {
        if (estado.esTerminal()) {
            throw new IllegalStateException("la entrada " + esperaId + " ya no esta activa");
        }
        Prioridad anterior = prioridad;
        this.prioridad = nueva;
        return anterior;
    }

    /**
     * Retira la entrada de la cola con el motivo obligatorio (UC14.4) y libera la
     * clave anti-reincidencia.
     *
     * @param motivo motivo del retiro, obligatorio
     * @throws IllegalStateException si la entrada ya esta en estado terminal
     */
    public void retirar(String motivo) {
        if (estado.esTerminal()) {
            throw new IllegalStateException("la entrada " + esperaId + " ya no esta activa");
        }
        this.estado = Estado.RETIRADA;
        this.motivoFin = motivo;
        this.claveDuplicada = null;
    }

    /**
     * Marca la entrada como asignada a la cita creada desde la cola (UC14.2) y
     * libera la clave anti-reincidencia.
     *
     * @param citaId cita del CDU12 recien creada
     * @throws IllegalStateException si la entrada ya esta en estado terminal
     */
    public void asignarA(String citaId) {
        if (estado.esTerminal()) {
            throw new IllegalStateException("la entrada " + esperaId + " ya no esta activa");
        }
        this.estado = Estado.ASIGNADA;
        this.citaId = citaId;
        this.claveDuplicada = null;
    }

    /**
     * Indica si la entrada sigue en la cola activa.
     *
     * @return true si el estado es {@link Estado#ACTIVA}
     */
    public boolean estaActiva() {
        return estado == Estado.ACTIVA;
    }

    /**
     * Comprueba si la entrada ya estaria en la misma posicion con esa prioridad:
     * a igualdad de prioridad la antiguedad manda y {@code fechaSolicitud} es
     * inmutable, asi que la ubicacion coincide si la prioridad es la misma.
     *
     * @param otra prioridad pedida en la reubicacion
     * @return true si la reubicacion no cambiaria nada (REDUNDANTE)
     */
    public boolean esMismaUbicacionQue(Prioridad otra) {
        return prioridad == otra;
    }

    /**
     * Orden determinista de la cola (RF14.3): prioridad descendente con la escala
     * CDU06 declarada como escala (sin metodo peso), antiguedad ascendente y
     * desempate estable por {@code esperaId}. Es el mismo orden que el
     * {@code ORDER BY} del repositorio, para que la posicion calculada en memoria
     * coincida con la fila que pinta la web.
     *
     * @return el comparador de la cola
     */
    public static Comparator<ListaEspera> porOrdenDeEspera() {
        return Comparator.comparing(ListaEspera::getPrioridad, Comparator.reverseOrder())
                .thenComparing(ListaEspera::getFechaSolicitud)
                .thenComparing(ListaEspera::getEsperaId);
    }

    // --- Getters de JPA ----------------------------------------------------

    /**
     * Devuelve la clave tecnica de la entrada.
     *
     * @return id tecnico
     */
    public Long getId() {
        return id;
    }

    /**
     * Devuelve el identificador funcional de la entrada.
     *
     * @return esperaId
     */
    public String getEsperaId() {
        return esperaId;
    }

    /**
     * Devuelve la solicitud de origen.
     *
     * @return solicitudId, {@code null} en alta directa
     */
    public String getSolicitudId() {
        return solicitudId;
    }

    /**
     * Devuelve el paciente en espera.
     *
     * @return pacienteId
     */
    public String getPacienteId() {
        return pacienteId;
    }

    /**
     * Devuelve el codigo de la prueba esperada.
     *
     * @return pruebaCodigo
     */
    public String getPruebaCodigo() {
        return pruebaCodigo;
    }

    /**
     * Devuelve el profesional deseado.
     *
     * @return profesionalId, {@code null} si vale cualquiera
     */
    public String getProfesionalId() {
        return profesionalId;
    }

    /**
     * Devuelve la prioridad de la entrada.
     *
     * @return prioridad CDU06
     */
    public Prioridad getPrioridad() {
        return prioridad;
    }

    /**
     * Devuelve el instante del alta.
     *
     * @return fechaSolicitud, inmutable tras el alta
     */
    public Instant getFechaSolicitud() {
        return fechaSolicitud;
    }

    /**
     * Devuelve el estado de la entrada.
     *
     * @return estado
     */
    public Estado getEstado() {
        return estado;
    }

    /**
     * Devuelve la cita creada al asignar.
     *
     * @return citaId, {@code null} mientras no se asigna
     */
    public String getCitaId() {
        return citaId;
    }

    /**
     * Devuelve el motivo del retiro.
     *
     * @return motivoFin, {@code null} mientras no se retira
     */
    public String getMotivoFin() {
        return motivoFin;
    }

    /**
     * Devuelve la clave anti-reincidencia.
     *
     * @return claveDuplicada, {@code null} en estados terminales
     */
    public String getClaveDuplicada() {
        return claveDuplicada;
    }

    /**
     * Devuelve la version del control optimista.
     *
     * @return version gestionada por JPA
     */
    public int getVersion() {
        return version;
    }
}
