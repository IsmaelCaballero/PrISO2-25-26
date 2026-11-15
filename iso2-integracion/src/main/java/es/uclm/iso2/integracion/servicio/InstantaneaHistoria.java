package es.uclm.iso2.integracion.servicio;

import es.uclm.iso2.common.integracion.InteropFacade.HistoriaSincronizadaDTO;
import es.uclm.iso2.common.integracion.InteropFacade.RegistroHistoriaDTO;
import java.time.Instant;
import java.util.List;

/**
 * Instantanea de una historia externa ya sincronizada, guardada en memoria por el
 * servicio para dar idempotencia a {@code sincronizarHistoriaClinica}: si la huella
 * SHA-256 del contenido externo no cambia, se reutiliza esta instantanea y el DTO
 * sale con {@code reutilizado=true}.
 *
 * @param huella         SHA-256 hex de la forma canonica sincronizada
 * @param registros      registros tal y como se entregaron al exterior
 * @param sincronizadaEn instante en que se guardo la instantanea
 * @since 1.1.0 (CFG-06, CDU19)
 */
public record InstantaneaHistoria(String huella, List<RegistroHistoriaDTO> registros,
                                  Instant sincronizadaEn) {

    /**
     * Crea la instantanea normalizando la lista de registros.
     */
    public InstantaneaHistoria {
        registros = registros == null ? List.of() : List.copyOf(registros);
    }

    /**
     * Devuelve el DTO de respuesta tal y como se entrego la ultima vez.
     *
     * @param pacienteExternoId paciente de la consulta
     * @return historia sincronizada reutilizada
     */
    public HistoriaSincronizadaDTO aDTOReutilizada(String pacienteExternoId) {
        return new HistoriaSincronizadaDTO(pacienteExternoId, true, huella, sincronizadaEn, registros);
    }
}
