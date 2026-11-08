package es.uclm.iso2.solicitudes;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Puerta de arquitectura del modulo solicitudes (CFG-04): las capas solo dependen hacia
 * abajo y el contrato comun nunca mira hacia el modulo. El CI exige 0 violaciones (D27).
 */
class ArquitecturaSolicitudesTest {

    private final JavaClasses clases = new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .importPackages("es.uclm.iso2.solicitudes", "es.uclm.iso2.common.solicitudes");

    @Test
    void elDominioNoDependeDeLasCapasSuperiores() {
        ArchRule regla = noClasses().that().resideInAPackage("..solicitudes.dominio..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..solicitudes.servicio..", "..solicitudes.web..", "..solicitudes.repositorio..");
        regla.check(clases);
    }

    @Test
    void laCapaWebNoAccedeDirectamenteAlRepositorio() {
        ArchRule regla = noClasses().that().resideInAPackage("..solicitudes.web..")
                .should().dependOnClassesThat().resideInAPackage("..solicitudes.repositorio..");
        regla.check(clases);
    }

    @Test
    void elServicioNoDependeDeLaCapaWeb() {
        ArchRule regla = noClasses().that().resideInAPackage("..solicitudes.servicio..")
                .should().dependOnClassesThat().resideInAPackage("..solicitudes.web..");
        regla.check(clases);
    }

    @Test
    void losContratosComunesNoMiranAlModulo() {
        ArchRule regla = noClasses().that().resideInAPackage("es.uclm.iso2.common..")
                .should().dependOnClassesThat().resideInAPackage("es.uclm.iso2.solicitudes..");
        regla.check(clases);
    }
}
