package org.tkit.onecx.onecxsvcgen.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.tkit.onecx.onecxsvcgen.model.ApiDef;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParentMapperSyncServiceTest {

    @Test
    void syncParentMappersAddsMissingIgnoreMappingsAndFixesExternalPageResultDto(@TempDir Path projectDir) throws Exception {
        ParentMapperSyncService service = new ParentMapperSyncService();
        service.naming = new NamingService();
        service.models = new ModelParserService();

        Path base = projectDir.resolve("src/main/java/org/tkit/onecx/demo");
        Path internalMapper = base.resolve("rs/internal/mappers/ProductMapper.java");
        Path externalMapper = base.resolve("rs/external/v1/mappers/ProductMapper.java");
        Path parentEntity = base.resolve("domain/models/Product.java");
        Files.createDirectories(internalMapper.getParent());
        Files.createDirectories(externalMapper.getParent());
        Files.createDirectories(parentEntity.getParent());

        Files.writeString(parentEntity, """
                package org.tkit.onecx.demo.domain.models;

                public class Product {
                    private String id;
                }
                """);

        Files.writeString(internalMapper, """
                package org.tkit.onecx.demo.rs.internal.mappers;

                import org.mapstruct.Mapper;
                import org.mapstruct.Mapping;
                import org.mapstruct.MappingTarget;
                import org.tkit.onecx.demo.domain.models.Product;
                import org.tkit.quarkus.jpa.daos.PageResult;
                import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

                import gen.org.tkit.onecx.demo.rs.internal.model.ProductDTO;
                import gen.org.tkit.onecx.demo.rs.internal.model.ProductPageResultDTO;

                @Mapper(uses = { OffsetDateTimeMapper.class })
                public interface ProductMapper {

                    ProductDTO toDto(Product entity);

                    @Mapping(target = "removeStreamItem", ignore = true)
                    ProductPageResultDTO toPageResultDto(PageResult<Product> page);

                    @Mapping(target = "id", ignore = true)
                    @Mapping(target = "tenantId", ignore = true)
                    @Mapping(target = "creationDate", ignore = true)
                    @Mapping(target = "creationUser", ignore = true)
                    @Mapping(target = "modificationDate", ignore = true)
                    @Mapping(target = "modificationUser", ignore = true)
                    @Mapping(target = "controlTraceabilityManual", ignore = true)
                    @Mapping(target = "modificationCount", ignore = true)
                    @Mapping(target = "persisted", ignore = true)
                    Product fromDto(ProductDTO dto);

                    @Mapping(target = "id", ignore = true)
                    @Mapping(target = "tenantId", ignore = true)
                    @Mapping(target = "creationDate", ignore = true)
                    @Mapping(target = "creationUser", ignore = true)
                    @Mapping(target = "modificationDate", ignore = true)
                    @Mapping(target = "modificationUser", ignore = true)
                    @Mapping(target = "controlTraceabilityManual", ignore = true)
                    @Mapping(target = "modificationCount", ignore = true)
                    @Mapping(target = "persisted", ignore = true)
                    void update(ProductDTO dto, @MappingTarget Product entity);
                }
                """);

        Files.writeString(externalMapper, """
                package org.tkit.onecx.demo.rs.external.v1.mappers;

                import org.mapstruct.Mapper;
                import org.mapstruct.Mapping;
                import org.tkit.onecx.demo.domain.models.Product;
                import org.tkit.quarkus.jpa.daos.PageResult;
                import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

                import gen.org.tkit.onecx.demo.rs.external.v1.model.ProductDTOV1;
                import gen.org.tkit.onecx.demo.rs.internal.model.ProductPageResultDTO;
                import gen.org.tkit.onecx.demo.rs.external.v1.model.ProductSearchCriteriaDTOV1;
                import gen.org.tkit.onecx.demo.rs.internal.model.ProductSearchCriteriaDTO;

                @Mapper(uses = { OffsetDateTimeMapper.class })
                public interface ProductMapper {
                    ProductDTOV1 toDto(Product entity);

                    ProductSearchCriteriaDTO toCriteria(ProductSearchCriteriaDTOV1 criteria);

                    @Mapping(target = "removeStreamItem", ignore = true)
                    ProductPageResultDTO mapPageResult(PageResult<Product> pageResult);
                }
                """);

        ApiDef apiDef = new ApiDef(false, "Product", "items", true, null, null);

        service.syncParentMappers(projectDir, "org.tkit.onecx.demo", apiDef);
        service.syncParentMappers(projectDir, "org.tkit.onecx.demo", apiDef);

        String internal = Files.readString(internalMapper);
        String external = Files.readString(externalMapper);

        assertTrue(internal.contains("@Mapping(target = \"items\", ignore = true)\n    @Mapping(target = \"removeItemsItem\", ignore = true)\n    ProductDTO toDto(Product entity);"));
        assertFalse(internal.contains("@Mapping(target = \"items\", ignore = true)\n    Product fromDto(ProductDTO dto);"));
        assertFalse(internal.contains("@Mapping(target = \"items\", ignore = true)\n    void update(ProductDTO dto, @MappingTarget Product entity);"));

        assertTrue(external.contains("@Mapping(target = \"items\", ignore = true)\n    @Mapping(target = \"removeItemsItem\", ignore = true)\n    ProductDTOV1 toDto(Product entity);"));
        assertTrue(external.contains("import gen.org.tkit.onecx.demo.rs.external.v1.model.ProductPageResultDTOV1;"));
        assertTrue(external.contains("ProductPageResultDTOV1 mapPageResult(PageResult<Product> pageResult);"));
        assertEquals(1, countOccurrences(internal, "@Mapping(target = \"removeItemsItem\", ignore = true)"));
        assertEquals(1, countOccurrences(external, "@Mapping(target = \"removeItemsItem\", ignore = true)"));
        assertEquals(1, countOccurrences(internal, "@Mapping(target = \"items\", ignore = true)"));
        assertEquals(1, countOccurrences(external, "@Mapping(target = \"items\", ignore = true)"));
    }

    private int countOccurrences(String content, String fragment) {
        int count = 0;
        int index = 0;
        while ((index = content.indexOf(fragment, index)) >= 0) {
            count++;
            index += fragment.length();
        }
        return count;
    }
}



