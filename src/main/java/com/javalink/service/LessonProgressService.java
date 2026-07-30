package com.javalink.service;

import com.javalink.model.LessonProgress;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 教材ごとの学習進捗をHTTPセッションで管理します。
 */
@Service
public class LessonProgressService {

    /**
     * セッション内で教材ごとの進捗Mapを保存する属性名です。
     */
    public static final String LESSON_PROGRESS_MAP = "LESSON_PROGRESS_MAP";

    private final LessonService lessonService;

    public LessonProgressService(LessonService lessonService) {
        this.lessonService = lessonService;
    }

    /**
     * 指定教材の進捗を取得します。
     * まだ進捗がなければ、教材の最初のステップから初期状態を作成します。
     *
     * @param session  HTTPセッション
     * @param lessonId 教材ID
     * @return 教材の学習進捗
     */
    public LessonProgress getProgress(HttpSession session, String lessonId) {
        validateSessionAndLesson(session, lessonId);
        Map<String, LessonProgress> progressMap = getProgressMap(session);

        return progressMap.computeIfAbsent(
                lessonId,
                this::createInitialProgress
        );
    }

    /**
     * 指定教材の進捗をセッションへ保存します。
     *
     * @param session  HTTPセッション
     * @param lessonId 教材ID
     * @param progress 保存する進捗
     */
    public void saveProgress(
            HttpSession session,
            String lessonId,
            LessonProgress progress
    ) {
        validateSessionAndLesson(session, lessonId);
        Objects.requireNonNull(progress, "progress must not be null");

        if (!lessonId.equals(progress.getLessonId())) {
            throw new IllegalArgumentException(
                    "lessonIdと進捗の教材IDが一致しません。lessonId: "
                            + lessonId + ", progress.lessonId: " + progress.getLessonId()
            );
        }

        getProgressMap(session).put(lessonId, progress);
    }

    /**
     * 指定教材の進捗を初期状態へ戻します。
     *
     * @param session  HTTPセッション
     * @param lessonId 教材ID
     * @return リセット後の進捗
     */
    public LessonProgress resetProgress(HttpSession session, String lessonId) {
        validateSessionAndLesson(session, lessonId);
        LessonProgress initialProgress = createInitialProgress(lessonId);
        getProgressMap(session).put(lessonId, initialProgress);
        return initialProgress;
    }

    /**
     * ステップを完了済みとして追加します。
     */
    public void addCompletedStep(
            HttpSession session,
            String lessonId,
            String stepId
    ) {
        lessonService.getStep(lessonId, stepId);
        LessonProgress progress = getProgress(session, lessonId);
        progress.completeStep(stepId);
        saveProgress(session, lessonId, progress);
    }

    /**
     * 現在学習しているステップを更新します。
     */
    public void updateCurrentStep(
            HttpSession session,
            String lessonId,
            String stepId
    ) {
        lessonService.getStep(lessonId, stepId);
        LessonProgress progress = getProgress(session, lessonId);
        progress.setCurrentStepId(stepId);
        saveProgress(session, lessonId, progress);
    }

    /**
     * 選択した回答と正解判定の状態を更新します。
     */
    public void updateAnswerState(
            HttpSession session,
            String lessonId,
            String selectedOptionId,
            boolean answered,
            boolean correct
    ) {
        LessonProgress progress = getProgress(session, lessonId);
        progress.setSelectedOptionId(
                Objects.requireNonNull(
                        selectedOptionId,
                        "selectedOptionId must not be null"
                )
        );
        progress.setAnswered(answered);
        progress.setCorrect(correct);
        saveProgress(session, lessonId, progress);
    }

    /**
     * 教材がCode Completeになったかを更新します。
     */
    public void updateCompleted(
            HttpSession session,
            String lessonId,
            boolean completed
    ) {
        LessonProgress progress = getProgress(session, lessonId);
        progress.setCompleted(completed);
        saveProgress(session, lessonId, progress);
    }

    /**
     * プログラムを実行済みかを更新します。
     */
    public void updateProgramExecuted(
            HttpSession session,
            String lessonId,
            boolean programExecuted
    ) {
        LessonProgress progress = getProgress(session, lessonId);
        progress.setProgramExecuted(programExecuted);
        saveProgress(session, lessonId, progress);
    }

    /**
     * 教材の最初のステップを使って初期進捗を作成します。
     */
    private LessonProgress createInitialProgress(String lessonId) {
        String firstStepId = lessonService.getFirstStep(lessonId).id();
        return new LessonProgress(lessonId, firstStepId);
    }

    private void validateSessionAndLesson(HttpSession session, String lessonId) {
        Objects.requireNonNull(session, "session must not be null");
        lessonService.getLesson(lessonId);
    }

    /**
     * セッションから教材別進捗Mapを取得します。
     * 初回は空のMapを作成してセッションへ保存します。
     */
    @SuppressWarnings("unchecked")
    private Map<String, LessonProgress> getProgressMap(HttpSession session) {
        Object storedProgress = session.getAttribute(LESSON_PROGRESS_MAP);

        if (storedProgress == null) {
            Map<String, LessonProgress> progressMap = new LinkedHashMap<>();
            session.setAttribute(LESSON_PROGRESS_MAP, progressMap);
            return progressMap;
        }

        if (!(storedProgress instanceof Map<?, ?>)) {
            throw new IllegalStateException(
                    "セッションの学習進捗データが正しいMap形式ではありません。"
            );
        }

        return (Map<String, LessonProgress>) storedProgress;
    }
}
