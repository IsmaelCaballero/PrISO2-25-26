package es.uclm.iso2.common.catalogo;

import es.uclm.iso2.common.shared.Paginacion;
import java.util.List;
import java.util.Optional;

/**
 * Fachada de contratos del subdominio {@code catalogo}: Catalogo clinico y administrativo de
 * pruebas de imagen (CDU16).
 * Solo contrato: la implementacion vive en el modulo iso2-catalogo.
 *
 * <p>Reglas de negocio del contrato (CDU16, RF16):</p>
 * <ul>
 *   <li>El {@code codigo} de prueba es unico e inmutable; reutilizar un codigo de una prueba
 *       desactivada se considera conflicto, no reactivacion implicita.</li>
 *   <li>Las pruebas nunca se borran fisicamente: la baja es logica ({@code activa = false}),
 *       para no romper trazabilidad de solicitudes y citas ya creadas.</li>
 *   <li>Una prueba desactivada no aparece en las busquedas operativas (solicitudes/agendas)
 *       pero si en la gestion de catalogo con {@link #listar(Paginacion)}.</li>
 * </ul>
 *
 * <p>Los errores de negocio se senalizan con excepciones tipadas de la implementacion
 * (no encontrada, codigo duplicado, validacion); cada aplicacion los traduce a su formato
 * (HTTP 404/409/400 en el servidor).</p>
 *
 * @since 1.0.0 (CFG-00) baseline · ampliado en 1.1.0 (CFG-01, CDU16)
 */
public interface PruebaCatalogoFacade {

    /**
     * Lista las pruebas del catalogo con paginacion (incluye las desactivadas: es la vista
     * de gestion administrativa). Orden por defecto: codigo ascendente.
     *
     * @param paginacion parametros de pagina, tamano y orden
     * @return pagina de pruebas del catalogo
     */
    List<PruebaImagenDTO> listar(Paginacion paginacion);

    /**
     * Busca una prueba por su codigo de catalogo (este contrato devuelve tambien las
     * desactivadas; el estado se lee en {@link PruebaImagenDTO#activa()}).
     *
     * @param codigo codigo unico de la prueba
     * @return la prueba si existe, vacio en caso contrario
     */
    Optional<PruebaImagenDTO> buscarPorCodigo(String codigo);

    /**
     * Alta de una prueba nueva en el catalogo (CDU16).
     *
     * @param prueba datos de la prueba; {@code activa} del DTO se ignora (nace activa)
     * @return la prueba creada con su estado persistido
     * @throws IllegalArgumentException     si la validacion del DTO falla
     * @throws es.uclm.iso2.common.catalogo.CatalogoConflictoException si el codigo ya existe
     */
    PruebaImagenDTO crear(PruebaImagenDTO prueba);

    /**
     * Actualiza los parametros de una prueba existente. El codigo del DTO debe coincidir
     * con el de la ruta/recurso; el campo {@code activa} se ignora (se gestiona con
     * {@link #desactivar(String)} y {@link #reactivar(String)}).
     *
     * @param codigo codigo de la prueba a actualizar
     * @param prueba nuevos datos de la prueba
     * @return la prueba actualizada
     * @throws IllegalArgumentException     si la validacion del DTO falla o el codigo no coincide
     * @throws es.uclm.iso2.common.catalogo.CatalogoNoEncontradoException si no existe la prueba
     */
    PruebaImagenDTO actualizar(String codigo, PruebaImagenDTO prueba);

    /**
     * Baja logica de una prueba ({@code activa = false}). Las pruebas ya desactivadas
     * se dejan estar (operacion idempotente).
     *
     * @param codigo codigo de la prueba a desactivar
     * @return la prueba desactivada
     * @throws es.uclm.iso2.common.catalogo.CatalogoNoEncontradoException si no existe la prueba
     */
    PruebaImagenDTO desactivar(String codigo);

    /**
     * Reactiva una prueba desactivada. Si el codigo esta libre no hay conflicto porque
     * los codigos no se reutilizan: la reactivacion afecta a la misma fila historica.
     *
     * @param codigo codigo de la prueba a reactivar
     * @return la prueba reactivada
     * @throws es.uclm.iso2.common.catalogo.CatalogoNoEncontradoException si no existe la prueba
     */
    PruebaImagenDTO reactivar(String codigo);

