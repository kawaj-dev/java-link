package com.javalink.model;

/**
 * 学習ステップと電球の状態を表します。
 */
public enum StepStatus {

    /**
     * 正解済みで、電球が点灯している状態です。
     */
    COMPLETED,

    /**
     * 現在学習している状態です。
     */
    CURRENT,

    /**
     * まだ学習しておらず、電球が消灯している状態です。
     */
    LOCKED
}
