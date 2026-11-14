package es.uclm.iso2.citas.servicio;

import es.uclm.iso2.citas.dominio.AuditoriaCitas;
import es.uclm.iso2.citas.dominio.AuditoriaCitas.AccionAuditoria;
import es.uclm.iso2.citas.dominio.Cita;
import es.uclm.iso2.citas.dominio.CitaHueco;
import es.uclm.iso2.citas.repositorio.AuditoriaCitasRepositorio;
import es.uclm.iso2.citas.repositorio.CitaHuecoRepositorio;
import es.uclm.iso2.citas.repositorio.CitaRepositorio;
import es.uclm.iso2.common.agendas.AgendaFacade;
import es.uclm.iso2.common.catalogo.PruebaCatalogoFacade;
import es.uclm.iso2.common.citas.CitaConflictoException;
import es.uclm.iso2.common.citas.CitaConflictoException.Conflicto;
import es.uclm.iso2.common.citas.CitaFacade.CitaDTO;
import es.uclm.iso2.common.citas.CitaFacade.CitaNuevaDTO;
import es.uclm.iso2.common.citas.CitaFacade.DetalleCitaDTO;
import es.uclm.iso2.common.citas.CitaFacade.HuecoDTO;
import es.uclm.iso2.common.citas.CitaNoEncontradaException;
import es.uclm.iso2.common.solicitudes.SolicitudFacade;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static es.uclm.iso2.citas.servicio.PruebasCitas.DIA;
import static es.uclm.iso2.citas.servicio.PruebasCitas.DURACION;
import static es.uclm.iso2.citas.servicio.PruebasCitas.FESTIVO;
import static es.uclm.iso2.citas.servicio.PruebasCitas.INICIO;
import static es.uclm.iso2.citas.servicio.PruebasCitas.INICIO_FOREIRA;
import static es.uclm.iso2.citas.servicio.PruebasCitas.INICIO_FESTIVO;
import static es.uclm.iso2.citas.servicio.PruebasCitas.INICIO_TARDE;
import static es.uclm.iso2.citas.servicio.PruebasCitas.PROF_MEDICO;
import static es.uclm.iso2.citas.servicio.PruebasCitas.PROF_SIN_AGENDA;
import static es.uclm.iso2.citas.servicio.PruebasCitas.SOL_CITADA;
import static es.uclm.iso2.citas.servicio.PruebasCitas.SOL_CREADA;
import static es.uclm.iso2.citas.servicio.PruebasCitas.SOL_INEXISTENTE;
import static es.uclm.iso2.citas.servicio.PruebasCitas.SOL_PRESCRITA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas tecnicas del servicio CDU12 sobre H2 (D24): agendar con solicitud y directa,
 * validaciones de entrada, franja y festivo (CDU13), ocupacion del hueco, la maquina de
 * estados con sus conflictos, el plazo de cancelacion (CDU16), la reapertura de la
 * solicitud (CDU06), el lote de expiracion, el motor de huecos libres y la auditoria
 * encadenada. El reloj del contexto de prueba va fijo en el instante de {@code PruebasCitas}.
 */
@DataJpaTest
@Import(PruebasCitas.class)
class CitaServicioTest {

    @Autowired
    CitaServicio servicio;

    @Autowired
    CitaRepositorio citas;

    @Autowired
    CitaHuecoRepositorio huecos;

    @Autowired
    AuditoriaCitasRepositorio auditoria;

    @Autowired
    AgendaFacade agendas;

    @Autowired
    PruebaCatalogoFacade catalogo;

    @Autowired
    SolicitudFacade fachadaSolicitudes;

    /** Fachada falsa accesible para comprobar el estado de la solicitud tras cada operacion. */
    PruebasCitas.SolicitudesFalsas solicitudes;

