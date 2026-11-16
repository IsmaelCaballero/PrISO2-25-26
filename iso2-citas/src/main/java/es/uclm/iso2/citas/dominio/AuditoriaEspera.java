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
 * Registro de auditoria ligera de los cambios en la lista de espera (CDU14, CFG-07).
 * Cada accion enlaza su SHA-256 con el hash del registro anterior ({@code hashAnterior}),
 * formando una cadena verificable que evidencia que nadie altero el rastro despues de
 * la escritura: la misma tecnica de {@link AuditoriaCitas} (CFG-05), ahora con genesis
 * propio en la tabla {@code espera_auditoria}.
 *
 * <p>Aqui el canonico responde por <em>que entrada</em> de la cola se dio de alta,
 * reubico, asigno o retiro, con su motivo: el material canonico es
 * {@code accion|esperaId|antes->despues|usuario|motivo|instante|hashAnterior}. Los
 * campos sin valor (el antes del alta, el motivo de un alta o una asignacion) firman
 * como cadena vacia. La cadena de la cola es independiente de la de
 * {@code cita_auditoria}: CDU18 las unira (TC14-G08).</p>
 *
 * @since 1.2.0 (CFG-07, CDU14)
 */
@Entity
@Table(name = "espera_auditoria")
public class AuditoriaEspera {

    /** Acciones registrables sobre las entradas de la cola (CDU14). */
    public enum Accion {
        /** Se dio de alta una entrada en la cola. */
        ALTA,
        /** Se cambio la prioridad de una entrada con motivo (UC14.3). */
        REUBICACION,
        /** Se asigno una cita desde la cola (UC14.2): enlaza con la cita del CDU12. */
        ASIGNACION,
        /** Se retiro una entrada de la cola con motivo (UC14.4). */
        RETIRADA
    }

    /**
     * Material del primer eslabon de la cadena, que no tiene predecesor que enlazar.
     * Cambiar este literal invalida todas las cadenas escritas antes; es el mismo
     * canon que {@code AuditoriaCitas.GENESIS} (decision cerrada en el diseno, #47).
     */
    static final String GENESIS = "GENESIS";

    /** Clave tecnica y orden natural de la cadena: las ids crecen con el tiempo. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Accion realizada. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Accion accion;

    /** Entrada de la cola sobre la que recae la accion. */
    @Column(name = "espera_id", nullable = false, length = 36)
    private String esperaId;

    /** Prioridad o estado previo; {@code null} en el alta. */
    @Column(length = 20)
    private String antes;

    /** Prioridad o estado nuevo. */
    @Column(nullable = false, length = 20)
    private String despues;

    /** Usuario del claim del JWT, o {@code sistema}. */
    @Column(nullable = false, length = 60)
    private String usuario;

    /** Motivo exigido por el contrato (reubicacion y retiro); vacio en alta y asignacion. */
    @Column(length = 300)
    private String motivo;

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
    protected AuditoriaEspera() {
    }

    /**
     * Crea el registro de auditoria con su eslabon ya calculado.
     *
     * @param accion       accion realizada
     * @param esperaId     entrada de la cola afectada
     * @param antes        prioridad o estado previo, {@code null} en el alta
     * @param despues      prioridad o estado nuevo
     * @param usuario      usuario que realiza la accion o {@code sistema}
     * @param motivo       motivo de la accion, {@code null} en alta y asignacion
     * @param instante     momento de la escritura
     * @param hashAnterior hash del registro previo, {@code null} en el primero
     * @param hash         SHA-256 del material canonico de este registro
     */
    public AuditoriaEspera(Accion accion, String esperaId, String antes, String despues,
            String usuario, String motivo, Instant instante, String hashAnterior, String hash) {
        this.accion = accion;
        this.esperaId = esperaId;
        this.antes = antes;
        this.despues = despues;
        this.usuario = usuario;
        this.motivo = motivo;
        this.instante = instante;
        this.hashAnterior = hashAnterior;
        this.hash = hash;
    }

