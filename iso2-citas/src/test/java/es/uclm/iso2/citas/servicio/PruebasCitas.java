package es.uclm.iso2.citas.servicio;

import es.uclm.iso2.common.agendas.AgendaFacade;
import es.uclm.iso2.common.agendas.AgendaFacade.AgendaDTO;
import es.uclm.iso2.common.agendas.AgendaFacade.FranjaDTO;
import es.uclm.iso2.common.agendas.AgendaFacade.FranjaGestionDTO;
import es.uclm.iso2.common.agendas.AgendaFacade.FestivoDTO;
import es.uclm.iso2.common.agendas.AgendaNoEncontradoException;
import es.uclm.iso2.common.catalogo.PruebaCatalogoFacade;
import es.uclm.iso2.common.catalogo.PruebaCatalogoFacade.PoliticaDTO;
import es.uclm.iso2.common.catalogo.PruebaCatalogoFacade.PruebaImagenDTO;
import es.uclm.iso2.common.catalogo.PruebaCatalogoFacade.ProtocoloDTO;
import es.uclm.iso2.common.shared.Paginacion;
import es.uclm.iso2.common.solicitudes.SolicitudConflictoException;
import es.uclm.iso2.common.solicitudes.SolicitudFacade;
import es.uclm.iso2.common.solicitudes.SolicitudFacade.SolicitudDTO;
import es.uclm.iso2.common.solicitudes.SolicitudFacade.SolicitudNuevaDTO;
import es.uclm.iso2.common.solicitudes.SolicitudFacade.SolicitudPrescripcionDTO;
import es.uclm.iso2.common.solicitudes.SolicitudFacade.TrazabilidadSolicitudDTO;
import es.uclm.iso2.common.solicitudes.SolicitudNoEncontradaException;
import es.uclm.iso2.citas.repositorio.AuditoriaCitasRepositorio;
import es.uclm.iso2.citas.repositorio.CitaHuecoRepositorio;
import es.uclm.iso2.citas.repositorio.CitaRepositorio;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Montura de pruebas del CDU12: el servicio real bajo prueba con reloj fijo y las fachadas
 * de los modulos con los que habla (CDU13, CDU16 y CDU06) sustituidas por dobles de prueba,
 * porque los modulos de dominio solo se conocen a traves del contrato comun.
 *
 * <p>El reloj se fija en el 11/11/2026 a las 08:00 UTC para que los plazos de cancelacion y
 * el lote de vencidas no dependan del instante real de la ejecucion. Las agendas falsas dan
 * franja de 08:00 a 15:00 todos los dias menos el festivo 2026-11-10, igual que el seed de
 * CDU13, y solo existen para el profesional {@link #PROF_MEDICO}. Las solicitudes falsas
 * guardan estado en memoria para que el CDU12 pueda marcarlas citadas y reabrirlas; los
 * identificadores son {@code api-*} para no chocar con el seed.</p>
 */
@TestConfiguration
public class PruebasCitas {

    /** Profesional con agenda de manana todos los dias salvo festivo. */
    public static final String PROF_MEDICO = "api-medico";

    /** Profesional sin agenda: cualquier hueco pedido queda fuera de franja. */
    public static final String PROF_SIN_AGENDA = "api-sin-agenda";

    /** Dia con franja (de la 08:00 a la 15:00). */
    public static final LocalDate DIA = LocalDate.parse("2026-11-13");

    /** Festivo de CDU13: ese dia no hay ninguna franja libre. */
    public static final LocalDate FESTIVO = LocalDate.parse("2026-11-10");

    /** Inicio de cita bien metido en la franja. */
    public static final LocalDateTime INICIO = LocalDateTime.parse("2026-11-13T09:00:00");

    /** Inicio de cita a ultima hora de la franja, sin solape con {@link #INICIO}. */
    public static final LocalDateTime INICIO_TARDE = LocalDateTime.parse("2026-11-13T13:00:00");

    /** Inicio de cita antes de la apertura de la franja. */
    public static final LocalDateTime INICIO_FOREIRA = LocalDateTime.parse("2026-11-13T07:00:00");

    /** Inicio de cita en el dia festivo. */
    public static final LocalDateTime INICIO_FESTIVO = LocalDateTime.parse("2026-11-10T09:00:00");

    /** Codigo de prueba del catalogo falso: duracion 30 min y plazo de cancelacion de 24 h. */
    public static final String PRUEBA = "api-prueba";

    /** Solicitud falsa en estado creada, apta para cita. */
    public static final String SOL_CREADA = "api-sol-creada";

    /** Solicitud falsa ya citada. */
    public static final String SOL_CITADA = "api-sol-citada";

    /** Solicitud falsa en estado prescrita: no admite cita. */
    public static final String SOL_PRESCRITA = "api-sol-prescrita";

    /** Solicitud inexistente. */
    public static final String SOL_INEXISTENTE = "api-sol-que-no-existe";

    /** Instante fijo del reloj de pruebas. */
    public static final Instant AHORA = Instant.parse("2026-11-11T08:00:00Z");

    /** Duracion de cita en minutos usada en las pruebas. */
    public static final int DURACION = 30;

    /**
     * Servicio real bajo prueba, con repositorios JPA reales y reloj fijo para plazos y
     * vencidas.
     *
     * @param citas     repositorio JPA de citas
     * @param huecos    repositorio JPA de huecos ocupados
     * @param auditoria repositorio JPA de auditoria encadenada
     * @param agendas   fachada de agendas falsa
     * @param catalogo  fachada de catalogo falsa
     * @param solicitudes fachada de solicitudes falsa
     * @return servicio de citas de prueba
     */
    @Bean
    public CitaServicio citaServicio(CitaRepositorio citas, CitaHuecoRepositorio huecos,
            AuditoriaCitasRepositorio auditoria, AgendaFacade agendas, PruebaCatalogoFacade catalogo,
            SolicitudFacade solicitudes) {
        return new CitaServicio(citas, huecos, auditoria, agendas, catalogo, solicitudes,
                Clock.fixed(AHORA, ZoneOffset.UTC));
    }

    /**
     * Agendas falsas: franja de 08:00 a 15:00 para {@link #PROF_MEDICO} menos el festivo.
     *
     * @return fachada de CDU13 de prueba
     */
    @Bean
    public AgendaFacade agendaFacade() {
        return new AgendasFalsas();
    }

    /**
     * Catalogo falso con una unica prueba de 30 minutos y plazo de cancelacion de 24 h.
     *
     * @return fachada de CDU05 de prueba
     */
    @Bean
    public PruebaCatalogoFacade pruebaCatalogoFacade() {
        return new CatalogoFalso();
    }

    /**
     * Solicitudes falsas con estado en memoria.
     *
     * @return fachada de CDU06 de prueba
     */
    @Bean
    public SolicitudFacade solicitudFacade() {
        return new SolicitudesFalsas();
    }

    /** Agendas de prueba: franja fija y un unico festivo. */
    static class AgendasFalsas implements AgendaFacade {

        @Override
        public List<FranjaDTO> franjasLibres(String profesionalId, LocalDate desde, LocalDate hasta) {
            if (!PROF_MEDICO.equals(profesionalId)) {
                throw new AgendaNoEncontradoException("no hay agenda para " + profesionalId);
            }
            List<FranjaDTO> libres = new ArrayList<>();
            for (LocalDate dia = desde; !dia.isAfter(hasta); dia = dia.plusDays(1)) {
                if (!FESTIVO.equals(dia)) {
                    libres.add(new FranjaDTO(profesionalId, dia, LocalTime.of(8, 0), LocalTime.of(15, 0)));
                }
            }
            return libres;
        }

        @Override
        public List<AgendaDTO> listarAgendas() {
            return List.of(buscarPorProfesional(PROF_MEDICO).orElseThrow());
        }

        @Override
        public Optional<AgendaDTO> buscarPorProfesional(String profesionalId) {
            if (!PROF_MEDICO.equals(profesionalId)) {
                return Optional.empty();
            }
            return Optional.of(new AgendaDTO(profesionalId,
                    List.of(new FranjaGestionDTO(1L, DIA, LocalTime.of(8, 0), LocalTime.of(15, 0))),
                    List.of(new FestivoDTO(1L, FESTIVO, "Festivo de prueba"))));
        }

        @Override
        public AgendaDTO crearAgenda(String profesionalId) {
            throw new UnsupportedOperationException("fuera del alcance del CDU12");
        }

        @Override
        public FranjaGestionDTO anadirFranja(String profesionalId, FranjaGestionDTO franja) {
            throw new UnsupportedOperationException("fuera del alcance del CDU12");
        }

        @Override
        public void eliminarFranja(String profesionalId, Long franjaId) {
            throw new UnsupportedOperationException("fuera del alcance del CDU12");
        }

        @Override
        public FestivoDTO anadirFestivo(String profesionalId, FestivoDTO festivo) {
            throw new UnsupportedOperationException("fuera del alcance del CDU12");
        }

        @Override
        public void eliminarFestivo(String profesionalId, Long festivoId) {
            throw new UnsupportedOperationException("fuera del alcance del CDU12");
        }
    }

    /** Catalogo de prueba con una sola imagen de 30 minutos. */
    static class CatalogoFalso implements PruebaCatalogoFacade {

        @Override
        public Optional<PruebaImagenDTO> buscarPorCodigo(String codigo) {
            if (!PRUEBA.equals(codigo)) {
                return Optional.empty();
            }
            return Optional.of(new PruebaImagenDTO(codigo, "Radiografia de prueba", "RX", true, DURACION, true,
                    new ProtocoloDTO("Preparacion standard", 0, "ninguna"), new PoliticaDTO(24, 3)));
        }

        @Override
        public List<PruebaImagenDTO> listar(Paginacion paginacion) {
            return List.of(buscarPorCodigo(PRUEBA).orElseThrow());
        }

        @Override
        public PruebaImagenDTO crear(PruebaImagenDTO prueba) {
            throw new UnsupportedOperationException("fuera del alcance del CDU12");
        }

        @Override
        public PruebaImagenDTO actualizar(String codigo, PruebaImagenDTO prueba) {
            throw new UnsupportedOperationException("fuera del alcance del CDU12");
        }

        @Override
        public PruebaImagenDTO desactivar(String codigo) {
            throw new UnsupportedOperationException("fuera del alcance del CDU12");
        }

        @Override
        public PruebaImagenDTO reactivar(String codigo) {
            throw new UnsupportedOperationException("fuera del alcance del CDU12");
        }
    }

    /** Solicitudes de prueba con estado en memoria y fallos provocables. */
    static class SolicitudesFalsas implements SolicitudFacade {

        private final Map<String, SolicitudDTO> datos = new HashMap<>();

        /** Cuando es true, reabrir la solicitud falla: simula un fallo tras reservar el hueco. */
        boolean fallaDevolverACreada;

        SolicitudesFalsas() {
            reiniciar();
        }

        /** Vuelve al estado inicial de las solicitudes de prueba. */
        final void reiniciar() {
            datos.clear();
            fallaDevolverACreada = false;
            datos.put(SOL_CREADA, nueva(SOL_CREADA, "creada"));
            datos.put(SOL_CITADA, nueva(SOL_CITADA, "citada"));
            datos.put(SOL_PRESCRITA, nueva(SOL_PRESCRITA, "prescrita"));
        }

        private static SolicitudDTO nueva(String id, String estado) {
            return new SolicitudDTO(id, "api-paciente", PRUEBA, LocalDate.parse("2026-11-12"), estado);
        }

        /** Estado actual de una solicitud, o null si no existe. */
        String estado(String solicitudId) {
            SolicitudDTO dto = datos.get(solicitudId);
            return dto == null ? null : dto.estado();
        }

        @Override
        public Optional<SolicitudDTO> buscar(String solicitudId) {
            return Optional.ofNullable(datos.get(solicitudId));
        }

        @Override
        public SolicitudDTO marcarCitada(String solicitudId) {
            SolicitudDTO dto = datos.get(solicitudId);
            if (dto == null) {
                throw new SolicitudNoEncontradaException(solicitudId);
            }
            if (!"creada".equalsIgnoreCase(dto.estado())) {
                throw new SolicitudConflictoException(
                        SolicitudConflictoException.Conflicto.NO_APTA_PARA_CITA,
                        "la solicitud " + solicitudId + " no esta en creada");
            }
            SolicitudDTO cambiada = nueva(solicitudId, "citada");
            datos.put(solicitudId, cambiada);
            return cambiada;
        }

        @Override
        public SolicitudDTO devolverACreada(String solicitudId) {
            if (fallaDevolverACreada) {
                throw new IllegalStateException("fallo simulado al reabrir la solicitud");
            }
            SolicitudDTO dto = datos.get(solicitudId);
            if (dto == null) {
                throw new SolicitudNoEncontradaException(solicitudId);
            }
            if (!"citada".equalsIgnoreCase(dto.estado())) {
                throw new SolicitudConflictoException(
                        SolicitudConflictoException.Conflicto.NO_APTA_PARA_CITA,
                        "la solicitud " + solicitudId + " no esta citada");
            }
            SolicitudDTO cambiada = nueva(solicitudId, "creada");
            datos.put(solicitudId, cambiada);
            return cambiada;
        }

        @Override
        public SolicitudDTO crear(SolicitudNuevaDTO solicitud) {
            throw new UnsupportedOperationException("fuera del alcance del CDU12");
        }

        @Override
        public SolicitudDTO prescribir(SolicitudPrescripcionDTO prescripcion) {
            throw new UnsupportedOperationException("fuera del alcance del CDU12");
        }

        @Override
        public List<SolicitudDTO> listarPorMedico(String medicoId) {
            throw new UnsupportedOperationException("fuera del alcance del CDU12");
        }

        @Override
        public SolicitudDTO detalle(String solicitudId) {
            throw new UnsupportedOperationException("fuera del alcance del CDU12");
        }

        @Override
        public TrazabilidadSolicitudDTO trazable(String solicitudId) {
            throw new UnsupportedOperationException("fuera del alcance del CDU12");
        }
    }
}
