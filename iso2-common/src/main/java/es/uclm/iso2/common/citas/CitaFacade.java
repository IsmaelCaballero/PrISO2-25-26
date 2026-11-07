package es.uclm.iso2.common.citas;

import java.time.LocalDateTime;

/**
 * Fachada de contratos del subdominio {@code citas}: Agendamiento de citas sobre agendas (CDU12).
 * Solo contrato: la implementacion vive en el modulo iso2-citas (baseline 1.0.0, D22).
 *
 * @since 1.0.0 (CFG-00)
 */
public interface CitaFacade {

    /**
     * Agenda una cita para una solicitud sobre una franja libre.
     *
     * @param nueva datos de la cita a agendar
     * @return cita creada con identificador y estado
     */
    CitaDTO agendar(CitaNuevaDTO nueva);

    /**
     * Cancela una cita existente.
     *
     * @param citaId      identificador de la cita
     * @param motivo      motivo de la cancelacion
     * @return cita actualizada con estado cancelada
     */
    CitaDTO cancelar(String citaId, String motivo);

    /**
     * Datos para agendar una cita.
     *
     * @param solicitudId    solicitud que origina la cita
     * @param profesionalId profesional asignada
     * @param inicio        fecha y hora de inicio
     * @param duracionMinutos duracion estimada en minutos
     */
    record CitaNuevaDTO(String solicitudId, String profesionalId, LocalDateTime inicio, int duracionMinutos) {
    }

    /**
     * Cita ya persistida.
     *
     * @param citaId      identificador asignado
     * @param solicitudId solicitud asociada
     * @param inicio      fecha y hora de inicio
     * @param fin         fecha y hora de fin calculadas
     * @param estado      estado (programada, confirmada, realizada, cancelada)
     */
    record CitaDTO(String citaId, String solicitudId, LocalDateTime inicio, LocalDateTime fin, String estado) {
    }
}
