package com.flywhl.saa.office.repository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.flywhl.saa.office.model.entity.CalendarEvent;
public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {
    List<CalendarEvent> findByUserIdAndStartTimeBetweenOrderByStartTimeAsc(Long userId, LocalDateTime from, LocalDateTime to);
}

