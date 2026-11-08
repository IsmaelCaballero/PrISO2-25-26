package es.uclm.iso2.catalogo.servicio;

import es.uclm.iso2.catalogo.dominio.AuditoriaCatalogo;
import es.uclm.iso2.catalogo.repositorio.AuditoriaCatalogoRepositorio;
import es.uclm.iso2.catalogo.repositorio.TipoPruebaRepositorio;
import es.uclm.iso2.common.catalogo.CatalogoConflictoException;
import es.uclm.iso2.common.catalogo.CatalogoNoEncontradoException;
import es.uclm.iso2.common.catalogo.PruebaCatalogoFacade.PruebaImagenDTO;
import es.uclm.iso2.common.catalogo.PruebaCatalogoFacade.ProtocoloDTO;
import es.uclm.iso2.common.catalogo.PruebaCatalogoFacade.PoliticaDTO;
import es.uclm.iso2.common.shared.Paginacion;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Pruebas tecnicas del servicio de catalogo (CDU16) sobre H2 en memoria.
 * Son humo/integracion del agente (D28): los TC humanos de CDU16 los disena Ismael
 * sobre el codigo ya visible y el agente los traduce a JUnit antes de cerrar la iteracion.
 */
@DataJpaTest
@Import(CatalogoServicio.class)
class CatalogoServicioTest {

    @Autowired
    private CatalogoServicio servicio;

    @Autowired
    private TipoPruebaRepositorio pruebas;

    @Autowired
    private AuditoriaCatalogoRepositorio auditoria;

    @Autowired
    private TestEntityManager em;

    private static PruebaImagenDTO prueba(String codigo, String nombre, int duracion) {
        return PruebaImagenDTO.sinEstado(codigo, nombre, "RM", true, duracion,
                new ProtocoloDTO("ayunar 4 h", 4, "marcapasos"),
                new PoliticaDTO(24, 3));
    }

    @Test
    void elAltaNaceActivaYDejaAuditoria() {
        PruebaImagenDTO creada = servicio.crear(prueba("RM-CERVICAL", "RM cervical", 25));

        assertThat(creada.activa()).isTrue();
        assertThat(creada.protocolo().ayunoHoras()).isEqualTo(4);
        assertThat(auditoria.findAll()).singleElement()
                .satisfies(r -> {
                    assertThat(r.getAccion()).isEqualTo(AuditoriaCatalogo.AccionAuditoria.ALTA);
                    assertThat(r.getHashAnterior()).isNull();
                });
    }

    @Test
    void elCodigoRepetidoProvocaConflicto() {
        servicio.crear(prueba("TAC-TORAX", "TAC torax", 10));

        assertThatThrownBy(() -> servicio.crear(prueba("TAC-TORAX", "otra", 15)))
                .isInstanceOf(CatalogoConflictoException.class);
    }

    @Test
    void losCodigosDePruebasDadasDeBajaNoSeReutilizan() {
        servicio.crear(prueba("ECO-ABD", "Ecografia abdominal", 15));
        servicio.desactivar("ECO-ABD");

        assertThatThrownBy(() -> servicio.crear(prueba("ECO-ABD", "reutilizada", 20)))
                .isInstanceOf(CatalogoConflictoException.class);
    }

    @Test
    void actualizarCambiaParametrosYPreservaElEstado() {
        servicio.crear(prueba("RX-TOBILLO", "RX tobillo", 8));
        servicio.desactivar("RX-TOBILLO");

        PruebaImagenDTO modificada = servicio.actualizar("RX-TOBILLO", prueba("RX-TOBILLO", "RX tobillo bilateral", 12));

        assertThat(modificada.nombre()).isEqualTo("RX tobillo bilateral");
        assertThat(modificada.duracionMinutos()).isEqualTo(12);
        assertThat(modificada.activa()).isFalse();
    }

