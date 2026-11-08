package es.uclm.iso2.client.ui;

import es.uclm.iso2.client.api.ApiClienteSolicitudes;
import es.uclm.iso2.client.api.ApiException;
import es.uclm.iso2.common.solicitudes.Prioridad;
import es.uclm.iso2.common.solicitudes.SolicitudFacade.SolicitudDTO;
import es.uclm.iso2.common.solicitudes.SolicitudFacade.SolicitudPrescripcionDTO;
import es.uclm.iso2.common.solicitudes.SolicitudFacade.TrazabilidadSolicitudDTO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List;

/**
 * Vista de prescripcion de solicitudes (CDU06, Iter4/CFG-04) para ADMINISTRADOR y
 * MEDICO, unicos roles con {@code solicitud:escribir} en la matriz RBAC (actor Medico
 * del CDU): alta de la solicitud con los campos obligatorios de la RF06.1 y historial
 * de lo prescrito por el usuario autenticado.
 *
 * <p>Las reglas las aplica el servidor y sus codigos de contrato se muestran tal cual
 * en alertas: {@code CONFLICTO_SOLICITUD_DUPLICADA} (409, TC duplicados de CFG-04:
 * misma prueba al mismo paciente mientras la anterior esta activa),
 * {@code CONFLICTO_PRUEBA_NO_DISPONIBLE} (409, la prueba debe estar activa en el
 * catalogo de CDU16) y {@code DATOS_INVALIDOS} (400). La vista no puede saltarse esas
 * reglas: solo evita el trabajo evidente, el autorizar es siempre el servidor.</p>
 *
 * <p>No repudio (RF06.2): el autor no se pregunta en el formulario, es el usuario de la
 * sesion, y {@code ApiClienteSolicitudes} no lo manda en el cuerpo: lo toma el
 * servidor del claim del token. El boton de no repudio muestra el autor, el instante,
 * la huella SHA-256 y el resultado de recalcularla sobre lo guardado.</p>
 *
 * <p>RECEPCIONISTA ve {@code VistaConsultaSolicitudes} en solo lectura; TECNICO y
 * CELADOR no ven la seccion (sin {@code solicitud:leer}).</p>
 *
 * @since 0.1.4 (CFG-04)
 */
public final class VistaSolicitudesMedico {

    private final ApiClienteSolicitudes api;
    private final String usuarioSesion;
    private final ObservableList<SolicitudDTO> filas = FXCollections.observableArrayList();
    private final TableView<SolicitudDTO> tabla = new TableView<>(filas);
    private final TextField campoPaciente = new TextField();
    private final TextField campoPrueba = new TextField();
    private final TextField campoIndicacion = new TextField();
    private final ObservableList<Prioridad> prioridades =
            FXCollections.observableArrayList(Prioridad.values());
    private final ComboBox<Prioridad> comboPrioridad = new ComboBox<>(prioridades);
    private final ObservableList<String> origenes =
            FXCollections.observableArrayList("AP", "HOSPITAL", "URGENCIAS", "ENFERMERIA");
    private final ComboBox<String> comboOrigen = new ComboBox<>(origenes);

    /**
     * Construye la vista conectada a la API de solicitudes con token de rol medico.
     *
     * @param api          cliente de solicitudes autenticado con el token JWT de la sesion
     * @param usuarioSesion usuario del claim JWT, solo para validar RF06.1 en el cliente:
     *                      el servidor fija el autor de la prescripcion con su propio claim
     */
    public VistaSolicitudesMedico(ApiClienteSolicitudes api, String usuarioSesion) {
        this.api = api;
        this.usuarioSesion = usuarioSesion;
        this.comboPrioridad.setValue(Prioridad.ORDINARIA);
        this.comboOrigen.setValue("AP");
        armarTabla();
    }

