package org.tkit.onecx.onecxsvcgen.model;

import java.nio.file.Path;

public record CreateSvcRequest(
        String name,
        String groupId,
        String artifactId,
        String pkg,
        Path outputDir,
        boolean build
) {
}
