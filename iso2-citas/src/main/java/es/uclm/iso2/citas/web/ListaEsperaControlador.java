package es.uclm.iso2.citas.web;

import es.uclm.iso2.common.citas.ListaEsperaFacade;
import es.uclm.iso2.common.citas.ListaEsperaFacade.AltaEsperaDTO;
import es.uclm.iso2.common.citas.ListaEsperaFacade.AsignacionEsperaDTO;
import es.uclm.iso2.common.citas.ListaEsperaFacade.AsignacionResultadoDTO;
import es.uclm.iso2.common.citas.ListaEsperaFacade.AuditoriaEsperaDTO;
import es.uclm.iso2.common.citas.ListaEsperaFacade.EntradaEsperaDTO;
import es.uclm.iso2.common.citas.ListaEsperaFacade.ReubicacionDTO;
import es.uclm.iso2.common.citas.ListaEsperaFacade.RetiradaEsperaDTO;
import es.uclm.iso2.common.citas.ListaEsperaFacade.VerificacionAuditoriaDTO;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * API REST de listas de espera del CDU14 sobre la que trabaja la pantalla del
 * Administrativo (UC14.1 a UC14.6). El contrato de errores lo traduce
 * {@link ListaEsperaManejadorErrores}: 409 cuando la entrada no esta activa, la
 * reubicacion es redundante o el alta duplica la cola, 404 cuando la entrada o la
 * referencia no existen y 400 cuando faltan datos.
 *
 * <p>Sin PUT ni PATCH: la prioridad y el estado de una entrada solo cambian por las
 * acciones de la cola (prioridad, asignar, retirar), que es lo que deja el eslabon
 * de auditoria con hash encadenado (RF14.4). Sin DELETE: retirar con motivo es la
 * unica salida terminal ademas de la asignacion; borrar la coleccion entera responde
 * 405 (TC14-X RETIRAR-SIN-ID).</p>
 *
 * <p>El path variable {@code esperaId} va tipado {@link UUID}: un id mal formado lo
 * rechaza el conversor con 400 {@code DATOS_INVALIDOS}, nunca un 404 disfrazado
 * (HALLAZGO 3 de CDU19 corregido en CDU12). El segmento literal
 * {@code auditoria-verificable} de la verificacion gana al path variable: Spring
 * prioriza la ruta literal (TC14-X COLISION-RUTA-LITERAL).</p>
 *
 * @since 1.2.0 (CFG-07, CDU14)
 */
@RestController
@RequestMapping("/api/listas-espera")
public class ListaEsperaControlador {

    private final ListaEsperaFacade cola;

    /**
     * Inyecta la fachada de la lista de espera.
     *
     * @param cola implementacion de {@code ListaEsperaFacade}
     */
    public ListaEsperaControlador(ListaEsperaFacade cola) {
        this.cola = cola;
    }

