package es.uclm.iso2.citas.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.time.LocalDateTime;

/**
 * Hueco que bloquea una cita activa en la agenda de un profesional (CDU12). Es la pieza
 * del motor anti-concurrencia: aqui vive el
 * {@code UNIQUE (profesional_id, inicio)} que es la unica puerta que de verdad bloquea
 * un hueco, y el {@link Version} exigido por la matriz de calidad de CFG-05.
 *
 * <p>Solo existen filas de las citas <em>activas</em>, de modo que cancelar, realizar o
 * expirar una cita borra su fila y libera el hueco. Como la base de datos es la que
 * decide, dos hilos que reservan el mismo inicio reciben un fallo de integridad y la
 * API lo traduce en 409: el hueco no puede quedar doblemente reservado ni con concurrencia
 * real, que es la puerta que exige la matriz de calidad.</p>
 *
 * @since 1.1.0 (CFG-05, CDU12)
 */
@Entity
@Table(name = "cita_hueco",
        uniqueConstraints = @UniqueConstraint(name = "uk_cita_hueco_profesional_inicio",
                columnNames = {"profesional_id", "inicio"}))
public class CitaHueco {

    /** Clave tecnica de la fila del hueco. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Cita que ocupa el hueco; una sola fila por cita. */
    @Column(name = "cita_id", nullable = false, unique = true, length = 36)
    private String citaId;

    /** Profesional cuya agenda se bloquea. */
    @Column(name = "profesional_id", nullable = false, length = 60)
    private String profesionalId;

    /** Inicio del hueco reservado: la columna que forma la restriccion unica. */
    @Column(nullable = false)
    private LocalDateTime inicio;

    /** Fin del hueco reservado. */
    @Column(nullable = false)
    private LocalDateTime fin;

    /** Control optimista: dos reprogramaciones simultaneas no se pisan. */
    @Version
    @Column(nullable = false)
    private int version;

    /** Construye un hueco sin persistir. */
    protected CitaHueco() {
    }

    /**
     * Crea el hueco que bloquea una cita activa.
     *
     * @param citaId        cita que lo ocupa
     * @param profesionalId profesional cuya agenda se bloquea
     * @param inicio        inicio del hueco
     * @param fin           fin del hueco
     */
    public CitaHueco(String citaId, String profesionalId, LocalDateTime inicio, LocalDateTime fin) {
        this.citaId = citaId;
        this.profesionalId = profesionalId;
        this.inicio = inicio;
        this.fin = fin;
    }

    /**
     * Comprueba si este hueco solapa el intervalo pedido. Los extremos que coinciden no
     * solapan: una cita que termina a las 10:00 deja libre un hueco que empieza a las 10:00.
     *
     * @param otroInicio inicio del intervalo a comprobar
     * @param otroFin    fin del intervalo a comprobar
     * @return true si los dos intervalos comparten al menos un minuto
     */
    public boolean solapaCon(LocalDateTime otroInicio, LocalDateTime otroFin) {
        return inicio.isBefore(otroFin) && otroInicio.isBefore(fin);
    }

    // --- Getters y setters de JPA -----------------------------------------

    /**
     * Devuelve la clave tecnica del hueco.
     *
     * @return id tecnico, {@code null} antes de persistir
     */
    public Long getId() {
        return id;
    }

    /**
     * Devuelve la cita que ocupa el hueco.
     *
     * @return citaId
     */
    public String getCitaId() {
        return citaId;
    }

    /**
     * Devuelve el profesional cuya agenda esta bloqueada.
     *
     * @return profesionalId
     */
    public String getProfesionalId() {
        return profesionalId;
    }

    /**
     * Devuelve el inicio del hueco bloqueado.
     *
     * @return inicio
     */
    public LocalDateTime getInicio() {
        return inicio;
    }

    /**
     * Devuelve el fin del hueco bloqueado.
     *
     * @return fin
     */
    public LocalDateTime getFin() {
        return fin;
    }

    /**
     * Devuelve la version del control optimista.
     *
     * @return version gestionada por JPA
     */
    public int getVersion() {
        return version;
    }

    /**
     * Mueve el hueco al nuevo intervalo de una reprogramacion.
     *
     * @param nuevoInicio nuevo inicio
     * @param nuevoFin    nuevo fin
     */
    public void moverA(LocalDateTime nuevoInicio, LocalDateTime nuevoFin) {
        this.inicio = nuevoInicio;
        this.fin = nuevoFin;
    }
}