    @Test
    void actualizarConCodigoQueNoCoincideEsErrorDeValidacion() {
        servicio.crear(prueba("RM-R ODILLA", "RM rodilla", 30));

        assertThatThrownBy(() -> servicio.actualizar("RM-RODILLA", prueba("RM-CODILLA", "mal", 30)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void operarSobrePruebaInexistenteDevuelveNoEncontrado() {
        assertThat(servicio.buscarPorCodigo("NO-EXISTE")).isEmpty();
        assertThatThrownBy(() -> servicio.actualizar("NO-EXISTE", prueba("NO-EXISTE", "x", 1)))
                .isInstanceOf(CatalogoNoEncontradoException.class);
        assertThatThrownBy(() -> servicio.desactivar("NO-EXISTE"))
                .isInstanceOf(CatalogoNoEncontradoException.class);
        assertThatThrownBy(() -> servicio.reactivar("NO-EXISTE"))
                .isInstanceOf(CatalogoNoEncontradoException.class);
    }

    @Test
    void laBajaEsLogicaYLaReactivacionRecuperaLaMismaFila() {
        servicio.crear(prueba("DENS-DRA", "Densitometria", 10));
        PruebaImagenDTO bajada = servicio.desactivar("DENS-DRA");
        assertThat(bajada.activa()).isFalse();

        Long idOriginal = pruebas.findByCodigo("DENS-DRA").orElseThrow().getId();

        PruebaImagenDTO reactivada = servicio.reactivar("DENS-DRA");
        assertThat(reactivada.activa()).isTrue();
        assertThat(pruebas.findByCodigo("DENS-DRA").orElseThrow().getId()).isEqualTo(idOriginal);

        // segunda desactivacion real: genera una segunda auditoria de BAJA
        servicio.desactivar("DENS-DRA");
        long bajas = auditoria.findAll().stream()
                .filter(r -> r.getAccion() == AuditoriaCatalogo.AccionAuditoria.BAJA).count();
        assertThat(bajas).isEqualTo(2L);

        // repetir la desactivacion sobre una prueba ya desactivada es idempotente:
        // no anade otra auditoria de BAJA
        servicio.desactivar("DENS-DRA");
        assertThat(auditoria.findAll().stream()
                .filter(r -> r.getAccion() == AuditoriaCatalogo.AccionAuditoria.BAJA).count())
                .isEqualTo(bajas);
    }

    @Test
    void listarIncluyeDesactivadasYOrdenaPorCodigoPorDefecto() {
        servicio.crear(prueba("TAC-CABEZA", "TAC craneal", 7));
        servicio.crear(prueba("RM-PELVICA", "RM pelvica", 35));
        servicio.desactivar("RM-PELVICA");

        List<PruebaImagenDTO> listado = servicio.listar(new Paginacion(0, 10, null));

        assertThat(listado).extracting(PruebaImagenDTO::codigo)
                .containsExactly("RM-PELVICA", "TAC-CABEZA");
        assertThat(listado).extracting(PruebaImagenDTO::activa).containsExactly(false, true);
    }

    @Test
    void unOrdenNoPermitidoEsErrorDeValidacion() {
        assertThatThrownBy(() -> servicio.listar(new Paginacion(0, 10, "password")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void laCadenaDeAuditoriaEsVerificableYDetectaManipulaciones() {
        servicio.crear(prueba("RM-HIGADO", "RM hepatica", 40));
        servicio.actualizar("RM-HIGADO", prueba("RM-HIGADO", "RM hepatica con contraste", 45));
        servicio.desactivar("RM-HIGADO");

        assertThat(servicio.auditoriaVerificable()).isTrue();
        assertThat(auditoria.count()).isEqualTo(3L);

        AuditoriaCatalogo primero = auditoria.findAll(org.springframework.data.domain.Sort.by("id")).getFirst();
        org.springframework.test.util.ReflectionTestUtils.setField(primero, "hash",
                "0000000000000000000000000000000000000000000000000000000000000000");
        em.flush();

        assertThat(servicio.auditoriaVerificable()).isFalse();
    }
}
