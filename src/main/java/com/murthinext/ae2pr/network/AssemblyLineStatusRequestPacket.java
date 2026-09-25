package com.murthinext.ae2pr.network;

import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import com.murthinext.ae2pr.ModNetwork;
import com.murthinext.ae2pr.block.assembly_line.AssemblyLineControllerBlockEntity;

/**
 * 客户端 -> 服务端：请求刷新水晶装配线的成型状态（状态界面打开时每秒一次）。
 */
public class AssemblyLineStatusRequestPacket {

    /** 允许请求的最大距离平方（防止远距离探测方块实体） */
    private static final double MAX_DISTANCE_SQR = 64.0 * 64.0;

    private final BlockPos pos;

    public AssemblyLineStatusRequestPacket(BlockPos pos) {
        this.pos = pos;
    }

    public static void encode(AssemblyLineStatusRequestPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
    }

    public static AssemblyLineStatusRequestPacket decode(FriendlyByteBuf buffer) {
        return new AssemblyLineStatusRequestPacket(buffer.readBlockPos());
    }

    public static void handle(AssemblyLineStatusRequestPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        var context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }
            if (player.distanceToSqr(packet.pos.getX() + 0.5, packet.pos.getY() + 0.5,
                    packet.pos.getZ() + 0.5) > MAX_DISTANCE_SQR) {
                return;
            }
            if (player.level().getBlockEntity(packet.pos) instanceof AssemblyLineControllerBlockEntity controller) {
                ModNetwork.sendAssemblyLineStatus(player, packet.pos, controller);
            }
        });
        context.setPacketHandled(true);
    }
}
