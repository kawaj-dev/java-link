package com.javalink.service;

import com.javalink.model.CodeReadingFlowState;
import com.javalink.model.CodeReadingPhase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CodeReadingFlowServiceTest {

    private static final String LESSON_ID = "hello-program-reading";

    private MockHttpSession session;
    private CodeReadingFlowService service;

    @BeforeEach
    void setUp() {
        LessonService lessonService = mock(LessonService.class);
        when(lessonService.getLesson(LESSON_ID)).thenReturn(mock(
                com.javalink.model.Lesson.class
        ));
        when(lessonService.getLesson("missing-lesson"))
                .thenThrow(new IllegalArgumentException(
                        "教材が見つかりません。lessonId: missing-lesson"
                ));
        session = new MockHttpSession();
        service = new CodeReadingFlowService(lessonService);
    }

    @Test
    void 初回は導入画面でセッションへ保存する() {
        CodeReadingFlowState state = service.getState(session, LESSON_ID);

        assertEquals(CodeReadingPhase.INTRO, state.phase());
        @SuppressWarnings("unchecked")
        Map<String, CodeReadingFlowState> states =
                (Map<String, CodeReadingFlowState>) session.getAttribute(
                        CodeReadingFlowService.CODE_READING_FLOW_MAP
                );
        assertEquals(state, states.get(LESSON_ID));
    }

    @Test
    void スタートすると学習画面になる() {
        assertEquals(
                CodeReadingPhase.LEARNING,
                service.startLearning(session, LESSON_ID).phase()
        );
        assertEquals(
                CodeReadingPhase.LEARNING,
                service.getState(session, LESSON_ID).phase()
        );
    }

    @Test
    void 全Part終了後はまとめ画面になる() {
        assertEquals(
                CodeReadingPhase.SUMMARY,
                service.showSummary(session, LESSON_ID).phase()
        );
    }

    @Test
    void リセットすると導入画面へ戻る() {
        service.startLearning(session, LESSON_ID);

        CodeReadingFlowState reset = service.reset(session, LESSON_ID);

        assertEquals(CodeReadingPhase.INTRO, reset.phase());
        assertEquals(
                CodeReadingPhase.INTRO,
                service.getState(session, LESSON_ID).phase()
        );
    }

    @Test
    void 存在しない教材では分かりやすい例外になる() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.getState(session, "missing-lesson")
        );

        assertTrue(exception.getMessage().contains("missing-lesson"));
    }
}
