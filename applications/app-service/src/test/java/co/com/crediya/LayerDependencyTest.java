package co.com.crediya;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Reglas de dependencia de la arquitectura limpia del scaffold: las dependencias siempre
 * apuntan hacia el dominio. A diferencia de {@link ArchitectureTest} (generado por el
 * scaffold, solo nombres y avisos), estas reglas hacen fallar el build.
 */
class LayerDependencyTest {
    private static final String MODEL = "co.com.crediya.model..";
    private static final String USECASE = "co.com.crediya.usecase..";
    private static final String ENTRY_POINTS = "co.com.crediya.api..";
    private static final String[] DRIVEN_ADAPTERS = {"co.com.crediya.jpa..", "co.com.crediya.security..", "co.com.crediya.client.."};
    private static final String[] FRAMEWORKS = {
            "org.springframework..", "jakarta..", "com.fasterxml.jackson..", "tools.jackson..",
            "org.hibernate..", "com.nimbusds.."};

    private static JavaClasses classes;

    @BeforeAll
    static void importClasses() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("co.com.crediya");
    }

    @Test
    void modelDependsOnNoFrameworkNorOuterLayer() {
        noClasses().that().resideInAPackage(MODEL)
                .should().dependOnClassesThat().resideInAnyPackage(FRAMEWORKS)
                .orShould().dependOnClassesThat().resideInAnyPackage(USECASE, ENTRY_POINTS)
                .orShould().dependOnClassesThat().resideInAnyPackage(DRIVEN_ADAPTERS)
                .check(classes);
    }

    @Test
    void useCasesDependOnlyOnTheModel() {
        noClasses().that().resideInAPackage(USECASE)
                .should().dependOnClassesThat().resideInAnyPackage(FRAMEWORKS)
                .orShould().dependOnClassesThat().resideInAPackage(ENTRY_POINTS)
                .orShould().dependOnClassesThat().resideInAnyPackage(DRIVEN_ADAPTERS)
                .check(classes);
    }

    @Test
    void entryPointsDoNotUseDrivenAdapters() {
        noClasses().that().resideInAPackage(ENTRY_POINTS)
                .should().dependOnClassesThat().resideInAnyPackage(DRIVEN_ADAPTERS)
                .check(classes);
    }

    @Test
    void drivenAdaptersDoNotUseEntryPointsNorUseCases() {
        noClasses().that().resideInAnyPackage(DRIVEN_ADAPTERS)
                .should().dependOnClassesThat().resideInAnyPackage(ENTRY_POINTS, USECASE)
                .check(classes);
    }
}
