package com.flywhl.saa.agentskills;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.modelcalllimit.ModelCallLimitHook;
import com.alibaba.cloud.ai.graph.agent.hook.skills.SkillsAgentHook;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.skills.registry.SkillRegistry;
import com.alibaba.cloud.ai.graph.skills.registry.classpath.ClasspathSkillRegistry;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Skills Agent：ClasspathSkillRegistry 扫描 resources/skills 下各技能目录的 SKILL.md，
 * 经 SkillsAgentHook 注入 read_skill 与 SkillsInterceptor（替代教程伪 API Skill.of）。
 *
 * @author flywhl
 */
@Configuration(proxyBeanMethods = false)
public class SkillsAgentConfig {

    @Bean(destroyMethod = "close")
    public SkillRegistry skillRegistry() {
        return ClasspathSkillRegistry.builder()
                .classpathPath("skills")
                .autoLoad(true)
                .build();
    }

    @Bean
    public SkillsAgentHook skillsAgentHook(SkillRegistry skillRegistry) {
        return SkillsAgentHook.builder()
                .skillRegistry(skillRegistry)
                .build();
    }

    @Bean
    public ReactAgent multiSkillAgent(ChatModel dashScopeChatModel, SkillsAgentHook skillsAgentHook) {
        return ReactAgent.builder()
                .name("multi-skill-agent")
                .model(dashScopeChatModel)
                .systemPrompt("你是企业智能助手。优先通过 read_skill 加载匹配技能的完整说明，再按技能指引回答。")
                .hooks(
                        skillsAgentHook,
                        ModelCallLimitHook.builder().runLimit(6).build())
                .saver(new MemorySaver())
                .build();
    }
}
