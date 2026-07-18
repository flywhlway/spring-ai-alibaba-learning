package com.flywhl.saa.smartcs.service;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.alibaba.cloud.ai.graph.action.InterruptionMetadata;

/**
 * HITL 待审批会话存储：Chat 中断路径与 {@code /api/handoff/start} 共用，
 * 供坐席 {@code /api/handoff/approve} 取出 {@link InterruptionMetadata} 后 resume。
 *
 * <p>演示环境为进程内 {@link ConcurrentHashMap}；生产应外置 Redis/DB（见 REVIEW IN-03）。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Service
public class HitlPendingStore {

    /**
     * approve 时 resume 的目标智能体（须与产生中断时写入 checkpoint 的 Agent 一致）。
     */
    public enum ResumeAgent {
        /** {@code humanEscalationAgent}（start 直调与 chat 路由到人工分支） */
        HUMAN_ESCALATION
    }

    /**
     * @param metadata    中断元数据（含待审批 ToolFeedback）
     * @param resumeAgent resume 目标 Agent
     */
    public record PendingSession(InterruptionMetadata metadata, ResumeAgent resumeAgent) {
    }

    private final ConcurrentHashMap<String, PendingSession> pendingByThread = new ConcurrentHashMap<>();

    public void put(String threadId, InterruptionMetadata metadata, ResumeAgent resumeAgent) {
        pendingByThread.put(threadId, new PendingSession(metadata, resumeAgent));
    }

    public PendingSession remove(String threadId) {
        return pendingByThread.remove(threadId);
    }

    public boolean contains(String threadId) {
        return pendingByThread.containsKey(threadId);
    }
}
