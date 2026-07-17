package com.flywhl.saa.smartcs.support;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.DockerClientFactory;

/**
 * JUnit 5 条件：仅当本机 Docker 可用时启用 Testcontainers 集成测试。
 * 自动探测 OrbStack socket（{@code ~/.orbstack/run/docker.sock}），兼容无
 * {@code /var/run/docker.sock} 的本机环境。
 *
 * @author flywhl
 */
public final class DockerAvailableCondition implements ExecutionCondition {

    static {
        configureDockerHostIfNeeded();
    }

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        configureDockerHostIfNeeded();
        if (DockerClientFactory.instance().isDockerAvailable()) {
            return ConditionEvaluationResult.enabled("Docker 可用");
        }
        return ConditionEvaluationResult.disabled("Docker 不可用，跳过 Testcontainers IT");
    }

    private static void configureDockerHostIfNeeded() {
        if (hasText(System.getenv("DOCKER_HOST")) || hasText(System.getProperty("DOCKER_HOST"))) {
            return;
        }
        Path defaultSock = Path.of("/var/run/docker.sock");
        if (Files.exists(defaultSock)) {
            return;
        }
        Path orbSock = Path.of(System.getProperty("user.home"), ".orbstack", "run", "docker.sock");
        if (Files.exists(orbSock)) {
            String uri = "unix://" + orbSock;
            System.setProperty("DOCKER_HOST", uri);
            System.setProperty("testcontainers.docker.socket.override", orbSock.toString());
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
