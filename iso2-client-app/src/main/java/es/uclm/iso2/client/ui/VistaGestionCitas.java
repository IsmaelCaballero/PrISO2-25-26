package es.uclm.iso2.client.ui;

import es.uclm.iso2.client.api.ApiClienteAgendas;
import es.uclm.iso2.client.api.ApiClienteCitas;
import es.uclm.iso2.client.api.ApiException;
import es.uclm.iso2.common.citas.CitaFacade.CitaNuevaDTO;
import es.uclm.iso2.common.citas.CitaFacade.DetalleCitaDTO;
import es.uclm.iso2.common.citas.CitaFacade.HuecoDTO;
import es.uclm.iso2.common.agendas.AgendaFacade.AgendaDTO;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Vista de gestion de citas (CDU12, Iter5/CFG-05) para ADMINISTRADOR y
 * RECEPCIONISTA, unicos roles con {@code cita:escribir} en la matriz RBAC (actor
 * Administrativo del CDU12): motor de huecos libres (UC12.2), agendar sobre un
 * hueco (UC12.1/UC12.2), confirmar (UC12.3), reprogramar (UC12.4), cancelar con
 * motivo (UC12.5) y realizar (UC12.6). El MEDICO y el TECNICO no llegan aqui: el
 * medico ve {@code VistaConsultaCitas} en solo lectura y el tecnico no tiene
 * {@code cita:leer}.
 *
 * <p>Las reglas de negocio las aplica el servidor y sus codigos de error del
 * contrato se muestran tal cual en alertas: {@code CONFLICTO_HUECO_OCUPADO} (409,
 * el duelo anti-concurrencia de RF12 resuelto por la unique de la D36: el segundo
 * usuario que picha el mismo hueco se lleva el aviso y tiene que elegir otro),
 * {@code CONFLICTO_FUERA_DE_FRANJA} (409, D36), {@code NO_APTA_PARA_CITA} (409,
 * la solicitud no esta en {@code creada}), {@code TRANSICION_INVALIDA} (409),
 * {@code PLAZO_CANCELACION} (409) y {@code DATOS_INVALIDOS} (400). No hay borrado:
 * una cita se cancela con motivo y el hueco se libera solo.</p>
 *
 * @since 0.1.5 (CFG-05)
 */
public final class VistaGestionCitas {

    /** Estados de la maquina de citas para el filtro (UC12.8). */
    private static final ObservableList<String> ESTADOS = FXCollections.observableArrayList(
            "", "programada", "confirmada", "realizada", "cancelada", "expirada");

    private final ApiClienteCitas citas;
    private final ApiClienteAgendas agendas;
    private final ObservableList<String> profesionales = FXCollections.observableArrayList();
    private final ComboBox<String> comboProfesional = new ComboBox<>(profesionales);
    private final ComboBox<String> comboEstado = new ComboBox<>(ESTADOS);
    private final DatePicker pickerDesdeFiltro = new DatePicker();
    private final DatePicker pickerHastaFiltro = new DatePicker();
    private final ObservableList<DetalleCitaDTO> filasCitas = FXCollections.observableArrayList();
    private final TableView<DetalleCitaDTO> tablaCitas = new TableView<>(filasCitas);
    private final DatePicker pickerNuevoInicio = new DatePicker();
    private final TextField campoHoraNueva = new TextField();
    private final TextField campoMotivo = new TextField();
    private final DatePicker pickerHuecoDesde = new DatePicker();
    private final DatePicker pickerHuecoHasta = new DatePicker();
    private final TextField campoDuracion = new TextField();
    private final ObservableList<HuecoDTO> filasHuecos = FXCollections.observableArrayList();
    private final TableView<HuecoDTO> tablaHuecos = new TableView<>(filasHuecos);
    private final TextField campoSolicitud = new TextField();

    /**
     * Construye la vista conectada a las APIs de citas y agendas con token de rol
     * gestor (ADMINISTRADOR o RECEPCIONISTA).
     *
     * @param citas   cliente de citas autenticado con el token JWT de la sesion
     * @param agendas cliente de agendas para el combo de profesionales con agenda
     */
    public VistaGestionCitas(ApiClienteCitas citas, ApiClienteAgendas agendas) {
        this.citas = citas;
        this.agendas = agendas;
        armarTablas();
    }

