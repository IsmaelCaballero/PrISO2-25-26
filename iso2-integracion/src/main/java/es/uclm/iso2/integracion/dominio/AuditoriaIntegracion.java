package es.uclm.iso2.integracion.dominio;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Auditoria ligera EN MEMORIA de las acciones de integracion (CDU19). Reproduce el
 * patron de hash encadenado de la auditoria de citas y agendas, pero sin tabla ni
 * migracion Flyway: el spike de CDU19 no persiste y deja el hueco V1.6.0 libre para la
 * version definitiva del subdominio.
 *
 * <p>La cadena es verificable: cualquier alteracion posterior de un eslabon rompe
 * {@link #cadenaIntegra()}. No es tamper-evident frente a un atacante con permiso de
 * escritura en el proceso (podria reescribir la cadena entera), igual que en los modulos
 * persistidos; su valor es la trazabilidad de la operacion normal.
 *
 * @since 1.1.0 (CFG-06, CDU19)
 */
public class AuditoriaIntegracion {

    /** Hash previo del primer eslabon. */
    public static final String GENESIS = "0".repeat(64);

    private final CopyOnWriteArrayList<EslabonAuditoria> eslabones = new CopyOnWriteArrayList<>();
    private final Object cerrojo = new Object();
    private String ultimoHash = GENESIS;

    /**
     * Anade un eslabon encadenado con el hash del ultimo registro. Sincronizada sobre un
     * cerrojo propio: la API REST atiende peticiones concurrentes y la secuencia y el
     * encadenado no pueden partirse.
     *
     * @param accion    accion auditada
     * @param sistema   sistema externo afectado
     * @param resultado OK o codigo de fallo
     * @param actor     usuario que provoco la accion
     * @return el eslabon creado
     */
    public EslabonAuditoria registrar(String accion, String sistema, String resultado, String actor) {
        synchronized (cerrojo) {
            long secuencia = eslabones.size() + 1L;
            Instant instante = Instant.now();
            String canonico = canonico(secuencia, accion, sistema, resultado, actor, instante, ultimoHash);
            String hash = HuellaContenido.sha256Hex(canonico);
            EslabonAuditoria eslabon = new EslabonAuditoria(secuencia, accion, sistema, resultado,
                    actor, instante, ultimoHash, hash);
            eslabones.add(eslabon);
            ultimoHash = hash;
            return eslabon;
        }
    }

    /**
     * Recalcula la cadena completa desde GENESIS y comprueba que cada hash coincide con
     * su eslabon y con el {@code hashAnterior} del siguiente.
     *
     * @return true si la cadena no fue alterada
     */
    public boolean cadenaIntegra() {
        String previo = GENESIS;
        for (EslabonAuditoria eslabon : eslabones) {
            if (!previo.equals(eslabon.hashAnterior())) {
                return false;
            }
            String canonico = canonico(eslabon.secuencia(), eslabon.accion(), eslabon.sistema(),
                    eslabon.resultado(), eslabon.actor(), eslabon.instante(), eslabon.hashAnterior());
            if (!HuellaContenido.sha256Hex(canonico).equals(eslabon.hash())) {
                return false;
            }
            previo = eslabon.hash();
        }
        return true;
    }

    /**
     * Copia ordenada de los eslabones registrados.
     *
     * @return la cadena de auditoria completa
     */
    public List<EslabonAuditoria> listar() {
        return List.copyOf(eslabones);
    }

    /**
     * Hash del ultimo eslabon registrado.
     *
     * @return el hash cabecera de la cadena, GENESIS si esta vacia
     */
    public String ultimoHash() {
        return ultimoHash;
    }

    private static String canonico(long secuencia, String accion, String sistema, String resultado,
                                   String actor, Instant instante, String hashAnterior) {
        return secuencia + "|" + accion + "|" + sistema + "|" + resultado + "|" + actor
                + "|" + instante + "|" + hashAnterior;
    }
}
