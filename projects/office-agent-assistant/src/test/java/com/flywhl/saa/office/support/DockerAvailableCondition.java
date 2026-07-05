package com.flywhl.saa.office.support;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.DockerClientFactory;

/**
 * 仅当 Docker 可用时运行 Testcontainers 集成测试。
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
