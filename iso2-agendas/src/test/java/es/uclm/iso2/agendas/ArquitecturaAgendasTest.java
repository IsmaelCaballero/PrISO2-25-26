package es.uclm.iso2.agendas;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Puerta de arquitectura del modulo agendas (CFG-03): las capas solo dependen hacia
 * abajo y el contrato comun nunca mira hacia el modulo. El CI exige 0 violaciones (D27).
 */
class ArquitecturaAgendasTest {

    private final JavaClasses clases = new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .importPackages("es.uclm.iso2.agendas", "es.uclm.iso2.common.agendas");

    @Test
    void elDominioNoDependeDeLasCapasSuperiores() {
        ArchRule regla = noClasses().that().resideInAPackage("..agendas.dominio..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..agendas.servicio..", "..agendas.web..", "..agendas.repositorio..");
        regla.check(clases);
    }

    @Test
    void laCapaWebNoAccedeDirectamenteAlRepositorio() {
        ArchRule regla = noClasses().that().resideInAPackage("..agendas.web..")
                .should().dependOnClassesThat().resideInAPackage("..agendas.repositorio..");
        regla.check(clases);
    }

    @Test
    void elServicioNoDependeDeLaCapaWeb() {
        ArchRule regla = noClasses().that().resideInAPackage("..agendas.servicio..")
                .should().dependOnClassesThat().resideInAPackage("..agendas.web..");
        regla.check(clases);
    }

    @Test
    void losContratosComunesNoMiranAlModulo() {
        ArchRule regla = noClasses().that().resideInAPackage("es.uclm.iso2.common..")
                .should().dependOnClassesThat().resideInAPackage("es.uclm.iso2.agendas..");
        regla.check(clases);
    }
}
