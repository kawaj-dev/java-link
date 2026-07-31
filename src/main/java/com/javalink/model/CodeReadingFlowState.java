package com.javalink.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * 教材ごとの「コードを左から読む」画面フェーズを保持します。
 * 問題の進捗は既存のLessonProgressへ任せ、このクラスでは画面だけを管理します。
 */
public record CodeReadingFlowState(
        String lessonId,
        CodeReadingPhase phase
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public CodeReadingFlowState {
        Objects.requireNonNull(lessonId, "lessonId must not be null");
        Objects.requireNonNull(phase, "phase must not be null");
    }

    /**
     * 導入画面から始まる初期状態を作ります。
     */
    public static CodeReadingFlowState initial(String lessonId) {
        return new CodeReadingFlowState(lessonId, CodeReadingPhase.INTRO);
    }
}
