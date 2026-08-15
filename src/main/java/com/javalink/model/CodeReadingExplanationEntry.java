package com.javalink.model;

import java.util.Objects;

/**
 * 説明セクション内の1行です。本文中の重要語だけを強調できます。
 */
public record CodeReadingExplanationEntry(
        String label,
        String before,
        String emphasis,
        String after,
        String detail,
        boolean tableHeader,
        boolean highlighted
) {

    public CodeReadingExplanationEntry(
            String label,
            String before,
            String emphasis,
            String after,
            String detail,
            boolean highlighted
    ) {
        this(label, before, emphasis, after, detail, false, highlighted);
    }

    public CodeReadingExplanationEntry(
            String label,
            String before,
            String emphasis,
            String after,
            boolean highlighted
    ) {
        this(label, before, emphasis, after, "", false, highlighted);
    }

    public CodeReadingExplanationEntry {
        Objects.requireNonNull(label, "label must not be null");
        Objects.requireNonNull(before, "before must not be null");
        Objects.requireNonNull(emphasis, "emphasis must not be null");
        Objects.requireNonNull(after, "after must not be null");
        Objects.requireNonNull(detail, "detail must not be null");
    }
}
