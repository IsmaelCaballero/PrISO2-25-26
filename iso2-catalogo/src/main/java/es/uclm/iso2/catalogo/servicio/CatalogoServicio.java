package es.uclm.iso2.catalogo.servicio;

import es.uclm.iso2.catalogo.dominio.AuditoriaCatalogo;
import es.uclm.iso2.catalogo.dominio.Politica;
import es.uclm.iso2.catalogo.dominio.Protocolo;
import es.uclm.iso2.catalogo.dominio.TipoPrueba;
import es.uclm.iso2.catalogo.repositorio.AuditoriaCatalogoRepositorio;
import es.uclm.iso2.catalogo.repositorio.TipoPruebaRepositorio;
import es.uclm.iso2.common.catalogo.CatalogoConflictoException;
import es.uclm.iso2.common.catalogo.CatalogoNoEncontradoException;
import es.uclm.iso2.common.catalogo.PruebaCatalogoFacade;
import es.uclm.iso2.common.shared.Paginacion;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementacion del catalogo de pruebas (CDU16): alta, modificacion, baja logica y
 * reactivacion con auditoria encadenada por SHA-256.
 *
 * <p>Toda escritura termina en un registro {@link AuditoriaCatalogo} cuyo hash enlaza con
 * el del registro anterior; el usuario se toma del contexto de seguridad (HttpBasic hasta
 * CDU17, JWT a partir de entonces) y se registra como {@code sistema} si no hay autenticacion.</p>
 *
 * @since 1.1.0 (CFG-01)
 */
@Service
@Transactional(readOnly = true)
public class CatalogoServicio implements PruebaCatalogoFacade {

    /** Orden por defecto de {@link #listar(Paginacion)}. */
    private static final String ORDEN_POR_DEFECTO = "codigo";

    private final TipoPruebaRepositorio pruebas;
    private final AuditoriaCatalogoRepositorio auditoria;

    /**
     * Inyecta los repositorios JPA del catalogo y su auditoria.
     *
     * @param pruebas   repositorio de tipos de prueba
     * @param auditoria repositorio de auditoria encadenada
     */
    public CatalogoServicio(TipoPruebaRepositorio pruebas, AuditoriaCatalogoRepositorio auditoria) {
        this.pruebas = pruebas;
        this.auditoria = auditoria;
    }

    @Override
    public List<PruebaImagenDTO> listar(Paginacion paginacion) {
        Sort sort = Sort.by(permitido(paginacion.orden()));
        return pruebas.findAll(PageRequest.of(paginacion.pagina(), paginacion.tamano(), sort))
                .map(CatalogoServicio::aDTO)
                .getContent();
    }

    @Override
    public Optional<PruebaImagenDTO> buscarPorCodigo(String codigo) {
        return pruebas.findByCodigo(codigo).map(CatalogoServicio::aDTO);
    }

    @Override
    @Transactional
    public PruebaImagenDTO crear(PruebaImagenDTO prueba) {
        if (pruebas.existsByCodigo(prueba.codigo())) {
            throw new CatalogoConflictoException(
                    "el codigo de prueba " + prueba.codigo() + " ya existe en el catalogo");
        }
        TipoPrueba entidad = new TipoPrueba(prueba.codigo(), prueba.nombre(), prueba.categoria(),
                prueba.requiereCita(), prueba.duracionMinutos(), true,
                aProtocolo(prueba.protocolo()), aPolitica(prueba.politica()));
        TipoPrueba guardada = pruebas.save(entidad);
        registrar(AuditoriaCatalogo.AccionAuditoria.ALTA, guardada.getCodigo());
        return aDTO(guardada);
    }

    @Override
    @Transactional
    public PruebaImagenDTO actualizar(String codigo, PruebaImagenDTO prueba) {
        if (prueba.codigo() == null || !prueba.codigo().equals(codigo)) {
            throw new IllegalArgumentException(
                    "el codigo del cuerpo (" + prueba.codigo() + ") no coincide con la ruta (" + codigo + ")");
        }
        TipoPrueba entidad = buscarEntidad(codigo);
        entidad.actualizarDatos(prueba.nombre(), prueba.categoria(), prueba.requiereCita(),
                prueba.duracionMinutos(), aProtocolo(prueba.protocolo()), aPolitica(prueba.politica()));
        registrar(AuditoriaCatalogo.AccionAuditoria.MODIFICACION, codigo);
        return aDTO(entidad);
    }

