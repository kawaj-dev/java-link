package com.javalink.model;

import java.util.List;
import java.util.Objects;

/**
 * 完成コードを意味のまとまりで学ぶ1つのPartを表します。
 */
public record CodeReadingPart(
        String id,
        int order,
        String title,
        String targetCode,
        List<String> stepIds,
        List<String> completionNotes,
        String reviewSummary
) {

    private static final int MAX_STEPS = 4;

    public CodeReadingPart {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(title, "title must not be null");
        Objects.requireNonNull(targetCode, "targetCode must not be null");
        Objects.requireNonNull(stepIds, "stepIds must not be null");
        Objects.requireNonNull(
                completionNotes,
                "completionNotes must not be null"
        );
        Objects.requireNonNull(
                reviewSummary,
                "reviewSummary must not be null"
        );
        stepIds = List.copyOf(stepIds);
        completionNotes = List.copyOf(completionNotes);

        if (order < 1) {
            throw new IllegalArgumentException("order must be 1 or greater");
        }
        if (stepIds.isEmpty()) {
            throw new IllegalArgumentException("stepIds must not be empty");
        }
        if (stepIds.size() > MAX_STEPS) {
            throw new IllegalArgumentException(
                    "1つのPartに登録できる項目は4つまでです。"
            );
        }
    }
}
