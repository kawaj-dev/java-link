package com.javalink.service;

import com.javalink.model.CodeReadingItem;
import com.javalink.model.CodeReadingAnswerResponse;
import com.javalink.model.CodeReadingPart;
import com.javalink.model.LessonProgress;
import com.javalink.model.QuizOption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Arrays;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * コードを左から読む画面と復習画面の表示データを確認します。
 */
class CodeReadingServiceTest {

    private static final String LESSON_ID =
            LessonService.HELLO_PROGRAM_LESSON_ID;

    private CodeReadingService codeReadingService;

    @BeforeEach
    void setUp() {
        codeReadingService = new CodeReadingService(
                new CodeReadingLessonCatalog()
        );
    }

    @Test
    void Stage1の用語と正解カードを教材定義の順に返す() {
        LessonProgress progress = new LessonProgress(
                LESSON_ID,
                "class-public"
        );

        List<CodeReadingItem> items =
                codeReadingService.createItems(LESSON_ID, progress);

        assertEquals(
                List.of(
                        "public", "class", "Main", "{",
                        "public", "static", "void", "main",
                        "String[]", "args", "{",
                        "System.out.println", "\"Hello\"", ";", "}", "}"
                ),
                items.stream().map(CodeReadingItem::keyword).toList()
        );
        assertEquals(
                List.of(
                        "ほかの場所からも使える", "クラスを作る", "クラスの名前", "ここから始まる",
                        "ほかの場所からも使える", "インスタンスを作らなくても使える",
                        "戻り値を返さない", "プログラム開始メソッド",
                        "文字列の配列", "変数の名前", "ここから始まる",
                        "画面に表示して改行する", "表示する文字", "命令の終わり",
                        "mainメソッド終了", "Mainクラス終了"
                ),
                items.stream().map(CodeReadingItem::meaning).toList()
        );
    }

    @Test
    void 進捗に合わせて完了済みと現在の用語を示す() {
        LessonProgress progress = new LessonProgress(
                LESSON_ID,
                "class-public"
        );
        progress.completeStep("class-public");
        progress.setCurrentStepId("class-keyword");

        List<CodeReadingItem> items =
                codeReadingService.createItems(LESSON_ID, progress);

        assertTrue(items.get(0).completed());
        assertFalse(items.get(0).current());
        assertFalse(items.get(1).completed());
        assertTrue(items.get(1).current());
    }

    @Test
    void 復習用の役割と初心者向け説明を返す() {
        LessonProgress progress = new LessonProgress(
                LESSON_ID,
                "class-public"
        );

        List<CodeReadingItem> items =
                codeReadingService.createItems(LESSON_ID, progress);

        assertEquals("アクセス修飾子", items.get(0).roleLabel());
        assertTrue(items.get(0).explanationSections().stream()
                .flatMap(section -> section.entries().stream())
                .map(entry -> entry.before() + entry.emphasis() + entry.after())
                .anyMatch(text -> text.contains("ほかのクラスからも利用できます")));
        assertEquals("引数名", items.get(9).roleLabel());
    }

    @Test
    void Stage1の全用語に必要な説明セクションが教材順である() {
        LessonProgress progress = new LessonProgress(
                LESSON_ID,
                "class-public"
        );

        List<CodeReadingItem> items =
                codeReadingService.createItems(LESSON_ID, progress);

        CodeReadingItem publicItem = items.get(0);
        assertTrue(publicItem.explanationSections().stream()
                .anyMatch(section -> section.title().contains("access modifier")));
        var accessTable = publicItem.explanationSections().stream()
                .filter(section -> section.sectionType().value().equals("table"))
                .findFirst().orElseThrow();
        assertEquals(4, accessTable.entries().size());
        assertTrue(accessTable.entries().stream().anyMatch(entry ->
                entry.label().equals("public")
        ));

        CodeReadingItem classItem = items.get(1);
        var classDeclaration = classItem.explanationSections().stream()
                .filter(section -> section.sectionType().value().equals("table"))
                .findFirst().orElseThrow();
        assertEquals("クラス宣言の基本形", classDeclaration.title());
        assertEquals(
                java.util.List.of("class クラス名 {　　}", "class", "クラス名", "{ ～ }"),
                classDeclaration.entries().stream().map(entry -> entry.label()).toList()
        );
        assertFalse(classItem.explanationSections().stream()
                .anyMatch(section -> section.title().equals("🏠 家でたとえると・・・")));

        CodeReadingItem mainItem = items.get(2);
        assertEquals(
                java.util.List.of("text", "text", "text", "official-references"),
                mainItem.explanationSections().stream()
                        .map(section -> section.sectionType().value()).toList()
        );
        assertEquals("名前を付けるときのルール", mainItem.explanationSections().get(1).title());
        assertEquals(3, mainItem.explanationSections().get(1).entries().size());
        assertEquals(3, mainItem.explanationSections().get(3).officialReferences().size());

        CodeReadingItem blockItem = items.get(3);
        assertTrue(blockItem.explanationSections().stream()
                .flatMap(section -> section.entries().stream())
                .anyMatch(entry -> entry.before().contains("対応する } で終わります")));
        assertTrue(blockItem.explanationSections().stream()
                .filter(section -> section.sectionType().value().equals("table"))
                .flatMap(section -> section.entries().stream())
                .anyMatch(entry -> entry.label().equals("{ と } はセット")));

        assertTrue(items.stream().allMatch(item ->
                !item.explanationSections().isEmpty()
        ));
        assertEquals(
                List.of("text", "table", "text", "official-references"),
                publicItem.explanationSections().stream()
                        .map(section -> section.sectionType().value()).toList()
        );
        assertEquals("ポイント", mainItem.explanationSections().get(2).title());
        for (int index : List.of(5, 6, 7, 8, 9)) {
            assertTrue(items.get(index).explanationSections().stream()
                    .anyMatch(section -> section.sectionType().value().equals("table")));
        }
        for (int index : List.of(11, 12, 13, 14, 15)) {
            CodeReadingItem textItem = items.get(index);
            assertTrue(textItem.explanationSections().stream()
                    .flatMap(section -> section.entries().stream())
                    .map(entry -> entry.before() + entry.emphasis() + entry.after())
                    .anyMatch(text -> !text.isBlank()));
        }
        assertTrue(items.stream()
                .flatMap(item -> item.explanationSections().stream())
                .noneMatch(section -> section.title().equals("初心者が迷いやすいポイント")));
    }

