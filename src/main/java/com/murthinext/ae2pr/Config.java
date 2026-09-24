package com.murthinext.ae2pr;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * 重复下单相关配置。
 */
public final class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.IntValue MAX_ROUNDS;
    private static final ForgeConfigSpec.IntValue RETRY_COUNT;
    private static final ForgeConfigSpec.IntValue RETRY_INTERVAL_TICKS;
    private static final ForgeConfigSpec.BooleanValue NOTIFY_ON_FAILURE;

    private static final ForgeConfigSpec.IntValue REQUESTER_SLOTS;
    private static final ForgeConfigSpec.DoubleValue REQUESTER_IDLE_ENERGY;
    private static final ForgeConfigSpec.BooleanValue REQUESTER_REQUIRE_CHANNEL;

    static final ForgeConfigSpec SPEC;

    static {
        BUILDER.push("repeatOrder");
        MAX_ROUNDS = BUILDER
                .comment("单次重复下单允许的最大轮数")
                .defineInRange("maxRounds", Integer.MAX_VALUE, 1, Integer.MAX_VALUE);
        RETRY_COUNT = BUILDER
                .comment("开始下一轮失败后的重试次数（每次重试会重新模拟计划）")
                .defineInRange("retryCount", 3, 0, 100);
        RETRY_INTERVAL_TICKS = BUILDER
                .comment("相邻两次重试之间的间隔（tick）")
                .defineInRange("retryIntervalTicks", 20, 1, 72000);
        NOTIFY_ON_FAILURE = BUILDER
                .comment("重复订单失败时是否发送无线终端通知")
                .define("notifyOnFailure", true);
        BUILDER.pop();

        BUILDER.push("redstoneRequester");
        REQUESTER_SLOTS = BUILDER
                .comment("单个 ME 红石请求器可标记的物品数量")
                .defineInRange("slots", 5, 1, 63);
        REQUESTER_IDLE_ENERGY = BUILDER
                .comment("ME 红石请求器空闲时从网络抽取的能量（AE）")
                .defineInRange("idleEnergy", 5.0, 0.0, Double.MAX_VALUE);
        REQUESTER_REQUIRE_CHANNEL = BUILDER
                .comment("ME 红石请求器是否需要占用一个网络频道")
                .define("requireChannel", true);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    private Config() {
    }

    public static int maxRounds() {
        return MAX_ROUNDS.get();
    }

    public static int retryCount() {
        return RETRY_COUNT.get();
    }

    public static int retryIntervalTicks() {
        return RETRY_INTERVAL_TICKS.get();
    }

    public static boolean notifyOnFailure() {
        return NOTIFY_ON_FAILURE.get();
    }

    public static int requesterSlots() {
        return REQUESTER_SLOTS.get();
    }

    public static double requesterIdleEnergy() {
        return REQUESTER_IDLE_ENERGY.get();
    }

    public static boolean requesterRequireChannel() {
        return REQUESTER_REQUIRE_CHANNEL.get();
    }
}
