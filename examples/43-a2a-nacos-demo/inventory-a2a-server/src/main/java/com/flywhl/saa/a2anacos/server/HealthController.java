package com.flywhl.saa.a2anacos.server;

import com.flywhl.saa.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 本地健康检查（A2A 协议由 starter 自动暴露 JSON-RPC 端点）。
 *
 * @author flywhl
 */
@RestController
public class HealthController {

    @GetMapping("/health")
    public Result<String> health() {
        return Result.ok("inventory-a2a-server ready on 18043");
    }
}
