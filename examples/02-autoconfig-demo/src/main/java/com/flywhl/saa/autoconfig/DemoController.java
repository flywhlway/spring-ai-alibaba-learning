package com.flywhl.saa.autoconfig;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 暴露 GreetingService 的问候接口，验证自定义自动装配的 Bean 已被注入。
 *
 * @author flywhl
 */
@RestController
public class DemoController {

    private final GreetingService greetingService;

    public DemoController(GreetingService greetingService) {
        this.greetingService = greetingService;
    }

    @GetMapping("/greet")
    public String greet(@RequestParam(defaultValue = "flywhl") String name) {
        return greetingService.greet(name);
    }
}
