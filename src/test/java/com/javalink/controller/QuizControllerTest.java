package com.javalink.controller;

import com.javalink.model.CodeReadingPageViewModel;
import com.javalink.model.CodeReadingPhase;
import com.javalink.model.LessonProgress;
import com.javalink.service.CodeReadingCourseService;
import com.javalink.service.LessonProgressService;
import com.javalink.service.LessonService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
class QuizControllerTest {

    private static final String LESSON_ID =
            LessonService.HELLO_PROGRAM_LESSON_ID;

    private final MockMvc mockMvc;

    @Autowired
    private CodeReadingCourseService courseService;

    @Autowired
    private LessonProgressService progressService;

    QuizControllerTest(@Autowired WebApplicationContext context) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void 初回GETは導入画面と完成コードだけを表示する() throws Exception {
        mockMvc.perform(get("/quiz"))
                .andExpect(status().isOk())
                .andExpect(view().name("quiz"))
                .andExpect(model().attribute("phase", CodeReadingPhase.INTRO))
                .andExpect(model().attributeExists("codeReadingPage"))
                .andExpect(content().string(containsString("完成コード")))
                .andExpect(content().string(containsString("Hello.java")))
                .andExpect(content().string(containsString("quiz-intro-code")))
                .andExpect(content().string(containsString("public class Main {\n    public static void main(String[] args) {\n        System.out.println(\"Hello\");\n    }\n}")))
                .andExpect(content().string(containsString("スタート")));
    }

    @Test
    void スタートでPart1の先頭へ進みリダイレクトする() throws Exception {
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/quiz/start").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/quiz"));

        mockMvc.perform(get("/quiz").session(session))
                .andExpect(status().isOk())
                .andExpect(model().attribute("phase", CodeReadingPhase.LEARNING))
                .andExpect(model().attributeExists("lessonViewModel"))
                .andExpect(content().string(containsString("Part 1 / 5")))
                .andExpect(content().string(containsString("クラスを作る")));

        assertEquals(
                "class-public",
                progressService.getProgress(session, LESSON_ID)
                        .getCurrentStepId()
        );
    }

    @Test
    void 正解すると同じPart内の次stepへ自動進行する() throws Exception {
        MockHttpSession session = new MockHttpSession();
        courseService.startLearning(session, LESSON_ID);

        mockMvc.perform(post("/quiz/answer")
                        .session(session)
                        .param("selectedOption", "accessible"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/quiz"));

        LessonProgress progress = progressService.getProgress(session, LESSON_ID);
        assertEquals("class-keyword", progress.getCurrentStepId());
        assertTrue(progress.isStepCompleted("class-public"));
        assertFalse(progress.isAnswered());
    }

    @Test
    void 不正解では同じstepに留まる() throws Exception {
        MockHttpSession session = new MockHttpSession();
        courseService.startLearning(session, LESSON_ID);

        mockMvc.perform(post("/quiz/answer")
                        .session(session)
                        .param("selectedOption", "no-return"))
                .andExpect(status().is3xxRedirection());

        LessonProgress progress = progressService.getProgress(session, LESSON_ID);
        assertEquals("class-public", progress.getCurrentStepId());
        assertTrue(progress.isAnswered());
        assertFalse(progress.isCorrect());
    }

    @Test
    void Part最後の正解後は同じPartに留まる() {
        MockHttpSession session = new MockHttpSession();
        completePart1(session);

        LessonProgress progress = progressService.getProgress(session, LESSON_ID);
        assertEquals("class-open", progress.getCurrentStepId());
        assertTrue(progress.isStepCompleted("class-open"));
        assertTrue(progress.isAnswered());
        assertTrue(progress.isCorrect());
    }

    @Test
    void 完了したPartだけ次のPartへ進める() throws Exception {
        MockHttpSession session = new MockHttpSession();
        completePart1(session);

        mockMvc.perform(post("/quiz/part/next").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/quiz"));

        assertEquals(
                "main-public",
                progressService.getProgress(session, LESSON_ID)
                        .getCurrentStepId()
        );
    }

    @Test
    void 未完了では次Partへ進まない() throws Exception {
        MockHttpSession session = new MockHttpSession();
        courseService.startLearning(session, LESSON_ID);

        mockMvc.perform(post("/quiz/part/next").session(session))
                .andExpect(status().is3xxRedirection());

        assertEquals(
                "class-public",
                progressService.getProgress(session, LESSON_ID)
                        .getCurrentStepId()
        );
    }

    @Test
    void 最終Part完了後に次のコードへ進むとまとめ画面になる() throws Exception {
        MockHttpSession session = new MockHttpSession();
        completeAllParts(session);

        assertEquals(
                "class-close",
                progressService.getProgress(session, LESSON_ID)
                        .getCurrentStepId()
        );

        mockMvc.perform(post("/quiz/part/next").session(session))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get("/quiz").session(session))
                .andExpect(status().isOk())
                .andExpect(model().attribute("phase", CodeReadingPhase.SUMMARY))
                .andExpect(content().string(containsString("今日読めるようになったコード")))
                .andExpect(content().string(containsString("Helloと表示して改行する")));
    }

    @Test
    void 更新後もPart完了待機状態をセッションから復元する() throws Exception {
        MockHttpSession session = new MockHttpSession();
        completePart1(session);

        mockMvc.perform(get("/quiz").session(session))
                .andExpect(status().isOk())
                .andExpect(model().attribute("phase", CodeReadingPhase.LEARNING))
                .andExpect(content().string(containsString("次のコードへ")));
    }

    @Test
    void リセットで問題進捗と画面フェーズが導入へ戻る() throws Exception {
        MockHttpSession session = new MockHttpSession();
        completePart1(session);

        mockMvc.perform(post("/quiz/reset").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/quiz"));

        mockMvc.perform(get("/quiz").session(session))
                .andExpect(model().attribute("phase", CodeReadingPhase.INTRO));
        LessonProgress progress = progressService.getProgress(session, LESSON_ID);
        assertEquals("class-public", progress.getCurrentStepId());
        assertTrue(progress.getCompletedStepIds().isEmpty());
    }

    private void completePart1(MockHttpSession session) {
        courseService.startLearning(session, LESSON_ID);
        courseService.answerCurrentItem(session, LESSON_ID, "accessible");
        courseService.answerCurrentItem(session, LESSON_ID, "declare-class");
        courseService.answerCurrentItem(session, LESSON_ID, "main-class-name");
        courseService.answerCurrentItem(session, LESSON_ID, "block-start");
    }

    private void completeAllParts(MockHttpSession session) {
        completePart1(session);
        courseService.moveToNextPart(session, LESSON_ID);

        answerAll(session, "accessible", "without-instance", "no-return", "program-entry");
        courseService.moveToNextPart(session, LESSON_ID);

        answerAll(session, "multiple-strings", "argument-variable", "block-start");
        courseService.moveToNextPart(session, LESSON_ID);

        answerAll(session, "display-and-newline", "display-text", "command-end");
        courseService.moveToNextPart(session, LESSON_ID);

        answerAll(session, "close-main", "close-class");
    }

    private void answerAll(MockHttpSession session, String... optionIds) {
        for (String optionId : optionIds) {
            courseService.answerCurrentItem(session, LESSON_ID, optionId);
        }
    }
}
