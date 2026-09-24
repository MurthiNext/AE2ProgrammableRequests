package com.murthinext.ae2pr.mixin;

import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.murthinext.ae2pr.logic.repeat.GenericRepeatOrders;

import appeng.api.stacks.AEKey;
import appeng.core.sync.packets.CraftingJobStatusPacket;

/**
 * 在"发送用"状态包构造时捕获参数，供通用（VCPU）重复订单识别轮次状态。
 *
 * <p>该构造器只把数据写入缓冲区、不写入实例字段（字段仅在客户端反序列化时赋值），
 * 因此不能通过字段访问器读取，只能在构造时捕获。
 */
@Mixin(value = CraftingJobStatusPacket.class, remap = false)
public abstract class CraftingJobStatusPacketMixin {

    @Inject(method = "<init>(Ljava/util/UUID;Lappeng/api/stacks/AEKey;JJLappeng/core/sync/packets/CraftingJobStatusPacket$Status;)V",
            at = @At("TAIL"), remap = false)
    private void ae2pr$captureStatus(UUID jobId, AEKey what, long requestedAmount, long remainingAmount,
            CraftingJobStatusPacket.Status status, CallbackInfo ci) {
        GenericRepeatOrders.observeConstruction(jobId, what, requestedAmount, status);
    }
}
