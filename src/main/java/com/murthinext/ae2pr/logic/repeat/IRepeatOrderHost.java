package com.murthinext.ae2pr.logic.repeat;

import org.jetbrains.annotations.Nullable;

/**
 * Duck 接口：由 {@code CraftingCpuLogic} 的 mixin 实现，用于访问重复订单上下文。
 */
public interface IRepeatOrderHost {
    @Nullable
    RepeatOrderContext ae2pr$getContext();

    void ae2pr$setContext(@Nullable RepeatOrderContext context);
}
