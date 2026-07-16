package org.tkit.onecx.onecxsvcgen.service;

import jakarta.enterprise.context.ApplicationScoped;

import java.nio.file.Files;
import java.nio.file.Path;

@ApplicationScoped
public class ProjectStructureService {

    public void ensureMainStructure(Path base, Path projectPath) throws Exception {
        Files.createDirectories(base.resolve("domain/models"));
        Files.createDirectories(base.resolve("domain/daos"));
        Files.createDirectories(base.resolve("domain/services"));
        Files.createDirectories(base.resolve("rs/internal/controllers"));
        Files.createDirectories(base.resolve("rs/internal/mappers"));
        Files.createDirectories(base.resolve("rs/external/v1/controllers"));
        Files.createDirectories(base.resolve("rs/external/v1/mappers"));
        Files.createDirectories(projectPath.resolve("src/main/resources/db/changelog"));
    }

    public void ensureTestStructure(Path testBase, Path projectPath) throws Exception {
        Files.createDirectories(testBase.resolve("rs/internal/controllers"));
        Files.createDirectories(testBase.resolve("rs/external/v1/controllers"));
        createFileIfMissing(projectPath.resolve("src/test/resources/application.properties"), "");
    }

    private void createFileIfMissing(Path file, String content) throws Exception {
        if (!Files.exists(file)) {
            Files.createDirectories(file.getParent());
            Files.writeString(file, content);
        }
    }
}

