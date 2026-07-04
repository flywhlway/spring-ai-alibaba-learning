package com.flywhl.saa.agentskills;

import com.flywhl.saa.common.exception.GlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * Agent Skills 渐进式披露演示：ClasspathSkillRegistry + SkillsAgentHook。
 *
 * @author flywhl
 */
@SpringBootApplication
@Import(GlobalExceptionHandler.class)
public class AgentSkillsDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgentSkillsDemoApplication.class, args);
    }
}
