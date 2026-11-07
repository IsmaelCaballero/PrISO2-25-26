package es.uclm.iso2.server;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Prueba de humo del caparazon servidor (CFG-00): el contexto Spring + H2 carga.
 */
@SpringBootTest
class Iso2ServerApplicationTests {

    @Test
    void elContextoCarga() {
        assertNotNull(Iso2ServerApplication.class);
    }
}
