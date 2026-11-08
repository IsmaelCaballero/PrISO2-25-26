package es.uclm.iso2.agendas.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Registro de auditoria ligera de los cambios de las agendas (CDU13). Cada accion
 * enlaza su SHA-256 con el hash del registro anterior ({@code hashAnterior}), formando
 * una cadena verificable que evidencia que nadie altero el rastro despues de la
 * escritura (mismo patron que la auditoria de catalogo de CFG-01).
 *
 * @since 1.1.0 (CFG-03)
 */
@Entity
@Table(name = "agenda_auditoria")
public class AuditoriaAgendas {

    /** Acciones registrables sobre las agendas. */
    public enum AccionAuditoria {
        /** Alta de la agenda de un profesional. */
        ALTA_AGENDA,
        /** Alta de una franja de disponibilidad. */
        ALTA_FRANJA,
        /** Baja de una franja. */
        BAJA_FRANJA,
        /** Alta de un festivo. */
        ALTA_FESTIVO,
        /** Baja de un festivo. */
        BAJA_FESTIVO
    }

    /** Clave tecnica del registro. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Accion ejecutada. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccionAuditoria accion;

    /** Profesional afectado por la accion. */
    @Column(name = "profesional_id", nullable = false, length = 60)
    private String profesionalId;

    /** Usuario de seguridad que ejecuto la accion ("sistema" si no hay contexto). */
    @Column(nullable = false, length = 100)
    private String usuario;

    /** Momento UTC de la accion. */
    @Column(nullable = false)
    private Instant instante;

    /** Hash SHA-256 (hex) del registro: accion|profesional|usuario|instante|hash anterior. */
    @Column(nullable = false, length = 64)
    private String hash;

    /** Hash del registro anterior (null solo en el primer registro de la cadena). */
    @Column(name = "hash_anterior", length = 64)
    private String hashAnterior;

    /**
     * Constructor JPA sin argumentos (requerido por Hibernate).
     */
    protected AuditoriaAgendas() {
    }

    /**
     * Crea el registro de auditoria ya con su hash calculado por el servicio.
     *
     * @param accion        accion ejecutada
     * @param profesionalId profesional afectado
     * @param usuario       usuario responsable
     * @param instante      momento UTC
     * @param hash          hash del registro
     * @param hashAnterior  hash del registro anterior (null en el primero)
     */
    public AuditoriaAgendas(AccionAuditoria accion, String profesionalId, String usuario,
                            Instant instante, String hash, String hashAnterior) {
        this.accion = accion;
        this.profesionalId = profesionalId;
        this.usuario = usuario;
        this.instante = instante;
        this.hash = hash;
        this.hashAnterior = hashAnterior;
    }

    /**
     * Devuelve el identificador del registro.
     *
     * @return id
     */
    public Long getId() {
        return id;
    }

    /**
     * Devuelve la accion registrada.
     *
     * @return accion
     */
    public AccionAuditoria getAccion() {
        return accion;
    }

    /**
     * Devuelve el profesional afectado.
     *
     * @return profesional
     */
    public String getProfesionalId() {
        return profesionalId;
    }

    /**
     * Devuelve el usuario responsable.
     *
     * @return usuario
     */
    public String getUsuario() {
        return usuario;
    }

    /**
     * Devuelve el momento UTC de la accion.
     *
     * @return instante
     */
    public Instant getInstante() {
        return instante;
    }

    /**
     * Devuelve el hash propio del registro.
     *
     * @return hash hex SHA-256
     */
    public String getHash() {
        return hash;
    }

    /**
     * Devuelve el hash del registro anterior.
     *
     * @return hash anterior o null
     */
    public String getHashAnterior() {
        return hashAnterior;
    }

    /**
     * Sustituye el hash propio de un registro ya escrito. Solo para pruebas: permite
     * comprobar que la cadena verificable se rompe si alguien altera la auditoria
     * directamente en base de datos. No usar desde codigo de produccion.
     *
     * @param hash nuevo hash (simulacion de manipulacion)
     */
    public void setHashParaPruebas(String hash) {
        this.hash = hash;
    }
}
