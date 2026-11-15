package es.uclm.iso2.integracion.configuracion;

import es.uclm.iso2.integracion.dominio.AuditoriaIntegracion;
import es.uclm.iso2.integracion.dominio.RegistroSimulacion;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Ensamblado del spike CDU19: enlaza {@link IntegracionProperties} (sin
 * {@code @ConfigurationPropertiesScan} en el proyecto) y crea los dos beans de dominio
 * compartidos. Los cuatro adaptadores son {@code @Component} y el servicio es
 * {@code @Service}: el escaneo de {@code es.uclm.iso2} de la aplicacion servidora los
 * recoge sin ayuda. El spike no persiste, asi que no hay beans de datos.
 *
 * @since 1.1.0 (CFG-06, CDU19)
 */
@Configuration
@EnableConfigurationProperties(IntegracionProperties.class)
public class IntegracionConfiguracion {

    /**
     * Registro vivo de politicas de simulacion, arrancado desde la configuracion.
     *
     * @param propiedades configuracion {@code iso2.integracion.*}
     * @return el registro compartido por adaptadores y controlador
     */
    @Bean
    public RegistroSimulacion registroSimulacion(IntegracionProperties propiedades) {
        return new RegistroSimulacion(propiedades.politicasIniciales());
    }

    /**
     * Cadena de auditoria del subdominio, en memoria (el spike no anade tabla ni
     * migracion Flyway).
     *
     * @return la auditoria compartida por servicio y controlador de simulacion
     */
    @Bean
    public AuditoriaIntegracion auditoriaIntegracion() {
        return new AuditoriaIntegracion();
    }
}
