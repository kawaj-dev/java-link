package com.javalink.model;

import com.fasterxml.jackson.annotation.JsonValue;

/** コード説明の根拠として参照する公式資料の種類です。 */
public enum CodeReadingOfficialSource {
    JLS("jls"),
    JAVA_SE_API("java-se-api");

    private final String value;

    CodeReadingOfficialSource(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }
}
