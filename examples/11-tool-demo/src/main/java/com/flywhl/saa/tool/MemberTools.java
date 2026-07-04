package com.flywhl.saa.tool;

import com.flywhl.saa.tool.model.TimeVO;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

/**
 * 演示两类工具设计：
 * <ul>
 *   <li>{@link #getMyMembershipLevel} —— 身份信息由 {@link ToolContext} 服务端注入，
 *       模型只感知"帮我查会员等级"这个意图，无法伪造/篡改 userId；</li>
 *   <li>{@link #getServerTime} —— {@code returnDirect=true}，结果是精确数据，
 *       跳过模型二次转述，避免转述引入的幻觉性误差。</li>
 * </ul>
 *
 * @author flywhl
 */
@Component
public class MemberTools {

    private static final Map<String, String> MEMBERSHIP_TABLE = Map.of(
            "u-1001", "钻石会员",
            "u-1002", "黄金会员");

    @Tool(description = "查询当前登录用户的会员等级，用户身份由服务端注入，不接受调用方声称的用户身份")
    public String getMyMembershipLevel(ToolContext toolContext) {
        String userId = (String) toolContext.getContext().getOrDefault("userId", "anonymous");
        return MEMBERSHIP_TABLE.getOrDefault(userId, "普通会员");
    }

    @Tool(description = "查询指定城市对应的服务端标准时间，结果为精确数据，无需模型转述", returnDirect = true)
    public TimeVO getServerTime(@ToolParam(description = "城市名称，如：上海") String city) {
        return new TimeVO(city, LocalDateTime.now().toString(), ZoneId.systemDefault().getId());
    }
}
