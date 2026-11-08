package es.uclm.iso2.solicitudes.dominio;

import es.uclm.iso2.common.solicitudes.Prioridad;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.UUID;

/**
 * Solicitud de una prueba de imagen prescrita desde Atencion Primaria (CDU06). Las
 * reglas del CDU viven aqui: los campos obligatorios de la RF06.1 se validan al
 * construir, y el no repudio (RF06.2) se sella con una huella SHA-256 del canonico
 * {@code paciente|codigoPrueba|indicacion|prioridad|origen|medico|creadaEn} que se
 * calcula una unica vez en la creacion y ya no cambia: la entidad no expone setters.
 * {@link #trazaIntegra()} recalcula la huella y revela manipulaciones directas en la
 * base de datos.
 *
 * @since 1.1.0 (CFG-04)
 */
@Entity
@Table(name = "solicitud")
public class Solicitud {

    /** Estados del ciclo de vida de una solicitud (matriz CFG-04). */
    public enum Estado {
        /** Recien prescrita por el medico. */
        CREADA,
        /** Le han asignado una cita (CDU12, futuro). */
        CITADA,
        /** La prueba ya se realizo (CDU04/CDU11, futuro). */
        REALIZADA,
        /** Existe informe de la prueba (CDU09, futuro). */
        INFORMADA,
        /** Baja logica: no cuenta como duplicada y deja historial. */
        CANCELADA
    }

    /** Clave tecnica del registro. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Identificador funcional de la solicitud (UUID). Unico. */
    @Column(name = "solicitud_id", nullable = false, length = 36, unique = true)
    private String solicitudId;

    /** Historia clinica del paciente (RF06.1). */
    @Column(name = "paciente_id", nullable = false, length = 40)
    private String pacienteId;

    /** Codigo de la prueba pedida, del catalogo de CDU16. */
    @Column(name = "codigo_prueba", nullable = false, length = 40)
    private String codigoPrueba;

    /** Indicacion clinica de la peticion (RF06.1). */
    @Column(name = "indicacion_clinica", nullable = false, length = 500)
    private String indicacionClinica;

    /** Prioridad clinica de la solicitud (RF06.1). */
    @Enumerated(EnumType.STRING)
    @Column(name = "prioridad", nullable = false, length = 20)
    private Prioridad prioridad;

    /** Origen de la peticion: AP, hospital, urgencias... */
    @Column(name = "origen", nullable = false, length = 40)
    private String origen;

    /** Medico autor de la prescripcion: claim del JWT en el alta (RF06.2, no repudio). */
    @Column(name = "medico_id", nullable = false, length = 60)
    private String medicoId;

    /** Instante de creacion, usado en el canonico de la huella. */
    @Column(name = "creada_en", nullable = false)
    private Instant creadaEn;

    /** Estado del ciclo de vida. */
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private Estado estado;

    /** Huella SHA-256 (hex) calculada una sola vez en la creacion (RF06.2). */
    @Column(name = "huella", nullable = false, length = 64)
    private String huella;

    /**
     * Constructor JPA sin argumentos (requerido por Hibernate).
     */
    protected Solicitud() {
    }

    /**
     * Crea una solicitud en estado {@link Estado#CREADA} y sella su huella de no
     * repudio sobre el canonico de RF06.2.
     *
     * @param pacienteId        historia clinica (no vacia)
     * @param codigoPrueba      codigo del catalogo (no vacio)
     * @param indicacionClinica motivo clinico (no vacio)
     * @param prioridad         prioridad clinica (no nula)
     * @param origen            origen de la peticion (no vacio)
     * @param medicoId          autor medico (no vacio)
     * @param creadaEn          instante de creacion (no nulo; se trunca a microsegundos,
     *                          la precision de la BD, y entra en la huella)
     * @throws IllegalArgumentException si algun campo obligatorio es invalido (RF06.1)
     */
    public Solicitud(String pacienteId, String codigoPrueba, String indicacionClinica,
                     Prioridad prioridad, String origen, String medicoId, Instant creadaEn) {
        if (pacienteId == null || pacienteId.isBlank()) {
            throw new IllegalArgumentException("la solicitud necesita un paciente");
        }
        if (codigoPrueba == null || codigoPrueba.isBlank()) {
            throw new IllegalArgumentException("la solicitud necesita una prueba");
        }
        if (indicacionClinica == null || indicacionClinica.isBlank()) {
            throw new IllegalArgumentException("la solicitud necesita una indicacion clinica");
        }
        if (prioridad == null) {
            throw new IllegalArgumentException("la solicitud necesita una prioridad");
        }
        if (origen == null || origen.isBlank()) {
            throw new IllegalArgumentException("la solicitud necesita un origen");
        }
        if (medicoId == null || medicoId.isBlank()) {
            throw new IllegalArgumentException("la solicitud necesita un medico autor");
        }
        if (creadaEn == null) {
            throw new IllegalArgumentException("la solicitud necesita un instante de creacion");
        }
        this.solicitudId = UUID.randomUUID().toString();
        this.pacienteId = pacienteId.trim();
        this.codigoPrueba = codigoPrueba.trim();
        this.indicacionClinica = indicacionClinica.trim();
        this.prioridad = prioridad;
        this.origen = origen.trim();
        this.medicoId = medicoId.trim();
        // El canonico incluye el Instant como texto y la BD lo guarda con precision de
        // microsegundos (TIMESTAMP(6)): se trunca aqui, antes de sellar la huella, para
        // que el recalculo sobre la fila leida dé exactamente el mismo canonico.
        this.creadaEn = creadaEn.truncatedTo(ChronoUnit.MICROS);
        this.estado = Estado.CREADA;
        this.huella = huellaCalculada();
    }

