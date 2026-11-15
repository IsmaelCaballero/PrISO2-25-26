package es.uclm.iso2.integracion.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Pruebas tecnicas del token de setup de la matriz TDOP del CDU19. El token es la
 * descripcion textual de un escenario (sistema, modo, latencia, tiempo de espera y
 * reintentos) y debe sobrevivir a un viaje de ida y vuelta sin cambiar.
 */
class SetupSimulacionTest {

    @Test
    void elTokenDeVueltaEsIdenticAlOriginal() {
        SetupSimulacion setup = new SetupSimulacion(SistemaExterno.HCE,
                new PoliticaSimulacion(ModoSimulacion.CAIDO, 5, 50, 0));

        assertEquals("SIM-HCE-CAIDO-5-50-0", setup.toString());
        assertEquals(setup, SetupSimulacion.desdeToken(setup.toString()));
    }

    @Test
    void elModoLENTO_TIMEOUTSeRompeYSeReconstruyeSinPerderTramo() {
        SetupSimulacion setup = new SetupSimulacion(SistemaExterno.RIS,
                new PoliticaSimulacion(ModoSimulacion.LENTO_TIMEOUT, 30, 10, 2));

        assertEquals("SIM-RIS-LENTO_TIMEOUT-30-10-2", setup.toString());

        SetupSimulacion leido = SetupSimulacion.desdeToken(setup.toString());
        assertEquals(ModoSimulacion.LENTO_TIMEOUT, leido.politica().modo());
        assertEquals(30, leido.politica().latenciaMs());
        assertEquals(10, leido.politica().timeoutMs());
        assertEquals(2, leido.politica().reintentos());
    }

    @Test
    void elTokenSeAceptaConMayusculasMinisculasYEspacios() {
        SetupSimulacion esperado = new SetupSimulacion(SistemaExterno.SSO,
                new PoliticaSimulacion(ModoSimulacion.LENTO, 5, 500, 1));

        assertEquals(esperado, SetupSimulacion.desdeToken(" sim-sso-lento-5-500-1 "));
        assertEquals(esperado, SetupSimulacion.desdeToken("SIM-SSO-LENTO-5-500-1"));
    }

    @Test
    void losTokensMaltiadosSonDatosInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> SetupSimulacion.desdeToken(null));
        assertThrows(IllegalArgumentException.class, () -> SetupSimulacion.desdeToken("  "));
        assertThrows(IllegalArgumentException.class,
                () -> SetupSimulacion.desdeToken("HCE-CAIDO-5-50-0"));
        assertThrows(IllegalArgumentException.class,
                () -> SetupSimulacion.desdeToken("SIM-HCE-CAIDO-5-50"));
        assertThrows(IllegalArgumentException.class,
                () -> SetupSimulacion.desdeToken("SIM-SAP-CAIDO-5-50-0"));
        assertThrows(IllegalArgumentException.class,
                () -> SetupSimulacion.desdeToken("SIM-HCE-PESIMO-5-50-0"));
        assertThrows(IllegalArgumentException.class,
                () -> SetupSimulacion.desdeToken("SIM-HCE-CAIDO-cinco-50-0"));
    }
}