    @BeforeEach
    void limpiarBase() {
        // El contexto esta compartido con las pruebas de concurrencia, que si confirman
        // filas: hay que dejar las tres tablas vacias antes de cada test.
        citas.deleteAll();
        huecos.deleteAll();
        auditoria.deleteAll();
        solicitudes = (PruebasCitas.SolicitudesFalsas) fachadaSolicitudes;
        solicitudes.reiniciar();
        SecurityContextHolder.clearContext();
    }

    /** Agenda una cita directa (sin solicitud) con duracion explicita. */
    private CitaDTO agendarDirecta(LocalDateTime inicio) {
        return servicio.agendar(new CitaNuevaDTO(null, PROF_MEDICO, inicio, DURACION));
    }

    // --- Agendar ------------------------------------------------------------

    @Test
    void agendarDirectaCreaCitaHuecoYAuditadoPorSistema() {
        CitaDTO dto = agendarDirecta(INICIO);

        assertNotNull(dto.citaId());
        assertEquals("programada", dto.estado());
        assertEquals(INICIO, dto.inicio());
        assertEquals(INICIO.plusMinutes(DURACION), dto.fin());
        assertNull(dto.solicitudId());

        Cita guardada = citas.findByCitaId(dto.citaId()).orElseThrow();
        assertEquals(Cita.Estado.PROGRAMADA, guardada.getEstado());

        CitaHueco hueco = huecos.findByCitaId(dto.citaId()).orElseThrow();
        assertEquals(PROF_MEDICO, hueco.getProfesionalId());
        assertEquals(INICIO, hueco.getInicio());

        List<AuditoriaCitas> registros = auditoria.findAllByOrderByIdAsc();
        assertEquals(1, registros.size());
        assertEquals(AccionAuditoria.AGENDAMIENTO, registros.get(0).getAccion());
        assertEquals("sistema", registros.get(0).getUsuario());
        assertNull(registros.get(0).getHashAnterior());
        assertTrue(servicio.auditoriaVerificable());
    }

    @Test
    void agendarConSolicitudTomaDuracionDelCatalogoYLaDejaCitada() {
        CitaDTO dto = servicio.agendar(new CitaNuevaDTO(SOL_CREADA, PROF_MEDICO, INICIO, 0));

        assertEquals(SOL_CREADA, dto.solicitudId());
        assertEquals(INICIO.plusMinutes(DURACION), dto.fin());
        assertEquals("citada", solicitudes.estado(SOL_CREADA));
    }

