package com.murthinext.ae2pr.mixin;

import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.murthinext.ae2pr.client.ClientRepeatOrderRounds;

import appeng.api.stacks.AEKey;
import appeng.client.gui.me.common.PendingCraftingJobs;
import appeng.core.sync.packets.CraftingJobStatusPacket;

/**
 * 客户端：存在重复订单的物品，其每轮完成的 AE2 原生 Toast 一律抑制；
 * 最终轮的完成提示由服务端 {@code RepeatOrderFinishedPacket} 触发（带总轮数）。
 * 若没有重复订单记录，则沿用 AE2 原生提示。
 */
@Mixin(PendingCraftingJobs.class)
public abstract class PendingCraftingJobsMixin {

    @Inject(method = "jobStatus", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/components/toasts/ToastComponent;addToast(Lnet/minecraft/client/gui/components/toasts/Toast;)V"),
            cancellable = true)
    private static void ae2pr$suppressRepeatOrderFinishedToast(UUID id, AEKey what, long requestedAmount,
            long remainingAmount, CraftingJobStatusPacket.Status status, CallbackInfo ci) {
        if (status != CraftingJobStatusPacket.Status.FINISHED) {
            return;
        }
        if (ClientRepeatOrderRounds.get(what) != null) {
            ci.cancel();
        }
    }
}
