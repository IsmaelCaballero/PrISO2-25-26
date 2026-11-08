package es.uclm.iso2.common.agendas;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Fachada de contratos del subdominio {@code agendas}: Agendas de medicos radiologos y
 * tecnicos (CDU13). Solo contrato: la implementacion vive en el modulo iso2-agendas
 * (baseline 1.0.0, D22).
 *
 * <p>El contrato baseline de CFG-00 ({@link #franjasLibres}) queda <strong>congelado</strong>.
 * Con CDU13 (Iter3, CFG-03) se amplia MINOR anadiendo la gestion de agendas, franjas y
 * festivos exigida por el CDU: un profesional tiene una agenda con sus franjas
 * disponibles por dia y sus festivos; sobre una agenda no pueden solaparse franjas y
 * los festivos mandan sobre las franjas.</p>
 *
 * @since 1.0.0 (CFG-00); ampliada en 1.1.0 (CDU13, CFG-03)
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
     * Lista todas las agendas dadas de alta con sus franjas y festivos.
     *
     * @return agendas ordenadas por profesional
     * @since 1.1.0 (CDU13)
     */
    List<AgendaDTO> listarAgendas();

    /**
     * Busca la agenda de un profesional.
     *
     * @param profesionalId identificador del profesional
     * @return la agenda si existe; vacio si el profesional no tiene agenda
     * @since 1.1.0 (CDU13)
     */
    Optional<AgendaDTO> buscarPorProfesional(String profesionalId);

    /**
     * Da de alta una agenda vacia para un profesional.
     *
     * @param profesionalId identificador del profesional
     * @return la agenda creada
     * @throws AgendaConflictoException si el profesional ya tenia agenda (tipo AGENDA_DUPLICADA)
     * @since 1.1.0 (CDU13)
     */
    AgendaDTO crearAgenda(String profesionalId);

    /**
     * Anade una franja de disponibilidad a la agenda de un profesional.
     *
     * @param profesionalId identificador del profesional
     * @param franja        dia, hora de inicio y hora de fin (el id se ignora en el alta)
     * @return la franja creada con su id asignado
     * @throws AgendaNoEncontradoException si el profesional no tiene agenda
     * @throws AgendaConflictoException    si la franja solapa con otra (SOLAPE) o cae en
     *                                     un dia festivo (FESTIVO)
     * @throws IllegalArgumentException    si faltan datos o {@code inicio >= fin}
     * @since 1.1.0 (CDU13)
     */
    FranjaGestionDTO anadirFranja(String profesionalId, FranjaGestionDTO franja);

    /**
     * Elimina una franja de la agenda de un profesional.
     *
     * @param profesionalId identificador del profesional
     * @param franjaId      id de la franja a eliminar
     * @throws AgendaNoEncontradoException si no existe la agenda o la franja
     * @since 1.1.0 (CDU13)
     */
    void eliminarFranja(String profesionalId, Long franjaId);

    /**
     * Anade un festivo a la agenda de un profesional. El festivo manda sobre las franjas:
     * el alta se rechaza si ese dia ya tiene franjas asignadas.
     *
     * @param profesionalId identificador del profesional
     * @param festivo       dia y motivo (el id se ignora en el alta)
     * @return el festivo creado con su id asignado
     * @throws AgendaNoEncontradoException si el profesional no tiene agenda
     * @throws AgendaConflictoException    si el dia ya era festivo o tiene franjas (FESTIVO)
     * @throws IllegalArgumentException    si falta el dia
     * @since 1.1.0 (CDU13)
     */
    FestivoDTO anadirFestivo(String profesionalId, FestivoDTO festivo);

    /**
     * Elimina un festivo de la agenda de un profesional.
     *
     * @param profesionalId identificador del profesional
     * @param festivoId     id del festivo a eliminar
     * @throws AgendaNoEncontradoException si no existe la agenda o el festivo
     * @since 1.1.0 (CDU13)
     */
    void eliminarFestivo(String profesionalId, Long festivoId);

    /**
     * Franja de disponibilidad de un profesional.
     *
     * @param profesionalId identificador del profesional
     * @param dia           dia de la franja
     * @param inicio        hora de inicio
     * @param fin           hora de fin
     */
    record FranjaDTO(String profesionalId, LocalDate dia, LocalTime inicio, LocalTime fin) {
    }

    /**
     * Estado completo de la agenda de un profesional (franjas y festivos ordenados).
     *
     * @param profesionalId identificador del profesional
     * @param franjas       franjas de disponibilidad, ordenadas por dia e inicio
     * @param festivos      festivos de la agenda, ordenados por dia
     * @since 1.1.0 (CDU13)
     */
    record AgendaDTO(String profesionalId, List<FranjaGestionDTO> franjas, List<FestivoDTO> festivos) {
    }

    /**
     * Franja en modo gestion: el id es nulo en el alta y viene asignado en la respuesta.
     *
     * @param id     identificador de la franja (nulo en peticiones de alta)
     * @param dia    dia de la franja
     * @param inicio hora de inicio
     * @param fin    hora de fin (debe ser posterior al inicio)
     * @since 1.1.0 (CDU13)
     */
    record FranjaGestionDTO(Long id, LocalDate dia, LocalTime inicio, LocalTime fin) {
    }

    /**
     * Festivo de una agenda: el id es nulo en el alta y viene asignado en la respuesta.
     *
     * @param id     identificador del festivo (nulo en peticiones de alta)
     * @param dia    dia festivo
     * @param motivo motivo del festivo (vacaciones, asunto, dia natural...)
     * @since 1.1.0 (CDU13)
     */
    record FestivoDTO(Long id, LocalDate dia, String motivo) {
    }
}
