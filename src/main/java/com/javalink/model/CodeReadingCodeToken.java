package com.javalink.model;

import java.util.Objects;

/** 濃紺コード欄へ描画するstepの表示状態です。 */
public record CodeReadingCodeToken(
        CodeReadingItem item,
        String prefix,
        String suffix
) {
    public CodeReadingCodeToken {
        Objects.requireNonNull(item, "item must not be null");
        Objects.requireNonNull(prefix, "prefix must not be null");
        Objects.requireNonNull(suffix, "suffix must not be null");
    }
}
