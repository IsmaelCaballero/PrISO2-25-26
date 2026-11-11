package es.uclm.iso2.citas.repositorio;

import es.uclm.iso2.citas.dominio.CitaHueco;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso a datos de los huecos bloqueados. Es la puerta anti-concurrencia: aqui se apoya
 * la comprobacion previa al alta y aqui salta la restriccion unica cuando dos hilos
 * reservan el mismo inicio.
 *
 * @since 1.1.0 (CFG-05, CDU12)
 */
public interface CitaHuecoRepositorio extends JpaRepository<CitaHueco, Long> {

    /**
     * Hueco que bloquea una cita.
     *
     * @param citaId identificador funcional de la cita
     * @return el hueco, si la cita sigue activa
     */
    Optional<CitaHueco> findByCitaId(String citaId);

    /**
     * Hueco que ocupa exactamente un inicio en la agenda de un profesional: la consulta
     * que choca contra {@code UNIQUE (profesional_id, inicio)}.
     *
     * @param profesionalId profesional cuya agenda se comprueba
     * @param inicio        inicio del hueco
     * @return el hueco en ese inicio, si existe
     */
    Optional<CitaHueco> findByProfesionalIdAndInicio(String profesionalId, LocalDateTime inicio);

    /**
     * Huecos del profesional que empiezan dentro del rango dado: candidatos a solapar la
     * cita que se quiere agendar o reprogramar.
     *
     * @param profesionalId profesional cuya agenda se comprueba
     * @param desde         inicio inclusivo del rango de busqueda
     * @param hasta         fin inclusivo del rango de busqueda
     * @return huecos candidatos
     */
    List<CitaHueco> findByProfesionalIdAndInicioBetween(String profesionalId, LocalDateTime desde,
            LocalDateTime hasta);

    /**
     * Libera el hueco de una cita: la borra de esta tabla, que es donde solo viven las
     * citas activas.
     *
     * @param citaId identificador funcional de la cita
     */
    void deleteByCitaId(String citaId);
}
