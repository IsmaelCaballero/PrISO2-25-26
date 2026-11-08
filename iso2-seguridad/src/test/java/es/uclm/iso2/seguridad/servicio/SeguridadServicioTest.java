package es.uclm.iso2.seguridad.servicio;

import es.uclm.iso2.common.seguridad.AutenticacionFacade.CredencialesDTO;
import es.uclm.iso2.common.seguridad.AutenticacionFacade.SesionDTO;
import es.uclm.iso2.seguridad.dominio.CredencialesInvalidasException;
import es.uclm.iso2.seguridad.dominio.CuentaBloqueadaException;
import es.uclm.iso2.seguridad.dominio.Rol;
import es.uclm.iso2.seguridad.dominio.Usuario;
import es.uclm.iso2.seguridad.repositorio.UsuarioRepositorio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios del servicio de autenticacion (CDU17): BCrypt real + JWT real +
 * repositorio simulado. Cubren las reglas de negocio sin Base de Datos: emision de
 * token, contador de intentos, bloqueo a los 5, cierre de cuenta inactiva y la
 * resolucion de permisos contra la matriz.
 */
class SeguridadServicioTest {

    private static final String SECRETO = "secreto-de-pruebas-para-jwt-de-mas-de-32-bytes-seguro";

    private UsuarioRepositorio repositorio;
    private PasswordEncoder codificador;
    private SeguridadServicio servicio;
    private Usuario medico;

    @BeforeEach
    void setUp() {
        repositorio = mock(UsuarioRepositorio.class);
        codificador = new BCryptPasswordEncoder();
        servicio = new SeguridadServicio(repositorio, codificador, new JwtUtils(SECRETO, 30));
        medico = new Usuario("medico", codificador.encode("iso2-medico"), Rol.MEDICO, true);
        when(repositorio.findByUsuario("medico")).thenReturn(Optional.of(medico));
    }

    /**
     * Credenciales correctas: sesion con token verificable y rol correcto.
     */
    @Test
    void credencialesValidasEmitenSesion() {
        SesionDTO sesion = servicio.iniciarSesion(new CredencialesDTO("medico", "iso2-medico"));
        assertEquals("medico", sesion.usuario());
        assertEquals("MEDICO", sesion.rol());
        assertNotNull(sesion.token());
        assertTrue(sesion.caducidad().isAfter(java.time.Instant.now()));
    }

    /**
     * El login con exito reinicia el contador de intentos (aunque viniera cargado).
     */
    @Test
    void exitoReiniciaElContador() {
        medico.registrarIntentoFallido(5, Duration.ofMinutes(15));
        medico.registrarIntentoFallido(5, Duration.ofMinutes(15));
        servicio.iniciarSesion(new CredencialesDTO("medico", "iso2-medico"));
        assertEquals(0, medico.getIntentosFallidos());
    }

    /**
     * Contrasena erronea: error unico y suma de intento. Al quinto, bloqueo y la
     * contrasena correcta ya no autentica (CuentaBloqueadaException con fecha).
     */
    @Test
    void quintoIntentoFallidoBloqueaLaCuenta() {
        for (int i = 1; i <= 5; i++) {
            assertThrows(CredencialesInvalidasException.class,
                    () -> servicio.iniciarSesion(new CredencialesDTO("medico", "no-es-la-clave")));
        }
        assertEquals(5, medico.getIntentosFallidos());
        assertTrue(medico.estaBloqueado());

        CuentaBloqueadaException bloqueo = assertThrows(CuentaBloqueadaException.class,
                () -> servicio.iniciarSesion(new CredencialesDTO("medico", "iso2-medico")));
        assertNotNull(bloqueo.getHasta());
    }

    /**
     * Usuario inexistente, inactivo o con rol corrupto en BD: nunca hay sesion.
     * El inexistente y el inactivo comparten error para no revelar la causa.
     */
    @Test
    void usuariosNoHabilitadosNoAutentican() {
        when(repositorio.findByUsuario("fantasma")).thenReturn(Optional.empty());
        assertThrows(CredencialesInvalidasException.class,
                () -> servicio.iniciarSesion(new CredencialesDTO("fantasma", "nada")));

        Usuario baja = new Usuario("baja", codificador.encode("clave"), Rol.MEDICO, false);
        when(repositorio.findByUsuario("baja")).thenReturn(Optional.of(baja));
        assertThrows(CredencialesInvalidasException.class,
                () -> servicio.iniciarSesion(new CredencialesDTO("baja", "clave")));
    }

    /**
     * Validacion de entrada: credenciales vacias o nulas son 400, no un 500.
     */
    @Test
    void credencialesVaciasSonIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> servicio.iniciarSesion(null));
        assertThrows(IllegalArgumentException.class,
                () -> servicio.iniciarSesion(new CredencialesDTO("", "iso2-medico")));
        assertThrows(IllegalArgumentException.class,
                () -> servicio.iniciarSesion(new CredencialesDTO("medico", "  ")));
    }

    /**
     * RBAC por permisos: coincide con la matriz; codigos desconocidos cierran (false).
     */
    @Test
    void tienePermisoResuelveContraLaMatriz() {
        assertTrue(servicio.tienePermiso("ADMINISTRADOR", "catalogo:escribir"));
        assertTrue(servicio.tienePermiso("MEDICO", "catalogo:leer"));
        org.junit.jupiter.api.Assertions.assertFalse(servicio.tienePermiso("CELADOR", "catalogo:escribir"));
        org.junit.jupiter.api.Assertions.assertFalse(servicio.tienePermiso("ROL-INVENTADO", "catalogo:leer"));
        org.junit.jupiter.api.Assertions.assertFalse(servicio.tienePermiso("ADMINISTRADOR", "no:existe"));
    }
}
