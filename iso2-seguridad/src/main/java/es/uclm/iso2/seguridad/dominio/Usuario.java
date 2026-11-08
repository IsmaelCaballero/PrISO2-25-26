package es.uclm.iso2.seguridad.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Duration;
import java.time.Instant;

/**
 * Entidad JPA del usuario con credenciales locales (CDU17, tabla {@code seguridad_usuario}).
 *
 * <p>Reglas de negocio encarnadas (RF17):</p>
 * <ul>
 *   <li>La contrasena <strong>nunca</strong> se guarda en claro: solo el hash BCrypt
 *       ({@code passwordHash}). La comprobacion la hace el servicio con el encoder.</li>
 *   <li>Bloqueo temporal por intentos fallidos (mitigacion de fuerza bruta): tras un
 *       maximo de intentos, la cuenta queda bloqueada hasta una fecha; al expirar el
 *       bloqueo o al autenticarse con exito se reinicia el contador.</li>
 *   <li>Baja logica ({@code activo = false}): un usuario de baja no puede iniciar
 *       sesion pero conserva su historico de auditoria.</li>
 * </ul>
 *
 * @since 1.1.0 (CFG-02)
 */
@Entity
@Table(name = "seguridad_usuario",
        uniqueConstraints = @UniqueConstraint(name = "uk_seguridad_usuario_usuario", columnNames = "usuario"))
public class Usuario {

    /** Clave tecnica sustituta; el negocio se referencia por {@code usuario}. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre de inicio de sesion, unico. */
    @Column(nullable = false, length = 60)
    private String usuario;

    /** Hash BCrypt de la contrasena; jamas la contrasena en claro. */
    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    /** Rol unico del usuario (nombre de {@link Rol}); el acceso al dominio RBAC se hace via {@link Rol}. */
    @Column(nullable = false, length = 20)
    private String rol;

    /** Baja logica: un usuario inactivo no puede iniciar sesion. */
    @Column(nullable = false)
    private boolean activo;

    /** Intentos fallidos acumulados desde el ultimo exito o desbloqueo. */
    @Column(name = "intentos_fallidos", nullable = false)
    private int intentosFallidos;

    /** Momento hasta el que la cuenta esta bloqueada (nulo si no lo esta). */
    @Column(name = "bloqueado_hasta")
    private Instant bloqueadoHasta;

    /**
     * Constructor JPA sin argumentos (requerido por Hibernate).
     */
    protected Usuario() {
    }

    /**
     * Crea un usuario con credenciales ya hasheadas y sin bloqueos.
     *
     * @param usuario      nombre de inicio de sesion
     * @param passwordHash hash BCrypt de la contrasena
     * @param rol          rol unico del usuario
     * @param activo       si la cuenta esta dada de alta y habilitada
     */
    public Usuario(String usuario, String passwordHash, Rol rol, boolean activo) {
        this.usuario = usuario;
        this.passwordHash = passwordHash;
        this.rol = rol.name();
        this.activo = activo;
        this.intentosFallidos = 0;
    }

    /**
     * Registra un intento fallido: si se alcanza el maximo, bloquea la cuenta
     * durante la duracion indicada.
     *
     * @param maxIntentos intentos permitidos antes de bloquear
     * @param duracion    cuanto se bloquea la cuenta al superar los intentos
     */
    public void registrarIntentoFallido(int maxIntentos, Duration duracion) {
        intentosFallidos++;
        if (intentosFallidos >= maxIntentos) {
            bloqueadoHasta = Instant.now().plus(duracion);
        }
    }

    /** Reinicia el contador de intentos y levanta cualquier bloqueo. */
    public void reiniciarIntentos() {
        intentosFallidos = 0;
        bloqueadoHasta = null;
    }

    /**
     * @return true si la cuenta esta bloqueada en este momento
     */
    public boolean estaBloqueado() {
        return bloqueadoHasta != null && Instant.now().isBefore(bloqueadoHasta);
    }

    /**
     * Rol del usuario como enum de dominio.
     *
     * @return el rol; vacio si el valor persistido no es un rol conocido
     */
    public java.util.Optional<Rol> rolEnum() {
        return Rol.desdeCodigo(rol);
    }

    /** @return clave tecnica sustituta */
    public Long getId() {
        return id;
    }

    /** @return nombre de inicio de sesion */
    public String getUsuario() {
        return usuario;
    }

    /** @return hash BCrypt almacenado */
    public String getPasswordHash() {
        return passwordHash;
    }

    /** @return codigo del rol tal y como esta persistido */
    public String getRol() {
        return rol;
    }

    /** @return true si la cuenta esta habilitada */
    public boolean isActivo() {
        return activo;
    }

    /** @return intentos fallidos acumulados */
    public int getIntentosFallidos() {
        return intentosFallidos;
    }

    /** @return momento de desbloqueo, o nulo si no hay bloqueo */
    public Instant getBloqueadoHasta() {
        return bloqueadoHasta;
    }
}
