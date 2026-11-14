package es.uclm.iso2.citas.dominio;

import es.uclm.iso2.citas.dominio.AuditoriaCitas.AccionAuditoria;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas tecnicas de la maquina de estados del CDU12 sobre el dominio puro (sin Spring
 * ni base de datos): la matriz de transiciones de la figura de analisis, el calculo del
 * solape de huecos con los extremos que no cuentan, y la cadena de auditoria con hash
 * encadenado. El dominio es el que rechaza con {@link IllegalStateException}; el servicio
 * traduce, y eso se prueba en {@code CitaServicioTest}.
 */
class MaquinaEstadosCitasTest {

    private static final LocalDateTime INICIO = LocalDateTime.parse("2026-11-13T09:00:00");

    private static Cita citaProgramada() {
        return Cita.nueva(null, "api-medico", INICIO, 30);
    }

    // --- Nacimiento y datos derivados --------------------------------------

    @Test
    void nuevaCitaNaceProgramadaConIdentificadorYFinCalculado() {
        Cita cita = Cita.nueva(" api-sol-creada ", " api-medico ", INICIO, 45);

        assertEquals(Cita.Estado.PROGRAMADA, cita.getEstado());
        assertTrue(cita.getCitaId().matches("[0-9a-f-]{36}"));
        assertEquals("api-sol-creada", cita.getSolicitudId());
        assertEquals("api-medico", cita.getProfesionalId());
        assertEquals(INICIO.plusMinutes(45), cita.getFin());
        assertEquals(45, cita.duracionMinutos());
        assertNull(cita.getMotivoCancelacion());
        assertTrue(cita.esActiva());
        assertFalse(cita.getEstado().esTerminal());
    }

    @Test
    void solicitudEnBlancoEsCitaDirecta() {
        assertNull(Cita.nueva("", "api-medico", INICIO, 30).getSolicitudId());
        assertNull(Cita.nueva(null, "api-medico", INICIO, 30).getSolicitudId());
    }

    @Test
    void nuevaRechazaDatosObligatorios() {
        assertThrows(IllegalArgumentException.class, () -> Cita.nueva(null, " ", INICIO, 30));
        assertThrows(IllegalArgumentException.class, () -> Cita.nueva(null, null, INICIO, 30));
        assertThrows(IllegalArgumentException.class, () -> Cita.nueva(null, "api-medico", null, 30));
        assertThrows(IllegalArgumentException.class, () -> Cita.nueva(null, "api-medico", INICIO, 0));
        assertThrows(IllegalArgumentException.class, () -> Cita.nueva(null, "api-medico", INICIO, -5));
    }

    // --- Matriz de transiciones (valores limite de la maquina de estados) ---

    @Test
    void confirmacionSoloDesdeProgramada() {
        Cita cita = citaProgramada();
        cita.confirmar();
        assertEquals(Cita.Estado.CONFIRMADA, cita.getEstado());
        assertTrue(cita.esActiva());

        assertThrows(IllegalStateException.class, cita::confirmar);
    }

    @Test
    void realizacionDesdeProgramadaOConfirmadaYNoDesdeTerminal() {
        Cita desdeProgramada = citaProgramada();
        desdeProgramada.realizar();
        assertEquals(Cita.Estado.REALIZADA, desdeProgramada.getEstado());
        assertFalse(desdeProgramada.esActiva());
        assertTrue(desdeProgramada.getEstado().esTerminal());
        assertThrows(IllegalStateException.class, desdeProgramada::realizar);

        Cita desdeConfirmada = citaProgramada();
        desdeConfirmada.confirmar();
        desdeConfirmada.realizar();
        assertEquals(Cita.Estado.REALIZADA, desdeConfirmada.getEstado());
    }

    @Test
    void cancelacionDesdeProgramadaOConfirmadaGuardaMotivo() {
        Cita desdeProgramada = citaProgramada();
        desdeProgramada.cancelar("paciente avisó");
        assertEquals(Cita.Estado.CANCELADA, desdeProgramada.getEstado());
        assertEquals("paciente avisó", desdeProgramada.getMotivoCancelacion());
        assertThrows(IllegalStateException.class, () -> desdeProgramada.cancelar("otra vez"));

        Cita desdeConfirmada = citaProgramada();
        desdeConfirmada.confirmar();
        desdeConfirmada.cancelar("lluvia");
        assertEquals(Cita.Estado.CANCELADA, desdeConfirmada.getEstado());
    }

