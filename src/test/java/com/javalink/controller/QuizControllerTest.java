package com.javalink.controller;

import com.javalink.model.CodeReadingItem;
import com.javalink.model.CodeReadingStage;
import com.javalink.model.LessonProgress;
import com.javalink.model.LessonViewModel;
import com.javalink.model.QuizOption;
import com.javalink.service.CodeReadingCourseService;
import com.javalink.service.CodeReadingService;
import com.javalink.service.LessonEngine;
import com.javalink.service.LessonProgressService;
import com.javalink.service.LessonRunService;
import com.javalink.service.LessonService;
import com.javalink.service.LessonViewModelService;
import com.javalink.service.QuizService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * 「コードを左から読む」3ステージ画面の連携を確認します。
 */
@WebMvcTest(QuizController.class)
@Import({
        QuizService.class,
        LessonService.class,
        LessonProgressService.class,
        LessonViewModelService.class,
        LessonRunService.class,
        LessonEngine.class,
        CodeReadingService.class,
        CodeReadingCourseService.class
})
class QuizControllerTest {

    private static final String LESSON_ID =
            LessonService.HELLO_PROGRAM_LESSON_ID;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LessonService lessonService;

    @Autowired
    private LessonProgressService lessonProgressService;

    @Autowired
    private CodeReadingService codeReadingService;

    @Test
    void 初期表示は完成コードとStage1を左右ペインへ表示する() throws Exception {
        mockMvc.perform(get("/quiz"))
                .andExpect(status().isOk())
                .andExpect(view().name("quiz"))
                .andExpect(model().attributeExists("lessonViewModel"))
                .andExpect(model().attribute("stageCount", 3))
                .andExpect(model().attribute("stageComplete", false))
                .andExpect(model().attribute("hasNextStage", false))
                .andExpect(content().string(containsString("quiz-workspace")))
                .andExpect(content().string(containsString("quiz-program-pane")))
                .andExpect(content().string(containsString("quiz-learning-pane")))
                .andExpect(content().string(containsString("public class")))
                .andExpect(content().string(containsString("System.out.println")))
                .andExpect(content().string(containsString("Stage 1 / 3")))
                .andExpect(content().string(containsString("Mainクラスを作る")));
    }

    @Test
    void 初期進捗は18項目でclassPublicから始まる() throws Exception {
        mockMvc.perform(get("/quiz"))
                .andExpect(model().attribute("questionNumber", 1))
                .andExpect(model().attribute("questionCount", 18))
                .andExpect(model().attribute("energizedSteps", 0))
                .andExpect(result -> {
                            LessonViewModel actual = (LessonViewModel) result
                                    .getModelAndView()
                                    .getModel()
                                    .get("lessonViewModel");
                            assertEquals("class-public", actual.currentStep().id());
                            assertEquals(0, actual.completedCount());
                            assertFalse(actual.codeComplete());
                            assertFalse(actual.runEnabled());
                });
    }

    @Test
    void Stage1は4項目と二択カードを表示する() throws Exception {
        mockMvc.perform(get("/quiz"))
                .andExpect(model().attribute("readingItems", hasSize(4)))
                .andExpect(model().attribute("shuffledOptions", hasSize(2)))
                .andExpect(content().string(containsString("カード選択場所")))
                .andExpect(content().string(containsString("これから使うカード")))
                .andExpect(content().string(containsString("data-target-step=\"class-public\"")));
    }

    @Test
    void 不正解では現在項目と進捗を変えない() throws Exception {
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/quiz/answer")
                        .session(session)
                        .param("selectedOption", "block-start"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("answered", true))
                .andExpect(model().attribute("correct", false))
                .andExpect(model().attribute("energizedSteps", 0))
                .andExpect(content().string(containsString(
                        "もう一度考えてみましょう。"
                )));

        LessonProgress progress =
                lessonProgressService.getProgress(session, LESSON_ID);
        assertEquals("class-public", progress.getCurrentStepId());
        assertTrue(progress.getCompletedStepIds().isEmpty());
    }

