package com.flywhl.saa.supervisor;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * 邮件相关 @Tool，供 email-agent 调用。
 *
 * @author flywhl
 */
@Component
public class EmailTools {

    @Tool(description = "根据要点起草一封简洁商务邮件，返回草稿正文")
    public String draftEmail(@ToolParam(description = "收件人称呼") String recipient,
                             @ToolParam(description = "邮件主题") String subject,
                             @ToolParam(description = "要点列表，逗号分隔") String bulletPoints) {
        return """
                收件人：%s
                主题：%s
                ---
                您好，
                
                %s
                
                此致
                敬礼
                """.formatted(recipient, subject, bulletPoints.replace(",", "\n- "));
    }

    @Tool(description = "模拟发送邮件并返回发送结果")
    public String sendEmail(@ToolParam(description = "收件人邮箱") String to,
                            @ToolParam(description = "邮件主题") String subject) {
        return "已向 " + to + " 发送邮件「" + subject + "」（模拟发送成功）";
    }
}
