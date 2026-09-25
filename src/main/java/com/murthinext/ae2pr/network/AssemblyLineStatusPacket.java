package com.murthinext.ae2pr.network;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

/**
 * 服务端 -> 客户端：水晶装配线的成型状态与诊断信息，用于打开/刷新状态界面。
 */
public class AssemblyLineStatusPacket {

    private final BlockPos pos;
    private final boolean formed;
    private final int slices;
    private final int minSlices;
    private final int maxSlices;
    private final int mismatches;
    @Nullable
    private final BlockPos mismatchPos;
    private final char expected;
    private final String foundId;

    public AssemblyLineStatusPacket(BlockPos pos, boolean formed, int slices, int minSlices, int maxSlices,
            int mismatches, @Nullable BlockPos mismatchPos, char expected, String foundId) {
        this.pos = pos;
        this.formed = formed;
        this.slices = slices;
        this.minSlices = minSlices;
        this.maxSlices = maxSlices;
        this.mismatches = mismatches;
        this.mismatchPos = mismatchPos;
        this.expected = expected;
        this.foundId = foundId;
    }

    public static void encode(AssemblyLineStatusPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeBoolean(packet.formed);
        buffer.writeVarInt(packet.slices);
        buffer.writeVarInt(packet.minSlices);
        buffer.writeVarInt(packet.maxSlices);
        buffer.writeVarInt(packet.mismatches);
        buffer.writeBoolean(packet.mismatchPos != null);
        if (packet.mismatchPos != null) {
            buffer.writeBlockPos(packet.mismatchPos);
        }
        buffer.writeChar(packet.expected);
        buffer.writeUtf(packet.foundId);
    }

    public static AssemblyLineStatusPacket decode(FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        boolean formed = buffer.readBoolean();
        int slices = buffer.readVarInt();
        int minSlices = buffer.readVarInt();
        int maxSlices = buffer.readVarInt();
        int mismatches = buffer.readVarInt();
        BlockPos mismatchPos = buffer.readBoolean() ? buffer.readBlockPos() : null;
        char expected = buffer.readChar();
        String foundId = buffer.readUtf();
        return new AssemblyLineStatusPacket(pos, formed, slices, minSlices, maxSlices, mismatches, mismatchPos,
                expected, foundId);
    }

    public static void handle(AssemblyLineStatusPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        var context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> com.murthinext.ae2pr.client.assembly_line.ClientAssemblyLineStatus.onStatus(packet)));
        context.setPacketHandled(true);
    }

    public BlockPos pos() {
        return pos;
    }

    public boolean formed() {
        return formed;
    }

    public int slices() {
        return slices;
    }

    public int minSlices() {
        return minSlices;
    }

    public int maxSlices() {
        return maxSlices;
    }

    public int mismatches() {
        return mismatches;
    }

    @Nullable
    public BlockPos mismatchPos() {
        return mismatchPos;
    }

    public char expected() {
        return expected;
    }

    public String foundId() {
        return foundId;
    }
}
