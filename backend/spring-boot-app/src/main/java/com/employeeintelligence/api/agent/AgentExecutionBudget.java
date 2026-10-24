package com.employeeintelligence.api.agent;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

/** Per-request guardrail for model/tool execution. */
public final class AgentExecutionBudget {
    private final int maxToolCalls;
    private final Duration timeout;
    private final AtomicInteger calls = new AtomicInteger();
    private static final ThreadLocal<AgentExecutionBudget> CURRENT = new ThreadLocal<>();
    public AgentExecutionBudget(int maxToolCalls, Duration timeout) { this.maxToolCalls = Math.max(1, maxToolCalls); this.timeout = timeout; }
    public static AgentExecutionBudget start(int maxToolCalls, Duration timeout) { AgentExecutionBudget b = new AgentExecutionBudget(maxToolCalls, timeout); CURRENT.set(b); return b; }
    public static AgentExecutionBudget current() { return CURRENT.get(); }
    public static void clear() { CURRENT.remove(); }
    public void consumeToolCall() { if (calls.incrementAndGet() > maxToolCalls) throw new IllegalStateException("agent tool-call budget exceeded"); }
    public int toolCalls() { return calls.get(); }
    public Duration timeout() { return timeout; }
}
