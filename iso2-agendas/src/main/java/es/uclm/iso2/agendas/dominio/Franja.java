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
import java.time.LocalTime;

/**
 * Franja de disponibilidad de una agenda (CDU13): tramo {@code [inicio, fin)} de un dia.
 * La invariante del dominio es {@code inicio < fin}; la regla de no solape se evalua
 * con {@link #solapaCon(LocalDate, LocalTime, LocalTime)} desde el servicio.
 *
 * @since 1.1.0 (CFG-03)
 */
@Entity
@Table(name = "agenda_franja")
public class Franja {

    /** Clave tecnica del registro. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Agenda a la que pertenece la franja. */
    @ManyToOne(optional = false)
    @JoinColumn(name = "agenda_id")
    private Agenda agenda;

    /** Dia de la franja. */
    @Column(nullable = false)
    private LocalDate dia;

    /** Hora de inicio. */
    @Column(nullable = false)
    private LocalTime inicio;

    /** Hora de fin (exclusiva a efectos de solape). */
    @Column(nullable = false)
    private LocalTime fin;

    /**
     * Constructor JPA sin argumentos (requerido por Hibernate).
     */
    protected Franja() {
    }

    /**
     * Crea una franja anexada a una agenda validando el tramo.
     *
     * @param agenda agenda propietaria (no nula)
     * @param dia    dia de la franja (no nulo)
     * @param inicio hora de inicio (no nula)
     * @param fin    hora de fin, posterior al inicio
     * @throws IllegalArgumentException si faltan datos o {@code inicio >= fin}
     */
    public Franja(Agenda agenda, LocalDate dia, LocalTime inicio, LocalTime fin) {
        if (agenda == null) {
            throw new IllegalArgumentException("la franja necesita una agenda");
        }
        if (dia == null || inicio == null || fin == null) {
            throw new IllegalArgumentException("la franja necesita dia, inicio y fin");
        }
        if (!inicio.isBefore(fin)) {
            throw new IllegalArgumentException("la franja exige inicio anterior al fin: "
                    + inicio + " >= " + fin);
        }
        this.agenda = agenda;
        this.dia = dia;
        this.inicio = inicio;
        this.fin = fin;
    }

    /**
     * Devuelve el identificador tecnico de la franja.
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
     * Devuelve el dia de la franja.
     *
     * @return dia
     */
    public LocalDate getDia() {
        return dia;
    }

    /**
     * Devuelve la hora de inicio.
     *
     * @return inicio
     */
    public LocalTime getInicio() {
        return inicio;
    }

    /**
     * Devuelve la hora de fin.
     *
     * @return fin
     */
    public LocalTime getFin() {
        return fin;
    }

    /**
     * Comprueba si esta franja se solapa con el tramo pedido el mismo dia. Los bordes
     * que se tocan (fin == inicio del otro tramo) no cuentan como solape.
     *
     * @param otroDia    dia del tramo a comparar
     * @param otroInicio inicio del tramo a comparar
     * @param otroFin    fin del tramo a comparar
     * @return true si hay solape estricto
     */
    public boolean solapaCon(LocalDate otroDia, LocalTime otroInicio, LocalTime otroFin) {
        return dia.equals(otroDia) && inicio.isBefore(otroFin) && otroInicio.isBefore(fin);
    }
}
