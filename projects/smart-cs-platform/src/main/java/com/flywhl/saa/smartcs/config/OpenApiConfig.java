package com.flywhl.saa.smartcs.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * OpenAPI/Knife4j 文档配置：分组（auth/chat/ticket/handoff/admin）、Bearer JWT 安全 Scheme。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Configuration(proxyBeanMethods = false)
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearer-jwt";

    @Bean
    OpenAPI smartCsOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("智能客服平台")
                        .description("认证 / 对话 / FAQ / 工单 / 人工接管 / 管理后台 REST API（统一 Result 响应）")
                        .version("1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("POST /api/auth/login 获取 access_token")));
    }

    @Bean
    GroupedOpenApi authApiGroup() {
        return GroupedOpenApi.builder()
                .group("auth")
                .displayName("认证")
                .pathsToMatch("/api/auth/**")
                .build();
    }

    @Bean
    GroupedOpenApi chatApiGroup() {
        return GroupedOpenApi.builder()
                .group("chat")
                .displayName("对话与 FAQ")
                .pathsToMatch("/api/chat/**", "/api/feedback/**")
                .build();
    }

    @Bean
    GroupedOpenApi ticketApiGroup() {
        return GroupedOpenApi.builder()
                .group("ticket")
                .displayName("工单与人工接管")
                .pathsToMatch("/api/tickets/**", "/api/handoff/**")
                .build();
    }

    @Bean
    GroupedOpenApi adminApiGroup() {
        return GroupedOpenApi.builder()
                .group("admin")
                .displayName("管理后台")
                .pathsToMatch("/api/admin/**")
                .build();
    }
}
