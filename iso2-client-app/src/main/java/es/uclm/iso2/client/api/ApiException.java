package es.uclm.iso2.client.api;

/**
 * Excepcion de la capa API del cliente: representa un error devuelto por el servidor
 * (400/404/409 etc.) con el cuerpo {@code ApiError} del contrato comun ya traducido.
 */
public class ApiException extends RuntimeException {

    private final int status;
    private final String codigo;
    private final String mensaje;

    /**
     * Construye el error de API.
     *
     * @param status  codigo HTTP devuelto por el servidor
     * @param codigo  codigo de error del contrato comun (ej. {@code DATOS_INVALIDOS})
     * @param mensaje mensaje legible para el usuario
     */
    public ApiException(int status, String codigo, String mensaje) {
        super(codigo + " (" + status + "): " + mensaje);
        this.status = status;
        this.codigo = codigo;
        this.mensaje = mensaje;
    }

    /**
     * Codigo HTTP de la respuesta que genero el error.
     *
     * @return el estado HTTP
     */
    public int status() {
        return status;
    }

    /**
     * Codigo de error del contrato comun.
     *
     * @return el codigo (ej. {@code PRUEBA_NO_ENCONTRADA})
     */
    public String codigo() {
        return codigo;
    }

    /**
     * Mensaje legible original del servidor (sin el prefijo {@code CODIGO (HTTP):}),
     * pensado para mostrar directamente en alertas de la interfaz (CDU13, CFG-03).
     *
     * @return el mensaje del cuerpo {@code ApiError}
     * @since 0.1.3 (CFG-03)
     */
    public String mensaje() {
        return mensaje;
    }
}
