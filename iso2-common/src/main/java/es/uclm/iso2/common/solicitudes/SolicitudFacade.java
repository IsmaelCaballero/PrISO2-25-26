package es.uclm.iso2.common.solicitudes;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Fachada de contratos del subdominio {@code solicitudes}: Solicitudes de prueba desde Atencion Primaria (CDU06).
 * Solo contrato: la implementacion vive en el modulo iso2-solicitudes (baseline 1.0.0, D22).
 *
 * @since 1.0.0 (CFG-00)
 */
public interface SolicitudFacade {

    /**
     * Crea una solicitud de prueba de imagen.
     *
     * @param nueva datos de la solicitud a crear
     * @return solicitud creada con identificador y estado inicial
     */
    SolicitudDTO crear(SolicitudNuevaDTO nueva);

    /**
     * Busca una solicitud por identificador.
     *
     * @param solicitudId identificador de la solicitud
     * @return la solicitud si existe, vacio en caso contrario
     */
    Optional<SolicitudDTO> buscar(String solicitudId);

    /**
     * Datos para crear una solicitud.
     *
     * @param pacienteId   historia clinica del paciente
     * @param codigoPrueba codigo de prueba del catalogo
     * @param origen       origen de la solicitud (AP, hospital, urgencias)
     */
    record SolicitudNuevaDTO(String pacienteId, String codigoPrueba, String origen) {
    }

    /**
     * Solicitud ya persistida.
     *
     * @param solicitudId     identificador asignado
     * @param pacienteId      historia clinica del paciente
     * @param codigoPrueba    codigo de prueba del catalogo
     * @param fechaSolicitada fecha de emision
     * @param estado          estado del ciclo de vida (creada, citada, realizada, informada, cancelada)
     */
    record SolicitudDTO(String solicitudId, String pacienteId, String codigoPrueba, LocalDate fechaSolicitada, String estado) {
    }
}
