package com.javalink.model;

import java.util.List;
import java.util.Objects;

/**
 * Java Linkの教材全体を表します。
 *
 * @param id           教材を区別するID
 * @param title        教材のタイトル
 * @param description  教材の説明
 * @param completeCode 全ステップ完了時のJavaコード
 * @param steps        学習する順番に並べたステップ
 */
public record Lesson(
        String id,
        String title,
        String description,
        String completeCode,
        List<LessonStep> steps
) {

    /**
     * 教材データにnullが入らないようにし、ステップ一覧を変更できない形で保持します。
     */
    public Lesson {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(title, "title must not be null");
        Objects.requireNonNull(description, "description must not be null");
        Objects.requireNonNull(completeCode, "completeCode must not be null");
        steps = List.copyOf(Objects.requireNonNull(steps, "steps must not be null"));
    }
}
