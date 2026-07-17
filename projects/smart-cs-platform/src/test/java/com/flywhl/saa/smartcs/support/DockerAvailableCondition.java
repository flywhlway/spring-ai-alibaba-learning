package com.flywhl.saa.smartcs.support;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.DockerClientFactory;

/**
 * JUnit 5 条件：仅当本机 Docker 可用时启用 Testcontainers 集成测试。
 *
 * @author flywhl
 */
public final class DockerAvailableCondition implements ExecutionCondition {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        if (DockerClientFactory.instance().isDockerAvailable()) {
            return ConditionEvaluationResult.enabled("Docker 可用");
        }
        return ConditionEvaluationResult.disabled("Docker 不可用，跳过 Testcontainers IT");
    }
}
