package es.uclm.iso2.integracion;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Puerta de arquitectura del spike CDU19 (CFG-06): el subdominio de integracion respeta
 * la hexagonal igual que citas y agendas, con el matiz del spike: el controlador de
 * simulacion si toca el dominio (es la pieza que instala modos de fallo para los TCs),
 * el resto de la capa web solo conoce el contrato comun. El CI exige 0 violaciones (D27).
 */
class ArquitecturaIntegracionTest {

    private final JavaClasses clases = new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .importPackages("es.uclm.iso2.integracion", "es.uclm.iso2.common.integracion");

    @Test
    void elDominioNoDependeDeLasCapasSuperiores() {
        ArchRule regla = noClasses().that().resideInAPackage("..integracion.dominio..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..integracion.servicio..", "..integracion.web..",
                        "..integracion.adaptador..", "..integracion.configuracion..");
        regla.check(clases);
    }

    @Test
    void losPuertosNoMiranHaciaAdaptadoresNiServicioNiWeb() {
        ArchRule regla = noClasses().that().resideInAPackage("..integracion.puerto..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..integracion.adaptador..", "..integracion.servicio..", "..integracion.web..");
        regla.check(clases);
    }

    @Test
    void elServicioNoDependeDeAdaptadoresNiDeLaCapaWeb() {
        ArchRule regla = noClasses().that().resideInAPackage("..integracion.servicio..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..integracion.adaptador..", "..integracion.web..");
        regla.check(clases);
    }

    @Test
    void laCapaWebNuncaDependeDeLaCapaAdaptador() {
        ArchRule regla = noClasses().that().resideInAPackage("..integracion.web..")
                .should().dependOnClassesThat().resideInAPackage("..integracion.adaptador..");
        regla.check(clases);
    }

    @Test
    void soloElControladorDeSimulacionTocaElDominio() {
        ArchRule regla = noClasses().that().resideInAPackage("..integracion.web..")
                .and().doNotHaveSimpleName("SimulacionControlador")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..integracion.dominio..", "..integracion.puerto..");
        regla.check(clases);
    }

    @Test
    void losContratosComunesNoMiranAlModulo() {
        ArchRule regla = noClasses().that().resideInAPackage("es.uclm.iso2.common..")
                .should().dependOnClassesThat().resideInAPackage("es.uclm.iso2.integracion..");
        regla.check(clases);
    }
}
