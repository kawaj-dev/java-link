package com.javalink.service;

import com.javalink.model.CodeReadingPart;
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
    void 五つのPartを学習順に返す() {
        List<CodeReadingPart> parts = service.getParts();

        assertEquals(5, parts.size());
        assertEquals(
                List.of(
                        "クラスを作る",
                        "mainメソッドを作る",
                        "mainメソッドが受け取る情報",
                        "「Hello」を表示する",
                        "コードのまとまりを閉じる"
                ),
                parts.stream().map(CodeReadingPart::title).toList()
        );
        assertEquals(
                List.of(4, 4, 3, 3, 2),
                parts.stream().map(part -> part.stepIds().size()).toList()
        );
    }

    @Test
    void すべてのPartが最大四項目に収まる() {
        assertTrue(service.getParts().stream()
                .allMatch(part -> part.stepIds().size() <= 4));
    }

    @Test
    void 括弧とドットをPart完了時の説明で補足する() {
        CodeReadingPart part3 = service.getPart("part-3");
        CodeReadingPart part4 = service.getPart("part-4");

        assertTrue(part3.completionNotes().stream()
                .anyMatch(note -> note.contains("( )")));
        assertTrue(part3.completionNotes().stream()
                .anyMatch(note -> note.contains("mainメソッドの中身")));
        assertTrue(part4.completionNotes().stream()
                .anyMatch(note -> note.contains("画面へ表示して改行")));
        assertTrue(part4.completionNotes().stream()
                .anyMatch(note -> note.contains(". は")));
    }

    @Test
    void ステップから所属Partを取得できる() {
        assertEquals(
                "part-3",
                service.getPartForStep("string-array").id()
        );
        assertEquals(
                "part-5",
                service.getPartForStep("class-close").id()
        );
    }

    @Test
    void 最後のPartを判定できる() {
        assertFalse(service.isLastPart(service.getPart("part-1")));
        assertTrue(service.isLastPart(service.getPart("part-5")));
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
