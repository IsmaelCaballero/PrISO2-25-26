package es.uclm.iso2.solicitudes.repositorio;

import es.uclm.iso2.solicitudes.dominio.Solicitud;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso JPA a las solicitudes de prueba (CDU06). Las consultas derivadas del nombre
 * cubren las tres necesidades del CDU: detalle por id funcional, historial del medico
 * y comprobacion de duplicados sobre estados activos (RF06.3).
 *
 * @since 1.1.0 (CFG-04)
 */
public interface SolicitudRepositorio extends JpaRepository<Solicitud, Long> {

    /**
     * Busca una solicitud por su identificador funcional (UUID).
     *
     * @param solicitudId id funcional
     * @return la solicitud si existe
     */
    Optional<Solicitud> findBySolicitudId(String solicitudId);

    /**
     * Solicitudes de un medico, de la mas reciente a la mas antigua.
     *
     * @param medicoId autor medico
     * @return lista ordenada por {@code creadaEn} descendente
     */
    List<Solicitud> findByMedicoIdOrderByCreadaEnDesc(String medicoId);

    /**
     * ¿Existe ya una solicitud del mismo medico, paciente y prueba en alguno de los
     * estados activos? Es la comprobacion de duplicados de la RF06.3.
     *
     * @param medicoId     autor medico
     * @param pacienteId   historia clinica
     * @param codigoPrueba codigo de la prueba
     * @param estados      estados que cuentan como activos
     * @return true si habria duplicado
     */
    boolean existsByMedicoIdAndPacienteIdAndCodigoPruebaAndEstadoIn(
            String medicoId, String pacienteId, String codigoPrueba, Collection<Solicitud.Estado> estados);
}