    /**
     * Raiz del escenario: barra de filtros y acciones sobre la cita seleccionada
     * (confirmar, realizar, reprogramar, cancelar) y buscador de huecos libres con
     * su formulario de agendado.
     *
     * @return el panel listo para poner en un {@code Scene}
     */
    public BorderPane raiz() {
        Button recargar = new Button("Recargar profesionales");
        recargar.setOnAction(e -> ejecutar(this::recargar));
        Button filtrar = new Button("Filtrar citas");
        filtrar.setOnAction(e -> ejecutar(this::aplicarFiltro));

        HBox barraFiltros = new HBox(8,
                new Label("Profesional:"), comboProfesional,
                new Label("Estado:"), comboEstado,
                new Label("Desde:"), pickerDesdeFiltro,
                new Label("Hasta:"), pickerHastaFiltro,
                filtrar, recargar);

        Button confirmar = new Button("Confirmar");
        confirmar.setOnAction(e -> ejecutar(() -> {
            DetalleCitaDTO cita = requireCita("confirmar");
            citas.confirmar(cita.citaId());
            aplicarFiltro();
        }));
        Button realizar = new Button("Realizar");
        realizar.setOnAction(e -> ejecutar(() -> {
            DetalleCitaDTO cita = requireCita("realizar");
            citas.realizar(cita.citaId());
            aplicarFiltro();
        }));
        Button reprogramar = new Button("Reprogramar a...");
        reprogramar.setOnAction(e -> ejecutar(() -> {
            DetalleCitaDTO cita = requireCita("reprogramar");
            citas.reprogramar(cita.citaId(), leerFechaHora(pickerNuevoInicio.getValue(), campoHoraNueva));
            aplicarFiltro();
        }));
        Button cancelar = new Button("Cancelar con motivo");
        cancelar.setOnAction(e -> ejecutar(() -> {
            DetalleCitaDTO cita = requireCita("cancelar");
            String motivo = campoMotivo.getText() == null ? "" : campoMotivo.getText().trim();
            if (motivo.isEmpty()) {
                throw new IllegalArgumentException(
                        "cancelar exige motivo (RF12.5): escribelo en el campo Motivo");
            }
            citas.cancelar(cita.citaId(), motivo);
            campoMotivo.clear();
            aplicarFiltro();
        }));
        GridPane formAccion = new GridPane();
        formAccion.setHgap(8);
        formAccion.setVgap(6);
        fila(formAccion, 0, "Nueva fecha", pickerNuevoInicio);
        fila(formAccion, 1, "Nueva hora (HH:mm)", campoHoraNueva);
        fila(formAccion, 2, "Motivo cancelacion", campoMotivo);
        HBox accionesCita = new HBox(8, confirmar, realizar, reprogramar, cancelar);

        Button buscarHuecos = new Button("Buscar huecos libres");
        buscarHuecos.setOnAction(e -> ejecutar(this::buscarHuecos));
        Button agendar = new Button("Agendar en hueco seleccionado");
        agendar.setOnAction(e -> ejecutar(this::agendarEnHueco));
        GridPane formHuecos = new GridPane();
        formHuecos.setHgap(8);
        formHuecos.setVgap(6);
        fila(formHuecos, 0, "Desde", pickerHuecoDesde);
        fila(formHuecos, 1, "Hasta", pickerHuecoHasta);
        fila(formHuecos, 2, "Duracion (min)", campoDuracion);
        fila(formHuecos, 3, "Solicitud (vacio = cita directa)", campoSolicitud);
        HBox accionesHuecos = new HBox(8, buscarHuecos, agendar);

        VBox centro = new VBox(10,
                new Label("Citas (selecciona una para operar)"), tablaCitas,
                new HBox(20, new VBox(8, new Label("Acciones sobre la cita seleccionada"),
                        formAccion, accionesCita),
                        new VBox(8, new Label("Huecos libres del profesional del combo"),
                                formHuecos, accionesHuecos, tablaHuecos)));
        centro.setPadding(new Insets(10, 0, 0, 0));

        BorderPane panel = new BorderPane();
        panel.setPadding(new Insets(12));
        panel.setTop(new VBox(8,
                new Label("Gestion de citas (recepcion)"), barraFiltros));
        panel.setCenter(centro);
        ejecutar(this::recargar);
        return panel;
    }

    private void armarTablas() {
        comboEstado.setValue("");

        tablaCitas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tablaCitas.getColumns().add(columnaCita("Cita", 180, DetalleCitaDTO::citaId));
        tablaCitas.getColumns().add(columnaCita("Solicitud", 150,
                c -> c.solicitudId() == null ? "(directa)" : c.solicitudId()));
        tablaCitas.getColumns().add(columnaCita("Profesional", 110, DetalleCitaDTO::profesionalId));
        tablaCitas.getColumns().add(columnaCita("Inicio", 140, c -> String.valueOf(c.inicio())));
        tablaCitas.getColumns().add(columnaCita("Fin", 140, c -> String.valueOf(c.fin())));
        tablaCitas.getColumns().add(columnaCita("Estado", 100, DetalleCitaDTO::estado));
        tablaCitas.setPlaceholder(new Label("Sin citas para ese filtro."));

        tablaHuecos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tablaHuecos.getColumns().add(columnaHueco("Inicio", 140, h -> String.valueOf(h.inicio())));
        tablaHuecos.getColumns().add(columnaHueco("Fin", 140, h -> String.valueOf(h.fin())));
        tablaHuecos.getColumns().add(columnaHueco("Minutos libres", 100,
                h -> h.duracionMinutos() == null ? "-" : h.duracionMinutos()));
        tablaHuecos.setPlaceholder(new Label("Busca huecos libres para el profesional del combo."));
    }

