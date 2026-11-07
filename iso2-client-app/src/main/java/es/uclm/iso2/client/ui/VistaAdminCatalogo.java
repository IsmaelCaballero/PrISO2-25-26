package es.uclm.iso2.client.ui;

import es.uclm.iso2.client.api.ApiClienteCatalogo;
import es.uclm.iso2.client.api.ApiException;
import es.uclm.iso2.common.catalogo.PruebaCatalogoFacade.PoliticaDTO;
import es.uclm.iso2.common.catalogo.PruebaCatalogoFacade.PruebaImagenDTO;
import es.uclm.iso2.common.catalogo.PruebaCatalogoFacade.ProtocoloDTO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;

/**
 * Vista de administrador para el catalogo de pruebas (CDU16, Iter1/CFG-01).
 *
 * <p>Tabla con las pruebas del servidor + formulario de alta/modificacion + botones
 * de baja logica y reactivacion. Toda la logica de negocio esta en el servidor; esta
 * vista solo recoge datos, llama a {@link ApiClienteCatalogo} y muestra el resultado
 * (incluidos los errores de contrato via {@link ApiException}).</p>
 */
public final class VistaAdminCatalogo {

    private static final String[] CATEGORIAS = {"RX", "TAC", "RM", "ECO", "MED-NUC", "DENS", "OTRO"};

    private final ApiClienteCatalogo api;
    private final ObservableList<PruebaImagenDTO> filas = FXCollections.observableArrayList();

    private final TableView<PruebaImagenDTO> tabla = new TableView<>(filas);
    private final TextField campoCodigo = new TextField();
    private final TextField campoNombre = new TextField();
    private final ComboBox<String> comboCategoria = new ComboBox<>(FXCollections.observableArrayList(CATEGORIAS));
    private final CheckBox checkCita = new CheckBox("requiere cita previa");
    private final Spinner<Integer> spinnerDuracion = new Spinner<>();
    private final TextArea areaPreparacion = new TextArea();
    private final Spinner<Integer> spinnerAyuno = new Spinner<>();
    private final TextArea areaContraindicaciones = new TextArea();
    private final Spinner<Integer> spinnerPlazo = new Spinner<>();
    private final Spinner<Integer> spinnerMaxCancel = new Spinner<>();

