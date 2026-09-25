package com.murthinext.ae2pr.client.assembly_line;

import net.minecraft.client.Minecraft;

import com.murthinext.ae2pr.network.AssemblyLineStatusPacket;

/**
 * 客户端侧的水晶装配线状态界面管理：同一控制器复用一个界面，其余则新开。
 */
public final class ClientAssemblyLineStatus {

    private static AssemblyLineStatusScreen current;

    private ClientAssemblyLineStatus() {
    }

    public static void onStatus(AssemblyLineStatusPacket packet) {
        Minecraft minecraft = Minecraft.getInstance();
        if (current != null && current.isFor(packet.pos()) && minecraft.screen == current) {
            current.update(packet);
            return;
        }
        current = new AssemblyLineStatusScreen(packet);
        minecraft.setScreen(current);
    }

    public static void clear(AssemblyLineStatusScreen screen) {
        if (current == screen) {
            current = null;
        }
    }
}
