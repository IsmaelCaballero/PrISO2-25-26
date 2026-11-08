package es.uclm.iso2.agendas.repositorio;

import es.uclm.iso2.agendas.dominio.Festivo;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso a datos de los festivos (CDU13). Se persisten por su propio repositorio
 * por el mismo motivo que {@link FranjaRepositorio}: el id generado tiene que
 * viajar de vuelta en el DTO de alta.
 *
 * @since 1.1.0 (CFG-03)
 */
public interface FestivoRepositorio extends JpaRepository<Festivo, Long> {
}
