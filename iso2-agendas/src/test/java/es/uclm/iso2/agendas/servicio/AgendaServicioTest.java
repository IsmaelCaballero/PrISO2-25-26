package es.uclm.iso2.agendas.servicio;

import es.uclm.iso2.agendas.repositorio.AgendaRepositorio;
import es.uclm.iso2.agendas.repositorio.AuditoriaAgendasRepositorio;
import es.uclm.iso2.common.agendas.AgendaConflictoException;
import es.uclm.iso2.common.agendas.AgendaFacade.AgendaDTO;
import es.uclm.iso2.common.agendas.AgendaFacade.FestivoDTO;
import es.uclm.iso2.common.agendas.AgendaFacade.FranjaDTO;
import es.uclm.iso2.common.agendas.AgendaFacade.FranjaGestionDTO;
import es.uclm.iso2.common.agendas.AgendaNoEncontradoException;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests de las reglas autoritativas de CDU13 exigidas por la puerta CFG-03
 * ("TC solapes/festivos"): no solape entre franjas, festivo manda sobre las franjas
 * y tramo {@code inicio < fin}, mas el contrato baseline de {@code franjasLibres} y
 * la verificabilidad de la cadena de auditoria.
 */
@DataJpaTest
@Import(AgendaServicio.class)
class AgendaServicioTest {

    private static final LocalDate LUNES = LocalDate.of(2026, 11, 9);
    private static final LocalDate MARTES = LocalDate.of(2026, 11, 10);
    private static final LocalDate MIERCOLES = LocalDate.of(2026, 11, 11);

    @Autowired
    private AgendaServicio servicio;

    @Autowired
    private AgendaRepositorio agendas;

    @Autowired
    private AuditoriaAgendasRepositorio auditoria;

    @Autowired
    private TestEntityManager em;

    @Test
    void crearAgendaYBuscarPorProfesional() {
        AgendaDTO creada = servicio.crearAgenda("medico");
        assertEquals("medico", creada.profesionalId());
        assertTrue(creada.franjas().isEmpty());
        assertTrue(creada.festivos().isEmpty());
        assertEquals("medico", servicio.buscarPorProfesional("medico").orElseThrow().profesionalId());
        assertEquals(1, servicio.listarAgendas().size());
        assertTrue(servicio.buscarPorProfesional("nadie").isEmpty());
    }

    @Test
    void laAgendaDuplicadaEsConflicto() {
        servicio.crearAgenda("medico");
        AgendaConflictoException e =
                assertThrows(AgendaConflictoException.class, () -> servicio.crearAgenda("medico"));
        assertEquals(AgendaConflictoException.Conflicto.AGENDA_DUPLICADA, e.tipo());
    }

    @Test
    void laAgendaNecesitaProfesional() {
        assertThrows(IllegalArgumentException.class, () -> servicio.crearAgenda("  "));
    }

    @Test
    void anadirFranjaDevuelveIdYSeReflejaEnElDetalle() {
        servicio.crearAgenda("medico");
        FranjaGestionDTO franja = servicio.anadirFranja("medico",
                new FranjaGestionDTO(null, LUNES, LocalTime.of(8, 0), LocalTime.of(15, 0)));
        assertNotNull(franja.id());
        AgendaDTO detalle = servicio.buscarPorProfesional("medico").orElseThrow();
        assertEquals(1, detalle.franjas().size());
        assertEquals(LocalTime.of(8, 0), detalle.franjas().get(0).inicio());
    }

    @Test
    void lasFranjasQueSeTocanEnElBordeNoSolapan() {
        servicio.crearAgenda("medico");
        servicio.anadirFranja("medico", franjaSinId(LUNES, "10:00", "12:00"));
        FranjaGestionDTO despues = servicio.anadirFranja("medico", franjaSinId(LUNES, "12:00", "14:00"));
        assertNotNull(despues.id());
        assertEquals(2, servicio.buscarPorProfesional("medico").orElseThrow().franjas().size());
    }

    @Test
    void elSolapeDeFranjasEsConflicto() {
        servicio.crearAgenda("medico");
        servicio.anadirFranja("medico", franjaSinId(LUNES, "10:00", "12:00"));
        AgendaConflictoException e = assertThrows(AgendaConflictoException.class,
                () -> servicio.anadirFranja("medico", franjaSinId(LUNES, "11:30", "13:00")));
        assertEquals(AgendaConflictoException.Conflicto.SOLAPE, e.tipo());
        // Distinto dia con la misma hora no es solape.
        assertNotNull(servicio.anadirFranja("medico", franjaSinId(MARTES, "11:30", "13:00")).id());
    }

    @Test
    void laFranjaExigeInicioAnteriorAlFin() {
        servicio.crearAgenda("medico");
        assertThrows(IllegalArgumentException.class,
                () -> servicio.anadirFranja("medico", franjaSinId(LUNES, "12:00", "12:00")));
        assertThrows(IllegalArgumentException.class,
                () -> servicio.anadirFranja("medico", franjaSinId(LUNES, "15:00", "08:00")));
        assertThrows(IllegalArgumentException.class,
                () -> servicio.anadirFranja("medico", new FranjaGestionDTO(null, LUNES, null, null)));
    }

