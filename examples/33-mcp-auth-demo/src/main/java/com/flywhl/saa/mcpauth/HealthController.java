package com.flywhl.saa.mcpauth;

import com.flywhl.saa.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author flywhl
 */
@RestController
public class HealthController {

    @GetMapping("/health")
    public Result<String> health() {
        return Result.ok("mcp-auth-demo ready, endpoint=/mcp, auth=Bearer demo-secret");
    }
}
