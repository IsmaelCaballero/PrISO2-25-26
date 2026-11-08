package es.uclm.iso2.client;

import es.uclm.iso2.client.api.ApiClienteAutenticacion;
import es.uclm.iso2.client.api.ApiClienteCatalogo;
import es.uclm.iso2.client.ui.VistaAdminCatalogo;
import es.uclm.iso2.client.ui.VistaConsultaCatalogo;
import es.uclm.iso2.client.ui.VistaInicioSesion;
import es.uclm.iso2.common.seguridad.AutenticacionFacade.SesionDTO;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Punto de arranque del cliente JavaFX (release train 0.1.x, D22).
 *
 * <p>En CFG-02 (Iter2/CDU17) el arranque empieza en la vista de inicio de sesion:
 * login real por API (BCrypt + emision JWT en el servidor). Al conseguir la sesion,
 * la aplicacion abre la vista de catalogo que corresponde al rol del token
 * ({@link NavegacionPorRol}): administracion para ADMINISTRADOR y solo lectura para
 * MEDICO/TECNICO/CELADOR/RECEPCIONISTA.</p>
 *
 * <p>Unica propiedad de configuracion: la URL del servidor
 * {@code -Diso2.server=http://localhost:8080}. Las credenciales ya no van por
 * propiedades: el secreto del usuario solo vive en la sesion (CFG-02).</p>
 *
 * <p>Arranque en desarrollo: {@code mvn javafx:run -pl iso2-client-app}.</p>
 */
public class Iso2ClientApplication extends Application {

    /** Version del release train de la aplicacion (D22). */
    public static final String VERSION = "0.1.2-SNAPSHOT";

    /**
     * Constructor estandar requerido por la plataforma JavaFX.
     */
    public Iso2ClientApplication() {
    }

    @Override
    public void start(Stage escenario) {
        String servidor = System.getProperty("iso2.server", "http://localhost:8080");

        ApiClienteAutenticacion auth = new ApiClienteAutenticacion(servidor);
        VistaInicioSesion inicio = new VistaInicioSesion(auth, sesion -> abrirCatalogo(escenario, servidor, sesion));
        escenario.setTitle("ISO2 - Radiodiagnostico SESCAM (cliente v" + VERSION + ")");
        escenario.setScene(new Scene(inicio.raiz(), 560, 260));
        escenario.show();
    }

    /**
     * Tras el login, cambia la escena a la vista de catalogo del rol con un cliente
     * autenticado por Bearer con el token recien emitido.
     *
     * @param escenario la ventana principal
     * @param servidor  raiz de la API
     * @param sesion    sesion devuelta por el login (token + rol)
     */
    private void abrirCatalogo(Stage escenario, String servidor, SesionDTO sesion) {
        ApiClienteCatalogo api = new ApiClienteCatalogo(servidor, sesion.token());
        Parent vista;
        if (NavegacionPorRol.catalogoEditablePara(sesion.rol())) {
            vista = new VistaAdminCatalogo(api).raiz();
        } else {
            vista = new VistaConsultaCatalogo(api).raiz();
        }
        escenario.setScene(new Scene(vista, 1100, 640));
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
