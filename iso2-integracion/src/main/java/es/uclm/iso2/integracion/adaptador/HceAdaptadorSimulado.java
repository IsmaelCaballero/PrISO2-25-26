package es.uclm.iso2.integracion.adaptador;

import es.uclm.iso2.common.integracion.HistoriaNoEncontradaException;
import es.uclm.iso2.integracion.dominio.HistoriaExterna;
import es.uclm.iso2.integracion.dominio.RegistroSimulacion;
import es.uclm.iso2.integracion.dominio.RegistroHistoria;
import es.uclm.iso2.integracion.dominio.SistemaExterno;
import es.uclm.iso2.integracion.puerto.HcePort;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Component;

/**
 * Adaptador simulado de la HCE externa. Dos pacientes fijos para poder probar la
 * idempotencia de la sincronizacion:
 *
 * <ul>
 *   <li>{@code api-paciente-ok}: historia estable, segunda sincronizacion
 *       {@code reutilizado=true}.</li>
 *   <li>{@code api-paciente-rotativo}: alterna dos contenidos, cada sincronizacion
 *       aporta huella nueva ({@code reutilizado=false}).</li>
 * </ul>
 *
 * Los demas pacientes no existen en el HCE y responden 404. Los ids se recortan antes
 * de buscarlos, la leccion de CDU12 con los ids de hueco.
 *
 * @since 1.1.0 (CFG-06, CDU19)
 */
@Component
public class HceAdaptadorSimulado extends AdaptadorSimulado implements HcePort {

    private static final List<RegistroHistoria> HISTORIA_OK = List.of(
            new RegistroHistoria("ENF-HTA", LocalDate.of(2024, 3, 12), "Hipertension arterial diagnostica"),
            new RegistroHistoria("ALG-PENI", LocalDate.of(2023, 11, 2), "Alergia a penicilina"));

    private static final List<RegistroHistoria> ROTATIVO_A = List.of(
            new RegistroHistoria("ENF-DII", LocalDate.of(2025, 1, 20), "Diabetes tipo II en control"));

    private static final List<RegistroHistoria> ROTATIVO_B = List.of(
            new RegistroHistoria("ENF-DII", LocalDate.of(2025, 1, 20), "Diabetes tipo II en control"),
            new RegistroHistoria("INF-RAD", LocalDate.of(2025, 6, 5), "Informe de radiografia de torax"));

    private final AtomicInteger rotador = new AtomicInteger();

    /**
     * Registra el adaptador de la historia clinica externa sobre el sistema HCE.
     *
     * @param registro registro compartido de politicas de simulacion
     */
    public HceAdaptadorSimulado(RegistroSimulacion registro) {
        super(SistemaExterno.HCE, registro);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HistoriaExterna buscarHistoria(String pacienteExternoId) {
        return proteger(() -> {
            String id = recortar(pacienteExternoId);
            if (id.isEmpty()) {
                throw new IllegalArgumentException("el identificador de paciente es obligatorio");
            }
            return switch (id) {
                case "api-paciente-ok" -> new HistoriaExterna(id, HISTORIA_OK);
                case "api-paciente-rotativo" -> new HistoriaExterna(id,
                        rotador.getAndIncrement() % 2 == 0 ? ROTATIVO_A : ROTATIVO_B);
                default -> throw new HistoriaNoEncontradaException(
                        "el paciente " + id + " no existe en la HCE externa");
            };
        });
    }
}