    @Test
    void expiracionDesdeProgramadaOConfirmadaYNoDesdeTerminal() {
        Cita desdeProgramada = citaProgramada();
        desdeProgramada.expirar();
        assertEquals(Cita.Estado.EXPIRADA, desdeProgramada.getEstado());
        assertThrows(IllegalStateException.class, desdeProgramada::expirar);

        Cita desdeConfirmada = citaProgramada();
        desdeConfirmada.confirmar();
        desdeConfirmada.expirar();
        assertEquals(Cita.Estado.EXPIRADA, desdeConfirmada.getEstado());
    }

    @Test
    void todosLosEstadosTerminalesRechazanCualquierTransicion() {
        for (Cita.Estado terminal : List.of(Cita.Estado.REALIZADA, Cita.Estado.CANCELADA,
                Cita.Estado.EXPIRADA)) {
            Cita cita = citaEnEstadoTerminal(terminal);
            assertThrows(IllegalStateException.class, cita::confirmar, terminal.toString());
            assertThrows(IllegalStateException.class, cita::realizar, terminal.toString());
            assertThrows(IllegalStateException.class, () -> cita.cancelar("motivo"), terminal.toString());
            assertThrows(IllegalStateException.class, cita::expirar, terminal.toString());
            assertThrows(IllegalStateException.class,
                    () -> cita.moverA(INICIO.plusHours(2), 30), terminal.toString());
            assertFalse(cita.esActiva(), terminal.toString());
        }
    }

    private static Cita citaEnEstadoTerminal(Cita.Estado terminal) {
        Cita cita = citaProgramada();
        switch (terminal) {
            case REALIZADA -> cita.realizar();
            case CANCELADA -> cita.cancelar("motivo");
            case EXPIRADA -> cita.expirar();
            default -> throw new IllegalArgumentException(terminal.name());
        }
        return cita;
    }

    @Test
    void reprogramacionConservaElEstadoYRecalculaElFin() {
        Cita cita = citaProgramada();
        cita.confirmar();
        LocalDateTime nuevoInicio = INICIO.plusHours(3);
        cita.moverA(nuevoInicio, 45);

        assertEquals(Cita.Estado.CONFIRMADA, cita.getEstado(), "mover no reinicia la maquina");
        assertEquals(nuevoInicio, cita.getInicio());
        assertEquals(nuevoInicio.plusMinutes(45), cita.getFin());
        assertEquals(45, cita.duracionMinutos());
    }

    // --- Solape de huecos ----------------------------------------------------

    @Test
    void losExtremosQueCoincidenNoSolapan() {
        CitaHueco hueco = new CitaHueco("c-1", "api-medico", INICIO, INICIO.plusMinutes(30));

        assertFalse(hueco.solapaCon(INICIO.plusMinutes(30), INICIO.plusMinutes(60)),
                "la cita que empieza cuando termina la otra no molesta");
        assertFalse(hueco.solapaCon(INICIO.minusMinutes(30), INICIO),
                "la cita que termina cuando empieza la otra no molesta");
        assertFalse(hueco.solapaCon(INICIO.plusHours(2), INICIO.plusHours(3)));
    }

    @Test
    void cualquierMinutoCompartiDOesSolape() {
        CitaHueco hueco = new CitaHueco("c-1", "api-medico", INICIO, INICIO.plusMinutes(30));

        assertTrue(hueco.solapaCon(INICIO.plusMinutes(15), INICIO.plusMinutes(45)), "solapa por la derecha");
        assertTrue(hueco.solapaCon(INICIO.minusMinutes(15), INICIO.plusMinutes(10)), "solapa por la izquierda");
        assertTrue(hueco.solapaCon(INICIO.plusMinutes(5), INICIO.plusMinutes(10)), "queda dentro");
        assertTrue(hueco.solapaCon(INICIO.minusHours(1), INICIO.plusHours(1)), "la contiene");
    }

