package com.murthinext.ae2pr.client;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.murthinext.ae2pr.Config;
import com.murthinext.ae2pr.client.gui.RepeatOrderFailedToast;
import com.murthinext.ae2pr.logic.repeat.FailureReason;

import appeng.api.stacks.AEKey;
import appeng.client.gui.me.common.MEStorageScreen;
import appeng.items.tools.powered.WirelessTerminalItem;
import appeng.util.SearchInventoryEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * 客户端失败标记：记录失败物品并渲染红闪/常亮背景，以及弹出失败 Toast。
 *
 * 闪烁语义：失败后首次显示该物品格子时开始闪烁 3 次（250ms 亮/250ms 灭），随后保持红色；
 * 重新打开终端（非从子界面返回）时清除已开始闪烁的标记，即使闪烁中途关闭终端也会被清除。
 */
@OnlyIn(Dist.CLIENT)
public final class ClientRepeatOrderFailures {
    private static final long BLINK_MS = 250L;
    private static final long FLASH_DURATION_MS = BLINK_MS * 2 * 3;
    private static final int FLASH_ALPHA = 0x66;
    private static final int FLASH_DIM_ALPHA = 0x1A;
    private static final int STEADY_ALPHA = 0x59;
    private static final int RED = 0xFF0000;

    private static final Map<AEKey, FailedMark> MARKS = new HashMap<>();

    private ClientRepeatOrderFailures() {
    }

    public static void onFailure(@Nullable AEKey what, long amountPerRound, int totalRounds, int completedRounds,
            FailureReason reason) {
        if (what == null) {
            return;
        }
        MARKS.put(what, new FailedMark(-1));

        var minecraft = Minecraft.getInstance();
        if (Config.notifyOnFailure()
                && !(minecraft.screen instanceof MEStorageScreen<?>)
                && minecraft.player != null
                && hasNotificationEnablingItem(minecraft.player)) {
            minecraft.getToasts().addToast(
                    new RepeatOrderFailedToast(what, amountPerRound, totalRounds, completedRounds, reason));
        }
    }

    /**
     * 与 AE2 完成提示相同的条件：身上有可用（有电且已链接）的无线终端。
     */
    public static boolean hasNotificationEnablingItem(LocalPlayer player) {
        for (var stack : SearchInventoryEvent.getItems(player)) {
            if (!stack.isEmpty()
                    && stack.getItem() instanceof WirelessTerminalItem wirelessTerminal
                    && wirelessTerminal.getAECurrentPower(stack) > 0
                    && wirelessTerminal.getLinkedPosition(stack) != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * 在终端物品格上渲染失败标记（在物品绘制之前调用）。
     */
    public static void renderMark(GuiGraphics guiGraphics, int x, int y, AEKey what) {
        var mark = MARKS.get(what);
        if (mark == null) {
            return;
        }

        var now = System.currentTimeMillis();
        var start = mark.flashStartMillis();
        if (start < 0) {
            start = now;
            MARKS.put(what, new FailedMark(start));
        }

        var elapsed = now - start;
        int alpha;
        if (elapsed < FLASH_DURATION_MS) {
            alpha = (elapsed / BLINK_MS) % 2 == 0 ? FLASH_ALPHA : FLASH_DIM_ALPHA;
        } else {
            alpha = STEADY_ALPHA;
        }

        guiGraphics.fill(x, y, x + 16, y + 16, (alpha << 24) | RED);
    }

    /**
     * 真正新开终端时调用：已经开始展示过（含中途关闭）的标记清除，从未展示的重置为待闪烁。
     */
    public static void onTerminalOpenedFresh() {
        MARKS.values().removeIf(mark -> mark.flashStartMillis() >= 0);
    }

    public static void clear() {
        MARKS.clear();
    }

    private record FailedMark(long flashStartMillis) {
    }
}
