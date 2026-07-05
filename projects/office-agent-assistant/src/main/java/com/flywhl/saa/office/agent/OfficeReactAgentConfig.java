package com.flywhl.saa.office.agent;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.modelcalllimit.ModelCallLimitHook;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.flywhl.saa.office.tool.CalendarTool;
import com.flywhl.saa.office.tool.ExcelTool;
import com.flywhl.saa.office.tool.HttpInternalTool;
import com.flywhl.saa.office.tool.SqlQueryTool;

@Configuration(proxyBeanMethods = false)
public class OfficeReactAgentConfig {
    @Bean
    ReactAgent officeAssistantAgent(ChatModel dashScopeChatModel,
            SqlQueryTool sqlQueryTool, HttpInternalTool httpInternalTool,
            ExcelTool excelTool, CalendarTool calendarTool) {
        return ReactAgent.builder()
                .name("office-assistant-agent")
                .model(dashScopeChatModel)
                .systemPrompt("""
                        你是企业办公 AI 助手，可帮员工查询报表、调用内部系统、生成 Excel、查看日程。
                        涉及数据变更时说明权限限制；回答简洁专业。
                        """)
                .methodTools(sqlQueryTool, httpInternalTool, excelTool, calendarTool)
                .hooks(ModelCallLimitHook.builder().runLimit(8).build())
                .saver(new MemorySaver())
                .build();
    }
}

