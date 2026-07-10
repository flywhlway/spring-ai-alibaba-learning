package com.flywhl.saa.smartcs.prompt;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.alibaba.cloud.ai.prompt.ConfigurablePromptTemplate;
import com.alibaba.cloud.ai.prompt.ConfigurablePromptTemplateFactory;
import com.flywhl.saa.smartcs.repository.PromptTemplateRepository;

/**
 * Prompt 模板读取门面：Nacos 热更新 → DB PUBLISHED → classpath prompts/*.st 三级回退。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Component
public class PromptTemplateProvider {

    private static final Logger log = LoggerFactory.getLogger(PromptTemplateProvider.class);
    private static final String PUBLISHED_STATUS = "PUBLISHED";

    private final ObjectProvider<ConfigurablePromptTemplateFactory> promptTemplateFactory;
    private final PromptTemplateRepository promptTemplateRepository;

    public PromptTemplateProvider(
            ObjectProvider<ConfigurablePromptTemplateFactory> promptTemplateFactory,
            PromptTemplateRepository promptTemplateRepository) {
        this.promptTemplateFactory = promptTemplateFactory;
        this.promptTemplateRepository = promptTemplateRepository;
    }

    /**
     * 按模板键读取内容，依次尝试 Nacos、数据库已发布版本、classpath 默认文件。
     */
    public String get(String templateKey) {
        String fromNacos = loadFromNacos(templateKey);
        if (fromNacos != null) {
            return fromNacos;
        }
        String fromDatabase = loadFromDatabase(templateKey);
        if (fromDatabase != null) {
            return fromDatabase;
        }
        return loadFromClasspath(templateKey);
    }

    /**
     * 查询改写模板，供 RagPipelineFactory 注入 RewriteQueryTransformer。
     */
    public String getQueryRewriteTemplate() {
        return get("query-rewrite");
    }

    /**
     * FAQ 问答系统 Prompt（含 {context} 占位符），供 FaqAnswerService 渲染混合检索上下文。
     */
    public String getFaqAnswerSystemTemplate() {
        return get("faq-answer-system");
    }

    private String loadFromNacos(String templateKey) {
        ConfigurablePromptTemplateFactory factory = promptTemplateFactory.getIfAvailable();
        if (factory == null) {
            return null;
        }
        try {
            ConfigurablePromptTemplate template = factory.getTemplate(templateKey);
            if (template == null) {
                return null;
            }
            return template.render(Collections.emptyMap());
        } catch (Exception ex) {
            log.debug("Nacos Prompt 模板 {} 不可用，降级至 DB/classpath: {}", templateKey, ex.getMessage());
            return null;
        }
    }

    private String loadFromDatabase(String templateKey) {
        return promptTemplateRepository
                .findFirstByTemplateKeyAndStatusOrderByVersionDesc(templateKey, PUBLISHED_STATUS)
                .map(entity -> entity.getContent())
                .orElse(null);
    }

    private String loadFromClasspath(String templateKey) {
        String path = "prompts/" + templateKey + ".st";
        ClassPathResource resource = new ClassPathResource(path);
        if (!resource.exists()) {
            throw new IllegalStateException("Prompt 模板不存在: " + templateKey + "（Nacos/DB/classpath 均未命中）");
        }
        try {
            return resource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("读取 classpath Prompt 失败: " + path, ex);
        }
    }
}
