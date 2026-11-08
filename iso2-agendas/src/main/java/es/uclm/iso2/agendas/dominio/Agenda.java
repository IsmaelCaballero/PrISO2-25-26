package es.uclm.iso2.agendas.dominio;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Agenda de un profesional (medico radiologo o tecnico): contenedor de sus franjas
 * de disponibilidad y de sus festivos (CDU13). Un profesional tiene una unica agenda;
 * las reglas del CDU viven aqui: dos franjas no pueden solaparse el mismo dia y un
 * festivo manda sobre las franjas de ese dia.
 *
 * @since 1.1.0 (CFG-03)
 */
@Entity
@Table(name = "agenda_profesional")
public class Agenda {

    /** Clave tecnica del registro. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Identificador funcional del profesional (su usuario en v1). Unico. */
    @Column(name = "profesional_id", nullable = false, length = 60, unique = true)
    private String profesionalId;

    /** Franjas de disponibilidad de la agenda. */
    @OneToMany(mappedBy = "agenda", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Franja> franjas = new ArrayList<>();

    /** Festivos de la agenda. */
    @OneToMany(mappedBy = "agenda", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Festivo> festivos = new ArrayList<>();

    /**
     * Constructor JPA sin argumentos (requerido por Hibernate).
     */
    protected Agenda() {
    }

    /**
     * Crea una agenda vacia para un profesional.
     *
     * @param profesionalId identificador del profesional (no vacio)
     * @throws IllegalArgumentException si el identificador es nulo o esta en blanco
     */
    public Agenda(String profesionalId) {
        if (profesionalId == null || profesionalId.isBlank()) {
            throw new IllegalArgumentException("la agenda necesita un profesional");
        }
        this.profesionalId = profesionalId.trim();
    }

    /**
     * Devuelve el identificador tecnico de la agenda.
     *
     * @return id
     */
    public Long getId() {
        return id;
    }

    /**
     * Devuelve el identificador funcional del profesional.
     *
     * @return profesional
     */
    public String getProfesionalId() {
        return profesionalId;
    }

    /**
     * Franjas de la agenda (vista viva; no copiar para mutar fuera de los metodos).
     *
     * @return lista de franjas
     */
    public List<Franja> getFranjas() {
        return franjas;
    }

    /**
     * Festivos de la agenda (vista viva; no copiar para mutar fuera de los metodos).
     *
     * @return lista de festivos
     */
    public List<Festivo> getFestivos() {
        return festivos;
    }

    /**
     * Anade una franja validando la invariante del CDU: {@code inicio < fin}.
     * Las reglas de solape y festivo se comprueban antes en el servicio, que dispone
     * del codigo de conflicto para HTTP 409.
     *
     * @param dia    dia de la franja
     * @param inicio hora de inicio
     * @param fin    hora de fin
     * @return la franja creada y anexada a la agenda
     * @throws IllegalArgumentException si faltan datos o el tramo es invalido
     */
    public Franja anadirFranja(LocalDate dia, LocalTime inicio, LocalTime fin) {
        Franja franja = new Franja(this, dia, inicio, fin);
        franjas.add(franja);
        return franja;
    }

    /**
     * Anade un festivo a la agenda.
     *
     * @param dia    dia festivo
     * @param motivo motivo del festivo (admite null con motivo vacio)
     * @return el festivo creado y anexada a la agenda
     * @throws IllegalArgumentException si falta el dia
     */
    public Festivo anadirFestivo(LocalDate dia, String motivo) {
        Festivo festivo = new Festivo(this, dia, motivo);
        festivos.add(festivo);
        return festivo;
    }

    /**
     * Busca una franja de esta agenda por su id.
     *
     * @param franjaId id de la franja
     * @return la franja si existe; vacio en caso contrario
     */
    public Optional<Franja> buscarFranja(Long franjaId) {
        return franjas.stream().filter(f -> f.getId().equals(franjaId)).findFirst();
    }

    /**
     * Busca un festivo de esta agenda por su id.
     *
     * @param festivoId id del festivo
     * @return el festivo si existe; vacio en caso contrario
     */
    public Optional<Festivo> buscarFestivo(Long festivoId) {
        return festivos.stream().filter(f -> f.getId().equals(festivoId)).findFirst();
    }

    /**
     * Indica si un dia esta declarado festivo en esta agenda.
     *
     * @param dia dia a comprobar
     * @return true si existe un festivo ese dia
     */
    public boolean esFestivo(LocalDate dia) {
        return festivos.stream().anyMatch(f -> f.getDia().equals(dia));
    }

    /**
     * Indica si el trampo pedido solapa con alguna franja ya asignada.
     *
     * @param dia    dia del trampo
     * @param inicio hora de inicio
     * @param fin    hora de fin
     * @return true si alguna franja existente se solapa
     */
    public boolean haySolape(LocalDate dia, LocalTime inicio, LocalTime fin) {
        return franjas.stream().anyMatch(f -> f.solapaCon(dia, inicio, fin));
    }

    /**
     * Indica si el dia tiene alguna franja asignada (lo que impide declarar festivo).
     *
     * @param dia dia a comprobar
     * @return true si hay franjas ese dia
     */
    public boolean hayFranjaElDia(LocalDate dia) {
        return franjas.stream().anyMatch(f -> f.getDia().equals(dia));
    }
}
