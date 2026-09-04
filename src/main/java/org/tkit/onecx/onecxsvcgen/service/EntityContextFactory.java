package org.tkit.onecx.onecxsvcgen.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.tkit.onecx.onecxsvcgen.model.ApiDef;
import org.tkit.onecx.onecxsvcgen.model.EntityDef;
import org.tkit.onecx.onecxsvcgen.model.FieldDef;
import org.tkit.onecx.onecxsvcgen.model.RelationDef;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class EntityContextFactory {

    @Inject
    NamingService naming;

    @Inject
    ModelParserService models;

    public Map<String, Object> buildContext(String projectName,
                                            String pkg,
                                            String scopePrefix,
                                            EntityDef entityDef) {
        String entity = entityDef.name();
        boolean root = entityDef.aggregateRoot();
        ApiDef api = entityDef.api();
        List<FieldDef> fields = entityDef.fields();
        List<RelationDef> relations = entityDef.relations();

        Map<String, Object> ctx = new HashMap<>();
        ctx.put("name", projectName);
        ctx.put("projectName", projectName);
        ctx.put("artifactId", projectName);

        String entityField = naming.lowerCamel(entity);
        String resourcePath = api.path() != null ? api.path() : naming.pluralPath(entity);
        String resourceOperationPlural = naming.upperFirst(resourcePath.replace("-", ""));

        String baseTag = api.tag() != null
                ? api.tag()
                : naming.lowerCamel(resourcePath.replace("-", ""));

        String internalTag = baseTag.endsWith("Internal")
                ? baseTag
                : baseTag + "Internal";

        String internalApiInterface = naming.apiInterfaceName(internalTag);
        String externalApiInterface = naming.upperFirst(baseTag) + "V1Api";

        ctx.put("package", pkg);
        ctx.put("packageName", pkg);
        ctx.put("basePackage", pkg);

        ctx.put("entity", entity);
        ctx.put("entityField", entityField);
        ctx.put("resourcePath", resourcePath);
        ctx.put("resourceOperationPlural", resourceOperationPlural);
        ctx.put("tableName", models.tableName(entity));
        ctx.put("entityImports", models.buildEntityImports(fields));
        ctx.put("scopePrefix", scopePrefix);

        ctx.put("resourceTag", internalTag);
        ctx.put("generatedApiPackage", models.generatedInternalApiPackage(pkg));
        ctx.put("generatedModelPackage", models.generatedInternalModelPackage(pkg));
        ctx.put("generatedApiInterface", internalApiInterface);
        ctx.put("generatedDto", entity + "DTO");
        ctx.put("generatedInternalSearchCriteria", entity + "SearchCriteriaDTO");
        ctx.put("generatedPageResultDto", entity + "PageResultDTO");

        ctx.put("generatedExternalApiPackage", models.generatedApiPackage(pkg));
        ctx.put("generatedExternalModelPackage", models.generatedModelPackage(pkg));
        ctx.put("generatedExternalApiInterface", externalApiInterface);
        ctx.put("generatedExternalDto", entity + "DTOV1");
        ctx.put("generatedExternalSearchCriteria", entity + "SearchCriteriaDTOV1");
        ctx.put("generatedExternalPageResultDto", entity + "PageResultDTOV1");
        ctx.put("externalOperationSuffix", "V1");

        ctx.put("externalMapperMappingImport", root ? "import org.mapstruct.Mapping;\n" : "");
        ctx.put("externalMapperPageResultImports",
                root
                        ? "import " + models.generatedModelPackage(pkg) + "." + entity + "PageResultDTOV1;\n"
                        + "import org.tkit.quarkus.jpa.daos.PageResult;\n"
                        : ""
        );
        ctx.put("mapPageResultMethod",
                root
                        ? "\n    @Mapping(target = \"removeStreamItem\", ignore = true)\n    "
                        + entity + "PageResultDTOV1 mapPageResult(PageResult<" + entity + "> pageResult);"
                        : ""
        );

        ctx.put("modelPackage", models.modelPackage(pkg));
        ctx.put("daoPackage", models.daoPackage(pkg));
        ctx.put("domainServicePackage", models.domainServicePackage(pkg));

        ctx.put("controllerPackage", models.controllerPackage(pkg));
        ctx.put("mapperPackage", models.mapperPackage(pkg));

        ctx.put("externalControllerPackage", models.externalControllerPackage(pkg));
        ctx.put("externalMapperPackage", models.externalMapperPackage(pkg));

        ctx.put("jpaAttributeOverrides", models.buildJpaAttributeOverrides());
        ctx.put("fieldsDecl", models.buildFieldsDecl(fields));
        ctx.put("relationsDecl", models.buildRelationsDecl(relations, pkg));
        ctx.put("liquibaseColumns", models.buildLiquibaseColumns(fields, relations));
        ctx.put("findByCriteriaPredicates", models.buildFindByCriteriaPredicates(entity, fields));
        ctx.put("relationMappingMethods", models.buildRelationMappingMethods(relations, pkg));

        ctx.put("serviceRelationImports", models.buildServiceRelationImports(relations, pkg));
        ctx.put("relationDaoInjections", models.buildRelationDaoInjections(relations));
        ctx.put("relationCreateResolvers", models.buildRelationCreateResolvers(relations));
        ctx.put("relationUpdateResolvers", models.buildRelationUpdateResolvers(relations));

        ctx.put("testCreateDtoBody", models.buildTestCreateDtoBody(fields, relations, entity + "DTO"));
        ctx.put("testUpdateDtoBody", models.buildTestUpdateDtoBody(fields, relations, entity + "DTO"));
        ctx.put("testSearchCriteriaBody", models.buildTestSearchCriteriaBody(fields, entity + "SearchCriteriaDTO"));
        ctx.put("testSearchSeedBody", models.buildTestSearchSeedBody(fields));
        ctx.put("testExternalSearchCriteriaBody", models.buildTestExternalSearchCriteriaBody(fields, entity + "SearchCriteriaDTOV1"));

        ctx.put("testInternalControllerAdditionalMethods", models.buildInternalControllerAdditionalMethods(entity, resourcePath, fields, relations));
        ctx.put("testInternalControllerHelperMethods", models.buildInternalControllerHelperMethods(entity, resourcePath, fields, relations));
        ctx.put("testExternalControllerAdditionalMethods", models.buildExternalControllerAdditionalMethods(entity, resourcePath, fields, relations));
        ctx.put("testExternalControllerHelperMethods", models.buildExternalControllerHelperMethods(entity, resourcePath, fields, relations));

        ctx.put("testEntityFieldsInit", models.buildTestEntityFieldsInit(fields, relations));
        ctx.put("testDtoFieldsInit", models.buildTestDtoFieldsInit(fields, relations, entity + "DTO"));
        ctx.put("testDtoUpdateFieldsInit", models.buildTestDtoUpdateFieldsInit(fields, relations, entity + "DTO"));

        ctx.put("testDtoAssertions", models.buildTestDtoAssertions(fields, relations));
        ctx.put("testExternalDtoAssertions", models.buildTestExternalDtoAssertions(fields, relations));
        ctx.put("testEntityAssertions", models.buildTestEntityAssertions(fields, relations));
        ctx.put("testUpdatedEntityAssertions", models.buildTestUpdatedEntityAssertions(fields, relations));

        return ctx;
    }
}

