package com.javalink.model;

import java.util.Objects;

/** コードへ結び付ける意味カードを定義します。 */
public record CodeReadingCardDefinition(String id, String text) {

    public CodeReadingCardDefinition {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(text, "text must not be null");
    }

    public QuizOption toQuizOption() {
        return new QuizOption(id, text);
    }
}
