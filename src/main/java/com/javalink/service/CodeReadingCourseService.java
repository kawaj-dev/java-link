package com.javalink.service;

import com.javalink.model.CodeReadingStage;
import com.javalink.model.LessonProgress;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

/**
 * 回答後の自動進行を、現在ステージの中だけに限定します。
 */
@Service
public class CodeReadingCourseService {

    private final LessonEngine lessonEngine;
    private final LessonProgressService lessonProgressService;
    private final CodeReadingService codeReadingService;

    public CodeReadingCourseService(
            LessonEngine lessonEngine,
            LessonProgressService lessonProgressService,
            CodeReadingService codeReadingService
    ) {
        this.lessonEngine = lessonEngine;
        this.lessonProgressService = lessonProgressService;
        this.codeReadingService = codeReadingService;
    }

    /**
     * 正解時は同じステージ内だけ自動で次へ進みます。
     */
    public LessonEngine.AnswerResult answerCurrentItem(
            HttpSession session,
            String lessonId,
            String selectedOptionId
    ) {
        LessonProgress before =
                lessonProgressService.getProgress(session, lessonId);
        CodeReadingStage answeredStage =
                codeReadingService.getStageForStep(before.getCurrentStepId());
        LessonEngine.AnswerResult result =
                lessonEngine.answerCurrentStep(
                        session,
                        lessonId,
                        selectedOptionId
                );

        if (result.correct()
                && result.nextStep().isPresent()
                && answeredStage.stepIds().contains(
                        result.nextStep().get().id()
                )) {
            lessonEngine.moveToNextStep(session, lessonId);
        }
        return result;
    }

    /**
     * ステージを読み終えた場合だけ、次のステージ先頭へ進めます。
     */
    public LessonProgress moveToNextStage(
            HttpSession session,
            String lessonId
    ) {
        LessonProgress progress =
                lessonProgressService.getProgress(session, lessonId);
        CodeReadingStage stage =
                codeReadingService.getStageForStep(
                        progress.getCurrentStepId()
                );

        if (!codeReadingService.isStageCompleted(stage, progress)
                || codeReadingService.isLastStage(stage)) {
            return progress;
        }
        return lessonEngine.moveToNextStep(session, lessonId);
    }
}
