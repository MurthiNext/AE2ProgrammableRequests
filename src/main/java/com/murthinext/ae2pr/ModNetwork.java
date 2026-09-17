package com.murthinext.ae2pr;

import com.murthinext.ae2pr.network.RepeatOrderStatusPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
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
    }

    public static void sendToPlayer(ServerPlayer player, RepeatOrderStatusPacket packet) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }
}
