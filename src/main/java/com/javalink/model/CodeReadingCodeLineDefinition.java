package com.javalink.model;

import java.util.List;
import java.util.Objects;

/** Partの濃紺コード欄に表示する1行を定義します。 */
public record CodeReadingCodeLineDefinition(
        List<CodeReadingCodeTokenDefinition> tokens,
        String trailingCode,
        String cssClass
) {

    public CodeReadingCodeLineDefinition {
        tokens = List.copyOf(Objects.requireNonNull(tokens, "tokens must not be null"));
        Objects.requireNonNull(trailingCode, "trailingCode must not be null");
        Objects.requireNonNull(cssClass, "cssClass must not be null");
        if (tokens.isEmpty()) {
            throw new IllegalArgumentException("tokens must not be empty");
        }
    }
}
