package com.javalink.model;

import java.util.Objects;

/**
 * 教材画面の表示に必要な情報をまとめます。
 *
 * @param lesson          表示する教材
 * @param currentStep     現在学習しているステップ
 * @param progress        セッションに保存された学習進捗
 * @param completedCount  完了済み必須ステップ数
 * @param totalCount      全必須ステップ数
 * @param progressPercent 進捗率
 * @param codeComplete    全必須ステップが完了したか
 * @param runEnabled      プログラムを実行できるか
 * @param consoleVisible  コンソールを表示するか
 */
public record LessonViewModel(
        Lesson lesson,
        LessonStep currentStep,
        LessonProgress progress,
        int completedCount,
        int totalCount,
        int progressPercent,
        boolean codeComplete,
        boolean runEnabled,
        boolean consoleVisible
) {

    /**
     * ViewModelに不正な値が入らないよう、生成時に確認します。
     */
    public LessonViewModel {
        Objects.requireNonNull(lesson, "lesson must not be null");
        Objects.requireNonNull(currentStep, "currentStep must not be null");
        Objects.requireNonNull(progress, "progress must not be null");

        if (completedCount < 0 || totalCount < 0) {
            throw new IllegalArgumentException("step counts must not be negative");
        }
        if (completedCount > totalCount) {
            throw new IllegalArgumentException(
                    "completedCount must not be greater than totalCount"
            );
        }
        if (progressPercent < 0 || progressPercent > 100) {
            throw new IllegalArgumentException(
                    "progressPercent must be between 0 and 100"
            );
        }
    }
}
