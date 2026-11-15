package es.uclm.iso2.integracion.adaptador;

import es.uclm.iso2.common.integracion.EstudioNoEncontradoException;
import es.uclm.iso2.integracion.dominio.EstudioExterno;
import es.uclm.iso2.integracion.dominio.RegistroSimulacion;
import es.uclm.iso2.integracion.dominio.SistemaExterno;
import es.uclm.iso2.integracion.puerto.RisPort;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Adaptador simulado del RIS. Mantiene cuatro estudios de ejemplo
 * ({@code ACC-0001} a {@code ACC-0004}) que dan respuesta al
 * {@code importarEstudio} del baseline 1.0.0; cualquier otro numero de acceso
 * responde 404.
 *
 * @since 1.1.0 (CFG-06, CDU19)
 */
@Component
public class RisAdaptadorSimulado extends AdaptadorSimulado implements RisPort {

    private final Map<String, EstudioExterno> estudios = new LinkedHashMap<>();

    /**
     * Registra el adaptador del RIS sobre el sistema RIS y siembra los estudios.
     *
     * @param registro registro compartido de politicas de simulacion
     */
    public RisAdaptadorSimulado(RegistroSimulacion registro) {
        super(SistemaExterno.RIS, registro);
        anadirEstudio(new EstudioExterno("ACC-0001", "HC-1001", LocalDate.of(2025, 9, 14), 3));
        anadirEstudio(new EstudioExterno("ACC-0002", "HC-1002", LocalDate.of(2025, 10, 2), 1));
        anadirEstudio(new EstudioExterno("ACC-0003", "HC-1001", LocalDate.of(2025, 10, 20), 5));
        anadirEstudio(new EstudioExterno("ACC-0004", "HC-2007", LocalDate.of(2025, 11, 8), 2));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EstudioExterno buscarEstudio(String accessionNumber) {
        return proteger(() -> {
            String id = recortar(accessionNumber);
            if (id.isEmpty()) {
                throw new IllegalArgumentException("el numero de acceso es obligatorio");
            }
            EstudioExterno estudio = estudios.get(id.toUpperCase(java.util.Locale.ROOT));
            if (estudio == null) {
                throw new EstudioNoEncontradoException("el estudio " + id + " no existe en el RIS");
            }
            return estudio;
        });
    }

    private void anadirEstudio(EstudioExterno estudio) {
        estudios.put(estudio.accessionNumber(), estudio);
    }
}
