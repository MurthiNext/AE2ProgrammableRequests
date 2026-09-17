package com.murthinext.ae2pr.mixin;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.murthinext.ae2pr.ModNetwork;
import com.murthinext.ae2pr.network.CpuRoundInfo;
import com.murthinext.ae2pr.network.RepeatOrderStatusPacket;
import com.murthinext.ae2pr.repeat.IRepeatOrderHost;

import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.menu.me.crafting.CraftingStatusMenu;
import net.minecraft.server.level.ServerPlayer;

/**
 * 合成状态菜单：CPU 列表刷新时，把重复订单轮次同步给正在查看该菜单的玩家。
 * 仅在内容变化时发送；首次广播必发。
 */
@Mixin(CraftingStatusMenu.class)
public abstract class CraftingStatusMenuBroadcastMixin {

    @Unique
    private List<CpuRoundInfo> ae2pr$lastSentRounds;

    // broadcastChanges 是原版菜单方法重写，必须保留重映射
    @Inject(method = "broadcastChanges", at = @At("RETURN"))
    private void ae2pr$sendRepeatRounds(CallbackInfo ci) {
        var menu = (CraftingStatusMenu) (Object) this;
        if (!(menu.getPlayer() instanceof ServerPlayer serverPlayer)) {
            return;
        }

        var accessor = (CraftingStatusMenuAccessor) (Object) this;
        List<CpuRoundInfo> rounds = new ArrayList<>();
        for (var cpu : accessor.ae2pr$getLastCpuSet()) {
            if (cpu instanceof CraftingCPUCluster cluster) {
                var context = ((IRepeatOrderHost) (Object) cluster.craftingLogic).ae2pr$getContext();
                if (context != null) {
                    rounds.add(new CpuRoundInfo(
                            accessor.ae2pr$getOrAssignCpuSerial(cpu),
                            context.currentRound(),
                            context.totalRounds));
                }
            }
        }

        if (rounds.equals(this.ae2pr$lastSentRounds)) {
            return;
        }
        this.ae2pr$lastSentRounds = rounds;
        ModNetwork.sendToPlayer(serverPlayer, new RepeatOrderStatusPacket(menu.containerId, rounds));
    }
}
