package es.uclm.iso2.integracion.dominio;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Estudio de imagen que devuelve el RIS en el spike. Es la contraparte interna del
 * {@code EstudioDTO} congelado del baseline 1.0.0 de {@code InteropFacade}.
 *
 * @param accessionNumber identificador del estudio en el RIS
 * @param pacienteId      paciente al que pertenece
 * @param fechaEstudio    fecha en la que se realizo
 * @param numeroSeries    numero de series disponibles en el PACS
 * @since 1.1.0 (CFG-06, CDU19)
 */
public record EstudioExterno(String accessionNumber, String pacienteId, LocalDate fechaEstudio,
                             int numeroSeries) {

    /**
     * Crea el estudio comprobando el identificador y la fecha.
     */
    public EstudioExterno {
        if (accessionNumber == null || accessionNumber.isBlank()) {
            throw new IllegalArgumentException("el accession number es obligatorio");
        }
        Objects.requireNonNull(fechaEstudio, "la fecha del estudio es obligatoria");
    }
}
