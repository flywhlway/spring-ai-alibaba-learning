package com.flywhl.saa.office.tool;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import com.flywhl.saa.office.model.entity.CalendarEvent;
import com.flywhl.saa.office.repository.CalendarEventRepository;

@Component
public class CalendarTool {
    private final CalendarEventRepository calendarEventRepository;
    public CalendarTool(CalendarEventRepository calendarEventRepository) {
        this.calendarEventRepository = calendarEventRepository;
    }
    @Tool(description = "查询当前用户指定日期范围内的日程")
    public String listEvents(@ToolParam(description = "开始日期 yyyy-MM-dd") String fromDate,
            @ToolParam(description = "结束日期 yyyy-MM-dd") String toDate,
            ToolContext toolContext) {
        Long userId = ToolSecuritySupport.userIdOf(toolContext);
        if (userId == null) return "无法识别当前用户";
        LocalDate from = LocalDate.parse(fromDate);
        LocalDate to = LocalDate.parse(toDate);
        List<CalendarEvent> events = calendarEventRepository.findByUserIdAndStartTimeBetweenOrderByStartTimeAsc(
                userId, LocalDateTime.of(from, LocalTime.MIN), LocalDateTime.of(to, LocalTime.MAX));
        if (events.isEmpty()) return "该时段无日程";
        StringBuilder sb = new StringBuilder();
        for (CalendarEvent e : events) {
            sb.append(e.getStartTime()).append(" ").append(e.getTitle());
            if (e.getLocation() != null) sb.append(" @").append(e.getLocation());
            sb.append("\n");
        }
        return sb.toString();
    }
}

