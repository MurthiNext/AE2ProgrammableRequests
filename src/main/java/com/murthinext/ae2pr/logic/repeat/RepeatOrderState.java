package com.murthinext.ae2pr.logic.repeat;

/**
 * 重复订单状态机的状态。
 */
public enum RepeatOrderState {
    /** 本轮任务正在执行 */
    RUNNING,
    /** 本轮成功结束，等待开始下一轮 */
    PENDING_NEXT,
    /** 正在异步重算下一轮计划 */
    PLANNING,
    /** 本次尝试失败，等待重试 */
    RETRY_WAIT,
    /** 重试耗尽，订单失败 */
    FAILED,
    /** 全部 N 轮完成 */
    DONE,
    /** 被玩家取消/CPU 销毁 */
    CANCELLED
}
