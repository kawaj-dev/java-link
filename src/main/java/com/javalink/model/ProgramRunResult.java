package com.javalink.model;

import java.util.Objects;

/**
 * 教材プログラムの安全な疑似実行結果を画面へ渡します。
 *
 * @param lessonId     実行対象の教材ID
 * @param success      実行に成功したか
 * @param consoleOutput Consoleへ表示する固定出力
 * @param message      実行結果を説明するメッセージ
 */
public record ProgramRunResult(
        String lessonId,
        boolean success,
        String consoleOutput,
        String message
) {

    public ProgramRunResult {
        Objects.requireNonNull(lessonId, "lessonId must not be null");
        Objects.requireNonNull(consoleOutput, "consoleOutput must not be null");
        Objects.requireNonNull(message, "message must not be null");
    }
}
