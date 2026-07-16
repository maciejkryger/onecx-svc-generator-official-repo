package org.tkit.onecx.onecxsvcgen.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ProjectStructureServiceTest {

    private final ProjectStructureService service = new ProjectStructureService();

    @Test
    void ensureMainStructureCreatesExpectedDirectories(@TempDir Path projectDir) throws Exception {
        Path base = projectDir.resolve("src/main/java/org/example");

        service.ensureMainStructure(base, projectDir);

        assertTrue(Files.isDirectory(base.resolve("domain/models")));
        assertTrue(Files.isDirectory(base.resolve("domain/daos")));
        assertTrue(Files.isDirectory(base.resolve("domain/services")));
        assertTrue(Files.isDirectory(base.resolve("rs/internal/controllers")));
        assertTrue(Files.isDirectory(base.resolve("rs/internal/mappers")));
        assertTrue(Files.isDirectory(base.resolve("rs/external/v1/controllers")));
        assertTrue(Files.isDirectory(base.resolve("rs/external/v1/mappers")));
        assertTrue(Files.isDirectory(projectDir.resolve("src/main/resources/db/changelog")));
    }

    @Test
    void ensureTestStructureCreatesExpectedDirectories(@TempDir Path projectDir) throws Exception {
        Path testBase = projectDir.resolve("src/test/java/org/example");

        service.ensureTestStructure(testBase, projectDir);

        assertTrue(Files.isDirectory(testBase.resolve("rs/internal/controllers")));
        assertTrue(Files.isDirectory(testBase.resolve("rs/external/v1/controllers")));
        assertTrue(Files.exists(projectDir.resolve("src/test/resources/application.properties")));
    }

    @Test
    void ensureTestStructureDoesNotOverwriteExistingApplicationProperties(@TempDir Path projectDir) throws Exception {
        Path propertiesFile = projectDir.resolve("src/test/resources/application.properties");
        Files.createDirectories(propertiesFile.getParent());
        Files.writeString(propertiesFile, "existing=content");

        Path testBase = projectDir.resolve("src/test/java/org/example");
        service.ensureTestStructure(testBase, projectDir);

        assertTrue(Files.readString(propertiesFile).contains("existing=content"),
                "Existing application.properties should not be overwritten");
    }

    @Test
    void ensureMainStructureIsIdempotent(@TempDir Path projectDir) throws Exception {
        Path base = projectDir.resolve("src/main/java/org/example");

        service.ensureMainStructure(base, projectDir);
        service.ensureMainStructure(base, projectDir);

        assertTrue(Files.isDirectory(base.resolve("domain/models")),
                "Calling twice must not fail and directories must still exist");
    }
}

