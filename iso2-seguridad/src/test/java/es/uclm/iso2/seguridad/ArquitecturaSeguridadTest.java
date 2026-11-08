package es.uclm.iso2.seguridad;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Puerta de arquitectura del modulo seguridad (CFG-02): capas solo hacia abajo y el
 * contrato comun nunca mira al modulo. Espejo de {@code ArquitecturaCatalogoTest}
 * (D27: el CI exige 0 violaciones).
 */
class ArquitecturaSeguridadTest {

    private final JavaClasses clases = new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .importPackages("es.uclm.iso2.seguridad", "es.uclm.iso2.common.seguridad");

    @Test
    void elDominioNoDependeDeLasCapasSuperiores() {
        ArchRule regla = noClasses().that().resideInAPackage("..seguridad.dominio..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..seguridad.servicio..", "..seguridad.web..", "..seguridad.repositorio..");
        regla.check(clases);
    }

    @Test
    void laCapaWebNoAccedeDirectamenteAlRepositorio() {
        ArchRule regla = noClasses().that().resideInAPackage("..seguridad.web..")
                .should().dependOnClassesThat().resideInAPackage("..seguridad.repositorio..");
        regla.check(clases);
    }

    @Test
    void elServicioNoDependeDeLaCapaWeb() {
        ArchRule regla = noClasses().that().resideInAPackage("..seguridad.servicio..")
                .should().dependOnClassesThat().resideInAPackage("..seguridad.web..");
        regla.check(clases);
    }

    @Test
    void losContratosComunesNoMiranAlModulo() {
        ArchRule regla = noClasses().that().resideInAPackage("es.uclm.iso2.common..")
                .should().dependOnClassesThat().resideInAPackage("es.uclm.iso2.seguridad..");
        regla.check(clases);
    }
}
