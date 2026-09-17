package com.murthinext.ae2pr.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.murthinext.ae2pr.Config;
import com.murthinext.ae2pr.ae2pr;
import com.murthinext.ae2pr.repeat.FailureReason;
import com.murthinext.ae2pr.repeat.IRepeatOrderHost;
import com.murthinext.ae2pr.repeat.RepeatOrderBinding;
import com.murthinext.ae2pr.repeat.RepeatOrderContext;
import com.murthinext.ae2pr.repeat.RepeatOrderState;

import appeng.api.features.IPlayerRegistry;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.CalculationStrategy;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.crafting.ICraftingSubmitResult;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.security.IActionSource;
import appeng.crafting.execution.CraftingCpuLogic;
import appeng.crafting.execution.ExecutingCraftingJob;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.me.service.CraftingService;
import net.minecraft.server.level.ServerPlayer;

/**
 * 重复订单核心状态机。
 *
 * 轮次流转：提交成功绑定上下文 -> 本轮 finishJob(true) 推进轮次 -> 下一 tick 异步重算 -> 提交下一轮；
 * 失败按间隔重试，重试耗尽后清理（M2 阶段补充失败通知）。
 */
@Mixin(value = CraftingCpuLogic.class, remap = false)
public abstract class CraftingCpuLogicMixin implements IRepeatOrderHost {

    @Shadow
    @Final
    CraftingCPUCluster cluster;

    @Shadow
    @Nullable
    private ExecutingCraftingJob job;

    @Unique
    @Nullable
    private RepeatOrderContext ae2pr$context;

    @Override
    @Nullable
    public RepeatOrderContext ae2pr$getContext() {
        return this.ae2pr$context;
    }

    @Override
    public void ae2pr$setContext(@Nullable RepeatOrderContext context) {
        this.ae2pr$context = context;
    }

    /**
     * 提交成功且本次下单携带重复轮数时，绑定订单上下文。
     */
    @Inject(method = "trySubmitJob", at = @At("RETURN"))
    private void ae2pr$bindRepeatOrder(IGrid grid, ICraftingPlan plan, IActionSource src,
            ICraftingRequester requester, CallbackInfoReturnable<ICraftingSubmitResult> cir) {
        var rounds = RepeatOrderBinding.consume();
        if (rounds == null || rounds <= 1) {
            return;
        }
        var result = cir.getReturnValue();
        if (result == null || !result.successful() || this.ae2pr$context != null) {
            return;
        }

        var context = new RepeatOrderContext();
        context.what = plan.finalOutput().what();
        context.amountPerRound = plan.finalOutput().amount();
        context.source = src;
        context.ownerPlayerId = ae2pr$getPlayerId(src);
        context.totalRounds = rounds;
        context.currentJob = this.job;
        context.state = RepeatOrderState.RUNNING;
        this.ae2pr$context = context;
    }

    /**
     * 任务结束：仅处理属于本订单的任务；成功则推进轮次，取消则清空。
     */
    @Inject(method = "finishJob", at = @At("HEAD"))
    private void ae2pr$onFinishJob(boolean success, CallbackInfo ci) {
        var context = this.ae2pr$context;
        if (context == null || context.currentJob != this.job) {
            return;
        }
        context.currentJob = null;

        if (!success) {
            context.state = RepeatOrderState.CANCELLED;
            this.ae2pr$context = null;
            return;
        }

        context.completedRounds++;
        context.retryCount = 0;
        if (context.completedRounds >= context.totalRounds) {
            context.state = RepeatOrderState.DONE;
            this.ae2pr$context = null;
        } else {
            context.state = RepeatOrderState.PENDING_NEXT;
        }
    }

