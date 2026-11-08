package es.uclm.iso2.common.solicitudes;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/**
 * Prioridad clinica de una solicitud de prueba (CDU06, RF06.1: campo obligatorio).
 * El nombre estable es lo que viaja en el JSON de la API y lo que se guarda en la
 * tabla {@code solicitud}: cambiar un nombre es un cambio de contrato (SemVer).
 *
 * @since 1.1.0 (CFG-04, CDU06)
 */
public enum Prioridad {

    /** Sin premura: se gestiona en lista de espera ordinaria. */
    ORDINARIA,

    /** Premura intermedia: por delante de las ordinarias. */
    PREFERENTE,

    /** Premura maxima: se gestiona con preferencia absoluta. */
    URGENTE;

    /**
     * Resuelve una prioridad desde su nombre, tolerante a nulos y a mayusculas.
     *
     * @param nombre nombre enviado por el cliente
     * @return la prioridad si el nombre es conocido; vacio en caso contrario
     */
    public static Optional<Prioridad> desdeNombre(String nombre) {
        if (nombre == null) {
            return Optional.empty();
        }
        String normalizado = nombre.trim().toUpperCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(p -> p.name().equals(normalizado))
                .findFirst();
    }
}
