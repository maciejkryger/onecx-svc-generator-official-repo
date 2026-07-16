package org.tkit.onecx.onecxsvcgen.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.tkit.onecx.onecxsvcgen.model.EntityDef;
import org.tkit.onecx.onecxsvcgen.model.FieldDef;
import org.tkit.onecx.onecxsvcgen.model.RelationDef;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class GenerationLogService {

    public void logEntityInput(EntityDef entityDef) {
        System.out.println("▶ Processing entity: " + entityDef.name());
        System.out.println("  root: " + entityDef.aggregateRoot());
        System.out.println("  fields: " + formatFields(entityDef.fields()));
        System.out.println("  relations: " + formatRelations(entityDef.relations()));
    }

    public void logEntityResult(EntityDef entityDef) {
        System.out.println("✔ Generated domain layer for: " + entityDef.name());
        if (entityDef.aggregateRoot()) {
            System.out.println("✔ Updated internal API/runtime (CRUD + search) and external-v1 API/runtime (get + search) for: " + entityDef.name());
            System.out.println("✔ Generated controller, mapper and service tests for root entity: " + entityDef.name());
        } else {
            System.out.println("✔ Added component schema " + entityDef.name() + " to parent API " + entityDef.api().parent()
                    + " in internal and external-v1 contracts. No standalone CRUD paths created.");
            System.out.println("✔ Generated mapper and service tests for non-root entity: " + entityDef.name());
        }
    }

    private String formatFields(List<FieldDef> fields) {
        if (fields == null || fields.isEmpty()) {
            return "-";
        }
        return fields.stream()
                .map(field -> field.name() + ":" + field.type())
                .collect(Collectors.joining(", "));
    }

    private String formatRelations(List<RelationDef> relations) {
        if (relations == null || relations.isEmpty()) {
            return "-";
        }
        return relations.stream()
                .map(rel -> rel.field() + ":" + rel.relationType() + ":" + rel.target())
                .collect(Collectors.joining(", "));
    }
}
