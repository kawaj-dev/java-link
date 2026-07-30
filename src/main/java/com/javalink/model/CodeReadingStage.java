package com.javalink.model;

import java.util.List;
import java.util.Objects;

/**
 * 「コードを左から読む」教材の1ステージ分を表します。
 */
public record CodeReadingStage(
        String id,
        int order,
        String title,
        String targetCode,
        List<String> stepIds,
        String reviewSummary
) {

    public CodeReadingStage {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(title, "title must not be null");
        Objects.requireNonNull(targetCode, "targetCode must not be null");
        Objects.requireNonNull(stepIds, "stepIds must not be null");
        Objects.requireNonNull(reviewSummary, "reviewSummary must not be null");
        stepIds = List.copyOf(stepIds);

        if (order < 1) {
            throw new IllegalArgumentException("order must be 1 or greater");
        }
        if (stepIds.isEmpty()) {
            throw new IllegalArgumentException("stepIds must not be empty");
        }
    }
}
