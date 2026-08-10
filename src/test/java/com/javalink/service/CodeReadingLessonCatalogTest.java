package com.javalink.service;

import com.javalink.model.CodeReadingLessonDefinition;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodeReadingLessonCatalogTest {

    private final CodeReadingLessonCatalog catalog =
            new CodeReadingLessonCatalog();

    @Test
    void Stage1固有情報を一つの教材定義から取得できる() {
        CodeReadingLessonDefinition definition = catalog.getDefinition(
                LessonService.HELLO_PROGRAM_LESSON_ID
        );

        assertEquals("Stage 1", definition.stageName());
        assertEquals(4, definition.parts().size());
        assertEquals(4, definition.circuits().size());
        assertEquals(16, definition.steps().size());
        assertEquals("Hello", definition.consoleOutput());
        assertTrue(catalog.supports(LessonService.HELLO_PROGRAM_LESSON_ID));
    }

    @Test
    void Step定義に正解カードと正本の説明セクションを保持する() {
        var step = catalog.getDefinition(
                LessonService.HELLO_PROGRAM_LESSON_ID
        ).getStep("main-public");

        assertEquals("accessible", step.correctCard().id());
        assertEquals("外から使える", step.correctCard().text());
        assertEquals("アクセス修飾子", step.technicalTerm());
        assertEquals(
                java.util.List.of("text", "table", "text"),
                step.explanationSections().stream()
                        .map(section -> section.sectionType().value()).toList()
        );
        assertTrue(step.explanationSections().stream()
                .flatMap(section -> section.entries().stream())
                .anyMatch(entry -> entry.emphasis().equals("public")));
        assertEquals("main-public", step.toLessonStep().id());
    }

    @Test
    void 未登録教材を明確に判定できる() {
        assertFalse(catalog.supports("stage-2-reading"));
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> catalog.getDefinition("stage-2-reading")
        );
        assertTrue(exception.getMessage().contains("stage-2-reading"));
    }

    @Test
    void 教材定義に種類別Helperが揃い文字列layoutを受け取らない() {
        Set<String> helperNames = Arrays.stream(
                        CodeReadingLessonCatalog.class.getDeclaredMethods()
                )
                .map(method -> method.getName())
                .collect(Collectors.toSet());

        assertTrue(helperNames.containsAll(Set.of(
                "textSection", "tableSection", "diagramSection",
                "examplesSection", "qaSection", "comparisonSection", "listSection",
                "text", "tableRow", "highlightedTableRow", "diagramRow",
                "example", "note", "qaEntry", "comparisonEntry", "listItem"
        )));
        assertTrue(Arrays.stream(CodeReadingLessonCatalog.class.getDeclaredMethods())
                .filter(method -> method.getName().equals("section"))
                .noneMatch(method -> method.getParameterTypes()[0].equals(String.class)));
    }
}
