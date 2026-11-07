package es.uclm.iso2.catalogo.dominio;

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
 * Registro de auditoria ligera de los cambios del catalogo (CDU16). Cada accion enlaza su
 * SHA-256 con el hash del registro anterior ({@code hashAnterior}), formando una cadena
 * verificable que evidencia que nadie altero el rastro despues de la escritura.
 *
 * @since 1.1.0 (CFG-01)
 */
@Entity
@Table(name = "catalogo_auditoria")
public class AuditoriaCatalogo {

    /** Acciones registrables sobre el catalogo. */
    public enum AccionAuditoria {
        /** Alta de una prueba nueva. */
        ALTA,
        /** Modificacion de parametros de una prueba existente. */
        MODIFICACION,
        /** Baja logica (desactivacion). */
        BAJA,
        /** Reactivacion de una prueba desactivada. */
        REACTIVACION
    }

    /** Clave tecnica del registro. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Accion ejecutada. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccionAuditoria accion;

    /** Codigo de la prueba afectada. */
    @Column(name = "codigo_prueba", nullable = false, length = 40)
    private String codigoPrueba;

    /** Usuario de seguridad que ejecuto la accion ("sistema" si no hay contexto). */
    @Column(nullable = false, length = 100)
    private String usuario;

    /** Momento UTC de la accion. */
    @Column(nullable = false)
    private Instant instante;

    /** Hash SHA-256 (hex) del registro: accion|codigo|usuario|instante|hash anterior. */
    @Column(nullable = false, length = 64)
    private String hash;

    /** Hash del registro anterior (null solo en el primer registro de la cadena). */
    @Column(name = "hash_anterior", length = 64)
    private String hashAnterior;

    /**
     * Constructor JPA sin argumentos (requerido por Hibernate).
     */
    protected AuditoriaCatalogo() {
    }

    /**
     * Crea el registro de auditoria ya con su hash calculado por el servicio.
     *
     * @param accion       accion ejecutada
     * @param codigoPrueba codigo de la prueba afectada
     * @param usuario      usuario responsable
     * @param instante     momento UTC
     * @param hash         hash del registro
     * @param hashAnterior hash del registro anterior (null en el primero)
     */
    public AuditoriaCatalogo(AccionAuditoria accion, String codigoPrueba, String usuario,
                             Instant instante, String hash, String hashAnterior) {
        this.accion = accion;
        this.codigoPrueba = codigoPrueba;
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
     * Devuelve el codigo de prueba afectado.
     *
     * @return codigo de prueba
     */
    public String getCodigoPrueba() {
        return codigoPrueba;
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
}
