package org.hswebframework.payment.openapi.docs;

import groovy.text.Template;
import groovy.text.TemplateEngine;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.Assert;

import java.io.File;
import java.io.FileWriter;
import java.util.*;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public class DefaultOpenApiDocsGenerator implements OpenApiDocsGenerator {

    private static String apiTemplateFile = "/docs/template/api-template.tpl";

    private static String apiMenuTemplateFile = "/docs/template/api-menu.tpl";

    private Template apiTTemplate;

    private Template apiMenuTemplate;

    public DefaultOpenApiDocsGenerator(TemplateEngine engine) throws Exception {
        apiTTemplate = engine.createTemplate(new ClassPathResource(apiTemplateFile).getURL());
        apiMenuTemplate = engine.createTemplate(new ClassPathResource(apiMenuTemplateFile).getURL());
    }

    @Getter
    @Setter
    public static class ApiGroup {
        private String name;

        private List<ApiInfo> apis = new LinkedList<>();

        public ApiGroup(String name) {
            this.name = name;
        }
    }

    @Override
    @SneakyThrows
    public void generate(List<ApiInfo> apiInfoList, String outputDir) {
        File outDir = new File(outputDir);
        if (!outDir.exists()) {
            Assert.isTrue(outDir.mkdirs(), "创建文档目录失败");
        }

        Map<String, ApiGroup> group = new HashMap<>();

        for (ApiInfo apiInfo : apiInfoList) {
            group.computeIfAbsent(apiInfo.getGroup(), ApiGroup::new)
                    .apis.add(apiInfo);

            String fileName = apiInfo.getId().concat(".md");
            File output = new File(outDir, fileName);
            Map<String, Object> binding = new HashMap<>();
            binding.put("api", apiInfo);
            try (FileWriter fileWriter = new FileWriter(output)) {
                apiTTemplate
                        .make(binding)
                        .writeTo(fileWriter);
            }
        }

        try (FileWriter fileWriter = new FileWriter(new File(outDir, "api-menu.md"))) {
            apiMenuTemplate.make(Collections.singletonMap("apiGroups", group.values()))
                    .writeTo(fileWriter);
        }
    }
}
