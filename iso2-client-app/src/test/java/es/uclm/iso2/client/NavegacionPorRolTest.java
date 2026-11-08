package es.uclm.iso2.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test unitario de la regla de navegacion por rol (CDU17, CFG-02): solo ADMINISTRADOR
 * ve la vista editable del catalogo; los demas roles de la matriz, solo lectura.
 * Refleja la matriz autoritativa de {@code iso2-seguridad} (MatrizPermisos).
 */
class NavegacionPorRolTest {

    /**
     * Tabla rol -&gt; esperado editable, identica a la columna catalogo:escribir de
     * la matriz RBAC.
     */
    @ParameterizedTest
    @CsvSource({
            "ADMINISTRADOR,   true",
            "MEDICO,          false",
            "TECNICO,         false",
            "CELADOR,         false",
            "RECEPCIONISTA,   false",
    })
    void soloElAdministradorVistaEditable(String rol, boolean esperado) {
        assertTrue(NavegacionPorRol.catalogoEditablePara(rol) == esperado, rol);
    }

    /**
     * Defectos de entrada (nulo, vacio, rol desconocido): vista de solo lectura,
     * nunca permiso por defecto.
     */
    @Test
    void rolesDesconocidosONulosNoDanPermisoDeEdicion() {
        assertFalse(NavegacionPorRol.catalogoEditablePara(null));
        assertFalse(NavegacionPorRol.catalogoEditablePara(""));
        assertFalse(NavegacionPorRol.catalogoEditablePara("PIRATA"));
        assertFalse(NavegacionPorRol.catalogoEditablePara("administrador"), "el claim usa el nombre del enum en mayusculas");
    }
}
