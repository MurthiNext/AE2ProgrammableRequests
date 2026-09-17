package com.murthinext.ae2pr.client;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.murthinext.ae2pr.client.gui.RepeatOrderFinishedToast;

import appeng.api.stacks.AEKey;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * 客户端：记录当前存在重复订单的物品（含剩余/总轮数）。
 *
 * 中间轮的 AE2 完成 Toast 由本记录抑制；最终轮的完成提示由服务端 {@code RepeatOrderFinishedPacket}
 * 触发 {@link RepeatOrderFinishedToast}，因此不依赖客户端对轮次的推断，重登/读档后也可靠。
 */
@OnlyIn(Dist.CLIENT)
public final class ClientRepeatOrderRounds {
    private static final Map<AEKey, RoundInfo> ROUNDS = new HashMap<>();

    private ClientRepeatOrderRounds() {
    }

    public static void update(@Nullable AEKey what, int remainingRounds, int totalRounds) {
        if (what == null) {
            return;
        }
        if (remainingRounds <= 0) {
            ROUNDS.remove(what);
        } else {
            ROUNDS.put(what, new RoundInfo(remainingRounds, totalRounds));
        }
    }

    @Nullable
    public static RoundInfo get(@Nullable AEKey what) {
        if (what == null) {
            return null;
        }
        return ROUNDS.get(what);
    }

    /**
     * 收到订单完成通知：清理活跃标记并弹出带总轮数的完成提示。
     */
    public static void onFinished(@Nullable AEKey what, long amountPerRound, int totalRounds) {
        if (what == null) {
            return;
        }
        ROUNDS.remove(what);
        RepeatOrderFinishedToast.show(what, amountPerRound, totalRounds);
    }

    public static void clear() {
        ROUNDS.clear();
    }

    /**
     * @param remainingRounds 含当前轮的剩余轮数
     * @param totalRounds     总轮数
     */
    public record RoundInfo(int remainingRounds, int totalRounds) {
    }
}