    @Test
    void 正解すると同じステージ内の次項目へ自動進行する() throws Exception {
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/quiz/answer")
                        .session(session)
                        .param("selectedOption", "accessible"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("energizedSteps", 1))
                .andExpect(result -> assertEquals(
                        "class-keyword",
                        ((LessonViewModel) result.getModelAndView()
                                .getModel()
                                .get("lessonViewModel"))
                                .currentStep().id()
                ))
                .andExpect(content().string(containsString("外から使える")));

        LessonProgress progress =
                lessonProgressService.getProgress(session, LESSON_ID);
        assertTrue(progress.getCompletedStepIds().contains("class-public"));
    }

    @Test
    void Stage1完了時は境界で止まり復習と次ステージボタンを表示する()
            throws Exception {
        MockHttpSession session = new MockHttpSession();
        completeCurrentStage(session);

        mockMvc.perform(get("/quiz").session(session))
                .andExpect(model().attribute("stageComplete", true))
                .andExpect(model().attribute("hasNextStage", true))
                .andExpect(model().attribute("energizedSteps", 4))
                .andExpect(content().string(containsString("Stage Complete!")))
                .andExpect(content().string(containsString("次のステージへ →")));

        assertEquals(
                "class-open",
                lessonProgressService.getProgress(session, LESSON_ID)
                        .getCurrentStepId()
        );
    }

    @Test
    void 次のステージ操作でStage2先頭へ進む() throws Exception {
        MockHttpSession session = new MockHttpSession();
        completeCurrentStage(session);

        mockMvc.perform(post("/quiz/stage/next").session(session))
                .andExpect(status().isOk())
                .andExpect(model().attribute("stageComplete", false))
                .andExpect(content().string(containsString("Stage 2 / 3")))
                .andExpect(content().string(containsString(
                        "プログラムの開始地点を作る"
                )));

        assertEquals(
                "main-public",
                lessonProgressService.getProgress(session, LESSON_ID)
                        .getCurrentStepId()
        );
    }

