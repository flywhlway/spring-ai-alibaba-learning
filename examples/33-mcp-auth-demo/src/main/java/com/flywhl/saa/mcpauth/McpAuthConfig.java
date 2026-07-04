package com.flywhl.saa.mcpauth;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.common.McpTransportContext;
import io.modelcontextprotocol.json.jackson.JacksonMcpJsonMapper;
import io.modelcontextprotocol.server.McpTransportContextExtractor;
import io.modelcontextprotocol.server.transport.WebMvcStreamableServerTransportProvider;
import org.springframework.ai.mcp.server.common.autoconfigure.properties.McpServerStreamableHttpProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.ServerRequest;

import java.util.Map;

/**
 * 自定义 Streamable HTTP 传输，从 Authorization 头提取 Bearer Token。
 *
 * <p>覆盖自动装配的 {@link WebMvcStreamableServerTransportProvider}
 * （{@code @ConditionalOnMissingBean}），注入 {@link McpTransportContextExtractor}。
 *
 * @author flywhl
 */
@Configuration
public class McpAuthConfig {

    public static final String AUTHORIZATION_KEY = "authorization";

    @Bean
    public McpTransportContextExtractor<ServerRequest> mcpTransportContextExtractor() {
        return serverRequest -> {
            String authorization = serverRequest.headers().firstHeader("Authorization");
            if (authorization == null || authorization.isBlank()) {
                return McpTransportContext.EMPTY;
            }
            return McpTransportContext.create(Map.of(AUTHORIZATION_KEY, authorization));
        };
    }

    @Bean
    public WebMvcStreamableServerTransportProvider webMvcStreamableServerTransportProvider(
            ObjectMapper objectMapper,
            McpServerStreamableHttpProperties properties,
            McpTransportContextExtractor<ServerRequest> contextExtractor) {
        WebMvcStreamableServerTransportProvider.Builder builder =
                WebMvcStreamableServerTransportProvider.builder()
                        .jsonMapper(new JacksonMcpJsonMapper(objectMapper))
                        .mcpEndpoint(properties.getMcpEndpoint())
                        .disallowDelete(properties.isDisallowDelete())
                        .contextExtractor(contextExtractor);
        if (properties.getKeepAliveInterval() != null) {
            builder.keepAliveInterval(properties.getKeepAliveInterval());
        }
        return builder.build();
    }
}
