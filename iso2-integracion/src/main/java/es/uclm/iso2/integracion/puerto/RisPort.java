package es.uclm.iso2.integracion.puerto;

import es.uclm.iso2.integracion.dominio.EstudioExterno;

/**
 * Salida hacia el sistema de informacion de radiologia (RIS).
 *
 * @since 1.1.0 (CFG-06, CDU19)
 */
public interface RisPort extends SistemaImagenPort {

    /**
     * Busca la cabecera de un estudio por su numero de acceso.
     *
     * @param accessionNumber numero de acceso del estudio
     * @return estudio encontrado
     */
    EstudioExterno buscarEstudio(String accessionNumber);
}
