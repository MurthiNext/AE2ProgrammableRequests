package com.murthinext.ae2pr.block.level_emitter;

/**
 * 使用过滤元件时的多触发项组合模式。
 * <p>
 * AND：所有触发项都满足阈值条件才输出；OR：任一触发项满足即输出。
 */
public enum CombineMode {
    AND,
    OR
}
