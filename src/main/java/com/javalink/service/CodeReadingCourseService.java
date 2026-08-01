package com.javalink.service;

import com.javalink.model.CodeReadingPart;
import com.javalink.model.CodeReadingPhase;
import com.javalink.model.CodeReadingAnswerResponse;
import com.javalink.model.LessonProgress;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

/**
 * 回答後の自動進行とPart間の遷移を管理します。
 */
@Service
public class CodeReadingCourseService {

    private final LessonEngine lessonEngine;
    private final LessonProgressService lessonProgressService;
    private final CodeReadingPartService partService;
    private final CodeReadingFlowService flowService;

    public CodeReadingCourseService(
            LessonEngine lessonEngine,
            LessonProgressService lessonProgressService,
            CodeReadingPartService partService,
            CodeReadingFlowService flowService
    ) {
        this.lessonEngine = lessonEngine;
        this.lessonProgressService = lessonProgressService;
        this.partService = partService;
        this.flowService = flowService;
    }

    /** 導入画面からPart 1の先頭へ移ります。 */
    public LessonProgress startLearning(
            HttpSession session,
            String lessonId
    ) {
        LessonProgress progress =
                lessonProgressService.resetProgress(session, lessonId);
        flowService.startLearning(session, lessonId);
        return progress;
    }

    /** 回答状態を保存し、正解後も確認のため現在stepに留まります。 */
    public LessonEngine.AnswerResult answerCurrentItem(
            HttpSession session,
            String lessonId,
            String selectedOptionId
    ) {
        return lessonEngine.answerCurrentStep(
                session,
                lessonId,
                selectedOptionId
        );
    }

    /** 正解確認後、同じPart内に次項目がある場合だけ移ります。 */
    public LessonProgress moveToNextItem(
            HttpSession session,
            String lessonId
    ) {
        LessonProgress progress =
                lessonProgressService.getProgress(session, lessonId);
        CodeReadingPart part =
                partService.getPartForStep(
                        lessonId,
                        progress.getCurrentStepId()
                );

        if (!progress.isAnswered()
                || !progress.isCorrect()
                || !progress.isStepCompleted(progress.getCurrentStepId())) {
            return progress;
        }

        boolean partCompleted = part.stepIds().stream()
                .allMatch(progress.getCompletedStepIds()::contains);
        if (partCompleted) {
            return progress;
        }

        return lessonEngine.moveToNextStep(session, lessonId);
    }

    /**
     * 画面演出用に、サーバーで判定・保存した結果を返します。
     * 正解判定そのものは既存のLessonEngineへ任せます。
     */
    public CodeReadingAnswerResponse answerCurrentItemForAnimation(
            HttpSession session,
            String lessonId,
            String selectedOptionId
    ) {
        LessonProgress before =
                lessonProgressService.getProgress(session, lessonId);
        CodeReadingPart answeredPart =
                partService.getPartForStep(
                        lessonId,
                        before.getCurrentStepId()
                );
        LessonEngine.AnswerResult result = answerCurrentItem(
                session,
                lessonId,
                selectedOptionId
        );
        int completedCount = (int) answeredPart.stepIds().stream()
                .filter(result.progress().getCompletedStepIds()::contains)
                .count();
        boolean partCompleted =
                completedCount == answeredPart.stepIds().size();

        return new CodeReadingAnswerResponse(
                result.correct(),
                result.answeredStep().id(),
                result.correctOption().text(),
                completedCount,
                partCompleted
        );
    }

    /**
     * 現在Partが完了している場合だけ次のPartへ進みます。
     * 最終Partでは現在位置を保ったままSUMMARYへ切り替えます。
     */
    public PartTransitionResult moveToNextPart(
            HttpSession session,
            String lessonId
    ) {
        LessonProgress progress =
                lessonProgressService.getProgress(session, lessonId);
        CodeReadingPart part =
                partService.getPartForStep(
                        lessonId,
                        progress.getCurrentStepId()
                );
        boolean completed = part.stepIds().stream()
                .allMatch(progress.getCompletedStepIds()::contains);

        if (flowService.getState(session, lessonId).phase()
                != CodeReadingPhase.LEARNING || !completed) {
            return new PartTransitionResult(progress, false, false);
        }

        if (partService.isLastPart(lessonId, part)) {
            flowService.showSummary(session, lessonId);
            return new PartTransitionResult(progress, true, true);
        }

        LessonProgress moved = lessonEngine.moveToNextStep(session, lessonId);
        return new PartTransitionResult(moved, true, false);
    }

    /** 問題進捗と画面フェーズを両方とも初期状態へ戻します。 */
    public LessonProgress reset(
            HttpSession session,
            String lessonId
    ) {
        LessonProgress progress =
                lessonProgressService.resetProgress(session, lessonId);
        flowService.reset(session, lessonId);
        return progress;
    }

    public record PartTransitionResult(
            LessonProgress progress,
            boolean moved,
            boolean summary
    ) {
    }
}
