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
        List<CodeReadingExplanationSection> explanationSections,
        String nextStepId
) {

    public CodeReadingAnswerResponse {
        Objects.requireNonNull(
                answeredStepId,
                "answeredStepId must not be null"
        );
        Objects.requireNonNull(meaning, "meaning must not be null");
        Objects.requireNonNull(technicalTerm, "technicalTerm must not be null");
        explanationSections = List.copyOf(Objects.requireNonNull(
                explanationSections,
                "explanationSections must not be null"
        ));
    }
}
