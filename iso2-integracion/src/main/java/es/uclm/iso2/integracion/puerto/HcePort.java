package es.uclm.iso2.integracion.puerto;

import es.uclm.iso2.integracion.dominio.EstadoSistema;
import es.uclm.iso2.integracion.dominio.HistoriaExterna;

/**
 * Salida hacia la historia clinica electronica externa.
 *
 * @since 1.1.0 (CFG-06, CDU19)
 */
public interface HcePort {

    /**
     * Busca la historia externa de un paciente.
     *
     * @param pacienteExternoId identificador del paciente en el HCE
     * @return historia encontrada
     */
    HistoriaExterna buscarHistoria(String pacienteExternoId);

    /**
     * Estado del HCE para monitorizacion (ping degradado, nunca lanza fallo).
     *
     * @return estado actual del sistema HCE
     */
    EstadoSistema estado();
}
