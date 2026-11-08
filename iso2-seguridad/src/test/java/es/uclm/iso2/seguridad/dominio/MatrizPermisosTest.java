package es.uclm.iso2.seguridad.dominio;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test parametrico de la matriz RBAC autoritativa (CFG-02 ampliada en CFG-03):
 * 5 roles x 5 permisos. La matriz espera exactamente lo pactado en los issues #13 y #15:
 * ADMINISTRADOR con todo; recepcionista gestiona agendas; medico y tecnico leen su
 * agenda; celador solo lee catalogo. Cualquier cambio en la matriz obliga a actualizar
 * esta tabla (y a revisarla con el cliente).
 */
class MatrizPermisosTest {

    /**
     * Tabla completa de la matriz: fila = rol, columnas = tiene_permiso esperado para
     * catalogo:leer, catalogo:escribir, agenda:leer, agenda:escribir, usuario:gestionar.
     */
    @ParameterizedTest
    @CsvSource({
            "ADMINISTRADOR,   true,  true,  true,  true,  true",
            "MEDICO,          true,  false, true,  false, false",
            "TECNICO,         true,  false, true,  false, false",
            "CELADOR,         true,  false, false, false, false",
            "RECEPCIONISTA,   true,  false, true,  true,  false",
    })
    void laMatrizConcedeExactamenteLoPactado(Rol rol, boolean catLeer, boolean catEscribir,
                                             boolean agLeer, boolean agEscribir, boolean gestionar) {
        assertEquals(catLeer, MatrizPermisos.tienePermiso(rol, Permiso.CATALOGO_LEER), rol + " catalogo:leer");
        assertEquals(catEscribir, MatrizPermisos.tienePermiso(rol, Permiso.CATALOGO_ESCRIBIR), rol + " catalogo:escribir");
        assertEquals(agLeer, MatrizPermisos.tienePermiso(rol, Permiso.AGENDA_LEER), rol + " agenda:leer");
        assertEquals(agEscribir, MatrizPermisos.tienePermiso(rol, Permiso.AGENDA_ESCRIBIR), rol + " agenda:escribir");
        assertEquals(gestionar, MatrizPermisos.tienePermiso(rol, Permiso.USUARIO_GESTIONAR), rol + " usuario:gestionar");
    }

    /**
     * El conjunto de permisos de un rol es inmutable: nadie puede corromper la matriz
     * en tiempo de ejecucion.
     */
    @Test
    void losPermisosDeRolSonInmutables() {
        var permisos = MatrizPermisos.permisosDe(Rol.MEDICO);
        assertEquals(2, permisos.size());
        assertTrue(permisos.contains(Permiso.CATALOGO_LEER));
        assertTrue(permisos.contains(Permiso.AGENDA_LEER));
        assertThrowsUnsupported(permisos);
    }

    /**
     * Codigo de permiso y de rol estables (el claim del JWT y la matriz deben coincidir).
     */
    @Test
    void codigosEstablesDeRolYPermiso() {
        assertEquals("catalogo:leer", Permiso.CATALOGO_LEER.codigo());
        assertEquals("catalogo:escribir", Permiso.CATALOGO_ESCRIBIR.codigo());
        assertEquals("agenda:leer", Permiso.AGENDA_LEER.codigo());
        assertEquals("agenda:escribir", Permiso.AGENDA_ESCRIBIR.codigo());
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
