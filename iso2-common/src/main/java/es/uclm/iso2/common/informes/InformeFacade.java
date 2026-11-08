package es.uclm.iso2.common.informes;

import java.time.Instant;
import java.util.Optional;

/**
 * Fachada de contratos del subdominio {@code informes}: Informes radiologicos en tres fases: borrador, revision y firma (CDU09).
 * Solo contrato: la implementacion vive en el modulo iso2-informes (baseline 1.0.0, D22).
 *
 * @since 1.0.0 (CFG-00)
 */
public interface InformeFacade {

    /**
     * Busca un informe por identificador.
     *
     * @param informeId identificador del informe
     * @return el informe si existe, vacio en caso contrario
     */
    Optional<InformeDTO> buscar(String informeId);

    /**
     * Firma un informe ya revisado (cambio de estado con auditoria).
     *
     * @param informeId  identificador del informe
     * @param radiologoId radiologo que firma
     * @return informe firmado con marca de tiempo
     */
    InformeDTO firmar(String informeId, String radiologoId);

    /**
     * Informe radiologico.
     *
     * @param informeId       identificador asignado
     * @param estudioAccession estudio al que pertenece
     * @param radiologoId     radiologo autor o firmante
     * @param estado          estado (borrador, en_revision, firmado)
     * @param firmadoEn       momento de la firma (nulo si aun no firmado)
     */
    record InformeDTO(String informeId, String estudioAccession, String radiologoId, String estado, Instant firmadoEn) {
    }
}
