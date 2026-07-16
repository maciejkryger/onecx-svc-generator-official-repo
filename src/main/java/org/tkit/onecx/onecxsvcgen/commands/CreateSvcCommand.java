package org.tkit.onecx.onecxsvcgen.commands;

import jakarta.inject.Inject;
import org.tkit.onecx.onecxsvcgen.model.CreateSvcRequest;
import org.tkit.onecx.onecxsvcgen.service.GeneratorService;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;

@Command(name = "create-svc", description = "Generate a full OneCX-compliant Quarkus backend service")
public class CreateSvcCommand implements Runnable {

    @Option(names = "--name", required = true, description = "Project/repository name, e.g. onecx-demo-svc")
    String name;

    @Option(names = { "--group", "--group-id" }, defaultValue = "org.tkit.onecx", description = "Maven groupId")
    String groupId;

    @Option(names = "--artifact-id", description = "Maven artifactId (defaults to sanitized project name)")
    String artifactId;

    @Option(names = "--package", required = true, description = "Base Java package")
    String pkg;

    @Option(names = "--output-dir", description = "Directory where the service project should be generated")
    Path outputDir;

    @Option(names = "--github-token", description = "GitHub token used to resolve latest dependency versions")
    String githubToken;

    @Option(
            names = "--build",
            defaultValue = "false",
            fallbackValue = "true",
            arity = "0..1",
            description = "Run 'mvn clean package -DskipTests' in the generated project after generation"
    )
    boolean build;

    @Inject
    GeneratorService generatorService;

    @Override
    public void run() {
        try {
            Path root = generatorService.generate(new CreateSvcRequest(name, groupId, artifactId, pkg, outputDir, githubToken,
                    build));
            System.out.println("✔ Generated OneCX service in: " + root.toAbsolutePath());
        } catch (Exception e) {
            throw new RuntimeException("create-svc failed", e);
        }
    }
}