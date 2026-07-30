package com.javalink.service;

import com.javalink.model.CodeReadingItem;
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

    private static final String LESSON_ID = "main-method-basic";

    private LessonService lessonService;
    private CodeReadingService codeReadingService;

    @BeforeEach
    void setUp() {
        QuizService quizService = new QuizService();
        lessonService = new LessonService(quizService);
        codeReadingService = new CodeReadingService(
                lessonService,
                quizService
        );
    }

    @Test
    void 六つの用語と正解の意味を登録順に返す() {
        LessonProgress progress = new LessonProgress(
                LESSON_ID,
                "public"
        );

        List<CodeReadingItem> items =
                codeReadingService.createItems(LESSON_ID, progress);

        assertEquals(
                List.of(
                        "public",
                        "static",
                        "void",
                        "main",
                        "String[]",
                        "args"
                ),
                items.stream().map(CodeReadingItem::keyword).toList()
        );
        assertEquals(
                List.of(
                        "外から使える",
                        "インスタンスを作らなくても使える",
                        "戻り値を返さない",
                        "プログラム開始メソッド",
                        "文字列の配列",
                        "受け取った値の名前"
                ),
                items.stream().map(CodeReadingItem::meaning).toList()
        );
    }

    @Test
    void 進捗に合わせて完了済みと現在の用語を示す() {
        LessonProgress progress = new LessonProgress(
                LESSON_ID,
                "public"
        );
        progress.completeStep("public");
        progress.setCurrentStepId("static");

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
                "public"
        );

        List<CodeReadingItem> items =
                codeReadingService.createItems(LESSON_ID, progress);

        assertEquals("アクセス修飾子", items.get(0).roleLabel());
        assertEquals(
                List.of(
                        "外から使えます。",
                        "他のクラスから呼び出せます。"
                ),
                items.get(0).explanations()
        );
        assertEquals("引数名", items.get(5).roleLabel());
    }

    @Test
    void 選択場所には現在の正解と別の未完了カードを返す() {
        LessonProgress progress = new LessonProgress(
                LESSON_ID,
                "public"
        );

        List<QuizOption> options =
                codeReadingService.createSelectionOptions(
                        LESSON_ID,
                        progress
                );

        assertEquals(2, options.size());
        assertTrue(options.stream().anyMatch(option ->
                option.id().equals("accessible")
        ));
    }

    @Test
    void 不正解後も選んだ誤答カードを選択場所へ残す() {
        LessonProgress progress = new LessonProgress(
                LESSON_ID,
                "public"
        );
        progress.setAnswered(true);
        progress.setCorrect(false);
        progress.setSelectedOptionId("without-instance");

        List<QuizOption> options =
                codeReadingService.createSelectionOptions(
                        LESSON_ID,
                        progress
                );

        assertEquals(
                java.util.Set.of("accessible", "without-instance"),
                options.stream()
                        .map(QuizOption::id)
                        .collect(java.util.stream.Collectors.toSet())
        );
    }

    @Test
    void 最後の未完了カードだけなら一択で返す() {
        LessonProgress progress = new LessonProgress(
                LESSON_ID,
                "public"
        );
        for (String stepId : List.of(
                "public",
                "static",
                "void",
                "main",
                "string-array"
        )) {
            progress.completeStep(stepId);
        }
        progress.setCurrentStepId("args");

        List<QuizOption> options =
                codeReadingService.createSelectionOptions(
                        LESSON_ID,
                        progress
                );

        assertEquals(1, options.size());
        assertEquals("argument-variable", options.get(0).id());
    }
}
