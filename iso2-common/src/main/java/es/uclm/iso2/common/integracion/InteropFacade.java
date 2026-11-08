package es.uclm.iso2.common.integracion;

import java.time.LocalDate;

/**
 * Fachada de contratos del subdominio {@code integracion}: Importacion de estudios desde HIS/RIS y despacho DICOM (CDU19; exportacion en CDU11/CDU09).
 * Solo contrato: la implementacion vive en el modulo iso2-integracion (baseline 1.0.0, D22).
 *
 * @since 1.0.0 (CFG-00)
 */
public interface InteropFacade {

    /**
     * Importa la cabecera de un estudio desde el sistema de origen.
     *
     * @param accessionNumber numero de acceso del estudio
     * @return cabecera del estudio importada
     */
    EstudioDTO importarEstudio(String accessionNumber);

    /**
     * Cabecera de un estudio importada (sin imagenes).
     *
     * @param accessionNumber numero de acceso
     * @param pacienteId      historia clinica del paciente
     * @param fechaEstudio    fecha del estudio
     * @param numeroSeries    numero de series del estudio
     */
    record EstudioDTO(String accessionNumber, String pacienteId, LocalDate fechaEstudio, int numeroSeries) {
    }
}
