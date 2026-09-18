package com.murthinext.ae2pr.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.murthinext.ae2pr.repeat.GenericRepeatOrders;

import appeng.core.sync.BasePacket;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.CraftingJobStatusPacket;
import net.minecraft.server.level.ServerPlayer;

/**
 * 观察发往玩家的合成任务状态包，用于通用（VCPU）重复订单的轮次推进。
 *
 * <p>AE2 与 GTLCore 的合成逻辑都会通过该包上报 STARTED/FINISHED/CANCELLED，
 * 因此无需依赖具体 CPU 实现即可判断轮次是否结束。钩子必须完全防御式：
 * 任何异常都不能影响正常下单流程。
 */
@Mixin(value = NetworkHandler.class, remap = false)
public abstract class NetworkHandlerMixin {

    @Inject(method = "sendTo", at = @At("HEAD"), remap = false)
    private void ae2pr$observeCraftingJobStatus(BasePacket message, ServerPlayer player, CallbackInfo ci) {
        if (!(message instanceof CraftingJobStatusPacket)) {
            return;
        }
        var pending = GenericRepeatOrders.pollConstruction();
        if (pending == null) {
            return;
        }
        try {
            GenericRepeatOrders.onJobStatus(player, pending.jobId(), pending.what(), pending.requestedAmount(),
                    pending.status());
        } catch (Throwable t) {
            GenericRepeatOrders.logObserverFailure(t);
        }
    }
}
