package com.javalink.model;

import java.util.Objects;

/** コード欄に置くstepと、その前後の記号を定義します。 */
public record CodeReadingCodeTokenDefinition(
        String stepId,
        String prefix,
        String suffix
) {

    public CodeReadingCodeTokenDefinition {
        Objects.requireNonNull(stepId, "stepId must not be null");
        Objects.requireNonNull(prefix, "prefix must not be null");
        Objects.requireNonNull(suffix, "suffix must not be null");
    }

    public static CodeReadingCodeTokenDefinition step(String stepId) {
        return new CodeReadingCodeTokenDefinition(stepId, "", "");
    }
}
