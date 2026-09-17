package com.murthinext.ae2pr.repeat;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

/**
 * 服务端登记表：玩家确认订单时暂存重复轮数，开始合成时消费。
 * 使用短时效防止玩家放弃确认界面后串单。
 */
public final class ServerRepeatRegistry {
    private static final long MAX_AGE_MILLIS = 5 * 60 * 1000L;

    private static final Map<UUID, Pending> PENDING = new HashMap<>();

    private ServerRepeatRegistry() {
    }

    public static void put(UUID playerId, int rounds) {
        PENDING.put(playerId, new Pending(rounds, System.currentTimeMillis()));
    }

    @Nullable
    public static Integer take(UUID playerId) {
        var pending = PENDING.remove(playerId);
        if (pending == null || System.currentTimeMillis() - pending.timestamp > MAX_AGE_MILLIS) {
            return null;
        }
        return pending.rounds;
    }

    /**
     * 查看但不移除登记值（用于从确认界面返回数量界面时恢复显示）。
     */
    @Nullable
    public static Integer peek(UUID playerId) {
        var pending = PENDING.get(playerId);
        if (pending == null || System.currentTimeMillis() - pending.timestamp > MAX_AGE_MILLIS) {
            return null;
        }
        return pending.rounds;
    }

    public static void clear(UUID playerId) {
        PENDING.remove(playerId);
    }

    private record Pending(int rounds, long timestamp) {
    }
}
