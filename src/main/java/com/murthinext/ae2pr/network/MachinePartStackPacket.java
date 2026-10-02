package com.murthinext.ae2pr.network;

import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

/**
 * 服务端 -> 客户端：赛特斯石英水晶总线存储（类型 + 数量）与自动搬运开关同步。
 * <p>
 * 原版 {@code writeItem} 的数量按 byte 传输（最大 127），因此物品只传 1 件，数量单独用 VarInt 传输。
 */
public class MachinePartStackPacket {

    private final BlockPos pos;
    private final ItemStack stack;
    private final boolean autoTransfer;

    public MachinePartStackPacket(BlockPos pos, ItemStack stack, boolean autoTransfer) {
        this.pos = pos;
        this.stack = stack;
        this.autoTransfer = autoTransfer;
    }

    public static void encode(MachinePartStackPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeItem(packet.stack.isEmpty() ? ItemStack.EMPTY : packet.stack.copyWithCount(1));
        buffer.writeVarInt(packet.stack.getCount());
        buffer.writeBoolean(packet.autoTransfer);
    }

    public static MachinePartStackPacket decode(FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        ItemStack stack = buffer.readItem();
        int amount = buffer.readVarInt();
        if (!stack.isEmpty()) {
            stack.setCount(amount);
        }
        return new MachinePartStackPacket(pos, stack, buffer.readBoolean());
    }

    public static void handle(MachinePartStackPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        var context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> com.murthinext.ae2pr.client.assembly_line.ItemBusScreen
                        .applyStackSync(packet.pos, packet.stack, packet.autoTransfer)));
        context.setPacketHandled(true);
    }
}
