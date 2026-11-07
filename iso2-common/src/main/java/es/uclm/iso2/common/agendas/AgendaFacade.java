package es.uclm.iso2.common.agendas;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Fachada de contratos del subdominio {@code agendas}: Agendas de medicos radiologos y tecnicos (CDU13).
 * Solo contrato: la implementacion vive en el modulo iso2-agendas (baseline 1.0.0, D22).
 *
 * @since 1.0.0 (CFG-00)
 */
public interface AgendaFacade {

    /**
     * Devuelve las franjas libres de un profesional en un rango de fechas.
     *
     * @param profesionalId identificador del profesional
     * @param desde         fecha inicial del rango (incluida)
     * @param hasta         fecha final del rango (incluida)
     * @return lista de franjas disponibles ordenadas por fecha
     */
    List<FranjaDTO> franjasLibres(String profesionalId, LocalDate desde, LocalDate hasta);

    /**
     * Franja de disponibilidad de un profesional.
     *
     * @param profesionalId identificador del profesional
     * @param dia           dia de la franja
     * @param inicio       hora de inicio
     * @param fin          hora de fin
     */
    record FranjaDTO(String profesionalId, LocalDate dia, LocalTime inicio, LocalTime fin) {
    }
}
