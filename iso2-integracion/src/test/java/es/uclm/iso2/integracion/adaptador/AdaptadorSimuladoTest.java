package es.uclm.iso2.integracion.adaptador;

import es.uclm.iso2.common.integracion.CredencialesInvalidasException;
import es.uclm.iso2.common.integracion.HistoriaNoEncontradaException;
import es.uclm.iso2.common.integracion.SistemaExternoException;
import es.uclm.iso2.common.integracion.SistemaExternoException.Fallo;
import es.uclm.iso2.integracion.dominio.EstudioExterno;
import es.uclm.iso2.integracion.dominio.EstadoSistema;
import es.uclm.iso2.integracion.dominio.ModoSimulacion;
import es.uclm.iso2.integracion.dominio.PoliticaSimulacion;
import es.uclm.iso2.integracion.dominio.RegistroSimulacion;
import es.uclm.iso2.integracion.dominio.SesionSso;
import es.uclm.iso2.integracion.dominio.SistemaExterno;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas tecnicas de la base comun de los adaptadores simulados (CDU19): la politica
 * de simulacion decide antes de ejecutar la llamada, de ahi la precedencia 503 &gt;
 * 401 &gt; 400 que fija el contrato. Se comprueba tambien que en modo caido o con
 * tiempo superado la llamada real jamas llega a ejecutarse, el desgaste de reintentos
 * sobre la latencia y la media movil del ping.
 */
class AdaptadorSimuladoTest {

    /** Adaptador de un solo uso para observar si la llamada interna se ejecuta. */
    private static final class LlamadaViva extends AdaptadorSimulado {

        private final AtomicBoolean ejecutada = new AtomicBoolean();

        LlamadaViva(RegistroSimulacion registro) {
            super(SistemaExterno.HCE, registro);
        }

        <T> T ejecutar(Supplier<T> llamada) {
            return proteger(() -> {
                ejecutada.set(true);
                return llamada.get();
            });
        }

        boolean seEjecuto() {
            return ejecutada.get();
        }
    }

    /** Adaptador SSO que solo sirve para invocar proteger directamente. */
    private static final class ProtegeSso extends SsoAdaptadorSimulado {

        ProtegeSso(RegistroSimulacion registro) {
            super(registro);
        }

        SesionSso protegerTodo(Supplier<SesionSso> llamada) {
            return proteger(llamada);
        }
    }

    private static RegistroSimulacion registroCon(SistemaExterno sistema, PoliticaSimulacion politica) {
        return new RegistroSimulacion(Map.of(sistema, politica));
    }

    // ------------------------------------------------------------------
    // La llamada real no llega a ejecutarse cuando la politica falla
    // ------------------------------------------------------------------

    @Test
    void enModoCaidoLaLlamadaRealNuncaSeEjecuta() {
        RegistroSimulacion registro = registroCon(SistemaExterno.HCE,
                new PoliticaSimulacion(ModoSimulacion.CAIDO, 0, 50, 0));
        LlamadaViva adaptador = new LlamadaViva(registro);

        SistemaExternoException e = assertThrows(SistemaExternoException.class,
                () -> adaptador.ejecutar(() -> "valor"));
        assertEquals(Fallo.NO_DISPONIBLE, e.tipo());
        assertFalse(adaptador.seEjecuto(), "un sistema caido no debe llegar a invocar la llamada");
    }

    @Test
    void conTiempoSuperadoLaLlamadaRealTampocoSeEjecutaYSeAgotanLosReintentos() {
        long latencia = 30;
        RegistroSimulacion registro = registroCon(SistemaExterno.HCE,
                new PoliticaSimulacion(ModoSimulacion.LENTO_TIMEOUT, latencia, 5, 2));
        LlamadaViva adaptador = new LlamadaViva(registro);

        long inicio = System.nanoTime();
        SistemaExternoException e = assertThrows(SistemaExternoException.class,
                () -> adaptador.ejecutar(() -> "valor"));
        long transcurridoMs = (System.nanoTime() - inicio) / 1_000_000;

        assertEquals(Fallo.TIEMPO_SUPERADO, e.tipo());
        assertFalse(adaptador.seEjecuto());
        // 1 intento + 2 reintentos de 30 ms: el desgaste minimo es de 3 latencias.
        assertTrue(transcurridoMs >= 3 * latencia,
                "esperado al menos 3 latencias, medido " + transcurridoMs + " ms");
    }

