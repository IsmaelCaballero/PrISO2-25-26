package es.uclm.iso2.common.reporting;

import java.time.LocalDate;
import java.util.List;

/**
 * Fachada de contratos del subdominio {@code reporting}: Cuadros de mando, KPI de lista de espera y trazas (CDU10).
 * Solo contrato: la implementacion vive en el modulo iso2-reporting (baseline 1.0.0, D22).
 *
 * @since 1.0.0 (CFG-00)
 */
public interface CuadroMandoFacade {

    /**
     * Calcula los indicadores del cuadro de mando a una fecha de corte.
     *
     * @param fechaCorte fecha hasta la que se calculan
     * @return lista de indicadores
     */
    List<IndicadorDTO> indicadores(LocalDate fechaCorte);

    /**
     * Indicador del cuadro de mando.
     *
     * @param clave      identificador del indicador (ej. lista_espera_tac)
     * @param valor      valor calculado en formato textual estable
     * @param fechaCorte fecha de calculo
     */
    record IndicadorDTO(String clave, String valor, LocalDate fechaCorte) {
    }
}