    /**
     * 每 tick 驱动状态机：重算 -> 提交 -> 重试。
     */
    @Inject(method = "tickCraftingLogic", at = @At("HEAD"))
    private void ae2pr$tickRepeatOrder(IEnergyService energyService, CraftingService craftingService,
            CallbackInfo ci) {
        var context = this.ae2pr$context;
        if (context == null) {
            return;
        }

        if (this.job != null) {
            if (context.state == RepeatOrderState.RUNNING && context.currentJob == this.job) {
                return;
            }
            if (context.state == RepeatOrderState.PENDING_NEXT || context.state == RepeatOrderState.PLANNING
                    || context.state == RepeatOrderState.RUNNING) {
                // 两轮之间 CPU 被其他任务占用（或本任务异常消失）
                context.recalc = null;
                this.ae2pr$retryOrFail(context, FailureReason.CPU_BUSY);
            }
            return;
        }

        switch (context.state) {
            case PENDING_NEXT -> this.ae2pr$beginRecalc(context);
            case PLANNING -> this.ae2pr$pollRecalc(context);
            case RETRY_WAIT -> {
                if (--context.retryCooldownTicks <= 0) {
                    this.ae2pr$beginRecalc(context);
                }
            }
            case RUNNING -> this.ae2pr$retryOrFail(context, FailureReason.UNKNOWN);
            default -> {
            }
        }
    }

    /**
     * 玩家/外部取消任务：清空重复订单。
     */
    @Inject(method = "cancel", at = @At("HEAD"))
    private void ae2pr$onCancel(CallbackInfo ci) {
        var context = this.ae2pr$context;
        if (context != null) {
            context.recalc = null;
            context.state = RepeatOrderState.CANCELLED;
            this.ae2pr$context = null;
        }
    }

    @Unique
    private void ae2pr$beginRecalc(RepeatOrderContext context) {
        var grid = this.cluster.getGrid();
        var level = this.cluster.getLevel();
        if (grid == null || level == null) {
            this.ae2pr$retryOrFail(context, FailureReason.UNKNOWN);
            return;
        }
        try {
            context.recalc = grid.getCraftingService().beginCraftingCalculation(level, () -> context.source,
                    context.what, context.amountPerRound, CalculationStrategy.CRAFT_LESS);
            context.state = RepeatOrderState.PLANNING;
        } catch (Throwable t) {
            ae2pr.LOGGER.warn("Failed to recalculate repeat order plan", t);
            this.ae2pr$retryOrFail(context, FailureReason.UNKNOWN);
        }
    }

    @Unique
    private void ae2pr$pollRecalc(RepeatOrderContext context) {
        var future = context.recalc;
        if (future == null) {
            context.state = RepeatOrderState.PENDING_NEXT;
            return;
        }
        if (!future.isDone()) {
            return;
        }
        context.recalc = null;

        ICraftingPlan plan;
        try {
            plan = future.get();
        } catch (Throwable t) {
            this.ae2pr$retryOrFail(context, FailureReason.UNKNOWN);
            return;
        }
        if (plan == null || plan.simulation()) {
            this.ae2pr$retryOrFail(context, FailureReason.MISSING_MATERIAL);
            return;
        }

        var grid = this.cluster.getGrid();
        if (grid == null) {
            this.ae2pr$retryOrFail(context, FailureReason.UNKNOWN);
            return;
        }

        ICraftingSubmitResult result;
        try {
            result = this.cluster.submitJob(grid, plan, context.source, null);
        } catch (Throwable t) {
            ae2pr.LOGGER.warn("Failed to submit next repeat order round", t);
            this.ae2pr$retryOrFail(context, FailureReason.UNKNOWN);
            return;
        }

        if (result != null && result.successful()) {
            context.currentPlan = plan;
            context.currentJob = this.job;
            context.retryCount = 0;
            context.state = RepeatOrderState.RUNNING;
        } else {
            this.ae2pr$retryOrFail(context, FailureReason.fromCode(result != null ? result.errorCode() : null));
        }
    }

    @Unique
    private void ae2pr$retryOrFail(RepeatOrderContext context, FailureReason reason) {
        context.recalc = null;
        if (++context.retryCount > Config.retryCount()) {
            ae2pr.LOGGER.info("Repeat order failed: {}x{} after {} retries (reason: {})",
                    context.amountPerRound, context.what, context.retryCount - 1, reason);
            context.state = RepeatOrderState.FAILED;
            this.ae2pr$context = null;
            return;
        }
        context.retryCooldownTicks = Config.retryIntervalTicks();
        context.state = RepeatOrderState.RETRY_WAIT;
    }

    @Unique
    @Nullable
    private static Integer ae2pr$getPlayerId(IActionSource source) {
        var player = source.player().orElse(null);
        if (player instanceof ServerPlayer serverPlayer) {
            return IPlayerRegistry.getPlayerId(serverPlayer);
        }
        return null;
    }
}
