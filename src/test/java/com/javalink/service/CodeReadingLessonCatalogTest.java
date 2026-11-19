package com.javalink.service;

import com.javalink.model.CodeReadingLessonDefinition;
import com.javalink.model.CodeReadingExplanationSection;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodeReadingLessonCatalogTest {

    private final CodeReadingLessonCatalog catalog =
            new CodeReadingLessonCatalog();

    @Test
    void Stage8は12Part55Stepと車のコードと実行結果を持つ() {
        var definition = catalog.getDefinition(
                CodeReadingLessonCatalog.STAGE8_LESSON_ID
        );

        assertEquals("Stage 8", definition.stageName());
        assertEquals("クラスから2台の車を作ってみよう", definition.learningGoal());
        assertEquals(12, definition.parts().size());
        assertEquals(55, definition.steps().size());
        assertEquals(java.util.List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12),
                definition.parts().stream().map(part -> part.order()).toList());
        assertEquals(definition.steps().size(), definition.steps().stream()
                .map(step -> step.id()).distinct().count());
        assertTrue(definition.parts().stream()
                .allMatch(part -> part.stepIds().size() <= 7));
        assertEquals("Car car1 = new Car();", definition.parts().get(2).targetCode());
        assertEquals("car1.showColor();\ncar2.showColor();",
                definition.parts().get(6).targetCode());
        assertEquals("class Car {", definition.parts().get(8).targetCode());
        assertEquals("red\nblue", definition.consoleOutput());
        assertEquals("""
                public class Main {
                    public static void main(String[] args) {
                        Car car1 = new Car();
                        Car car2 = new Car();

                        car1.color = "red";
                        car2.color = "blue";

                        car1.showColor();
                        car2.showColor();
                    }
                }

                class Car {
                    String color;

                    void showColor() {
                        System.out.println(color);
                    }
                }
                """.strip(), definition.completedCode().strip());
    }

    @Test
    void Stage8はクラスと参照変数と独立したインスタンスを区別して説明する() {
        var definition = catalog.getDefinition(
                CodeReadingLessonCatalog.STAGE8_LESSON_ID
        );

        assertTrue(allExplanationText(definition, "car1-name")
                .contains("インスタンスを参照する変数"));
        assertTrue(allExplanationText(definition, "car1-new")
                .contains("現実の自動車を製造するのではなく"));
        assertTrue(allExplanationText(definition, "car2-new")
                .contains("独立した2つのインスタンス"));
        assertTrue(allExplanationText(definition, "car1-color-field")
                .contains("互いに異なる値を保持"));
        assertTrue(allExplanationText(definition, "car1-show-color")
                .contains("呼び出し対象が異なる"));
        assertTrue(allExplanationText(definition, "print-color")
                .contains("対象インスタンスのcolorフィールド"));
    }

    @Test
    void Stage8は基礎構文と代入とString型の共通Explanationを再利用する() {
        var stage1 = catalog.getDefinition(CodeReadingLessonCatalog.STAGE1_LESSON_ID);
        var stage2 = catalog.getDefinition(CodeReadingLessonCatalog.STAGE2_LESSON_ID);
        var stage4 = catalog.getDefinition(CodeReadingLessonCatalog.STAGE4_LESSON_ID);
        var stage8 = catalog.getDefinition(CodeReadingLessonCatalog.STAGE8_LESSON_ID);

        assertSame(stage1.getStep("class-public").explanationSections(),
                stage8.getStep("class-public").explanationSections());
        assertSame(stage1.getStep("static").explanationSections(),
                stage8.getStep("main-static").explanationSections());
        assertSame(stage1.getStep("print-command").explanationSections(),
                stage8.getStep("print-command").explanationSections());
        assertSame(stage2.getStep("assignment").explanationSections(),
                stage8.getStep("car1-assignment").explanationSections());
        assertSame(stage4.getStep("signal-string-type").explanationSections(),
                stage8.getStep("color-string-type").explanationSections());
    }

    @Test
    void Stage7は9Part38Stepとメソッドコードと実行結果を持つ() {
        var definition = catalog.getDefinition(
                CodeReadingLessonCatalog.STAGE7_LESSON_ID
        );

        assertEquals("Stage 7", definition.stageName());
        assertEquals("メソッドを使って計算結果を表示しよう", definition.learningGoal());
        assertEquals(9, definition.parts().size());
        assertEquals(38, definition.steps().size());
        assertEquals(java.util.List.of(1, 2, 3, 4, 5, 6, 7, 8, 9),
                definition.parts().stream().map(part -> part.order()).toList());
        assertEquals(definition.steps().size(), definition.steps().stream()
                .map(step -> step.id()).distinct().count());
        assertTrue(definition.parts().stream()
                .allMatch(part -> part.stepIds().size() <= 7));
        assertEquals("int result = add(10, 20);", definition.parts().get(2).targetCode());
        assertEquals("public static int add(", definition.parts().get(5).targetCode());
        assertEquals("int a, int b) {", definition.parts().get(6).targetCode());
        assertEquals("return a + b;", definition.parts().get(7).targetCode());
        assertEquals("}\n}", definition.parts().get(8).targetCode());
        assertEquals("30", definition.consoleOutput());
        assertEquals("""
                public class Main {
                    public static void main(String[] args) {
                        int result = add(10, 20);
                        System.out.println(result);
                    }

                    public static int add(int a, int b) {
                        return a + b;
                    }
                }
                """.strip(), definition.completedCode().strip());
    }

    @Test
    void Stage7は呼び出しからreturnで戻る値の流れを正しく説明する() {
        var definition = catalog.getDefinition(
                CodeReadingLessonCatalog.STAGE7_LESSON_ID
        );

        assertTrue(allExplanationText(definition, "add-call")
                .contains("処理はいったんaddメソッドへ移ります"));
        assertTrue(allExplanationText(definition, "add-call")
                .contains("値30がresultへ代入"));
        assertTrue(allExplanationText(definition, "add-name")
                .contains("mainメソッドより後ろ"));
        assertTrue(allExplanationText(definition, "return-keyword")
                .contains("呼び出した位置へ戻ります"));
        assertTrue(allExplanationText(definition, "parameter-a")
                .contains("aが10"));
        assertTrue(allExplanationText(definition, "add-return-type")
                .contains("mainメソッドのvoid"));
    }

    @Test
    void Stage7は基礎構文と変数と加算の共通Explanationを再利用する() {
        var stage1 = catalog.getDefinition(CodeReadingLessonCatalog.STAGE1_LESSON_ID);
        var stage2 = catalog.getDefinition(CodeReadingLessonCatalog.STAGE2_LESSON_ID);
        var stage3 = catalog.getDefinition(CodeReadingLessonCatalog.STAGE3_LESSON_ID);
        var stage7 = catalog.getDefinition(CodeReadingLessonCatalog.STAGE7_LESSON_ID);

        assertSame(stage1.getStep("class-public").explanationSections(),
                stage7.getStep("class-public").explanationSections());
        assertSame(stage1.getStep("static").explanationSections(),
                stage7.getStep("add-static").explanationSections());
        assertSame(stage2.getStep("int-type").explanationSections(),
                stage7.getStep("result-int").explanationSections());
        assertSame(stage2.getStep("age-declaration").explanationSections(),
                stage7.getStep("result-name").explanationSections());
        assertSame(stage2.getStep("assignment").explanationSections(),
                stage7.getStep("result-assignment").explanationSections());
        assertSame(stage2.getStep("age-use").explanationSections(),
                stage7.getStep("result-use").explanationSections());
        assertSame(stage3.getStep("addition").explanationSections(),
                stage7.getStep("return-addition").explanationSections());
    }

    @Test
    void Stage6は7Part35Stepと配列コードと実行結果を持つ() {
        var definition = catalog.getDefinition(
                CodeReadingLessonCatalog.STAGE6_LESSON_ID
        );

        assertEquals("Stage 6", definition.stageName());
        assertEquals("配列を使って複数の数字を表示しよう", definition.learningGoal());
        assertEquals(7, definition.parts().size());
        assertEquals(35, definition.steps().size());
        assertEquals(java.util.List.of(1, 2, 3, 4, 5, 6, 7),
                definition.parts().stream().map(part -> part.order()).toList());
        assertEquals(definition.steps().size(), definition.steps().stream()
                .map(step -> step.id()).distinct().count());
        assertTrue(definition.parts().stream()
                .allMatch(part -> part.stepIds().size() <= 7));
        assertEquals("int[] scores = {80, 90, 100};", definition.parts().get(2).targetCode());
        assertEquals("for (int i = 0;", definition.parts().get(3).targetCode());
        assertEquals("i < scores.length; i++) {", definition.parts().get(4).targetCode());
        assertEquals("System.out.println(scores[i]);", definition.parts().get(5).targetCode());
        assertEquals("}\n}\n}", definition.parts().get(6).targetCode());
        assertEquals("80\n90\n100", definition.consoleOutput());
        assertEquals("""
                public class Main {
                    public static void main(String[] args) {
                        int[] scores = {80, 90, 100};

                        for (int i = 0; i < scores.length; i++) {
                            System.out.println(scores[i]);
                        }
                    }
                }
                """.strip(), definition.completedCode().strip());
    }

    @Test
    void Stage6は配列の添字と長さと要素取得を正しく説明する() {
        var definition = catalog.getDefinition(
                CodeReadingLessonCatalog.STAGE6_LESSON_ID
        );

        assertTrue(allExplanationText(definition, "scores-initializer")
                .contains("scores[0]80"));
        assertTrue(allExplanationText(definition, "scores-length")
                .contains("メソッドではない"));
        assertTrue(allExplanationText(definition, "less-than")
                .contains("ArrayIndexOutOfBoundsException"));
        assertTrue(allExplanationText(definition, "scores-element").contains("80"));
        assertTrue(allExplanationText(definition, "scores-element").contains("90"));
        assertTrue(allExplanationText(definition, "scores-element").contains("100"));
        assertFalse(definition.getStep("scores-name").correctCard().text().isBlank());
    }

    @Test
    void Stage6は基礎構文とfor文の共通Explanationインスタンスを再利用する() {
        var stage1 = catalog.getDefinition(CodeReadingLessonCatalog.STAGE1_LESSON_ID);
        var stage2 = catalog.getDefinition(CodeReadingLessonCatalog.STAGE2_LESSON_ID);
        var stage5 = catalog.getDefinition(CodeReadingLessonCatalog.STAGE5_LESSON_ID);
        var stage6 = catalog.getDefinition(CodeReadingLessonCatalog.STAGE6_LESSON_ID);

        assertSame(stage1.getStep("class-public").explanationSections(),
                stage6.getStep("class-public").explanationSections());
        assertSame(stage1.getStep("class-open").explanationSections(),
                stage6.getStep("for-open").explanationSections());
        assertSame(stage2.getStep("int-type").explanationSections(),
                stage6.getStep("loop-int").explanationSections());
        assertSame(stage2.getStep("assignment").explanationSections(),
                stage6.getStep("scores-assignment").explanationSections());
        assertSame(stage5.getStep("for-keyword").explanationSections(),
                stage6.getStep("for-keyword").explanationSections());
        assertSame(stage5.getStep("increment").explanationSections(),
                stage6.getStep("increment").explanationSections());
    }

    @Test
    void Stage5は6Part30Stepとfor文コードと実行結果を持つ() {
        var definition = catalog.getDefinition(
                CodeReadingLessonCatalog.STAGE5_LESSON_ID
        );

        assertEquals("Stage 5", definition.stageName());
        assertEquals("繰り返し処理を使って数字を表示しよう", definition.learningGoal());
        assertEquals(6, definition.parts().size());
        assertEquals(30, definition.steps().size());
        assertEquals(java.util.List.of(1, 2, 3, 4, 5, 6),
                definition.parts().stream().map(part -> part.order()).toList());
        assertEquals(definition.steps().size(), definition.steps().stream()
                .map(step -> step.id()).distinct().count());
        assertEquals("for (int i = 1;", definition.parts().get(2).targetCode());
        assertEquals("i <= 5; i++) {", definition.parts().get(3).targetCode());
        assertEquals("System.out.println(i);", definition.parts().get(4).targetCode());
        assertEquals("}\n}\n}", definition.parts().get(5).targetCode());
        assertEquals("1\n2\n3\n4\n5", definition.consoleOutput());
        assertEquals("""
                public class Main {
                    public static void main(String[] args) {

                        for (int i = 1; i <= 5; i++) {
                            System.out.println(i);
                        }

                    }
                }
                """.strip(), definition.completedCode().strip());
    }

    @Test
    void Stage5はfor文の実行順序と条件式と更新式を正しく説明する() {
        var definition = catalog.getDefinition(
                CodeReadingLessonCatalog.STAGE5_LESSON_ID
        );

        assertEquals("条件を満たす間、処理を繰り返す",
                definition.getStep("for-keyword").correctCard().text());
        assertEquals("左側が右側以下かを判定する",
                definition.getStep("less-than-or-equal").correctCard().text());
        assertEquals("変数iの値を1増やす",
                definition.getStep("increment").correctCard().text());
        assertTrue(allExplanationText(definition, "for-keyword")
                .contains("最初の1回だけ"));
        assertTrue(allExplanationText(definition, "less-than-or-equal")
                .contains("i < 5"));
        assertTrue(allExplanationText(definition, "increment")
                .contains("ブロック内の処理を実行した後"));
        assertTrue(allExplanationText(definition, "print-i")
                .contains("文字列の\"i\"ではなく"));
        assertTrue(allExplanationText(definition, "condition-i")
                .contains("6false表示しない"));
    }

    @Test
    void Stage5は既習の基礎構文Explanationを再利用する() {
        var stage1 = catalog.getDefinition(CodeReadingLessonCatalog.STAGE1_LESSON_ID);
        var stage2 = catalog.getDefinition(CodeReadingLessonCatalog.STAGE2_LESSON_ID);
        var stage5 = catalog.getDefinition(CodeReadingLessonCatalog.STAGE5_LESSON_ID);

        assertSame(stage1.getStep("class-public").explanationSections(),
                stage5.getStep("class-public").explanationSections());
        assertSame(stage1.getStep("static").explanationSections(),
                stage5.getStep("static").explanationSections());
        assertSame(stage1.getStep("class-open").explanationSections(),
                stage5.getStep("for-open").explanationSections());
        assertSame(stage1.getStep("print-command").explanationSections(),
                stage5.getStep("print-command").explanationSections());
        assertSame(stage1.getStep("semicolon").explanationSections(),
                stage5.getStep("print-semicolon").explanationSections());
        assertSame(stage2.getStep("int-type").explanationSections(),
                stage5.getStep("loop-int").explanationSections());
        assertSame(stage2.getStep("assignment").explanationSections(),
                stage5.getStep("initial-assignment").explanationSections());
    }

    @Test
    void Stage4は9Part33Stepと条件分岐コードと実行結果を持つ() {
        var definition = catalog.getDefinition(
                CodeReadingLessonCatalog.STAGE4_LESSON_ID
        );

        assertEquals("Stage 4", definition.stageName());
        assertEquals("信号を見て行動を決めよう", definition.learningGoal());
        assertEquals(9, definition.parts().size());
        assertEquals(33, definition.steps().size());
        assertEquals("進む", definition.consoleOutput());
        assertEquals(java.util.List.of(1, 2, 3, 4, 5, 6, 7, 8, 9),
                definition.parts().stream().map(part -> part.order()).toList());
        assertEquals(definition.steps().size(), definition.steps().stream()
                .map(step -> step.id()).distinct().count());
        assertEquals("public class Main {", definition.parts().get(0).targetCode());
        assertEquals("public static void main(String[] args) {", definition.parts().get(1).targetCode());
        assertEquals("String signal = \"青\";", definition.parts().get(2).targetCode());
        assertEquals("if (signal.equals(\"青\")) {", definition.parts().get(3).targetCode());
        assertEquals("System.out.println(\"進む\");", definition.parts().get(4).targetCode());
        assertEquals("} else {", definition.parts().get(5).targetCode());
        assertEquals("System.out.println(\"止まる\");", definition.parts().get(6).targetCode());
        assertEquals("}", definition.parts().get(7).targetCode());
        assertEquals("}\n}", definition.parts().get(8).targetCode());
        assertEquals("""
                public class Main {
                    public static void main(String[] args) {
                        String signal = "青";

                        if (signal.equals("青")) {
                            System.out.println("進む");
                        } else {
                            System.out.println("止まる");
                        }
                    }
                }
                """.strip(), definition.completedCode().strip());
    }

    @Test
    void Stage4はifとelseとequalsを初心者向けに正しく説明する() {
        var definition = catalog.getDefinition(
                CodeReadingLessonCatalog.STAGE4_LESSON_ID
        );

        assertEquals("条件がtrueのときだけ処理する",
                definition.getStep("if-keyword").correctCard().text());
        assertTrue(definition.getStep("if-keyword").explanationSections().stream()
                .flatMap(section -> section.entries().stream())
                .anyMatch(entry -> entry.before().contains("true")));
        assertEquals("条件がfalseのときに処理する",
                definition.getStep("else-keyword").correctCard().text());
        assertTrue(definition.getStep("else-keyword").explanationSections().stream()
                .flatMap(section -> section.entries().stream())
                .anyMatch(entry -> entry.before().contains("false")));
        assertEquals("文字列の内容が同じか比べる",
                definition.getStep("equals-method").correctCard().text());
        assertTrue(definition.getStep("equals-method").explanationSections().stream()
                .flatMap(section -> section.entries().stream())
                .anyMatch(entry -> entry.before().contains("== ではなく equals()")));
        assertTrue(definition.getStep("equals-method").explanationSections().stream()
                .flatMap(section -> section.officialReferences().stream())
                .anyMatch(reference -> reference.sectionTitle().equals("String.equals(Object)")));
    }

    @Test
    void Stage4は基礎構文の共通Explanationインスタンスを再利用する() {
        var stage1 = catalog.getDefinition(CodeReadingLessonCatalog.STAGE1_LESSON_ID);
        var stage4 = catalog.getDefinition(CodeReadingLessonCatalog.STAGE4_LESSON_ID);

        assertSame(stage1.getStep("class-public").explanationSections(),
                stage4.getStep("class-public").explanationSections());
        assertSame(stage1.getStep("main-public").explanationSections(),
                stage4.getStep("main-public").explanationSections());
        assertSame(stage1.getStep("class-keyword").explanationSections(),
                stage4.getStep("class-keyword").explanationSections());
        assertSame(stage1.getStep("class-name").explanationSections(),
                stage4.getStep("class-name").explanationSections());
        assertSame(stage1.getStep("static").explanationSections(),
                stage4.getStep("static").explanationSections());
        assertSame(stage1.getStep("void").explanationSections(),
                stage4.getStep("void").explanationSections());
        assertSame(stage1.getStep("main").explanationSections(),
                stage4.getStep("main").explanationSections());
        assertSame(stage1.getStep("string-array").explanationSections(),
                stage4.getStep("string-array").explanationSections());
        assertSame(stage1.getStep("args").explanationSections(),
                stage4.getStep("args").explanationSections());

        var blockStart = stage1.getStep("class-open").explanationSections();
        assertSame(blockStart, stage4.getStep("class-open").explanationSections());
        assertSame(blockStart, stage4.getStep("main-open").explanationSections());
        assertSame(blockStart, stage4.getStep("if-open").explanationSections());
        assertSame(blockStart, stage4.getStep("else-open").explanationSections());

        assertSame(stage1.getStep("print-command").explanationSections(),
                stage4.getStep("true-print-command").explanationSections());
        assertSame(stage4.getStep("true-print-command").explanationSections(),
                stage4.getStep("false-print-command").explanationSections());

        assertSame(stage1.getStep("semicolon").explanationSections(),
                stage4.getStep("signal-semicolon").explanationSections());
        assertSame(stage4.getStep("signal-semicolon").explanationSections(),
                stage4.getStep("true-semicolon").explanationSections());
        assertSame(stage4.getStep("true-semicolon").explanationSections(),
                stage4.getStep("false-semicolon").explanationSections());

        var blockEnd = stage1.getStep("main-close").explanationSections();
        assertSame(blockEnd, stage4.getStep("if-close").explanationSections());
        assertSame(blockEnd, stage4.getStep("else-close").explanationSections());
        assertSame(blockEnd, stage4.getStep("main-close").explanationSections());
        assertSame(blockEnd, stage4.getStep("class-close").explanationSections());
    }

    @Test
    void Stage3からStage8まで順に遷移しStage8の次は存在しない() {
        assertEquals(CodeReadingLessonCatalog.STAGE4_LESSON_ID,
                catalog.getNextDefinition(CodeReadingLessonCatalog.STAGE3_LESSON_ID)
                        .orElseThrow().lessonId());
        assertEquals(CodeReadingLessonCatalog.STAGE5_LESSON_ID,
                catalog.getNextDefinition(CodeReadingLessonCatalog.STAGE4_LESSON_ID)
                        .orElseThrow().lessonId());
        assertEquals(CodeReadingLessonCatalog.STAGE6_LESSON_ID,
                catalog.getNextDefinition(CodeReadingLessonCatalog.STAGE5_LESSON_ID)
                        .orElseThrow().lessonId());
        assertEquals(CodeReadingLessonCatalog.STAGE7_LESSON_ID,
                catalog.getNextDefinition(CodeReadingLessonCatalog.STAGE6_LESSON_ID)
                        .orElseThrow().lessonId());
        assertEquals(CodeReadingLessonCatalog.STAGE8_LESSON_ID,
                catalog.getNextDefinition(CodeReadingLessonCatalog.STAGE7_LESSON_ID)
                        .orElseThrow().lessonId());
        assertTrue(catalog.getNextDefinition(
                CodeReadingLessonCatalog.STAGE8_LESSON_ID
        ).isEmpty());
    }

    @Test
    void Stage3は7Part33Stepと実行結果30を持つ() {
        var definition = catalog.getDefinition(
                CodeReadingLessonCatalog.STAGE3_LESSON_ID
        );

        assertEquals("Stage 3", definition.stageName());
        assertEquals("演算子を使って計算しよう", definition.learningGoal());
        assertEquals(7, definition.parts().size());
        assertEquals(33, definition.steps().size());
        var closingPart = definition.parts().get(6);
        assertEquals("part-7", closingPart.id());
        assertEquals("プログラムを閉じる", closingPart.title());
        assertEquals("}\n}", closingPart.targetCode());
        assertEquals(java.util.List.of("main-close", "class-close"), closingPart.stepIds());
        assertEquals(java.util.List.of("}", "}"), closingPart.displayTokens());
        assertEquals(java.util.List.of("main-close", "class-close"),
                definition.circuits().get(6).stepIds());
        assertEquals("30", definition.consoleOutput());
        assertEquals("""
                public class Main {
                    public static void main(String[] args) {
                        int a = 10;
                        int b = 20;
                        int c = a + b;
                        System.out.println(c);
                    }
                }
                """.strip(), definition.completedCode().strip());
    }

    @Test
    void 全Stageは共通用語の説明Sectionインスタンスを再利用する() {
        var stage1 = catalog.getDefinition(CodeReadingLessonCatalog.STAGE1_LESSON_ID);
        var stage2 = catalog.getDefinition(CodeReadingLessonCatalog.STAGE2_LESSON_ID);
        var stage3 = catalog.getDefinition(CodeReadingLessonCatalog.STAGE3_LESSON_ID);

        var publicSections = stage1.getStep("class-public").explanationSections();
        assertSame(publicSections, stage1.getStep("main-public").explanationSections());
        assertSame(publicSections, stage2.getStep("class-public").explanationSections());
        assertSame(publicSections, stage2.getStep("main-public").explanationSections());
        assertSame(publicSections, stage3.getStep("class-public").explanationSections());
        assertSame(publicSections, stage3.getStep("main-public").explanationSections());

        assertSame(stage1.getStep("class-keyword").explanationSections(),
                stage2.getStep("class-keyword").explanationSections());
        assertSame(stage1.getStep("class-keyword").explanationSections(),
                stage3.getStep("class-keyword").explanationSections());
        assertSame(stage1.getStep("class-name").explanationSections(),
                stage2.getStep("class-name").explanationSections());
        assertSame(stage1.getStep("class-name").explanationSections(),
                stage3.getStep("class-name").explanationSections());

        var blockStartSections = stage1.getStep("class-open").explanationSections();
        assertSame(blockStartSections, stage1.getStep("main-open").explanationSections());
        assertSame(blockStartSections, stage2.getStep("class-open").explanationSections());
        assertSame(blockStartSections, stage2.getStep("main-open").explanationSections());
        assertSame(blockStartSections, stage3.getStep("class-open").explanationSections());
        assertSame(blockStartSections, stage3.getStep("main-open").explanationSections());

        assertSame(stage1.getStep("static").explanationSections(),
                stage2.getStep("static").explanationSections());
        assertSame(stage1.getStep("static").explanationSections(),
                stage3.getStep("static").explanationSections());
        assertSame(stage1.getStep("void").explanationSections(),
                stage2.getStep("void").explanationSections());
        assertSame(stage1.getStep("void").explanationSections(),
                stage3.getStep("void").explanationSections());
        assertSame(stage1.getStep("main").explanationSections(),
                stage2.getStep("main").explanationSections());
        assertSame(stage1.getStep("main").explanationSections(),
                stage3.getStep("main").explanationSections());
        assertSame(stage1.getStep("string-array").explanationSections(),
                stage2.getStep("string-array").explanationSections());
        assertSame(stage1.getStep("string-array").explanationSections(),
                stage3.getStep("string-array").explanationSections());
        assertSame(stage1.getStep("args").explanationSections(),
                stage2.getStep("args").explanationSections());
        assertSame(stage1.getStep("args").explanationSections(),
                stage3.getStep("args").explanationSections());
        assertSame(stage2.getStep("int-type").explanationSections(),
                stage3.getStep("a-int").explanationSections());
        assertSame(stage3.getStep("a-int").explanationSections(),
                stage3.getStep("b-int").explanationSections());
        assertSame(stage3.getStep("b-int").explanationSections(),
                stage3.getStep("c-int").explanationSections());
        assertSame(stage2.getStep("age-declaration").explanationSections(),
                stage3.getStep("a-name").explanationSections());
        assertSame(stage3.getStep("a-name").explanationSections(),
                stage3.getStep("b-name").explanationSections());
        assertSame(stage3.getStep("b-name").explanationSections(),
                stage3.getStep("c-name").explanationSections());
        assertSame(stage2.getStep("assignment").explanationSections(),
                stage3.getStep("a-assignment").explanationSections());
        assertSame(stage3.getStep("a-assignment").explanationSections(),
                stage3.getStep("b-assignment").explanationSections());
        assertSame(stage3.getStep("b-assignment").explanationSections(),
                stage3.getStep("c-assignment").explanationSections());
        assertSame(stage2.getStep("integer-literal").explanationSections(),
                stage3.getStep("a-literal").explanationSections());
        assertSame(stage3.getStep("a-literal").explanationSections(),
                stage3.getStep("b-literal").explanationSections());
        assertSame(stage2.getStep("age-use").explanationSections(),
                stage3.getStep("a-use").explanationSections());
        assertSame(stage3.getStep("a-use").explanationSections(),
                stage3.getStep("b-use").explanationSections());
        assertSame(stage2.getStep("age-use").explanationSections(),
                stage3.getStep("c-use").explanationSections());
        assertSame(stage3.getStep("a-use").explanationSections(),
                stage3.getStep("c-use").explanationSections());
        assertEquals(java.util.List.of(
                        "変数の値を使う",
                        "変数の値を使う",
                        "変数の値を使う",
                        "変数の値を使う"),
                java.util.List.of(
                        stage2.getStep("age-use").correctCard().text(),
                        stage3.getStep("a-use").correctCard().text(),
                        stage3.getStep("b-use").correctCard().text(),
                        stage3.getStep("c-use").correctCard().text()));
        assertTrue(stage3.getStep("c-use").explanationSections().stream()
                .noneMatch(section -> "計算結果を表示する".equals(section.title())));
        assertSame(stage1.getStep("print-command").explanationSections(),
                stage2.getStep("print-command").explanationSections());
        assertSame(stage2.getStep("print-command").explanationSections(),
                stage3.getStep("print-command").explanationSections());
        assertEquals(stage2.getStep("print-command").correctCard().text(),
                stage3.getStep("print-command").correctCard().text());
        assertEquals("画面に表示して改行する",
                stage3.getStep("print-command").correctCard().text());
        assertSame(stage1.getStep("semicolon").explanationSections(),
                stage3.getStep("a-semicolon").explanationSections());
        assertSame(stage3.getStep("a-semicolon").explanationSections(),
                stage3.getStep("b-semicolon").explanationSections());
        assertSame(stage3.getStep("b-semicolon").explanationSections(),
                stage3.getStep("c-semicolon").explanationSections());
        assertSame(stage3.getStep("c-semicolon").explanationSections(),
                stage3.getStep("print-semicolon").explanationSections());
        assertSame(stage1.getStep("main-close").explanationSections(),
                stage1.getStep("class-close").explanationSections());
        assertSame(stage1.getStep("main-close").explanationSections(),
                stage2.getStep("main-close").explanationSections());
        assertSame(stage2.getStep("main-close").explanationSections(),
                stage2.getStep("class-close").explanationSections());
        assertSame(stage2.getStep("main-close").explanationSections(),
                stage3.getStep("main-close").explanationSections());
        assertSame(stage3.getStep("main-close").explanationSections(),
                stage3.getStep("class-close").explanationSections());
        assertEquals("ここで終わる", stage3.getStep("main-close").correctCard().text());
        assertEquals("ここで終わる", stage3.getStep("class-close").correctCard().text());
    }

    @Test
    void 算術演算子説明は2つの明示的な4列表と公式根拠を持つ() {
        var addition = catalog.getDefinition(CodeReadingLessonCatalog.STAGE3_LESSON_ID)
                .getStep("addition");
        var sections = addition.explanationSections();
        var plusTable = sections.get(1);
        var table = sections.get(2);

        assertEquals("左右の数値を足す", addition.correctCard().text());
        assertEquals(5, sections.size());
        assertEquals("算術演算子　arithmetic operator", sections.get(0).title());
        assertEquals(java.util.List.of("数値を使って計算するときに使う記号です。"),
                sections.get(0).entries().stream().map(entry -> entry.before()).toList());
        assertEquals("+ 演算子の2つの働き（数値の加算と文字列の結合）", plusTable.title());
        assertTrue(plusTable.tableHeader());
        assertEquals(java.util.List.of(
                        java.util.List.of("働き", "コード例", "結果", "ルール"),
                        java.util.List.of("数値の加算", "10 + 20", "30", "両方が数値なら、足し算をします。"),
                        java.util.List.of("文字列の結合", "\"10\" + \"20\"", "\"1020\"",
                                "両方が文字列なら、文字をそのままつなぎます。"),
                        java.util.List.of("文字列の結合", "10 + \"20\"", "\"1020\"",
                                "片方が String なら、もう片方も文字列に変換してつなぎます。")),
                plusTable.entries().stream().map(entry -> java.util.List.of(
                        entry.label(), entry.before(), entry.emphasis(), entry.after())).toList());
        assertTrue(sections.stream().noneMatch(section -> "+ の2つの働き".equals(section.title())));
        assertEquals("代表的な算術演算子", table.title());
        assertTrue(table.tableHeader());
        assertEquals(java.util.List.of(
                        java.util.List.of("演算子", "機能", "計算例", "結果"),
                        java.util.List.of("+", "加算演算子（足し算）", "10 + 3", "13"),
                        java.util.List.of("-", "減算演算子（引き算）", "10 - 3", "7"),
                        java.util.List.of("*", "乗算演算子（掛け算）", "10 * 3", "30"),
                        java.util.List.of("/", "除算演算子（割り算）", "10 / 3", "3"),
                        java.util.List.of("%", "剰余演算子（割り算の余り）", "10 % 3", "1")),
                table.entries().stream().map(entry -> java.util.List.of(
                        entry.label(), entry.before(), entry.emphasis(), entry.after())).toList());
        assertEquals("int同士の割り算では、小数部分は切り捨てられ、結果は0に近い方の整数になります。",
                sections.get(3).entries().get(0).before());
        assertEquals("10 / 3 → 3", sections.get(3).entries().get(1).before());
        assertTrue(sections.stream().noneMatch(section -> "a + b の計算".equals(section.title())));
        assertEquals("official-references", sections.get(4).sectionType().value());
        assertEquals(java.util.List.of("§15.18", "§15.18.1", "§15.18.2", "§15.17", "§15.17.2", "§15.17.3"),
                sections.get(4).officialReferences().stream().map(reference -> reference.sectionNumber()).toList());
        var remainder = sections.get(4).officialReferences().get(5);
        assertEquals("Remainder Operator %", remainder.sectionTitle());
        assertEquals("https://docs.oracle.com/javase/specs/jls/se21/html/jls-15.html#jls-15.17.3",
                remainder.uri().toString());
    }

    @Test
    void Stage2は5Part21Stepと実行結果20を持つ() {
        var definition = catalog.getDefinition(CodeReadingLessonCatalog.STAGE2_LESSON_ID);

        assertEquals("Stage 2", definition.stageName());
        assertEquals("変数を使って年齢を表示しよう", definition.learningGoal());
        assertEquals(5, definition.parts().size());
        assertEquals(21, definition.steps().size());
        assertEquals("20", definition.consoleOutput());
        assertTrue(definition.completedCode().contains("int age = 20;"));
        assertTrue(definition.completedCode().contains("System.out.println(age);"));
    }

    @Test
    void Stage2はStage1説明を再利用し新規stepを役割別に持つ() {
        var stage1 = catalog.getDefinition(CodeReadingLessonCatalog.STAGE1_LESSON_ID);
        var stage2 = catalog.getDefinition(CodeReadingLessonCatalog.STAGE2_LESSON_ID);

        assertEquals(stage1.getStep("class-public").explanationSections(),
                stage2.getStep("class-public").explanationSections());
        assertSame(stage1.getStep("semicolon").explanationSections(),
                stage2.getStep("declaration-semicolon").explanationSections());
        assertEquals("変数名", stage2.getStep("age-declaration").technicalTerm());
        assertEquals("変数の利用", stage2.getStep("age-use").technicalTerm());
    }

    @Test
    void Stage2とStage3のintは同じ明示ヘッダー付き3列表を共有する() {
        var stage2 = catalog.getDefinition(CodeReadingLessonCatalog.STAGE2_LESSON_ID);
        var stage3 = catalog.getDefinition(CodeReadingLessonCatalog.STAGE3_LESSON_ID);
        var sections = stage2.getStep("int-type").explanationSections();
        var table = sections.get(2);

        assertSame(sections, stage3.getStep("a-int").explanationSections());
        assertSame(sections, stage3.getStep("b-int").explanationSections());
        assertSame(sections, stage3.getStep("c-int").explanationSections());
        assertSame(table, stage3.getStep("a-int").explanationSections().get(2));

        assertEquals("table", table.sectionType().value());
        assertTrue(table.tableHeader());
        assertEquals(10, table.entries().size());
        assertEquals(java.util.List.of(
                        java.util.List.of("分類", "型名", "格納するデータ"),
                        java.util.List.of("整数", "byte", "とても小さな整数"),
                        java.util.List.of("整数", "short", "小さな整数"),
                        java.util.List.of("整数", "int", "一般的な整数"),
                        java.util.List.of("整数", "long", "大きな整数"),
                        java.util.List.of("小数", "float", "精度の低い小数"),
                        java.util.List.of("小数", "double", "精度の高い小数"),
                        java.util.List.of("文字", "char", "1つの文字"),
                        java.util.List.of("真偽", "boolean", "true または false"),
                        java.util.List.of("文字列", "String", "文字の並び")),
                table.entries().stream()
                        .map(entry -> java.util.List.of(entry.label(), entry.before(), entry.emphasis()))
                        .toList());
        assertTrue(table.entries().stream()
                .allMatch(entry -> entry.after().isEmpty() && entry.detail().isEmpty()));
        assertTrue(table.entries().stream().noneMatch(entry -> entry.highlighted()));
    }

    @Test
    void 全Stageのセミコロンは同じ罫線付き3列表を共有する() {
        var stage1 = catalog.getDefinition(CodeReadingLessonCatalog.STAGE1_LESSON_ID);
        var stage2 = catalog.getDefinition(CodeReadingLessonCatalog.STAGE2_LESSON_ID);
        var stage1Semicolon = stage1.getStep("semicolon").explanationSections();
        var declaration = stage2.getStep("declaration-semicolon").explanationSections();
        var print = stage2.getStep("print-semicolon").explanationSections();
        var table = declaration.get(1);

        assertSame(stage1Semicolon, declaration);
        assertSame(declaration, print);
        assertEquals("table", table.sectionType().value());
        assertEquals("; が必要な場合と必要でない場合", table.title());
        assertEquals(java.util.List.of(
                        java.util.List.of("種類", "例", ";"),
                        java.util.List.of("式文・宣言文", "println(...); / int x = 1;", "必要"),
                        java.util.List.of("return・break など", "return; / break;", "必要"),
                        java.util.List.of("ブロック", "{ ... }", "不要"),
                        java.util.List.of("クラス・メソッドの本体", "class Main { ... }", "不要")),
                table.entries().stream()
                        .map(entry -> java.util.List.of(entry.label(), entry.before(), entry.emphasis()))
                        .toList());
        assertTrue(table.entries().stream().noneMatch(entry -> entry.highlighted()));
    }

    @Test
    void Stage2の新規説明は確定文言と登録順を維持する() {
        var stage2 = catalog.getDefinition(CodeReadingLessonCatalog.STAGE2_LESSON_ID);

        var intSections = stage2.getStep("int-type").explanationSections();
        assertEquals(java.util.List.of(
                        "整数型　integral type", "型（データ型）とは", "よく使う基本のデータ型", "",
                        "このページの技術的根拠：Javaの公式仕様・API"),
                intSections.stream().map(section -> section.title()).toList());
        assertEquals("int は、小数点のない整数を扱うためのデータ型です。",
                sectionText(intSections.get(0)));
        assertEquals("型（データ型）は、扱うデータの種類を決めるものです。"
                        + "型は、「この箱（変数）にはどんな種類のものを入れるのか」を決める表示のようなものです。",
                sectionText(intSections.get(1)));

        var declarationSections = stage2.getStep("age-declaration").explanationSections();
        assertEquals("変数につけた名前です。", sectionText(declarationSections.get(0)));
        assertTrue(sectionText(declarationSections.get(1)).contains("あとから別の値に変えることができます。"));
        assertEquals(java.util.List.of("", "値を入れておく箱", ""),
                declarationSections.get(1).entries().stream().map(entry -> entry.emphasis()).toList());
        assertEquals("📦 「", declarationSections.get(1).entries().get(1).before());
        assertEquals("」と考えるとイメージしやすくなります。",
                declarationSections.get(1).entries().get(1).after());
        assertEquals("text", declarationSections.get(2).sectionType().value());
        assertFalse(declarationSections.get(2).tableHeader());
        assertEquals("ポイント", declarationSections.get(2).title());
        assertEquals(java.util.List.of(
                        "1. 基本的に自由",
                        "2. 予約語は使えない",
                        "3. 小文字から始めるのが慣習",
                        "4. 複数の単語は2語目から大文字"),
                declarationSections.get(2).entries().stream().map(entry -> entry.label()).toList());
        assertEquals(java.util.List.of(
                        "変数名は自分で決められます。",
                        "int や class などのJavaの予約語（keyword）は、変数名として使えません。",
                        "Javaでは、変数名を小文字から始めるのが一般的です。",
                        "複数の単語をつなげる場合は、2つ目以降の単語の先頭を大文字にします。\n例：myAge"),
                declarationSections.get(2).entries().stream()
                        .map(entry -> entry.before() + entry.emphasis() + entry.after())
                        .toList());
        assertEquals(java.util.List.of("", "int や class", "", "myAge"),
                declarationSections.get(2).entries().stream().map(entry -> entry.emphasis()).toList());

        var assignmentSections = stage2.getStep("assignment").explanationSections();
        assertEquals("変数を宣言するときに最初の値を入れることを「初期化」といいます。",
                assignmentSections.get(1).entries().get(0).before());
        assertEquals("初期化と代入では、どちらも = が使われます。",
                assignmentSections.get(1).entries().get(1).before());
        assertEquals(java.util.List.of("種類", "コード", "意味"),
                java.util.List.of(
                        assignmentSections.get(1).entries().get(2).label(),
                        assignmentSections.get(1).entries().get(2).before(),
                        assignmentSections.get(1).entries().get(2).emphasis()));
        assertTrue(assignmentSections.get(1).entries().get(2).tableHeader());
        assertTrue(assignmentSections.get(1).entries().stream()
                .filter(entry -> !entry.tableHeader())
                .noneMatch(entry -> entry.label().equals("初期化") && entry.highlighted()));
        assertEquals("新しい変数を宣言するときに、最初の値を入れる",
                assignmentSections.get(1).entries().get(3).emphasis());
        assertEquals("すでにある変数の値を、新しい値に変える",
                assignmentSections.get(1).entries().get(4).emphasis());

        var literalSections = stage2.getStep("integer-literal").explanationSections();
        assertEquals("リテラルとは、プログラムの中に値を直接書いたものです。",
                literalSections.get(1).entries().get(0).before());
        assertEquals(java.util.List.of("書き方", "種類"),
                java.util.List.of(
                        literalSections.get(1).entries().get(1).label(),
                        literalSections.get(1).entries().get(1).before()));
        assertTrue(literalSections.get(1).entries().get(1).tableHeader());
        assertTrue(literalSections.get(2).entries().get(1).tableHeader());
        assertEquals(java.util.List.of("20", "整数リテラル"),
                java.util.List.of(
                        literalSections.get(1).entries().get(2).label(),
                        literalSections.get(1).entries().get(2).before()));
        assertEquals(java.util.List.of("\"Hello\"", "文字列リテラル"),
                java.util.List.of(
                        literalSections.get(1).entries().get(3).label(),
                        literalSections.get(1).entries().get(3).before()));
        assertEquals("書き方によって、Javaが扱うデータの種類が変わります。",
                literalSections.get(2).entries().get(0).before());
        assertEquals(java.util.List.of("書き方", "データの種類"),
                java.util.List.of(
                        literalSections.get(2).entries().get(1).label(),
                        literalSections.get(2).entries().get(1).before()));
        assertEquals(java.util.List.of("20", "整数"),
                java.util.List.of(
                        literalSections.get(2).entries().get(2).label(),
                        literalSections.get(2).entries().get(2).before()));
        assertEquals(java.util.List.of("\"20\"", "文字列"),
                java.util.List.of(
                        literalSections.get(2).entries().get(3).label(),
                        literalSections.get(2).entries().get(3).before()));

        var useSections = stage2.getStep("age-use").explanationSections();
        assertEquals("変数を「宣言する」と「使う」の違い", useSections.get(1).title());
        assertEquals("table", useSections.get(1).sectionType().value());
        assertEquals(java.util.List.of("種類", "コード", "意味"),
                java.util.List.of(
                        useSections.get(1).entries().get(0).label(),
                        useSections.get(1).entries().get(0).before(),
                        useSections.get(1).entries().get(0).emphasis()));
        assertTrue(useSections.get(1).entries().get(0).tableHeader());
        assertEquals(java.util.List.of("宣言する", "int age = 20;", "変数を宣言して、最初の値を保存する"),
                java.util.List.of(
                        useSections.get(1).entries().get(1).label(),
                        useSections.get(1).entries().get(1).before(),
                        useSections.get(1).entries().get(1).emphasis()));
        assertEquals(java.util.List.of("使う", "System.out.println(age);", "変数に保存されている値を使う"),
                java.util.List.of(
                        useSections.get(1).entries().get(2).label(),
                        useSections.get(1).entries().get(2).before(),
                        useSections.get(1).entries().get(2).emphasis()));
        assertEquals("table", useSections.get(2).sectionType().value());
        assertEquals("値を直接使う場合と、変数を使う場合", useSections.get(2).title());
        assertEquals(java.util.List.of("書き方", "値の使い方"),
                java.util.List.of(
                        useSections.get(2).entries().get(0).label(),
                        useSections.get(2).entries().get(0).before()));
        assertTrue(useSections.get(2).entries().get(0).tableHeader());
        assertEquals(java.util.List.of("20", "値を直接書いて使う"),
                java.util.List.of(
                        useSections.get(2).entries().get(1).label(),
                        useSections.get(2).entries().get(1).before()));
        assertEquals(java.util.List.of("age", "変数に保存されている値を使う"),
                java.util.List.of(
                        useSections.get(2).entries().get(2).label(),
                        useSections.get(2).entries().get(2).before()));
        assertEquals("変数名を書いた場所では、その変数に保存されている値が使われます。",
                useSections.get(2).entries().get(3).before());
        assertTrue(useSections.stream().noneMatch(section -> section.title().equals("ポイント")));
        assertTrue(useSections.stream().noneMatch(section -> section.title().equals("計算で使う変数")));
    }

    private static String sectionText(CodeReadingExplanationSection section) {
        return section.entries().stream()
                .map(entry -> entry.before() + entry.emphasis() + entry.after())
                .collect(java.util.stream.Collectors.joining());
    }

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
    void summaryUsesConfirmedDescriptionsForAllFourParts() {
        var parts = catalog.getDefinition(LessonService.HELLO_PROGRAM_LESSON_ID).parts();

        assertEquals(java.util.List.of(
                        "public class Main {",
                        "public static void main(String[] args) {",
                        "System.out.println(\"Hello\");",
                        "}\n}"),
                parts.stream().map(part -> part.targetCode()).toList());
        assertEquals(java.util.List.of(
                        "Mainというクラスを宣言し、その中身を始める",
                        "mainメソッドを宣言し、その中身を始める",
                        "Helloと表示して改行する",
                        "mainメソッドとMainクラスを順番に終了する"),
                parts.stream().map(part -> part.reviewSummary()).toList());
    }

    @Test
    void Step定義に正解カードと正本の説明セクションを保持する() {
        var step = catalog.getDefinition(
                LessonService.HELLO_PROGRAM_LESSON_ID
        ).getStep("main-public");

        assertEquals("accessible", step.correctCard().id());
        assertEquals("ほかの場所からも使える", step.correctCard().text());
        assertEquals("アクセス修飾子", step.technicalTerm());
        assertEquals(
                java.util.List.of("text", "table", "text", "official-references"),
                step.explanationSections().stream()
                        .map(section -> section.sectionType().value()).toList()
        );
        assertTrue(step.explanationSections().stream()
                .flatMap(section -> section.entries().stream())
                .anyMatch(entry -> entry.emphasis().equals("public")));
        assertEquals("main-public", step.toLessonStep().id());
    }

    @Test
    void publicKeepsBeginnerSectionsAndAddsJavaSe21OfficialReferences() {
        var step = catalog.getDefinition(
                LessonService.HELLO_PROGRAM_LESSON_ID
        ).getStep("class-public");

        var sections = step.explanationSections();
        assertEquals("アクセス修飾子　access modifier　", sections.get(0).title());
        assertEquals("アクセスできる範囲の違い", sections.get(1).title());
        assertTrue(sections.get(1).tableHeader());
        assertEquals(
                java.util.List.of("書き方", "public", "protected", "（指定なし）", "private"),
                sections.get(1).entries().stream().map(entry -> entry.label()).toList()
        );
        assertEquals(
                java.util.List.of(
                        "アクセスできる範囲",
                        "🌎 ほかの場所からも使える",
                        "📦 同じパッケージ＋子クラス",
                        "📦 同じパッケージだけ",
                        "🔒 自分のクラスだけ"
                ),
                sections.get(1).entries().stream().map(entry -> entry.before()).toList()
        );
        assertEquals("ポイント", sections.get(2).title());
        assertEquals("このページの技術的根拠：Javaの公式仕様・API", sections.get(3).title());

        var references = sections.get(3).officialReferences();
        assertEquals(2, references.size());
        assertEquals(java.util.List.of("§6.6", "§6.6.1"), references.stream()
                .map(reference -> reference.sectionNumber()).toList());
        assertTrue(references.stream().allMatch(reference ->
                reference.source().value().equals("jls")
                        && reference.version().equals("Java SE 21")
                        && reference.sourceName().equals("JLS Java SE 21")
                        && reference.uri().getScheme().equals("https")
                        && reference.uri().getHost().equals("docs.oracle.com")));
        assertEquals(
                java.util.List.of(
                        "Javaのアクセス制御について定めています。",
                        "public などで宣言された要素にアクセスできる条件を定めています。"
                ),
                references.stream().map(reference -> reference.description()).toList()
        );
        assertFalse(references.stream().anyMatch(reference ->
                reference.source().value().equals("java-se-api")));
    }

    @Test
    void classUsesDeclarationBasicsAndAddsJavaSe21OfficialReferences() {
        var step = catalog.getDefinition(
                LessonService.HELLO_PROGRAM_LESSON_ID
        ).getStep("class-keyword");

        var sections = step.explanationSections();
        assertEquals("クラス　class　", sections.get(0).title());
        assertEquals("クラス宣言の基本形", sections.get(1).title());
        assertEquals("ポイント", sections.get(2).title());
        assertEquals("このページの技術的根拠：Javaの公式仕様・API", sections.get(3).title());
        assertFalse(sections.stream().anyMatch(section -> section.title().contains("家でたとえる")));

        assertEquals(
                java.util.List.of(
                        "class クラス名 {\n}",
                        "書き方",
                        "class",
                        "クラス名",
                        "{ }"),
                sections.get(1).entries().stream().map(entry -> entry.label()).toList()
        );
        assertEquals(
                java.util.List.of(
                        "",
                        "意味",
                        "クラスを作る",
                        "作るクラスの名前（自分で決めることができます）",
                        "クラスの中身を書く範囲"),
                sections.get(1).entries().stream().map(entry -> entry.before()).toList()
        );

        var references = sections.get(3).officialReferences();
        assertEquals(2, references.size());
        assertEquals(java.util.List.of("§8.1", "§3.9"), references.stream()
                .map(reference -> reference.sectionNumber()).toList());
        assertEquals(java.util.List.of("Class Declarations", "Keywords"), references.stream()
                .map(reference -> reference.sectionTitle()).toList());
        assertTrue(references.stream().allMatch(reference ->
                reference.source().value().equals("jls")
                        && reference.version().equals("Java SE 21")
                        && reference.sourceName().equals("JLS Java SE 21")
                        && reference.uri().getScheme().equals("https")
                        && reference.uri().getHost().equals("docs.oracle.com")));
    }

    @Test
    void mainClassNameUsesNamingRulesAndAddsJavaSe21OfficialReferences() {
        var step = catalog.getDefinition(
                LessonService.HELLO_PROGRAM_LESSON_ID
        ).getStep("class-name");

        assertEquals("クラスの名前", step.correctCard().text());

        var sections = step.explanationSections();
        assertEquals(java.util.List.of("text", "text", "text", "official-references"),
                sections.stream().map(section -> section.sectionType().value()).toList());
        assertEquals(java.util.List.of(
                        "クラス名　class name",
                        "名前を付けるときのルール",
                        "ポイント",
                        "このページの技術的根拠：Javaの公式仕様・API"),
                sections.stream().map(section -> section.title()).toList());

        assertEquals(java.util.List.of("自分で決めるクラスの名前"),
                sections.get(0).entries().stream().map(entry -> entry.emphasis()).toList());
        assertEquals(java.util.List.of(
                        "",
                        "Main や Test",
                        "public class Main と書いた場合、ファイル名は Main.java"),
                sections.get(1).entries().stream().map(entry -> entry.emphasis()).toList());
        assertEquals(java.util.List.of(
                        "1. 自由に決めてOK",
                        "2. 大文字で始めるのが慣習",
                        "3. ファイル名と同じにする"),
                sections.get(1).entries().stream().map(entry -> entry.label()).toList());
        assertFalse(sections.get(1).tableHeader());
        assertEquals(java.util.List.of(
                        "予約語でなければ、好きな名前を付けられます。",
                        "Main や Test のように、大文字で書き始めることが推奨されています。",
                        "public class Main と書いた場合、ファイル名は Main.java にします。"),
                sections.get(1).entries().stream()
                        .map(entry -> entry.before() + entry.emphasis() + entry.after())
                        .toList());
        assertEquals(1, sections.get(2).entries().size());
        assertEquals("class", sections.get(2).entries().get(0).emphasis());

        var references = sections.get(3).officialReferences();
        assertEquals(3, references.size());
        assertEquals(java.util.List.of("§8.1", "§7.6", "§6.1"), references.stream()
                .map(reference -> reference.sectionNumber()).toList());
        assertEquals(java.util.List.of(
                        "Class Declarations",
                        "Top Level Class and Interface Declarations",
                        "Declarations"),
                references.stream().map(reference -> reference.sectionTitle()).toList());
        assertTrue(references.stream().allMatch(reference ->
                reference.source().value().equals("jls")
                        && reference.version().equals("Java SE 21")
                        && reference.sourceName().equals("JLS Java SE 21")
                        && reference.uri().getScheme().equals("https")
                        && reference.uri().getHost().equals("docs.oracle.com")));
    }

    @Test
    void staticKeepsExistingFirstSectionsAndAddsJavaSe21OfficialReferences() {
        var step = catalog.getDefinition(
                LessonService.HELLO_PROGRAM_LESSON_ID
        ).getStep("static");

        var sections = step.explanationSections();
        assertEquals(java.util.List.of("text", "table", "text", "official-references"),
                sections.stream().map(section -> section.sectionType().value()).toList());
        assertEquals("static修飾子　static modifier", sections.get(0).title());
        assertEquals("static修飾子の特徴", sections.get(1).title());
        assertEquals(java.util.List.of(
                        java.util.List.of("書き方", "特徴"),
                        java.util.List.of("static", "インスタンス（実体）を作らなくても使える"),
                        java.util.List.of("（なし）", "インスタンス（実体）を作ってから使う")),
                sections.get(1).entries().stream()
                        .map(entry -> java.util.List.of(entry.label(), entry.before()))
                        .toList());
        assertTrue(sections.get(1).entries().stream().noneMatch(entry -> entry.highlighted()));
        assertEquals("ポイント", sections.get(2).title());
        assertEquals(
                "Javaのプログラムを実行するとき、開始地点となる main メソッドにも、インスタンスを作らずに呼び出せるように static が付いています。",
                sections.get(2).entries().stream()
                        .map(entry -> entry.before() + entry.emphasis() + entry.after())
                        .collect(java.util.stream.Collectors.joining())
        );
        assertEquals(
                "開始地点となる main メソッドにも、インスタンスを作らずに呼び出せるように static",
                sections.get(2).entries().get(0).emphasis()
        );

        var references = sections.get(3).officialReferences();
        assertEquals(2, references.size());
        assertEquals(java.util.List.of("§8.4.3.2", "§12.1.4"), references.stream()
                .map(reference -> reference.sectionNumber()).toList());
        assertEquals(java.util.List.of("static Methods", "Invoke Test.main"), references.stream()
                .map(reference -> reference.sectionTitle()).toList());
        assertTrue(references.stream().allMatch(reference ->
                reference.source().value().equals("jls")
                        && reference.version().equals("Java SE 21")
                        && reference.sourceName().equals("JLS Java SE 21")
                        && reference.uri().getScheme().equals("https")
                        && reference.uri().getHost().equals("docs.oracle.com")));
    }

    @Test
    void voidUsesReturnValueComparisonAndAddsJavaSe21OfficialReferences() {
        var step = catalog.getDefinition(
                LessonService.HELLO_PROGRAM_LESSON_ID
        ).getStep("void");

        assertEquals("戻り値を返さない", step.correctCard().text());

        var sections = step.explanationSections();
        assertEquals(java.util.List.of("text", "table", "text", "official-references"),
                sections.stream().map(section -> section.sectionType().value()).toList());
        assertEquals(java.util.List.of(
                        "戻り値　return value",
                        "戻り値の型の違い",
                        "ポイント",
                        "このページの技術的根拠：Javaの公式仕様・API"),
                sections.stream().map(section -> section.title()).toList());
        assertEquals("値を返さない", sections.get(0).entries().get(0).emphasis());
        assertEquals(java.util.List.of("戻り値の型", "void", "int", "String"), sections.get(1).entries().stream()
                .map(entry -> entry.label()).toList());
        assertEquals(java.util.List.of("意味", "値を返さない", "int型の値を返す", "String型の値を返す"),
                sections.get(1).entries().stream().map(entry -> entry.before()).toList());
        assertEquals(
                "画面に表示するだけの処理など、結果を返す必要がない場合に使います。",
                sections.get(2).entries().get(0).before()
        );

        var references = sections.get(3).officialReferences();
        assertEquals(2, references.size());
        assertEquals(java.util.List.of("§8.4.5", "§12.1.4"), references.stream()
                .map(reference -> reference.sectionNumber()).toList());
        assertEquals(java.util.List.of("Method Result", "Invoke Test.main"), references.stream()
                .map(reference -> reference.sectionTitle()).toList());
        assertTrue(references.stream().allMatch(reference ->
                reference.source().value().equals("jls")
                        && reference.version().equals("Java SE 21")
                        && reference.sourceName().equals("JLS Java SE 21")
                        && reference.uri().getScheme().equals("https")
                        && reference.uri().getHost().equals("docs.oracle.com")));
    }

    @Test
    void mainUsesMethodNameComparisonAndAddsJavaSe21OfficialReference() {
        var step = catalog.getDefinition(
                LessonService.HELLO_PROGRAM_LESSON_ID
        ).getStep("main");

        assertEquals("プログラム開始メソッド", step.correctCard().text());

        var sections = step.explanationSections();
        assertEquals(java.util.List.of("text", "table", "text", "official-references"),
                sections.stream().map(section -> section.sectionType().value()).toList());
        assertEquals(java.util.List.of(
                        "メソッド名　method name",
                        "mainメソッドとMainクラスの違い",
                        "ポイント",
                        "このページの技術的根拠：Javaの公式仕様・API"),
                sections.stream().map(section -> section.title()).toList());
        assertEquals("メソッドの名前", sections.get(0).entries().get(0).emphasis());
        assertEquals(java.util.List.of("書き方", "main", "Main"), sections.get(1).entries().stream()
                .map(entry -> entry.label()).toList());
        assertEquals(java.util.List.of(
                        "意味",
                        "Javaが最初に実行するメソッドの名前",
                        "自分で決めるクラスの名前"),
                sections.get(1).entries().stream().map(entry -> entry.before()).toList());
        assertEquals("main", sections.get(2).entries().get(0).emphasis());
        assertEquals(
                " はJavaのキーワードではありませんが、プログラムの開始に使われる決められたメソッド名です。",
                sections.get(2).entries().get(0).after()
        );

        var references = sections.get(3).officialReferences();
        assertEquals(1, references.size());
        var reference = references.get(0);
        assertEquals("§12.1.4", reference.sectionNumber());
        assertEquals("Invoke Test.main", reference.sectionTitle());
        assertEquals("JLS Java SE 21", reference.sourceName());
        assertEquals("Java SE 21", reference.version());
        assertEquals("https", reference.uri().getScheme());
        assertEquals("docs.oracle.com", reference.uri().getHost());
        assertEquals(
                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-12.html#jls-12.1.4",
                reference.uri().toString()
        );
    }

    @Test
    void stringArrayUsesArrayTypeExplanationAndAddsJavaSe21OfficialReferences() {
        var step = catalog.getDefinition(
                LessonService.HELLO_PROGRAM_LESSON_ID
        ).getStep("string-array");

        assertEquals("文字列の配列", step.correctCard().text());

        var sections = step.explanationSections();
        assertEquals(java.util.List.of("text", "table", "text", "official-references"),
                sections.stream().map(section -> section.sectionType().value()).toList());
        assertEquals(java.util.List.of(
                        "配列型　array type",
                        "String[]の意味",
                        "ポイント",
                        "このページの技術的根拠：Javaの公式仕様・API"),
                sections.stream().map(section -> section.title()).toList());
        assertEquals(
                "文字列（String）を複数まとめて扱うための型です。",
                sections.get(0).entries().get(0).before()
        );
        assertEquals(java.util.List.of("書き方", "String", "[]"), sections.get(1).entries().stream()
                .map(entry -> entry.label()).toList());
        assertEquals(java.util.List.of(
                        "意味",
                        "文字列を表す型です。",
                        "複数の値をまとめて扱う配列であることを表します。"),
                sections.get(1).entries().stream().map(entry -> entry.before()).toList());
        assertEquals(
                "プログラムの実行時に、外から渡された文字列を受け取るために使います。",
                sections.get(2).entries().get(0).before()
        );

        var references = sections.get(3).officialReferences();
        assertEquals(2, references.size());
        assertEquals(java.util.List.of("§10.1", "§12.1.4"), references.stream()
                .map(reference -> reference.sectionNumber()).toList());
        assertEquals(java.util.List.of("Array Types", "Invoke Test.main"), references.stream()
                .map(reference -> reference.sectionTitle()).toList());
        assertTrue(references.stream().allMatch(reference ->
                reference.source().value().equals("jls")
                        && reference.version().equals("Java SE 21")
                        && reference.sourceName().equals("JLS Java SE 21")
                        && reference.uri().getScheme().equals("https")
                        && reference.uri().getHost().equals("docs.oracle.com")));
    }

    @Test
    void argsUsesArgumentNameComparisonAndAddsJavaSe21OfficialReference() {
        var step = catalog.getDefinition(
                LessonService.HELLO_PROGRAM_LESSON_ID
        ).getStep("args");

        assertEquals("変数の名前", step.correctCard().text());

        var sections = step.explanationSections();
        assertEquals(java.util.List.of("text", "table", "text", "official-references"),
                sections.stream().map(section -> section.sectionType().value()).toList());
        assertEquals(java.util.List.of(
                        "引数名　argument name",
                        "String[] と args の関係",
                        "ポイント",
                        "このページの技術的根拠：Javaの公式仕様・API"),
                sections.stream().map(section -> section.title()).toList());
        assertEquals("変数の名前", sections.get(0).entries().get(0).emphasis());
        assertEquals(java.util.List.of("書き方", "String[]", "args"), sections.get(1).entries().stream()
                .map(entry -> entry.label()).toList());
        assertEquals(java.util.List.of("意味", "受け取る値の型です", "受け取る変数の名前です"),
                sections.get(1).entries().stream().map(entry -> entry.before()).toList());
        assertEquals(java.util.List.of("args", "args", ""),
                sections.get(2).entries().stream().map(entry -> entry.emphasis()).toList());

        var references = sections.get(3).officialReferences();
        assertEquals(1, references.size());
        var reference = references.get(0);
        assertEquals("§12.1.4", reference.sectionNumber());
        assertEquals("Invoke Test.main", reference.sectionTitle());
        assertEquals("JLS Java SE 21", reference.sourceName());
        assertEquals("Java SE 21", reference.version());
        assertEquals("https", reference.uri().getScheme());
        assertEquals("docs.oracle.com", reference.uri().getHost());
    }

    @Test
    void bothOpeningBracesShareSeparatorExplanationAndOfficialReferences() {
        var definition = catalog.getDefinition(LessonService.HELLO_PROGRAM_LESSON_ID);
        var classOpen = definition.getStep("class-open");
        var mainOpen = definition.getStep("main-open");

        assertEquals("ここから始まる", classOpen.correctCard().text());
        assertEquals("ここから始まる", mainOpen.correctCard().text());
        assertEquals(classOpen.explanationSections(), mainOpen.explanationSections());

        var sections = classOpen.explanationSections();
        assertEquals(java.util.List.of("text", "table", "text", "official-references"),
                sections.stream().map(section -> section.sectionType().value()).toList());
        assertEquals(java.util.List.of(
                        "セパレータ（左波括弧）　separator",
                        "波括弧のルール",
                        "ポイント",
                        "このページの技術的根拠：Javaの公式仕様・API"),
                sections.stream().map(section -> section.title()).toList());
        assertEquals("ブロックが、ここから始まる", sections.get(0).entries().get(0).emphasis());
        assertFalse(sections.get(1).tableHeader());
        assertEquals("{ と } はセット", sections.get(1).entries().get(0).label());
        assertEquals("{ で始まった範囲は、対応する } で終わります。",
                sections.get(1).entries().get(0).before());
        assertEquals("閉じ忘れがあると、Javaはプログラムの構造を正しく理解できずエラーになります。",
                sections.get(2).entries().get(0).before());

        var references = sections.get(3).officialReferences();
        assertEquals(3, references.size());
        assertEquals(java.util.List.of("§3.11", "§8.1.7", "§14.2"), references.stream()
                .map(reference -> reference.sectionNumber()).toList());
        assertEquals(java.util.List.of(
                        "Separators",
                        "Class Body and Member Declarations",
                        "Blocks"),
                references.stream().map(reference -> reference.sectionTitle()).toList());
        assertTrue(references.stream().allMatch(reference ->
                reference.source().value().equals("jls")
                        && reference.version().equals("Java SE 21")
                        && reference.uri().getScheme().equals("https")
                        && reference.uri().getHost().equals("docs.oracle.com")));
    }

    @Test
    void systemOutPrintlnUsesStandardOutputComparisonAndJavaSe21ApiReferences() {
        var step = catalog.getDefinition(
                LessonService.HELLO_PROGRAM_LESSON_ID
        ).getStep("print-command");

        assertEquals("画面に表示して改行する", step.correctCard().text());

        var sections = step.explanationSections();
        assertEquals(java.util.List.of("text", "table", "table", "official-references"),
                sections.stream().map(section -> section.sectionType().value()).toList());
        assertEquals(java.util.List.of(
                        "標準出力　standard output",
                        "System.out.println のしくみ",
                        "println と print の違い",
                        "このページの技術的根拠：Javaの公式仕様・API"),
                sections.stream().map(section -> section.title()).toList());
        assertEquals("最後に改行します", sections.get(0).entries().get(0).emphasis());
        assertTrue(sections.get(1).tableHeader());
        assertTrue(sections.get(2).tableHeader());
        assertEquals(java.util.List.of("書き方", "System", "out", "println"), sections.get(1).entries().stream()
                .map(entry -> entry.label()).toList());
        assertEquals(java.util.List.of(
                        "意味",
                        "Java標準のクラスです",
                        "標準出力を表すフィールドです",
                        "内容を出力して改行するメソッドです"),
                sections.get(1).entries().stream().map(entry -> entry.before()).toList());
        assertEquals(java.util.List.of("書き方", "println", "print"), sections.get(2).entries().stream()
                .map(entry -> entry.label()).toList());
        assertEquals(java.util.List.of("違い", "表示したあと改行します", "表示したあと改行しません"),
                sections.get(2).entries().stream().map(entry -> entry.before()).toList());

        var stage2Sections = catalog.getDefinition(CodeReadingLessonCatalog.STAGE2_LESSON_ID)
                .getStep("print-command").explanationSections();
        assertEquals(sections, stage2Sections);
        assertTrue(stage2Sections.get(1).tableHeader());
        assertTrue(stage2Sections.get(2).tableHeader());

        var references = sections.get(3).officialReferences();
        assertEquals(2, references.size());
        assertEquals(java.util.List.of("System.out", "PrintStream.println(String)"), references.stream()
                .map(reference -> reference.sectionTitle()).toList());
        assertTrue(references.stream().allMatch(reference ->
                reference.source().value().equals("java-se-api")
                        && reference.version().equals("Java SE 21")
                        && reference.sourceName().equals("Java SE 21")
                        && reference.sectionNumber().equals("API")
                        && reference.uri().getScheme().equals("https")
                        && reference.uri().getHost().equals("docs.oracle.com")));
        assertTrue(references.stream().noneMatch(reference ->
                reference.sectionTitle().contains("String Literals")));
    }

    @Test
    void helloStringUsesLiteralExamplesAndJavaSe21OfficialReferences() {
        var step = catalog.getDefinition(
                LessonService.HELLO_PROGRAM_LESSON_ID
        ).getStep("hello-string");

        assertEquals("表示する文字", step.correctCard().text());

        var sections = step.explanationSections();
        assertEquals(java.util.List.of("text", "table", "text", "official-references"),
                sections.stream().map(section -> section.sectionType().value()).toList());
        assertEquals(java.util.List.of(
                        "文字列リテラル　string literal",
                        "ダブルクォート \" \" の中",
                        "println との関係",
                        "このページの技術的根拠：Javaの公式仕様・API"),
                sections.stream().map(section -> section.title()).toList());
        assertEquals("文字列を直接書いたもの", sections.get(0).entries().get(0).emphasis());
        assertTrue(sections.get(1).tableHeader());
        assertEquals(java.util.List.of("書き方", "\"Hello\"", "\"こんにちは\"", "\"Javaを勉強中\"", "\"123\""),
                sections.get(1).entries().stream().map(entry -> entry.label()).toList());
        assertEquals(java.util.List.of(
                        "意味",
                        "英語を書くことができます。",
                        "日本語を書くことができます。",
                        "文字を組み合わせて書くこともできます。",
                        "数字も文字列として書くことができます。"),
                sections.get(1).entries().stream().map(entry -> entry.before()).toList());
        assertEquals("引数", sections.get(2).entries().get(0).emphasis());

        var references = sections.get(3).officialReferences();
        assertEquals(2, references.size());
        assertEquals(java.util.List.of("§3.10.5", "API"), references.stream()
                .map(reference -> reference.sectionNumber()).toList());
        assertEquals(java.util.List.of("String Literals", "java.lang.String"), references.stream()
                .map(reference -> reference.sectionTitle()).toList());
        assertEquals(java.util.List.of("jls", "java-se-api"), references.stream()
                .map(reference -> reference.source().value()).toList());
        assertTrue(references.stream().allMatch(reference ->
                reference.version().equals("Java SE 21")
                        && reference.uri().getScheme().equals("https")
                        && reference.uri().getHost().equals("docs.oracle.com")));
    }

    @Test
    void semicolonExplainsStatementBoundariesAndJavaSe21References() {
        var step = catalog.getDefinition(
                LessonService.HELLO_PROGRAM_LESSON_ID
        ).getStep("semicolon");

        assertEquals("文の終わり", step.correctCard().text());

        var sections = step.explanationSections();
        assertEquals(java.util.List.of("text", "table", "text", "official-references"),
                sections.stream().map(section -> section.sectionType().value()).toList());
        assertEquals(java.util.List.of(
                        "セパレータ（セミコロン）　separator",
                        "; が必要な場合と必要でない場合",
                        "ポイント",
                        "このページの技術的根拠：Javaの公式仕様・API"),
                sections.stream().map(section -> section.title()).toList());
        assertEquals("文を区切るために使われる記号", sections.get(0).entries().get(0).emphasis());
        assertEquals(java.util.List.of(
                        "種類",
                        "式文・宣言文",
                        "return・break など",
                        "ブロック",
                        "クラス・メソッドの本体"),
                sections.get(1).entries().stream().map(entry -> entry.label()).toList());
        assertEquals(java.util.List.of(
                        "例",
                        "println(...); / int x = 1;",
                        "return; / break;",
                        "{ ... }",
                        "class Main { ... }"),
                sections.get(1).entries().stream().map(entry -> entry.before()).toList());
        assertEquals(java.util.List.of(";", "必要", "必要", "不要", "不要"),
                sections.get(1).entries().stream().map(entry -> entry.emphasis()).toList());
        assertTrue(sections.get(1).entries().stream().noneMatch(entry -> entry.highlighted()));
        assertEquals("if 文、while 文、for 文など、;", sections.get(2).entries().get(0).emphasis());

        var references = sections.get(3).officialReferences();
        assertEquals(3, references.size());
        assertEquals(java.util.List.of("§3.11", "§14.8", "§14.5"), references.stream()
                .map(reference -> reference.sectionNumber()).toList());
        assertEquals(java.util.List.of("Separators", "Expression Statements", "Statements"),
                references.stream().map(reference -> reference.sectionTitle()).toList());
        assertTrue(references.stream().allMatch(reference ->
                reference.source().value().equals("jls")
                        && reference.version().equals("Java SE 21")
                        && reference.uri().getScheme().equals("https")
                        && reference.uri().getHost().equals("docs.oracle.com")));
    }

    @Test
    void bothClosingBracesShareEndExplanationAndOfficialReferences() {
        var definition = catalog.getDefinition(LessonService.HELLO_PROGRAM_LESSON_ID);
        var mainClose = definition.getStep("main-close");
        var classClose = definition.getStep("class-close");

        assertEquals("ここで終わる", mainClose.correctCard().text());
        assertEquals("ここで終わる", classClose.correctCard().text());
        assertSame(mainClose.explanationSections(), classClose.explanationSections());

        var sections = mainClose.explanationSections();
        assertEquals(java.util.List.of("text", "text", "text", "official-references"),
                sections.stream().map(section -> section.sectionType().value()).toList());
        assertEquals(java.util.List.of(
                        "セパレータ（右波括弧）　separator",
                        "{ } を書くときのコツ",
                        "ポイント",
                        "このページの技術的根拠：Javaの公式仕様・API"),
                sections.stream().map(section -> section.title()).toList());
        assertEquals("ブロックがここで終わる", sections.get(0).entries().get(0).emphasis());
        assertEquals("{ }", sections.get(1).entries().get(0).label());
        assertEquals("一般的なプログラミング上の推奨習慣", sections.get(1).entries().get(0).emphasis());
        assertEquals("}", sections.get(2).entries().get(0).label());
        assertEquals("インデント（字下げ）の位置", sections.get(2).entries().get(0).emphasis());

        var references = sections.get(3).officialReferences();
        assertEquals(3, references.size());
        assertEquals(java.util.List.of("§3.11", "§8.1.7", "§14.2"), references.stream()
                .map(reference -> reference.sectionNumber()).toList());
        assertEquals(java.util.List.of(
                        "Separators",
                        "Class Body and Member Declarations",
                        "Blocks"),
                references.stream().map(reference -> reference.sectionTitle()).toList());
        assertTrue(references.stream().allMatch(reference ->
                reference.source().value().equals("jls")
                        && reference.version().equals("Java SE 21")
                        && reference.uri().getScheme().equals("https")
                        && reference.uri().getHost().equals("docs.oracle.com")));
    }

    @Test
    void 補足を担う第3Sectionの見出しをポイントに統一する() {
        var definition = catalog.getDefinition(LessonService.HELLO_PROGRAM_LESSON_ID);
        var targetStepIds = java.util.List.of(
                "class-public", "class-keyword", "class-name", "class-open",
                "main-public", "static", "void", "main", "string-array", "args", "main-open",
                "main-close", "class-close"
        );

        for (String stepId : targetStepIds) {
            var sections = definition.getStep(stepId).explanationSections();
            assertTrue(sections.size() >= 3, stepId);
            assertEquals("ポイント", sections.get(2).title(), stepId);
        }

        assertEquals(
                "このページの技術的根拠：Javaの公式仕様・API",
                definition.getStep("class-public").explanationSections().get(3).title()
        );
        assertEquals(
                "このページの技術的根拠：Javaの公式仕様・API",
                definition.getStep("class-keyword").explanationSections().get(3).title()
        );
        assertEquals(
                "println と print の違い",
                definition.getStep("print-command").explanationSections().get(2).title()
        );
        assertEquals(
                "println との関係",
                definition.getStep("hello-string").explanationSections().get(2).title()
        );
        assertEquals("ポイント", definition.getStep("semicolon").explanationSections().get(2).title());
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
                "officialReferencesSection", "jlsReference", "javaSeApiReference",
                "text", "tableRow", "highlightedTableRow", "diagramRow",
                "example", "note", "qaEntry", "comparisonEntry", "listItem"
        )));
        assertTrue(Arrays.stream(CodeReadingLessonCatalog.class.getDeclaredMethods())
                .filter(method -> method.getName().equals("section"))
                .noneMatch(method -> method.getParameterTypes()[0].equals(String.class)));
    }

    private static String allExplanationText(
            CodeReadingLessonDefinition definition,
            String stepId
    ) {
        return definition.getStep(stepId).explanationSections().stream()
                .flatMap(section -> section.entries().stream())
                .map(entry -> entry.label() + entry.before() + entry.emphasis()
                        + entry.after() + entry.detail())
                .collect(Collectors.joining());
    }
}
