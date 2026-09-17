package com.murthinext.ae2pr.network;

/**
 * 单个合成 CPU 的重复订单展示数据。
 *
 * @param serial CPU 在当前菜单中的序号（与 AE2 {@code CraftingCpuListEntry.serial()} 对应）
 * @param round  当前轮次（1 起）
 * @param total  总轮数
 */
public record CpuRoundInfo(int serial, int round, int total) {
}
