package es.uclm.iso2.server.persistencia;

import es.uclm.iso2.common.shared.ApiError;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Punto de entrada del diagnostico de persistencia (D34):
 * {@code GET /api/persistencia/diagnostico} devuelve SGBD, esquema, tablas y registros
 * por tabla. Solo lo puede llamar el rol {@code ADMINISTRADOR} (regla anadida en
 * {@code ConfigSeguridad}), porque revela informacion interna de la base.
 */
@RestController
@RequestMapping("/api/persistencia")
public class DiagnosticoPersistenciaController {

    private static final Logger LOG = LoggerFactory.getLogger(DiagnosticoPersistenciaController.class);

    private final Agente agente;
    private final DiagnosticoPersistenciaDao dao;

    /**
     * Inyecta agente y DAO de diagnostico.
     *
     * @param agente agente del SGBD activo
     * @param dao DAO JDBC de diagnostico sobre {@code INFORMATION_SCHEMA}
     */
    public DiagnosticoPersistenciaController(Agente agente, DiagnosticoPersistenciaDao dao) {
        this.agente = agente;
        this.dao = dao;
    }

    /**
     * Diagnostico del estado de la persistencia.
     *
     * @return 200 con {@link DiagnosticoSgbD}: sgbd, esquema, tablas y conteos
     * @throws SQLException si el SGBD no responde al catalogo o a un recuento
     */
    @GetMapping("/diagnostico")
    public DiagnosticoSgbD diagnostico() throws SQLException {
        String esquema = agente.esquemaActual();
        List<String> tablas = dao.tablasDelEsquema();
        List<ConteoTabla> conteos = dao.conteoPorTabla();
        return DiagnosticoSgbD.de(agente, tablas, conteos, esquema);
    }

    /**
     * Traduce los fallos del SGBD al formato de error comun de la API: el diagnostico
     * no puede inventar datos, asi que responde 500 con {@code PERSISTENCIA_DIAGNOSTICO_FALLO}.
     *
     * @param ex fallo JDBC/catalogo producido durante el diagnostico
     * @return 500 con {@link ApiError} explicando el fallo
     */
    @ExceptionHandler(SQLException.class)
    public ResponseEntity<ApiError> baseInaccesible(SQLException ex) {
        LOG.error("Fallo del SGBD durante el diagnostico de persistencia", ex);
        ApiError error = new ApiError("PERSISTENCIA_DIAGNOSTICO_FALLO",
                "El SGBD no ha podido responder al diagnostico: " + ex.getMessage(),
                Instant.now());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
