package es.uclm.iso2.integracion.dominio;

import java.util.Optional;

/**
 * Sistemas externos que el CDU19 integra y monitoriza. El codigo en minusculas es el
 * que viaja por la API ({@code /api/integracion/sistemas/{sistema}/ping}) y el que
 * aparece en los tokens de setup de la matriz TDOP.
 *
 * @since 1.1.0 (CFG-06, CDU19)
 */
public enum SistemaExterno {

    /** Proveedor de identidad (SSO). */
    SSO,
    /** Historia clinica electronica externa. */
    HCE,
    /** Sistema de informacion de radiologia. */
    RIS,
    /** Archivo y comunicacion de imagenes. */
    PACS;

    /**
     * Codigo estable del sistema en minusculas.
     *
     * @return el codigo usado por la API y por los setups TDOP
     */
    public String codigo() {
        return name().toLowerCase();
    }

    /**
     * Resuelve el sistema desde su codigo, tolerando mayusculas y espacios.
     *
     * @param codigo codigo recibido por la API
     * @return el sistema, vacio si el codigo no es conocido
     */
    public static Optional<SistemaExterno> desdeCodigo(String codigo) {
        if (codigo == null) {
            return Optional.empty();
        }
        String normalizado = codigo.trim().toLowerCase();
        for (SistemaExterno sistema : values()) {
            if (sistema.codigo().equals(normalizado)) {
                return Optional.of(sistema);
            }
        }
        return Optional.empty();
    }
}
