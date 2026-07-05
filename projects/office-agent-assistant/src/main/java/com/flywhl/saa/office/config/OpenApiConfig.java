package com.flywhl.saa.office.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * OpenAPI / Knife4j 文档配置。
 *
 * @author flywhl
 */
@Configuration(proxyBeanMethods = false)
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearer-jwt";

    @Bean
    OpenAPI officeOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("企业 AI Agent 办公助手")
                        .description("认证 / 对话 / 任务 / 审批 / 管理后台 REST API")
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
        return GroupedOpenApi.builder().group("auth").displayName("认证").pathsToMatch("/api/auth/**").build();
    }

    @Bean
    GroupedOpenApi chatApiGroup() {
        return GroupedOpenApi.builder().group("chat").displayName("对话与任务")
                .pathsToMatch("/api/chat/**", "/api/tasks/**", "/api/approvals/**").build();
    }

    @Bean
    GroupedOpenApi adminApiGroup() {
        return GroupedOpenApi.builder().group("admin").displayName("管理后台").pathsToMatch("/api/admin/**").build();
    }
}