    /**
     * Material canonico del que se calcula el hash de un registro. Solo la cadena
     * de auditoria lo necesita: queda fuera de la API publica del modulo.
     *
     * @param accion       accion realizada
     * @param esperaId     entrada afectada
     * @param antes        prioridad o estado previo, firma como cadena vacia si es {@code null}
     * @param despues      prioridad o estado nuevo
     * @param usuario      usuario que realiza la accion
     * @param motivo       motivo de la accion, firma como cadena vacia si es {@code null}
     * @param instante     momento de la escritura
     * @param hashAnterior hash del registro previo, o el literal genesis si es {@code null}
     * @return el material canonico, tal cual se dio de alta en la cadena
     */
    static String canonico(Accion accion, String esperaId, String antes, String despues,
            String usuario, String motivo, Instant instante, String hashAnterior) {
        return accion.name() + "|" + esperaId + "|"
                + (antes == null ? "" : antes) + "->" + despues + "|"
                + usuario + "|" + (motivo == null ? "" : motivo) + "|"
                + instante.toString() + "|" + (hashAnterior == null ? GENESIS : hashAnterior);
    }

    /**
     * Calcula el SHA-256 hex del material canonico de un registro.
     *
     * @param accion       accion realizada
     * @param esperaId     entrada afectada
     * @param antes        prioridad o estado previo
     * @param despues      prioridad o estado nuevo
     * @param usuario      usuario que realiza la accion
     * @param motivo       motivo de la accion
     * @param instante     momento de la escritura
     * @param hashAnterior hash del registro previo, o {@code null} en el primero
     * @return hash SHA-256 en hexadecimal
     */
    public static String hashDe(Accion accion, String esperaId, String antes, String despues,
            String usuario, String motivo, Instant instante, String hashAnterior) {
        return sha256Hex(canonico(accion, esperaId, antes, despues, usuario, motivo,
                instante, hashAnterior));
    }

    /**
     * Verifica la cadena completa: cada registro debe enlazar con el hash recalculado
     * del anterior y contener el suyo propio. Basta alterar una fila a mano para que
     * la cadena deje de verificar; un eslabon borrado o reordenado deja el siguiente
     * huerfano y tambien corta la cadena (TC14-G05, TC14-G06).
     *
     * @param registros la cadena en orden ascendente de id
     * @return true si ningun registro fue alterado tras su escritura
     */
    public static boolean cadenaIntegra(List<AuditoriaEspera> registros) {
        String anterior = null;
        for (AuditoriaEspera registro : registros) {
            String esperado = hashDe(registro.getAccion(), registro.getEsperaId(),
                    registro.getAntes(), registro.getDespues(), registro.getUsuario(),
                    registro.getMotivo(), registro.getInstante(), anterior);
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
    public Accion getAccion() {
        return accion;
    }

    /**
     * Devuelve la entrada de la cola afectada.
     *
     * @return esperaId
     */
    public String getEsperaId() {
        return esperaId;
    }

    /**
     * Devuelve la prioridad o estado previo.
     *
     * @return antes, {@code null} en el alta
     */
    public String getAntes() {
        return antes;
    }

    /**
     * Devuelve la prioridad o estado nuevo.
     *
     * @return despues
     */
    public String getDespues() {
        return despues;
    }

    /**
     * Devuelve el usuario que realizo la accion.
     *
     * @return usuario, o {@code sistema}
     */
    public String getUsuario() {
        return usuario;
    }

    /**
     * Devuelve el motivo de la accion.
     *
     * @return motivo, {@code null} en alta y asignacion
     */
    public String getMotivo() {
        return motivo;
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
     * Altera el hash de un registro. Solo lo usan los tests que demuestran que la
     * cadena deja de verificar cuando alguien toca el rastro a mano (TC14-G04).
     *
     * @param hash nuevo hash
     */
    public void setHashParaPruebas(String hash) {
        this.hash = hash;
    }

    /**
     * Altera el motivo de un registro ya guardado. Solo lo usan los tests que
     * demuestran que cambiar el contenido de un eslabon rompe la cadena (TC14-G04).
     *
     * @param motivo nuevo motivo
     */
    public void setMotivoParaPruebas(String motivo) {
        this.motivo = motivo;
    }
}
