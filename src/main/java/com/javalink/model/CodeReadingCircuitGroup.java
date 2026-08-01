package com.javalink.model;

import java.util.List;
import java.util.Objects;

/** 意味のあるJavaコードと、そのコードを構成する電球をまとめます。 */
public record CodeReadingCircuitGroup(
        String id,
        String codeLabel,
        List<CodeReadingCircuitBulb> bulbs,
        boolean current
) {
    public CodeReadingCircuitGroup {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(codeLabel, "codeLabel must not be null");
        bulbs = List.copyOf(bulbs);
    }
}