    /**
     * Construye la vista conectada a la API del servidor.
     *
     * @param api cliente del catalogo ya autenticado con rol ADMIN
     */
    public VistaAdminCatalogo(ApiClienteCatalogo api) {
        this.api = api;
        this.spinnerDuracion.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 600, 30));
        this.spinnerAyuno.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 72, 0));
        this.spinnerPlazo.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 720, 24));
        this.spinnerMaxCancel.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 20, 3));
        this.comboCategoria.getSelectionModel().selectFirst();
        armarTabla();
    }

    /**
     * Raiz del escenario: tabla a la izquierda, formulario y acciones a la derecha.
     *
     * @return el panel listo para poner en un {@code Scene}
     */
    public BorderPane raiz() {
        BorderPane panel = new BorderPane();
        panel.setPadding(new Insets(12));
        panel.setCenter(tabla);
        panel.setRight(formulario());
        ejecutar(this::recargar);
        return panel;
    }

    private void armarTabla() {
        tabla.setPrefWidth(640);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tabla.getColumns().add(columna("Codigo", 140, p -> p.codigo()));
        tabla.getColumns().add(columna("Nombre", 200, p -> p.nombre()));
        tabla.getColumns().add(columna("Categoria", 90, p -> p.categoria()));
        tabla.getColumns().add(columna("Duracion (min)", 110, PruebaImagenDTO::duracionMinutos));
        tabla.getColumns().add(columna("Activa", 70, PruebaImagenDTO::activa));
    }

    private TableColumn<PruebaImagenDTO, Object> columna(
            String titulo, double ancho, java.util.function.Function<PruebaImagenDTO, Object> acceso) {
        TableColumn<PruebaImagenDTO, Object> c = new TableColumn<>(titulo);
        c.setPrefWidth(ancho);
        c.setCellValueFactory(dato -> new javafx.beans.property.ReadOnlyObjectWrapper<>(acceso.apply(dato.getValue())));
        return c;
    }

    private VBox formulario() {
        GridPane datos = new GridPane();
        datos.setHgap(8);
        datos.setVgap(6);
        datos.setPadding(new Insets(0, 0, 0, 12));
        fila(datos, 0, "Codigo", campoCodigo);
        fila(datos, 1, "Nombre", campoNombre);
        fila(datos, 2, "Categoria", comboCategoria);
        datos.add(checkCita, 1, 3);
        fila(datos, 4, "Duracion (min)", spinnerDuracion);
        fila(datos, 5, "Preparacion", areaPreparacion);
        fila(datos, 6, "Ayuno (h)", spinnerAyuno);
        fila(datos, 7, "Contraindicac.", areaContraindicaciones);
        fila(datos, 8, "Plazo cancel. (h)", spinnerPlazo);
        fila(datos, 9, "Max. cancel.", spinnerMaxCancel);

        Button alta = new Button("Alta");
        alta.setOnAction(e -> ejecutar(this::crear));
        Button modificar = new Button("Modificar");
        modificar.setOnAction(e -> ejecutar(() -> {
            PruebaImagenDTO seleccion = tabla.getSelectionModel().getSelectedItem();
            if (seleccion == null) {
                avisar("Selecciona una prueba en la tabla para modificarla.");
                return;
            }
            api.actualizar(seleccion.codigo(), leeraFormulario(seleccion.codigo()));
        }));
        Button baja = new Button("Baja logica");
        baja.setOnAction(e -> ejecutar(() -> {
            PruebaImagenDTO seleccion = tabla.getSelectionModel().getSelectedItem();
            if (seleccion == null) {
                avisar("Selecciona una prueba en la tabla para darla de baja.");
                return;
            }
            api.desactivar(seleccion.codigo());
        }));
        Button reactivar = new Button("Reactivar");
        reactivar.setOnAction(e -> ejecutar(() -> {
            PruebaImagenDTO seleccion = tabla.getSelectionModel().getSelectedItem();
            if (seleccion == null) {
                avisar("Selecciona una prueba en la tabla para reactivarla.");
                return;
            }
            api.reactivar(seleccion.codigo());
        }));
        Button recargar = new Button("Recargar");
        recargar.setOnAction(e -> ejecutar(this::recargar));

        HBox acciones = new HBox(8, alta, modificar, baja, reactivar, recargar);
        acciones.setPadding(new Insets(10, 0, 0, 12));
        return new VBox(8, new Label("Nueva prueba / modificacion"), datos, acciones);
    }

    private void fila(GridPane panel, int y, String etiqueta, javafx.scene.Node control) {
        panel.add(new Label(etiqueta), 0, y);
        panel.add(control, 1, y);
    }

    private PruebaImagenDTO leeraFormulario(String codigo) {
        return PruebaImagenDTO.sinEstado(
                codigo,
                campoNombre.getText(),
                comboCategoria.getValue(),
                checkCita.isSelected(),
                spinnerDuracion.getValue(),
                new ProtocoloDTO(areaPreparacion.getText(), spinnerAyuno.getValue(),
                        areaContraindicaciones.getText()),
                new PoliticaDTO(spinnerPlazo.getValue(), spinnerMaxCancel.getValue()));
    }

    private void crear() throws IOException, InterruptedException {
        PruebaImagenDTO altaReal = api.crear(leeraFormulario(campoCodigo.getText().trim()));
        campoCodigo.setText(altaReal.codigo());
    }

    private void recargar() throws IOException, InterruptedException {
        filas.setAll(api.listar(0, 200));
    }

    private void ejecutar(Accion accion) {
        try {
            accion.ejecutar();
            recargar();
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
