package es.uclm.iso2.catalogo.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Parametros administrativos de una prueba (CDU16: "politicas"): reglas de cancelacion
 * que aplican a las citas de esa prueba. Valor embebido en {@link TipoPrueba}; su homologo
 * de contrato es {@code PruebaCatalogoFacade.PoliticaDTO}.
 *
 * @since 1.1.0 (CFG-01)
 */
@Embeddable
public class Politica {

    /** Horas minimas de antelacion para cancelar una cita de esta prueba. */
    @Column(name = "politica_plazo_cancelacion_horas", nullable = false)
    private int plazoCancelacionHoras;

    /** Cancelaciones permitidas por paciente y prueba antes de bloqueo administrativo. */
    @Column(name = "politica_max_cancelaciones", nullable = false)
    private int maxCancelaciones;

    /**
     * Constructor JPA sin argumentos (requerido por Hibernate).
     */
    protected Politica() {
    }

    /**
     * Crea la politica con sus dos parametros.
     *
     * @param plazoCancelacionHoras horas minimas de antelacion para cancelar (&gt; 0)
     * @param maxCancelaciones      cancelaciones permitidas (&ge; 0)
     */
    public Politica(int plazoCancelacionHoras, int maxCancelaciones) {
        this.plazoCancelacionHoras = plazoCancelacionHoras;
        this.maxCancelaciones = maxCancelaciones;
    }

    /**
     * Devuelve el plazo minimo de cancelacion en horas.
     *
     * @return plazo en horas
     */
    public int getPlazoCancelacionHoras() {
        return plazoCancelacionHoras;
    }

    /**
     * Devuelve el maximo de cancelaciones permitidas.
     *
     * @return maximo de cancelaciones
     */
    public int getMaxCancelaciones() {
        return maxCancelaciones;
    }
}
