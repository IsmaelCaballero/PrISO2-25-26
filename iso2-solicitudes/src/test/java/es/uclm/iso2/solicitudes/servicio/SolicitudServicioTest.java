package es.uclm.iso2.solicitudes.servicio;

import es.uclm.iso2.common.catalogo.PruebaCatalogoFacade;
import es.uclm.iso2.common.catalogo.PruebaCatalogoFacade.PoliticaDTO;
import es.uclm.iso2.common.catalogo.PruebaCatalogoFacade.ProtocoloDTO;
import es.uclm.iso2.common.catalogo.PruebaCatalogoFacade.PruebaImagenDTO;
import es.uclm.iso2.common.solicitudes.Prioridad;
import es.uclm.iso2.common.solicitudes.SolicitudConflictoException;
import es.uclm.iso2.common.solicitudes.SolicitudFacade.SolicitudDTO;
import es.uclm.iso2.common.solicitudes.SolicitudFacade.SolicitudNuevaDTO;
import es.uclm.iso2.common.solicitudes.SolicitudFacade.SolicitudPrescripcionDTO;
import es.uclm.iso2.common.solicitudes.SolicitudFacade.TrazabilidadSolicitudDTO;
import es.uclm.iso2.common.solicitudes.SolicitudNoEncontradaException;
import es.uclm.iso2.solicitudes.dominio.Solicitud;
import es.uclm.iso2.solicitudes.repositorio.SolicitudRepositorio;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Pruebas tecnicas del servicio CDU06 sobre H2 (D24): prescripcion con huella de no
 * repudio (RF06.2), anti-duplicado (RF06.3), validacion de la prueba contra el
 * catalogo (CDU16 simulado con un stub) y baseline de CFG-00 intacto. El reloj va fijo
 * para que la huella y la fecha sean deterministas. Los TC humanos del CDU los disenya
 * y ejecuta el equipo de CDU06 (D28).
 */
@DataJpaTest
class SolicitudServicioTest {

    private static final Instant FIJO = Instant.parse("2026-03-01T09:00:00Z");

    /** Catalogo de CDU16 simulado: RX01 activa, RX99 desactivada, el resto no existe. */
    @TestConfiguration
    static class ConfigCatalogo {

        @Bean
        PruebaCatalogoFacade catalogo() {
            PruebaCatalogoFacade stub = mock(PruebaCatalogoFacade.class);
            when(stub.buscarPorCodigo(anyString())).thenAnswer(inv -> {
                String codigo = inv.getArgument(0);
                if ("RX01".equals(codigo) || "RX02".equals(codigo)) {
                    return Optional.of(prueba(codigo, true));
                }
                if ("RX99".equals(codigo)) {
                    return Optional.of(prueba("RX99", false));
                }
                return Optional.empty();
            });
            return stub;
        }

        @Bean
        SolicitudServicio servicio(SolicitudRepositorio solicitudes, PruebaCatalogoFacade catalogo) {
            return new SolicitudServicio(solicitudes, catalogo, Clock.fixed(FIJO, ZoneOffset.UTC));
        }

        private static PruebaImagenDTO prueba(String codigo, boolean activa) {
            return new PruebaImagenDTO(codigo, "Radiografia", "RX", false, 10, activa,
                    new ProtocoloDTO("sin preparacion", 0, "ninguna"), new PoliticaDTO(24, 3));
        }
    }

    @Autowired
    SolicitudServicio servicio;

    @Autowired
    SolicitudRepositorio solicitudes;

    @Autowired
    TestEntityManager em;

    @Autowired
    jakarta.persistence.EntityManager jpa;

    private static SolicitudPrescripcionDTO prescripcion(String medico, String paciente, String prueba) {
        return new SolicitudPrescripcionDTO(paciente, prueba, "dolor toracico", Prioridad.ORDINARIA, "AP", medico);
    }

    @Test
    void prescribirCreaSolicitudConHuellaDeNoRepudio() {
        SolicitudDTO creada = servicio.prescribir(prescripcion("medico1", "P1", "RX01"));

        assertEquals("creada", creada.estado());
        assertEquals("P1", creada.pacienteId());
        assertEquals("RX01", creada.codigoPrueba());
        assertEquals(LocalDate.of(2026, 3, 1), creada.fechaSolicitada());
        assertTrue(creada.solicitudId().matches("[0-9a-f-]{36}"));

        TrazabilidadSolicitudDTO traza = servicio.trazable(creada.solicitudId());
        assertEquals("medico1", traza.autor());
        assertEquals(FIJO, traza.creadaEn());
        assertTrue(traza.huella().matches("[0-9a-f]{64}"));
        assertTrue(traza.integridadOk(), "recien creada la huella debe estar integra");
    }

    @Test
    void prescribirRechazaPruebaFueraDelCatalogo() {
        SolicitudConflictoException e = assertThrows(SolicitudConflictoException.class,
                () -> servicio.prescribir(prescripcion("medico1", "P1", "RX42")));
        assertEquals(SolicitudConflictoException.Conflicto.PRUEBA_NO_DISPONIBLE, e.tipo());
    }

    @Test
    void prescribirRechazaPruebaDesactivada() {
        SolicitudConflictoException e = assertThrows(SolicitudConflictoException.class,
                () -> servicio.prescribir(prescripcion("medico1", "P1", "RX99")));
        assertEquals(SolicitudConflictoException.Conflicto.PRUEBA_NO_DISPONIBLE, e.tipo());
    }

