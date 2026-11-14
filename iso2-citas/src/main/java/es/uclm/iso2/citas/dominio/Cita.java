package es.uclm.iso2.citas.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Cita sobre la agenda de un profesional (CDU12). Es la raiz de agregado del modulo:
 * el hueco que bloquea y el rastro de auditoria cuelgan de su {@link #getCitaId() citaId},
 * un identificador funcional unico generado al agendar.
 *
 * <p>El ciclo de vida lo fija la maquina de estados de la figura de analisis: una cita
 * nace {@link Estado#PROGRAMADA} y solo admite las transiciones
 * programada -&gt; confirmada -&gt; realizada, programada o confirmada -&gt; cancelada y
 * programada -&gt; expirada. El dominio es el que las rechaza lanzando
 * {@link IllegalStateException}; el servicio las traduce al error de contrato
 * {@code CitaConflictoException(TRANSICION_INVALIDA)} para que la API devuelva 409.</p>
 *
 * <p>La duracion no se duplica en una columna: se deriva del par {@code inicio}/{@code fin},
 * que es lo que ensena la pantalla de recepcion. La cancelacion guarda el motivo porque
 * UC12.5 lo pide expresamente.</p>
 *
 * @since 1.1.0 (CFG-05, CDU12)
 */
@Entity
@Table(name = "cita")
public class Cita {

    /**
     * Estados de la maquina de citas de la figura de analisis CDU12. En la API REST se
     * serializan en minusculas ({@code programada}, {@code confirmada}, {@code realizada},
     * {@code cancelada}, {@code expirada}).
     */
    public enum Estado {
        /** Hueco reservado a la espera de la confirmacion del paciente. */
        PROGRAMADA,
        /** Asistencia confirmada: el hueco sigue bloqueado. */
        CONFIRMADA,
        /** Prueba atendida: terminal, el hueco se libera. */
        REALIZADA,
        /** Baja con motivo: terminal, el hueco se libera y la solicitud vuelve a creada. */
        CANCELADA,
        /** Nadie acudio y nadie cancelo: terminal, el hueco se libera. */
        EXPIRADA;

        /**
         * Indica si el estado ya no admite ninguna transicion.
         *
         * @return true si el estado es terminal
         */
        public boolean esTerminal() {
            return this == REALIZADA || this == CANCELADA || this == EXPIRADA;
        }
    }

    /** Clave tecnica; la de negocio es {@link #getCitaId()}. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Identificador funcional de la cita, unico. */
    @Column(name = "cita_id", nullable = false, unique = true, length = 36)
    private String citaId;

    /** Solicitud que origino la cita; {@code null} en citas directas de recepcion. */
    @Column(name = "solicitud_id", length = 36)
    private String solicitudId;

    /** Profesional (medico radiologo o tecnico) cuya agenda ocupa. */
    @Column(name = "profesional_id", nullable = false, length = 60)
    private String profesionalId;

    /** Fecha y hora de inicio del hueco reservado. */
    @Column(nullable = false)
    private LocalDateTime inicio;

    /** Fecha y hora de fin, derivadas de la duracion de la prueba. */
    @Column(nullable = false)
    private LocalDateTime fin;

    /** Estado de la maquina de citas. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Estado estado;

    /** Motivo de la cancelacion (UC12.5); {@code null} mientras no se cancela. */
    @Column(name = "motivo_cancelacion", length = 300)
    private String motivoCancelacion;

    /** Reconstructora de JPA: no fija estado, Hibernate lo escribe despues. */
    protected Cita() {
    }

    private Cita(String citaId, String solicitudId, String profesionalId,
            LocalDateTime inicio, LocalDateTime fin) {
        this.citaId = citaId;
        this.solicitudId = solicitudId;
        this.profesionalId = profesionalId;
        this.inicio = inicio;
        this.fin = fin;
        this.estado = Estado.PROGRAMADA;
    }

    /**
     * Crea una cita sin persistir, con identificador funcional nuevo y en estado
     * {@link Estado#PROGRAMADA}: agendar es la unica forma de nacer una cita (UC12.2).
     *
     * @param solicitudId     solicitud que la origina, {@code null} o vacia en citas directas
     * @param profesionalId   profesional cuya agenda ocupa
     * @param inicio          fecha y hora de inicio
     * @param duracionMinutos duracion estimada de la prueba, en minutos
     * @return la cita nacida, lista para bloquear su hueco
     * @throws IllegalArgumentException si faltan datos o la duracion no es positiva
     */
    public static Cita nueva(String solicitudId, String profesionalId, LocalDateTime inicio,
            int duracionMinutos) {
        if (profesionalId == null || profesionalId.isBlank()) {
            throw new IllegalArgumentException("la cita necesita un profesional");
        }
        if (inicio == null) {
            throw new IllegalArgumentException("la cita necesita un inicio");
        }
        if (duracionMinutos <= 0) {
            throw new IllegalArgumentException("la cita necesita una duracion positiva en minutos");
        }
        String solicitud = (solicitudId == null || solicitudId.isBlank()) ? null : solicitudId.trim();
        return new Cita(UUID.randomUUID().toString(), solicitud, profesionalId.trim(), inicio,
                inicio.plusMinutes(duracionMinutos));
    }

    // --- Transiciones de la maquina de estados ----------------------------

    /**
     * Confirma la asistencia del paciente sobre una cita programada.
     *
     * @throws IllegalStateException si la cita no esta programada
     */
    public void confirmar() {
        if (estado != Estado.PROGRAMADA) {
            throw new IllegalStateException("solo se puede confirmar una cita programada, y estaba " + estado);
        }
        this.estado = Estado.CONFIRMADA;
    }

    /**
     * Registra la prueba como atendida sobre una cita programada o confirmada.
     *
     * @throws IllegalStateException si la cita no esta programada ni confirmada
     */
    public void realizar() {
        if (estado != Estado.PROGRAMADA && estado != Estado.CONFIRMADA) {
            throw new IllegalStateException(
                    "solo se puede realizar una cita programada o confirmada, y estaba " + estado);
        }
        this.estado = Estado.REALIZADA;
    }

    /**
     * Cancela la cita con el motivo exigido por UC12.5.
     *
     * @param motivo motivo de la cancelacion
     * @throws IllegalStateException si la cita ya es terminal
     */
    public void cancelar(String motivo) {
        exigeNoTerminal();
        this.estado = Estado.CANCELADA;
        this.motivoCancelacion = motivo;
    }

    /**
     * Marca la cita como vencida por el lote de expiracion (UC12.7).
     *
     * @throws IllegalStateException si la cita ya es terminal
     */
    public void expirar() {
        exigeNoTerminal();
        this.estado = Estado.EXPIRADA;
    }

    /**
     * Mueve la cita a un hueco nuevo conservando su estado (UC12.4). La reprogramacion no
     * reinicia la maquina: una cita confirmada sigue confirmada en la hora nueva.
     *
     * @param nuevoInicio     nuevo inicio
     * @param duracionMinutos duracion que se conserva de la cita
     * @throws IllegalStateException si la cita ya es terminal
     */
    public void moverA(LocalDateTime nuevoInicio, int duracionMinutos) {
        exigeNoTerminal();
        this.inicio = nuevoInicio;
        this.fin = nuevoInicio.plusMinutes(duracionMinutos);
    }

    // --- Consultas del modelo ---------------------------------------------

    /**
     * Indica si la cita sigue ocupando su hueco: las canceladas, realizadas y vencidas
     * dejan de bloquear la agenda.
     *
     * @return true si el estado es programada o confirmada
     */
    public boolean esActiva() {
        return estado == Estado.PROGRAMADA || estado == Estado.CONFIRMADA;
    }

    /**
     * Duracion de la cita en minutos, derivada del intervalo reservado.
     *
     * @return minutos entre inicio y fin
     */
    public int duracionMinutos() {
        return (int) Duration.between(inicio, fin).toMinutes();
    }

    private void exigeNoTerminal() {
        if (estado.esTerminal()) {
            throw new IllegalStateException("la cita ya esta en estado terminal " + estado);
        }
    }

    // --- Getters de JPA ---------------------------------------------------

    /**
     * Devuelve la clave tecnica de la fila.
     *
     * @return id tecnico, {@code null} antes de persistir
     */
    public Long getId() {
        return id;
    }

    /**
     * Devuelve el identificador funcional de la cita.
     *
     * @return citaId unico
     */
    public String getCitaId() {
        return citaId;
    }

    /**
     * Devuelve la solicitud asociada.
     *
     * @return solicitudId, {@code null} en citas directas
     */
    public String getSolicitudId() {
        return solicitudId;
    }

    /**
     * Devuelve el profesional cuya agenda ocupa.
     *
     * @return profesionalId
     */
    public String getProfesionalId() {
        return profesionalId;
    }

    /**
     * Devuelve el inicio del hueco reservado.
     *
     * @return inicio
     */
    public LocalDateTime getInicio() {
        return inicio;
    }

    /**
     * Devuelve el fin del hueco reservado.
     *
     * @return fin
     */
    public LocalDateTime getFin() {
        return fin;
    }

    /**
     * Devuelve el estado de la maquina de citas.
     *
     * @return estado
     */
    public Estado getEstado() {
        return estado;
    }

    /**
     * Devuelve el motivo de la cancelacion.
     *
     * @return motivo, {@code null} si la cita no esta cancelada
     */
    public String getMotivoCancelacion() {
        return motivoCancelacion;
    }
}
