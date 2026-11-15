package es.uclm.iso2.integracion.dominio;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Registro clinico que llega de la HCE externa. El codigo y la fecha son lo unico que
 * el spike usa para ordenar de forma estable la huella de idempotencia, por eso el
 * compacto los exige.
 *
 * @param codigo  codigo del registro (p. ej. {@code ENF-HTA})
 * @param fecha   fecha del registro clinico
 * @param detalle texto libre del registro
 * @since 1.1.0 (CFG-06, CDU19)
 */
public record RegistroHistoria(String codigo, LocalDate fecha, String detalle) {

    /**
     * Crea el registro comprobando que codigo y fecha existen.
     */
    public RegistroHistoria {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("el codigo del registro es obligatorio");
        }
        Objects.requireNonNull(fecha, "la fecha del registro es obligatoria");
    }
}
