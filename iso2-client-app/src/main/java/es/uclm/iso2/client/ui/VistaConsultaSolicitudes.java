package es.uclm.iso2.client.ui;

import es.uclm.iso2.client.api.ApiClienteSolicitudes;
import es.uclm.iso2.client.api.ApiException;
import es.uclm.iso2.common.solicitudes.SolicitudFacade.SolicitudDTO;
import es.uclm.iso2.common.solicitudes.SolicitudFacade.TrazabilidadSolicitudDTO;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;

/**
 * Vista de consulta de solicitudes (CDU06, Iter4/CFG-04) en solo lectura para la
 * RECEPCIONISTA, rol con {@code solicitud:leer} pero sin {@code solicitud:escribir}:
 * no prescribe, solo localiza una solicitud por su identificador y comprueba su
 * trazabilidad de no repudio (RF06.2) antes de citar al paciente.
 *
 * <p>El identificador (UUID) lo facilita el propio medico o el mostrador de admision:
 * no existe en el contrato una busqueda por paciente, y no se inventa en el cliente:
 * la autorizacion y el alcance de los datos los decide el servidor. Un id inexistente
 * se muestra con el codigo estable {@code SOLICITUD_NO_ENCONTRADA} (404) del contrato.</p>
 *
 * @since 0.1.4 (CFG-04)
 */
public final class VistaConsultaSolicitudes {

    private final ApiClienteSolicitudes api;
    private final TextField campoIdSolicitud = new TextField();
    private final Label etiquetaSolicitud = new Label("Sin solicitud seleccionada.");
    private final Label etiquetaTrazabilidad = new Label("Sin verificar.");

    /**
     * Construye la vista conectada a la API de solicitudes con token de recepcion.
     *
     * @param api cliente de solicitudes autenticado con el token JWT de la sesion
     */
    public VistaConsultaSolicitudes(ApiClienteSolicitudes api) {
        this.api = api;
    }

    /**
     * Raiz del escenario: buscador por identificador, detalle de la solicitud y boton
     * de verificacion de la huella.
     *
     * @return el panel listo para poner en un {@code Scene}
     */
    public BorderPane raiz() {
        Button buscar = new Button("Buscar solicitud");
        buscar.setOnAction(e -> ejecutar(this::buscar));
        Button verificar = new Button("Verificar no repudio");
        verificar.setOnAction(e -> ejecutar(this::verificar));
        campoIdSolicitud.setPromptText("identificador (UUID) de la solicitud");
        campoIdSolicitud.setOnAction(e -> ejecutar(this::buscar));

        GridPane datos = new GridPane();
        datos.setHgap(8);
        datos.setVgap(6);
        datos.add(new Label("Solicitud:"), 0, 0);
        datos.add(etiquetaSolicitud, 1, 0);
        datos.add(new Label("No repudio:"), 0, 1);
        datos.add(etiquetaTrazabilidad, 1, 1);

        BorderPane panel = new BorderPane();
        panel.setPadding(new Insets(12));
        panel.setTop(new VBox(8,
                new Label("Consulta de solicitudes de prueba (solo lectura)"),
                new HBox(8, new Label("Identificador:"), campoIdSolicitud, buscar, verificar)));
        panel.setCenter(datos);
        return panel;
    }

    private void buscar() throws IOException, InterruptedException {
        String id = requireId();
        SolicitudDTO solicitud = api.detalle(id);
        etiquetaSolicitud.setText("%s | paciente %s | prueba %s | %s | estado %s".formatted(
                solicitud.solicitudId(), solicitud.pacienteId(), solicitud.codigoPrueba(),
                solicitud.fechaSolicitada(), solicitud.estado()));
        etiquetaTrazabilidad.setText("sin verificar");
    }

    private void verificar() throws IOException, InterruptedException {
        String id = requireId();
        TrazabilidadSolicitudDTO traza = api.integridad(id);
        etiquetaTrazabilidad.setText(traza.integridadOk()
                ? "autor %s, creada %s, huella integra".formatted(traza.autor(), traza.creadaEn())
                : "AUTORIDAD DE DATOS EN RIESGO: la huella no coincide");
        if (!traza.integridadOk()) {
            Alert manipulada = new Alert(Alert.AlertType.ERROR,
                    ("Huella guardada: %s%nRecalculada sobre lo guardado: no coincide"
                            .formatted(traza.huella())));
            manipulada.setHeaderText("La solicitud " + traza.solicitudId()
                    + " fue modificada fuera de la aplicacion");
            manipulada.showAndWait();
        }
    }

    private String requireId() {
        String id = campoIdSolicitud.getText() == null ? "" : campoIdSolicitud.getText().trim();
        if (id.isEmpty()) {
            throw new IllegalArgumentException("escribe el identificador (UUID) de la solicitud");
        }
        return id;
    }

    private void ejecutar(Accion accion) {
        try {
            accion.ejecutar();
        } catch (IllegalArgumentException datosInvalidos) {
            avisar(datosInvalidos.getMessage());
        } catch (ApiException error) {
            avisar(error.mensaje());
        } catch (IOException | InterruptedException error) {
            avisar("No hay conexion con el servidor: " + error.getMessage());
            if (error instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void avisar(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR, mensaje);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }

    /**
     * Accion contra la API (puede lanzar IOException/InterruptedException).
     */
    @FunctionalInterface
    private interface Accion {
        /**
         * Ejecuta la llamada a la API.
         *
         * @throws IOException          si el servidor no responde
         * @throws InterruptedException si se interrumpe la espera
         */
        void ejecutar() throws IOException, InterruptedException;
    }
}