    @Test
    void sinReintentosElDesgasteDelTiempoSuperadoEsDeUnaSolaLatencia() {
        long latencia = 40;
        RegistroSimulacion registro = registroCon(SistemaExterno.HCE,
                new PoliticaSimulacion(ModoSimulacion.LENTO_TIMEOUT, latencia, 5, 0));
        LlamadaViva adaptador = new LlamadaViva(registro);

        long inicio = System.nanoTime();
        assertThrows(SistemaExternoException.class, () -> adaptador.ejecutar(() -> "valor"));
        long transcurridoMs = (System.nanoTime() - inicio) / 1_000_000;

        assertTrue(transcurridoMs >= latencia);
        assertTrue(transcurridoMs < 3 * latencia,
                "sin reintentos no debe pagarse mas de una latencia, medido " + transcurridoMs + " ms");
    }

    @Test
    void lentoDentroDePlazoRespondeNormal() {
        long latencia = 15;
        RegistroSimulacion registro = registroCon(SistemaExterno.HCE,
                new PoliticaSimulacion(ModoSimulacion.LENTO, latencia, 500, 2));
        LlamadaViva adaptador = new LlamadaViva(registro);

        long inicio = System.nanoTime();
        String valor = adaptador.ejecutar(() -> "valor");
        long transcurridoMs = (System.nanoTime() - inicio) / 1_000_000;

        assertEquals("valor", valor);
        assertTrue(adaptador.seEjecuto());
        assertTrue(transcurridoMs >= latencia, "la latencia simulada debe pagarse, medido "
                + transcurridoMs + " ms");
    }

    // ------------------------------------------------------------------
    // Precedencia 503 > 401 > 400 (la politica se mira antes que la peticion)
    // ------------------------------------------------------------------

    @Test
    void elFalloDeSistemaGanaALaCredencialInvalida() {
        RegistroSimulacion registro = registroCon(SistemaExterno.SSO,
                new PoliticaSimulacion(ModoSimulacion.CAIDO, 0, 50, 0));
        SsoAdaptadorSimulado sso = new SsoAdaptadorSimulado(registro);

        SistemaExternoException e = assertThrows(SistemaExternoException.class,
                () -> sso.autenticar("nadie", "fallida"));
        assertEquals(Fallo.NO_DISPONIBLE, e.tipo());
    }

    @Test
    void elFalloDeSistemaGanaAlIdentificadorVacio() {
        RegistroSimulacion registro = registroCon(SistemaExterno.HCE,
                new PoliticaSimulacion(ModoSimulacion.CAIDO, 0, 50, 0));
        HceAdaptadorSimulado hce = new HceAdaptadorSimulado(registro);

        assertThrows(SistemaExternoException.class, () -> hce.buscarHistoria("   "));

        registro.cambiar(SistemaExterno.HCE, PoliticaSimulacion.normal(0, 50, 0));
        assertThrows(IllegalArgumentException.class, () -> hce.buscarHistoria("   "));
    }

    @Test
    void elTiempoSuperadoGanaAlPacienteInexistente() {
        RegistroSimulacion registro = registroCon(SistemaExterno.HCE,
                new PoliticaSimulacion(ModoSimulacion.LENTO_TIMEOUT, 20, 5, 0));
        HceAdaptadorSimulado hce = new HceAdaptadorSimulado(registro);

        SistemaExternoException e = assertThrows(SistemaExternoException.class,
                () -> hce.buscarHistoria("paciente-que-no-existe"));
        assertEquals(Fallo.TIEMPO_SUPERADO, e.tipo());
    }

    @Test
    void laCredencialInvalidaSigueLlegandoCuandoElSistemaResponde() {
        RegistroSimulacion registro = new RegistroSimulacion(Map.of());
        SsoAdaptadorSimulado sso = new SsoAdaptadorSimulado(registro);

        assertThrows(CredencialesInvalidasException.class, () -> sso.autenticar("nadie", "fallida"));
        assertDoesNotThrow(() -> sso.autenticar("idp-admin", "idp-pass"));
    }

    // ------------------------------------------------------------------
    // Ping degradado y media movil
    // ------------------------------------------------------------------

    @Test
    void elEstadoDelSistemaJamasLanzaAunqueEsteCaido() {
        RegistroSimulacion registro = registroCon(SistemaExterno.PACS,
                new PoliticaSimulacion(ModoSimulacion.CAIDO, 7, 50, 0));
        PacsAdaptadorSimulado pacs = new PacsAdaptadorSimulado(registro);

        EstadoSistema estado = assertDoesNotThrow(pacs::estado);
        assertEquals(SistemaExterno.PACS, estado.sistema());
        assertFalse(estado.disponible());
        assertEquals("CAIDO", estado.motivo());
        assertEquals(7, estado.latenciaMediaMs(), "sin llamadas la media es la latencia de la politica");
    }

