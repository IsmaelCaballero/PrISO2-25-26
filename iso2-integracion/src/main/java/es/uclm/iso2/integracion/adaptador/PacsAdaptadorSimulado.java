package es.uclm.iso2.integracion.adaptador;

import es.uclm.iso2.integracion.dominio.RegistroSimulacion;
import es.uclm.iso2.integracion.dominio.SistemaExterno;
import es.uclm.iso2.integracion.puerto.PacsPort;
import org.springframework.stereotype.Component;

/**
 * Adaptador simulado del PACS. En el spike CDU19 el PACS solo se monitoriza (ping
 * degradado); el despacho DICOM real llegara con CDU11/CDU09.
 *
 * @since 1.1.0 (CFG-06, CDU19)
 */
@Component
public class PacsAdaptadorSimulado extends AdaptadorSimulado implements PacsPort {

    /**
     * Registra el adaptador del PACS sobre el sistema PACS.
     *
     * @param registro registro compartido de politicas de simulacion
     */
    public PacsAdaptadorSimulado(RegistroSimulacion registro) {
        super(SistemaExterno.PACS, registro);
    }
}
