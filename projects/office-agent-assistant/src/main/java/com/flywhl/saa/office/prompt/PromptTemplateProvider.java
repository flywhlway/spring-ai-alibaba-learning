package com.flywhl.saa.office.prompt;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import com.flywhl.saa.office.repository.PromptTemplateRepository;

@Component
public class PromptTemplateProvider {
    private static final String PUBLISHED = "PUBLISHED";
    private final PromptTemplateRepository promptTemplateRepository;
    public PromptTemplateProvider(PromptTemplateRepository promptTemplateRepository) {
        this.promptTemplateRepository = promptTemplateRepository;
    }
    public String get(String templateKey) {
        return promptTemplateRepository
                .findFirstByTemplateKeyAndStatusOrderByVersionDesc(templateKey, PUBLISHED)
                .map(e -> e.getContent())
                .orElseGet(() -> loadFromClasspath(templateKey));
    }
    private String loadFromClasspath(String templateKey) {
        String path = "prompts/" + templateKey + ".st";
        ClassPathResource resource = new ClassPathResource(path);
        if (!resource.exists()) {
            throw new IllegalStateException("Prompt 模板不存在: " + templateKey);
        }
        try {
            return resource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("读取 Prompt 失败: " + path, ex);
        }
    }
}

