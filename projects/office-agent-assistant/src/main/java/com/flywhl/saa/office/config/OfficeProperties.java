package com.flywhl.saa.office.config;

import java.time.Duration;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 项目自有配置（前缀 {@code office}）。
 *
 * @author flywhl
 */
@ConfigurationProperties(prefix = "office")
public record OfficeProperties(
        VectorDatasource vectorDatasource,
        Memory memory,
        Tool tool,
        Approval approval,
        Security security) {

    public OfficeProperties {
        if (vectorDatasource == null) {
            vectorDatasource = new VectorDatasource(
                    "jdbc:postgresql://localhost:5432/office_vector", "saa", "saa123456", true);
        }
        if (memory == null) {
            memory = new Memory(20, Duration.ofDays(3));
        }
        if (tool == null) {
            tool = new Tool(
                    new Sql(List.of("report_sales", "report_attendance", "approval_request"), 200),
                    new Http(List.of("localhost", "127.0.0.1"), Duration.ofSeconds(10)),
                    new Excel(System.getProperty("java.io.tmpdir") + "/office-agent/excel"));
        }
        if (approval == null) {
            approval = new Approval(5000);
        }
        if (security == null) {
            security = new Security(new Jwt("office-agent-assistant", Duration.ofHours(2), null));
        }
    }

    public record VectorDatasource(String url, String username, String password, Boolean enabled) {
        public VectorDatasource {
            if (enabled == null) {
                enabled = true;
            }
        }
    }

    public record Memory(int shortTermMaxMessages, Duration shortTermTtl) {
    }

    public record Tool(Sql sql, Http http, Excel excel) {
    }

    public record Sql(List<String> allowedTables, int maxRows) {
    }

    public record Http(List<String> allowedHosts, Duration timeout) {
    }

    public record Excel(String workspace) {
    }

    public record Approval(int escalationThreshold) {
    }

    public record Security(Jwt jwt) {
    }

    public record Jwt(String issuer, Duration accessTokenTtl, String secret) {

        public Jwt {
            if (secret == null || secret.isBlank()) {
                secret = "dev-only-office-jwt-secret-32bytes!!";
            }
        }
    }
}
