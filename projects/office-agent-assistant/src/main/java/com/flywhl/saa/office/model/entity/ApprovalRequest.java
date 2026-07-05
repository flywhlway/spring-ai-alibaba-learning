package com.flywhl.saa.office.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "approval_request")
@Getter @Setter
public class ApprovalRequest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "request_no", nullable = false, unique = true, length = 64)
    private String requestNo;
    @Column(nullable = false, length = 32)
    private String type;
    @Column(nullable = false, length = 256)
    private String title;
    @Column(precision = 12, scale = 2)
    private BigDecimal amount;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    @Column(name = "ai_summary", columnDefinition = "TEXT")
    private String aiSummary;
    @Column(name = "ai_opinion", columnDefinition = "TEXT")
    private String aiOpinion;
    @Column(nullable = false, length = 32)
    private String status = "PENDING";
    @Column(name = "applicant_id", nullable = false)
    private Long applicantId;
    @Column(name = "approver_id")
    private Long approverId;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

