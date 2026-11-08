package es.uclm.iso2.server;

import es.uclm.iso2.client.api.ApiClienteAgendas;
import es.uclm.iso2.client.api.ApiClienteAutenticacion;
import es.uclm.iso2.client.api.ApiException;
import es.uclm.iso2.common.agendas.AgendaFacade.AgendaDTO;
import es.uclm.iso2.common.agendas.AgendaFacade.FestivoDTO;
import es.uclm.iso2.common.agendas.AgendaFacade.FranjaDTO;
import es.uclm.iso2.common.agendas.AgendaFacade.FranjaGestionDTO;
import es.uclm.iso2.common.seguridad.AutenticacionFacade.SesionDTO;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test de integracion cliente-servidor de las agendas (CDU13, puertas "TC
 * solapes/festivos" y "frontera" de CFG-03): la aplicacion servidora real escucha en
 * un puerto efimero ({@code webEnvironment = RANDOM_PORT}) y la consumen los
 * clientes HTTP reales de {@code iso2-client-app} ({@link ApiClienteAutenticacion} y
 * {@link ApiClienteAgendas}), exactamente los mismos que usan las vistas JavaFX.
 *
 * <p>Se verifica el ciclo completo por HTTP: login JWT -&gt; Bearer -&gt; alta de
 * agenda/franjas/festivos -&gt; consulta, con los DTOs compartidos viajando con
 * fechas ISO en ambos sentidos. Los profesionales usados llevan prefijo {@code it-*}
 * para no chocar con la semilla V1.3.0 ni con los {@code api-*} de
 * {@code AgendasApiTest}: el contexto H2 se comparte entre clases.</p>
 *
 * <p>No sustituye los TC funcionales de {@code docs/pruebas/} (D28): aqui se prueba
 * que cliente y servidor se entienden de verdad (contrato, JSON y seguridad), no
 * solo el servidor consigo mismo.</p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AgendasClienteServidorIntegracionTest {

    @LocalServerPort
    private int puerto;

    /**
     * Recepcionista (actor Administrativo del CDU13): flujo completo por HTTP con el
     * cliente real. Altas, solape rechazado CONFLICTO_SOLAPE, bordes que se tocan
     * admitidos, festivo sobre dia con franjas rechazado CONFLICTO_FESTIVO, y la
     * consulta del detalle ve todo lo creado con las fechas correctas.
     *
     * @throws Exception si falla la comunicacion
     */
    @Test
    void recepcionistaGestionaUnaAgendaCompletaPorHttp() throws Exception {
        ApiClienteAgendas api = clienteDe("recepcionista", "iso2-rec");
        String profesional = "it-recepcion-flujo";
        api.crearAgenda(profesional);

        FranjaGestionDTO manana = api.anadirFranja(profesional,
                franja("2027-01-11", "08:00", "12:00"));
        api.anadirFranja(profesional, franja("2027-01-11", "12:00", "15:00")); // bordes que se tocan: OK
        api.anadirFestivo(profesional, new FestivoDTO(null,
                LocalDate.parse("2027-01-12"), "asunto propio"));

        ApiException solape = assertThrows(ApiException.class, () ->
                api.anadirFranja(profesional, franja("2027-01-11", "11:30", "13:00")));
        assertEquals(409, solape.status());
        assertEquals("CONFLICTO_SOLAPE", solape.codigo());
        assertTrue(solape.mensaje().contains("solapa"), "el cliente muestra el mensaje del servidor");

        ApiException festivoSobreFranja = assertThrows(ApiException.class, () ->
                api.anadirFestivo(profesional, new FestivoDTO(null,
                        LocalDate.parse("2027-01-11"), "vacaciones")));
        assertEquals(409, festivoSobreFranja.status());
        assertEquals("CONFLICTO_FESTIVO", festivoSobreFranja.codigo());

        AgendaDTO agenda = api.detalle(profesional);
        assertEquals(2, agenda.franjas().size());
        assertEquals(1, agenda.festivos().size());
        assertEquals(LocalDate.parse("2027-01-11"), agenda.franjas().get(0).dia());
        assertEquals(manana.id(), agenda.franjas().get(0).id());

        api.eliminarFranja(profesional, manana.id());
        assertEquals(1, api.detalle(profesional).franjas().size());
    }

    /**
     * "TC festivos" por HTTP en ambos sentidos: con festivo declarado primero, la
     * franja de ese dia se rechaza CONFLICTO_FESTIVO (el festivo manda sobre las
     * franjas, regla del CDU13).
     *
     * @throws Exception si falla la comunicacion
     */
    @Test
    void elFestivoDeclaradoPrimeroBloqueaLasFranjas() throws Exception {
        ApiClienteAgendas api = clienteDe("admin", "iso2-admin");
        String profesional = "it-festivo-manda";
        api.crearAgenda(profesional);
        api.anadirFestivo(profesional, new FestivoDTO(null,
                LocalDate.parse("2027-01-18"), "dia natural"));

        ApiException error = assertThrows(ApiException.class, () ->
                api.anadirFranja(profesional, franja("2027-01-18", "08:00", "15:00")));
        assertEquals(409, error.status());
        assertEquals("CONFLICTO_FESTIVO", error.codigo());
    }

    /**
     * Validacion del CDU por HTTP: franja con inicio no anterior al fin responde 400
     * con el codigo estable DATOS_INVALIDOS que el cliente traduce tal cual.
     *
     * @throws Exception si falla la comunicacion
     */
    @Test
    void franjaConInicioNoAnteriorAlFinEsDatosInvalidos() throws Exception {
        ApiClienteAgendas api = clienteDe("recepcionista", "iso2-rec");
        String profesional = "it-horas-malas";
        api.crearAgenda(profesional);

        ApiException error = assertThrows(ApiException.class, () ->
                api.anadirFranja(profesional, franja("2027-01-20", "12:00", "12:00")));
        assertEquals(400, error.status());
        assertEquals("DATOS_INVALIDOS", error.codigo());
    }

    /**
     * Agenda duplicada por HTTP: el segunda alta de la misma agenda (una creada por
     * el propio test y la semilla medica) responde CONFLICTO_AGENDA_EXISTENTE.
     *
     * @throws Exception si falla la comunicacion
     */
    @Test
    void repetirAltaDeAgendaEsConflictoExistente() throws Exception {
        ApiClienteAgendas api = clienteDe("admin", "iso2-admin");
        api.crearAgenda("it-duplicada");

        ApiException propia = assertThrows(ApiException.class, () -> api.crearAgenda("it-duplicada"));
        assertEquals(409, propia.status());
        assertEquals("CONFLICTO_AGENDA_EXISTENTE", propia.codigo());

        ApiException deSemilla = assertThrows(ApiException.class, () -> api.crearAgenda("medico"));
        assertEquals("CONFLICTO_AGENDA_EXISTENTE", deSemilla.codigo());
    }

    /**
     * Profesional sin agenda por HTTP: el detalle y el alta de franja responden 404
     * con el codigo AGENDA_NO_ENCONTRADA del contrato.
     *
     * @throws Exception si falla la comunicacion
     */
    @Test
    void elProfesionalSinAgendaDevuelveNoEncontrada() throws Exception {
        ApiClienteAgendas api = clienteDe("recepcionista", "iso2-rec");

        ApiException detalle = assertThrows(ApiException.class, () -> api.detalle("it-inexistente"));
        assertEquals(404, detalle.status());
        assertEquals("AGENDA_NO_ENCONTRADA", detalle.codigo());

        ApiException franja = assertThrows(ApiException.class, () ->
                api.anadirFranja("it-inexistente", franja("2027-01-22", "08:00", "15:00")));
        assertEquals(404, franja.status());
        assertEquals("AGENDA_NO_ENCONTRADA", franja.codigo());
    }

    /**
     * Frontera de seguridad por HTTP (matriz RBAC): el CELADOR, que no tiene
     * {@code agenda:leer} ni {@code agenda:escribir}, recibe 403 FORBIDDEN al intentar
     * dar de alta una agenda con el cliente real.
     *
     * @throws Exception si falla la comunicacion
     */
    @Test
    void elCeladorNoPuedeGestionarAgendasPorHttp() throws Exception {
        ApiClienteAgendas api = clienteDe("celador", "iso2-ses");

        ApiException error = assertThrows(ApiException.class, () -> api.crearAgenda("it-celador"));
        assertEquals(403, error.status());
        assertEquals("FORBIDDEN", error.codigo());
    }

    /**
     * Contrato baseline CDU12 por HTTP: el medico de la semilla tiene sus tres franjas
     * de noviembre y, al pedir las franjas libres del 9 al 13, el festivo del dia 10
     * queda fuera (el festivo manda). El medico (rol de solo lectura) puede consultar.
     *
     * @throws Exception si falla la comunicacion
     */
    @Test
    void lasFranjasLibresDeLaSemillaDescartanElFestivo() throws Exception {
        ApiClienteAgendas api = clienteDe("medico", "iso2-medico");

        List<FranjaDTO> libres = api.franjasLibres("medico",
                LocalDate.parse("2026-11-09"), LocalDate.parse("2026-11-13"));

        Set<LocalDate> dias = libres.stream().map(FranjaDTO::dia).collect(Collectors.toSet());
        assertTrue(dias.contains(LocalDate.parse("2026-11-09")));
        assertTrue(dias.contains(LocalDate.parse("2026-11-11")));
        assertTrue(dias.contains(LocalDate.parse("2026-11-13")));
        assertTrue(libres.stream().noneMatch(f -> LocalDate.parse("2026-11-10").equals(f.dia())),
                "el festivo del dia natural no ofrece huecos libres");
    }

    /**
     * Login real por HTTP con el cliente del CDU17 y cliente de agendas autenticado
     * con el JWT recien emitido (igual que hace la aplicacion JavaFX al abrir sesion).
     *
     * @param usuario usuario semilla de desarrollo
     * @param password contrasena semilla de desarrollo
     * @return cliente de agendas con el token de la sesion
     * @throws Exception si falla el login
     */
    private ApiClienteAgendas clienteDe(String usuario, String password) throws Exception {
        String raiz = "http://127.0.0.1:" + puerto;
        SesionDTO sesion = new ApiClienteAutenticacion(raiz).iniciarSesion(usuario, password);
        return new ApiClienteAgendas(raiz, sesion.token());
    }

    private static FranjaGestionDTO franja(String dia, String inicio, String fin) {
        return new FranjaGestionDTO(null, LocalDate.parse(dia),
                LocalTime.parse(inicio), LocalTime.parse(fin));
    }
}
