package com.javalink.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * 1つの教材に対する学習進捗を表します。
 *
 * 教材データとは分離し、回答によって変化する状態だけを保持します。
 */
public class LessonProgress implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String lessonId;
    private String currentStepId;
    private final Set<String> completedStepIds = new LinkedHashSet<>();
    private String selectedOptionId = "";
    private boolean answered;
    private boolean correct;
    private boolean completed;
    private boolean programExecuted;

    /**
     * 教材の初期進捗を作ります。
     *
     * @param lessonId     教材を区別するID
     * @param firstStepId  最初に学習するステップのID
     */
    public LessonProgress(String lessonId, String firstStepId) {
        this.lessonId = Objects.requireNonNull(lessonId, "lessonId must not be null");
        this.currentStepId =
                Objects.requireNonNull(firstStepId, "firstStepId must not be null");
    }

    public String getLessonId() {
        return lessonId;
    }

    public String getCurrentStepId() {
        return currentStepId;
    }

    public void setCurrentStepId(String currentStepId) {
        this.currentStepId =
                Objects.requireNonNull(currentStepId, "currentStepId must not be null");
    }

    /**
     * 呼び出し側から完了済み一覧を変更できない形で返します。
     *
     * @return 完了済みステップID
     */
    public Set<String> getCompletedStepIds() {
        return Collections.unmodifiableSet(completedStepIds);
    }

    /**
     * ステップを完了済みとして記録します。
     * Setを使うため、同じIDを追加しても重複しません。
     *
     * @param stepId 完了したステップのID
     */
    public void completeStep(String stepId) {
        completedStepIds.add(Objects.requireNonNull(stepId, "stepId must not be null"));
    }

    public boolean isStepCompleted(String stepId) {
        return completedStepIds.contains(stepId);
    }

    public String getSelectedOptionId() {
        return selectedOptionId;
    }

    public void setSelectedOptionId(String selectedOptionId) {
        this.selectedOptionId =
                Objects.requireNonNull(selectedOptionId, "selectedOptionId must not be null");
    }

    public boolean isAnswered() {
        return answered;
    }

    public void setAnswered(boolean answered) {
        this.answered = answered;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean isProgramExecuted() {
        return programExecuted;
    }

    public void setProgramExecuted(boolean programExecuted) {
        this.programExecuted = programExecuted;
    }
}
