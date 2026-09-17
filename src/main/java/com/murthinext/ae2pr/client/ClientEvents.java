package com.murthinext.ae2pr.client;

import com.murthinext.ae2pr.ae2pr;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 客户端事件：登出时清理缓存状态。
 */
@Mod.EventBusSubscriber(modid = ae2pr.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ClientEvents {
    private ClientEvents() {
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientRepeatOrderState.clear();
        ClientRepeatOrderFailures.clear();
        ClientRepeatOrderRounds.clear();
        ClientConfirmRounds.clear();
    }
}
