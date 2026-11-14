package es.uclm.iso2.citas.servicio;

import es.uclm.iso2.citas.dominio.AuditoriaCitas;
import es.uclm.iso2.citas.dominio.Cita;
import es.uclm.iso2.citas.dominio.CitaHueco;
import es.uclm.iso2.citas.repositorio.AuditoriaCitasRepositorio;
import es.uclm.iso2.citas.repositorio.CitaHuecoRepositorio;
import es.uclm.iso2.citas.repositorio.CitaRepositorio;
import es.uclm.iso2.common.citas.CitaConflictoException;
import es.uclm.iso2.common.citas.CitaConflictoException.Conflicto;
import es.uclm.iso2.common.citas.CitaFacade.CitaDTO;
import es.uclm.iso2.common.citas.CitaFacade.CitaNuevaDTO;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static es.uclm.iso2.citas.servicio.PruebasCitas.DURACION;
import static es.uclm.iso2.citas.servicio.PruebasCitas.INICIO;
import static es.uclm.iso2.citas.servicio.PruebasCitas.PROF_MEDICO;
import static es.uclm.iso2.citas.servicio.PruebasCitas.SOL_CREADA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Concurrencia real del CDU12 (matriz de calidad de CFG-05 y D36): dos recepcionistas
 * reservando el mismo hueco a la vez. Aqui las transacciones del servicio SI se confirman
 * o se deshacen de verdad: la clase se ejecuta sin transaccion de prueba para que H2 vea
 * las dos sesiones como dos usuarios distintos, que es justo lo que la restriccion unica
 * {@code (profesional_id, inicio)} de la tabla de huecos tiene que resolver.
 */
