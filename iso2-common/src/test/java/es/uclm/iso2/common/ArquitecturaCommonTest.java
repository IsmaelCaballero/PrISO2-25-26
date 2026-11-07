package es.uclm.iso2.common;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * rieles de arquitectura del modulo de contratos (D24/D27):
 * se ejecutan en cada PR y bloquean desvios del baseline contractual.
 */
class ArquitecturaCommonTest {

    private static JavaClasses clasesComunes;

    @BeforeAll
    static void importarContratos() {
        clasesComunes = new ClassFileImporter()
                .withImportOption(new ImportOption.DoNotIncludeTests())
                .importPackages("es.uclm.iso2.common");
    }

    @Test
    void commonNoDependeDeFrameworks() {
        ArchRule regla = noClasses()
                .that().resideInAPackage("es.uclm.iso2.common..")
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                        "org.springframework..",
                        "org.openjfx..",
                        "javafx..",
                        "jakarta..",
                        "com.fasterxml..");
        regla.check(clasesComunes);
    }

    @Test
    void subdominiosLibresDeCiclos() {
        ArchRule regla = SlicesRuleDefinition.slices()
                .matching("es.uclm.iso2.common.(*)..")
                .should().beFreeOfCycles();
        regla.check(clasesComunes);
    }
}
