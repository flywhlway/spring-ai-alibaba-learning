package com.flywhl.saa.mcpserver;

import org.junit.jupiter.api.Test;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 冒烟 IT：Spring context 加载且 {@link OrderTools} 含 {@link McpTool} 方法。
 *
 * @author flywhl
 */
@SpringBootTest
class McpServerDemoApplicationIT {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private OrderTools orderTools;

    @Test
    void contextLoadsWithMcpToolBean() {
        assertThat(applicationContext.getBean(OrderTools.class)).isSameAs(orderTools);

        Method[] mcpTools = Arrays.stream(OrderTools.class.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(McpTool.class))
                .toArray(Method[]::new);

        assertThat(mcpTools).isNotEmpty();
        assertThat(orderTools.getOrderStatus("SO-IT-001"))
                .contains("SO-IT-001")
                .contains("配送中");
    }
}