    /**
     * Prueba de imagen del catalogo con sus parametros de protocolo y politica (CFG-01).
     *
     * @param codigo          codigo unico e inmutable de la prueba (ej. {@code RM-CERVICAL})
     * @param nombre          nombre legible para profesionales (ej. {@code Resonancia magnetica cervical})
     * @param categoria       agrupacion por modalidad: RX, TAC, RM, ECO, MED-NUC, DENS, OTRO
     * @param requiereCita    si la prueba necesita cita previa con recursos asignables
     * @param duracionMinutos duracion estimada de la ejecucion, para encaje de agendas (&gt; 0)
     * @param activa          estado de baja logica (CDU16: nunca hay borrado fisico)
     * @param protocolo       parametros de preparacion del paciente
     * @param politica        parametros administrativos de cancelacion
     * @since 1.1.0 (CFG-01)
     */
    record PruebaImagenDTO(
            String codigo,
            String nombre,
            String categoria,
            boolean requiereCita,
            int duracionMinutos,
            boolean activa,
            ProtocoloDTO protocolo,
            PoliticaDTO politica) {

        /**
         * Construye la prueba validando los parametros exigidos por el contrato.
         *
         * @throws IllegalArgumentException si algun campo obligatorio o numerico es invalido
         */
        public PruebaImagenDTO {
            if (codigo == null || codigo.isBlank()) {
                throw new IllegalArgumentException("el codigo de prueba es obligatorio");
            }
            if (nombre == null || nombre.isBlank()) {
                throw new IllegalArgumentException("el nombre de prueba es obligatorio");
            }
            if (categoria == null || categoria.isBlank()) {
                throw new IllegalArgumentException("la categoria de prueba es obligatoria");
            }
            if (duracionMinutos <= 0) {
                throw new IllegalArgumentException("la duracion en minutos debe ser positiva");
            }
            if (protocolo == null) {
                throw new IllegalArgumentException("el protocolo es obligatorio");
            }
            if (politica == null) {
                throw new IllegalArgumentException("la politica es obligatoria");
            }
        }

        /**
         * Variante sin estado: se usa en altas y actualizaciones, donde {@code activa}
         * lo fija el servicio (alta = activa; modificacion no cambia el estado).
         *
         * @param codigo          codigo unico de la prueba
         * @param nombre          nombre legible
         * @param categoria       modalidad de imagen
         * @param requiereCita    si necesita cita previa
         * @param duracionMinutos duracion estimada en minutos
         * @param protocolo       parametros de preparacion
         * @param politica        parametros administrativos
         * @return DTO sin estado de baja logica
         */
        public static PruebaImagenDTO sinEstado(
                String codigo,
                String nombre,
                String categoria,
                boolean requiereCita,
                int duracionMinutos,
                ProtocoloDTO protocolo,
                PoliticaDTO politica) {
            return new PruebaImagenDTO(codigo, nombre, categoria, requiereCita, duracionMinutos,
                    true, protocolo, politica);
        }
    }

    /**
     * Parametros de preparacion del paciente para una prueba (CDU16: "protocolos").
     *
     * @param preparacion        instrucciones de preparacion (texto libre, obligatorio)
     * @param ayunoHoras         horas de ayuno previo exigidas (&ge; 0)
     * @param contraindicaciones texto de contraindicaciones conocidas (obligatorio, puede ser "ninguna")
     * @since 1.1.0 (CFG-01)
     */
    record ProtocoloDTO(String preparacion, int ayunoHoras, String contraindicaciones) {

        /**
         * Construye el protocolo validando los limites pactados.
         *
         * @throws IllegalArgumentException si faltan textos o el ayuno es negativo
         */
        public ProtocoloDTO {
            if (preparacion == null || preparacion.isBlank()) {
                throw new IllegalArgumentException("la preparacion del protocolo es obligatoria");
            }
            if (ayunoHoras < 0) {
                throw new IllegalArgumentException("las horas de ayuno no pueden ser negativas");
            }
            if (contraindicaciones == null || contraindicaciones.isBlank()) {
                throw new IllegalArgumentException("las contraindicaciones son obligatorias (use 'ninguna')");
            }
        }
    }

    /**
     * Parametros administrativos de una prueba (CDU16: "politicas").
     *
     * @param plazoCancelacionHoras horas minimas de antelacion para poder cancelar una cita
     *                              de esta prueba (&gt; 0)
     * @param maxCancelaciones      cancelaciones permitidas por paciente y prueba antes de
     *                              bloqueo administrativo (&ge; 0)
     * @since 1.1.0 (CFG-01)
     */
    record PoliticaDTO(int plazoCancelacionHoras, int maxCancelaciones) {

        /**
         * Construye la politica validando los limites pactados.
         *
         * @throws IllegalArgumentException si el plazo no es positivo o el maximo es negativo
         */
        public PoliticaDTO {
            if (plazoCancelacionHoras <= 0) {
                throw new IllegalArgumentException("el plazo de cancelacion debe ser positivo");
            }
            if (maxCancelaciones < 0) {
                throw new IllegalArgumentException("el maximo de cancelaciones no puede ser negativo");
            }
        }
    }
}
