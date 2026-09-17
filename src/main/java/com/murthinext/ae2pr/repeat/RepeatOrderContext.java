package com.murthinext.ae2pr.repeat;

import java.util.concurrent.Future;

import org.jetbrains.annotations.Nullable;

import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.crafting.execution.ExecutingCraftingJob;

/**
 * 服务端重复订单上下文：附加在 {@code CraftingCpuLogic} 上（每个 CPU 一份）。
 */
public final class RepeatOrderContext {
    /** 每轮合成目标 */
    public AEKey what;
    /** 每轮数量 */
    public long amountPerRound;
    /** 本轮使用的计划（最近一次重算结果，缓存） */
    @Nullable
    public ICraftingPlan currentPlan;
    /** 在途的异步模拟（同一时刻至多一个，记忆化限流） */
    @Nullable
    public Future<ICraftingPlan> recalc;
    /** 原始下单来源（用于抽取/重算） */
    public IActionSource source;
    /** AE2 玩家 ID（失败通知使用，可能为 null） */
    @Nullable
    public Integer ownerPlayerId;
    /** 总轮数 */
    public int totalRounds;
    /** 已成功完成的轮数 */
    public int completedRounds;
    /** 本次尝试已消耗的重试次数 */
    public int retryCount;
    /** 重试倒计时（tick） */
    public int retryCooldownTicks;
    /** 轮次状态保活通知倒计时（tick），用于玩家重登/存档恢复后补发剩余轮数 */
    public int notifyCooldownTicks;
    /** 当前正在执行的 AE2 任务（用于识别任务归属） */
    @Nullable
    public ExecutingCraftingJob currentJob;
    /** 状态机状态 */
    public RepeatOrderState state = RepeatOrderState.RUNNING;

    public int currentRound() {
        return Math.min(completedRounds + 1, totalRounds);
    }
}
