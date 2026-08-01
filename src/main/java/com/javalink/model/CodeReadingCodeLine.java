package com.javalink.model;

import java.util.List;
import java.util.Objects;

/** ViewModelからテンプレートへ渡す、汎用的なコード表示行です。 */
public record CodeReadingCodeLine(
        List<CodeReadingCodeToken> tokens,
        String trailingCode,
        String cssClass
) {
    public CodeReadingCodeLine {
        tokens = List.copyOf(Objects.requireNonNull(tokens, "tokens must not be null"));
        Objects.requireNonNull(trailingCode, "trailingCode must not be null");
        Objects.requireNonNull(cssClass, "cssClass must not be null");
    }
}
