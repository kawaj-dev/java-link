package com.javalink.service;

import com.javalink.model.CodeReadingItem;
import com.javalink.model.CodeReadingPart;
import com.javalink.model.LessonProgress;
import com.javalink.model.QuizOption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

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
                        "外から使える", "クラスを作る", "クラスの名前", "ここから始まる",
                        "外から使える", "インスタンスを作らなくても使える",
                        "戻り値を返さない", "プログラム開始メソッド",
                        "文字列の配列", "受け取った値の名前", "ここから始まる",
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
        assertTrue(items.get(0).explanations().contains(
                "publicを付けると、どこからでも使えるようになります。"
        ));
        assertEquals("引数名", items.get(9).roleLabel());
    }

    @Test
    void Part1の四用語に構造化された詳細説明がある() {
        LessonProgress progress = new LessonProgress(
                LESSON_ID,
                "class-public"
        );

        List<CodeReadingItem> items =
                codeReadingService.createItems(LESSON_ID, progress);

        CodeReadingItem publicItem = items.get(0);
        assertTrue(publicItem.technicalExplanation().contains("access modifier"));
        var accessTable = publicItem.explanationSections().stream()
                .filter(section -> section.layout().equals("table"))
                .findFirst().orElseThrow();
        assertEquals(4, accessTable.entries().size());
        assertTrue(accessTable.entries().stream().anyMatch(entry ->
                entry.label().equals("package-private（何も書かない）")
        ));
        assertTrue(accessTable.entries().stream().anyMatch(entry ->
                entry.label().equals("public") && entry.highlighted()
        ));

        CodeReadingItem classItem = items.get(1);
        assertTrue(classItem.explanationSections().stream().anyMatch(section ->
                section.layout().equals("diagram") && section.title().equals("クラスのイメージ")
        ));
        assertTrue(classItem.explanationSections().stream().anyMatch(section ->
                section.layout().equals("comparison")
                        && section.title().equals("初心者が迷いやすいポイント")
        ));

        CodeReadingItem mainItem = items.get(2);
        assertTrue(mainItem.explanationSections().stream().anyMatch(section ->
                section.layout().equals("examples")
                        && section.entries().stream()
                        .filter(entry -> entry.label().endsWith(".java")).count() == 2
        ));
        assertTrue(mainItem.explanationSections().stream().anyMatch(section ->
                section.layout().equals("comparison")
                        && section.title().equals("初心者が迷いやすいポイント")
        ));

        CodeReadingItem blockItem = items.get(3);
        assertTrue(blockItem.explanationSections().stream()
                .flatMap(section -> section.entries().stream())
                .anyMatch(entry -> entry.before().contains("必ず対応する } で閉じます")));
        assertTrue(blockItem.explanationSections().stream()
                .filter(section -> section.layout().equals("diagram"))
                .flatMap(section -> section.entries().stream())
                .noneMatch(entry -> entry.label().equals("}")));

        assertTrue(items.subList(0, 4).stream().allMatch(item ->
                item.explanationSections().stream().map(section -> section.kind()).toList()
                        .equals(List.of("identity", "rule", "beginner-point"))
        ));
        assertTrue(items.subList(0, 4).stream()
                .flatMap(item -> item.explanationSections().stream())
                .noneMatch(section -> section.kind().equals("takeaway")));
        assertTrue(items.get(4).explanationSections().isEmpty());
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
        assertEquals("}", line.trailingCode());
    }
}
