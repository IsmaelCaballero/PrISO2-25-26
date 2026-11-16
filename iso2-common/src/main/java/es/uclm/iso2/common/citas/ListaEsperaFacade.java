package es.uclm.iso2.common.citas;

import es.uclm.iso2.common.solicitudes.Prioridad;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Fachada de contratos del subdominio {@code citas}: listas de espera y reubicacion
 * con trazabilidad (CDU14, CFG-07). Solo contrato: la implementacion vive en el
 * modulo iso2-citas.
 *
 * <p>La cola es determinista por contrato (RF14.3): prioridad descendente
 * (URGENTE &gt; PREFERENTE &gt; ORDINARIA, la escala del CDU06) y, a igualdad de
 * prioridad, antiguedad ascendente con el instante de alta. No hay desempate
 * aleatorio: pedida dos veces con el mismo estado y los mismos filtros, la cola
 * trae el mismo orden.</p>
 *
 * <p>La reubicacion (cambio de prioridad con motivo) y el retiro (con motivo)
 * quedan registrados en una cadena de auditoria con hash encadenada propia, la misma
 * tecnica probada por {@code AuditoriaCitas} en CDU12 (RF14.4). El contrato de
 * {@link CitaFacade} (CDU12) NO se toca: es aditivo (baseline CDU12 congelado).</p>
 *
 * @since 1.2.0 (CFG-07, CDU14)
 */
public interface ListaEsperaFacade {

    /**
     * Entrada de alta en la lista de espera.
     *
     * @param solicitudId   solicitud de origen, opcional (alta directa sin CDU06)
     * @param pacienteId    paciente, obligatorio
     * @param pruebaCodigo  codigo de la prueba, obligatorio
     * @param profesionalId profesional deseado, opcional (si falta, cualquiera vale)
     * @param prioridad     nombre de la escala CDU06: URGENTE, PREFERENTE u ORDINARIA
     * @param fechaSolicitud instante del alta; null = reloj del servidor (el test lo fija)
     */
    record AltaEsperaDTO(String solicitudId, String pacienteId, String pruebaCodigo,
                         String profesionalId, String prioridad, Instant fechaSolicitud) {

        /**
         * Valida el alta: paciente y prueba no vacios y prioridad entendible
         * segun {@link Prioridad#desdeNombre(String)}.
         *
         * @throws IllegalArgumentException si faltan pacienteId o pruebaCodigo
         *                                  o si la prioridad no es de la escala
         */
        public AltaEsperaDTO {
            if (pacienteId == null || pacienteId.isBlank()) {
                throw new IllegalArgumentException("pacienteId es obligatorio");
            }
            if (pruebaCodigo == null || pruebaCodigo.isBlank()) {
                throw new IllegalArgumentException("pruebaCodigo es obligatorio");
            }
            if (Prioridad.desdeNombre(prioridad).isEmpty()) {
                throw new IllegalArgumentException("prioridad no entendible: " + prioridad);
            }
        }
    }

    /**
     * Entrada de la cola devuelta a la pantalla del Administrativo.
     *
     * @param esperaId      identificador interno de la entrada
     * @param solicitudId   solicitud de origen (nullable)
     * @param pacienteId    paciente
     * @param pruebaCodigo  codigo de la prueba
     * @param profesionalId profesional pedido (nullable)
     * @param prioridad     nombre de la escala CDU06
     * @param fechaSolicitud instante del alta (la clave de desempate determinista)
     * @param estado        activa / asignada / retirada
     * @param citaId        cita creada al asignar desde la cola (nullable)
     * @param motivoFin     motivo del retiro o de la asignacion (nullable)
     * @param posicion      1-based dentro de la cola pedida (CALCULADA, no persistida)
     * @param total         entradas que cumplen el filtro pedido
     */
    record EntradaEsperaDTO(String esperaId, String solicitudId, String pacienteId,
                            String pruebaCodigo, String profesionalId, String prioridad,
                            Instant fechaSolicitud, String estado, String citaId,
                            String motivoFin, int posicion, long total) {
    }

    /**
     * Peticion de reubicacion: cambio de prioridad con motivo obligatorio.
     *
     * @param prioridad nueva prioridad de la escala CDU06
     * @param motivo    motivo obligatorio de la reubicacion
     */
    record ReubicacionDTO(String prioridad, String motivo) {
    }

    /**
     * Peticion de asignacion desde la cola: hueco elegido para la cita.
     *
     * @param inicio          inicio del hueco
     * @param duracionMinutos duracion en minutos; null = la del catalogo de la prueba
     */
    record AsignacionEsperaDTO(LocalDateTime inicio, Integer duracionMinutos) {
    }

    /**
     * Peticion de retiro de la cola con motivo obligatorio.
     *
     * @param motivo motivo obligatorio del retiro
     */
    record RetiradaEsperaDTO(String motivo) {
    }

    /**
     * Resultado de asignar una entrada de la cola: la cita creada por el motor
     * del CDU12 mas la posicion que ocupaba la entrada al asignar.
     *
     * @param cita          la cita del CDU12 creada (contrato congelado)
     * @param esperaId      entrada de la cola que se asigno
     * @param posicionEnCola la posicion 1-based que ocupaba en su cola
     * @param totalEnCola   entradas que cumplian el filtro de esa cola
     */
    record AsignacionResultadoDTO(CitaFacade.CitaDTO cita, String esperaId,
                                  int posicionEnCola, long totalEnCola) {
    }

