package com.javalink.service;

import com.javalink.model.CodeReadingPart;
import com.javalink.model.CodeReadingCircuitGroup;
import com.javalink.model.LessonProgress;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodeReadingPartServiceTest {

    private final CodeReadingPartService service =
            new CodeReadingPartService();

    @Test
    void 四つのPartを学習順に返す() {
        List<CodeReadingPart> parts = service.getParts();

        assertEquals(4, parts.size());
        assertEquals(
                List.of(
                        "クラスを作る",
                        "mainメソッドを作る",
                        "「Hello」を表示する",
                        "ブロックの終わりを確認する"
                ),
                parts.stream().map(CodeReadingPart::title).toList()
        );
        assertEquals(
                List.of(4, 7, 3, 2),
                parts.stream().map(part -> part.stepIds().size()).toList()
        );
    }

    @Test
    void Part2は七項目で他Partは四項目以内に収まる() {
        assertEquals(7, service.getPart("part-2").stepIds().size());
        assertTrue(service.getParts().stream()
                .filter(part -> !part.id().equals("part-2"))
                .allMatch(part -> part.stepIds().size() <= 4));
    }

    @Test
    void 括弧とドットをPart完了時の説明で補足する() {
        CodeReadingPart part3 = service.getPart("part-3");

        assertTrue(part3.completionNotes().stream()
                .anyMatch(note -> note.contains("( )")));
        assertTrue(part3.completionNotes().stream()
                .anyMatch(note -> note.contains("画面へ表示して改行")));
        assertTrue(part3.completionNotes().stream()
                .anyMatch(note -> note.contains(". は")));
        assertEquals("(\"Hello\")", part3.displayTokenFor("hello-string"));
    }

    @Test
    void 各Partに初心者向けの導入説明がある() {
        assertTrue(service.getPart("part-1").introduction().stream()
                .anyMatch(line -> line.contains("自由に変更")));
        assertEquals(
                "public static void main(String[] args) は、Javaでプログラムを始めるための決まり文句です。",
                service.getPart("part-2").introduction().get(0)
        );
        assertTrue(service.getParts().stream()
                .allMatch(part -> !part.introduction().isEmpty()));
    }

    @Test
    void 現在Part用のコード回路を完了stepから復元できる() {
        LessonProgress progress = new LessonProgress("hello", "string-array");
        progress.completeStep("main-public");
        progress.completeStep("static");
        progress.completeStep("void");
        progress.completeStep("main");

        List<CodeReadingCircuitGroup> groups = service.createCircuitGroups(progress);

        assertEquals(4, groups.size());
        assertEquals(
                List.of(4, 7, 3, 2),
                groups.stream().map(group -> group.bulbs().size()).toList()
        );
        CodeReadingCircuitGroup mainMethod = groups.get(1);
        assertEquals("public static void main(String[] args) { }", mainMethod.codeLabel());
        assertEquals(4, mainMethod.bulbs().stream().filter(bulb -> bulb.completed()).count());
        assertTrue(mainMethod.current());
        assertTrue(mainMethod.bulbs().stream()
                .anyMatch(bulb -> bulb.stepId().equals("string-array") && bulb.current()));
        assertEquals(
                List.of("public", "static", "void", "main", "String[]", "args", "{ }"),
                mainMethod.bulbs().stream().map(bulb -> bulb.codeLabel()).toList()
        );
    }

    @Test
    void ステップから所属Partを取得できる() {
        assertEquals(
                "part-2",
                service.getPartForStep("string-array").id()
        );
        assertEquals(
                "part-4",
                service.getPartForStep("class-close").id()
        );
    }

    @Test
    void 最後のPartを判定できる() {
        assertFalse(service.isLastPart(service.getPart("part-1")));
        assertTrue(service.isLastPart(service.getPart("part-4")));
    }

    @Test
    void 存在しないPartでは分かりやすい例外になる() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.getPart("missing-part")
        );

        assertTrue(exception.getMessage().contains("missing-part"));
    }
}
