package com.javalink.model;

import java.util.Objects;

/** コード回路内の1つの学習stepを表します。 */
public record CodeReadingCircuitBulb(
        String stepId,
        String codeLabel,
        String optionId,
        String meaning,
        boolean completed,
        boolean current,
        boolean actionable,
        boolean explaining,
        boolean locked
) {
    public CodeReadingCircuitBulb {
        Objects.requireNonNull(stepId, "stepId must not be null");
        Objects.requireNonNull(codeLabel, "codeLabel must not be null");
        Objects.requireNonNull(optionId, "optionId must not be null");
        Objects.requireNonNull(meaning, "meaning must not be null");
    }
}