    @Test
    void elFestivoMandaSobreLasFranjas() {
        servicio.crearAgenda("medico");
        servicio.anadirFestivo("medico", new FestivoDTO(null, MIERCOLES, "natural"));
        AgendaConflictoException e = assertThrows(AgendaConflictoException.class,
                () -> servicio.anadirFranja("medico", franjaSinId(MIERCOLES, "08:00", "15:00")));
        assertEquals(AgendaConflictoException.Conflicto.FESTIVO, e.tipo());
    }

    @Test
    void noSeDeclaraFestivoUnDiaConFranjas() {
        servicio.crearAgenda("medico");
        servicio.anadirFranja("medico", franjaSinId(LUNES, "08:00", "15:00"));
        AgendaConflictoException e = assertThrows(AgendaConflictoException.class,
                () -> servicio.anadirFestivo("medico", new FestivoDTO(null, LUNES, "vacaciones")));
        assertEquals(AgendaConflictoException.Conflicto.FESTIVO, e.tipo());
    }

    @Test
    void elFestivoRepetidoEsConflicto() {
        servicio.crearAgenda("medico");
        servicio.anadirFestivo("medico", new FestivoDTO(null, MIERCOLES, "natural"));
        AgendaConflictoException e = assertThrows(AgendaConflictoException.class,
                () -> servicio.anadirFestivo("medico", new FestivoDTO(null, MIERCOLES, "otro")));
        assertEquals(AgendaConflictoException.Conflicto.FESTIVO, e.tipo());
    }

    @Test
    void franjasLibresFiltraRangoYFestivos() {
        servicio.crearAgenda("medico");
        servicio.anadirFestivo("medico", new FestivoDTO(null, MARTES, "asunto"));
        servicio.anadirFranja("medico", franjaSinId(LUNES, "08:00", "15:00"));
        servicio.anadirFranja("medico", franjaSinId(MIERCOLES, "08:00", "15:00"));

        java.util.List<FranjaDTO> libres =
                servicio.franjasLibres("medico", LUNES, MIERCOLES);
        assertEquals(2, libres.size());
        assertEquals(LUNES, libres.get(0).dia());
        assertEquals(MIERCOLES, libres.get(1).dia());
        assertEquals("medico", libres.get(0).profesionalId());

        // Rango estrecho que deja fuera el festivo: solo MIERCOLES.
        assertEquals(1, servicio.franjasLibres("medico", MARTES, MIERCOLES).size());
        // Profesional sin agenda: lista vacia (contrato baseline tolerante).
        assertTrue(servicio.franjasLibres("fantasma", LUNES, MIERCOLES).isEmpty());
    }

    @Test
    void eliminarFranjaYFestivo() {
        servicio.crearAgenda("medico");
        Long franjaId = servicio.anadirFranja("medico", franjaSinId(LUNES, "08:00", "15:00")).id();
        Long festivoId = servicio.anadirFestivo("medico", new FestivoDTO(null, MIERCOLES, "natural")).id();

        servicio.eliminarFranja("medico", franjaId);
        servicio.eliminarFestivo("medico", festivoId);

        AgendaDTO detalle = servicio.buscarPorProfesional("medico").orElseThrow();
        assertTrue(detalle.franjas().isEmpty());
        assertTrue(detalle.festivos().isEmpty());
        // Y el dia vuelve a admitir franja tras quitar el festivo.
        assertNotNull(servicio.anadirFranja("medico", franjaSinId(MIERCOLES, "08:00", "15:00")).id());
    }

    @Test
    void eliminarElementosInexistentesDa404DeContrato() {
        servicio.crearAgenda("medico");
        assertThrows(AgendaNoEncontradoException.class, () -> servicio.eliminarFranja("medico", 999L));
        assertThrows(AgendaNoEncontradoException.class, () -> servicio.eliminarFestivo("medico", 999L));
        assertThrows(AgendaNoEncontradoException.class,
                () -> servicio.anadirFranja("fantasma", franjaSinId(LUNES, "08:00", "15:00")));
        assertThrows(AgendaNoEncontradoException.class,
                () -> servicio.anadirFestivo("fantasma", new FestivoDTO(null, LUNES, "x")));
    }

    @Test
    void laAuditoriaQuedaEncadenadaYVerificable() {
        servicio.crearAgenda("medico");
        Long franjaId = servicio.anadirFranja("medico", franjaSinId(LUNES, "08:00", "15:00")).id();
        servicio.anadirFestivo("medico", new FestivoDTO(null, MIERCOLES, "natural"));
        servicio.eliminarFranja("medico", franjaId);
        em.flush();

        assertEquals(4, auditoria.count());
        assertTrue(servicio.auditoriaVerificable());
    }

    @Test
    void laAuditoriaManipuladaRompeLaCadena() {
        servicio.crearAgenda("medico");
        servicio.anadirFranja("medico", franjaSinId(LUNES, "08:00", "15:00"));
        em.flush();
        auditoria.findAll().forEach(r -> {
            if (r.getAccion() == es.uclm.iso2.agendas.dominio.AuditoriaAgendas.AccionAuditoria.ALTA_FRANJA) {
                r.setHashParaPruebas("0".repeat(64));
            }
        });
        em.flush();
        assertFalse(servicio.auditoriaVerificable());
    }

    private static FranjaGestionDTO franjaSinId(LocalDate dia, String inicio, String fin) {
        return new FranjaGestionDTO(null, dia, LocalTime.parse(inicio), LocalTime.parse(fin));
    }
}
