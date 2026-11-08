package es.uclm.iso2.common.comunicacion;

import java.time.Instant;
import java.util.List;

/**
 * Fachada de contratos del subdominio {@code comunicacion}: Notificaciones SMS/email al paciente (CDU02; reintentos en CDU04).
 * Solo contrato: la implementacion vive en el modulo iso2-comunicacion (baseline 1.0.0, D22).
 *
 * @since 1.0.0 (CFG-00)
 */
public interface ComunicacionFacade {

    /**
     * Envia una notificacion al paciente por el canal indicado.
     *
     * @param nueva datos de la notificacion
     * @return notificacion registrada con su estado de envio
     */
    NotificacionDTO enviar(NotificacionNuevaDTO nueva);

    /**
     * Lista las notificaciones pendientes de un destinatario.
     *
     * @param destinatario identificador del destinatario
     * @return notificaciones pendientes ordenadas por fecha
     */
    List<NotificacionDTO> pendientesPara(String destinatario);

    /**
     * Datos para enviar una notificacion.
     *
     * @param destinatario identificador del destinatario
     * @param asunto       asunto del mensaje
     * @param mensaje      cuerpo del mensaje
     * @param canal        canal de envio (sms, email)
     */
    record NotificacionNuevaDTO(String destinatario, String asunto, String mensaje, String canal) {
    }

    /**
     * Notificacion registrada.
     *
     * @param notificacionId identificador asignado
     * @param destinatario   identificador del destinatario
     * @param asunto         asunto del mensaje
     * @param canal          canal usado
     * @param enviada        momento del envio (nulo si pendiente)
     */
    record NotificacionDTO(String notificacionId, String destinatario, String asunto, String canal, Instant enviada) {
    }
}
