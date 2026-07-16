package org.tkit.onecx.onecxsvcgen.service;

import org.junit.jupiter.api.Test;
import org.tkit.onecx.onecxsvcgen.model.ApiDef;
import org.tkit.onecx.onecxsvcgen.model.EntityDef;
import org.tkit.onecx.onecxsvcgen.model.FieldDef;
import org.tkit.onecx.onecxsvcgen.model.RelationDef;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GenerationLogServiceTest {

    private final GenerationLogService logService = new GenerationLogService();

    @Test
    void logEntityInputIncludesNameRootAndFields() {
        EntityDef entityDef = new EntityDef(
                "Product",
                true,
                new ApiDef(true, null, null, false, null, null),
                List.of(new FieldDef("name", "String"), new FieldDef("price", "BigDecimal")),
                List.of()
        );

        String output = captureOutput(() -> logService.logEntityInput(entityDef));

        assertTrue(output.contains("Product"));
        assertTrue(output.contains("root: true"));
        assertTrue(output.contains("name:String"));
        assertTrue(output.contains("price:BigDecimal"));
        assertTrue(output.contains("relations: -"));
    }

    @Test
    void logEntityInputIncludesRelations() {
        EntityDef entityDef = new EntityDef(
                "Product",
                true,
                new ApiDef(true, null, null, false, null, null),
                List.of(),
                List.of(new RelationDef("category", "ManyToOne", "Category"))
        );

        String output = captureOutput(() -> logService.logEntityInput(entityDef));

        assertTrue(output.contains("category:ManyToOne:Category"));
        assertTrue(output.contains("fields: -"));
    }

    @Test
    void logEntityInputShowsDashWhenFieldsAndRelationsEmpty() {
        EntityDef entityDef = new EntityDef(
                "Tag",
                false,
                new ApiDef(false, "Product", "tags", true, null, null),
                List.of(),
                List.of()
        );

        String output = captureOutput(() -> logService.logEntityInput(entityDef));

        assertTrue(output.contains("fields: -"));
        assertTrue(output.contains("relations: -"));
        assertTrue(output.contains("root: false"));
    }

    @Test
    void logEntityResultForRootEntityIncludesCrudMessage() {
        EntityDef entityDef = new EntityDef(
                "Product",
                true,
                new ApiDef(true, null, null, false, null, null),
                List.of(),
                List.of()
        );

        String output = captureOutput(() -> logService.logEntityResult(entityDef));

        assertTrue(output.contains("Generated domain layer for: Product"));
        assertTrue(output.contains("CRUD"));
        assertTrue(output.contains("external-v1"));
    }

    @Test
    void logEntityResultForNonRootEntityIncludesParentApiMessage() {
        EntityDef entityDef = new EntityDef(
                "Tag",
                false,
                new ApiDef(false, "Product", "tags", true, null, null),
                List.of(),
                List.of()
        );

        String output = captureOutput(() -> logService.logEntityResult(entityDef));

        assertTrue(output.contains("Generated domain layer for: Tag"));
        assertTrue(output.contains("parent API Product"));
        assertTrue(output.contains("No standalone CRUD"));
    }

    private String captureOutput(Runnable action) {
        PrintStream original = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        try {
            action.run();
        } finally {
            System.setOut(original);
        }
        return baos.toString();
    }
}

