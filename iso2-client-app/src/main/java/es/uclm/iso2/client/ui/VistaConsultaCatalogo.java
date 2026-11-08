package es.uclm.iso2.client.ui;

import es.uclm.iso2.client.api.ApiClienteCatalogo;
import es.uclm.iso2.client.api.ApiException;
import es.uclm.iso2.common.catalogo.PruebaCatalogoFacade.PruebaImagenDTO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List;

/**
 * Vista de solo lectura del catalogo para roles no administradores (CDU17,
 * Iter2/CFG-02): tabla de pruebas + boton de recargar, sin formulario ni botones
 * de escritura.
 *
 * <p>La restriccion real la impone el servidor (403 RBAC); esta vista evita ademas
 * que MEDICO/TECNICO/CELADOR/RECEPCIONISTA vean controles que no pueden usar.
 * Los administradores siguen viendo {@code VistaAdminCatalogo}.</p>
 *
 * @since 0.1.2 (CFG-02)
 */
public final class VistaConsultaCatalogo {

    private final ApiClienteCatalogo api;
    private final ObservableList<PruebaImagenDTO> filas = FXCollections.observableArrayList();
    private final TableView<PruebaImagenDTO> tabla = new TableView<>(filas);

    /**
     * Construye la vista conectada a la API del catalogo (token de cualquier rol:
     * la lectura esta abierta a todos los roles autenticados).
     *
     * @param api cliente del catalogo autenticado con el token JWT de la sesion
     */
    public VistaConsultaCatalogo(ApiClienteCatalogo api) {
        this.api = api;
        armarTabla();
    }

    /**
     * Raiz del escenario: cabecera con recargar + tabla.
     *
     * @return el panel listo para poner en un {@code Scene}
     */
    public BorderPane raiz() {
        Button recargar = new Button("Recargar");
        recargar.setOnAction(e -> recargar());

        BorderPane panel = new BorderPane();
        panel.setPadding(new Insets(12));
        panel.setTop(new VBox(8,
                new Label("Catalogo de pruebas (solo lectura)"),
                new HBox(8, recargar)));
        panel.setCenter(tabla);
        recargar();
        return panel;
    }

    private void armarTabla() {
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tabla.getColumns().add(columna("Codigo", 140, PruebaImagenDTO::codigo));
        tabla.getColumns().add(columna("Nombre", 220, PruebaImagenDTO::nombre));
        tabla.getColumns().add(columna("Categoria", 90, PruebaImagenDTO::categoria));
        tabla.getColumns().add(columna("Duracion (min)", 110, PruebaImagenDTO::duracionMinutos));
        tabla.getColumns().add(columna("Cita previa", 90, PruebaImagenDTO::requiereCita));
        tabla.getColumns().add(columna("Activa", 70, PruebaImagenDTO::activa));
    }

    private TableColumn<PruebaImagenDTO, Object> columna(
            String titulo, double ancho, java.util.function.Function<PruebaImagenDTO, Object> acceso) {
        TableColumn<PruebaImagenDTO, Object> c = new TableColumn<>(titulo);
        c.setPrefWidth(ancho);
        c.setCellValueFactory(dato -> new javafx.beans.property.ReadOnlyObjectWrapper<>(acceso.apply(dato.getValue())));
        return c;
    }

    private void recargar() {
        try {
            List<PruebaImagenDTO> pruebas = api.listar(0, 200);
            filas.setAll(pruebas);
        } catch (ApiException error) {
            avisar(error.getMessage());
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
}
