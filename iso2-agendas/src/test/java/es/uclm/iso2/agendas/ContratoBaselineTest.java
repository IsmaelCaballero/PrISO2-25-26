package es.uclm.iso2.agendas;

import es.uclm.iso2.common.agendas.AgendaFacade;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Prueba de humo del baseline contractual (CFG-00): la fachada del dominio
 * se resuelve desde el classpath de este modulo (dependencia iso2-common OK).
 */
class ContratoBaselineTest {

    @Test
    void laFachadaDelDominioEstaDisponible() {
        assertEquals("es.uclm.iso2.common.agendas", AgendaFacade.class.getPackage().getName());
    }
}