package com.javalink.model;

import java.util.Objects;

/** コード回路内の1つの学習stepを表します。 */
public record CodeReadingCircuitBulb(
        String stepId,
        String codeLabel,
        boolean completed,
        boolean current
) {
    public CodeReadingCircuitBulb {
        Objects.requireNonNull(stepId, "stepId must not be null");
        Objects.requireNonNull(codeLabel, "codeLabel must not be null");
    }
}
