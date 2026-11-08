package es.uclm.iso2.seguridad.servicio;

import es.uclm.iso2.common.seguridad.AutenticacionFacade;
import es.uclm.iso2.common.seguridad.AutenticacionFacade.CredencialesDTO;
import es.uclm.iso2.common.seguridad.AutenticacionFacade.SesionDTO;
import es.uclm.iso2.seguridad.dominio.CredencialesInvalidasException;
import es.uclm.iso2.seguridad.dominio.CuentaBloqueadaException;
import es.uclm.iso2.seguridad.dominio.MatrizPermisos;
import es.uclm.iso2.seguridad.dominio.Permiso;
import es.uclm.iso2.seguridad.dominio.Rol;
import es.uclm.iso2.seguridad.dominio.Usuario;
import es.uclm.iso2.seguridad.repositorio.UsuarioRepositorio;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

/**
 * Autenticacion local y RBAC de CDU17 (RF17): implementa la fachada congelada
 * {@link AutenticacionFacade} de {@code iso2-common}.
 *
 * <p>Reglas encarnadas:</p>
 * <ul>
 *   <li>Verificacion BCrypt de la contrasena contra el hash persistido.</li>
 *   <li>Bloqueo temporal: tras {@value #MAX_INTENTOS} intentos fallidos seguidos, la
 *       cuenta queda bloqueada {@value #MINUTOS_BLOQUEO} minutos (mitigacion de fuerza
 *       bruta). Un inicio con exito reinicia el contador.</li>
 *   <li>Emision de JWT con el rol unico via {@link JwtUtils}.</li>
 *   <li>{@link #tienePermiso(String, String)} resuelve contra {@link MatrizPermisos}.</li>
 * </ul>
 *
 * @since 1.1.0 (CFG-02)
 */
@Service
public class SeguridadServicio implements AutenticacionFacade {

    /** Intentos fallidos permitidos antes de bloquear la cuenta. */
    public static final int MAX_INTENTOS = 5;

    /** Duracion del bloqueo temporal por exceso de intentos. */
    public static final long MINUTOS_BLOQUEO = 15;

    private final UsuarioRepositorio usuarios;
    private final PasswordEncoder codificador;
    private final JwtUtils jwt;

    /**
     * Inyecta repositorio de usuarios, codificador BCrypt y emisor de tokens.
     *
     * @param usuarios    repositorio JPA de usuarios
     * @param codificador codificador BCrypt (bean en la aplicacion servidora)
     * @param jwt         emisor/verificador de JWT
     */
    public SeguridadServicio(UsuarioRepositorio usuarios, PasswordEncoder codificador, JwtUtils jwt) {
        this.usuarios = usuarios;
        this.codificador = codificador;
        this.jwt = jwt;
    }

    /**
     * Inicio de sesion local: comprueba bloqueo, actividad, hash BCrypt y emite token.
     *
     * <p>El identificador se compara de forma exacta (asi lo pactan los datos semilla
     * y el alta de usuarios de los proximos CDU).</p>
     *
     * @param credenciales usuario y contrasena en claro solo durante la peticion
     * @return sesion con JWT firmado y su caducidad
     * @throws CredencialesInvalidasException si el usuario no existe, esta inactivo o la
     *                                        contrasena no coincide (suma intento)
     * @throws CuentaBloqueadaException       si la cuenta esta bloqueada por intentos
     */
    @Override
    // noRollbackFor: el incremento de intentos fallidos y el bloqueo que lo acompana
    // DEBEN persistirse aunque el metodo termine lanzando CredencialesInvalidasException
    // (RuntimeException). Sin esto, el rollback dejaria el contador a 0 y la cuenta
    // jamas se bloquearia (se demostro en la prueba de integracion de server-app).
    @Transactional(noRollbackFor = CredencialesInvalidasException.class)
    public SesionDTO iniciarSesion(CredencialesDTO credenciales) {
        if (credenciales == null || esVacio(credenciales.usuario()) || esVacio(credenciales.password())) {
            throw new IllegalArgumentException("Usuario y contrasena son obligatorios");
        }
        Usuario usuario = usuarios.findByUsuario(credenciales.usuario())
                .orElseThrow(CredencialesInvalidasException::new);
        if (usuario.estaBloqueado()) {
            throw new CuentaBloqueadaException(usuario.getBloqueadoHasta());
        }
        if (!usuario.isActivo()) {
            throw new CredencialesInvalidasException();
        }
        if (!codificador.matches(credenciales.password(), usuario.getPasswordHash())) {
            usuario.registrarIntentoFallido(MAX_INTENTOS, Duration.ofMinutes(MINUTOS_BLOQUEO));
            throw new CredencialesInvalidasException();
        }
        usuario.reiniciarIntentos();
        Rol rol = usuario.rolEnum().orElseThrow(() -> new IllegalStateException(
                "El usuario " + usuario.getUsuario() + " tiene un rol desconocido en la base de datos"));
        return jwt.generar(usuario.getUsuario(), rol);
    }

    /**
     * RBAC: comprueba si un rol posee un permiso segun la matriz autoritativa.
     * Los codigos desconocidos se resuelven como "sin permiso" (cierre por defecto).
     *
     * @param rol          nombre del rol (claim del JWT)
     * @param codigoPermiso codigo estable del permiso ({@code dominio:accion})
     * @return true si el permiso esta concedido al rol
     */
    @Override
    public boolean tienePermiso(String rol, String codigoPermiso) {
        return Rol.desdeCodigo(rol)
                .flatMap(r -> Permiso.desdeCodigo(codigoPermiso)
                        .map(p -> MatrizPermisos.tienePermiso(r, p)))
                .orElse(false);
    }

    private static boolean esVacio(String valor) {
        return valor == null || valor.isBlank();
    }
}
