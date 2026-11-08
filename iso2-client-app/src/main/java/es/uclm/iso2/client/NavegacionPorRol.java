package es.uclm.iso2.client;

/**
 * Reglas de navegacion por rol del cliente (CDU17, Iter2/CFG-02; ampliadas a las
 * agendas en CDU13, Iter3/CFG-03 y a las solicitudes en CDU06, Iter4/CFG-04).
 *
 * <p>Separadas de JavaFX a proposito: es la unica logica de menus por rol del cliente
 * y queda cubierta por JaCoCo y por tests unitarios (las vistas {@code ui/**} estan
 * excluidas de cobertura porque necesitan pantalla). La autorizacion de verdad la
 * aplica siempre el servidor; esto solo decide que interfaz se muestra para no
 * someter al usuario a 403 seguros.</p>
 *
 * @since 0.1.2 (CFG-02); secciones de agenda desde 0.1.3 (CFG-03); seccion de
 * solicitudes desde 0.1.4 (CFG-04)
 */
public final class NavegacionPorRol {

    private NavegacionPorRol() {
        // utilidad sin estado
    }

    /**
     * Si el rol puede ver la vista de administracion del catalogo (alta, modificacion,
     * baja logica y reactivacion). El resto de roles veran la vista de solo lectura.
     *
     * @param rol nombre del rol del claim JWT (ver {@code AutenticacionFacade.SesionDTO})
     * @return true solo para {@code ADMINISTRADOR} (matriz RBAC de CDU17)
     */
    public static boolean catalogoEditablePara(String rol) {
        return "ADMINISTRADOR".equals(rol);
    }

    /**
     * Si el rol puede ver la seccion de agendas (consulta o gestion). El CELADOR no
     * tiene el permiso {@code agenda:leer} en la matriz RBAC, asi que la seccion no
     * se le muestra (el servidor le responderia 403 igualmente).
     *
     * @param rol nombre del rol del claim JWT (ver {@code AutenticacionFacade.SesionDTO})
     * @return true para ADMINISTRADOR, MEDICO, TECNICO y RECEPCIONISTA
     * @since 0.1.3 (CDU13, CFG-03)
     */
    public static boolean agendasVisiblePara(String rol) {
        return switch (rol == null ? "" : rol) {
            case "ADMINISTRADOR", "MEDICO", "TECNICO", "RECEPCIONISTA" -> true;
            default -> false;
        };
    }

    /**
     * Si el rol puede gestionar agendas (alta de agenda/franjas/festivos y bajas).
     * Solo ADMINISTRADOR y RECEPCIONISTA tienen {@code agenda:escribir} (actor
     * Administrativo del CDU13); MEDICO y TECNICO ven la agenda en solo lectura.
     *
     * @param rol nombre del rol del claim JWT (ver {@code AutenticacionFacade.SesionDTO})
     * @return true solo para ADMINISTRADOR y RECEPCIONISTA
     * @since 0.1.3 (CDU13, CFG-03)
     */
    public static boolean agendasEditablesPara(String rol) {
        return "ADMINISTRADOR".equals(rol) || "RECEPCIONISTA".equals(rol);
    }

    /**
     * Si el rol puede ver la seccion de solicitudes. TECNICO y CELADOR no tienen el
     * permiso {@code solicitud:leer} en la matriz RBAC, asi que la seccion no se les
     * muestra (el servidor les responderia 403 igualmente).
     *
     * @param rol nombre del rol del claim JWT (ver {@code AutenticacionFacade.SesionDTO})
     * @return true para ADMINISTRADOR, MEDICO y RECEPCIONISTA
     * @since 0.1.4 (CDU06, CFG-04)
     */
    public static boolean solicitudesVisiblesPara(String rol) {
        return switch (rol == null ? "" : rol) {
            case "ADMINISTRADOR", "MEDICO", "RECEPCIONISTA" -> true;
            default -> false;
        };
    }

    /**
     * Si el rol puede prescribir pruebas (alta de solicitud). Solo ADMINISTRADOR y
     * MEDICO tienen {@code solicitud:escribir} (actor Medico del CDU06); la recepcion
     * solo consulta el detalle y su trazabilidad de no repudio.
     *
     * @param rol nombre del rol del claim JWT (ver {@code AutenticacionFacade.SesionDTO})
     * @return true solo para ADMINISTRADOR y MEDICO
     * @since 0.1.4 (CDU06, CFG-04)
     */
    public static boolean solicitudesPrescribiblesPara(String rol) {
        return "ADMINISTRADOR".equals(rol) || "MEDICO".equals(rol);
    }
}
