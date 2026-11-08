package es.uclm.iso2.client.ui;

import es.uclm.iso2.client.api.ApiClienteAutenticacion;
import es.uclm.iso2.client.api.ApiException;
import es.uclm.iso2.common.seguridad.AutenticacionFacade.SesionDTO;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * Vista de inicio de sesion (CDU17, Iter2/CFG-02): usuario + contrasena contra
 * {@code POST /api/auth/login}. Al obtener sesion, entrega el {@link SesionDTO} al
 * callback de la aplicacion, que decidira la vista segun el rol (NavegacionPorRol).
 *
 * <p>Muestra los codigos del contrato tal cual llegan (CREDENCIALES_INVALIDAS,
 * CUENTA_BLOQUEADA, DATOS_INVALIDOS) para que el usuario entienda el bloqueo por
 * intentos sin necesidad de documentacion.</p>
 */
public final class VistaInicioSesion {

    private final ApiClienteAutenticacion api;
    private final Consumer<SesionDTO> alEntrar;
    private final TextField campoUsuario = new TextField();
    private final PasswordField campoPassword = new PasswordField();

    /**
     * Construye la vista de login.
     *
     * @param api      cliente de autenticacion
     * @param alEntrar callback que recibe la sesion cuando el login es correcto
     */
    public VistaInicioSesion(ApiClienteAutenticacion api, Consumer<SesionDTO> alEntrar) {
        this.api = api;
        this.alEntrar = alEntrar;
    }

    /**
     * Raiz del escenario con el formulario de acceso.
     *
     * @return el panel listo para poner en un {@code Scene}
     */
    public BorderPane raiz() {
        GridPane datos = new GridPane();
        datos.setHgap(8);
        datos.setVgap(6);
        datos.add(new Label("Usuario"), 0, 0);
        datos.add(campoUsuario, 1, 0);
        datos.add(new Label("Contrasena"), 0, 1);
        datos.add(campoPassword, 1, 1);

        Button acceder = new Button("Acceder");
        acceder.setDefaultButton(true);
        acceder.setOnAction(e -> iniciarSesion());

        VBox panel = new VBox(12, new Label("Acceso al sistema (cuentas SESCAM)"), datos, acceder);
        panel.setPadding(new Insets(24));
        BorderPane raiz = new BorderPane();
        raiz.setCenter(panel);
        return raiz;
    }

    private void iniciarSesion() {
        try {
            SesionDTO sesion = api.iniciarSesion(campoUsuario.getText().trim(), campoPassword.getText());
            alEntrar.accept(sesion);
        } catch (ApiException error) {
            avisar(error.codigo() + ": " + error.getMessage());
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
