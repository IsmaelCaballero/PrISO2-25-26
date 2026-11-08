package es.uclm.iso2.common.solicitudes;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Fachada de contratos del subdominio {@code solicitudes}: Solicitudes de prueba desde Atencion Primaria (CDU06).
 * Solo contrato: la implementacion vive en el modulo iso2-solicitudes (baseline 1.0.0, D22).
 *
 * <p>El contrato baseline de CFG-00 ({@link #crear} y {@link #buscar}) queda <strong>congelado</strong>.
 * Con CDU06 (Iter4, CFG-04) se amplia MINOR anadiendo la prescripcion medica con no repudio:
 * campos obligatorios (RF06.1), validacion de la prueba contra el catalogo de CDU16,
 * prohibicion de solicitudes duplicadas del mismo medico (RF06.3) y trazabilidad de
 * no repudio con huella SHA-256 (RF06.2).</p>
 *
 * @since 1.0.0 (CFG-00); ampliada en 1.1.0 (CDU06, CFG-04)
 */
public interface SolicitudFacade {

    /**
     * Crea una solicitud de prueba de imagen.
     *
     * @param nueva datos de la solicitud a crear
     * @return solicitud creada con identificador y estado inicial
     */
    SolicitudDTO crear(SolicitudNuevaDTO nueva);

    /**
     * Busca una solicitud por identificador.
     *
     * @param solicitudId identificador de la solicitud
     * @return la solicitud si existe, vacio en caso contrario
     */
    Optional<SolicitudDTO> buscar(String solicitudId);

    /**
     * Prescribe una prueba de imagen con todos los campos obligatorios del CDU06
     * (RF06.1). El {@code medicoId} del dto es fijado por el controlador con el claim
     * del token JWT: el cuerpo de la peticion no puede fingir otro autor (RF06.2,
     * no repudio). Reglas del CDU: la prueba debe existir y estar activa en el
     * catalogo (CDU16) y el medico no puede tener otra solicitud activa (no cancelada)
     * del mismo paciente y prueba (RF06.3).
     *
     * @param prescripcion datos completos de la prescripcion
     * @return solicitud creada con estado inicial {@code creada}
     * @throws IllegalArgumentException               si falta algun campo obligatorio (400)
     * @throws SolicitudConflictoException            si la prueba no esta disponible o
     *                                                la solicitud esta duplicada (409)
     * @since 1.1.0 (CDU06, CFG-04)
     */
    SolicitudDTO prescribir(SolicitudPrescripcionDTO prescripcion);

    /**
     * Lista las solicitudes prescritas por un medico, de la mas reciente a la mas antigua.
     *
     * @param medicoId identificador del medico autor (claim del token)
     * @return solicitudes del medico ordenadas por instante de creacion descendente
     * @since 1.1.0 (CDU06, CFG-04)
     */
    List<SolicitudDTO> listarPorMedico(String medicoId);

    /**
     * Detalle completo de una solicitud.
     *
     * @param solicitudId identificador de la solicitud
     * @return la solicitud
     * @throws SolicitudNoEncontradaException si la solicitud no existe (404)
     * @since 1.1.0 (CDU06, CFG-04)
     */
    SolicitudDTO detalle(String solicitudId);

    /**
     * Verifica la trazabilidad de no repudio de una solicitud (RF06.2): autor,
     * instante y huella SHA-256 guardados, mas el resultado de recalcular la huella
     * sobre el contenido actual.
     *
     * @param solicitudId identificador de la solicitud
     * @return informacion de trazabilidad con el flag de integridad
     * @throws SolicitudNoEncontradaException si la solicitud no existe (404)
     * @since 1.1.0 (CDU06, CFG-04)
     */
    TrazabilidadSolicitudDTO trazable(String solicitudId);

    /**
     * Datos para crear una solicitud.
     *
     * @param pacienteId   historia clinica del paciente
     * @param codigoPrueba codigo de prueba del catalogo
     * @param origen       origen de la solicitud (AP, hospital, urgencias)
     */
    record SolicitudNuevaDTO(String pacienteId, String codigoPrueba, String origen) {
    }

    /**
     * Solicitud ya persistida.
     *
     * @param solicitudId     identificador asignado
     * @param pacienteId      historia clinica del paciente
     * @param codigoPrueba    codigo de prueba del catalogo
     * @param fechaSolicitada fecha de emision
     * @param estado          estado del ciclo de vida (creada, citada, realizada, informada, cancelada)
     */
    record SolicitudDTO(String solicitudId, String pacienteId, String codigoPrueba, LocalDate fechaSolicitada, String estado) {
    }

    /**
     * Prescripcion completa de una prueba (CDU06). El constructor compacto valida los
     * campos obligatorios de la RF06.1 como parte del contrato: cualquier JSON con un
     * campo en blanco se rechaza con un {@link IllegalArgumentException} (400) antes de
     * tocar el servicio. Ojo: el {@code medicoId} NO debe venir del cuerpo JSON, lo fija
     * el controlador con el usuario autenticado (RF06.2, no repudio).
     *
     * @param pacienteId        historia clinica del paciente (no vacia)
     * @param codigoPrueba      codigo de prueba del catalogo (no vacio)
     * @param indicacionClinica motivo clinico de la peticion (no vacio, RF06.1)
     * @param prioridad         prioridad clinica (no nula, RF06.1)
     * @param origen            origen de la solicitud: AP, hospital, urgencias (no vacio)
     * @param medicoId          autor medico de la prescripcion (no vacio, RF06.2)
     * @since 1.1.0 (CDU06, CFG-04)
     */
    record SolicitudPrescripcionDTO(String pacienteId, String codigoPrueba, String indicacionClinica,
                                    Prioridad prioridad, String origen, String medicoId) {

        public SolicitudPrescripcionDTO {
            exigirTexto(pacienteId, "pacienteId");
            exigirTexto(codigoPrueba, "codigoPrueba");
            exigirTexto(indicacionClinica, "indicacionClinica");
            if (prioridad == null) {
                throw new IllegalArgumentException("el campo prioridad es obligatorio");
            }
            exigirTexto(origen, "origen");
            exigirTexto(medicoId, "medicoId");
        }

        private static void exigirTexto(String valor, String campo) {
            if (valor == null || valor.isBlank()) {
                throw new IllegalArgumentException("el campo " + campo + " es obligatorio");
            }
        }
    }

    /**
     * Trazabilidad de no repudio de una solicitud (RF06.2): el autor y el instante
     * quedan sellados por la huella SHA-256 del contenido. {@code integridadOk} es el
     * resultado de recalcular la huella sobre lo guardado: {@code false} revela una
     * manipulacion directa de la base de datos.
     *
     * @param solicitudId  identificador de la solicitud
     * @param autor        medico firmante (claim del token en el alta)
     * @param creadaEn     instante de creacion usado en el canonico de la huella
     * @param huella       huella SHA-256 (hex) guardada en la solicitud
     * @param integridadOk true si el recalculo de la huella coincide con la guardada
     * @since 1.1.0 (CDU06, CFG-04)
     */
    record TrazabilidadSolicitudDTO(String solicitudId, String autor, Instant creadaEn,
                                    String huella, boolean integridadOk) {
    }
}
