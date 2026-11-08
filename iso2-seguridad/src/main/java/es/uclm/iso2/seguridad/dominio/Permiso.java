package es.uclm.iso2.seguridad.dominio;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/**
 * Permisos finos del RBAC (CDU17). El codigo estable es lo que viaja en la matriz
 * y lo que comprueba {@code AutenticacionFacade.tienePermiso(rol, codigoPermiso)}:
 * cambiar un codigo es un cambio de contrato (SemVer).
 *
 * <p>En CDU17 (CFG-02) se materializaron los permisos del catalogo (CDU16 ya existente) y
 * la autogestion de seguridad; con CDU13 (CFG-03) se anaden los de agenda. Los permisos
 * de los demas dominios (solicitudes, citas, informes) se iran anadiendo con su CDU
 * correspondiente.</p>
 *
 * @since 1.1.0 (CFG-02)
 */
public enum Permiso {

    /** Consultar el catalogo de pruebas de imagen. */
    CATALOGO_LEER("catalogo:leer"),

    /** Alta, edicion y baja logica del catalogo. */
    CATALOGO_ESCRIBIR("catalogo:escribir"),

    /** Consultar las agendas de medicos y tecnicos (llega con CDU13). */
    AGENDA_LEER("agenda:leer"),

    /** Alta y gestion de franjas y festivos de las agendas (actor Administrativo del CDU13). */
    AGENDA_ESCRIBIR("agenda:escribir"),

    /** Gestionar usuarios y roles (llega completo con CDU18). */
    USUARIO_GESTIONAR("usuario:gestionar");

    private final String codigo;

    Permiso(String codigo) {
        this.codigo = codigo;
    }

    /**
     * Codigo estable del permiso (el que se compara en la fachada RBAC).
     *
     * @return codigo en forma {@code dominio:accion}
     */
    public String codigo() {
        return codigo;
    }

    /**
     * Busca un permiso por su codigo estable, tolerante a nulos.
     *
     * @param codigo codigo a resolver
     * @return el permiso si existe; vacio si el codigo no es un permiso conocido
     */
    public static Optional<Permiso> desdeCodigo(String codigo) {
        if (codigo == null) {
            return Optional.empty();
        }
        String normalizado = codigo.trim().toLowerCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(permiso -> permiso.codigo.equals(normalizado))
                .findFirst();
    }
}
