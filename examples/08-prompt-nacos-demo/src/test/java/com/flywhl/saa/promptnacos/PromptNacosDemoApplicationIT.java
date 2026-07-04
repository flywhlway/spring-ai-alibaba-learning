package com.flywhl.saa.promptnacos;

import com.alibaba.cloud.ai.prompt.ConfigurablePromptTemplateFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 冒烟 IT：关闭 Nacos 订阅，仅验证 Factory 装配与默认模板兜底路径可编译运行。
 *
 * @author flywhl
 */
@SpringBootTest(properties = "spring.ai.nacos.prompt.template.enabled=false")
@EnabledIfEnvironmentVariable(named = "AI_DASHSCOPE_API_KEY", matches = ".+")
class PromptNacosDemoApplicationIT {

    @Autowired
    private ConfigurablePromptTemplateFactory promptTemplateFactory;

    @Test
    void factoryAvailableAndDefaultCreateWorks() {
        assertThat(promptTemplateFactory).isNotNull();
        assertThat(promptTemplateFactory.getTemplate("missing")).isNull();
        assertThat(promptTemplateFactory.create("demo", "hello {name}", java.util.Map.of("name", "world")))
                .isNotNull();
    }
}
