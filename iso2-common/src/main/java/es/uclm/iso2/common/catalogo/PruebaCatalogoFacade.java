package es.uclm.iso2.common.catalogo;

import es.uclm.iso2.common.shared.Paginacion;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Fachada de contratos del subdominio {@code catalogo}: Catalogo clinico y administrativo de pruebas de imagen (CDU16).
 * Solo contrato: la implementacion vive en el modulo iso2-catalogo (baseline 1.0.0, D22).
 *
 * @since 1.0.0 (CFG-00)
 */
public interface PruebaCatalogoFacade {

    /**
     * Lista las pruebas del catalogo con paginacion.
     *
     * @param paginacion parametros de pagina, tamano y orden
     * @return pagina de pruebas del catalogo
     */
    List<PruebaImagenDTO> listar(Paginacion paginacion);

    /**
     * Busca una prueba por su codigo de catalogo.
     *
     * @param codigo codigo unico de la prueba
     * @return la prueba si existe, vacio en caso contrario
     */
    Optional<PruebaImagenDTO> buscarPorCodigo(String codigo);

    /**
     * Prueba de imagen del catalogo.
     *
     * @param codigo       codigo unico
     * @param nombre       nombre clinico de la prueba
     * @param categoria    categoria (RX, TAC, RM, Eco, MMG...)
     * @param requiereCita true si necesita cita previa
     * @param duracionMinutos duracion estimada de la prueba
     */
    record PruebaImagenDTO(String codigo, String nombre, String categoria, boolean requiereCita, int duracionMinutos) {
    }
}
