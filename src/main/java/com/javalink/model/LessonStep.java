package com.javalink.model;

import java.util.Objects;

/**
 * 教材内の1つの学習ステップを表します。
 *
 * 1ステップを、コードの学習部分・問題・電球の1単位として扱います。
 *
 * @param id           ステップを区別するID
 * @param order        教材内の表示順。1から始めます
 * @param displayLabel 電球などに表示する名前
 * @param codeBefore   現在学習する部分より前のコード
 * @param targetCode   現在学習するコード
 * @param codeAfter    現在学習する部分より後のコード
 * @param question     このステップで出題する問題
 * @param required     Code Completeに必要なステップか
 */
public record LessonStep(
        String id,
        int order,
        String displayLabel,
        String codeBefore,
        String targetCode,
        String codeAfter,
        QuizQuestion question,
        boolean required
) {

    /**
     * 必須データと問題順を生成時に確認します。
     */
    public LessonStep {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(displayLabel, "displayLabel must not be null");
        Objects.requireNonNull(codeBefore, "codeBefore must not be null");
        Objects.requireNonNull(targetCode, "targetCode must not be null");
        Objects.requireNonNull(codeAfter, "codeAfter must not be null");
        Objects.requireNonNull(question, "question must not be null");

        if (order < 1) {
            throw new IllegalArgumentException("order must be 1 or greater");
        }
    }
}
