package com.javalink.model;

import java.util.List;
import java.util.Objects;

/**
 * コードリーディング教材に表示する、1つのコード回路を定義します。
 */
public record CodeReadingCircuitDefinition(
        String id,
        String codeLabel,
        List<String> stepIds,
        List<String> codeLabels
) {

    public CodeReadingCircuitDefinition {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(codeLabel, "codeLabel must not be null");
        stepIds = List.copyOf(Objects.requireNonNull(
                stepIds,
                "stepIds must not be null"
        ));
        codeLabels = List.copyOf(Objects.requireNonNull(
                codeLabels,
                "codeLabels must not be null"
        ));
        if (stepIds.isEmpty()) {
            throw new IllegalArgumentException("stepIds must not be empty");
        }
        if (stepIds.size() != codeLabels.size()) {
            throw new IllegalArgumentException(
                    "回路のstepIdとコード表示は同じ数が必要です。"
            );
        }
    }
}
