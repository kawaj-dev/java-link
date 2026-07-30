package com.javalink.service;

import com.javalink.model.Lesson;
import com.javalink.model.LessonProgress;
import com.javalink.model.LessonStep;
import com.javalink.model.QuizOption;
import com.javalink.model.QuizQuestion;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * 回答を判定し、教材の学習進捗を更新します。
 */
@Service
public class LessonEngine {

    private final LessonService lessonService;
    private final LessonProgressService lessonProgressService;
    private final QuizService quizService;

    public LessonEngine(
            LessonService lessonService,
            LessonProgressService lessonProgressService,
            QuizService quizService
    ) {
        this.lessonService = lessonService;
        this.lessonProgressService = lessonProgressService;
        this.quizService = quizService;
    }

    /**
     * セッションで現在学習中のステップへ回答します。
     *
     * @param session          HTTPセッション
     * @param lessonId         教材ID
     * @param selectedOptionId 選択した回答ID
     * @return 回答したステップ、判定結果、更新後進捗、次の必須ステップ
     */
    public AnswerResult answerCurrentStep(
            HttpSession session,
            String lessonId,
            String selectedOptionId
    ) {
        Objects.requireNonNull(
                selectedOptionId,
                "selectedOptionId must not be null"
        );

        Lesson lesson = lessonService.getLesson(lessonId);
        LessonProgress progress =
                lessonProgressService.getProgress(session, lessonId);
        LessonStep currentStep =
                lessonService.getStep(lessonId, progress.getCurrentStepId());
        QuizQuestion question = currentStep.question();

        // 完了済み問題は復習表示専用です。直接POSTされても進捗を変更しません。
        if (progress.isStepCompleted(currentStep.id())) {
            return new AnswerResult(
                    currentStep,
                    progress,
                    true,
                    findNextRequiredStep(lesson, currentStep),
                    quizService.getCorrectOption(question)
            );
        }

        boolean correct = quizService.isCorrect(question, selectedOptionId);

        progress.setSelectedOptionId(selectedOptionId);
        progress.setAnswered(true);
        progress.setCorrect(correct);

        if (correct) {
            progress.completeStep(currentStep.id());
            progress.setCompleted(
                    areAllRequiredStepsCompleted(lesson, progress)
            );
        }

        /*
         * 回答に伴うすべての変更が終わった状態を、最後に1回保存します。
         */
        lessonProgressService.saveProgress(session, lessonId, progress);

        Optional<LessonStep> nextStep = correct
                ? findNextRequiredStep(lesson, currentStep)
                : Optional.empty();

        return new AnswerResult(
                currentStep,
                progress,
                correct,
                nextStep,
                quizService.getCorrectOption(question)
        );
    }

    /**
     * 意味カードを現在のJava用語へ対応付けます。
     * 正解した場合は完了を保存し、次の用語へ自動で進みます。
     */
    public AnswerResult placeMeaningCard(
            HttpSession session,
            String lessonId,
            String selectedOptionId
    ) {
        AnswerResult result =
                answerCurrentStep(session, lessonId, selectedOptionId);

        if (!result.correct() || result.nextStep().isEmpty()) {
            return result;
        }

        LessonProgress progress = result.progress();
        progress.setCurrentStepId(result.nextStep().get().id());
        resetAnswerState(progress);
        lessonProgressService.saveProgress(session, lessonId, progress);
        return result;
    }

    /**
     * 正解を確認した後、次の必須ステップへ移動します。
     * 正解済みでない場合や最終ステップの場合は現在位置を維持します。
     */
    public LessonProgress moveToNextStep(
            HttpSession session,
            String lessonId
    ) {
        Lesson lesson = lessonService.getLesson(lessonId);
        LessonProgress progress =
                lessonProgressService.getProgress(session, lessonId);
        LessonStep currentStep =
                lessonService.getStep(lessonId, progress.getCurrentStepId());

        boolean reviewMode = progress.isStepCompleted(currentStep.id())
                && !progress.isAnswered();
        boolean canMove = reviewMode
                || (progress.isAnswered()
                && progress.isCorrect()
                && progress.isStepCompleted(currentStep.id()));

        if (!canMove) {
            return progress;
        }

        Optional<LessonStep> nextStep =
                findNextRequiredStep(lesson, currentStep);
        if (nextStep.isEmpty()) {
            return progress;
        }

        progress.setCurrentStepId(nextStep.get().id());
        resetAnswerState(progress);
        lessonProgressService.saveProgress(session, lessonId, progress);
        return progress;
    }