@DataJpaTest
@Import(PruebasCitas.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class ConcurrenciaCitasTest {

    @Autowired
    CitaServicio servicio;

    @Autowired
    CitaRepositorio citas;

    @Autowired
    CitaHuecoRepositorio huecos;

    @Autowired
    AuditoriaCitasRepositorio auditoria;

    /** Fachada falsa para comprobar el estado final de la solicitud. */
    @Autowired
    PruebasCitas.SolicitudesFalsas solicitudes;

    @BeforeEach
    void limpiarTablas() {
        // Sin transaccion de prueba: cada repositorio confirma su propio envia. Al ir
        // compartiendo contexto con las demas clases, hay que partir de cero siempre.
        citas.deleteAll();
        huecos.deleteAll();
        auditoria.deleteAll();
        solicitudes.reiniciar();
    }

    private static CitaNuevaDTO duelo() {
        return new CitaNuevaDTO(SOL_CREADA, PROF_MEDICO, INICIO, DURACION);
    }

    @Test
    void dosHilosReservanElMismoHuecoYGanaUnoSolo() throws InterruptedException {
        // Protocolo de duelo: los dos hilos se ponen en marcha, avisan de que estan listos
        // (lista) y esperan en la linea de salida (salida, latch de a uno). El test suelta
        // la salida cuando los dos estan esperando: ahi SI compiten de verdad.
        CountDownLatch lista = new CountDownLatch(2);
        CountDownLatch salida = new CountDownLatch(1);
        AtomicReference<CitaDTO> ganadora = new AtomicReference<>();
        AtomicReference<Throwable> perdedora = new AtomicReference<>();

        Runnable asaltante = () -> {
            try {
                lista.countDown();
                if (!salida.await(15, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("el duelo no arranco a tiempo");
                }
                CitaDTO dto = servicio.agendar(duelo());
                ganadora.compareAndSet(null, dto);
            } catch (Throwable e) {
                perdedora.compareAndSet(null, e);
            }
        };

        Thread uno = new Thread(asaltante, "asaltante-1");
        Thread dos = new Thread(asaltante, "asaltante-2");
        uno.start();
        dos.start();
        assertTrue(lista.await(15, TimeUnit.SECONDS), "los dos hilos no llegaron a la salida");
        salida.countDown();
        uno.join(30_000);
        dos.join(30_000);
        assertFalse(uno.isAlive() || dos.isAlive(), "un hilo se quedo colgado");

        // Exactamente una reserva triunfa: la garantia que pide la matriz de CFG-05.
        assertNotNull(ganadora.get(), "algun hilo tenia que conseguir el hueco");
        assertNotNull(perdedora.get(), "el otro hilo tenia que quedarse sin hueco");

        Throwable fallo = perdedora.get();
        boolean precomprobacion = fallo instanceof CitaConflictoException c
                && c.tipo() == Conflicto.HUECO_OCUPADO;
        boolean baseDeDatos = fallo instanceof DataIntegrityViolationException
                || fallo instanceof ObjectOptimisticLockingFailureException
                || fallo instanceof CannotAcquireLockException;
        assertTrue(precomprobacion || baseDeDatos,
                "fallo inesperado del perdedor: " + fallo);

        // Invariantes despues del duelo: un hueco, una cita y una sola linea de auditoria.
        assertEquals(1, citas.count());
        assertEquals(1, huecos.count());
        assertEquals(1, auditoria.count());
        CitaHueco ocupado = huecos.findByProfesionalIdAndInicio(PROF_MEDICO, INICIO).orElseThrow();
        assertEquals(ganadora.get().citaId(), ocupado.getCitaId());
        assertTrue(servicio.auditoriaVerificable());
        assertEquals("citada", solicitudes.estado(SOL_CREADA));
    }

    @Test
    void huecoConfirmadoAntesDelDueloBloqueaAMbosPorPrecomprobacion() {
        // Escenario determinista equivalente: si el hueco ya estaba confirmado en la base
        // de datos, los dos competidores lo ven en la comprobacion previa y ninguno escribe.
        huecos.save(new CitaHueco("cita-de-otro", PROF_MEDICO, INICIO, INICIO.plusMinutes(DURACION)));

        for (int turno = 0; turno < 2; turno++) {
            CitaConflictoException e = assertThrows(CitaConflictoException.class,
                    () -> servicio.agendar(duelo()));
            assertEquals(Conflicto.HUECO_OCUPADO, e.tipo());
        }
        assertEquals(0, citas.count());
        assertEquals(1, huecos.count());
        assertEquals(0, auditoria.count());
    }

    @Test
    void dueloPorVersionNoPisaLaFilaDelHueco() {
        // Dos reprogramaciones leen la misma fila de hueco (misma @Version) y quieren
        // moverla: la segunda escritura detecta que la version cambio y revienta. Cada
        // lectura y cada escritura corren en su propia transaccion (estamos fuera de la
        // de prueba), asi que los dos objetos llegan desenganchados, como en dos sesiones
        // de usuario distintas.
        CitaDTO creada = servicio.agendar(new CitaNuevaDTO(null, PROF_MEDICO, INICIO, DURACION));
        CitaHueco lectorA = huecos.findByCitaId(creada.citaId()).orElseThrow();
        CitaHueco lectorB = huecos.findByCitaId(creada.citaId()).orElseThrow();
        assertNotSame(lectorA, lectorB, "hacen falta dos copias de la misma fila");
        assertEquals(0, lectorA.getVersion());
        assertEquals(0, lectorB.getVersion());

        lectorA.moverA(INICIO.plusMinutes(15), INICIO.plusMinutes(DURACION + 15));
        huecos.saveAndFlush(lectorA); // gana A: la version de la fila sube a 1

        lectorB.moverA(INICIO.plusMinutes(30), INICIO.plusMinutes(DURACION + 30));
        assertThrows(ObjectOptimisticLockingFailureException.class,
                () -> huecos.saveAndFlush(lectorB));

        // La fila sobrevive con el movimiento del ganador: la perdedora no la pisoteo.
        CitaHueco viva = huecos.findByCitaId(creada.citaId()).orElseThrow();
        assertEquals(INICIO.plusMinutes(15), viva.getInicio());
        assertEquals(1, viva.getVersion());
        assertEquals(1, huecos.count());
    }

    @Test
    void falloAlReabrirLaSolicitudDeshaceTodaLaCancelacion() {
        CitaDTO creada = servicio.agendar(new CitaNuevaDTO(SOL_CREADA, PROF_MEDICO, INICIO, DURACION));
        solicitudes.fallaDevolverACreada = true;

        // El fallo simulado llega despues de borrar el hueco y auditar la cancelacion.
        // Como revienta crudo (sin envolver), el proxy del servicio deshace la transaccion
        // entera: nada de lo que hizo el cancelar debe quedar escrito.
        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> servicio.cancelar(creada.citaId(), "fallo de ejemplo"));
        assertEquals("fallo simulado al reabrir la solicitud", e.getMessage());

        assertEquals(Cita.Estado.PROGRAMADA,
                citas.findByCitaId(creada.citaId()).orElseThrow().getEstado());
        assertTrue(huecos.findByCitaId(creada.citaId()).isPresent());
        List<AuditoriaCitas> registros = auditoria.findAllByOrderByIdAsc();
        assertEquals(1, registros.size());
        assertTrue(servicio.auditoriaVerificable());
        assertEquals("citada", solicitudes.estado(SOL_CREADA));
    }

    @Test
    void expirarVencidasSinHuecosNiSolicitudesNoRompeNada() {
        CitaHueco huesoSuelto = new CitaHueco("cita-vieja-que-no-existe", PROF_MEDICO,
                LocalDateTime.parse("2026-11-09T09:00:00"), LocalDateTime.parse("2026-11-09T09:30:00"));
        huecos.save(huesoSuelto);

        assertEquals(0, servicio.expirarVencidas(null).size());
    }
}
