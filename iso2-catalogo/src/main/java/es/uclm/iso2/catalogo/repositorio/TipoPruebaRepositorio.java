package es.uclm.iso2.catalogo.repositorio;

import es.uclm.iso2.catalogo.dominio.TipoPrueba;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio JPA del catalogo de pruebas. La unicidad de {@code codigo} la defiende la
 * base de datos; {@link #existsByCodigo(String)} permite dar un error de negocio limpio
 * antes de provocar la violacion de restriccion.
 *
 * @since 1.1.0 (CFG-01)
 */
public interface TipoPruebaRepositorio extends JpaRepository<TipoPrueba, Long> {

    /**
     * Busca una prueba por su codigo unico (incluye desactivadas).
     *
     * @param codigo codigo de la prueba
     * @return la prueba si existe
     */
    Optional<TipoPrueba> findByCodigo(String codigo);

    /**
     * Comprueba si un codigo esta ocupado (activa o desactivada: los codigos no se reutilizan).
     *
     * @param codigo codigo a comprobar
     * @return true si ya existe
     */
    boolean existsByCodigo(String codigo);
}