    /**
     * 登録順で1つ前の必須ステップへ移動します。
     * 完了状況や教材完了・実行状態は変更しません。
     */
    public LessonProgress moveToPreviousStep(
            HttpSession session,
            String lessonId
    ) {
        Lesson lesson = lessonService.getLesson(lessonId);
        LessonProgress progress =
                lessonProgressService.getProgress(session, lessonId);
        LessonStep currentStep =
                lessonService.getStep(lessonId, progress.getCurrentStepId());
        Optional<LessonStep> previousStep =
                findPreviousRequiredStep(lesson, currentStep);

        if (previousStep.isEmpty()) {
            return progress;
        }

        progress.setCurrentStepId(previousStep.get().id());
        resetAnswerState(progress);
        lessonProgressService.saveProgress(session, lessonId, progress);
        return progress;
    }

    /**
     * 現在画面の前後移動と復習表示に必要な状態を返します。
     */
    public NavigationState getNavigationState(
            HttpSession session,
            String lessonId
    ) {
        Lesson lesson = lessonService.getLesson(lessonId);
        LessonProgress progress =
                lessonProgressService.getProgress(session, lessonId);
        LessonStep currentStep =
                lessonService.getStep(lessonId, progress.getCurrentStepId());
        Optional<LessonStep> previousStep =
                findPreviousRequiredStep(lesson, currentStep);
        Optional<LessonStep> nextStep =
                findNextRequiredStep(lesson, currentStep);
        boolean reviewMode = progress.isStepCompleted(currentStep.id())
                && !progress.isAnswered();
        boolean normalNextAllowed = progress.isAnswered()
                && progress.isCorrect()
                && progress.isStepCompleted(currentStep.id());

        return new NavigationState(
                previousStep.isPresent(),
                nextStep.isPresent(),
                reviewMode,
                nextStep.isPresent()
                        && (reviewMode || normalNextAllowed),
                quizService.getCorrectOption(currentStep.question())
        );
    }

    private Optional<LessonStep> findPreviousRequiredStep(
            Lesson lesson,
            LessonStep currentStep
    ) {
        int currentIndex = lesson.steps().indexOf(currentStep);

        for (int index = currentIndex - 1; index >= 0; index--) {
            LessonStep step = lesson.steps().get(index);
            if (step.required()) {
                return Optional.of(step);
            }
        }
        return Optional.empty();
    }

    private void resetAnswerState(LessonProgress progress) {
        progress.setSelectedOptionId("");
        progress.setAnswered(false);
        progress.setCorrect(false);
    }

    /**
     * 現在位置より後にある、次の必須ステップを探します。
     */
    private Optional<LessonStep> findNextRequiredStep(
            Lesson lesson,
            LessonStep currentStep
    ) {
        int currentIndex = lesson.steps().indexOf(currentStep);

        return lesson.steps().stream()
                .skip(currentIndex + 1L)
                .filter(LessonStep::required)
                .findFirst();
    }

    /**
     * 全必須stepIdが完了済みSetに含まれるかを確認します。
     */
    private boolean areAllRequiredStepsCompleted(
            Lesson lesson,
            LessonProgress progress
    ) {
        List<LessonStep> requiredSteps = lesson.steps().stream()
                .filter(LessonStep::required)
                .toList();
        Set<String> completedStepIds = progress.getCompletedStepIds();

        return !requiredSteps.isEmpty()
                && requiredSteps.stream()
                .allMatch(step -> completedStepIds.contains(step.id()));
    }

    /**
     * 回答後にControllerが画面へ渡すための処理結果です。
     *
     * @param answeredStep 回答したステップ
     * @param progress     更新後の進捗
     * @param correct      正解か
     * @param nextStep     正解時の次の必須ステップ
     * @param correctOption 問題に登録されている正解選択肢
     */
    public record AnswerResult(
            LessonStep answeredStep,
            LessonProgress progress,
            boolean correct,
            Optional<LessonStep> nextStep,
            QuizOption correctOption
    ) {

        public AnswerResult {
            Objects.requireNonNull(
                    answeredStep,
                    "answeredStep must not be null"
            );
            Objects.requireNonNull(progress, "progress must not be null");
            Objects.requireNonNull(nextStep, "nextStep must not be null");
            Objects.requireNonNull(
                    correctOption,
                    "correctOption must not be null"
            );
        }
    }

    /**
     * Controllerと画面へ渡す前後移動・復習状態です。
     */
    public record NavigationState(
            boolean hasPreviousStep,
            boolean hasNextStep,
            boolean reviewMode,
            boolean canMoveNext,
            QuizOption correctOption
    ) {

        public NavigationState {
            Objects.requireNonNull(
                    correctOption,
                    "correctOption must not be null"
            );
        }
    }
}
