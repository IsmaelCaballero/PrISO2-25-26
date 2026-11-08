package es.uclm.iso2.seguridad.dominio;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/**
 * Roles humanos del sistema (CDU17, RF17). En v1 una persona desempena un unico rol
 * (supuesto de simplificacion D10), por eso el rol es un campo simple del usuario y
 * no una coleccion.
 *
 * <p>Los cinco roles salen del diagrama de casos de uso de CDU16 (Iter1): el
 * ADMINISTRADOR gestiona; medico, tecnico, celador y recepcionista son los roles
 * de trabajo clinico/asistencial con permisos de consulta.</p>
 *
 * @since 1.1.0 (CFG-02)
 */
public enum Rol {

    /** Gestion de catalogo, usuarios y consola de datos. */
    ADMINISTRADOR,

    /** Facultativo: solicita, ve resultados y agenda clinica (futuros CDU). */
    MEDICO,

    /** Tecnico facultativo: ejecuta pruebas y gestiona equipos/protocolos (futuros CDU). */
    TECNICO,

    /** Celador: traslados y preparacion de pacientes (futuros CDU). */
    CELADOR,

    /** Recepcionista: administracion de solicitudes y citacion (futuros CDU). */
    RECEPCIONISTA;

    /**
     * Busca un rol por su codigo (case-insensitive), tolerante a cadenas nulas.
     *
     * @param codigo codigo del rol, tal y como viene del token o de la tabla de usuarios
     * @return el rol si existe; vacio si el codigo no es un rol conocido
     */
    public static Optional<Rol> desdeCodigo(String codigo) {
        if (codigo == null) {
            return Optional.empty();
        }
        String normalizado = codigo.trim().toUpperCase(Locale.ROOT);
        return Arrays.stream(values()).filter(rol -> rol.name().equals(normalizado)).findFirst();
    }
}
