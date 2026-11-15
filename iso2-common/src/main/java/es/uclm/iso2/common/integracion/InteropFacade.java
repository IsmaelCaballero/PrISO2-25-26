package es.uclm.iso2.common.integracion;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Fachada de contratos del subdominio {@code integracion}: Importacion de estudios desde HIS/RIS y despacho DICOM (CDU19; exportacion en CDU11/CDU09).
 * Solo contrato: la implementacion vive en el modulo iso2-integracion (baseline 1.0.0, D22).
 *
 * <p>Baseline 1.0.0 (CFG-00): {@link #importarEstudio(String)} y el record
 * {@link EstudioDTO}, que siguen congelados e intocados (D22: el contrato baseline
 * no se rompe). La version 1.1.0 (CFG-06, CDU19) anade por compatibilidad
 * descendente (D23, extension MINOR) el acceso al IdP externo
 * {@link #autenticarSso(CredencialesSsoDTO)}, la sincronizacion idempotente de la
 * historia clinica externa {@link #sincronizarHistoriaClinica(String)}, el estado
 * de los sistemas externos {@link #pingRis()}, {@link #pingPacs()},
 * {@link #pingSistema(String)} y {@link #estadoSistemas()}, y la auditoria
 * encadenada del subdominio {@link #estadoAuditoriaIntegracion()}, ademas de los
 * records nuevos y de las excepciones {@link CredencialesInvalidasException},
 * {@link HistoriaNoEncontradaException}, {@link EstudioNoEncontradoException},
 * {@link SistemaNoEncontradoException} y {@link SistemaExternoException}.</p>
 *
 * <p>Spike CDU19: no hay IdP, HCE ni RIS/PACS reales en develop. El modulo
 * implementa los puertos con adaptadores simulados cuya politica de fallos es
 * mutable desde la API para poder ejecutar los TCs de modo degradado
 * (CAIDO/LENTO/TIEMPO SUPERADO). Los hallazgos del spike se recogen en
 * {@code docs/informes/spike-cdu19.md}.</p>
 *
 * @since 1.0.0 (CFG-00); extendida en 1.1.0 (CFG-06, CDU19)
 */
public interface InteropFacade {

    /**
     * Importa la cabecera de un estudio desde el sistema de origen.
     *
     * @param accessionNumber numero de acceso del estudio
     * @return cabecera del estudio importada
     */
    EstudioDTO importarEstudio(String accessionNumber);

    // ------------------------------------------------------------------
    // Extension MINOR 1.1.0 (CFG-06, CDU19): nada de lo anterior cambia.
    // ------------------------------------------------------------------

    /**
     * Autentica al usuario contra el IdP externo (SSO) y devuelve la sesion emitida.
     *
     * <p>Hallazgo del spike: esta sesion NO sustituye al JWT de CDU17; la API de
     * ISO2 sigue autenticandose con el token propio.</p>
     *
     * @param credenciales usuario y contrasena que acepta el IdP
     * @return sesion SSO con sujeto, rol externo y caducidad
     * @throws CredencialesInvalidasException si el IdP rechaza las credenciales (401)
     * @throws SistemaExternoException        si el IdP esta caido o no responde a tiempo (503);
     *                                        el fallo del sistema gana a la credencial invalida
     */
    SesionSsoDTO autenticarSso(CredencialesSsoDTO credenciales);

    /**
     * Sincroniza la historia clinica externa de un paciente. La operacion es
     * idempotente: si el contenido externo no cambio desde la ultima sincronizacion
     * (misma huella SHA-256 de la forma canonica), se reutiliza la instantanea y el
     * resultado lleva {@code reutilizado=true}.
     *
     * @param pacienteExternoId identificador del paciente en el HCE externo
     * @return historia sincronizada con su huella y el flag de reutilizacion
     * @throws HistoriaNoEncontradaException si el paciente no existe en el HCE (404)
     * @throws SistemaExternoException       si el HCE esta caido o no responde a tiempo (503)
     */
    HistoriaSincronizadaDTO sincronizarHistoriaClinica(String pacienteExternoId);

    /**
     * Estado del RIS (ping degradado: un RIS caido se responde como no disponible,
     * nunca como error).
     *
     * @return estado del sistema RIS
     */
    EstadoSistemaDTO pingRis();

    /**
     * Estado del PACS (ping degradado, igual que {@link #pingRis()}).
     *
     * @return estado del sistema PACS
     */
    EstadoSistemaDTO pingPacs();

    /**
     * Estado de un sistema externo por su nombre ({@code sso}, {@code hce},
     * {@code ris} o {@code pacs}). Tampoco lanza fallo de sistema: el ping siempre
     * pinta estado.
     *
     * @param nombre nombre del sistema en minusculas
     * @return estado del sistema pedido
     * @throws SistemaNoEncontradoException si el nombre no es un sistema conocido (404)
     */
    EstadoSistemaDTO pingSistema(String nombre);

    /**
     * Estado de los cuatro sistemas externos monitorizados (SSO, HCE, RIS y PACS).
     *
     * @return lista de estados, en el orden SSO, HCE, RIS, PACS
     */
    List<EstadoSistemaDTO> estadoSistemas();

    /**
     * Estado de la cadena de auditoria del subdominio de integracion: sus eslabones
     * con el hash encadenado (SHA-256) y el resultado de verificar la integridad.
     *
     * @return cadena de auditoria con marca de integridad
     */
    AuditoriaIntegracionDTO estadoAuditoriaIntegracion();

    /**
     * Cabecera de un estudio importada (sin imagenes).
     *
     * @param accessionNumber numero de acceso
     * @param pacienteId      historia clinica del paciente
     * @param fechaEstudio    fecha del estudio
     * @param numeroSeries    numero de series del estudio
     */
    record EstudioDTO(String accessionNumber, String pacienteId, LocalDate fechaEstudio, int numeroSeries) {
    }

    /**
     * Credenciales que presenta el usuario al IdP externo (1.1.0).
     *
     * @param usuario   usuario registrado en el IdP
     * @param contrasena contrasena aceptada por el IdP
     */
    record CredencialesSsoDTO(String usuario, String contrasena) {
    }

    /**
     * Sesion emitida por el IdP externo tras un login SSO correcto (1.1.0).
     *
     * @param sujeto    identidad externa autenticada
     * @param rolExterno rol que el IdP asigna al sujeto
     * @param caducaEn  instante de caducidad de la sesion
     */
    record SesionSsoDTO(String sujeto, String rolExterno, Instant caducaEn) {
    }

    /**
     * Registro clinico traido del HCE externo (1.1.0).
     *
     * @param codigo  codigo del registro (p. ej. diagnostico o informe previo)
     * @param fecha   fecha clinica del registro
     * @param detalle texto legible del registro
     */
    record RegistroHistoriaDTO(String codigo, LocalDate fecha, String detalle) {
    }

    /**
     * Historia clinica externa sincronizada para un paciente (1.1.0).
     *
     * @param pacienteExternoId identificador del paciente en el HCE
     * @param reutilizado       {@code true} si la huella no cambio y se reutilizo la
     *                          instantanea previa (idempotencia de la sincronizacion)
     * @param huella            SHA-256 hex de la forma canonica del contenido
     * @param sincronizadaEn    instante de la ultima sincronizacion
     * @param registros         registros de la historia en orden canonico
     */
    record HistoriaSincronizadaDTO(String pacienteExternoId, boolean reutilizado, String huella,
                                   Instant sincronizadaEn, List<RegistroHistoriaDTO> registros) {
    }

    /**
     * Estado monitorizado de un sistema externo (1.1.0). El ping degradado lo
     * devuelve incluso cuando el sistema esta caido: el monitor necesita pintar el
     * estado, no recibir un 5xx.
     *
     * @param nombre         nombre en minusculas ({@code sso}, {@code hce}, {@code ris}, {@code pacs})
     * @param disponible     {@code false} si el sistema esta caido o sin respuesta
     * @param motivo         motivo del no-disponible (p. ej. {@code CAIDO}), {@code null} si esta disponible
     * @param latenciaMediaMs latencia media observada en las ultimas llamadas
     */
    record EstadoSistemaDTO(String nombre, boolean disponible, String motivo, long latenciaMediaMs) {
    }

    /**
     * Eslabon de la cadena de auditoria de integracion (1.1.0).
     *
     * @param secuencia    numero de eslabon, empezando en 1
     * @param accion       accion auditada ({@code SSO_LOGIN}, {@code HCE_SINCRONIZAR}, ...)
     * @param sistema      sistema externo implicado, {@code null} si no aplica
     * @param resultado    resultado de la accion ({@code OK}, {@code FALLO_CREDENCIALES}, ...)
     * @param actor        usuario ISO2 que provoco la accion
     * @param instante     momento del asiento
     * @param hashAnterior hash del eslabon previo (el genezis SHA-256 en el primero)
     * @param hash         SHA-256 hex del canonico de este eslabon mas el hash anterior
     */
    record EslabonAuditoriaDTO(long secuencia, String accion, String sistema, String resultado, String actor,
                               Instant instante, String hashAnterior, String hash) {
    }

    /**
     * Estado completo de la cadena de auditoria de integracion (1.1.0).
     *
     * @param cadenaIntegra  {@code true} si al recalcular los hashes la cadena no fue alterada
     * @param totalRegistros eslabones escritos (el genezis no se cuenta ni se devuelve)
     * @param ultimoHash     hash del ultimo eslabon escrito, el genezis si la cadena esta vacia
     * @param eslabones      eslabones de la cadena desde el de secuencia 1 (el genezis no se lista)
     */
    record AuditoriaIntegracionDTO(boolean cadenaIntegra, int totalRegistros, String ultimoHash,
                                   List<EslabonAuditoriaDTO> eslabones) {
    }
}
