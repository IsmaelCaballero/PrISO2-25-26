package es.uclm.iso2.citas.repositorio;

import es.uclm.iso2.citas.dominio.AuditoriaEspera;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso a datos del rastro encadenado de la cola (CDU14). El ultimo eslabon es el
 * que enlaza el siguiente asiento ({@code null} en la cadena vacia: el primero firma
 * con el genesis, igual que en CDU12), y la lectura completa alimenta
 * {@link AuditoriaEspera#cadenaIntegra(List)}.
 *
 * @since 1.2.0 (CFG-07, CDU14)
 */
public interface AuditoriaEsperaRepositorio extends JpaRepository<AuditoriaEspera, Long> {

    /**
     * Ultimo eslabon escrito: el hash del eslabon que toca enlazar.
     *
     * @return el eslabon mas reciente, si la cadena no esta vacia
     */
    Optional<AuditoriaEspera> findTopByOrderByIdDesc();

    /**
     * Cadena completa en orden de escritura, la entrada de la verificacion global.
     *
     * @return todos los eslabones, del genesis al ultimo
     */
    List<AuditoriaEspera> findAllByOrderByIdAsc();

    /**
     * Eslabones de una entrada concreta, en orden de escritura: la auditoria de la
     * pantalla de detalle y el esperadoEslabones de la tabla TDOP.
     *
     * @param esperaId entrada cuyos asientos se piden
     * @return asientos de esa entrada en orden
     */
    List<AuditoriaEspera> findByEsperaIdOrderByIdAsc(String esperaId);
}