    @Override
    @Transactional
    public PruebaImagenDTO desactivar(String codigo) {
        TipoPrueba entidad = buscarEntidad(codigo);
        if (entidad.isActiva()) {
            entidad.cambiarEstado(false);
            registrar(AuditoriaCatalogo.AccionAuditoria.BAJA, codigo);
        }
        return aDTO(entidad);
    }

    @Override
    @Transactional
    public PruebaImagenDTO reactivar(String codigo) {
        TipoPrueba entidad = buscarEntidad(codigo);
        if (!entidad.isActiva()) {
            entidad.cambiarEstado(true);
            registrar(AuditoriaCatalogo.AccionAuditoria.REACTIVACION, codigo);
        }
        return aDTO(entidad);
    }

    /**
     * Comprueba el encadenado de la auditoria recalculando los hashes: si alguien altero o
     * elimino un registro, la cadena deja de cuadrar y este metodo devuelve {@code false}.
     *
     * @return true si la cadena de auditoria es integridad-verificable
     */
    public boolean auditoriaVerificable() {
        String anterior = null;
        for (AuditoriaCatalogo registro : auditoria.findAll(Sort.by(Sort.Direction.ASC, "id"))) {
            boolean enlaza = anterior == null ? registro.getHashAnterior() == null
                    : anterior.equals(registro.getHashAnterior());
            boolean coherente = hashDe(registro.getAccion(), registro.getCodigoPrueba(),
                    registro.getUsuario(), registro.getInstante(), registro.getHashAnterior())
                    .equals(registro.getHash());
            if (!enlaza || !coherente) {
                return false;
            }
            anterior = registro.getHash();
        }
        return true;
    }

    private TipoPrueba buscarEntidad(String codigo) {
        return pruebas.findByCodigo(codigo)
                .orElseThrow(() -> new CatalogoNoEncontradoException(codigo));
    }

    private static String permitido(String orden) {
        if (orden == null || orden.isBlank()) {
            return ORDEN_POR_DEFECTO;
        }
        return switch (orden) {
            case "codigo", "nombre", "categoria", "duracionMinutos" -> orden;
            default -> throw new IllegalArgumentException("campo de orden no permitido: " + orden);
        };
    }

    private void registrar(AuditoriaCatalogo.AccionAuditoria accion, String codigo) {
        String hashAnterior = auditoria.findTopByOrderByIdDesc()
                .map(AuditoriaCatalogo::getHash)
                .orElse(null);
        Instant ahora = Instant.now();
        String usuario = usuarioEnSeguridad();
        String hash = hashDe(accion, codigo, usuario, ahora, hashAnterior);
        auditoria.save(new AuditoriaCatalogo(accion, codigo, usuario, ahora, hash, hashAnterior));
    }

    private static String usuarioEnSeguridad() {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        return autenticacion == null ? "sistema" : autenticacion.getName();
    }

    /**
     * Calcula el hash SHA-256 (hex) de un registro de auditoria.
     *
     * @param accion       accion
     * @param codigo       codigo de prueba
     * @param usuario      usuario responsable
     * @param instante     momento UTC
     * @param hashAnterior hash del registro anterior (null en el primero)
     * @return hash hex de 64 digitos
     */
    static String hashDe(AuditoriaCatalogo.AccionAuditoria accion, String codigo, String usuario,
                         Instant instante, String hashAnterior) {
        String material = accion.name() + "|" + codigo + "|" + usuario + "|" + instante
                + "|" + (hashAnterior == null ? "GENESIS" : hashAnterior);
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(sha.digest(material.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible en esta JVM", e);
        }
    }

    private static PruebaImagenDTO aDTO(TipoPrueba entidad) {
        return new PruebaImagenDTO(entidad.getCodigo(), entidad.getNombre(), entidad.getCategoria(),
                entidad.isRequiereCita(), entidad.getDuracionMinutos(), entidad.isActiva(),
                new ProtocoloDTO(entidad.getProtocolo().getPreparacion(),
                        entidad.getProtocolo().getAyunoHoras(),
                        entidad.getProtocolo().getContraindicaciones()),
                new PoliticaDTO(entidad.getPolitica().getPlazoCancelacionHoras(),
                        entidad.getPolitica().getMaxCancelaciones()));
    }

    private static Protocolo aProtocolo(ProtocoloDTO dto) {
        return new Protocolo(dto.preparacion(), dto.ayunoHoras(), dto.contraindicaciones());
    }

    private static Politica aPolitica(PoliticaDTO dto) {
        return new Politica(dto.plazoCancelacionHoras(), dto.maxCancelaciones());
    }
}
