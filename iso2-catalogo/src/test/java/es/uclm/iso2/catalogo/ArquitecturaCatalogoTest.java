package es.uclm.iso2.catalogo;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Puerta de arquitectura del modulo catalogo (CFG-01): las capas solo dependen hacia abajo
 * y el contrato comun nunca mira hacia el modulo. El CI exige 0 violaciones (D27).
 */
class ArquitecturaCatalogoTest {

    private final JavaClasses clases = new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .importPackages("es.uclm.iso2.catalogo", "es.uclm.iso2.common.catalogo");

    @Test
    void elDominioNoDependeDeLasCapasSuperiores() {
        ArchRule regla = noClasses().that().resideInAPackage("..catalogo.dominio..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..catalogo.servicio..", "..catalogo.web..", "..catalogo.repositorio..");
        regla.check(clases);
    }

    @Test
    void laCapaWebNoAccedeDirectamenteAlRepositorio() {
        ArchRule regla = noClasses().that().resideInAPackage("..catalogo.web..")
                .should().dependOnClassesThat().resideInAPackage("..catalogo.repositorio..");
        regla.check(clases);
    }

    @Test
    void elServicioNoDependeDeLaCapaWeb() {
        ArchRule regla = noClasses().that().resideInAPackage("..catalogo.servicio..")
                .should().dependOnClassesThat().resideInAPackage("..catalogo.web..");
        regla.check(clases);
    }

    @Test
    void losContratosComunesNoMiranAlModulo() {
        ArchRule regla = noClasses().that().resideInAPackage("es.uclm.iso2.common..")
                .should().dependOnClassesThat().resideInAPackage("es.uclm.iso2.catalogo..");
        regla.check(clases);
    }
}
