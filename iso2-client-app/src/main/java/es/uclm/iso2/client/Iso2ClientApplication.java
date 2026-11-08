package es.uclm.iso2.client;

import es.uclm.iso2.client.api.ApiClienteAgendas;
import es.uclm.iso2.client.api.ApiClienteAutenticacion;
import es.uclm.iso2.client.api.ApiClienteCatalogo;
import es.uclm.iso2.client.ui.VistaAdminCatalogo;
import es.uclm.iso2.client.ui.VistaConsultaAgendas;
import es.uclm.iso2.client.ui.VistaConsultaCatalogo;
import es.uclm.iso2.client.ui.VistaGestionAgendas;
import es.uclm.iso2.client.ui.VistaInicioSesion;
import es.uclm.iso2.common.seguridad.AutenticacionFacade.SesionDTO;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * Punto de arranque del cliente JavaFX (release train 0.1.x, D22).
 *
 * <p>En CFG-02 (Iter2/CDU17) el arranque empieza en la vista de inicio de sesion:
 * login real por API (BCrypt + emision JWT en el servidor). Al conseguir la sesion,
 * la aplicacion abre una barra de secciones (CDU13, Iter3/CFG-03) decidida por el rol
 * del token ({@link NavegacionPorRol}): catalogo (editar solo ADMINISTRADOR, el resto
 * solo lectura) y agendas (gestionar ADMINISTRADOR/RECEPCIONISTA, consultar
 * MEDICO/TECNICO, y sin seccion para el CELADOR).</p>
 *
 * <p>Unica propiedad de configuracion: la URL del servidor
 * {@code -Diso2.server=http://localhost:8080}. Las credenciales ya no van por
 * propiedades: el secreto del usuario solo vive en la sesion (CFG-02).</p>
 *
 * <p>Arranque en desarrollo: {@code mvn javafx:run -pl iso2-client-app}.</p>
 */
public class Iso2ClientApplication extends Application {

    /** Version del release train de la aplicacion (D22). */
    public static final String VERSION = "0.1.3-SNAPSHOT";

    /**
     * Constructor estandar requerido por la plataforma JavaFX.
     */
    public Iso2ClientApplication() {
    }

    @Override
    public void start(Stage escenario) {
        String servidor = System.getProperty("iso2.server", "http://localhost:8080");

        ApiClienteAutenticacion auth = new ApiClienteAutenticacion(servidor);
        VistaInicioSesion inicio = new VistaInicioSesion(auth, sesion -> abrirAplicacion(escenario, servidor, sesion));
        escenario.setTitle("ISO2 - Radiodiagnostico SESCAM (cliente v" + VERSION + ")");
        escenario.setScene(new Scene(inicio.raiz(), 560, 260));
        escenario.show();
    }

    /**
     * Tras el login, cambia la escena a la aplicacion con barra de secciones
     * (Catálogo y, segun el rol, Agendas) y la vista del rol dentro de cada seccion.
     * Los clientes HTTP quedan autenticados por Bearer con el token recien emitido.
     *
     * @param escenario la ventana principal
     * @param servidor  raiz de la API
     * @param sesion    sesion devuelta por el login (token + rol)
     */
    private void abrirAplicacion(Stage escenario, String servidor, SesionDTO sesion) {
        ApiClienteCatalogo catalogo = new ApiClienteCatalogo(servidor, sesion.token());
        ApiClienteAgendas agendas = new ApiClienteAgendas(servidor, sesion.token());
        boolean veAgendas = NavegacionPorRol.agendasVisiblePara(sesion.rol());
        boolean gestionaAgendas = NavegacionPorRol.agendasEditablesPara(sesion.rol());
        boolean editaCatalogo = NavegacionPorRol.catalogoEditablePara(sesion.rol());

        BorderPane raiz = new BorderPane();
        Button irCatalogo = new Button("Catalogo");
        Button irAgendas = new Button("Agendas");
        irAgendas.setVisible(veAgendas);
        Runnable alCatalogo = () -> raiz.setCenter(editaCatalogo
                ? new VistaAdminCatalogo(catalogo).raiz()
                : new VistaConsultaCatalogo(catalogo).raiz());
        Runnable aAgendas = () -> raiz.setCenter(gestionaAgendas
                ? new VistaGestionAgendas(agendas).raiz()
                : new VistaConsultaAgendas(agendas).raiz());
        irCatalogo.setOnAction(e -> alCatalogo.run());
        irAgendas.setOnAction(e -> aAgendas.run());

        raiz.setTop(new HBox(10,
                new Label("Sesion: " + sesion.usuario() + " (" + sesion.rol() + ")"),
                irCatalogo, irAgendas));
        alCatalogo.run();
        escenario.setScene(new Scene(raiz, 1100, 680));
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
