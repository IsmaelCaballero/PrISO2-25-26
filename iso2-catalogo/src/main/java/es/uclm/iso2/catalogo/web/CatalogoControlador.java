package es.uclm.iso2.catalogo.web;

import es.uclm.iso2.common.catalogo.CatalogoNoEncontradoException;
import es.uclm.iso2.common.catalogo.PruebaCatalogoFacade;
import es.uclm.iso2.common.catalogo.PruebaCatalogoFacade.PruebaImagenDTO;
import es.uclm.iso2.common.shared.Paginacion;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * API REST de administracion del catalogo de pruebas (CDU16, RF16).
 * Protegida por RBAC: solo rol {@code ADMIN} (configurado en {@code iso2-server-app}).
 *
 * <p>Codigos de estado: 201 alta; 200 lectura/modificacion/reactivacion; 204 baja logica;
 * 400 validacion; 404 prueba inexistente; 409 codigo duplicado.</p>
 *
 * @since 1.1.0 (CFG-01)
 */
@RestController
@RequestMapping("/api/catalogo/pruebas")
public class CatalogoControlador {

    private final PruebaCatalogoFacade catalogo;

    /**
     * Inyecta la fachada del catalogo.
     *
     * @param catalogo implementacion del servicio de catalogo
     */
    public CatalogoControlador(PruebaCatalogoFacade catalogo) {
        this.catalogo = catalogo;
    }

    /**
     * Lista las pruebas del catalogo (incluye desactivadas).
     *
     * @param pagina pagina zero-based (defecto 0)
     * @param tamano tamano de pagina 1..200 (defecto 50)
     * @param orden  campo de orden: codigo, nombre, categoria o duracionMinutos
     * @return lista de pruebas
     */
    @GetMapping
    public List<PruebaImagenDTO> listar(@RequestParam(defaultValue = "0") int pagina,
                                        @RequestParam(defaultValue = "50") int tamano,
                                        @RequestParam(required = false) String orden) {
        return catalogo.listar(new Paginacion(pagina, tamano, orden));
    }

    /**
     * Detalle de una prueba por codigo.
     *
     * @param codigo codigo de la prueba
     * @return 200 con la prueba, o 404 con {@code ApiError(PRUEBA_NO_ENCONTRADA)} si no
     *         existe: el cuerpo de error es el mismo que devuelven el PUT y el DELETE, de
     *         modo que el cliente puede leer el codigo de error en cualquier operacion
     */
    @GetMapping("/{codigo}")
    public ResponseEntity<PruebaImagenDTO> detalle(@PathVariable String codigo) {
        return ResponseEntity.ok(catalogo.buscarPorCodigo(codigo)
                .orElseThrow(() -> new CatalogoNoEncontradoException(codigo)));
    }

    /**
     * Alta de una prueba nueva.
     *
     * @param prueba datos de la prueba
     * @return 201 con la prueba creada (409 si el codigo ya existe, 400 si es invalida)
     */
    @PostMapping
    public ResponseEntity<PruebaImagenDTO> crear(@RequestBody PruebaImagenDTO prueba) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogo.crear(prueba));
    }

    /**
     * Modificacion de los parametros de una prueba existente.
     *
     * @param codigo codigo en la ruta (debe coincidir con el del cuerpo)
     * @param prueba nuevos datos
     * @return 200 con la prueba actualizada
     */
    @PutMapping("/{codigo}")
    public ResponseEntity<PruebaImagenDTO> actualizar(@PathVariable String codigo,
                                                      @RequestBody PruebaImagenDTO prueba) {
        return ResponseEntity.ok(catalogo.actualizar(codigo, prueba));
    }

    /**
     * Baja logica de una prueba (nunca hay borrado fisico).
     *
     * @param codigo codigo de la prueba
     * @return 204 sin cuerpo
     */
    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void> desactivar(@PathVariable String codigo) {
        catalogo.desactivar(codigo);
        return ResponseEntity.noContent().build();
    }

    /**
     * Reactivacion de una prueba desactivada.
     *
     * @param codigo codigo de la prueba
     * @return 200 con la prueba reactivada
     */
    @PutMapping("/{codigo}/reactivar")
    public ResponseEntity<PruebaImagenDTO> reactivar(@PathVariable String codigo) {
        return ResponseEntity.ok(catalogo.reactivar(codigo));
    }
}
