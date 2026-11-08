package es.uclm.iso2.catalogo.repositorio;

import es.uclm.iso2.catalogo.dominio.AuditoriaCatalogo;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio JPA de la auditoria encadenada del catalogo.
 *
 * @since 1.1.0 (CFG-01)
 */
public interface AuditoriaCatalogoRepositorio extends JpaRepository<AuditoriaCatalogo, Long> {

    /**
     * Devuelve el ultimo registro insertado: su hash es el que debe enlazar el siguiente.
     *
     * @return el ultimo registro de auditoria, si la cadena no esta vacia
     */
    Optional<AuditoriaCatalogo> findTopByOrderByIdDesc();
}
