package com.flywhl.saa.smartcs.config;

import java.util.Locale;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.flywhl.saa.smartcs.model.entity.ModelProfile;
import com.flywhl.saa.smartcs.repository.ModelProfileRepository;
import com.flywhl.saa.starter.routing.ModelRouter;

/**
 * 按 scene（FAQ/BUSINESS/TICKET）从 {@code model_profile} 选 enabled 最高 priority 配置，
 * 映射到 DashScope / DeepSeek {@link ChatModel} Bean；无匹配时回退 starter
 * {@link ModelRouter}（主备降级）。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Component
public class ConfigurableModelRouter {

    private static final Logger log = LoggerFactory.getLogger(ConfigurableModelRouter.class);

    public static final String SCENE_FAQ = "FAQ";
    public static final String SCENE_BUSINESS = "BUSINESS";
    public static final String SCENE_TICKET = "TICKET";

    private final ModelProfileRepository modelProfileRepository;
    private final ModelRouter fallbackModelRouter;
    private final ApplicationContext applicationContext;

    public ConfigurableModelRouter(
            ModelProfileRepository modelProfileRepository,
            ModelRouter modelRouter,
            ApplicationContext applicationContext) {
        this.modelProfileRepository = modelProfileRepository;
        this.fallbackModelRouter = modelRouter;
        this.applicationContext = applicationContext;
    }

    /**
     * 无 scene 时走 starter 主备降级。
     */
    public ChatModel route() {
        return fallbackModelRouter.route();
    }

    /**
     * 按场景选模型；无匹配或 Bean 缺失时回退 {@link #route()}。
     */
    public ChatModel routeForScene(String scene) {
        if (scene == null || scene.isBlank()) {
            return route();
        }
        String normalized = scene.trim().toUpperCase(Locale.ROOT);
        Optional<ModelProfile> profile = modelProfileRepository
                .findBySceneAndEnabledTrueOrderByPriorityDesc(normalized)
                .stream()
                .findFirst();
        if (profile.isEmpty()) {
            log.debug("scene={} 无可用 model_profile，回退 FallbackModelRouter", normalized);
            return route();
        }
        return resolveChatModel(profile.get()).orElseGet(this::route);
    }

    private Optional<ChatModel> resolveChatModel(ModelProfile profile) {
        String beanName = mapProviderToBean(profile.getProvider());
        if (beanName == null) {
            log.warn("未知 provider={}（profileKey={}），回退 FallbackModelRouter",
                    profile.getProvider(), profile.getProfileKey());
            return Optional.empty();
        }
        try {
            return Optional.of(applicationContext.getBean(beanName, ChatModel.class));
        } catch (Exception ex) {
            log.warn("无法解析 ChatModel Bean={}（profileKey={}）: {}", beanName, profile.getProfileKey(),
                    ex.getMessage());
            return Optional.empty();
        }
    }

    /**
     * provider → Spring ChatModel Bean 名（与 application.yml / starter 约定对齐）。
     */
    static String mapProviderToBean(String provider) {
        if (provider == null) {
            return null;
        }
        return switch (provider.trim().toUpperCase(Locale.ROOT)) {
            case "DASHSCOPE", "QWEN" -> "dashScopeChatModel";
            case "DEEPSEEK" -> "deepSeekChatModel";
            default -> null;
        };
    }
}
