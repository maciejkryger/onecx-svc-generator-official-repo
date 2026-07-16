package org.tkit.onecx.onecxsvcgen.service;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class GitHubContextFactory {

    public Map<String, Object> build(String projectName, String pkg, String scopePrefix) {
        Map<String, Object> ctx = new HashMap<>();
        ctx.put("name", projectName);
        ctx.put("projectName", projectName);
        ctx.put("artifactId", projectName);
        ctx.put("package", pkg);
        ctx.put("packageName", pkg);
        ctx.put("basePackage", pkg);
        ctx.put("scopePrefix", scopePrefix);
        return ctx;
    }
}

