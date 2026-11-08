package es.uclm.iso2.client;

/**
 * Reglas de navegacion por rol del cliente (CDU17, Iter2/CFG-02).
 *
 * <p>Separadas de JavaFX a proposito: es la unica logica de menus por rol del cliente
 * y queda cubierta por JaCoCo y por tests unitarios (las vistas {@code ui/**} estan
 * excluidas de cobertura porque necesitan pantalla). La autorizacion de verdad la
 * aplica siempre el servidor; esto solo decide que interfaz se muestra para no
 * someter al usuario a 403 seguros.</p>
 *
 * @since 0.1.2 (CFG-02)
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
}
