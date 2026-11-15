package es.uclm.iso2.integracion.servicio;

import es.uclm.iso2.common.integracion.CredencialesInvalidasException;
import es.uclm.iso2.common.integracion.EstudioNoEncontradoException;
import es.uclm.iso2.common.integracion.HistoriaNoEncontradaException;
import es.uclm.iso2.common.integracion.InteropFacade.AuditoriaIntegracionDTO;
import es.uclm.iso2.common.integracion.InteropFacade.CredencialesSsoDTO;
import es.uclm.iso2.common.integracion.InteropFacade.EstadoSistemaDTO;
import es.uclm.iso2.common.integracion.InteropFacade.EstudioDTO;
import es.uclm.iso2.common.integracion.InteropFacade.HistoriaSincronizadaDTO;
import es.uclm.iso2.common.integracion.InteropFacade.SesionSsoDTO;
import es.uclm.iso2.common.integracion.SistemaExternoException;
import es.uclm.iso2.common.integracion.SistemaExternoException.Fallo;
import es.uclm.iso2.common.integracion.SistemaNoEncontradoException;
import es.uclm.iso2.integracion.adaptador.HceAdaptadorSimulado;
import es.uclm.iso2.integracion.adaptador.PacsAdaptadorSimulado;
import es.uclm.iso2.integracion.adaptador.RisAdaptadorSimulado;
import es.uclm.iso2.integracion.adaptador.SsoAdaptadorSimulado;
import es.uclm.iso2.integracion.dominio.AuditoriaIntegracion;
import es.uclm.iso2.integracion.dominio.EslabonAuditoria;
import es.uclm.iso2.integracion.dominio.HistoriaExterna;
import es.uclm.iso2.integracion.dominio.HuellaContenido;
import es.uclm.iso2.integracion.dominio.ModoSimulacion;
import es.uclm.iso2.integracion.dominio.PoliticaSimulacion;
import es.uclm.iso2.integracion.dominio.RegistroHistoria;
import es.uclm.iso2.integracion.dominio.RegistroSimulacion;
import es.uclm.iso2.integracion.dominio.SistemaExterno;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas tecnicas del servicio que implementa {@code InteropFacade} (CDU19) con los
 * cuatro adaptadores simulados reales y el registro de politicas en modo normal: idem-
 * potencia de la sincronizacion por huella SHA-256, auditoria encadenada con los resul-
 * tados que fija el contrato, actor tomado del contexto de seguridad y ping degradado
 * que jamas lanza. Sin Spring: el servicio se construye a mano igual que en produccion.
 */
class IntegracionServicioTest {

    private RegistroSimulacion registro;
    private AuditoriaIntegracion auditoria;
    private IntegracionServicio servicio;

