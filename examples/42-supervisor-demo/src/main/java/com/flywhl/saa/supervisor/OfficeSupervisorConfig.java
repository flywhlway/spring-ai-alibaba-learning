package com.flywhl.saa.supervisor;

import com.alibaba.cloud.ai.graph.agent.AgentTool;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.modelcalllimit.ModelCallLimitHook;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Supervisor 模式：ReactAgent 总控 + {@link AgentTool#create} 封装子 Agent（无专用 Supervisor 类）。
 *
 * @author flywhl
 */
@Configuration(proxyBeanMethods = false)
public class OfficeSupervisorConfig {

    @Bean
    ReactAgent calendarAgent(ChatModel dashScopeChatModel, CalendarTools calendarTools) {
        return ReactAgent.builder()
                .name("calendar-agent")
                .description("处理日程查询、安排会议等日程相关任务")
                .model(dashScopeChatModel)
                .systemPrompt("你专门负责日程查询与安排，优先调用工具，回复简洁。")
                .methodTools(calendarTools)
                .hooks(ModelCallLimitHook.builder().runLimit(4).build())
                .build();
    }

    @Bean
    ReactAgent emailAgent(ChatModel dashScopeChatModel, EmailTools emailTools) {
        return ReactAgent.builder()
                .name("email-agent")
                .description("处理邮件起草、发送等邮件相关任务")
                .model(dashScopeChatModel)
                .systemPrompt("你专门负责邮件起草与发送，优先调用工具，回复简洁。")
                .methodTools(emailTools)
                .hooks(ModelCallLimitHook.builder().runLimit(4).build())
                .build();
    }

    @Bean
    ReactAgent officeSupervisor(ChatModel dashScopeChatModel,
                                ReactAgent calendarAgent,
                                ReactAgent emailAgent) {
        return ReactAgent.builder()
                .name("office-supervisor")
                .model(dashScopeChatModel)
                .tools(AgentTool.create(calendarAgent), AgentTool.create(emailAgent))
                .systemPrompt("""
                        你是企业办公助手总控。根据用户需求调度专职助手：
                        - calendar-agent：日程查询与会议安排
                        - email-agent：邮件起草与发送
                        不要自己编造工具结果，通过调用对应助手完成任务后汇总回复。
                        """)
                .hooks(ModelCallLimitHook.builder().runLimit(6).build())
                .build();
    }
}
