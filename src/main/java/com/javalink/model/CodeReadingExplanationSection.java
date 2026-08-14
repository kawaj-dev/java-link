package com.javalink.model;

import java.util.List;
import java.util.Objects;

/**
 * 見出しと表示形式を持つ、初心者向け説明のまとまりです。
 */
public record CodeReadingExplanationSection(
        ExplanationSectionType sectionType,
        String title,
        List<CodeReadingExplanationEntry> entries,
        List<CodeReadingOfficialReference> officialReferences,
        boolean tableHeader
) {

    public CodeReadingExplanationSection {
        Objects.requireNonNull(sectionType, "sectionType must not be null");
        Objects.requireNonNull(title, "title must not be null");
        entries = List.copyOf(Objects.requireNonNull(entries, "entries must not be null"));
        officialReferences = List.copyOf(Objects.requireNonNull(
                officialReferences,
                "officialReferences must not be null"
        ));
        if (sectionType == ExplanationSectionType.OFFICIAL_REFERENCES
                && officialReferences.isEmpty()) {
            throw new IllegalArgumentException("officialReferences must not be empty");
        }
        if (sectionType != ExplanationSectionType.OFFICIAL_REFERENCES
                && entries.isEmpty()) {
            throw new IllegalArgumentException("entries must not be empty");
        }
        if (sectionType == ExplanationSectionType.OFFICIAL_REFERENCES
                && !entries.isEmpty()) {
            throw new IllegalArgumentException("official reference sections must not have entries");
        }
        if (sectionType != ExplanationSectionType.OFFICIAL_REFERENCES
                && !officialReferences.isEmpty()) {
            throw new IllegalArgumentException("only official reference sections may have references");
        }
    }
}
