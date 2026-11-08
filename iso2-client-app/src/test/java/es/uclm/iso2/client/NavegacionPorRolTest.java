package es.uclm.iso2.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test unitario de las reglas de navegacion por rol (CDU17, CFG-02; secciones de
 * agenda en CDU13, CFG-03 y de solicitudes en CDU06, CFG-04): solo ADMINISTRADOR ve la
 * vista editable del catalogo; en agendas, CELADOR no ve la seccion, MEDICO/TECNICO solo
 * lectura y ADMINISTRADOR/RECEPCIONISTA gestion; en solicitudes, TECNICO/CELADOR no ven
 * la seccion y solo ADMINISTRADOR/MEDICO prescriben. Refleja la matriz autoritativa de
 * {@code iso2-seguridad} (MatrizPermisos).
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
     * Tabla rol -&gt; ve la seccion de agendas (matriz RBAC: CELADOR sin
     * {@code agenda:leer}).
     */
    @ParameterizedTest
    @CsvSource({
            "ADMINISTRADOR,   true",
            "MEDICO,          true",
            "TECNICO,         true",
            "RECEPCIONISTA,   true",
            "CELADOR,         false",
    })
    void elCeladorNoVeLaSeccionDeAgendas(String rol, boolean esperado) {
        assertTrue(NavegacionPorRol.agendasVisiblePara(rol) == esperado, rol);
    }

    /**
     * Tabla rol -&gt; gestiona agendas (matriz RBAC: {@code agenda:escribir} solo
     * para el actor Administrativo del CDU13 y el administrador del sistema).
     */
    @ParameterizedTest
    @CsvSource({
            "ADMINISTRADOR,   true",
            "RECEPCIONISTA,   true",
            "MEDICO,          false",
            "TECNICO,         false",
            "CELADOR,         false",
    })
    void soloAdministradorYRecepcionistaGestionanAgendas(String rol, boolean esperado) {
        assertTrue(NavegacionPorRol.agendasEditablesPara(rol) == esperado, rol);
    }

    /**
     * Tabla rol -&gt; ve la seccion de solicitudes (matriz RBAC: TECNICO y CELADOR sin
     * {@code solicitud:leer}).
     */
    @ParameterizedTest
    @CsvSource({
            "ADMINISTRADOR,   true",
            "MEDICO,          true",
            "RECEPCIONISTA,   true",
            "TECNICO,         false",
            "CELADOR,         false",
    })
    void soloTecnicoYCeladorNoVenLaSeccionDeSolicitudes(String rol, boolean esperado) {
        assertTrue(NavegacionPorRol.solicitudesVisiblesPara(rol) == esperado, rol);
    }

    /**
     * Tabla rol -&gt; prescribe pruebas (matriz RBAC: {@code solicitud:escribir} solo
     * para el actor Medico del CDU06 y el administrador del sistema; la recepcion solo
     * consulta el detalle y su trazabilidad).
     */
    @ParameterizedTest
    @CsvSource({
            "ADMINISTRADOR,   true",
            "MEDICO,          true",
            "RECEPCIONISTA,   false",
            "TECNICO,         false",
            "CELADOR,         false",
    })
    void soloAdministradorYMedicoPrescriben(String rol, boolean esperado) {
        assertTrue(NavegacionPorRol.solicitudesPrescribiblesPara(rol) == esperado, rol);
    }

    /**
     * Defectos de entrada (nulo, vacio, rol desconocido): sin seccion de agenda ni de
     * solicitudes y sin edicion, nunca permiso por defecto.
     */
    @Test
    void rolesDesconocidosONulosNoDanPermisoDeEdicion() {
        assertFalse(NavegacionPorRol.catalogoEditablePara(null));
        assertFalse(NavegacionPorRol.catalogoEditablePara(""));
        assertFalse(NavegacionPorRol.catalogoEditablePara("PIRATA"));
        assertFalse(NavegacionPorRol.catalogoEditablePara("administrador"), "el claim usa el nombre del enum en mayusculas");
        assertFalse(NavegacionPorRol.agendasVisiblePara(null));
        assertFalse(NavegacionPorRol.agendasVisiblePara("PIRATA"));
        assertFalse(NavegacionPorRol.agendasEditablesPara(null));
        assertFalse(NavegacionPorRol.agendasEditablesPara("recepcionista"), "el claim usa el nombre del enum en mayusculas");
        assertFalse(NavegacionPorRol.solicitudesVisiblesPara(null));
        assertFalse(NavegacionPorRol.solicitudesVisiblesPara("PIRATA"));
        assertFalse(NavegacionPorRol.solicitudesPrescribiblesPara(null));
        assertFalse(NavegacionPorRol.solicitudesPrescribiblesPara("medico"), "el claim usa el nombre del enum en mayusculas");
    }
}
