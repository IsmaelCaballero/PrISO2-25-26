package es.uclm.iso2.integracion.configuracion;

import es.uclm.iso2.integracion.dominio.ModoSimulacion;
import es.uclm.iso2.integracion.dominio.PoliticaSimulacion;
import es.uclm.iso2.integracion.dominio.SistemaExterno;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuracion externa del spike CDU19 bajo el prefijo {@code iso2.integracion}:
 * politica inicial de todos los sistemas y repas por sistema
 * ({@code iso2.integracion.sistemas.hce.modo=CAIDO}...). Se enlaza con
 * {@code @EnableConfigurationProperties} desde {@link IntegracionConfiguracion}; el
 * proyecto no usa {@code @ConfigurationPropertiesScan}.
 *
 * <p>Valores por defecto del spike: NORMAL, latencia de 20 ms, tiempo de espera de
 * 800 ms y cero reintentos. Las politicas de simulacion se cambian en caliente por
 * API, asi que la configuracion fija solo el punto de partida del arranque.</p>
 *
 * @param modo       modo inicial de todos los sistemas
 * @param latenciaMs latencia simulada inicial en milisegundos
 * @param timeoutMs  tiempo de espera inicial en milisegundos
 * @param reintentos reintentos iniciales por llamada
 * @param sistemas   repas por sistema (clave en minusculas: sso, hce, ris, pacs)
 * @since 1.1.0 (CFG-06, CDU19)
 */
@ConfigurationProperties(prefix = "iso2.integracion")
public record IntegracionProperties(String modo, Long latenciaMs, Long timeoutMs,
                                    Integer reintentos, Map<String, SistemaOverride> sistemas) {

    /** Modo por defecto del arranque. */
    public static final String MODO_DEFECTO = "NORMAL";

    /** Latencia por defecto del arranque en milisegundos. */
    public static final long LATENCIA_DEFECTO_MS = 20;

    /** Tiempo de espera por defecto del arranque en milisegundos. */
    public static final long TIMEOUT_DEFECTO_MS = 800;

    /**
     * Normaliza los valores ausentes y la lista de repas.
     */
    public IntegracionProperties {
        modo = modo == null || modo.isBlank() ? MODO_DEFECTO : modo;
        latenciaMs = latenciaMs == null ? LATENCIA_DEFECTO_MS : latenciaMs;
        timeoutMs = timeoutMs == null ? TIMEOUT_DEFECTO_MS : timeoutMs;
        reintentos = reintentos == null ? 0 : reintentos;
        sistemas = sistemas == null ? Map.of() : Map.copyOf(sistemas);
    }

    /**
     * Politica inicial de un sistema: los valores generales repasados por los del
     * sistema si los tiene.
     *
     * @param sistema sistema a configurar en el arranque
     * @return su politica inicial
     */
    public PoliticaSimulacion politicaDe(SistemaExterno sistema) {
        SistemaOverride override = sistemas.get(sistema.codigo());
        String modoEfectivo = override != null && override.modo() != null ? override.modo() : modo;
        long latenciaEfectiva = override != null && override.latenciaMs() != null
                ? override.latenciaMs() : latenciaMs;
        long timeoutEfectivo = override != null && override.timeoutMs() != null
                ? override.timeoutMs() : timeoutMs;
        int reintentosEfectivos = override != null && override.reintentos() != null
                ? override.reintentos() : reintentos;
        ModoSimulacion modoSimulacion;
        try {
            modoSimulacion = ModoSimulacion.valueOf(modoEfectivo.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                    "iso2.integracion: modo de simulacion desconocido '" + modoEfectivo + "'", e);
        }
        return new PoliticaSimulacion(modoSimulacion, latenciaEfectiva, timeoutEfectivo, reintentosEfectivos);
    }

    /**
     * Mapa completo de politicas iniciales para el {@code RegistroSimulacion}.
     *
     * @return la politica inicial de los cuatro sistemas
     */
    public Map<SistemaExterno, PoliticaSimulacion> politicasIniciales() {
        Map<SistemaExterno, PoliticaSimulacion> iniciales = new EnumMap<>(SistemaExterno.class);
        for (SistemaExterno sistema : SistemaExterno.values()) {
            iniciales.put(sistema, politicaDe(sistema));
        }
        return new LinkedHashMap<>(iniciales);
    }

    /**
     * Repaso de la politica general para un sistema concreto. Los campos sin valor
     * heredan el valor general.
     *
     * @param modo       modo para este sistema
     * @param latenciaMs latencia para este sistema
     * @param timeoutMs  tiempo de espera para este sistema
     * @param reintentos reintentos para este sistema
     */
    public record SistemaOverride(String modo, Long latenciaMs, Long timeoutMs, Integer reintentos) {
    }
}
