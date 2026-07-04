package com.flywhl.saa.promptbuilder;

import com.flywhl.saa.common.exception.BizException;
import com.flywhl.saa.common.result.CommonResultCode;
import com.flywhl.saa.promptbuilder.model.PromptVersionVO;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * 内存版 Prompt 组装器与版本注册表：按 {@code name -> version -> 模板原文} 两级索引管理。
 *
 * <p>生产环境应把本类替换为 Nacos/数据库支撑的实现（见 {@code examples/08-prompt-nacos-demo}），
 * 本 Demo 只演示"按 key+version 注册/查询/渲染"这一组装器职责本身。
 *
 * @author flywhl
 */
@Component
public class PromptRegistry {

    private final Map<String, ConcurrentSkipListMap<String, String>> store = new ConcurrentHashMap<>();

    public void register(String name, String version, String template) {
        store.computeIfAbsent(name, k -> new ConcurrentSkipListMap<>()).put(version, template);
    }

    public List<String> listVersions(String name) {
        ConcurrentSkipListMap<String, String> versions = store.get(name);
        if (versions == null) {
            throw new BizException(CommonResultCode.NOT_FOUND, "未注册的 Prompt：" + name);
        }
        return List.copyOf(versions.keySet());
    }

    public PromptVersionVO get(String name, String version) {
        String template = templateOf(name, version);
        return new PromptVersionVO(name, version, template);
    }

    public String render(String name, String version, Map<String, Object> params) {
        String template = templateOf(name, version);
        return PromptTemplate.builder().template(template).build().render(params == null ? Map.of() : params);
    }

    private String templateOf(String name, String version) {
        ConcurrentSkipListMap<String, String> versions = store.get(name);
        if (versions == null || !versions.containsKey(version)) {
            throw new BizException(CommonResultCode.NOT_FOUND, "未注册的 Prompt 版本：" + name + "@" + version);
        }
        return versions.get(version);
    }
}
