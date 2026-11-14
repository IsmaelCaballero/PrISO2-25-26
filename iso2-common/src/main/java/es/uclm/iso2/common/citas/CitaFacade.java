package es.uclm.iso2.common.citas;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Fachada de contratos del subdominio {@code citas}: Agendamiento de citas sobre agendas (CDU12).
 * Solo contrato: la implementacion vive en el modulo iso2-citas.
 *
 * <p>Baseline 1.0.0 (CFG-00): {@link #agendar(CitaNuevaDTO)} y
 * {@link #cancelar(String, String)} y los records {@link CitaNuevaDTO} y
 * {@link CitaDTO}, que siguen congelados e intocados (D22: el contrato baseline no
 * se rompe). La version 1.1.0 (CFG-05, CDU12) añade por compatibilidad descendente
 * (D23, extension MINOR) los metodos de ciclo de vida {@link #confirmar(String)},
 * {@link #realizar(String)} y {@link #reprogramar(String, LocalDateTime)}, la
 * consulta {@link #listar(LocalDateTime, LocalDateTime, String, String)},
 * {@link #detalle(String)} y {@link #huecosLibres(String, LocalDate, LocalDate, Integer)}
 * (el motor de asignacion de huecos) y el mantenimiento {@link #expirarVencidas(LocalDateTime)},
 * ademas de los records nuevos {@link DetalleCitaDTO} y {@link HuecoDTO}.</p>
 *
 * @since 1.0.0 (CFG-00); extendida en 1.1.0 (CFG-05, CDU12)
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

    // ------------------------------------------------------------------
    // Extension MINOR 1.1.0 (CFG-05, CDU12): nada de lo anterior cambia.
    // ------------------------------------------------------------------

    /**
     * Confirma una cita programada (asistencia confirmada del paciente).
     *
     * @param citaId identificador de la cita
     * @return cita con estado confirmada
     * @throws CitaNoEncontradaException  si la cita no existe
     * @throws CitaConflictoException    si la transicion de estado no es valida
     */
    CitaDTO confirmar(String citaId);

    /**
     * Marca una cita como realizada (la prueba se ha hecho en ese hueco).
     *
     * @param citaId identificador de la cita
     * @return cita con estado realizada
     * @throws CitaNoEncontradaException  si la cita no existe
     * @throws CitaConflictoException    si la transicion de estado no es valida
     */
    CitaDTO realizar(String citaId);

    /**
     * Reprograma una cita a un nuevo inicio, validando de nuevo la franja de
     * disponibilidad y el festivo del dia (CDU13) y bloqueando el hueco de destino:
     * si otro usuario lo ha ocupado antes, la operacion se rechaza y la cita
     * conserva su hueco anterior. El hueco antiguo queda libre.
     *
     * @param citaId identificador de la cita
     * @param inicio nuevo inicio de la cita
     * @return cita con la nueva franja horaria
     * @throws CitaNoEncontradaException  si la cita no existe
     * @throws CitaConflictoException    si el hueco esta ocupado, si cae fuera de
     *                                   franja o en festivo, o si la transicion no es valida
     */
    CitaDTO reprogramar(String citaId, LocalDateTime inicio);

    /**
     * Lista las citas del periodo indicando profesional y estado (UC12.8).
     *
     * @param desde         inicio del periodo (opcional)
     * @param hasta         fin del periodo (opcional)
     * @param profesionalId filtro por profesional (opcional)
     * @param estado        filtro por estado en minusculas (opcional)
     * @return citas que cumplen los filtros, ordenadas por inicio
     * @throws IllegalArgumentException si el rango de fechas no es coherente o si el
     *                                  estado pedido no es de la maquina de estados
     */
    List<DetalleCitaDTO> listar(LocalDateTime desde, LocalDateTime hasta,
                                String profesionalId, String estado);

    /**
     * Detalle completo de una cita, con profesional y motivo de cancelacion.
     *
     * @param citaId identificador de la cita
     * @return detalle de la cita
     * @throws CitaNoEncontradaException si la cita no existe
     */
    DetalleCitaDTO detalle(String citaId);

    /**
     * Motor de asignacion de huecos: huecos libres de un profesional en un rango de
     * dias, calculados sobre las franjas de disponibilidad del CDU13 (los festivos
     * mandan sobre las franjas) descontando los huecos ya bloqueados por citas activas.
     *
     * @param profesionalId   profesional cuya agenda se consulta
     * @param desde           primer dia del rango (inclusive)
     * @param hasta           ultimo dia del rango (inclusive)
     * @param duracionMinutos duracion que debe caber en el hueco; si es {@code null}
     *                        se devuelven las franjas libres enteras
     * @return huecos libres, ordenados por inicio
     */
    List<HuecoDTO> huecosLibres(String profesionalId, LocalDate desde, LocalDate hasta,
                                Integer duracionMinutos);

    /**
     * Mantiene la maquina de estados: expira las citas no atendidas cuyo fin ya paso
     * (programadas o confirmadas antes del corte). Cada expiracion libera el hueco y
     * reabre la solicitud asociada.
     *
     * @param corte instante de referencia (inclusive: una cita que termina justo en
     *              el corte no esta vencida)
     * @return citas que han pasado a expirada
     */
    List<CitaDTO> expirarVencidas(LocalDateTime corte);

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

    /**
     * Vista completa de una cita para las pantallas de recepcion (1.1.0).
     *
     * @param citaId            identificador asignado
     * @param solicitudId       solicitud asociada, {@code null} en citas directas
     * @param profesionalId     profesional que atiende
     * @param inicio            fecha y hora de inicio
     * @param fin               fecha y hora de fin
     * @param estado            estado en minusculas
     * @param motivoCancelacion motivo de la cancelacion, {@code null} si no esta cancelada
     * @param version           marca de control de concurrencia optimista del agregado
     */
    record DetalleCitaDTO(String citaId, String solicitudId, String profesionalId, LocalDateTime inicio,
                          LocalDateTime fin, String estado, String motivoCancelacion, long version) {
    }

    /**
     * Hueco libre de una agenda propuesto por el motor de asignacion (1.1.0).
     *
     * @param profesionalId   profesional de la agenda
     * @param inicio          inicio del hueco
     * @param fin             fin del hueco
     * @param duracionMinutos minutos libres del hueco, {@code null} si no se acoto por duracion
     */
    record HuecoDTO(String profesionalId, LocalDateTime inicio, LocalDateTime fin, Integer duracionMinutos) {
    }
}
