package com.flywhl.saa.office.model.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "calendar_event")
@Getter @Setter
public class CalendarEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Column(nullable = false, length = 256)
    private String title;
    @Column(length = 256)
    private String location;
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;
    @Column(length = 512)
    private String remark;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}