    /**
     * Resumen de verificacion de la cadena de auditoria de la cola (RF14.4):
     * la misma tecnica de hash encadenado que el verificar del CDU12, pero con
     * genesis propio en la tabla {@code espera_auditoria}.
     *
     * @param integracion   CADENA_INTEGRAL si ningun eslabon fue alterado,
     *                      CADENA_MANIPULADA en caso contrario
     * @param totalAsientos eslabones recorridos
     * @param hashUltimo    hash del ultimo eslabon de la cadena
     */
    record VerificacionAuditoriaDTO(String integracion, long totalAsientos, String hashUltimo) {
    }

    /**
     * Da de alta una entrada en la lista de espera con prioridad de la escala CDU06.
     *
     * @param alta datos del alta
     * @return la entrada creada con su posicion en la cola
     * @throws IllegalArgumentException        si faltan campos obligatorios o la prioridad
     *                                         no es de la escala (400 {@code DATOS_INVALIDOS})
     * @throws ListaEsperaConflictoException   si el paciente ya tiene una entrada activa
     *                                         para la misma prueba (409 {@code CONFLICTO_ALTA_DUPLICADA})
     */
    EntradaEsperaDTO darDeAlta(AltaEsperaDTO alta);

    /**
     * Cola ordenada de forma determinista (RF14.3): prioridad descendente y, a
     * igualdad de prioridad, {@code fechaSolicitud} ascendente. Leida dos veces
     * con el mismo estado y los mismos filtros, el orden es identico.
     *
     * @param pruebaCodigo  filtro por prueba, opcional
     * @param profesionalId filtro por profesional pedido, opcional
     * @param estado        filtro por estado en minusculas (activa por defecto), opcional
     * @return la cola completa con su posicion calculada
     */
    List<EntradaEsperaDTO> cola(String pruebaCodigo, String profesionalId, String estado);

    /**
     * Detalle de una entrada de la cola.
     *
     * @param esperaId identificador de la entrada
     * @return la entrada con su posicion actual
     * @throws ListaEsperaNoEncontradaException si no existe (404)
     */
    EntradaEsperaDTO detalle(String esperaId);

    /**
     * Cambio de prioridad con motivo obligatorio: reordena la cola de forma
     * determinista y deja el eslabon {@code REUBICACION} en la cadena de auditoria;
     * la {@code fechaSolicitud} original no cambia.
     *
     * @param esperaId  entrada a reubicar
     * @param prioridad nueva prioridad de la escala CDU06
     * @param motivo    motivo obligatorio de la reubicacion
     * @return la entrada reubicada con su posicion nueva
     * @throws ListaEsperaNoEncontradaException si la entrada no existe (404)
     * @throws ListaEsperaConflictoException    si la entrada no esta activa o la
     *                                          prioridad es la misma (409 {@code CONFLICTO_ENTRADA_NO_ACTIVA}
     *                                          o {@code CONFLICTO_REUBICACION_REDUNDANTE})
     * @throws IllegalArgumentException         si la prioridad o el motivo no valen (400)
     */
    EntradaEsperaDTO cambiarPrioridad(String esperaId, String prioridad, String motivo);

    /**
     * Retira una entrada de la cola con motivo obligatorio; deja el eslabon
     * {@code RETIRADA} en la cadena.
     *
     * @param esperaId entrada a retirar
     * @param motivo   motivo obligatorio del retiro
     * @return la entrada retirada, ya fuera de la cola activa
     * @throws ListaEsperaNoEncontradaException si la entrada no existe (404)
     * @throws ListaEsperaConflictoException    si ya fue asignada o retirada
     *                                          (409 {@code CONFLICTO_ENTRADA_NO_ACTIVA})
     * @throws IllegalArgumentException         si falta el motivo (400)
     */
    EntradaEsperaDTO retirar(String esperaId, String motivo);

    /**
     * Asigna una cita a una entrada de la cola usando el motor de huecos del CDU12:
     * el contrato de {@link CitaFacade} no se toca y sus codigos 409 viajan tal cual.
     * Deja el eslabon {@code ASIGNACION} en la cadena de la cola.
     *
     * @param esperaId        entrada de la cola, debe estar activa
     * @param inicio          inicio del hueco elegido
     * @param duracionMinutos duracion en minutos; null = la del catalogo de la prueba
     * @return la cita creada y la posicion que ocupaba la entrada
     * @throws ListaEsperaNoEncontradaException si la entrada no existe (404)
     * @throws ListaEsperaConflictoException    si la entrada no esta activa o la
     *                                          solicitud de origen no es apta (409)
     * @throws CitaConflictoException           si el hueco no vale (409, contrato CDU12)
     * @throws IllegalArgumentException         si faltan datos (400)
     */
    AsignacionResultadoDTO asignarDesdeCola(String esperaId, LocalDateTime inicio, Integer duracionMinutos);

    /**
     * Verifica la cadena de auditoria de la cola: si ningun eslabon de altas,
     * reubicaciones, asignaciones o retiros fue alterado.
     *
     * @return el resumen de verificacion
     */
    VerificacionAuditoriaDTO verificarAuditoria();
}
