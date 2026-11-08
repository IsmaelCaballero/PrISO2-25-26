package es.uclm.iso2.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Prueba de humo del cliente (CFG-00) sin levantar la plataforma JavaFX
 * (los agentes ejecutan el CI en entorno headless).
 */
class ClienteCaparazonTest {

    @Test
    void laVersionDelClienteEsLaDelReleaseTrain() {
        assertEquals("0.1.2-SNAPSHOT", Iso2ClientApplication.VERSION);
    }
}
