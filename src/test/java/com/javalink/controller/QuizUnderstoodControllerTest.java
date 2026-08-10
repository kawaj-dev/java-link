package com.javalink.controller;

import com.javalink.model.CodeReadingPhase;
import com.javalink.model.LessonProgress;
import com.javalink.service.CodeReadingCourseService;
import com.javalink.service.CodeReadingFlowService;
import com.javalink.service.CodeReadingLessonCatalog;
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
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class QuizUnderstoodControllerTest {

    private static final String LESSON_ID =
            LessonService.HELLO_PROGRAM_LESSON_ID;

    private final MockMvc mockMvc;

    @Autowired
    private CodeReadingCourseService courseService;

    @Autowired
    private LessonProgressService progressService;

    @Autowired
    private CodeReadingFlowService flowService;

    @Autowired
    private CodeReadingLessonCatalog lessonCatalog;

    QuizUnderstoodControllerTest(@Autowired WebApplicationContext context) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void learningHeaderShowsPartUnderstoodActionInsteadOfNumericProgress()
            throws Exception {
        MockHttpSession session = new MockHttpSession();
        courseService.startLearning(session, LESSON_ID);

        mockMvc.perform(get("/quiz").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(
                        "理解済み　次のPartへ"
                )))
                .andExpect(content().string(containsString(
                        "/quiz/part/understood"
                )))
                .andExpect(content().string(not(containsString(
                        "data-part-progress"
                ))))
                .andExpect(content().string(not(containsString(
                        "完了 0 / 4"
                ))));
    }

    @Test
    void understoodAtFirstStepCompletesPartAndMovesToNextPart()
            throws Exception {
        MockHttpSession session = new MockHttpSession();
        courseService.startLearning(session, LESSON_ID);

        postUnderstood(session);

        LessonProgress progress = progressService.getProgress(session, LESSON_ID);
        assertEquals("main-public", progress.getCurrentStepId());
        assertPartOneCompleted(progress);

        mockMvc.perform(get("/quiz").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Part 2 / 4")));
    }

    @Test
    void understoodInMiddleCompletesRemainingStepsInsteadOfMovingOneStep()
            throws Exception {
        MockHttpSession session = new MockHttpSession();
        courseService.startLearning(session, LESSON_ID);
        answerNormallyAndMove(session);
        answerNormallyAndMove(session);
        assertEquals(
                "class-name",
                progressService.getProgress(session, LESSON_ID).getCurrentStepId()
        );

        postUnderstood(session);

        LessonProgress progress = progressService.getProgress(session, LESSON_ID);
        assertEquals("main-public", progress.getCurrentStepId());
        assertPartOneCompleted(progress);
    }

    @Test
    void understoodInPartTwoMovesToPartThreeAndKeepsAllBulbsCompleted()
            throws Exception {
        MockHttpSession session = new MockHttpSession();
        courseService.startLearning(session, LESSON_ID);
        postUnderstood(session);
        answerNormallyAndMove(session);
        answerNormallyAndMove(session);

        postUnderstood(session);

        LessonProgress progress = progressService.getProgress(session, LESSON_ID);
        assertEquals("print-command", progress.getCurrentStepId());
        assertEquals(11, progress.getCompletedStepIds().size());
        assertTrue(lessonCatalog.getDefinition(LESSON_ID).parts().get(1)
                .stepIds().stream()
                .allMatch(progress.getCompletedStepIds()::contains));
    }

    @Test
    void understoodInLastPartUsesExistingSummaryTransition()
            throws Exception {
        MockHttpSession session = new MockHttpSession();
        courseService.startLearning(session, LESSON_ID);

        postUnderstood(session);
        postUnderstood(session);
        postUnderstood(session);
        postUnderstood(session);

        LessonProgress progress = progressService.getProgress(session, LESSON_ID);
        assertEquals(16, progress.getCompletedStepIds().size());
        assertEquals(
                CodeReadingPhase.SUMMARY,
                flowService.getState(session, LESSON_ID).phase()
        );
    }

    private void postUnderstood(MockHttpSession session) throws Exception {
        mockMvc.perform(post("/quiz/part/understood").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/quiz"));
    }

    private void answerNormallyAndMove(MockHttpSession session) {
        LessonProgress progress = progressService.getProgress(session, LESSON_ID);
        String correctCardId = lessonCatalog.getDefinition(LESSON_ID)
                .getStep(progress.getCurrentStepId())
                .correctCard()
                .id();
        courseService.answerCurrentItem(session, LESSON_ID, correctCardId);
        courseService.moveToNextItem(session, LESSON_ID);
    }

    private void assertPartOneCompleted(LessonProgress progress) {
        assertEquals(4, progress.getCompletedStepIds().size());
        assertTrue(lessonCatalog.getDefinition(LESSON_ID).parts().get(0)
                .stepIds().stream()
                .allMatch(progress.getCompletedStepIds()::contains));
    }
}
