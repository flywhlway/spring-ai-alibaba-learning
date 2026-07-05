package com.flywhl.saa.office.service;

import java.util.Map;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.flywhl.saa.office.model.dto.TaskGenerateRequest;
import com.flywhl.saa.office.model.entity.SysUser;
import com.flywhl.saa.office.model.vo.TaskResultVO;
import com.flywhl.saa.office.prompt.PromptTemplateProvider;
import com.flywhl.saa.office.repository.UserPreferenceRepository;

@Service
public class TaskService {
    private final ChatClient chatClient;
    private final PromptTemplateProvider promptTemplateProvider;
    private final UserPreferenceRepository userPreferenceRepository;
    private final ArtifactService artifactService;
    private final AuthService authService;
    @Value("${spring.ai.dashscope.chat.options.model:qwen-plus}")
    private String modelName;
    public TaskService(ChatClient.Builder chatClientBuilder, PromptTemplateProvider promptTemplateProvider,
            UserPreferenceRepository userPreferenceRepository, ArtifactService artifactService, AuthService authService) {
        this.chatClient = chatClientBuilder.build();
        this.promptTemplateProvider = promptTemplateProvider;
        this.userPreferenceRepository = userPreferenceRepository;
        this.artifactService = artifactService;
        this.authService = authService;
    }
    public TaskResultVO meetingSummary(TaskGenerateRequest request) {
        String template = promptTemplateProvider.get("meeting-summary");
        String prompt = template.replace("{transcript}", request.input());
        return generate("MEETING_SUMMARY", request.title() != null ? request.title() : "会议纪要", prompt);
    }
    public TaskResultVO dailyReport(TaskGenerateRequest request) {
        SysUser user = authService.requireCurrentUser();
        String format = userPreferenceRepository.findByUserIdAndPrefKey(user.getId(), "report-format")
                .map(p -> p.getPrefValue()).orElse("三段式：今日完成/明日计划/风险");
        String template = promptTemplateProvider.get("daily-report");
        String prompt = template.replace("{format}", format).replace("{material}", request.input());
        return generate("DAILY_REPORT", request.title() != null ? request.title() : "工作日报", prompt);
    }
    public TaskResultVO emailDraft(TaskGenerateRequest request) {
        SysUser user = authService.requireCurrentUser();
        String tone = userPreferenceRepository.findByUserIdAndPrefKey(user.getId(), "email-tone")
                .map(p -> p.getPrefValue()).orElse("正式、简洁");
        String template = promptTemplateProvider.get("email-draft");
        String prompt = template.replace("{tone}", tone).replace("{request}", request.input());
        return generate("EMAIL_DRAFT", request.title() != null ? request.title() : "邮件草稿", prompt);
    }
    private TaskResultVO generate(String type, String title, String prompt) {
        SysUser user = authService.requireCurrentUser();
        String content = chatClient.prompt().user(prompt).call().content();
        var artifact = artifactService.save(user, type, title, content, null, modelName);
        return new TaskResultVO(artifact.getId(), title, content, null);
    }
}

