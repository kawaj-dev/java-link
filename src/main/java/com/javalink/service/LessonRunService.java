package com.javalink.service;

import com.javalink.model.Lesson;
import com.javalink.model.LessonProgress;
import com.javalink.model.LessonStep;
import com.javalink.model.ProgramRunResult;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 完了した教材について、教材側で定義した安全な固定結果を返します。
 */
@Service
public class LessonRunService {

    private static final String SUPPORTED_LESSON_ID = "main-method-basic";
    private static final String HELLO_PROGRAM_LESSON_ID =
            LessonService.HELLO_PROGRAM_LESSON_ID;
    private static final String SUCCESS_OUTPUT = "Program finished successfully.";
    private static final String SUCCESS_MESSAGE = "プログラムを実行しました。";
    private static final String INCOMPLETE_MESSAGE =
            "すべてのステップを完了すると実行できます。";

    private final LessonService lessonService;
    private final LessonProgressService lessonProgressService;

    public LessonRunService(
            LessonService lessonService,
            LessonProgressService lessonProgressService
    ) {
        this.lessonService = lessonService;
        this.lessonProgressService = lessonProgressService;
    }

    /**
     * 教材の完了状態を検証し、安全な疑似実行結果を返します。
     */
    public ProgramRunResult runLesson(
            HttpSession session,
            String lessonId
    ) {
        Lesson lesson = lessonService.getLesson(lessonId);
        validateSupportedLesson(lesson.id());

        LessonProgress progress =
                lessonProgressService.getProgress(session, lessonId);
        List<LessonStep> requiredSteps = lesson.steps().stream()
                .filter(LessonStep::required)
                .toList();
        boolean allRequiredStepsCompleted = !requiredSteps.isEmpty()
                && requiredSteps.stream()
                .allMatch(step ->
                        progress.getCompletedStepIds().contains(step.id())
                );

        if (!allRequiredStepsCompleted) {
            return new ProgramRunResult(
                    lessonId,
                    false,
                    "",
                    INCOMPLETE_MESSAGE
            );
        }

        /*
         * main-method-basic の completeCode は出力処理を持たないため、
         * Javaコードを解析・コンパイルせず、正常終了を示す固定結果を返します。
         */
        progress.setProgramExecuted(true);
        lessonProgressService.saveProgress(session, lessonId, progress);

        String consoleOutput = HELLO_PROGRAM_LESSON_ID.equals(lessonId)
                ? "Hello"
                : SUCCESS_OUTPUT;
        return new ProgramRunResult(
                lessonId,
                true,
                consoleOutput,
                SUCCESS_MESSAGE
        );
    }

    private void validateSupportedLesson(String lessonId) {
        if (!SUPPORTED_LESSON_ID.equals(lessonId)
                && !HELLO_PROGRAM_LESSON_ID.equals(lessonId)) {
            throw new IllegalArgumentException(
                    "この教材はプログラム実行に対応していません。lessonId: "
                            + lessonId
            );
        }
    }
}
