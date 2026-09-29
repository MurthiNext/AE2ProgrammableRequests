package com.murthinext.ae2pr.network;

import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

/**
 * 服务端 -> 客户端：赛特斯石英输入仓的罐内流体同步（仅发给打开对应界面的玩家）。
 */
public class MachinePartFluidPacket {

    private final BlockPos pos;
    private final FluidStack fluid;

    public MachinePartFluidPacket(BlockPos pos, FluidStack fluid) {
        this.pos = pos;
        this.fluid = fluid;
    }

    public static void encode(MachinePartFluidPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeFluidStack(packet.fluid);
    }

    public static MachinePartFluidPacket decode(FriendlyByteBuf buffer) {
        return new MachinePartFluidPacket(buffer.readBlockPos(), buffer.readFluidStack());
    }

    public static void handle(MachinePartFluidPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        var context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> com.murthinext.ae2pr.client.assembly_line.FluidHatchScreen
                        .applyFluidSync(packet.pos, packet.fluid)));
        context.setPacketHandled(true);
    }
}
