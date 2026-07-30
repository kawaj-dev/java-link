package com.javalink.service;

import com.javalink.model.Lesson;
import com.javalink.model.LessonProgress;
import com.javalink.model.LessonStep;
import com.javalink.model.LessonViewModel;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * 教材とセッション進捗から、画面表示用のViewModelを組み立てます。
 */
@Service
public class LessonViewModelService {

    private final LessonService lessonService;
    private final LessonProgressService lessonProgressService;

    public LessonViewModelService(
            LessonService lessonService,
            LessonProgressService lessonProgressService
    ) {
        this.lessonService = lessonService;
        this.lessonProgressService = lessonProgressService;
    }

    /**
     * 指定教材の現在状態を画面表示用にまとめます。
     *
     * @param session  HTTPセッション
     * @param lessonId 教材ID
     * @return 教材画面用ViewModel
     */
    public LessonViewModel createViewModel(
            HttpSession session,
            String lessonId
    ) {
        Lesson lesson = lessonService.getLesson(lessonId);
        LessonProgress progress =
                lessonProgressService.getProgress(session, lessonId);
        LessonStep currentStep =
                lessonService.getStep(lessonId, progress.getCurrentStepId());

        List<LessonStep> requiredSteps = lesson.steps().stream()
                .filter(LessonStep::required)
                .toList();
        Set<String> completedStepIds = progress.getCompletedStepIds();

        int totalCount = requiredSteps.size();
        int completedCount = (int) requiredSteps.stream()
                .filter(step -> completedStepIds.contains(step.id()))
                .count();

        /*
         * 小数部分は四捨五入します。
         * 6問中1問なら16.6...%を17%と表示し、進み具合を分かりやすくします。
         */
        int progressPercent = totalCount == 0
                ? 0
                : (int) Math.round(completedCount * 100.0 / totalCount);

        /*
         * 件数だけではなく、すべての必須stepIdが完了済みSetに
         * 含まれていることを確認します。
         */
        boolean codeComplete = totalCount > 0
                && requiredSteps.stream()
                .allMatch(step -> completedStepIds.contains(step.id()));
        boolean runEnabled = codeComplete;
        boolean consoleVisible = progress.isProgramExecuted();

        return new LessonViewModel(
                lesson,
                currentStep,
                progress,
                completedCount,
                totalCount,
                progressPercent,
                codeComplete,
                runEnabled,
                consoleVisible
        );
    }
}