    @Test
    void agendarRechazaDatosInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> servicio.agendar(null));
        assertThrows(IllegalArgumentException.class,
                () -> servicio.agendar(new CitaNuevaDTO(null, " ", INICIO, DURACION)));
        assertThrows(IllegalArgumentException.class,
                () -> servicio.agendar(new CitaNuevaDTO(null, PROF_MEDICO, null, DURACION)));
        assertThrows(IllegalArgumentException.class,
                () -> servicio.agendar(new CitaNuevaDTO(null, PROF_MEDICO, INICIO, -5)));
        // Cita directa sin duracion: no hay prueba catalogada de donde sacarla.
        assertThrows(IllegalArgumentException.class,
                () -> servicio.agendar(new CitaNuevaDTO(null, PROF_MEDICO, INICIO, 0)));
        assertEquals(0, citas.count());
    }

    @Test
    void agendarRechazaSolicitudNoApta() {
        for (String solicitudId : List.of(SOL_INEXISTENTE, SOL_PRESCRITA, SOL_CITADA)) {
            CitaConflictoException e = assertThrows(CitaConflictoException.class,
                    () -> servicio.agendar(new CitaNuevaDTO(solicitudId, PROF_MEDICO, INICIO, DURACION)));
            assertEquals(Conflicto.SOLICITUD_NO_APTA, e.tipo());
        }
        assertEquals(0, citas.count());
        assertEquals(0, huecos.count());
    }

    @Test
    void agendarRechazaFranjasInvalidas() {
        // Antes de la apertura de la jornada (08:00).
        CitaConflictoException antes = assertThrows(CitaConflictoException.class, () -> servicio.agendar(
                new CitaNuevaDTO(null, PROF_MEDICO, INICIO_FOREIRA, DURACION)));
        assertEquals(Conflicto.FUERA_DE_FRANJA, antes.tipo());

        // Dia festivo: no hay franja ese dia (CDU13 manda sobre las franjas).
        CitaConflictoException festivo = assertThrows(CitaConflictoException.class, () -> servicio.agendar(
                new CitaNuevaDTO(null, PROF_MEDICO, INICIO_FESTIVO, DURACION)));
        assertEquals(Conflicto.FUERA_DE_FRANJA, festivo.tipo());

        // Profesional sin agenda: el AgendaNoEncontradoException de CDU13 se traduce aqui.
        CitaConflictoException sinAgenda = assertThrows(CitaConflictoException.class, () -> servicio.agendar(
                new CitaNuevaDTO(null, PROF_SIN_AGENDA, INICIO, DURACION)));
        assertEquals(Conflicto.FUERA_DE_FRANJA, sinAgenda.tipo());

        assertEquals(0, huecos.count());
    }

    @Test
    void agendarRechazaHuecoOcupadoEInicioExacto() {
        agendarDirecta(INICIO);

        // Mismo inicio exacto: la columna de la restriccion unica.
        CitaConflictoException mismo = assertThrows(CitaConflictoException.class, () -> agendarDirecta(INICIO));
        assertEquals(Conflicto.HUECO_OCUPADO, mismo.tipo());

        // Solape a mitad de la cita existente (09:15 cae dentro de 09:00-09:30).
        CitaConflictoException solapado = assertThrows(CitaConflictoException.class,
                () -> agendarDirecta(INICIO.plusMinutes(15)));
        assertEquals(Conflicto.HUECO_OCUPADO, solapado.tipo());

        assertEquals(1, citas.count());
        assertEquals(1, huecos.count());
    }

    @Test
    void agendarPermiteHuecosQueSoloSeTocan() {
        // Limite de equivalencia: el fin de una cita puede coincidir con el inicio de la
        // siguiente sin solaparse.
        agendarDirecta(INICIO);
        CitaDTO despues = agendarDirecta(INICIO.plusMinutes(DURACION));

        assertEquals("programada", despues.estado());
        assertEquals(2, huecos.count());
    }

    // --- Cancelar -----------------------------------------------------------

    @Test
    void cancelarFelizLiberaHuecoYReabreSolicitud() {
        CitaDTO creada = servicio.agendar(new CitaNuevaDTO(SOL_CREADA, PROF_MEDICO, INICIO, DURACION));

        CitaDTO cancelada = servicio.cancelar(creada.citaId(), "  el paciente avisa con tiempo  ");

        assertEquals("cancelada", cancelada.estado());
        DetalleCitaDTO detalle = servicio.detalle(creada.citaId());
        assertEquals("el paciente avisa con tiempo", detalle.motivoCancelacion());
        assertFalse(huecos.findByCitaId(creada.citaId()).isPresent());
        assertEquals("creada", solicitudes.estado(SOL_CREADA));
        assertEquals(2, auditoria.count());
        assertEquals(AccionAuditoria.CANCELACION, auditoria.findAllByOrderByIdAsc().get(1).getAccion());
        assertTrue(servicio.auditoriaVerificable());
    }

    @Test
    void cancelarExigeMotivo() {
        CitaDTO creada = agendarDirecta(INICIO);

        assertThrows(IllegalArgumentException.class, () -> servicio.cancelar(creada.citaId(), null));
        assertThrows(IllegalArgumentException.class, () -> servicio.cancelar(creada.citaId(), "   "));
    }

    @Test
    void cancelarRespetaPlazoDeCancelacionDeCatalogo() {
        // Rejo del test: 24 h de preaviso con reloj fijo. El limite de esta cita es el
        // 2026-11-10T09:00 y el reloj del contexto marca 2026-11-11T08:00Z: llega tarde.
        CitaDTO dto = servicio.agendar(new CitaNuevaDTO(SOL_CREADA, PROF_MEDICO,
                LocalDateTime.parse("2026-11-11T09:00:00"), DURACION));

        CitaConflictoException e = assertThrows(CitaConflictoException.class,
                () -> servicio.cancelar(dto.citaId(), "tarde"));
        assertEquals(Conflicto.PLAZO_CANCELACION, e.tipo());

        // La transaccion ya no se escribe: el hueco sigue bloqueado y la solicitud citada.
        // No se comprueba el estado de la cita: la entidad gestionada queda sucia tras la
        // excepcion y cualquier consulta volcaria el cambio en memoria.
        assertTrue(huecos.findByCitaId(dto.citaId()).isPresent());
        assertEquals("citada", solicitudes.estado(SOL_CREADA));
    }

    @Test
    void cancelarCitaDirectaNoExigePlazo() {
        // Sin solicitud no hay prueba catalogada ni politica que exigir, aunque la cita
        // este dentro de la ventana de las 24 h.
        CitaDTO dto = agendarDirecta(LocalDateTime.parse("2026-11-11T09:00:00"));

        CitaDTO cancelada = servicio.cancelar(dto.citaId(), "lluvia");

        assertEquals("cancelada", cancelada.estado());
    }

    @Test
    void transicionInvalidaGanaAlPlazoDeCancelacion() {
        // D36: en cancelar, la maquina de estados decide antes que el plazo. La cita esta
        // en plazo de veto y ademas es terminal: gana TRANSICION_INVALIDA.
        CitaDTO dto = servicio.agendar(new CitaNuevaDTO(SOL_CREADA, PROF_MEDICO,
                LocalDateTime.parse("2026-11-11T09:00:00"), DURACION));
        servicio.realizar(dto.citaId());

        CitaConflictoException e = assertThrows(CitaConflictoException.class,
                () -> servicio.cancelar(dto.citaId(), "ya realizada"));
        assertEquals(Conflicto.TRANSICION_INVALIDA, e.tipo());
    }

    // --- Confirmar y realizar -------------------------------------------------

    @Test
    void confirmarYRealizarDejanHuecoLibreYSolicitudCitada() {
        CitaDTO creada = servicio.agendar(new CitaNuevaDTO(SOL_CREADA, PROF_MEDICO, INICIO, DURACION));

        assertEquals("confirmada", servicio.confirmar(creada.citaId()).estado());
        assertTrue(huecos.findByCitaId(creada.citaId()).isPresent());

        assertEquals("realizada", servicio.realizar(creada.citaId()).estado());
        assertFalse(huecos.findByCitaId(creada.citaId()).isPresent());
        assertEquals("citada", solicitudes.estado(SOL_CREADA));

        // Terminal: ya no se puede cancelar ni reprogramar.
        CitaConflictoException e = assertThrows(CitaConflictoException.class,
                () -> servicio.cancelar(creada.citaId(), "tardia"));
        assertEquals(Conflicto.TRANSICION_INVALIDA, e.tipo());
        assertTrue(servicio.auditoriaVerificable());
    }

    // --- Reprogramar ----------------------------------------------------------

    @Test
    void reprogramarMueveLaMismaFilaDeHuecoYSubeSuVersion() {
        CitaDTO creada = agendarDirecta(INICIO);
        assertEquals(0, servicio.detalle(creada.citaId()).version());

        CitaDTO movida = servicio.reprogramar(creada.citaId(), INICIO_TARDE);

        assertEquals("programada", movida.estado());
        assertEquals(INICIO_TARDE, movida.inicio());
        assertEquals(1, huecos.count());
        CitaHueco hueco = huecos.findByCitaId(creada.citaId()).orElseThrow();
        assertEquals(INICIO_TARDE, hueco.getInicio());
        assertEquals(1, hueco.getVersion());
        assertEquals(1, servicio.detalle(creada.citaId()).version());
        assertThrows(IllegalArgumentException.class, () -> servicio.reprogramar(creada.citaId(), null));
    }

    @Test
    void reprogramarPermiteDestinosQueSolapanSuPropioHueco() {
        // El hueco propio no puede bloquear a la cita: si no, moverse una media hora
        // sobre si misma seria imposible.
        CitaDTO creada = agendarDirecta(INICIO);

        CitaDTO movida = servicio.reprogramar(creada.citaId(), INICIO.plusMinutes(15));

        assertEquals(LocalDateTime.parse("2026-11-13T09:15:00"), movida.inicio());
        assertEquals(1, huecos.count());
    }

    @Test
    void reprogramarDestinoOcupadoDejaElHuecoAnterior() {
        CitaDTO primera = agendarDirecta(INICIO);
        agendarDirecta(INICIO_TARDE);

        CitaConflictoException e = assertThrows(CitaConflictoException.class,
                () -> servicio.reprogramar(primera.citaId(), INICIO_TARDE));
        assertEquals(Conflicto.HUECO_OCUPADO, e.tipo());

        // Solo se comprueba la fila del hueco: tras la excepcion la cita gestionada queda
        // sucia en memoria y cualquier lectura volcaria ese cambio.
        assertEquals(INICIO, huecos.findByCitaId(primera.citaId()).orElseThrow().getInicio());
        assertEquals(2, huecos.count());
    }

    @Test
    void reprogramarRechazaFueraDeFranjaFestivoYEstadoTerminal() {
        CitaDTO creada = agendarDirecta(INICIO);

        CitaConflictoException foreira = assertThrows(CitaConflictoException.class,
                () -> servicio.reprogramar(creada.citaId(), INICIO_FOREIRA));
        assertEquals(Conflicto.FUERA_DE_FRANJA, foreira.tipo());

        CitaConflictoException festivo = assertThrows(CitaConflictoException.class,
                () -> servicio.reprogramar(creada.citaId(), INICIO_FESTIVO));
        assertEquals(Conflicto.FUERA_DE_FRANJA, festivo.tipo());

        servicio.realizar(creada.citaId());
        CitaConflictoException terminal = assertThrows(CitaConflictoException.class,
                () -> servicio.reprogramar(creada.citaId(), INICIO_TARDE));
        assertEquals(Conflicto.TRANSICION_INVALIDA, terminal.tipo());
    }

    // --- Lote de expiracion -----------------------------------------------------

    @Test
    void expirarVencidasRespetaElLimiteDelCorteYLiberaTodo() {
        CitaDTO creada = servicio.agendar(new CitaNuevaDTO(SOL_CREADA, PROF_MEDICO, INICIO, DURACION));
        LocalDateTime fin = INICIO.plusMinutes(DURACION);

        // Cita que termina justo en el corte: aun no esta vencida (limite inclusivo).
        assertTrue(servicio.expirarVencidas(fin).isEmpty());

        List<CitaDTO> vencidas = servicio.expirarVencidas(fin.plusMinutes(1));

        assertEquals(1, vencidas.size());
        assertEquals("expirada", vencidas.get(0).estado());
        assertFalse(huecos.findByCitaId(creada.citaId()).isPresent());
        assertEquals("creada", solicitudes.estado(SOL_CREADA));
    }

    @Test
    void expirarVencidasSinCorteUsaElRelojFijoDelContexto() {
        Cita antigua = Cita.nueva(null, PROF_MEDICO, LocalDateTime.parse("2026-11-09T09:00:00"), DURACION);
        citas.save(antigua);

        List<CitaDTO> vencidas = servicio.expirarVencidas(null);

        assertEquals(1, vencidas.size());
        assertEquals(antigua.getCitaId(), vencidas.get(0).citaId());
        assertEquals("expirada", vencidas.get(0).estado());
    }

    @Test
    void expirarEsToleranteConSolicitudesYaCerradas() {
        // La cita nacio sobre una solicitud prescrita (guardada directa: por API esa
        // combinacion no se puede crear). Al expirar, CDU06 se niega a reabrirla y aun
        // asi el lote debe cerrar la cita y su auditoria sin romper nada.
        Cita sobrePrescrita = Cita.nueva(SOL_PRESCRITA, PROF_MEDICO,
                LocalDateTime.parse("2026-11-09T09:00:00"), DURACION);
        citas.save(sobrePrescrita);

        assertEquals(1, servicio.expirarVencidas(null).size());

        assertEquals("prescrita", solicitudes.estado(SOL_PRESCRITA));
        assertEquals(AccionAuditoria.EXPIRACION, auditoria.findAllByOrderByIdAsc().get(0).getAccion());
        assertTrue(servicio.auditoriaVerificable());
    }

    // --- Motor de huecos libres ---------------------------------------------------

    @Test
    void huecosLibresDescuentanLosHuecosBloqueados() {
        // Bloqueo activo de 09:00 a 09:30 dentro de la jornada de 08:00 a 15:00.
        agendarDirecta(INICIO);

        List<HuecoDTO> libres = servicio.huecosLibres(PROF_MEDICO, DIA, DIA, null);

        assertEquals(2, libres.size());
        assertEquals(LocalDateTime.parse("2026-11-13T08:00:00"), libres.get(0).inicio());
        assertEquals(INICIO, libres.get(0).fin());
        assertEquals(INICIO.plusMinutes(DURACION), libres.get(1).inicio());
        assertEquals(LocalDateTime.parse("2026-11-13T15:00:00"), libres.get(1).fin());
        assertTrue(libres.stream().allMatch(hueco -> hueco.duracionMinutos() == null));
    }

    @Test
    void huecosLibresSeAcretanSiSePideDuracion() {
        agendarDirecta(INICIO);

        List<HuecoDTO> libres = servicio.huecosLibres(PROF_MEDICO, DIA, DIA, 240);

        // Solo el hueco de la tarde (330 min) admite una prueba de 4 h.
        assertEquals(1, libres.size());
        assertEquals(INICIO.plusMinutes(DURACION), libres.get(0).inicio());
        assertEquals(330, libres.get(0).duracionMinutos());
    }

    @Test
    void huecosLibresExcluyenFestivosYExigenAgenda() {
        assertTrue(servicio.huecosLibres(PROF_MEDICO, FESTIVO, FESTIVO, null).isEmpty());

        CitaConflictoException e = assertThrows(CitaConflictoException.class,
                () -> servicio.huecosLibres(PROF_SIN_AGENDA, DIA, DIA, null));
        assertEquals(Conflicto.FUERA_DE_FRANJA, e.tipo());
    }

    @Test
    void huecosLibresValidanLaEntrada() {
        assertThrows(IllegalArgumentException.class, () -> servicio.huecosLibres(" ", DIA, DIA, null));
        assertThrows(IllegalArgumentException.class, () -> servicio.huecosLibres(PROF_MEDICO, null, DIA, null));
        assertThrows(IllegalArgumentException.class, () -> servicio.huecosLibres(PROF_MEDICO, DIA, FESTIVO, null));
        assertThrows(IllegalArgumentException.class, () -> servicio.huecosLibres(PROF_MEDICO, DIA, DIA, 0));
    }

    // --- Consultas ---------------------------------------------------------------

    @Test
    void listarFiltraPorVentanaProfesionalYEstado() {
        agendarDirecta(INICIO);
        agendarDirecta(INICIO_TARDE);

        LocalDateTime desde = LocalDateTime.parse("2026-11-12T00:00:00");
        LocalDateTime hasta = LocalDateTime.parse("2026-11-14T00:00:00");

        assertEquals(2, servicio.listar(desde, hasta, PROF_MEDICO, "programada").size());
        assertEquals(2, servicio.listar(desde, hasta, null, null).size());
        assertEquals(0, servicio.listar(desde, hasta, PROF_MEDICO, "confirmada").size());
        // El estado admite mayusculas: es la maquina, no el literal de la peticion.
        assertEquals(2, servicio.listar(desde, hasta, null, "PROGRAMADA").size());
    }

    @Test
    void listarRechazaRangoIncoherenteYEstadoDesconocido() {
        assertThrows(IllegalArgumentException.class, () -> servicio.listar(INICIO_TARDE, INICIO, null, null));
        assertThrows(IllegalArgumentException.class, () -> servicio.listar(null, null, null, "terminada"));
    }

    @Test
    void detalleDevuelveLaVistaCompletaDEspera() {
        CitaDTO creada = servicio.agendar(new CitaNuevaDTO(SOL_CREADA, PROF_MEDICO, INICIO, DURACION));

        DetalleCitaDTO detalle = servicio.detalle(creada.citaId());

        assertEquals(creada.citaId(), detalle.citaId());
        assertEquals(SOL_CREADA, detalle.solicitudId());
        assertEquals(PROF_MEDICO, detalle.profesionalId());
        assertEquals("programada", detalle.estado());
        assertNull(detalle.motivoCancelacion());
        assertEquals(0, detalle.version());
    }

    @Test
    void cualquierOperacionSobreCitaInexistenteEsCuatroCuatroCuatro() {
        assertThrows(CitaNoEncontradaException.class, () -> servicio.detalle(null));
        assertThrows(CitaNoEncontradaException.class, () -> servicio.detalle("   "));
        assertThrows(CitaNoEncontradaException.class, () -> servicio.detalle("id-que-no-existe"));
        assertThrows(CitaNoEncontradaException.class, () -> servicio.confirmar("id-que-no-existe"));
        assertThrows(CitaNoEncontradaException.class, () -> servicio.realizar("id-que-no-existe"));
        assertThrows(CitaNoEncontradaException.class,
                () -> servicio.reprogramar("id-que-no-existe", INICIO_TARDE));
        assertThrows(CitaNoEncontradaException.class, () -> servicio.cancelar("id-que-no-existe", "motivo"));
    }

    // --- Auditoria encadenada ------------------------------------------------------

    @Test
    void auditoriaEncadenaRegistrosConElUsuarioAutenticado() {
        try {
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken("recepcion1", null));
            CitaDTO creada = agendarDirecta(INICIO);
            servicio.cancelar(creada.citaId(), "el paciente no puede acudir");
        } finally {
            SecurityContextHolder.clearContext();
        }

        List<AuditoriaCitas> registros = auditoria.findAllByOrderByIdAsc();
        assertEquals(2, registros.size());
        assertNull(registros.get(0).getHashAnterior());
        assertEquals(registros.get(0).getHash(), registros.get(1).getHashAnterior());
        assertEquals("recepcion1", registros.get(0).getUsuario());
        assertEquals(PruebasCitas.AHORA, registros.get(1).getInstante());
        assertTrue(servicio.auditoriaVerificable());

        // Alguien altero un registro: la cadena deja de verificar.
        registros.get(0).setHashParaPruebas("hash-alterado");
        auditoria.saveAndFlush(registros.get(0));
        assertFalse(servicio.auditoriaVerificable());
    }

    @Test
    void constructorPublicoUsaRelojRealYFunciona() {
        CitaServicio conRelojReal = new CitaServicio(citas, huecos, auditoria, agendas, catalogo,
                fachadaSolicitudes);

        CitaDTO dto = conRelojReal.agendar(new CitaNuevaDTO(SOL_CREADA, PROF_MEDICO, INICIO, DURACION));

        assertEquals("programada", dto.estado());
        assertEquals("citada", solicitudes.estado(SOL_CREADA));
    }
}
