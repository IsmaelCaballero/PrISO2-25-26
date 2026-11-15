package es.uclm.iso2.integracion.puerto;

import es.uclm.iso2.integracion.dominio.EstadoSistema;

/**
 * Comun a los puertos de sistemas de imagen (RIS y PACS): el spike CDU19 solo los
 * monitoriza, por eso la monitorizacion es la parte compartida.
 *
 * @since 1.1.0 (CFG-06, CDU19)
 */
public interface SistemaImagenPort {

    /**
     * Estado del sistema para monitorizacion (ping degradado, nunca lanza fallo).
     *
     * @return estado actual del sistema
     */
    EstadoSistema estado();
}
