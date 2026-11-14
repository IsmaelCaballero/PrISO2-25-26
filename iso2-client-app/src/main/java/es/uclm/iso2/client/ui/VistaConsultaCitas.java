package es.uclm.iso2.client.ui;

import es.uclm.iso2.client.api.ApiClienteCitas;
import es.uclm.iso2.client.api.ApiException;
import es.uclm.iso2.common.citas.CitaFacade.DetalleCitaDTO;
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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Vista de consulta de citas (CDU12, Iter5/CFG-05) en solo lectura para el MEDICO,
 * rol con {@code cita:leer} pero sin {@code cita:escribir} en la matriz RBAC: ve la
 * cita que se le ha agendado (filtro por profesional/estado/fechas + detalle con el
 * motivo de cancelacion), pero no agenda, confirma, reprograma ni cancela: eso lo
 * hace la RECEPCIONISTA desde {@code VistaGestionCitas} (UC12.x, actor
 * Administrativo del PD).
 *
 * <p>La restriccion real la impone el servidor (403 RBAC en las acciones de
 * escritura); esta vista evita que el medico vea controles que no puede usar. El
 * ADMINISTRADOR ve la vista de gestion en todas las secciones (mismo patron que
 * catalogo/agendas/solicitudes) y TECNICO/CELADOR no ven la seccion porque no
 * tienen {@code cita:leer}.</p>
 *
 * @since 0.1.5 (CFG-05)
 */
public final class VistaConsultaCitas {

    /** Estados de la maquina de citas para el filtro (UC12.8). */
    private static final ObservableList<String> ESTADOS = FXCollections.observableArrayList(
            "", "programada", "confirmada", "realizada", "cancelada", "expirada");

    private final ApiClienteCitas api;
    private final ComboBox<String> comboEstado = new ComboBox<>(ESTADOS);
    private final DatePicker pickerDesde = new DatePicker();
    private final DatePicker pickerHasta = new DatePicker();
    private final ObservableList<DetalleCitaDTO> filas = FXCollections.observableArrayList();
    private final TableView<DetalleCitaDTO> tabla = new TableView<>(filas);
    private final Label detalle = new Label("Selecciona una cita para ver su detalle.");

    /**
     * Construye la vista conectada a la API de citas con token de rol de lectura.
     *
     * @param api cliente de citas autenticado con el token JWT de la sesion
     */
    public VistaConsultaCitas(ApiClienteCitas api) {
        this.api = api;
        comboEstado.setValue("");
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tabla.getColumns().add(columna("Cita", 180, DetalleCitaDTO::citaId));
        tabla.getColumns().add(columna("Solicitud", 150,
                c -> c.solicitudId() == null ? "(directa)" : c.solicitudId()));
        tabla.getColumns().add(columna("Profesional", 110, DetalleCitaDTO::profesionalId));
        tabla.getColumns().add(columna("Inicio", 140, c -> String.valueOf(c.inicio())));
        tabla.getColumns().add(columna("Fin", 140, c -> String.valueOf(c.fin())));
        tabla.getColumns().add(columna("Estado", 100, DetalleCitaDTO::estado));
        tabla.setPlaceholder(new Label("Sin citas para ese filtro."));
        tabla.getSelectionModel().selectedItemProperty()
                .addListener((obs, anterior, seleccionada) -> mostrarDetalle(seleccionada));
    }

    /**
     * Raiz del escenario: barra de filtros + tabla + etiqueta de detalle
     * (motivo de cancelacion incluido, RF12.5).
     *
     * @return el panel listo para poner en un {@code Scene}
     */
    public BorderPane raiz() {
        Button filtrar = new Button("Filtrar citas");
        filtrar.setOnAction(e -> aplicarFiltro());

        BorderPane panel = new BorderPane();
        panel.setPadding(new Insets(12));
        panel.setTop(new VBox(8,
                new Label("Citas (solo lectura)"),
                new HBox(8, new Label("Estado:"), comboEstado,
                        new Label("Desde:"), pickerDesde,
                        new Label("Hasta:"), pickerHasta, filtrar)));
        panel.setCenter(new VBox(10, tabla, detalle));
        aplicarFiltro();
        return panel;
    }

    private TableColumn<DetalleCitaDTO, Object> columna(
            String titulo, double ancho, java.util.function.Function<DetalleCitaDTO, Object> acceso) {
        TableColumn<DetalleCitaDTO, Object> c = new TableColumn<>(titulo);
        c.setPrefWidth(ancho);
        c.setCellValueFactory(dato -> new javafx.beans.property.ReadOnlyObjectWrapper<>(acceso.apply(dato.getValue())));
        return c;
    }

    private void aplicarFiltro() {
        LocalDateTime desde = pickerDesde.getValue() == null
                ? null : pickerDesde.getValue().atStartOfDay();
        LocalDateTime hasta = pickerHasta.getValue() == null
                ? null : pickerHasta.getValue().atTime(LocalTime.MAX);
        try {
            List<DetalleCitaDTO> filtradas = api.listar(desde, hasta, null, comboEstado.getValue());
            filas.setAll(filtradas);
        } catch (ApiException error) {
            avisar(error.mensaje());
        } catch (IOException | InterruptedException error) {
            avisar("No hay conexion con el servidor: " + error.getMessage());
            if (error instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * El detalle por API es el unico que trae el motivo de cancelacion (el listado
     * baseline no lo incluye), por eso se pide al seleccionar la fila.
     */
    private void mostrarDetalle(DetalleCitaDTO seleccionada) {
        if (seleccionada == null) {
            detalle.setText("Selecciona una cita para ver su detalle.");
            return;
        }
        try {
            DetalleCitaDTO completo = api.detalle(seleccionada.citaId());
            detalle.setText("Cita " + completo.citaId() + " [" + completo.estado() + "] "
                    + completo.inicio() + " - " + completo.fin()
                    + " con " + completo.profesionalId()
                    + (completo.motivoCancelacion() == null
                    ? "" : " | motivo de cancelacion: " + completo.motivoCancelacion()));
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
}
