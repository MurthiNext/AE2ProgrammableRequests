package com.murthinext.ae2pr.mixin;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.murthinext.ae2pr.ModNetwork;
import com.murthinext.ae2pr.network.RepeatOrderConfirmRoundsPacket;
import com.murthinext.ae2pr.repeat.GenericRepeatOrder;
import com.murthinext.ae2pr.repeat.GenericRepeatOrders;
import com.murthinext.ae2pr.repeat.RepeatOrderBinding;
import com.murthinext.ae2pr.repeat.RepeatOrderNotifier;
import com.murthinext.ae2pr.repeat.ServerRepeatRegistry;

import appeng.api.features.IPlayerRegistry;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.ISubMenuHost;
import appeng.menu.me.crafting.CraftConfirmMenu;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

/**
 * 确认菜单：构造时同步轮数给客户端，开始合成时消费登记表中的轮数并绑定到线程本地。
 *
 * <p>若本次提交没有落到 {@code CraftingCpuLogic}（例如 GTLCore 超限演算阵列等 VCPU 型 CPU），
 * 线程本地绑定不会被消费，此时改为登记通用重复订单，由 {@link GenericRepeatOrders} 驱动后续轮次。
 */
@Mixin(CraftConfirmMenu.class)
public abstract class CraftConfirmMenuMixin {

    @Shadow(remap = false)
    @Nullable
    private ICraftingPlan result;

    @Shadow(remap = false)
    @Nullable
    private ICraftingCPU selectedCpu;

    @Shadow(remap = false)
    protected abstract IGrid getGrid();

    @Shadow(remap = false)
    protected abstract IActionSource getActionSrc();

    /** 本次提交前处于忙碌的 CPU 快照，用于识别提交后新进入忙碌的 CPU */
    @Unique
    @Nullable
    private Set<ICraftingCPU> ae2pr$busyBefore;

    /**
     * 打开确认界面时把登记表中的轮数同步给客户端（标题显示）。
     * 由 {@code MenuOpenerMixin} 在打开前完成登记，因此这里直接 peek。
     */
    @Inject(method = "<init>", at = @At("TAIL"))
    private void ae2pr$syncConfirmRounds(int id, Inventory playerInventory, ISubMenuHost host, CallbackInfo ci) {
        var menu = (CraftConfirmMenu) (Object) this;
        if (menu.getPlayer() instanceof ServerPlayer serverPlayer) {
            var rounds = ServerRepeatRegistry.peek(serverPlayer.getUUID());
            if (rounds != null && rounds > 1) {
                ModNetwork.sendToPlayer(serverPlayer, new RepeatOrderConfirmRoundsPacket(menu.containerId, rounds));
            }
        }
    }

    // startJob 为 AE2 自定义方法，不做映射
    @Inject(method = "startJob", at = @At("HEAD"), remap = false)
    private void ae2pr$bindRepeatRounds(CallbackInfo ci) {
        var menu = (CraftConfirmMenu) (Object) this;
        if (!(menu.getPlayer() instanceof ServerPlayer serverPlayer)) {
            return;
        }
        var rounds = ServerRepeatRegistry.take(serverPlayer.getUUID());
        if (rounds == null || rounds <= 1) {
            return;
        }
        RepeatOrderBinding.set(rounds);
        try {
            var grid = this.getGrid();
            if (grid != null) {
                this.ae2pr$busyBefore = ae2pr$busyCpus(grid);
            }
        } catch (Throwable t) {
            this.ae2pr$busyBefore = null;
            GenericRepeatOrders.logObserverFailure(t);
        }
    }

    @Inject(method = "startJob", at = @At("RETURN"), remap = false)
    private void ae2pr$clearRepeatRoundsBinding(CallbackInfo ci) {
        var leftover = RepeatOrderBinding.consume();
        var busyBefore = this.ae2pr$busyBefore;
        this.ae2pr$busyBefore = null;
        if (leftover == null || leftover <= 1 || busyBefore == null) {
            return;
        }
        try {
            ae2pr$registerGenericOrder(leftover, busyBefore);
        } catch (Throwable t) {
            GenericRepeatOrders.logObserverFailure(t);
        }
    }

    /**
     * 提交未命中普通 CPU 时登记通用（VCPU）重复订单。
     */
    @Unique
    private void ae2pr$registerGenericOrder(int rounds, Set<ICraftingCPU> busyBefore) {
        var menu = (CraftConfirmMenu) (Object) this;
        if (!(menu.getPlayer() instanceof ServerPlayer serverPlayer) || this.result == null) {
            return;
        }
        var grid = this.getGrid();
        if (grid == null) {
            return;
        }

        ICraftingCPU newlyBusy = null;
        for (var cpu : grid.getCraftingService().getCpus()) {
            if (cpu.isBusy() && !busyBefore.contains(cpu)) {
                newlyBusy = cpu;
                break;
            }
        }
        var target = this.selectedCpu != null ? this.selectedCpu : newlyBusy;
        if (target == null) {
            return;
        }
        var finalOutput = this.result.finalOutput();
        if (newlyBusy == null && !ae2pr$isRunning(target, finalOutput)) {
            // 提交未成功（例如选中的 CPU 正在忙别的工作）
            return;
        }

        var order = new GenericRepeatOrder();
        order.grid = grid;
        order.level = serverPlayer.serverLevel();
        order.source = this.getActionSrc();
        order.ownerPlayerId = IPlayerRegistry.getPlayerId(serverPlayer);
        order.what = finalOutput.what();
        order.amountPerRound = Math.max(1, finalOutput.amount());
        order.pendingFinalAmount = order.amountPerRound;
        order.lastFinalAmount = order.amountPerRound;
        order.totalRounds = rounds;
        // 提交目标保持玩家选择/自动选择语义；展示目标取实际开始执行的 CPU
        order.cpu = this.selectedCpu;
        order.displayCpu = newlyBusy != null ? newlyBusy : target;
        GenericRepeatOrders.register(order);
        RepeatOrderNotifier.sendRound(order.ownerPlayerId, order.level, order.what, order.totalRounds,
                order.totalRounds);
        order.notifyCooldownTicks = 20;
    }

    @Unique
    private static boolean ae2pr$isRunning(ICraftingCPU cpu, GenericStack finalOutput) {
        if (!cpu.isBusy()) {
            return false;
        }
        var status = cpu.getJobStatus();
        return status != null && status.crafting() != null && status.crafting().what().equals(finalOutput.what());
    }

    @Unique
    private static Set<ICraftingCPU> ae2pr$busyCpus(IGrid grid) {
        Set<ICraftingCPU> busy = Collections.newSetFromMap(new IdentityHashMap<>());
        for (var cpu : grid.getCraftingService().getCpus()) {
            if (cpu.isBusy()) {
                busy.add(cpu);
            }
        }
        return busy;
    }

    // removed(Player) 是原版菜单方法重写，必须保留重映射
    /**
     * 离开确认界面（取消/返回/开始合成后）时把残留轮数转入恢复队列。
     * 返回数量界面时由 {@code CraftAmountMenu} 构造阶段回填显示；
     * GTLCore 会取消 {@code goBack} 主体，因此不能依赖其 RETURN。
     */
    @Inject(method = "removed", at = @At("HEAD"))
    private void ae2pr$pushRestoreRepeatRounds(Player player, CallbackInfo ci) {
        if (player instanceof ServerPlayer serverPlayer) {
            ServerRepeatRegistry.pushRestore(serverPlayer.getUUID());
        }
    }
}
