package com.javalink.model;

/**
 * 「コードを左から読む」教材で、現在表示する画面を表します。
 */
public enum CodeReadingPhase {
    /** 完成コードを見て、これから学ぶ内容を確認する画面です。 */
    INTRO,

    /** コードを意味のまとまりごとに学ぶ画面です。 */
    LEARNING,

    /** 学んだコードを意味のまとまりで読み直す画面です。 */
    SUMMARY
}