    @Test
    void 表示Modelに旧互換説明フィールドが存在しない() {
        Set<String> itemComponents = Arrays.stream(CodeReadingItem.class.getRecordComponents())
                .map(component -> component.getName())
                .collect(java.util.stream.Collectors.toSet());
        Set<String> responseComponents = Arrays.stream(CodeReadingAnswerResponse.class.getRecordComponents())
                .map(component -> component.getName())
                .collect(java.util.stream.Collectors.toSet());

        assertFalse(itemComponents.contains("technicalExplanation"));
        assertFalse(itemComponents.contains("explanations"));
        assertFalse(responseComponents.contains("technicalExplanation"));
        assertFalse(responseComponents.contains("beginnerExplanations"));
        assertTrue(itemComponents.contains("explanationSections"));
        assertTrue(responseComponents.contains("explanationSections"));
    }

    @Test
    void 現在の正解を含む四択を返す() {
        LessonProgress progress = new LessonProgress(
                LESSON_ID,
                "class-public"
        );

        List<QuizOption> options =
                codeReadingService.createSelectionOptions(
                        LESSON_ID,
                        progress
                );

        assertEquals(1, options.size());
        assertTrue(options.stream().anyMatch(option ->
                option.id().equals("accessible")
        ));
    }

    @Test
    void 不正解後も選んだ誤答カードを選択場所へ残す() {
        LessonProgress progress = new LessonProgress(
                LESSON_ID,
                "class-public"
        );
        progress.setAnswered(true);
        progress.setCorrect(false);
        progress.setSelectedOptionId("without-instance");

        List<QuizOption> options =
                codeReadingService.createSelectionOptions(
                        LESSON_ID,
                        progress
                );

        assertEquals(1, options.size());
        assertTrue(options.stream().anyMatch(option ->
                option.id().equals("accessible")
        ));
        assertFalse(options.stream().anyMatch(option ->
                option.id().equals("without-instance")
        ));
    }

    @Test
    void 最終問題でも四択を返す() {
        LessonProgress progress = new LessonProgress(
                LESSON_ID,
                "class-public"
        );
        for (String stepId : List.of(
                "class-public",
                "class-keyword",
                "class-name",
                "class-open",
                "main-public",
                "static",
                "void",
                "main",
                "string-array",
                "args",
                "main-open",
                "print-command",
                "hello-string",
                "semicolon",
                "main-close"
        )) {
            progress.completeStep(stepId);
        }
        progress.setCurrentStepId("class-close");

        List<QuizOption> options =
                codeReadingService.createSelectionOptions(
                        LESSON_ID,
                        progress
                );

        assertEquals(1, options.size());
        assertTrue(options.stream().anyMatch(option ->
                option.id().equals("close-class")
        ));
    }

    @Test
    void Part別分岐なしでコード表示行を生成できる() {
        LessonProgress progress = new LessonProgress(
                LESSON_ID,
                "string-array"
        );
        CodeReadingPart part = new CodeReadingPartService(
                new CodeReadingLessonCatalog()
        ).getPart(LESSON_ID, "part-2");

        var line = codeReadingService.createCodeLines(
                LESSON_ID,
                progress,
                part
        ).get(0);

        assertEquals(7, line.tokens().size());
        assertEquals("(", line.tokens().get(4).prefix());
        assertEquals(")", line.tokens().get(5).suffix());
        assertEquals("", line.trailingCode());
    }
}
