package com.murthinext.ae2pr.repeat;

import java.util.UUID;
import java.util.concurrent.Future;

import org.jetbrains.annotations.Nullable;

import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import net.minecraft.server.level.ServerLevel;

/**
 * 通用（非 {@code CraftingCpuLogic}）重复订单上下文，用于 GTLCore 超限演算阵列、AAE 量子计算机等 VCPU 型 CPU。
 *
 * <p>与普通 CPU 路径不同，这里的轮次结束/取消通过 AE2 标准的
 * {@code CraftingJobStatusPacket} 识别，不依赖具体 CPU 的内部实现。
 */
public final class GenericRepeatOrder {
    /** 所属网络 */
    public IGrid grid;
    /** 发起时所在服务端世界（用于通知与重算） */
    public ServerLevel level;
    /** 原始下单来源 */
    public IActionSource source;
    /** AE2 玩家 ID（通知使用，可能为 null） */
    @Nullable
    public Integer ownerPlayerId;
    /** 每轮合成目标 */
    @Nullable
    public AEKey what;
    /** 每轮请求数量 */
    public long amountPerRound;
    /** 本轮计划最终产出量（用于匹配 STARTED 状态包） */
    public long pendingFinalAmount;
    /** 最近一轮计划最终产出量（用于匹配 FINISHED/CANCELLED 状态包） */
    public long lastFinalAmount;
    /** 总轮数 */
    public int totalRounds;
    /** 已完成的轮数 */
    public int completedRounds;
    /** 本次尝试已消耗的重试次数 */
    public int retryCount;
    /** 重试倒计时（tick） */
    public int retryCooldownTicks;
    /** 轮次状态保活通知倒计时（tick） */
    public int notifyCooldownTicks;
    /** 在途的异步模拟（同一时刻至多一个） */
    @Nullable
    public Future<ICraftingPlan> recalc;
    /** 下一轮的提交目标（可能为 null，表示自动选择） */
    @Nullable
    public ICraftingCPU cpu;
    /** 状态界面展示用的 CPU（通常是实际在执行任务的 vcpu） */
    @Nullable
    public ICraftingCPU displayCpu;
    /** 最近一轮的合成任务 ID，收到 STARTED 状态包后记录，用于精确匹配结束包 */
    @Nullable
    public UUID jobId;
    /** 状态机状态 */
    public RepeatOrderState state = RepeatOrderState.RUNNING;

    public int currentRound() {
        return Math.min(completedRounds + 1, totalRounds);
    }
}