    @Test
    void laMismaPrescripcionActivaNoSePuedeRepetir() {
        servicio.prescribir(prescripcion("medico1", "P1", "RX01"));

        SolicitudConflictoException e = assertThrows(SolicitudConflictoException.class,
                () -> servicio.prescribir(prescripcion("medico1", "P1", "RX01")));
        assertEquals(SolicitudConflictoException.Conflicto.DUPLICADA, e.tipo());

        // Otro medico, otro paciente u otra prueba no cuentan como duplicado (RF06.3).
        servicio.prescribir(prescripcion("medico2", "P1", "RX01"));
        servicio.prescribir(prescripcion("medico1", "P2", "RX01"));
        servicio.prescribir(prescripcion("medico1", "P1", "RX02"));
    }

    @Test
    void cancelarLiberaElAntiDuplicado() {
        SolicitudDTO primera = servicio.prescribir(prescripcion("medico1", "P1", "RX01"));

        Solicitud entidad = solicitudes.findBySolicitudId(primera.solicitudId()).orElseThrow();
        entidad.cancelar();
        em.flush();

        SolicitudDTO segunda = servicio.prescribir(prescripcion("medico1", "P1", "RX01"));
        assertFalse(primera.solicitudId().equals(segunda.solicitudId()));
    }

    @Test
    void unaFilaManipuladaRompeLaIntegridad() {
        SolicitudDTO creada = servicio.prescribir(prescripcion("medico1", "P1", "RX01"));
        em.flush();
        jpa.createQuery("update Solicitud s set s.pacienteId = 'P-MANIPULADA' where s.solicitudId = :id")
                .setParameter("id", creada.solicitudId()).executeUpdate();
        em.clear();

        TrazabilidadSolicitudDTO traza = servicio.trazable(creada.solicitudId());
        assertFalse(traza.integridadOk(), "la huella guardada ya no corresponde con el contenido");
    }

    @Test
    void listarPorMedicoOrdenaDescartandoOtrosAutores() {
        solicitudes.save(new Solicitud("P1", "RX01", "ind", Prioridad.ORDINARIA, "AP", "medico1", FIJO));
        solicitudes.save(new Solicitud("P2", "RX01", "ind", Prioridad.URGENTE, "AP", "medico1", FIJO.plusSeconds(60)));
        solicitudes.save(new Solicitud("P3", "RX01", "ind", Prioridad.ORDINARIA, "AP", "medico2", FIJO));
        em.flush();

        List<SolicitudDTO> mias = servicio.listarPorMedico("medico1");
        assertEquals(2, mias.size());
        assertEquals("P2", mias.get(0).pacienteId(), "la mas reciente primero");
        assertEquals("P1", mias.get(1).pacienteId());

        assertThrows(IllegalArgumentException.class, () -> servicio.listarPorMedico(" "));
    }

    @Test
    void detalleYTrazabilidadExigenSolicitudExistente() {
        assertThrows(SolicitudNoEncontradaException.class, () -> servicio.detalle("no-existe"));
        assertThrows(SolicitudNoEncontradaException.class, () -> servicio.trazable("no-existe"));
        assertTrue(servicio.buscar("no-existe").isEmpty());
    }

    @Test
    void elBaselineDeCrearDelegaEnPrescribirConDefaults() {
        SolicitudDTO creada = servicio.crear(new SolicitudNuevaDTO("P1", "RX01", "AP"));
        assertEquals("creada", creada.estado());
        assertEquals("SISTEMA", servicio.trazable(creada.solicitudId()).autor());
        assertTrue(servicio.buscar(creada.solicitudId()).isPresent());

        assertThrows(IllegalArgumentException.class, () -> servicio.crear(new SolicitudNuevaDTO(" ", "RX01", "AP")));
        assertThrows(IllegalArgumentException.class, () -> servicio.crear(new SolicitudNuevaDTO("P1", null, "AP")));
    }

    @Test
    void elContratoExigeLosCamposObligatoriosDeRF061() {
        assertThrows(IllegalArgumentException.class, () -> new SolicitudPrescripcionDTO(
                " ", "RX01", "ind", Prioridad.ORDINARIA, "AP", "medico1"));
        assertThrows(IllegalArgumentException.class, () -> new SolicitudPrescripcionDTO(
                "P1", " ", "ind", Prioridad.ORDINARIA, "AP", "medico1"));
        assertThrows(IllegalArgumentException.class, () -> new SolicitudPrescripcionDTO(
                "P1", "RX01", " ", Prioridad.ORDINARIA, "AP", "medico1"));
        assertThrows(IllegalArgumentException.class, () -> new SolicitudPrescripcionDTO(
                "P1", "RX01", "ind", null, "AP", "medico1"));
        assertThrows(IllegalArgumentException.class, () -> new SolicitudPrescripcionDTO(
                "P1", "RX01", "ind", Prioridad.ORDINARIA, " ", "medico1"));
        assertThrows(IllegalArgumentException.class, () -> new SolicitudPrescripcionDTO(
                "P1", "RX01", "ind", Prioridad.ORDINARIA, "AP", " "));
    }

    @Test
    void elDominioSellaYSoloSePuedeCancelar() {
        Solicitud s = new Solicitud("P1", "RX01", "ind", Prioridad.PREFERENTE, "AP", "medico1", FIJO);
        assertTrue(s.esActiva());
        assertTrue(s.trazaIntegra());
        assertEquals(64, s.huellaCalculada().length());

        s.cancelar();
        assertFalse(s.esActiva());
        assertTrue(s.trazaIntegra(), "cancelar no altera los campos firmados");
    }
}
