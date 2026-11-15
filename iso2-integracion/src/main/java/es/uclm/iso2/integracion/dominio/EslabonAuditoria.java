package es.uclm.iso2.integracion.dominio;

import java.time.Instant;

/**
 * Eslabon inmutable de la cadena de auditoria de integracion. Cada eslabon guarda el
 * hash del anterior ({@code hashAnterior}) y el suyo ({@code hash}), de modo que borrar
 * o alterar un registro posterior rompe {@link AuditoriaIntegracion#cadenaIntegra()}.
 *
 * @param secuencia    numero de orden dentro de la cadena
 * @param accion       accion auditada (SSO_LOGIN, HCE_SINCRONIZAR, RIS_IMPORTAR o
 *                     SIMULACION: el ping de monitorizacion no se registra)
 * @param sistema      sistema externo afectado (sso, hce, ris, pacs o {@code -})
 * @param resultado    OK o el codigo de fallo (CREDENCIALES_INVALIDAS, CAIDO...)
 * @param actor        usuario que provoco la accion
 * @param instante     momento del registro
 * @param hashAnterior hash del eslabon previo (GENESIS en el primero)
 * @param hash         hash SHA-256 de este eslabon
 * @since 1.1.0 (CFG-06, CDU19)
 */
public record EslabonAuditoria(long secuencia, String accion, String sistema, String resultado,
                               String actor, Instant instante, String hashAnterior, String hash) {
}
