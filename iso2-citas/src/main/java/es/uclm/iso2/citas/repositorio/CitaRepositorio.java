package es.uclm.iso2.citas.repositorio;

import es.uclm.iso2.citas.dominio.Cita;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Acceso a datos de las citas. Las consultas derivadas del nombre bastan para el alta,
 * el detalle y el lote de expiracion; la busqueda con filtros opcionales de la pantalla
 * de recepcion necesita una consulta propia.
 *
 * @since 1.1.0 (CFG-05, CDU12)
 */
public interface CitaRepositorio extends JpaRepository<Cita, Long> {

    /**
     * Busca una cita por su identificador funcional.
     *
     * @param citaId identificador funcional
     * @return la cita, si existe
     */
    Optional<Cita> findByCitaId(String citaId);

    /**
     * Busca citas con filtros opcionales para la agenda del dia de recepcion. Un filtro
     * {@code null} no estrecha el resultado, de modo que la pantalla puede preguntar solo
     * por rango de fechas, solo por profesional o solo por estado.
     *
     * @param desde         inicio inclusivo del rango, {@code null} si no se filtra
     * @param hasta         fin inclusivo del rango, {@code null} si no se filtra
     * @param profesionalId profesional a filtrar, {@code null} si no se filtra
     * @param estado        estado a filtrar, {@code null} si no se filtra
     * @return citas que cumplen los filtros, ordenadas por profesional e inicio
     */
    @Query("""
            select c from Cita c
             where (:desde is null or c.inicio >= :desde)
               and (:hasta is null or c.fin <= :hasta)
               and (:profesionalId is null or c.profesionalId = :profesionalId)
               and (:estado is null or c.estado = :estado)
             order by c.profesionalId asc, c.inicio asc
            """)
    List<Cita> busca(LocalDateTime desde, LocalDateTime hasta, String profesionalId, Cita.Estado estado);

    /**
     * Citas en algunos de los estados dados cuyo fin ya quedo atras del corte: las
     * candidatas a vencer por el lote de UC12.7.
     *
     * @param estados estados no terminales a considerar
     * @param corte   momento a partir del cual la cita ya vencio
     * @return citas vencibles
     */
    List<Cita> findByEstadoInAndFinBefore(Collection<Cita.Estado> estados, LocalDateTime corte);
}
