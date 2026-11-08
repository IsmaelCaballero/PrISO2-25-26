package es.uclm.iso2.solicitudes;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

/**
 * Configuracion minima de arranque para las pruebas {@code @DataJpaTest} del modulo
 * (el modulo de dominio no tiene clase {@code @SpringBootApplication}: esa vive en
 * {@code iso2-server-app}).
 */
@SpringBootConfiguration
@EnableAutoConfiguration
class ConfigPruebasJpa {
}
