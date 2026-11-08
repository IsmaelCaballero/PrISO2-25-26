package es.uclm.iso2.seguridad.dominio;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test parametrico de la matriz RBAC autoritativa (CFG-02): 5 roles x 3 permisos.
 * La matriz espera exactamente lo pactado en el issue #13: ADMINISTRADOR con todo;
 * los demas roles, solo lectura de catalogo. Cualquier cambio en la matriz obliga a
 * actualizar esta tabla (y a revisarla con el cliente).
 */
class MatrizPermisosTest {

    /**
     * Tabla completa de la matriz: fila = rol, columnas = tiene_permiso esperado para
     * catalogo:leer, catalogo:escribir, usuario:gestionar.
     */
    @ParameterizedTest
    @CsvSource({
            "ADMINISTRADOR,   true,  true,  true",
            "MEDICO,          true,  false, false",
            "TECNICO,         true,  false, false",
            "CELADOR,         true,  false, false",
            "RECEPCIONISTA,   true,  false, false",
    })
    void laMatrizConcedeExactamenteLoPactado(Rol rol, boolean leer, boolean escribir, boolean gestionar) {
        assertEquals(leer, MatrizPermisos.tienePermiso(rol, Permiso.CATALOGO_LEER), rol + " catalogo:leer");
        assertEquals(escribir, MatrizPermisos.tienePermiso(rol, Permiso.CATALOGO_ESCRIBIR), rol + " catalogo:escribir");
        assertEquals(gestionar, MatrizPermisos.tienePermiso(rol, Permiso.USUARIO_GESTIONAR), rol + " usuario:gestionar");
    }

    /**
     * El conjunto de permisos de un rol es inmutable: nadie puede corromper la matriz
     * en tiempo de ejecucion.
     */
    @Test
    void losPermisosDeRolSonInmutables() {
        var permisos = MatrizPermisos.permisosDe(Rol.MEDICO);
        assertEquals(1, permisos.size());
        assertTrue(permisos.contains(Permiso.CATALOGO_LEER));
        assertThrowsUnsupported(permisos);
    }

    /**
     * Codigo de permiso y de rol estables (el claim del JWT y la matriz deben coincidir).
     */
    @Test
    void codigosEstablesDeRolYPermiso() {
        assertEquals("catalogo:leer", Permiso.CATALOGO_LEER.codigo());
        assertEquals("catalogo:escribir", Permiso.CATALOGO_ESCRIBIR.codigo());
        assertEquals("usuario:gestionar", Permiso.USUARIO_GESTIONAR.codigo());
        assertTrue(Permiso.desdeCodigo("catalogo:leer").orElseThrow() == Permiso.CATALOGO_LEER);
        assertTrue(Permiso.desdeCodigo("no:existe").isEmpty());
        assertTrue(Rol.desdeCodigo("MEDICO").orElseThrow() == Rol.MEDICO);
        assertTrue(Rol.desdeCodigo("NO_ROL").isEmpty());
    }

    private static void assertThrowsUnsupported(java.util.Set<Permiso> permisos) {
        assertFalse(intentarMutacion(permisos), "la matriz no debe ser mutable en ejecucion");
    }

    private static boolean intentarMutacion(java.util.Set<Permiso> permisos) {
        try {
            permisos.add(Permiso.USUARIO_GESTIONAR);
            return true;
        } catch (UnsupportedOperationException esperado) {
            return false;
        }
    }
}