    @Test
    void 未完了で次ステージURLを直接呼んでも進まない() throws Exception {
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/quiz/stage/next").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Stage 1 / 3")));

        assertEquals(
                "class-public",
                lessonProgressService.getProgress(session, LESSON_ID)
                        .getCurrentStepId()
        );
    }

    @Test
    void Stage2とStage3の項目数と順番を維持する() throws Exception {
        MockHttpSession session = new MockHttpSession();
        completeCurrentStage(session);
        mockMvc.perform(post("/quiz/stage/next").session(session));

        mockMvc.perform(get("/quiz").session(session))
                .andExpect(model().attribute("readingItems", hasSize(7)))
                .andExpect(result -> assertKeywords(
                                result.getModelAndView()
                                        .getModel()
                                        .get("readingItems"),
                                "public", "static", "void", "main",
                                "String[]", "args", "{"
                ));

        completeCurrentStage(session);
        mockMvc.perform(post("/quiz/stage/next").session(session));

        mockMvc.perform(get("/quiz").session(session))
                .andExpect(model().attribute("readingItems", hasSize(7)))
                .andExpect(result -> assertKeywords(
                                result.getModelAndView()
                                        .getModel()
                                        .get("readingItems"),
                                "System", ".", "out", ".",
                                "println", "\"Hello\"", ";"
                ));
    }

    @Test
    void 全18項目完了でRunをすぐ有効表示する() throws Exception {
        MockHttpSession session = new MockHttpSession();
        completeLesson(session);

        mockMvc.perform(get("/quiz").session(session))
                .andExpect(model().attribute("completed", true))
                .andExpect(model().attribute("energizedSteps", 18))
                .andExpect(result -> {
                            LessonViewModel actual = (LessonViewModel) result
                                    .getModelAndView()
                                    .getModel()
                                    .get("lessonViewModel");
                            assertEquals(100, actual.progressPercent());
                            assertTrue(actual.codeComplete());
                            assertTrue(actual.runEnabled());
                })
                .andExpect(content().string(containsString("Code Complete!")))
                .andExpect(content().string(not(containsString(
                        "quiz-course-power-link"
                ))))
                .andExpect(content().string(not(containsString(
                        "3つのステージをすべて読むと実行できます。"
                ))));
    }

    @Test
    void 完了前のRunは実行されず完了後はHelloを安全に表示する()
            throws Exception {
        MockHttpSession incomplete = new MockHttpSession();
        mockMvc.perform(post("/quiz/run").session(incomplete))
                .andExpect(status().isOk())
                .andExpect(result -> assertFalse(
                                ((com.javalink.model.ProgramRunResult) result
                                        .getModelAndView()
                                        .getModel()
                                        .get("programRunResult"))
                                        .success()
                ));

        MockHttpSession completed = new MockHttpSession();
        completeLesson(completed);
        mockMvc.perform(post("/quiz/run").session(completed))
                .andExpect(status().isOk())
                .andExpect(mvcResult -> {
                            var runResult =
                                    (com.javalink.model.ProgramRunResult)
                                            mvcResult.getModelAndView()
                                                    .getModel()
                                                    .get("programRunResult");
                            assertTrue(runResult.success());
                            assertEquals("Hello", runResult.consoleOutput());
                })
                .andExpect(content().string(containsString(">Hello</code>")))
                .andExpect(content().string(containsString(
                        "完成プログラムを左から読む"
                )));
    }

    @Test
    void リセットでStage1初期状態へ戻る() throws Exception {
        MockHttpSession session = new MockHttpSession();
        completeCurrentStage(session);

        mockMvc.perform(post("/quiz/reset").session(session))
                .andExpect(status().isOk())
                .andExpect(model().attribute("energizedSteps", 0))
                .andExpect(content().string(containsString("Stage 1 / 3")));

        LessonProgress progress =
                lessonProgressService.getProgress(session, LESSON_ID);
        assertEquals("class-public", progress.getCurrentStepId());
        assertTrue(progress.getCompletedStepIds().isEmpty());
    }

    @Test
    void 図鑑と既存十二用語カードを維持する() throws Exception {
        mockMvc.perform(get("/quiz"))
                .andExpect(content().string(containsString(
                        "data-glossary-term=\"public\""
                )))
                .andExpect(content().string(containsString(
                        "📖 Javaことば図鑑"
                )))
                .andExpect(content().string(containsString(
                        "data-glossary-card-term=\"String[]\""
                )))
                .andExpect(result -> assertEquals(
                        12,
                        countOccurrences(
                                result.getResponse().getContentAsString(),
                                "class=\"java-glossary-card\""
                        )
                ));
    }

    @Test
    void テンプレートにカード飛行と650ミリ秒処理を維持する()
            throws Exception {
        mockMvc.perform(get("/quiz"))
                .andExpect(content().string(containsString(
                        "flyCorrectCard(button)"
                )))
                .andExpect(content().string(containsString(
                        "scale(0.9)"
                )))
                .andExpect(content().string(containsString(
                        "reducedMotion ? 40 : 650"
                )))
                .andExpect(content().string(not(containsString(
                        "positionPowerLink"
                ))));
    }

    private void completeLesson(MockHttpSession session) throws Exception {
        completeCurrentStage(session);
        mockMvc.perform(post("/quiz/stage/next").session(session));
        completeCurrentStage(session);
        mockMvc.perform(post("/quiz/stage/next").session(session));
        completeCurrentStage(session);
    }

    private void completeCurrentStage(MockHttpSession session)
            throws Exception {
        LessonProgress progress =
                lessonProgressService.getProgress(session, LESSON_ID);
        CodeReadingStage stage =
                codeReadingService.getStageForStep(progress.getCurrentStepId());

        for (String stepId : stage.stepIds()) {
            progress = lessonProgressService.getProgress(session, LESSON_ID);
            if (progress.getCompletedStepIds().contains(stepId)) {
                continue;
            }
            assertEquals(stepId, progress.getCurrentStepId());
            String correctOptionId = lessonService
                    .getStep(LESSON_ID, stepId)
                    .question()
                    .correctOptionId();
            mockMvc.perform(post("/quiz/answer")
                    .session(session)
                    .param("selectedOption", correctOptionId));
        }
    }

    @SuppressWarnings("unchecked")
    private static void assertKeywords(Object value, String... expected) {
        List<CodeReadingItem> items = (List<CodeReadingItem>) value;
        assertEquals(
                List.of(expected),
                items.stream().map(CodeReadingItem::keyword).toList()
        );
    }

    private static int countOccurrences(String source, String target) {
        int count = 0;
        int index = 0;
        while ((index = source.indexOf(target, index)) >= 0) {
            count++;
            index += target.length();
        }
        return count;
    }
}
