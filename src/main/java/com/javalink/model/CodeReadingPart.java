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
        List<String> introduction,
        String targetCode,
        List<String> stepIds,
        List<String> displayTokens,
        List<String> completionNotes,
        String reviewSummary
) {

    private static final int MAX_STEPS = 7;

    public CodeReadingPart {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(title, "title must not be null");
        Objects.requireNonNull(introduction, "introduction must not be null");
        Objects.requireNonNull(targetCode, "targetCode must not be null");
        Objects.requireNonNull(stepIds, "stepIds must not be null");
        Objects.requireNonNull(displayTokens, "displayTokens must not be null");
        Objects.requireNonNull(
                completionNotes,
                "completionNotes must not be null"
        );
        Objects.requireNonNull(
                reviewSummary,
                "reviewSummary must not be null"
        );
        introduction = List.copyOf(introduction);
        stepIds = List.copyOf(stepIds);
        displayTokens = List.copyOf(displayTokens);
        completionNotes = List.copyOf(completionNotes);

        if (order < 1) {
            throw new IllegalArgumentException("order must be 1 or greater");
        }
        if (stepIds.isEmpty()) {
            throw new IllegalArgumentException("stepIds must not be empty");
        }
        if (stepIds.size() > MAX_STEPS) {
            throw new IllegalArgumentException(
                    "1つのPartに登録できる項目は7つまでです。"
            );
        }
        if (displayTokens.size() != stepIds.size()) {
            throw new IllegalArgumentException(
                    "displayTokens must correspond to every stepId"
            );
        }
    }

    public String displayTokenFor(String stepId) {
        int index = stepIds.indexOf(stepId);
        if (index < 0) {
            throw new IllegalArgumentException(
                    "Partにステップがありません。stepId: " + stepId
            );
        }
        return displayTokens.get(index);
    }
}
