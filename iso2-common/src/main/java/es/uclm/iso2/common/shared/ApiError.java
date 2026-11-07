package es.uclm.iso2.common.shared;

import java.time.Instant;

/**
 * Representacion de un error de API para las dos aplicaciones (baseline 1.0.0).
 *
 * @param codigo  codigo estable de error (ej. {@code SOLICITUD_NO_ENCONTRADA})
 * @param mensaje mensaje legible para el usuario final
 * @param instante momento en que se genero el error
 */
public record ApiError(String codigo, String mensaje, Instant instante) {
}