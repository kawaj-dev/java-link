package com.javalink.model;

import java.util.List;
import java.util.Objects;

/**
 * 見出しと表示形式を持つ、初心者向け説明のまとまりです。
 */
public record CodeReadingExplanationSection(
        ExplanationSectionType sectionType,
        String title,
        List<CodeReadingExplanationEntry> entries
) {

    public CodeReadingExplanationSection {
        Objects.requireNonNull(sectionType, "sectionType must not be null");
        Objects.requireNonNull(title, "title must not be null");
        entries = List.copyOf(Objects.requireNonNull(entries, "entries must not be null"));
        if (entries.isEmpty()) {
            throw new IllegalArgumentException("entries must not be empty");
        }
    }
}