    /**
     * Raiz del escenario: formulario de prescripcion, botonera y tabla con el
     * historial del medico autenticado.
     *
     * @return el panel listo para poner en un {@code Scene}
     */
    public BorderPane raiz() {
        Button prescribir = new Button("Prescribir prueba");
        prescribir.setOnAction(e -> ejecutar(this::prescribir));
        Button recargar = new Button("Recargar historial");
        recargar.setOnAction(e -> ejecutar(this::recargar));
        Button verNoRepudio = new Button("Ver no repudio de la seleccion");
        verNoRepudio.setOnAction(e -> ejecutar(this::verNoRepudio));

        GridPane formulario = new GridPane();
        formulario.setHgap(8);
        formulario.setVgap(6);
        fila(formulario, 0, "Paciente (historia clinica)", campoPaciente);
        fila(formulario, 1, "Codigo de prueba", campoPrueba);
        fila(formulario, 2, "Indicacion clinica", campoIndicacion);
        fila(formulario, 3, "Prioridad", comboPrioridad);
        fila(formulario, 4, "Origen", comboOrigen);
        campoPrueba.setPromptText("p. ej. SEM-RX-TORAX (ver seccion Catalogo)");
        campoIndicacion.setPromptText("motivo clinico de la peticion");

        Button limpiar = new Button("Limpiar");
        limpiar.setOnAction(e -> {
            campoPaciente.clear();
            campoPrueba.clear();
            campoIndicacion.clear();
            comboPrioridad.setValue(Prioridad.ORDINARIA);
            comboOrigen.setValue("AP");
        });
        HBox acciones = new HBox(8, prescribir, limpiar, recargar, verNoRepudio);

        BorderPane panel = new BorderPane();
        panel.setPadding(new Insets(12));
        panel.setTop(new VBox(8,
                new Label("Prescripcion de solicitudes de prueba"),
                new Label("El autor es la sesion (" + usuarioSesion + "): queda sellado en la huella de no repudio."),
                formulario));
        panel.setCenter(new VBox(10, new Label("Solicitudes prescritas por mi"), acciones, tabla));
        ejecutar(this::recargar);
        return panel;
    }

    private void armarTabla() {
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tabla.getColumns().add(columna("Solicitud", 300, SolicitudDTO::solicitudId));
        tabla.getColumns().add(columna("Paciente", 150, SolicitudDTO::pacienteId));
        tabla.getColumns().add(columna("Prueba", 170, SolicitudDTO::codigoPrueba));
        tabla.getColumns().add(columna("Fecha", 110, s -> String.valueOf(s.fechaSolicitada())));
        tabla.getColumns().add(columna("Estado", 100, SolicitudDTO::estado));
        tabla.setPlaceholder(new Label("Aun no has prescrito ninguna solicitud."));
    }

    private TableColumn<SolicitudDTO, Object> columna(
            String titulo, double ancho, java.util.function.Function<SolicitudDTO, Object> acceso) {
        TableColumn<SolicitudDTO, Object> c = new TableColumn<>(titulo);
        c.setPrefWidth(ancho);
        c.setCellValueFactory(dato -> new javafx.beans.property.ReadOnlyObjectWrapper<>(acceso.apply(dato.getValue())));
        return c;
    }

    private void fila(GridPane panel, int y, String etiqueta, javafx.scene.Node control) {
        panel.add(new Label(etiqueta), 0, y);
        panel.add(control, 1, y);
    }

    private void prescribir() throws IOException, InterruptedException {
        // El medicoId del DTO se rellena con la sesion para aplicar la validacion de
        // RF06.1 del contrato aqui, en el cliente; en el cuerpo HTTP no viaja (RF06.2).
        api.prescribir(new SolicitudPrescripcionDTO(
                campoPaciente.getText(), campoPrueba.getText(), campoIndicacion.getText(),
                comboPrioridad.getValue(), comboOrigen.getValue(), usuarioSesion));
        campoPaciente.clear();
        campoPrueba.clear();
        campoIndicacion.clear();
        recargar();
    }

    private void recargar() throws IOException, InterruptedException {
        List<SolicitudDTO> mias = api.misSolicitudes();
        filas.setAll(mias);
    }

    private void verNoRepudio() throws IOException, InterruptedException {
        SolicitudDTO seleccion = tabla.getSelectionModel().getSelectedItem();
        if (seleccion == null) {
            avisar("Selecciona una solicitud en la tabla para ver su trazabilidad.");
            return;
        }
        TrazabilidadSolicitudDTO traza = api.integridad(seleccion.solicitudId());
        if (traza.integridadOk()) {
            Alert detalle = new Alert(Alert.AlertType.INFORMATION,
                    ("Autor: %s%nCreada: %s%nHuella SHA-256: %s%nIntegridad verificada: SI"
                            .formatted(traza.autor(), traza.creadaEn(), traza.huella())));
            detalle.setHeaderText("La trazabilidad de la solicitud es integra");
            detalle.showAndWait();
        } else {
            Alert manipulada = new Alert(Alert.AlertType.ERROR,
                    ("Autor declarado: %s%nCreada: %s%nHuella guardada: %s%nIntegridad verificada: NO"
                            .formatted(traza.autor(), traza.creadaEn(), traza.huella())));
            manipulada.setHeaderText("ALERTA: el contenido de la solicitud no corresponde con su huella"
                    + " (posible manipulacion directa en la base de datos)");
            manipulada.showAndWait();
        }
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