    @BeforeEach
    void montar() {
        registro = new RegistroSimulacion(Map.of());
        auditoria = new AuditoriaIntegracion();
        servicio = new IntegracionServicio(new SsoAdaptadorSimulado(registro),
                new HceAdaptadorSimulado(registro), new RisAdaptadorSimulado(registro),
                new PacsAdaptadorSimulado(registro), auditoria);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void desmontar() {
        SecurityContextHolder.clearContext();
    }

    private static void autenticarComo(String usuario) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(usuario, "x"));
    }

    private static CredencialesSsoDTO credenciales(String usuario, String contrasena) {
        return new CredencialesSsoDTO(usuario, contrasena);
    }

    private List<EslabonAuditoria> cadena() {
        return auditoria.listar();
    }

    // ------------------------------------------------------------------
    // Auditoria en crudo
    // ------------------------------------------------------------------

    @Test
    void laAuditoriaVaciaDevuelveElGenesisSinEslabones() {
        AuditoriaIntegracionDTO dto = servicio.estadoAuditoriaIntegracion();
        assertTrue(dto.cadenaIntegra());
        assertEquals(0, dto.totalRegistros());
        assertEquals(AuditoriaIntegracion.GENESIS, dto.ultimoHash());
        assertTrue(dto.eslabones().isEmpty());
    }

    // ------------------------------------------------------------------
    // Login SSO
    // ------------------------------------------------------------------

    @Test
    void elLoginSsoCorrectoAuditaUnOkConElActorDelContexto() {
        autenticarComo("admin-iso2");
        SesionSsoDTO sesion = servicio.autenticarSso(credenciales("idp-admin", "idp-pass"));

        assertEquals("idp-admin", sesion.sujeto());
        assertEquals("radiologia", sesion.rolExterno());
        assertNotNull(sesion.caducaEn());

        List<EslabonAuditoria> cadena = cadena();
        assertEquals(1, cadena.size());
        assertEquals("SSO_LOGIN", cadena.get(0).accion());
        assertEquals("sso", cadena.get(0).sistema());
        assertEquals("OK", cadena.get(0).resultado());
        assertEquals("admin-iso2", cadena.get(0).actor());
    }

    @Test
    void sinContextoDeSeguridadElActorAuditadoEsElSistema() {
        servicio.autenticarSso(credenciales("idp-admin", "idp-pass"));
        assertEquals("USUARIO_SISTEMA", cadena().get(0).actor());
    }

    @Test
    void elLoginRechazadoAuditaFalloDeCredenciales() {
        assertThrows(CredencialesInvalidasException.class,
                () -> servicio.autenticarSso(credenciales("nadie", "fallida")));

        List<EslabonAuditoria> cadena = cadena();
        assertEquals(1, cadena.size());
        assertEquals("FALLO_CREDENCIALES", cadena.get(0).resultado());
        assertTrue(auditoria.cadenaIntegra());
    }

    @Test
    void elLoginConIdpCaidoAuditaFalloDeSistema() {
        registro.cambiar(SistemaExterno.SSO, new PoliticaSimulacion(ModoSimulacion.CAIDO, 0, 50, 0));

        SistemaExternoException e = assertThrows(SistemaExternoException.class,
                () -> servicio.autenticarSso(credenciales("idp-admin", "idp-pass")));
        assertEquals(Fallo.NO_DISPONIBLE, e.tipo());
        assertEquals("FALLO_SISTEMA", cadena().get(0).resultado());
    }

    @Test
    void lasCredencialesNulasSonDatosInvalidosYNoSeAuditan() {
        assertThrows(IllegalArgumentException.class, () -> servicio.autenticarSso(null));
        assertTrue(cadena().isEmpty());
    }

    // ------------------------------------------------------------------
    // Idempotencia de la sincronizacion de historia
    // ------------------------------------------------------------------

    @Test
    void laPrimeraSincronizacionCalculaLaHuellaYLaSegundaLaReutiliza() {
        HistoriaSincronizadaDTO primera = servicio.sincronizarHistoriaClinica("api-paciente-ok");
        assertFalse(primera.reutilizado());
        assertEquals(2, primera.registros().size());
        assertEquals(64, primera.huella().length());

        HistoriaExterna esperada = new HistoriaExterna("api-paciente-ok", List.of(
                new RegistroHistoria("ENF-HTA", LocalDate.of(2024, 3, 12), "Hipertension arterial diagnostica"),
                new RegistroHistoria("ALG-PENI", LocalDate.of(2023, 11, 2), "Alergia a penicilina")));
        assertEquals(HuellaContenido.sha256Hex(esperada.canonica()), primera.huella());

        HistoriaSincronizadaDTO segunda = servicio.sincronizarHistoriaClinica("api-paciente-ok");
        assertTrue(segunda.reutilizado());
        assertEquals(primera.huella(), segunda.huella());
        assertEquals(primera.sincronizadaEn(), segunda.sincronizadaEn());
        assertEquals(primera.registros(), segunda.registros());

        List<EslabonAuditoria> cadena = cadena();
        assertEquals("OK_ACTUALIZADA", cadena.get(0).resultado());
        assertEquals("OK_REUTILIZADA", cadena.get(1).resultado());
        assertEquals("HCE_SINCRONIZAR", cadena.get(1).accion());
    }

    @Test
    void elPacienteRotativoAportaSiempreHuellaNueva() {
        HistoriaSincronizadaDTO primera = servicio.sincronizarHistoriaClinica("api-paciente-rotativo");
        HistoriaSincronizadaDTO segunda = servicio.sincronizarHistoriaClinica("api-paciente-rotativo");
        HistoriaSincronizadaDTO tercera = servicio.sincronizarHistoriaClinica("api-paciente-rotativo");

        assertFalse(primera.reutilizado());
        assertFalse(segunda.reutilizado(), "contenido nuevo: no se reutiliza la instantanea");
        assertFalse(tercera.reutilizado());
        assertNotEquals(primera.huella(), segunda.huella());
        assertEquals(primera.huella(), tercera.huella(),
                "la tercera sincronizacion reemite el contenido de la primera");
        assertEquals(3, cadena().size(), "cada sincronizacion deja su eslabon");
    }

    @Test
    void losEspaciosDelPacienteNoRompenLaIdempotencia() {
        HistoriaSincronizadaDTO primera = servicio.sincronizarHistoriaClinica("  api-paciente-ok ");
        assertEquals("api-paciente-ok", primera.pacienteExternoId());

        HistoriaSincronizadaDTO segunda = servicio.sincronizarHistoriaClinica("api-paciente-ok  ");
        assertTrue(segunda.reutilizado(), "la clave del snapshot usa el id recortado");
        assertEquals("api-paciente-ok", segunda.pacienteExternoId());
    }

    @Test
    void elPacienteInexistenteNoDejaEslabonEnLaCadena() {
        assertThrows(HistoriaNoEncontradaException.class,
                () -> servicio.sincronizarHistoriaClinica("quien-esos"));
        assertThrows(IllegalArgumentException.class,
                () -> servicio.sincronizarHistoriaClinica("   "));
        assertTrue(cadena().isEmpty());
    }

    @Test
    void laSincronizacionConHceCaidaEsFalloDeSistema() {
        registro.cambiar(SistemaExterno.HCE, new PoliticaSimulacion(ModoSimulacion.CAIDO, 0, 50, 0));

        SistemaExternoException e = assertThrows(SistemaExternoException.class,
                () -> servicio.sincronizarHistoriaClinica("api-paciente-ok"));
        assertEquals(Fallo.NO_DISPONIBLE, e.tipo());
        assertTrue(cadena().isEmpty(), "el 404/503 de sistema no audita la sincronizacion");
    }

    // ------------------------------------------------------------------
    // Importacion del baseline (RIS)
    // ------------------------------------------------------------------

    @Test
    void importarEstudioDevuelveLaCabeceraYauditaLaImportacion() {
        EstudioDTO estudio = servicio.importarEstudio("  acc-0002 ");

        assertEquals("ACC-0002", estudio.accessionNumber());
        assertEquals("HC-1002", estudio.pacienteId());
        assertEquals(LocalDate.of(2025, 10, 2), estudio.fechaEstudio());
        assertEquals(1, estudio.numeroSeries());
        assertEquals("RIS_IMPORTAR", cadena().get(0).accion());
        assertEquals("OK", cadena().get(0).resultado());
    }

    @Test
    void importarUnEstudioInexistenteLanzaCuatroCuatroSinAuditar() {
        assertThrows(EstudioNoEncontradoException.class, () -> servicio.importarEstudio("ACC-9999"));
        assertTrue(cadena().isEmpty());
    }

    @Test
    void importarConRisCaidoAuditaFalloDeSistema() {
        registro.cambiar(SistemaExterno.RIS, new PoliticaSimulacion(ModoSimulacion.CAIDO, 0, 50, 0));

        assertThrows(SistemaExternoException.class, () -> servicio.importarEstudio("ACC-0001"));
        assertEquals("FALLO_SISTEMA", cadena().get(0).resultado());
    }

    // ------------------------------------------------------------------
    // Ping degradado
    // ------------------------------------------------------------------

    @Test
    void estadoSistemasDevuelveLosCuatroEnOrdenYDisponibles() {
        List<EstadoSistemaDTO> estados = servicio.estadoSistemas();

        assertEquals(4, estados.size());
        assertEquals(List.of("sso", "hce", "ris", "pacs"),
                estados.stream().map(EstadoSistemaDTO::nombre).toList());
        assertTrue(estados.stream().allMatch(EstadoSistemaDTO::disponible));
        assertTrue(estados.stream().allMatch(e -> e.motivo() == null));
    }

    @Test
    void elPingDeUnSistemaCaidoPintaEstadoSinLanzar() {
        registro.cambiar(SistemaExterno.RIS, new PoliticaSimulacion(ModoSimulacion.CAIDO, 5, 50, 0));

        EstadoSistemaDTO ris = servicio.pingRis();
        assertFalse(ris.disponible());
        assertEquals("CAIDO", ris.motivo());

        EstadoSistemaDTO pacs = servicio.pingPacs();
        assertTrue(pacs.disponible());
    }

    @Test
    void elPingDegradadoNoAuditaNada() {
        servicio.pingSistema("hce");
        servicio.pingRis();
        servicio.pingPacs();
        servicio.estadoSistemas();
        assertTrue(cadena().isEmpty(), "la monitorizacion periodica no llena la cadena");
    }

    @Test
    void unNombreDeSistemaDesconocidoEsCuatroCuatro() {
        assertThrows(SistemaNoEncontradoException.class, () -> servicio.pingSistema("sap"));
        assertThrows(SistemaNoEncontradoException.class, () -> servicio.pingSistema(null));
    }

    @Test
    void elPingAceptaMayusculasYEspacios() {
        EstadoSistemaDTO estado = servicio.pingSistema("  RIS ");
        assertEquals("ris", estado.nombre());
        assertTrue(estado.disponible());
    }

    // ------------------------------------------------------------------
    // Huella de contenido
    // ------------------------------------------------------------------

    @Test
    void laHuellaEsEstableYDependeDelContenido() {
        String base = HuellaContenido.sha256Hex("api-paciente-ok\nA|1");
        assertEquals(64, base.length());
        assertEquals(base, HuellaContenido.sha256Hex("api-paciente-ok\nA|1"));
        assertFalse(base.equals(HuellaContenido.sha256Hex("api-paciente-ok\nB|1")));
    }
}
