package com.javalink.service;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.javalink.model.CodeReadingFlowState;
import com.javalink.model.CodeReadingPart;
import com.javalink.model.CodeReadingPhase;
import com.javalink.model.LessonProgress;
import com.javalink.model.LessonStep;
import com.javalink.model.QuizOption;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CodeReadingCourseServiceTest {

    private static final String LESSON_ID = "stage1";

    @Mock
    private LessonEngine lessonEngine;

    @Mock
    private LessonProgressService lessonProgressService;

    @Mock
    private CodeReadingPartService partService;

    @Mock
    private CodeReadingFlowService flowService;

    @Mock
    private CodeReadingLessonCatalog lessonCatalog;

    @Mock
    private LearningProgressSyncService learningProgressSyncService;

    @Mock
    private HttpSession session;

    private CodeReadingCourseService courseService;

    @BeforeEach
    void setUp() {
        courseService = new CodeReadingCourseService(
                lessonEngine,
                lessonProgressService,
                partService,
                flowService,
                lessonCatalog,
                learningProgressSyncService
        );
    }

    @Test
    void 通常Stepで実際に次へ移動した場合は移動後の進捗を同期する() {
        LessonProgress current = mock(LessonProgress.class);
        LessonProgress moved = mock(LessonProgress.class);
        CodeReadingPart part = mock(CodeReadingPart.class);
        when(lessonProgressService.getProgress(session, LESSON_ID))
                .thenReturn(current);
        when(current.getCurrentStepId()).thenReturn("public");
        when(current.isAnswered()).thenReturn(true);
        when(current.isCorrect()).thenReturn(true);
        when(current.isStepCompleted("public")).thenReturn(true);
        when(current.getCompletedStepIds()).thenReturn(Set.of("public"));
        when(partService.getPartForStep(LESSON_ID, "public")).thenReturn(part);
        when(part.stepIds()).thenReturn(List.of("public", "class"));
        when(lessonEngine.moveToNextStep(session, LESSON_ID)).thenReturn(moved);
        when(moved.getCurrentStepId()).thenReturn("class");

        LessonProgress result = courseService.moveToNextItem(session, LESSON_ID);

        assertSame(moved, result);
        verify(learningProgressSyncService).saveIfAuthenticated(session, moved);
    }

    @Test
    void 次のPartへ移動した場合は移動後の進捗を同期する() {
        LessonProgress current = mock(LessonProgress.class);
        LessonProgress moved = mock(LessonProgress.class);
        CodeReadingPart part = mock(CodeReadingPart.class);
        when(lessonProgressService.getProgress(session, LESSON_ID))
                .thenReturn(current);
        when(current.getCurrentStepId()).thenReturn("part1-last");
        when(current.getCompletedStepIds()).thenReturn(Set.of("part1-last"));
        when(partService.getPartForStep(LESSON_ID, "part1-last")).thenReturn(part);
        when(part.stepIds()).thenReturn(List.of("part1-last"));
        when(flowService.getState(session, LESSON_ID)).thenReturn(
                new CodeReadingFlowState(LESSON_ID, CodeReadingPhase.LEARNING)
        );
        when(partService.isLastPart(LESSON_ID, part)).thenReturn(false);
        when(lessonEngine.moveToNextStep(session, LESSON_ID)).thenReturn(moved);
        when(moved.getCurrentStepId()).thenReturn("part2-first");

        CodeReadingCourseService.PartTransitionResult result =
                courseService.moveToNextPart(session, LESSON_ID);

        assertSame(moved, result.progress());
        verify(learningProgressSyncService).saveIfAuthenticated(session, moved);
    }

    @Test
    void 最終Stepを正解して教材完了になった場合は進捗を同期する() {
        LessonProgress completed = mock(LessonProgress.class);
        when(completed.isCompleted()).thenReturn(true);
        LessonEngine.AnswerResult answerResult = answerResult(true, completed);
        when(lessonEngine.answerCurrentStep(session, LESSON_ID, "correct"))
                .thenReturn(answerResult);

        LessonEngine.AnswerResult result = courseService.answerCurrentItem(
                session,
                LESSON_ID,
                "correct"
        );

        assertSame(answerResult, result);
        verify(learningProgressSyncService).saveIfAuthenticated(session, completed);
    }

    @Test
    void 不正解の場合は進捗を同期しない() {
        LessonProgress progress = mock(LessonProgress.class);
        when(lessonEngine.answerCurrentStep(session, LESSON_ID, "incorrect"))
                .thenReturn(answerResult(false, progress));

        courseService.answerCurrentItem(session, LESSON_ID, "incorrect");

        verifyNoInteractions(learningProgressSyncService);
    }

    @Test
    void Step移動条件を満たさない場合は進捗を同期しない() {
        LessonProgress progress = mock(LessonProgress.class);
        CodeReadingPart part = mock(CodeReadingPart.class);
        when(lessonProgressService.getProgress(session, LESSON_ID))
                .thenReturn(progress);
        when(progress.getCurrentStepId()).thenReturn("public");
        when(progress.isAnswered()).thenReturn(false);
        when(partService.getPartForStep(LESSON_ID, "public")).thenReturn(part);

        LessonProgress result = courseService.moveToNextItem(session, LESSON_ID);

        assertSame(progress, result);
        verify(lessonEngine, never()).moveToNextStep(session, LESSON_ID);
        verifyNoInteractions(learningProgressSyncService);
    }

    private LessonEngine.AnswerResult answerResult(
            boolean correct,
            LessonProgress progress
    ) {
        return new LessonEngine.AnswerResult(
                mock(LessonStep.class),
                progress,
                correct,
                Optional.empty(),
                mock(QuizOption.class)
        );
    }
}