    /**
     * Canonico de no repudio (RF06.2): los campos firmados unidos por barras verticales.
     *
     * @return texto canonico sobre el que se calcula la huella
     */
    public String canonico() {
        return pacienteId + "|" + codigoPrueba + "|" + indicacionClinica + "|" + prioridad.name()
                + "|" + origen + "|" + medicoId + "|" + creadaEn;
    }

    /**
     * Recalcula la huella SHA-256 (hex) sobre el {@link #canonico()} actual.
     *
     * @return huella actual del contenido
     */
    public String huellaCalculada() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(canonico().getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("el JDK no soporta SHA-256", e);
        }
    }

    /**
     * Verifica el no repudio: la huella guardada sigue coincidiendo con el contenido.
     * Si alguien manipulo la fila en la base de datos, devuelve {@code false}.
     *
     * @return true si la traza esta integra
     */
    public boolean trazaIntegra() {
        return huella.equals(huellaCalculada());
    }

    /**
     * Si la solicitud sigue en curso (no cancelada): solo las activas bloquean una
     * nueva prescripcion del mismo medico, paciente y prueba (RF06.3).
     *
     * @return true si el estado no es {@link Estado#CANCELADA}
     */
    public boolean esActiva() {
        return estado != Estado.CANCELADA;
    }

    /**
     * Baja logica de la solicitud: deja historial y libera el hueco del anti-duplicado.
     */
    public void cancelar() {
        this.estado = Estado.CANCELADA;
    }

    /**
     * Devuelve el identificador tecnico de la solicitud.
     *
     * @return id
     */
    public Long getId() {
        return id;
    }

    /**
     * Devuelve el identificador funcional (UUID) de la solicitud.
     *
     * @return solicitudId
     */
    public String getSolicitudId() {
        return solicitudId;
    }

    /**
     * Devuelve la historia clinica del paciente.
     *
     * @return pacienteId
     */
    public String getPacienteId() {
        return pacienteId;
    }

    /**
     * Devuelve el codigo de prueba del catalogo.
     *
     * @return codigoPrueba
     */
    public String getCodigoPrueba() {
        return codigoPrueba;
    }

    /**
     * Devuelve la indicacion clinica.
     *
     * @return indicacionClinica
     */
    public String getIndicacionClinica() {
        return indicacionClinica;
    }

    /**
     * Devuelve la prioridad clinica.
     *
     * @return prioridad
     */
    public Prioridad getPrioridad() {
        return prioridad;
    }

    /**
     * Devuelve el origen de la peticion.
     *
     * @return origen
     */
    public String getOrigen() {
        return origen;
    }

    /**
     * Devuelve el medico autor (no repudio).
     *
     * @return medicoId
     */
    public String getMedicoId() {
        return medicoId;
    }

    /**
     * Devuelve el instante de creacion usado en la huella.
     *
     * @return creadaEn
     */
    public Instant getCreadaEn() {
        return creadaEn;
    }

    /**
     * Devuelve el estado del ciclo de vida.
     *
     * @return estado
     */
    public Estado getEstado() {
        return estado;
    }

    /**
     * Devuelve la huella SHA-256 guardada.
     *
     * @return huella en hexadecimal
     */
    public String getHuella() {
        return huella;
    }
}
