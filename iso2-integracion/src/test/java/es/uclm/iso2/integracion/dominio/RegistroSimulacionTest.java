package es.uclm.iso2.integracion.dominio;

import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Pruebas tecnicas del registro vivo de politicas de simulacion (CDU19): es la pieza
 * que cambia la API para ejecutar los TCs degradados sin reiniciar, asi que se
 * comprueba el valor por defecto, el cambio en caliente y que la copia que se pinta
 * por GET no se pueda romper desde fuera.
 */
class RegistroSimulacionTest {

    @Test
    void unSistemaSinPoliticaArrancaEnModoNormal() {
        RegistroSimulacion registro = new RegistroSimulacion(null);

        PoliticaSimulacion politica = registro.de(SistemaExterno.HCE);
        assertEquals(ModoSimulacion.NORMAL, politica.modo());
        assertEquals(0, politica.latenciaMs());
        assertEquals(50, politica.timeoutMs());
        assertEquals(0, politica.reintentos());
    }

    @Test
    void cambiarInstalaLaPoliticaDesdeLaSiguienteLlamada() {
        RegistroSimulacion registro = new RegistroSimulacion(
                Map.of(SistemaExterno.RIS, PoliticaSimulacion.normal(20, 800, 0)));
        assertEquals(20, registro.de(SistemaExterno.RIS).latenciaMs());

        registro.cambiar(SistemaExterno.RIS,
                new PoliticaSimulacion(ModoSimulacion.LENTO_TIMEOUT, 30, 10, 2));

        PoliticaSimulacion nueva = registro.de(SistemaExterno.RIS);
        assertEquals(ModoSimulacion.LENTO_TIMEOUT, nueva.modo());
        assertEquals(30, nueva.latenciaMs());
        assertEquals(10, nueva.timeoutMs());
        assertEquals(2, nueva.reintentos());
    }

    @Test
    void cambiarSinSistemaOPoliticaEsDatosInvalidos() {
        RegistroSimulacion registro = new RegistroSimulacion(Map.of());

        assertThrows(IllegalArgumentException.class,
                () -> registro.cambiar(null, PoliticaSimulacion.normal(0, 50, 0)));
        assertThrows(IllegalArgumentException.class,
                () -> registro.cambiar(SistemaExterno.SSO, null));
    }

    @Test
    void todasDevuelveLosCuatroSistemasAunqueNoTenganPolitica() {
        RegistroSimulacion registro = new RegistroSimulacion(
                Map.of(SistemaExterno.PACS, new PoliticaSimulacion(ModoSimulacion.CAIDO, 7, 50, 0)));

        Map<SistemaExterno, PoliticaSimulacion> todas = registro.todas();
        assertEquals(4, todas.size());
        for (SistemaExterno sistema : SistemaExterno.values()) {
            org.junit.jupiter.api.Assertions.assertNotNull(todas.get(sistema));
        }
        assertEquals(ModoSimulacion.CAIDO, todas.get(SistemaExterno.PACS).modo());
        assertThrows(UnsupportedOperationException.class,
                () -> todas.put(SistemaExterno.SSO, PoliticaSimulacion.normal(0, 50, 0)));
    }
}
