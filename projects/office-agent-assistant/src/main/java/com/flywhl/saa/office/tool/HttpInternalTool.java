package com.flywhl.saa.office.tool;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.flywhl.saa.office.config.OfficeProperties;

@Component
public class HttpInternalTool {
    private final RestTemplate restTemplate;
    private final List<String> allowedHosts;
    public HttpInternalTool(RestTemplateBuilder builder, OfficeProperties properties) {
        Duration timeout = properties.tool().http().timeout();
        this.restTemplate = builder.connectTimeout(timeout).readTimeout(timeout).build();
        this.allowedHosts = properties.tool().http().allowedHosts();
    }
    @Tool(description = "调用内部 HTTP GET 接口（host 白名单校验）")
    public String httpGet(@ToolParam(description = "完整 URL") String url, ToolContext toolContext) {
        if (ToolSecuritySupport.requireRole(toolContext, "EMPLOYEE", "ADMIN") == null) {
            return "权限不足";
        }
        try {
            URI uri = URI.create(url.trim());
            String host = uri.getHost();
            if (host == null || allowedHosts.stream().noneMatch(h -> h.equalsIgnoreCase(host))) {
                return "Host 不在白名单：" + allowedHosts;
            }
            if (!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme())) {
                return "仅支持 http/https";
            }
            ResponseEntity<String> resp = restTemplate.getForEntity(uri, String.class);
            String body = resp.getBody();
            return body == null ? "" : body.substring(0, Math.min(body.length(), 2000));
        } catch (Exception ex) {
            return "HTTP 调用失败：" + ex.getMessage();
        }
    }
}

