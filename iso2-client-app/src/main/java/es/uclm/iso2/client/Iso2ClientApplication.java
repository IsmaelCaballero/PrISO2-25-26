package es.uclm.iso2.client;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/**
 * Punto de arranque del cliente JavaFX (Iter0/CFG-00, v0.1.0).
 *
 * <p>En Iter0 solo se muestra la ventana base con el nombre del sistema.
 * La interfaz por rol (medico, tecnico, recepcion, administrativo, paciente,
 * administrador) se construira CDU a CDU con sus casos de uso.</p>
 *
 * <p>Arranque en desarrollo: {@code mvn javafx:run -pl iso2-client-app}.</p>
 */
public class Iso2ClientApplication extends Application {

    /** Version del release train de la aplicacion (D22). */
    public static final String VERSION = "0.1.0";

    /**
     * Constructor estandar requerido por la plataforma JavaFX.
     */
    public Iso2ClientApplication() {
    }

    @Override
    public void start(Stage escenario) {
        Label titulo = new Label(
                "Sistema Integral de Gestion de Radiodiagnostico e Imagen Medica (SESCAM)");
        Label version = new Label("Cliente v" + VERSION + " - Iter0 (CFG-00): interfaz por roles pendiente CDU a CDU");
        javafx.scene.layout.VBox raiz = new javafx.scene.layout.VBox(12, titulo, version);
        raiz.setStyle("-fx-padding: 24; -fx-alignment: center-left;");
        escenario.setTitle("ISO2 - Radiodiagnostico SESCAM");
        escenario.setScene(new Scene(raiz, 720, 240));
        escenario.show();
    }

    /**
     * Lanza la plataforma JavaFX.
     *
     * @param args argumentos de linea de comandos
     */
    public static void main(String[] args) {
        launch(args);
    }
}
