package com.murthinext.ae2pr.repeat;

/**
 * Duck 接口：由 {@code CraftAmountMenu} 的 mixin 实现，用于在客户端/服务端菜单间同步重复轮数。
 */
public interface IRepeatRoundsHolder {
    int ae2pr$getRepeatRounds();

    void ae2pr$setRepeatRounds(int rounds);
}
