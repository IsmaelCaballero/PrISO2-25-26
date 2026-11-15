package es.uclm.iso2.integracion.dominio;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registro vivo de las politicas de simulacion de los cuatro sistemas externos. Los
 * adaptadores lo consultan en cada llamada y la API lo cambia por dentro
 * ({@code POST /api/integracion/simulacion}) para poder ejecutar los TCs de modo
 * degradado sin reiniciar el servidor.
 *
 * @since 1.1.0 (CFG-06, CDU19)
 */
public class RegistroSimulacion {

    private final Map<SistemaExterno, PoliticaSimulacion> politicas = new ConcurrentHashMap<>();

    /**
     * Arranca con una politica inicial por sistema.
     *
     * @param iniciales politica inicial de cada sistema externo
     */
    public RegistroSimulacion(Map<SistemaExterno, PoliticaSimulacion> iniciales) {
        if (iniciales != null) {
            politicas.putAll(iniciales);
        }
    }

    /**
     * Politica vigente de un sistema.
     *
     * @param sistema sistema externo consultado
     * @return la politica activa, normal por defecto si el sistema no tenia entrada
     */
    public PoliticaSimulacion de(SistemaExterno sistema) {
        return politicas.getOrDefault(sistema, PoliticaSimulacion.normal(0, 50, 0));
    }

    /**
     * Instala una politica nueva para un sistema. A partir de la siguiente llamada el
     * adaptador se comporta como diga la politica.
     *
     * @param sistema  sistema externo a configurar
     * @param politica politica que queda vigente
     */
    public void cambiar(SistemaExterno sistema, PoliticaSimulacion politica) {
        if (sistema == null || politica == null) {
            throw new IllegalArgumentException("sistema y politica son obligatorios");
        }
        politicas.put(sistema, politica);
    }

    /**
     * Copia inmutable del estado actual, en el orden SSO, HCE, RIS, PACS.
     *
     * @return las cuatro politicas vigentes
     */
    public Map<SistemaExterno, PoliticaSimulacion> todas() {
        Map<SistemaExterno, PoliticaSimulacion> copia = new LinkedHashMap<>();
        for (SistemaExterno sistema : SistemaExterno.values()) {
            copia.put(sistema, de(sistema));
        }
        return Map.copyOf(copia);
    }
}
