package com.murthinext.ae2pr;

import com.murthinext.ae2pr.block.assembly_line.AssemblyLineControllerBlockEntity;
import com.murthinext.ae2pr.block.assembly_line.AssemblyLineStructure;
import com.murthinext.ae2pr.network.AssemblyLineStatusPacket;
import com.murthinext.ae2pr.network.AssemblyLineStatusRequestPacket;
import com.murthinext.ae2pr.network.RepeatOrderConfirmRoundsPacket;
import com.murthinext.ae2pr.network.RepeatOrderFailedPacket;
import com.murthinext.ae2pr.network.RepeatOrderFinishedPacket;
import com.murthinext.ae2pr.network.RepeatOrderRoundPacket;
import com.murthinext.ae2pr.network.RepeatOrderStatusPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * 模组自有网络通道（仅用于 S2C 展示数据；C2S 轮数同步复用 AE2 菜单 client action）。
 */
public final class ModNetwork {
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(ae2pr.MODID, "main"))
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .simpleChannel();

    private static int nextMessageId;

    private ModNetwork() {
    }

    public static void register() {
        CHANNEL.registerMessage(nextMessageId++,
                RepeatOrderStatusPacket.class,
                RepeatOrderStatusPacket::encode,
                RepeatOrderStatusPacket::decode,
                RepeatOrderStatusPacket::handle);

        CHANNEL.registerMessage(nextMessageId++,
                RepeatOrderFailedPacket.class,
                RepeatOrderFailedPacket::encode,
                RepeatOrderFailedPacket::decode,
                RepeatOrderFailedPacket::handle);

        CHANNEL.registerMessage(nextMessageId++,
                RepeatOrderRoundPacket.class,
                RepeatOrderRoundPacket::encode,
                RepeatOrderRoundPacket::decode,
                RepeatOrderRoundPacket::handle);

        CHANNEL.registerMessage(nextMessageId++,
                RepeatOrderFinishedPacket.class,
                RepeatOrderFinishedPacket::encode,
                RepeatOrderFinishedPacket::decode,
                RepeatOrderFinishedPacket::handle);

        CHANNEL.registerMessage(nextMessageId++,
                RepeatOrderConfirmRoundsPacket.class,
                RepeatOrderConfirmRoundsPacket::encode,
                RepeatOrderConfirmRoundsPacket::decode,
                RepeatOrderConfirmRoundsPacket::handle);

        CHANNEL.registerMessage(nextMessageId++,
                AssemblyLineStatusPacket.class,
                AssemblyLineStatusPacket::encode,
                AssemblyLineStatusPacket::decode,
                AssemblyLineStatusPacket::handle);

        CHANNEL.registerMessage(nextMessageId++,
                AssemblyLineStatusRequestPacket.class,
                AssemblyLineStatusRequestPacket::encode,
                AssemblyLineStatusRequestPacket::decode,
                AssemblyLineStatusRequestPacket::handle);
    }

    /** 下发一次水晶装配线的成型状态（用于打开/刷新状态界面）。 */
    public static void sendAssemblyLineStatus(ServerPlayer player, BlockPos pos,
            AssemblyLineControllerBlockEntity controller) {
        Block found = controller.getLastFound();
        ResourceLocation foundKey = found != null ? ForgeRegistries.BLOCKS.getKey(found) : null;
        String foundId = foundKey != null ? foundKey.toString() : "";
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new AssemblyLineStatusPacket(pos, controller.isFormed(), controller.getLastSlices(),
                        AssemblyLineStructure.MIN_SLICES, AssemblyLineStructure.MAX_SLICES,
                        controller.getLastMismatches(), controller.getLastMismatchPos(),
                        controller.getLastExpected(), foundId));
    }

    public static void sendToPlayer(ServerPlayer player, RepeatOrderStatusPacket packet) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendToPlayer(ServerPlayer player, RepeatOrderFailedPacket packet) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendToPlayer(ServerPlayer player, RepeatOrderRoundPacket packet) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendToPlayer(ServerPlayer player, RepeatOrderFinishedPacket packet) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendToPlayer(ServerPlayer player, RepeatOrderConfirmRoundsPacket packet) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }
}
