package com.javalink.model;

import com.fasterxml.jackson.annotation.JsonValue;

/** 説明セクションが表す教材要素の種類です。 */
public enum ExplanationSectionType {
    TEXT("text"),
    TABLE("table"),
    DIAGRAM("diagram"),
    EXAMPLES("examples"),
    QA("qa"),
    COMPARISON("comparison"),
    LIST("list"),
    OFFICIAL_REFERENCES("official-references");

    private final String value;

    ExplanationSectionType(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
