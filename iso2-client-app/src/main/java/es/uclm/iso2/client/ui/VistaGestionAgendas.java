package es.uclm.iso2.client.ui;

import es.uclm.iso2.client.api.ApiClienteAgendas;
import es.uclm.iso2.client.api.ApiException;
import es.uclm.iso2.common.agendas.AgendaFacade.AgendaDTO;
import es.uclm.iso2.common.agendas.AgendaFacade.FestivoDTO;
import es.uclm.iso2.common.agendas.AgendaFacade.FranjaGestionDTO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Vista de gestion de agendas (CDU13, Iter3/CFG-03) para ADMINISTRADOR y
 * RECEPCIONISTA, unicos roles con {@code agenda:escribir} en la matriz RBAC (actor
 * Administrativo del CDU): alta de agenda, alta/baja de franjas de disponibilidad y
 * alta/baja de festivos.
 *
 * <p>Las reglas de negocio las aplica el servidor y sus codigos de error del
 * contrato se muestran tal cual en alertas: {@code CONFLICTO_SOLAPE} y
 * {@code CONFLICTO_FESTIVO} (409, puertas de CFG-03 "TC solapes/festivos"),
 * {@code CONFLICTO_AGENDA_EXISTENTE} (409), {@code AGENDA_NO_ENCONTRADA} (404) y
 * {@code DATOS_INVALIDOS} (400, p. ej. inicio no anterior a fin). MEDICO y TECNICO
 * ven {@code VistaConsultaAgendas} en solo lectura.</p>
 *
 * @since 0.1.3 (CFG-03)
 */
public final class VistaGestionAgendas {

    private final ApiClienteAgendas api;
    private final ObservableList<String> profesionales = FXCollections.observableArrayList();
    private final ComboBox<String> comboProfesional = new ComboBox<>(profesionales);
    private final TextField campoNuevoProfesional = new TextField();
    private final ObservableList<FranjaGestionDTO> filasFranjas = FXCollections.observableArrayList();
    private final TableView<FranjaGestionDTO> tablaFranjas = new TableView<>(filasFranjas);
    private final ObservableList<FestivoDTO> filasFestivos = FXCollections.observableArrayList();
    private final TableView<FestivoDTO> tablaFestivos = new TableView<>(filasFestivos);
    private final DatePicker pickerDiaFranja = new DatePicker();
    private final TextField campoInicio = new TextField();
    private final TextField campoFin = new TextField();
    private final DatePicker pickerDiaFestivo = new DatePicker();
    private final TextField campoMotivo = new TextField();

    /**
     * Construye la vista conectada a la API de agendas con token de rol gestor.
     *
     * @param api cliente de agendas autenticado con el token JWT de la sesion
     */
    public VistaGestionAgendas(ApiClienteAgendas api) {
        this.api = api;
        armarTablas();
    }

    /**
     * Raiz del escenario: combo de profesional + alta de agenda, tablas de franjas
     * y festivos con sus formularios de alta y botones de baja.
     *
     * @return el panel listo para poner en un {@code Scene}
     */
    public BorderPane raiz() {
        Button recargar = new Button("Recargar");
        recargar.setOnAction(e -> ejecutar(this::recargar));
        comboProfesional.valueProperty().addListener((obs, anterior, nuevo) -> seleccionar());

        Button altaAgenda = new Button("Dar de alta agenda");
        altaAgenda.setOnAction(e -> ejecutar(() -> {
            api.crearAgenda(campoNuevoProfesional.getText().trim());
            campoNuevoProfesional.clear();
            recargar();
        }));
        HBox barraAgenda = new HBox(8, new Label("Profesional:"), comboProfesional, recargar,
                new Label("Nueva agenda para:"), campoNuevoProfesional, altaAgenda);

        Button altaFranja = new Button("Anadir franja");
        altaFranja.setOnAction(e -> ejecutar(() -> {
            requireProfesional();
            api.anadirFranja(comboProfesional.getValue(), new FranjaGestionDTO(
                    null, requireFecha(pickerDiaFranja.getValue(), "nueva franja"),
                    leerHora(campoInicio), leerHora(campoFin)));
            recargar();
        }));
        Button bajaFranja = new Button("Eliminar franja seleccionada");
        bajaFranja.setOnAction(e -> ejecutar(() -> {
            FranjaGestionDTO seleccion = tablaFranjas.getSelectionModel().getSelectedItem();
            if (seleccion == null) {
                avisar("Selecciona una franja en la tabla para eliminarla.");
                return;
            }
            requireProfesional();
            api.eliminarFranja(comboProfesional.getValue(), seleccion.id());
            recargar();
        }));

        Button altaFestivo = new Button("Anadir festivo");
        altaFestivo.setOnAction(e -> ejecutar(() -> {
            requireProfesional();
            api.anadirFestivo(comboProfesional.getValue(), new FestivoDTO(
                    null, requireFecha(pickerDiaFestivo.getValue(), "nuevo festivo"),
                    campoMotivo.getText().trim()));
            recargar();
        }));
        Button bajaFestivo = new Button("Eliminar festivo seleccionado");
        bajaFestivo.setOnAction(e -> ejecutar(() -> {
            FestivoDTO seleccion = tablaFestivos.getSelectionModel().getSelectedItem();
            if (seleccion == null) {
                avisar("Selecciona un festivo en la tabla para eliminarlo.");
                return;
            }
            requireProfesional();
            api.eliminarFestivo(comboProfesional.getValue(), seleccion.id());
            recargar();
        }));

        GridPane formFranja = new GridPane();
        formFranja.setHgap(8);
        formFranja.setVgap(6);
        fila(formFranja, 0, "Dia", pickerDiaFranja);
        fila(formFranja, 1, "Inicio (HH:mm)", campoInicio);
        fila(formFranja, 2, "Fin (HH:mm)", campoFin);
        HBox accionesFranja = new HBox(8, altaFranja, bajaFranja);

        GridPane formFestivo = new GridPane();
        formFestivo.setHgap(8);
        formFestivo.setVgap(6);
        fila(formFestivo, 0, "Dia", pickerDiaFestivo);
        fila(formFestivo, 1, "Motivo", campoMotivo);
        HBox accionesFestivo = new HBox(8, altaFestivo, bajaFestivo);

        VBox centro = new VBox(10,
                new Label("Franjas de disponibilidad"), tablaFranjas,
                new HBox(20, new VBox(8, new Label("Nueva franja"), formFranja, accionesFranja),
                        new VBox(8, new Label("Nuevo festivo"), formFestivo, accionesFestivo)),
                new Label("Festivos"), tablaFestivos);
        centro.setPadding(new Insets(10, 0, 0, 0));

        BorderPane panel = new BorderPane();
        panel.setPadding(new Insets(12));
        panel.setTop(new VBox(8,
                new Label("Gestion de agendas de medicos y tecnicos"), barraAgenda));
        panel.setCenter(centro);
        ejecutar(this::recargar);
        return panel;
    }

