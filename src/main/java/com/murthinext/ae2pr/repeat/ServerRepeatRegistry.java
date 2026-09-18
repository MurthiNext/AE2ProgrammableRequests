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
    /**
     * 返回数量界面时的恢复队列时效：确认菜单关闭与数量菜单构造在同一次调用栈内完成，
     * 仅需覆盖极短窗口，避免过期值被下一个订单误用。
     */
    private static final long RESTORE_MAX_AGE_MILLIS = 2 * 1000L;

    private static final Map<UUID, Pending> PENDING = new HashMap<>();
    private static final Map<UUID, Pending> RESTORE = new HashMap<>();

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

    /**
     * 把待确认轮数转入恢复队列，供随后打开的数量菜单回填显示。
     * 由确认菜单关闭时调用（此时数量菜单尚未构造）。
     */
    public static void pushRestore(UUID playerId) {
        var pending = PENDING.remove(playerId);
        if (pending != null) {
            RESTORE.put(playerId, new Pending(pending.rounds, System.currentTimeMillis()));
        }
    }

    /**
     * 消费恢复队列（数量菜单构造时调用），仅接受短时效内的值。
     */
    @Nullable
    public static Integer consumeRestore(UUID playerId) {
        var restore = RESTORE.remove(playerId);
        if (restore == null || System.currentTimeMillis() - restore.timestamp > RESTORE_MAX_AGE_MILLIS) {
            return null;
        }
        return restore.rounds;
    }

    public static void clearRestore(UUID playerId) {
        RESTORE.remove(playerId);
    }

    private record Pending(int rounds, long timestamp) {
    }
}
