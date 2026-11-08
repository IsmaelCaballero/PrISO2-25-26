package es.uclm.iso2.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de arranque de la aplicacion servidora (Iter0/CFG-00, v0.1.0).
 *
 * <p>En Iter0 solo arranca el caparazon: Spring Boot + H2 + JPA sin endpoints.
 * Los controladores y servicios de cada dominio se anadiran con su CDU
 * (ver mapa en docs/planificacion-iteraciones.csv).</p>
 */
@SpringBootApplication
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
