package es.uclm.iso2.agendas.repositorio;

import es.uclm.iso2.agendas.dominio.Franja;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso a datos de las franjas de disponibilidad (CDU13). Se persisten por su
 * propio repositorio porque el insert en cascada desde la coleccion de la agenda
 * no reasigna el id @IDENTITY en la instancia en memoria.
 *
 * @since 1.1.0 (CFG-03)
 */
public interface FranjaRepositorio extends JpaRepository<Franja, Long> {
}
