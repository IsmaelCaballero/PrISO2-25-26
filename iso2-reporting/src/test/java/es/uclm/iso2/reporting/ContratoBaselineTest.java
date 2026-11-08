package es.uclm.iso2.reporting;

import es.uclm.iso2.common.reporting.CuadroMandoFacade;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Prueba de humo del baseline contractual (CFG-00): la fachada del dominio
 * se resuelve desde el classpath de este modulo (dependencia iso2-common OK).
 */
class ContratoBaselineTest {

    @Test
    void laFachadaDelDominioEstaDisponible() {
        assertEquals("es.uclm.iso2.common.reporting", CuadroMandoFacade.class.getPackage().getName());
    }
}