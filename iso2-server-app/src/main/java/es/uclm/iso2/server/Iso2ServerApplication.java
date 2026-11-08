package es.uclm.iso2.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Punto de arranque de la aplicacion servidora (release train 0.1.x, D22).
 *
 * <p>Desde CFG-01 (Iter1/CDU16) la aplicacion escanea todo el arbol
 * {@code es.uclm.iso2} para ensamblar los modulos de dominio: trajo el catalogo de
 * pruebas (entidades, repositorios JPA, servicio y controlador REST). Las entidades y
 * repositorios de los modulos de dominio se descubren con {@link EntityScan} y
 * {@link EnableJpaRepositories} porque viven fuera del paquete de esta clase.</p>
 *
 * <p>El esquema de base de datos lo versiona Flyway ({@code db/migration}); Hibernate no
 * lo crea ni lo valida ({@code ddl-auto: none}).</p>
 */
@SpringBootApplication(scanBasePackages = "es.uclm.iso2")
@EntityScan(basePackages = "es.uclm.iso2")
@EnableJpaRepositories(basePackages = "es.uclm.iso2")
public class Iso2ServerApplication {

    /**
     * Constructor estandar requerido por el contenedor de Spring.
     */
    public Iso2ServerApplication() {
    }

    /**
     * Inicia el servidor web embebido con la base H2 en fichero.
     *
     * @param args argumentos de linea de comandos (puerto etc.)
     */
    public static void main(String[] args) {
        SpringApplication.run(Iso2ServerApplication.class, args);
    }
}
