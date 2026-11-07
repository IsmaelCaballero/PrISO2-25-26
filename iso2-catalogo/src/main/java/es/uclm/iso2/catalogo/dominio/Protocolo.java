package es.uclm.iso2.catalogo.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Parametros de preparacion del paciente para una prueba de imagen (CDU16: "protocolos").
 * Valor embebido en {@link TipoPrueba}; su homologo de contrato es
 * {@code PruebaCatalogoFacade.ProtocoloDTO}.
 *
 * @since 1.1.0 (CFG-01)
 */
@Embeddable
public class Protocolo {

    /** Instrucciones de preparacion (texto libre). */
    @Column(name = "protocolo_preparacion", nullable = false, length = 1000)
    private String preparacion;

    /** Horas de ayuno previo exigidas (0 = sin ayuno). */
    @Column(name = "protocolo_ayuno_horas", nullable = false)
    private int ayunoHoras;

    /** Contraindicaciones conocidas ("ninguna" si no hay). */
    @Column(name = "protocolo_contraindicaciones", nullable = false, length = 1000)
    private String contraindicaciones;

    /**
     * Constructor JPA sin argumentos (requerido por Hibernate).
     */
    protected Protocolo() {
    }

    /**
     * Crea el protocolo con sus tres parametros.
     *
     * @param preparacion        instrucciones de preparacion
     * @param ayunoHoras         horas de ayuno previo (&ge; 0)
     * @param contraindicaciones contraindicaciones conocidas
     */
    public Protocolo(String preparacion, int ayunoHoras, String contraindicaciones) {
        this.preparacion = preparacion;
        this.ayunoHoras = ayunoHoras;
        this.contraindicaciones = contraindicaciones;
    }

    /**
     * Devuelve las instrucciones de preparacion.
     *
     * @return preparacion
     */
    public String getPreparacion() {
        return preparacion;
    }

    /**
     * Devuelve las horas de ayuno previo.
     *
     * @return ayuno en horas
     */
    public int getAyunoHoras() {
        return ayunoHoras;
    }

    /**
     * Devuelve las contraindicaciones conocidas.
     *
     * @return contraindicaciones
     */
    public String getContraindicaciones() {
        return contraindicaciones;
    }
}
