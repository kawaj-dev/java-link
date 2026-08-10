package com.javalink.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 1つの学習stepについて、コード、カード、初心者向け説明を定義します。
 * 現在の4択と、将来の正解カード1枚方式の両方で利用できます。
 */
public record CodeReadingStepDefinition(
        String id,
        int order,
        String displayLabel,
        String codeBefore,
        String targetCode,
        String codeAfter,
        String questionText,
        CodeReadingCardDefinition correctCard,
        List<CodeReadingCardDefinition> distractorCards,
        String technicalTerm,
        List<CodeReadingExplanationSection> explanationSections,
        boolean required
) {

    public CodeReadingStepDefinition {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(displayLabel, "displayLabel must not be null");
        Objects.requireNonNull(codeBefore, "codeBefore must not be null");
        Objects.requireNonNull(targetCode, "targetCode must not be null");
        Objects.requireNonNull(codeAfter, "codeAfter must not be null");
        Objects.requireNonNull(questionText, "questionText must not be null");
        Objects.requireNonNull(correctCard, "correctCard must not be null");
        distractorCards = List.copyOf(Objects.requireNonNull(
                distractorCards,
                "distractorCards must not be null"
        ));
        Objects.requireNonNull(technicalTerm, "technicalTerm must not be null");
        explanationSections = List.copyOf(Objects.requireNonNull(
                explanationSections,
                "explanationSections must not be null"
        ));
        if (order < 1) {
            throw new IllegalArgumentException("order must be 1 or greater");
        }
    }

    public LessonStep toLessonStep() {
        List<QuizOption> options = new ArrayList<>();
        options.add(correctCard.toQuizOption());
        distractorCards.stream()
                .map(CodeReadingCardDefinition::toQuizOption)
                .forEach(options::add);
        QuizQuestion question = new QuizQuestion(
                id,
                codeBefore,
                targetCode,
                codeAfter,
                questionText,
                options,
                correctCard.id()
        );
        return new LessonStep(
                id,
                order,
                displayLabel,
                codeBefore,
                targetCode,
                codeAfter,
                question,
                required
        );
    }
}
