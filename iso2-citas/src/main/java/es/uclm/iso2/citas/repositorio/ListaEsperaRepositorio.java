package es.uclm.iso2.citas.repositorio;

import es.uclm.iso2.citas.dominio.ListaEspera;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Acceso a datos de las entradas de la lista de espera (CDU14). La proyeccion HTTP
 * de la cola necesita filtros opcionales, igual que la busqueda de CDU12; el orden de
 * la consulta es el mismo que {@link ListaEspera#porOrdenDeEspera()} para que la
 * posicion calculada en memoria coincida con la fila que pinta la web (RF14.3).
 *
 * @since 1.2.0 (CFG-07, CDU14)
 */
public interface ListaEsperaRepositorio extends JpaRepository<ListaEspera, Long> {

    /**
     * Busca una entrada por su identificador funcional.
     *
     * @param esperaId identificador funcional
     * @return la entrada, si existe
     */
    Optional<ListaEspera> findByEsperaId(String esperaId);

    /**
     * Busca la entrada viva que bloquea el alta duplicada: la {@code claveDuplicada}
     * solo es no nula mientras la entrada esta ACTIVA, asi que un golpe aqui es un
     * duplicado vivo (409 CONFLICTO_ALTA_DUPLICADA).
     *
     * @param claveDuplicada clave pacienteId|pruebaCodigo
     * @return la entrada en conflicto, si existe
     */
    Optional<ListaEspera> findByClaveDuplicada(String claveDuplicada);

    /**
     * Busca la entrada de un paciente para una prueba en un estado concreto: sirve
     * para el detalle y para comprobar si ya tiene una entrada ACTIVA.
     *
     * @param pacienteId   paciente buscado
     * @param pruebaCodigo prueba buscada
     * @param estado       estado buscado
     * @return la entrada, si existe
     */
    Optional<ListaEspera> findByPacienteIdAndPruebaCodigoAndEstado(
            String pacienteId, String pruebaCodigo, ListaEspera.Estado estado);

    /**
     * Cola con filtros opcionales para la pantalla del Administrativo. Un filtro
     * {@code null} no estrecha el resultado: sin {@code estado} se sirven activas y
     * terminales. El orden replicado de {@link ListaEspera#porOrdenDeEspera()} deja
     * la lista lista para numerar posiciones 1..n.
     *
     * @param pruebaCodigo  prueba a filtrar, {@code null} si no se filtra
     * @param profesionalId profesional a filtrar, {@code null} si no se filtra
     * @param estado        estado a filtrar, {@code null} si no se filtra
     * @return entradas que cumplen los filtros, en el orden determinista de la cola
     */
    @Query("""
            select e from ListaEspera e
             where (:pruebaCodigo is null or e.pruebaCodigo = :pruebaCodigo)
               and (:profesionalId is null or e.profesionalId = :profesionalId)
               and (:estado is null or e.estado = :estado)
             order by e.prioridad desc, e.fechaSolicitud asc, e.esperaId asc
            """)
    List<ListaEspera> cola(String pruebaCodigo, String profesionalId, ListaEspera.Estado estado);

    /**
     * Entradas ACTIVA de una prueba: el total que acompana a la posicion calculada.
     *
     * @param pruebaCodigo codigo de la prueba
     * @return numero de entradas activas de esa prueba
     */
    @Query("""
            select count(e) from ListaEspera e
             where e.pruebaCodigo = :pruebaCodigo and e.estado = 'ACTIVA'
            """)
    long contarActivas(String pruebaCodigo);
}
