package com.flywhl.saa.supervisor;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;

/**
 * 日程相关 @Tool，供 calendar-agent 调用。
 *
 * @author flywhl
 */
@Component
public class CalendarTools {

    private static final Map<String, String> SCHEDULE = Map.of(
            "2026-07-05", "10:00 产品评审 | 14:00 客户拜访",
            "2026-07-06", "09:30 团队站会 | 16:00 架构讨论");

    @Tool(description = "查询指定日期的日程安排，日期格式 yyyy-MM-dd")
    public String querySchedule(@ToolParam(description = "日期，如 2026-07-05") String date) {
        return SCHEDULE.getOrDefault(date, date + " 暂无已安排会议");
    }

    @Tool(description = "安排一场会议并返回确认信息")
    public String scheduleMeeting(@ToolParam(description = "会议主题") String topic,
                                  @ToolParam(description = "日期 yyyy-MM-dd") String date,
                                  @ToolParam(description = "开始时间 HH:mm") String time) {
        return "已安排会议「" + topic + "」于 " + date + " " + time + "（模拟写入）";
    }

    @Tool(description = "返回今天日期，便于用户说「明天」时换算")
    public String today() {
        return LocalDate.now().toString();
    }
}
