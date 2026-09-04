package org.tkit.onecx.onecxsvcgen.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.tkit.onecx.onecxsvcgen.model.ApiDef;

import java.nio.file.Files;
import java.nio.file.Path;

@ApplicationScoped
public class ParentMapperSyncService {

    @Inject
    NamingService naming;

    @Inject
    ModelParserService models;

    public void syncParentMappers(Path projectPath, String pkg, ApiDef apiDef) throws Exception {
        if (apiDef == null || apiDef.expose() || apiDef.parent() == null || apiDef.field() == null) {
            return;
        }

        Path base = projectPath.resolve("src/main/java/" + pkg.replace('.', '/'));
        Path parentEntity = base.resolve("domain/models/" + apiDef.parent() + ".java");
        boolean parentHasField = hasField(parentEntity, apiDef.field());

        updateInternalMapper(base.resolve("rs/internal/mappers/" + apiDef.parent() + "Mapper.java"), apiDef, parentHasField);
        updateExternalMapper(base.resolve("rs/external/v1/mappers/" + apiDef.parent() + "Mapper.java"), pkg, apiDef);
    }

    private void updateInternalMapper(Path file, ApiDef apiDef, boolean parentHasField) throws Exception {
        if (!Files.exists(file)) {
            return;
        }

        String parent = apiDef.parent();
        String field = apiDef.field();
        String content = Files.readString(file);

        content = addAnnotationBeforeMethod(
                content,
                parent + "DTO toDto(" + parent + " entity);",
                "@Mapping(target = \"" + field + "\", ignore = true)"
        );

        if (apiDef.parentFieldCollection()) {
            content = addAnnotationBeforeMethod(
                    content,
                    parent + "DTO toDto(" + parent + " entity);",
                    "@Mapping(target = \"" + removeItemMethod(field) + "\", ignore = true)"
            );
        }

        if (parentHasField) {
            content = addAnnotationBeforeMethod(
                    content,
                    parent + " fromDto(" + parent + "DTO dto);",
                    "@Mapping(target = \"" + field + "\", ignore = true)"
            );
            content = addAnnotationBeforeMethod(
                    content,
                    "void update(" + parent + "DTO dto, @MappingTarget " + parent + " entity);",
                    "@Mapping(target = \"" + field + "\", ignore = true)"
            );
        }

        Files.writeString(file, content);
    }

    private void updateExternalMapper(Path file, String pkg, ApiDef apiDef) throws Exception {
        if (!Files.exists(file)) {
            return;
        }

        String parent = apiDef.parent();
        String field = apiDef.field();
        String content = Files.readString(file);

        content = addAnnotationBeforeMethod(
                content,
                parent + "DTOV1 toDto(" + parent + " entity);",
                "@Mapping(target = \"" + field + "\", ignore = true)"
        );

        if (apiDef.parentFieldCollection()) {
            content = addAnnotationBeforeMethod(
                    content,
                    parent + "DTOV1 toDto(" + parent + " entity);",
                    "@Mapping(target = \"" + removeItemMethod(field) + "\", ignore = true)"
            );
        }

        String internalPageResultImport = "import " + models.generatedInternalModelPackage(pkg) + "." + parent + "PageResultDTO;";
        String externalPageResultImport = "import " + models.generatedModelPackage(pkg) + "." + parent + "PageResultDTOV1;";
        content = content.replace(internalPageResultImport, externalPageResultImport);
        content = content.replace(
                parent + "PageResultDTO mapPageResult(PageResult<" + parent + "> pageResult);",
                parent + "PageResultDTOV1 mapPageResult(PageResult<" + parent + "> pageResult);"
        );

        Files.writeString(file, content);
    }

    private String addAnnotationBeforeMethod(String content, String methodSignature, String annotation) {
        String signatureWithIndent = "    " + methodSignature;
        int index = content.indexOf(signatureWithIndent);
        if (index < 0) {
            return content;
        }

        int blockStart = content.lastIndexOf("\n\n", index);
        blockStart = blockStart < 0 ? 0 : blockStart + 2;
        String methodBlockPrefix = content.substring(blockStart, index);
        String annotationLine = "    " + annotation + "\n";
        if (methodBlockPrefix.contains(annotationLine)) {
            return content;
        }

        return content.substring(0, index) + "    " + annotation + "\n" + content.substring(index);
    }

    private String removeItemMethod(String field) {
        return "remove" + naming.upperFirst(field) + "Item";
    }

    private boolean hasField(Path entityFile, String field) {
        if (!Files.exists(entityFile)) {
            return false;
        }

        try {
            String content = Files.readString(entityFile);
            String marker = " " + field + ";";
            String initializedMarker = " " + field + " =";
            return content.contains(marker) || content.contains(initializedMarker);
        } catch (Exception e) {
            return false;
        }
    }
}