    private void armarTablas() {
        tablaFranjas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tablaFranjas.getColumns().add(columnaFranja("Id", 70, FranjaGestionDTO::id));
        tablaFranjas.getColumns().add(columnaFranja("Dia", 120, f -> String.valueOf(f.dia())));
        tablaFranjas.getColumns().add(columnaFranja("Inicio", 90, f -> String.valueOf(f.inicio())));
        tablaFranjas.getColumns().add(columnaFranja("Fin", 90, f -> String.valueOf(f.fin())));
        tablaFranjas.setPlaceholder(new Label("El profesional no tiene franjas registradas."));

        tablaFestivos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tablaFestivos.getColumns().add(columnaFestivo("Id", 70, FestivoDTO::id));
        tablaFestivos.getColumns().add(columnaFestivo("Dia", 120, f -> String.valueOf(f.dia())));
        tablaFestivos.getColumns().add(columnaFestivo("Motivo", 240, FestivoDTO::motivo));
        tablaFestivos.setPlaceholder(new Label("El profesional no tiene festivos."));
    }

    private TableColumn<FranjaGestionDTO, Object> columnaFranja(
            String titulo, double ancho, java.util.function.Function<FranjaGestionDTO, Object> acceso) {
        TableColumn<FranjaGestionDTO, Object> c = new TableColumn<>(titulo);
        c.setPrefWidth(ancho);
        c.setCellValueFactory(dato -> new javafx.beans.property.ReadOnlyObjectWrapper<>(acceso.apply(dato.getValue())));
        return c;
    }

    private TableColumn<FestivoDTO, Object> columnaFestivo(
            String titulo, double ancho, java.util.function.Function<FestivoDTO, Object> acceso) {
        TableColumn<FestivoDTO, Object> c = new TableColumn<>(titulo);
        c.setPrefWidth(ancho);
        c.setCellValueFactory(dato -> new javafx.beans.property.ReadOnlyObjectWrapper<>(acceso.apply(dato.getValue())));
        return c;
    }

    private void fila(GridPane panel, int y, String etiqueta, javafx.scene.Node control) {
        panel.add(new Label(etiqueta), 0, y);
        panel.add(control, 1, y);
    }

    private void requireProfesional() {
        if (comboProfesional.getValue() == null) {
            throw new IllegalArgumentException(
                    "selecciona primero un profesional con agenda en el combo");
        }
    }

    private LocalDate requireFecha(LocalDate dia, String nombre) {
        if (dia == null) {
            throw new IllegalArgumentException("el formulario de " + nombre + " necesita una fecha");
        }
        return dia;
    }

    private LocalTime leerHora(TextField campo) {
        try {
            return LocalTime.parse(campo.getText().trim());
        } catch (DateTimeParseException formatoInvalido) {
            throw new IllegalArgumentException(
                    "la hora \"" + campo.getText() + "\" no es valida (formato HH:mm)");
        }
    }

    private void recargar() throws IOException, InterruptedException {
        List<AgendaDTO> agendas = api.listar();
        String seleccionActual = comboProfesional.getValue();
        profesionales.setAll(agendas.stream().map(AgendaDTO::profesionalId).sorted().toList());
        if (seleccionActual != null && profesionales.contains(seleccionActual)) {
            comboProfesional.setValue(seleccionActual);
        } else if (!profesionales.isEmpty()) {
            comboProfesional.getSelectionModel().selectFirst();
        }
        seleccionar();
    }

    private void seleccionar() {
        String profesional = comboProfesional.getValue();
        if (profesional == null) {
            filasFranjas.clear();
            filasFestivos.clear();
            return;
        }
        try {
            AgendaDTO agenda = api.detalle(profesional);
            filasFranjas.setAll(agenda.franjas());
            filasFestivos.setAll(agenda.festivos());
        } catch (ApiException error) {
            avisar(error.mensaje());
        } catch (IOException | InterruptedException error) {
            avisar("No hay conexion con el servidor: " + error.getMessage());
            if (error instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
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
