package es.uclm.iso2.citas.dominio;

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
import java.util.List;
import java.util.Objects;

/**
 * Registro de auditoria ligera de los cambios de estado de las citas (CDU12). Cada
 * accion enlaza su SHA-256 con el hash del registro anterior ({@code hashAnterior}),
 * formando una cadena verificable que evidencia que nadie altero el rastro despues de la
 * escritura (el mismo patron que la auditoria de catalogo de CFG-01 y de agendas de CFG-03).
 *
 * <p>Aqui el canonico incluye la cita y el profesional, no solo el profesional: el
 * rastro que exige UC12.6 responde por <em>que cita</em> se agendo, confirmo, reprogramo,
 * cancelo, realizo o vencio, y quien lo hizo. El material canonico es
 * {@code accion|citaId|profesionalId|usuario|instante|hashAnterior}.</p>
 *
 * @since 1.1.0 (CFG-05, CDU12)
 */
@Entity
@Table(name = "cita_auditoria")
public class AuditoriaCitas {

    /** Acciones registrables sobre las citas. */
    public enum AccionAuditoria {
        /** Se agendo una cita y se bloqueo su hueco. */
        AGENDAMIENTO,
        /** Se confirmo la asistencia de una cita programada. */
        CONFIRMACION,
        /** Se movio una cita a un hueco nuevo. */
        REPROGRAMACION,
        /** Se cancelo una cita con motivo. */
        CANCELACION,
        /** La prueba de la cita se dio por realizada. */
        REALIZACION,
        /** El lote vencio una cita a la que nadie acudio. */
        EXPIRACION
    }

    /**
     * Material del primer registro de la cadena, que no tiene predecesor que enlazar.
     * Cambiar este literal invalida todas las cadenas escritas antes.
     */
    static final String GENESIS = "GENESIS";

    /** Clave tecnica y orden natural de la cadena: las ids crecen con el tiempo. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Accion realizada. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccionAuditoria accion;

    /** Cita sobre la que recae la accion. */
    @Column(name = "cita_id", nullable = false, length = 36)
    private String citaId;

    /** Profesional cuya agenda se vio afectada. */
    @Column(name = "profesional_id", nullable = false, length = 60)
    private String profesionalId;

    /** Usuario del claim del JWT, o {@code sistema} en los lotes de expiracion. */
    @Column(nullable = false, length = 60)
    private String usuario;

    /** Momento de la escritura, con zona horaria. */
    @Column(nullable = false)
    private Instant instante;

    /** Hash del registro anterior; {@code null} en el primero de la cadena. */
    @Column(name = "hash_anterior", length = 64)
    private String hashAnterior;

    /** SHA-256 hex del material canonico de este registro. */
    @Column(nullable = false, length = 64)
    private String hash;

    /** Construye un registro de auditoria sin persistir. */
    protected AuditoriaCitas() {
    }

    /**
     * Crea el registro de auditoria con su eslabon ya calculado.
     *
     * @param accion        accion realizada
     * @param citaId        cita afectada
     * @param profesionalId profesional afectado
     * @param usuario       usuario que realiza la accion o {@code sistema}
     * @param instante      momento de la escritura
     * @param hashAnterior  hash del registro previo, {@code null} en el primero
     * @param hash          SHA-256 del material canonico de este registro
     */
    public AuditoriaCitas(AccionAuditoria accion, String citaId, String profesionalId,
            String usuario, Instant instante, String hashAnterior, String hash) {
        this.accion = accion;
        this.citaId = citaId;
        this.profesionalId = profesionalId;
        this.usuario = usuario;
        this.instante = instante;
        this.hashAnterior = hashAnterior;
        this.hash = hash;
    }

    /**
     * Material canonico del que se calcula el hash de un registro. Solo la cadena de
     * auditoria lo necesita: queda fuera de la API publica del modulo.
     *
     * @param accion        accion realizada
     * @param citaId        cita afectada
     * @param profesionalId profesional afectado
     * @param usuario       usuario que realiza la accion
     * @param instante      momento de la escritura
     * @param hashAnterior  hash del registro previo, o {@code null} en el primero
     * @return el material canonico, tal cual se daria de alta en la cadena
     */
    static String canonico(AccionAuditoria accion, String citaId, String profesionalId,
            String usuario, Instant instante, String hashAnterior) {
        return accion.name() + "|" + citaId + "|" + profesionalId + "|" + usuario + "|"
                + instante.toString() + "|" + (hashAnterior == null ? GENESIS : hashAnterior);
    }

    /**
     * Calcula el SHA-256 hex del material canonico de un registro.
     *
     * @param accion        accion realizada
     * @param citaId        cita afectada
     * @param profesionalId profesional afectado
     * @param usuario       usuario que realiza la accion
     * @param instante      momento de la escritura
     * @param hashAnterior  hash del registro previo, o {@code null} en el primero
     * @return hash SHA-256 en hexadecimal
     */
    public static String hashDe(AccionAuditoria accion, String citaId, String profesionalId,
            String usuario, Instant instante, String hashAnterior) {
        return sha256Hex(canonico(accion, citaId, profesionalId, usuario, instante, hashAnterior));
    }

    /**
     * Verifica la cadena completa: cada registro debe enlazar con el hash recalculado del
     * anterior y contener el suyo propio. Basta alterar una fila a mano para que la
     * cadena deje de verificar, que es lo que quiere evidenciar la auditoria ligera.
     *
     * @param registros la cadena en orden ascendente de id
     * @return true si ningun registro fue alterado tras su escritura
     */
    public static boolean cadenaIntegra(List<AuditoriaCitas> registros) {
        String anterior = null;
        for (AuditoriaCitas registro : registros) {
            String esperado = hashDe(registro.getAccion(), registro.getCitaId(),
                    registro.getProfesionalId(), registro.getUsuario(), registro.getInstante(),
                    anterior);
            if (!esperado.equals(registro.getHash())
                    || !Objects.equals(anterior, registro.getHashAnterior())) {
                return false;
            }
            anterior = registro.getHash();
        }
        return true;
    }

    private static String sha256Hex(String material) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] vector = digest.digest(material.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(vector.length * 2);
            for (byte b : vector) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("el JDK no ofrece SHA-256", e);
        }
    }

    // --- Getters y setters de JPA -----------------------------------------

    /**
     * Devuelve la clave tecnica del registro.
     *
     * @return id tecnico
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
     * Devuelve la cita afectada.
     *
     * @return citaId
     */
    public String getCitaId() {
        return citaId;
    }

    /**
     * Devuelve el profesional afectado.
     *
     * @return profesionalId
     */
    public String getProfesionalId() {
        return profesionalId;
    }

    /**
     * Devuelve el usuario que realizo la accion.
     *
     * @return usuario, o {@code sistema} en los lotes
     */
    public String getUsuario() {
        return usuario;
    }

    /**
     * Devuelve el momento de la escritura.
     *
     * @return instante
     */
    public Instant getInstante() {
        return instante;
    }

    /**
     * Devuelve el hash del registro anterior.
     *
     * @return hash anterior, {@code null} en el primero de la cadena
     */
    public String getHashAnterior() {
        return hashAnterior;
    }

    /**
     * Devuelve el hash SHA-256 del registro.
     *
     * @return hash
     */
    public String getHash() {
        return hash;
    }

    /**
     * Altera el hash de un registro. Solo lo usan los tests que demuestran que la cadena
     * deja de verificar cuando alguien toca el rastro a mano.
     *
     * @param hash nuevo hash
     */
    public void setHashParaPruebas(String hash) {
        this.hash = hash;
    }
}
