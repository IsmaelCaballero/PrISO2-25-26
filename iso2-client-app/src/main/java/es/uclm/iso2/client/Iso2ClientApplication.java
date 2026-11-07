package es.uclm.iso2.client;

import es.uclm.iso2.client.api.ApiClienteCatalogo;
import es.uclm.iso2.client.ui.VistaAdminCatalogo;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Punto de arranque del cliente JavaFX (release train 0.1.x, D22).
 *
 * <p>En CFG-01 (Iter1/CDU16) abre directamente la vista de administrador con el
 * catalogo de pruebas, autenticada por HTTP Basic contra el servidor. Las credenciales
 * y la URL se pueden ajustar por propiedades de sistema para poder probarlo contra
 * otro servidor o con otras cuentas:</p>
 * <ul>
 *   <li>{@code -Diso2.server=http://localhost:8080}</li>
 *   <li>{@code -Diso2.usuario=admin} / {@code -Diso2.password=iso2-admin}</li>
 * </ul>
 *
 * <p>El login por rol con JWT definitivo llega con CDU17 (Iter2); hasta entonces la
 * interfaz mostrada es solo la de administrador (matriz CFG-01).</p>
 *
 * <p>Arranque en desarrollo: {@code mvn javafx:run -pl iso2-client-app}.</p>
 */
public class Iso2ClientApplication extends Application {

    /** Version del release train de la aplicacion (D22). */
    public static final String VERSION = "0.1.1-SNAPSHOT";

    /**
     * Constructor estandar requerido por la plataforma JavaFX.
     */
    public Iso2ClientApplication() {
    }

    @Override
    public void start(Stage escenario) {
        String servidor = System.getProperty("iso2.server", "http://localhost:8080");
        String usuario = System.getProperty("iso2.usuario", "admin");
        String password = System.getProperty("iso2.password", "iso2-admin");

        ApiClienteCatalogo api = new ApiClienteCatalogo(servidor, usuario, password);
        VistaAdminCatalogo vista = new VistaAdminCatalogo(api);
        escenario.setTitle("ISO2 - Radiodiagnostico SESCAM (cliente v" + VERSION + ")");
        escenario.setScene(new Scene(vista.raiz(), 1100, 640));
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
