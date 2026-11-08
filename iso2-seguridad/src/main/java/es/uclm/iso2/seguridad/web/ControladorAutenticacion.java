package es.uclm.iso2.seguridad.web;

import es.uclm.iso2.common.seguridad.AutenticacionFacade.CredencialesDTO;
import es.uclm.iso2.common.seguridad.AutenticacionFacade.SesionDTO;
import es.uclm.iso2.common.shared.ApiError;
import es.uclm.iso2.seguridad.dominio.CredencialesInvalidasException;
import es.uclm.iso2.seguridad.dominio.CuentaBloqueadaException;
import es.uclm.iso2.seguridad.servicio.SeguridadServicio;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

/**
 * Endpoint publico de inicio de sesion (CDU17): {@code POST /api/auth/login} recibe
 * credenciales en claro y devuelve el JWT con su caducidad ({@link SesionDTO}).
 *
 * <p>Codigos de error (todos con cuerpo {@link ApiError}): 400 {@code DATOS_INVALIDOS}
 * si faltan usuario o contrasena; 401 {@code CREDENCIALES_INVALIDAS} si no coinciden
 * (o el usuario no existe/esta de baja); 401 {@code CUENTA_BLOQUEADA} tras
 * {@value SeguridadServicio#MAX_INTENTOS} intentos fallidos.</p>
 *
 * <p>Los manejadores son locales al controlador a proposito: {@code iso2-catalogo} ya
 * tiene su propio advice global y no debe solaparse con los errores de seguridad.</p>
 *
 * @since 1.1.0 (CFG-02)
 */
@RestController
@RequestMapping("/api/auth")
public class ControladorAutenticacion {

    private final SeguridadServicio seguridad;

    /**
     * Inyecta el servicio de autenticacion.
     *
     * @param seguridad implementacion de la fachada de seguridad
     */
    public ControladorAutenticacion(SeguridadServicio seguridad) {
        this.seguridad = seguridad;
    }

    /**
     * Inicia sesion y emite el token JWT para el cliente.
     *
     * @param credenciales usuario y contrasena
     * @return 200 con la sesion (usuario, rol, token, caducidad)
     */
    @PostMapping("/login")
    public SesionDTO login(@RequestBody(required = false) CredencialesDTO credenciales) {
        return seguridad.iniciarSesion(credenciales);
    }

    /**
     * Credenciales incorrectas: 401 sin revelar si el usuario existe.
     *
     * @param e excepcion de dominio
     * @return 401 con codigo {@code CREDENCIALES_INVALIDAS}
     */
    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<ApiError> credencialesInvalidas(CredencialesInvalidasException e) {
        return error(HttpStatus.UNAUTHORIZED, "CREDENCIALES_INVALIDAS", e.getMessage());
    }

    /**
     * Cuenta bloqueada por intentos fallidos: 401 indicando el desbloqueo.
     *
     * @param e excepcion de dominio
     * @return 401 con codigo {@code CUENTA_BLOQUEADA}
     */
    @ExceptionHandler(CuentaBloqueadaException.class)
    public ResponseEntity<ApiError> cuentaBloqueada(CuentaBloqueadaException e) {
        return error(HttpStatus.UNAUTHORIZED, "CUENTA_BLOQUEADA", e.getMessage());
    }

    /**
     * Peticion mal formada (faltan credenciales).
     *
     * @param e excepcion de validacion
     * @return 400 con codigo {@code DATOS_INVALIDOS}
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> datosInvalidos(IllegalArgumentException e) {
        return error(HttpStatus.BAD_REQUEST, "DATOS_INVALIDOS", e.getMessage());
    }

    private static ResponseEntity<ApiError> error(HttpStatus estado, String codigo, String mensaje) {
        return ResponseEntity.status(estado).body(new ApiError(codigo, mensaje, Instant.now()));
    }
}