    @Test
    void elEstadoReportaTiempoSuperadoComoNoDisponible() {
        RegistroSimulacion registro = registroCon(SistemaExterno.RIS,
                new PoliticaSimulacion(ModoSimulacion.LENTO_TIMEOUT, 40, 10, 1));
        RisAdaptadorSimulado ris = new RisAdaptadorSimulado(registro);

        EstadoSistema estado = assertDoesNotThrow(ris::estado);
        assertFalse(estado.disponible());
        assertEquals("TIEMPO_SUPERADO", estado.motivo());
    }

    @Test
    void laMediaMovilSoloGuardaLasUltimasDiezLatencias() {
        RegistroSimulacion registro = new RegistroSimulacion(Map.of());
        RisAdaptadorSimulado ris = new RisAdaptadorSimulado(registro);

        registro.cambiar(SistemaExterno.RIS, PoliticaSimulacion.normal(10, 500, 0));
        for (int i = 0; i < 4; i++) {
            ris.buscarEstudio("ACC-0001");
        }
        // El tiempo superado tambien gasta y registra su latencia en cada intento.
        registro.cambiar(SistemaExterno.RIS, new PoliticaSimulacion(ModoSimulacion.LENTO_TIMEOUT, 50, 5, 0));
        for (int i = 0; i < 12; i++) {
            assertThrows(SistemaExternoException.class, () -> ris.buscarEstudio("ACC-0001"));
        }

        // La ventana es de 10: las cuatro latencias buenas de 10 ms ya se cayeron
        // y la media quedan solo los 50 ms de los intentos fallidos.
        EstadoSistema estado = ris.estado();
        assertFalse(estado.disponible());
        assertEquals(50, estado.latenciaMediaMs());
    }

    @Test
    void laMediaMovilReflejaLasLatenciasObservadas() {
        RegistroSimulacion registro = new RegistroSimulacion(Map.of());
        RisAdaptadorSimulado ris = new RisAdaptadorSimulado(registro);

        registro.cambiar(SistemaExterno.RIS, PoliticaSimulacion.normal(10, 500, 0));
        ris.buscarEstudio("ACC-0001");
        registro.cambiar(SistemaExterno.RIS, PoliticaSimulacion.normal(20, 500, 0));
        ris.buscarEstudio("ACC-0002");

        EstadoSistema estado = ris.estado();
        assertTrue(estado.disponible());
        assertEquals(15, estado.latenciaMediaMs(), "media de 10 y 20 observadas");
    }

    // ------------------------------------------------------------------
    // Recortado de identificadores (leccion de CDU12)
    // ------------------------------------------------------------------

    @Test
    void losIdentificadoresSeBuscanAunqueLleguenConEspacios() {
        RegistroSimulacion registro = new RegistroSimulacion(Map.of());

        SesionSso sesion = new SsoAdaptadorSimulado(registro).autenticar("  idp-admin ", "idp-pass");
        assertEquals("idp-admin", sesion.sujeto());
        assertEquals("radiologia", sesion.rolExterno());
        assertTrue(sesion.caducaEn().isAfter(Instant.now()));

        EstudioExterno estudio = new RisAdaptadorSimulado(registro).buscarEstudio("  acc-0002 ");
        assertEquals("ACC-0002", estudio.accessionNumber());
        assertEquals(LocalDate.of(2025, 10, 2), estudio.fechaEstudio());

        assertEquals("api-paciente-ok",
                new HceAdaptadorSimulado(registro).buscarHistoria("  api-paciente-ok  ")
                        .pacienteExternoId());
    }

    @Test
    void losIdentificadoresVaciosSonDatosInvalidosCuandoElSistemaResponde() {
        RegistroSimulacion registro = new RegistroSimulacion(Map.of());

        assertThrows(IllegalArgumentException.class,
                () -> new SsoAdaptadorSimulado(registro).autenticar("   ", "idp-pass"));
        assertThrows(IllegalArgumentException.class,
                () -> new SsoAdaptadorSimulado(registro).autenticar("idp-admin", "  "));
        assertThrows(IllegalArgumentException.class,
                () -> new RisAdaptadorSimulado(registro).buscarEstudio(" "));

        HceAdaptadorSimulado hce = new HceAdaptadorSimulado(new RegistroSimulacion(Map.of()));
        assertThrows(HistoriaNoEncontradaException.class, () -> hce.buscarHistoria("quien-esos"));
    }

    @Test
    void elPacienteRotativoAlternaContenidoSinFallar() {
        RegistroSimulacion registro = new RegistroSimulacion(Map.of());
        HceAdaptadorSimulado hce = new HceAdaptadorSimulado(registro);

        assertEquals(1, hce.buscarHistoria("api-paciente-rotativo").registros().size());
        assertEquals(2, hce.buscarHistoria("api-paciente-rotativo").registros().size());
        assertEquals(1, hce.buscarHistoria("api-paciente-rotativo").registros().size());
    }
}
