package com.flywhl.saa.mcpserver;

import com.flywhl.saa.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 简易健康检查，便于确认进程已启动。
 *
 * @author flywhl
 */
@RestController
public class HealthController {

    @GetMapping("/health")
    public Result<String> health() {
        return Result.ok("mcp-server-demo ready, endpoint=/mcp");
    }
}