    @Test
    void moverHuecoActualizaInicioYFinYSuVersionLaGestionaJpa() {
        CitaHueco hueco = new CitaHueco("c-1", "api-medico", INICIO, INICIO.plusMinutes(30));
        assertEquals(0, hueco.getVersion(), "sin persistir la version aun no interviene");

        hueco.moverA(INICIO.plusHours(4), INICIO.plusHours(4).plusMinutes(30));

        assertEquals(INICIO.plusHours(4), hueco.getInicio());
        assertEquals(INICIO.plusHours(4).plusMinutes(30), hueco.getFin());
        assertEquals("c-1", hueco.getCitaId());
        assertEquals("api-medico", hueco.getProfesionalId());
        assertNull(hueco.getId());
    }

    // --- Cadena de auditoria --------------------------------------------------

    @Test
    void canonicoYHashSonDeterministas() {
        Instant instante = Instant.parse("2026-11-11T08:00:00Z");
        String material = AuditoriaCitas.canonico(AccionAuditoria.AGENDAMIENTO, "c-1", "api-medico",
                "recepcion1", instante, null);
        assertEquals("AGENDAMIENTO|c-1|api-medico|recepcion1|2026-11-11T08:00:00Z|GENESIS", material);

        String hash = AuditoriaCitas.hashDe(AccionAuditoria.AGENDAMIENTO, "c-1", "api-medico",
                "recepcion1", instante, null);
        assertEquals(material, AuditoriaCitas.canonico(AccionAuditoria.AGENDAMIENTO, "c-1", "api-medico",
                "recepcion1", instante, null));
        assertTrue(hash.matches("[0-9a-f]{64}"), "SHA-256 en hexadecimal");
    }

    @Test
    void cadenaCorrectaVerificaYCadenaAlteradaNo() {
        Instant instante = Instant.parse("2026-11-11T08:00:00Z");
        String hash1 = AuditoriaCitas.hashDe(AccionAuditoria.AGENDAMIENTO, "c-1", "api-medico",
                "recepcion1", instante, null);
        AuditoriaCitas r1 = new AuditoriaCitas(AccionAuditoria.AGENDAMIENTO, "c-1", "api-medico",
                "recepcion1", instante, null, hash1);
        String hash2 = AuditoriaCitas.hashDe(AccionAuditoria.CONFIRMACION, "c-1", "api-medico",
                "recepcion1", instante, hash1);
        AuditoriaCitas r2 = new AuditoriaCitas(AccionAuditoria.CONFIRMACION, "c-1", "api-medico",
                "recepcion1", instante, hash1, hash2);

        assertTrue(AuditoriaCitas.cadenaIntegra(List.of()));
        assertTrue(AuditoriaCitas.cadenaIntegra(List.of(r1, r2)));

        r2.setHashParaPruebas("f".repeat(64));
        assertFalse(AuditoriaCitas.cadenaIntegra(List.of(r1, r2)),
                "tocar el hash de un registro a mano rompe la cadena");
    }

    @Test
    void eslabonQueNoEnlazaConElAnteriorRompeLaCadena() {
        Instant instante = Instant.parse("2026-11-11T08:00:00Z");
        String hash1 = AuditoriaCitas.hashDe(AccionAuditoria.AGENDAMIENTO, "c-1", "api-medico",
                "recepcion1", instante, null);
        AuditoriaCitas r1 = new AuditoriaCitas(AccionAuditoria.AGENDAMIENTO, "c-1", "api-medico",
                "recepcion1", instante, null, hash1);
        String hashRoto = AuditoriaCitas.hashDe(AccionAuditoria.CONFIRMACION, "c-1", "api-medico",
                "recepcion1", instante, "e".repeat(64));
        AuditoriaCitas r2 = new AuditoriaCitas(AccionAuditoria.CONFIRMACION, "c-1", "api-medico",
                "recepcion1", instante, "e".repeat(64), hashRoto);

        assertFalse(AuditoriaCitas.cadenaIntegra(List.of(r1, r2)),
                "insertar o borrar un eslabon rompe el enlazado");
    }
}
