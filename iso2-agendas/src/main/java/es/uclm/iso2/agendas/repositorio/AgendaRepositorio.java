package es.uclm.iso2.agendas.repositorio;

import es.uclm.iso2.agendas.dominio.Agenda;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Acceso a datos de las agendas de profesional (CDU13).
 *
 * @since 1.1.0 (CFG-03)
 */
public interface AgendaRepositorio extends JpaRepository<Agenda, Long> {

    /**
     * Busca la agenda de un profesional por su identificador funcional.
     *
     * @param profesionalId identificador del profesional
     * @return la agenda si existe
     */
    Optional<Agenda> findByProfesionalId(String profesionalId);

    /**
     * Indica si un profesional ya tiene agenda dada de alta.
     *
     * @param profesionalId identificador del profesional
     * @return true si existe
     */
    boolean existsByProfesionalId(String profesionalId);
}
