package es.uclm.iso2.agendas.repositorio;

import es.uclm.iso2.agendas.dominio.AuditoriaAgendas;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Acceso a datos de la auditoria encadenada de agendas (CDU13).
 *
 * @since 1.1.0 (CFG-03)
 */
public interface AuditoriaAgendasRepositorio extends JpaRepository<AuditoriaAgendas, Long> {

    /**
     * Ultimo registro de la cadena: del que nace el hash anterior del siguiente.
     *
     * @return el registro mas reciente si la cadena no esta vacia
     */
    Optional<AuditoriaAgendas> findTopByOrderByIdDesc();
}
