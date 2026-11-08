package es.uclm.iso2.agendas.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;

/**
 * Festivo de una agenda (CDU13): dia en el que el profesional no esta disponible.
 * La regla autoritativa del CDU es que el festivo manda sobre las franjas: el servicio
 * rechaza franjas en dias festivos y rechaza declarar festivo un dia con franjas.
 *
 * @since 1.1.0 (CFG-03)
 */
@Entity
@Table(name = "agenda_festivo")
public class Festivo {

    /** Clave tecnica del registro. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Agenda a la que pertenece el festivo. */
    @ManyToOne(optional = false)
    @JoinColumn(name = "agenda_id")
    private Agenda agenda;

    /** Dia festivo. */
    @Column(nullable = false)
    private LocalDate dia;

    /** Motivo del festivo (vacaciones, asunto propio, dia natural...). */
    @Column(nullable = false, length = 200)
    private String motivo;

    /**
     * Constructor JPA sin argumentos (requerido por Hibernate).
     */
    protected Festivo() {
    }

    /**
     * Crea un festivo anexado a una agenda.
     *
     * @param agenda agenda propietaria (no nula)
     * @param dia    dia festivo (no nulo)
     * @param motivo motivo legible (null se convierte en cadena vacia)
     * @throws IllegalArgumentException si faltan datos esenciales
     */
    public Festivo(Agenda agenda, LocalDate dia, String motivo) {
        if (agenda == null) {
            throw new IllegalArgumentException("el festivo necesita una agenda");
        }
        if (dia == null) {
            throw new IllegalArgumentException("el festivo necesita un dia");
        }
        this.agenda = agenda;
        this.dia = dia;
        this.motivo = motivo == null ? "" : motivo.trim();
    }

    /**
     * Devuelve el identificador tecnico del festivo.
     *
     * @return id
     */
    public Long getId() {
        return id;
    }

    /**
     * Devuelve la agenda propietaria.
     *
     * @return agenda
     */
    public Agenda getAgenda() {
        return agenda;
    }

    /**
     * Devuelve el dia festivo.
     *
     * @return dia
     */
    public LocalDate getDia() {
        return dia;
    }

    /**
     * Devuelve el motivo del festivo.
     *
     * @return motivo (posiblemente vacio)
     */
    public String getMotivo() {
        return motivo;
    }
}
