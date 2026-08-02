package com.javalink.model;

import java.util.List;
import java.util.Objects;

/**
 * サーバーで回答を判定した後、Part学習画面へ返す結果です。
 */
public record CodeReadingAnswerResponse(
        boolean correct,
        String answeredStepId,
        String meaning,
        int completedCount,
        boolean partCompleted,
        String technicalTerm,
        String technicalExplanation,
        List<String> beginnerExplanations,
        String nextStepId
) {

    public CodeReadingAnswerResponse {
        Objects.requireNonNull(
                answeredStepId,
                "answeredStepId must not be null"
        );
        Objects.requireNonNull(meaning, "meaning must not be null");
        Objects.requireNonNull(technicalTerm, "technicalTerm must not be null");
        Objects.requireNonNull(technicalExplanation, "technicalExplanation must not be null");
        beginnerExplanations = List.copyOf(beginnerExplanations);
    }
}
