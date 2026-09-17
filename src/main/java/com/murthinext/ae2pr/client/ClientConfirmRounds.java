package com.murthinext.ae2pr.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * 客户端：确认界面（CraftConfirmMenu）的重复轮数，按菜单 containerId 匹配，避免时序问题。
 */
@OnlyIn(Dist.CLIENT)
public final class ClientConfirmRounds {
    private static int containerId = -1;
    private static int rounds = 1;

    private ClientConfirmRounds() {
    }

    public static void update(int menuContainerId, int value) {
        containerId = menuContainerId;
        rounds = Math.max(1, value);
    }

    /**
     * @return 指定菜单的重复轮数；不匹配时返回 1
     */
    public static int getFor(int menuContainerId) {
        return menuContainerId == containerId ? rounds : 1;
    }

    public static void clear() {
        containerId = -1;
        rounds = 1;
    }
}
