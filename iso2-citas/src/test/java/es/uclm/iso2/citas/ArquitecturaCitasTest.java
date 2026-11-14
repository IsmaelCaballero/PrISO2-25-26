package es.uclm.iso2.citas;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Puerta de arquitectura del modulo citas (CFG-05): las capas solo dependen hacia abajo
 * y el contrato comun nunca mira hacia el modulo. El CI exige 0 violaciones (D27).
 */
class ArquitecturaCitasTest {

    private final JavaClasses clases = new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .importPackages("es.uclm.iso2.citas", "es.uclm.iso2.common.citas");

    @Test
    void elDominioNoDependeDeLasCapasSuperiores() {
        ArchRule regla = noClasses().that().resideInAPackage("..citas.dominio..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..citas.servicio..", "..citas.web..", "..citas.repositorio..");
        regla.check(clases);
    }

    @Test
    void laCapaWebNoAccedeDirectamenteAlRepositorio() {
        ArchRule regla = noClasses().that().resideInAPackage("..citas.web..")
                .should().dependOnClassesThat().resideInAPackage("..citas.repositorio..");
        regla.check(clases);
    }

    @Test
    void elServicioNoDependeDeLaCapaWeb() {
        ArchRule regla = noClasses().that().resideInAPackage("..citas.servicio..")
                .should().dependOnClassesThat().resideInAPackage("..citas.web..");
        regla.check(clases);
    }

    @Test
    void losContratosComunesNoMiranAlModulo() {
        ArchRule regla = noClasses().that().resideInAPackage("es.uclm.iso2.common..")
                .should().dependOnClassesThat().resideInAPackage("es.uclm.iso2.citas..");
        regla.check(clases);
    }
}