    /**
     * Da de alta una entrada en la lista de espera con prioridad de la escala
     * CDU06. Los obligatorios se leen solo del cuerpo: intentar colarlos por query
     * termina en 400 porque el alta ignora los parametros (TC14-X
     * QUERY-CON-PARAMS-EN-ALTA).
     *
     * @param alta paciente, prueba, prioridad y demas campos opcionales
     * @return 201 con la entrada creada y su posicion en la cola
     */
    @PostMapping
    public ResponseEntity<EntradaEsperaDTO> darDeAlta(@RequestBody AltaEsperaDTO alta) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cola.darDeAlta(alta));
    }

    /**
     * Cola ordenada de forma determinista (RF14.3) con los filtros de la pantalla.
     * Un valor repetido en un filtro se resuelve como canon de Spring (primer
     * valor); un estado o prueba imposibles responden 400, nunca una cola vacia
     * que miente (TC14-X ESTADO-ABSURDO-EN-FILTRO).
     *
     * @param pruebaCodigo  filtro por prueba, opcional
     * @param profesionalId filtro por profesional pedido, opcional
     * @param estado        filtro por estado en minusculas, opcional
     * @return 200 con la cola y su posicion calculada
     */
    @GetMapping
    public List<EntradaEsperaDTO> cola(
            @RequestParam(required = false) String pruebaCodigo,
            @RequestParam(required = false) String profesionalId,
            @RequestParam(required = false) String estado) {
        return cola.cola(pruebaCodigo, profesionalId, estado);
    }

    /**
     * Verificacion anti-manipulacion de la cadena de auditoria de la cola
     * (UC14.6, RF14.4). Ruta literal: no la secuestra el path variable.
     *
     * @return 200 con el resumen de verificacion
     */
    @GetMapping("/auditoria-verificable")
    public VerificacionAuditoriaDTO verificarAuditoria() {
        return cola.verificarAuditoria();
    }

    /**
     * Detalle de una entrada de la cola.
     *
     * @param esperaId identificador de la entrada
     * @return 200 con la entrada y su posicion actual; 404 si no existe
     */
    @GetMapping("/{esperaId}")
    public EntradaEsperaDTO detalle(@PathVariable UUID esperaId) {
        return cola.detalle(esperaId.toString());
    }

    /**
     * Cadena de auditoria de una entrada: eslabones ALTA, REUBICACION, ASIGNACION
     * o RETIRADA en orden de encadenamiento (tabla G de la spec). Auditar una
     * entrada inexistente es 404 propio, no una lista vacia.
     *
     * @param esperaId entrada a auditar
     * @return 200 con los eslabones; 404 si la entrada no existe
     */
    @GetMapping("/{esperaId}/auditoria")
    public List<AuditoriaEsperaDTO> auditoriaDe(@PathVariable UUID esperaId) {
        return cola.auditoriaDe(esperaId.toString());
    }

    /**
     * Reubica una entrada: cambio de prioridad con motivo obligatorio (UC14.3)
     * que reordena la cola de forma determinista y deja el eslabon REUBICACION.
     * La prioridad y el motivo se leen solo del cuerpo.
     *
     * @param esperaId    entrada a reubicar
     * @param reubicacion nueva prioridad y motivo
     * @return 200 con la entrada reubicada y su posicion nueva
     */
    @PostMapping("/{esperaId}/prioridad")
    public EntradaEsperaDTO cambiarPrioridad(@PathVariable UUID esperaId,
            @RequestBody ReubicacionDTO reubicacion) {
        return cola.cambiarPrioridad(esperaId.toString(), reubicacion.prioridad(), reubicacion.motivo());
    }

    /**
     * Asigna una cita a una entrada de la cola con el motor de huecos del CDU12
     * (UC14.2). El hueco va en el cuerpo: pasar {@code profesionalId} o
     * {@code inicio} por query termina en 400 porque el controlador solo lee el
     * cuerpo (TC14-X ASIGNAR-CON-PARAMS-EN-QUERY). El doble clic sobre el boton
     * se resuelve por la maquina de estados: el segundo intento es 409
     * {@code CONFLICTO_ENTRADA_NO_ACTIVA} sin consultar la agenda.
     *
     * @param esperaId   entrada de la cola, debe estar activa
     * @param asignacion profesional, inicio y duracion del hueco elegido
     * @return 201 con la cita creada y la posicion que ocupaba la entrada
     */
    @PostMapping("/{esperaId}/asignar")
    public ResponseEntity<AsignacionResultadoDTO> asignarDesdeCola(@PathVariable UUID esperaId,
            @RequestBody AsignacionEsperaDTO asignacion) {
        AsignacionResultadoDTO resultado = cola.asignarDesdeCola(esperaId.toString(),
                asignacion.profesionalId(), asignacion.inicio(), asignacion.duracionMinutos());
        return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
    }

    /**
     * Retira una entrada de la cola con motivo obligatorio (UC14.4); deja el
     * eslabon RETIRADA y la entrada fuera de la cola activa. Sin DELETE: esta es
     * la unica salida terminal manual y conserva el rastro.
     *
     * @param esperaId entrada a retirar
     * @param retirada motivo del retiro
     * @return 200 con la entrada retirada
     */
    @PostMapping("/{esperaId}/retirar")
    public EntradaEsperaDTO retirar(@PathVariable UUID esperaId,
            @RequestBody RetiradaEsperaDTO retirada) {
        return cola.retirar(esperaId.toString(), retirada.motivo());
    }
}
