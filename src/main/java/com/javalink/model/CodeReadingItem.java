package com.javalink.model;

import java.util.List;
import java.util.Objects;

/**
 * コードを左から読む画面に表示する、Java用語と意味の組み合わせです。
 */
public record CodeReadingItem(
        String stepId,
        String keyword,
        String optionId,
        String meaning,
        String roleLabel,
        List<String> explanations,
        String technicalExplanation,
        List<CodeReadingExplanationSection> explanationSections,
        int order,
        boolean completed,
        boolean current
) {

    public CodeReadingItem {
        Objects.requireNonNull(stepId, "stepId must not be null");
        Objects.requireNonNull(keyword, "keyword must not be null");
        Objects.requireNonNull(optionId, "optionId must not be null");
        Objects.requireNonNull(meaning, "meaning must not be null");
        Objects.requireNonNull(roleLabel, "roleLabel must not be null");
        explanations = List.copyOf(explanations);
        Objects.requireNonNull(
                technicalExplanation,
                "technicalExplanation must not be null"
        );
        explanationSections = List.copyOf(Objects.requireNonNull(
                explanationSections,
                "explanationSections must not be null"
        ));
    }
}
