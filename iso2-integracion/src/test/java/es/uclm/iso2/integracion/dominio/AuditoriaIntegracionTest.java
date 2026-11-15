package es.uclm.iso2.integracion.dominio;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas tecnicas de la cadena de auditoria en memoria del CDU19: encadenado SHA-256
 * desde el genezis, secuencia 1-based, verificacion de integridad y deteccion de un
 * eslabon alterado. Es el patron de citas y agendas repetido sin tabla ni Flyway.
 */
class AuditoriaIntegracionTest {

    @Test
    void laCadenaVaciaArrancaEnElGenesis() {
        AuditoriaIntegracion auditoria = new AuditoriaIntegracion();
        assertEquals(AuditoriaIntegracion.GENESIS, auditoria.ultimoHash());
        assertEquals(64, AuditoriaIntegracion.GENESIS.length());
        assertTrue(auditoria.listar().isEmpty());
        assertTrue(auditoria.cadenaIntegra());
    }

    @Test
    void losEslabonesSeEncadenanDesdeElGenesis() {
        AuditoriaIntegracion auditoria = new AuditoriaIntegracion();
        EslabonAuditoria primero = auditoria.registrar("SSO_LOGIN", "sso", "OK", "admin");
        EslabonAuditoria segundo = auditoria.registrar("HCE_SINCRONIZAR", "hce", "OK_ACTUALIZADA", "admin");
        EslabonAuditoria tercero = auditoria.registrar("SIMULACION", "hce", "SIM-HCE-CAIDO-5-50-0", "admin");

        assertEquals(1L, primero.secuencia());
        assertEquals(2L, segundo.secuencia());
        assertEquals(3L, tercero.secuencia());
        assertEquals(AuditoriaIntegracion.GENESIS, primero.hashAnterior());
        assertEquals(primero.hash(), segundo.hashAnterior());
        assertEquals(segundo.hash(), tercero.hashAnterior());
        assertEquals(tercero.hash(), auditoria.ultimoHash());
        assertTrue(auditoria.cadenaIntegra());
    }

    @Test
    void elHashCoincideConElCanonicoRecalculado() {
        AuditoriaIntegracion auditoria = new AuditoriaIntegracion();
        EslabonAuditoria eslabon = auditoria.registrar("RIS_IMPORTAR", "ris", "OK", "admin");
        String canonico = eslabon.secuencia() + "|RIS_IMPORTAR|ris|OK|admin|"
                + eslabon.instante() + "|" + AuditoriaIntegracion.GENESIS;
        assertEquals(HuellaContenido.sha256Hex(canonico), eslabon.hash());
    }

    @Test
    void alterarUnEslabonRompeLaIntegridad() throws Exception {
        AuditoriaIntegracion auditoria = new AuditoriaIntegracion();
        auditoria.registrar("SSO_LOGIN", "sso", "OK", "admin");
        auditoria.registrar("SSO_LOGIN", "sso", "FALLO_CREDENCIALES", "admin");
        assertTrue(auditoria.cadenaIntegra());

        Field campo = AuditoriaIntegracion.class.getDeclaredField("eslabones");
        campo.setAccessible(true);
        @SuppressWarnings("unchecked")
        CopyOnWriteArrayList<EslabonAuditoria> eslabones =
                (CopyOnWriteArrayList<EslabonAuditoria>) campo.get(auditoria);
        EslabonAuditoria original = eslabones.get(0);
        EslabonAuditoria falseificado = new EslabonAuditoria(original.secuencia(), original.accion(),
                original.sistema(), "OK_BORRADO_POR_UN_ATACANTE", original.actor(), original.instante(),
                original.hashAnterior(), original.hash());
        eslabones.set(0, falseificado);

        assertFalse(auditoria.cadenaIntegra());
    }

    @Test
    void borrarUnEslabonRompeLaCadenaPorElHiloDelSiguiente() throws Exception {
        AuditoriaIntegracion auditoria = new AuditoriaIntegracion();
        auditoria.registrar("SSO_LOGIN", "sso", "OK", "admin");
        auditoria.registrar("HCE_SINCRONIZAR", "hce", "OK_ACTUALIZADA", "admin");
        auditoria.registrar("RIS_IMPORTAR", "ris", "OK", "admin");

        Field campo = AuditoriaIntegracion.class.getDeclaredField("eslabones");
        campo.setAccessible(true);
        @SuppressWarnings("unchecked")
        CopyOnWriteArrayList<EslabonAuditoria> eslabones =
                (CopyOnWriteArrayList<EslabonAuditoria>) campo.get(auditoria);
        eslabones.remove(1);

        assertFalse(auditoria.cadenaIntegra());
    }

    @Test
    void registrarEsSeguroEnConcurrencia() throws InterruptedException {
        AuditoriaIntegracion auditoria = new AuditoriaIntegracion();
        int hilos = 8;
        int porHilo = 25;
        List<Thread> ejecutando = new ArrayList<>();
        for (int h = 0; h < hilos; h++) {
            Thread hilo = new Thread(() -> {
                for (int i = 0; i < porHilo; i++) {
                    auditoria.registrar("HCE_SINCRONIZAR", "hce", "OK_ACTUALIZADA", "admin");
                }
            });
            ejecutando.add(hilo);
            hilo.start();
        }
        for (Thread hilo : ejecutando) {
            hilo.join();
        }

        assertEquals(hilos * porHilo, auditoria.listar().size());
        assertTrue(auditoria.cadenaIntegra());
        long esperado = 1L;
        for (EslabonAuditoria eslabon : auditoria.listar()) {
            assertEquals(esperado++, eslabon.secuencia());
        }
    }
}