    private TableColumn<DetalleCitaDTO, Object> columnaCita(
            String titulo, double ancho, java.util.function.Function<DetalleCitaDTO, Object> acceso) {
        TableColumn<DetalleCitaDTO, Object> c = new TableColumn<>(titulo);
        c.setPrefWidth(ancho);
        c.setCellValueFactory(dato -> new javafx.beans.property.ReadOnlyObjectWrapper<>(acceso.apply(dato.getValue())));
        return c;
    }

    private TableColumn<HuecoDTO, Object> columnaHueco(
            String titulo, double ancho, java.util.function.Function<HuecoDTO, Object> acceso) {
        TableColumn<HuecoDTO, Object> c = new TableColumn<>(titulo);
        c.setPrefWidth(ancho);
        c.setCellValueFactory(dato -> new javafx.beans.property.ReadOnlyObjectWrapper<>(acceso.apply(dato.getValue())));
        return c;
    }

    private void fila(GridPane panel, int y, String etiqueta, javafx.scene.Node control) {
        panel.add(new Label(etiqueta), 0, y);
        panel.add(control, 1, y);
    }

    private DetalleCitaDTO requireCita(String accion) {
        DetalleCitaDTO cita = tablaCitas.getSelectionModel().getSelectedItem();
        if (cita == null) {
            throw new IllegalArgumentException(
                    "selecciona primero una cita en la tabla para " + accion);
        }
        return cita;
    }

    private String requireProfesional() {
        if (comboProfesional.getValue() == null) {
            throw new IllegalArgumentException(
                    "selecciona primero un profesional con agenda en el combo");
        }
        return comboProfesional.getValue();
    }

    private LocalDate requireFecha(LocalDate dia, String nombre) {
        if (dia == null) {
            throw new IllegalArgumentException("se necesita una fecha para " + nombre);
        }
        return dia;
    }

    private LocalDateTime leerFechaHora(LocalDate dia, TextField campoHora) {
        LocalTime hora;
        try {
            hora = LocalTime.parse(campoHora.getText().trim());
        } catch (DateTimeParseException | NullPointerException formatoInvalido) {
            throw new IllegalArgumentException(
                    "la hora \"" + campoHora.getText() + "\" no es valida (formato HH:mm)");
        }
        return requireFecha(dia, "la nueva hora").atTime(hora);
    }

    private Integer leerDuracion(boolean obligatoria) {
        String texto = campoDuracion.getText() == null ? "" : campoDuracion.getText().trim();
        if (texto.isEmpty()) {
            if (obligatoria) {
                throw new IllegalArgumentException(
                        "para agendar indica la duracion de la prueba en minutos");
            }
            return null;
        }
        try {
            int minutos = Integer.parseInt(texto);
            if (minutos <= 0) {
                throw new IllegalArgumentException("la duracion debe ser mayor que cero");
            }
            return minutos;
        } catch (NumberFormatException noEsNumero) {
            throw new IllegalArgumentException("la duracion \"" + texto + "\" no es un numero de minutos");
        }
    }

    private void recargar() throws IOException, InterruptedException {
        List<AgendaDTO> agendaDeProfesionales = agendas.listar();
        String seleccionActual = comboProfesional.getValue();
        profesionales.setAll(agendaDeProfesionales.stream()
                .map(AgendaDTO::profesionalId).sorted().toList());
        if (seleccionActual != null && profesionales.contains(seleccionActual)) {
            comboProfesional.setValue(seleccionActual);
        }
        aplicarFiltro();
    }

    private void aplicarFiltro() throws IOException, InterruptedException {
        LocalDateTime desde = pickerDesdeFiltro.getValue() == null
                ? null : pickerDesdeFiltro.getValue().atStartOfDay();
        LocalDateTime hasta = pickerHastaFiltro.getValue() == null
                ? null : pickerHastaFiltro.getValue().atTime(LocalTime.MAX);
        List<DetalleCitaDTO> filtradas = citas.listar(desde, hasta,
                comboProfesional.getValue(), comboEstado.getValue());
        filasCitas.setAll(filtradas);
    }

    private void buscarHuecos() throws IOException, InterruptedException {
        String profesional = requireProfesional();
        LocalDate desde = requireFecha(pickerHuecoDesde.getValue(), "buscar huecos");
        LocalDate hasta = requireFecha(pickerHuecoHasta.getValue(), "buscar huecos");
        if (hasta.isBefore(desde)) {
            throw new IllegalArgumentException("el rango de huecos esta al reves");
        }
        List<HuecoDTO> libres = citas.huecosLibres(profesional, desde, hasta, leerDuracion(false));
        filasHuecos.setAll(libres);
    }

    private void agendarEnHueco() throws IOException, InterruptedException {
        HuecoDTO hueco = tablaHuecos.getSelectionModel().getSelectedItem();
        if (hueco == null) {
            throw new IllegalArgumentException(
                    "busca huecos libres y selecciona uno en la tabla para agendar");
        }
        Integer duracion = leerDuracion(true);
        String solicitud = campoSolicitud.getText() == null ? "" : campoSolicitud.getText().trim();
        citas.agendar(new CitaNuevaDTO(solicitud.isEmpty() ? null : solicitud,
                hueco.profesionalId(), hueco.inicio(), duracion));
        aplicarFiltro();
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
