package es.uclm.iso2.seguridad.dominio;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Matriz RBAC autoritativa de CDU17: que permisos tiene cada rol. Unica fuente de
 * verdad para {@code AutenticacionFacade.tienePermiso}; las reglas de la cadena de
 * seguridad del servidor se derivan de aqui (decision D del issue #13: los endpoints
 * se protegen por permiso, no por rol suelto).
 *
 * <p>Regla de diseno: el ADMINISTRADOR tiene todos los permisos; los cuatro roles de
 * trabajo (medico, tecnico, celador, recepcionista) nacen con permisos de lectura y se
 * amplian con su CDU. Con CDU13 (CFG-03) el actor Administrativo del CDU (recepcion)
 * recibe escritura de agendas junto al ADMINISTRADOR, y medico/tecnico leen su agenda.
 * Con CDU06 (CFG-04) el Medico prescribe (solicitud:escribir) y el resto de roles
 * sanitarios solo consultan: recepcion lee solicitudes, tecnico y celador no.</p>
 *
 * @since 1.1.0 (CFG-02)
 */
public final class MatrizPermisos {

    private static final Map<Rol, Set<Permiso>> MATRIZ = new EnumMap<>(Rol.class);

    static {
        MATRIZ.put(Rol.ADMINISTRADOR, EnumSet.allOf(Permiso.class));
        MATRIZ.put(Rol.MEDICO, EnumSet.of(Permiso.CATALOGO_LEER, Permiso.AGENDA_LEER,
                Permiso.SOLICITUD_LEER, Permiso.SOLICITUD_ESCRIBIR));
        MATRIZ.put(Rol.TECNICO, EnumSet.of(Permiso.CATALOGO_LEER, Permiso.AGENDA_LEER));
        MATRIZ.put(Rol.CELADOR, EnumSet.of(Permiso.CATALOGO_LEER));
        MATRIZ.put(Rol.RECEPCIONISTA, EnumSet.of(Permiso.CATALOGO_LEER,
                Permiso.AGENDA_LEER, Permiso.AGENDA_ESCRIBIR, Permiso.SOLICITUD_LEER));
    }

    private MatrizPermisos() {
    }

    /**
     * Permisos (inmutables) asignados a un rol.
     *
     * @param rol rol a consultar
     * @return conjunto de permisos del rol
     */
    public static Set<Permiso> permisosDe(Rol rol) {
        return Collections.unmodifiableSet(MATRIZ.getOrDefault(rol, EnumSet.noneOf(Permiso.class)));
    }

    /**
     * Comprueba si un rol posee un permiso concreto.
     *
     * @param rol     rol a evaluar
     * @param permiso permiso a comprobar
     * @return true si el rol tiene el permiso
     */
    public static boolean tienePermiso(Rol rol, Permiso permiso) {
        return MATRIZ.getOrDefault(rol, EnumSet.noneOf(Permiso.class)).contains(permiso);
    }
}
