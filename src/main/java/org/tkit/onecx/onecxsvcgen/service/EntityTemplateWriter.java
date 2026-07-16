package org.tkit.onecx.onecxsvcgen.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@ApplicationScoped
public class EntityTemplateWriter {

    @Inject
    TemplateService templates;

    public void writeEntityFiles(Path base, Path testBase, String entity, boolean root, Map<String, Object> ctx) throws Exception {
        templates.renderToFile(
                "templates/entity/Entity.java.tpl",
                base.resolve("domain/models/" + entity + ".java"),
                ctx
        );

        templates.renderToFile(
                root
                        ? "templates/entity/DAO.java.tpl"
                        : "templates/entity/NonRootDAO.java.tpl",
                base.resolve("domain/daos/" + entity + "DAO.java"),
                ctx
        );

        if (root) {
            templates.renderToFile(
                    "templates/entity/Service.java.tpl",
                    base.resolve("domain/services/" + entity + "Service.java"),
                    ctx
            );
        }

        templates.renderToFile(
                "templates/entity/Mapper.java.tpl",
                base.resolve("rs/internal/mappers/" + entity + "Mapper.java"),
                ctx
        );

        renderIfMissing(
                "templates/entity/InternalExceptionMapper.java.tpl",
                base.resolve("rs/internal/mappers/InternalExceptionMapper.java"),
                ctx
        );

        templates.renderToFile(
                "templates/entity/ExternalMapper.java.tpl",
                base.resolve("rs/external/v1/mappers/" + entity + "Mapper.java"),
                ctx
        );

        renderIfMissing(
                "templates/entity/ExternalExceptionMapper.java.tpl",
                base.resolve("rs/external/v1/mappers/ExternalExceptionMapper.java"),
                ctx
        );

        if (root) {
            templates.renderToFile(
                    "templates/entity/Controller.java.tpl",
                    base.resolve("rs/internal/controllers/" + entity + "Controller.java"),
                    ctx
            );

            templates.renderToFile(
                    "templates/entity/ExternalController.java.tpl",
                    base.resolve("rs/external/v1/controllers/" + entity + "Controller.java"),
                    ctx
            );

            renderIfMissing(
                    "templates/test/AbstractTest.java.tpl",
                    testBase.resolve("AbstractTest.java"),
                    ctx
            );

            renderIfMissing(
                    "templates/test/ControllerTest.java.tpl",
                    testBase.resolve("rs/internal/controllers/" + entity + "ControllerTest.java"),
                    ctx
            );

            renderIfMissing(
                    "templates/test/ExternalControllerTest.java.tpl",
                    testBase.resolve("rs/external/v1/controllers/" + entity + "ControllerTest.java"),
                    ctx
            );

            renderIfMissing(
                    "templates/test/ControllerIT.java.tpl",
                    testBase.resolve("rs/internal/controllers/" + entity + "ControllerIT.java"),
                    ctx
            );

            renderIfMissing(
                    "templates/test/ExternalControllerIT.java.tpl",
                    testBase.resolve("rs/external/v1/controllers/" + entity + "ControllerIT.java"),
                    ctx
            );
        }
    }

    private void renderIfMissing(String template, Path target, Map<String, Object> ctx) throws Exception {
        if (!Files.exists(target)) {
            templates.renderToFile(template, target, ctx);
        }
    }
}

