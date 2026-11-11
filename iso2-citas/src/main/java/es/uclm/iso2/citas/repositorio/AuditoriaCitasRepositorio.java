package es.uclm.iso2.citas.repositorio;

import es.uclm.iso2.citas.dominio.AuditoriaCitas;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso a datos del rastro de auditoria de las citas. La cadena se recorre de atras hacia
 * adelante para escribir (hace falta el ultimo hash) y de adelante hacia atras para
 * verificarla.
 *
 * @since 1.1.0 (CFG-05, CDU12)
 */
public interface AuditoriaCitasRepositorio extends JpaRepository<AuditoriaCitas, Long> {

    /**
     * Ultimo registro escrito: el que aporta el {@code hash_anterior} del siguiente.
     *
     * @return el ultimo eslabon de la cadena, si la cadena no esta vacia
     */
    Optional<AuditoriaCitas> findTopByOrderByIdDesc();

    /**
     * Cadena completa en el orden en que se escribio.
     *
     * @return registros por id ascendente
     */
    List<AuditoriaCitas> findAllByOrderByIdAsc();
}
