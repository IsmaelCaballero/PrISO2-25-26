package es.uclm.iso2.common.shared;

/**
 * Parametros de paginacion para las operaciones de lista (baseline 1.0.0).
 *
 * @param pagina numero de pagina, empezando en 0
 * @param tamano tamano de pagina (1..200)
 * @param orden  campo de ordenacion, puede ser nulo para el orden por defecto
 */
public record Paginacion(int pagina, int tamano, String orden) {

    /**
     * Construye la paginacion validando los limites pactados en el contrato.
     *
     * @throws IllegalArgumentException si la pagina es negativa o el tamano esta fuera de 1..200
     */
    public Paginacion {
        if (pagina < 0) {
            throw new IllegalArgumentException("la pagina no puede ser negativa");
        }
        if (tamano < 1 || tamano > 200) {
            throw new IllegalArgumentException("el tamano de pagina debe estar entre 1 y 200");
        }
    }
}