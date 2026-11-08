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
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List;

/**
 * Vista de solo lectura de las agendas (CDU13, Iter3/CFG-03) para MEDICO y TECNICO,
 * que tienen {@code agenda:leer} pero no {@code agenda:escribir} en la matriz RBAC:
 * combo de profesional + tablas de franjas y festivos, sin formulario.
 *
 * <p>La restriccion real la impone el servidor (403 RBAC); esta vista evita que esos
 * roles vean controles que no pueden usar. ADMINISTRADOR y RECEPCIONISTA ven en
 * cambio {@code VistaGestionAgendas}, y el CELADOR no ve la seccion (CDU13: el
 * celador no participa en la gestion de agendas).</p>
 *
 * @since 0.1.3 (CFG-03)
 */
public final class VistaConsultaAgendas {

    private final ApiClienteAgendas api;
    private final ObservableList<String> profesionales = FXCollections.observableArrayList();
    private final ComboBox<String> comboProfesional = new ComboBox<>(profesionales);
    private final ObservableList<FranjaGestionDTO> filasFranjas = FXCollections.observableArrayList();
    private final TableView<FranjaGestionDTO> tablaFranjas = new TableView<>(filasFranjas);
    private final ObservableList<FestivoDTO> filasFestivos = FXCollections.observableArrayList();
    private final TableView<FestivoDTO> tablaFestivos = new TableView<>(filasFestivos);

    /**
     * Construye la vista conectada a la API de agendas (token de rol de lectura:
     * MEDICO o TECNICO).
     *
     * @param api cliente de agendas autenticado con el token JWT de la sesion
     */
    public VistaConsultaAgendas(ApiClienteAgendas api) {
        this.api = api;
        armarTablas();
    }

    /**
     * Raiz del escenario: cabecera con combo y recargar + dos tablas.
     *
     * @return el panel listo para poner en un {@code Scene}
     */
    public BorderPane raiz() {
        Button recargar = new Button("Recargar");
        recargar.setOnAction(e -> recargar());
        comboProfesional.valueProperty().addListener((obs, anterior, nuevo) -> seleccionar());

        BorderPane panel = new BorderPane();
        panel.setPadding(new Insets(12));
        panel.setTop(new VBox(8,
                new Label("Agendas de medicos y tecnicos (solo lectura)"),
                new HBox(8, new Label("Profesional:"), comboProfesional, recargar)));
        panel.setCenter(new VBox(10,
                new Label("Franjas de disponibilidad"), tablaFranjas,
                new Label("Festivos"), tablaFestivos));
        recargar();
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

    private void recargar() {
        try {
            List<AgendaDTO> agendas = api.listar();
            String seleccionActual = comboProfesional.getValue();
            profesionales.setAll(agendas.stream().map(AgendaDTO::profesionalId).sorted().toList());
            if (seleccionActual != null && profesionales.contains(seleccionActual)) {
                comboProfesional.setValue(seleccionActual);
            } else if (!profesionales.isEmpty()) {
                comboProfesional.getSelectionModel().selectFirst();
            }
            seleccionar();
        } catch (ApiException error) {
            avisar(error.mensaje());
        } catch (IOException | InterruptedException error) {
            avisar("No hay conexion con el servidor: " + error.getMessage());
            if (error instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
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

    private void avisar(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR, mensaje);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }
}
