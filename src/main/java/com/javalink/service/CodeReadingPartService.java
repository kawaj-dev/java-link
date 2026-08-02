package com.javalink.service;

import com.javalink.model.CodeReadingCircuitBulb;
import com.javalink.model.CodeReadingCircuitDefinition;
import com.javalink.model.CodeReadingCircuitGroup;
import com.javalink.model.CodeReadingLessonDefinition;
import com.javalink.model.CodeReadingPart;
import com.javalink.model.LessonProgress;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * lessonIdに対応するPartとコード回路を提供します。
 */
@Service
public class CodeReadingPartService {

    private final CodeReadingLessonCatalog lessonCatalog;

    public CodeReadingPartService(CodeReadingLessonCatalog lessonCatalog) {
        this.lessonCatalog = lessonCatalog;
    }

    public List<CodeReadingPart> getParts(String lessonId) {
        return definition(lessonId).parts();
    }

    public CodeReadingPart getPart(String lessonId, String partId) {
        return getParts(lessonId).stream()
                .filter(part -> part.id().equals(partId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Partが見つかりません。lessonId: " + lessonId
                                + ", partId: " + partId
                ));
    }

    public CodeReadingPart getPartForStep(String lessonId, String stepId) {
        return getParts(lessonId).stream()
                .filter(part -> part.stepIds().contains(stepId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "ステップに対応するPartが見つかりません。lessonId: "
                                + lessonId + ", stepId: " + stepId
                ));
    }

    public boolean isLastPart(String lessonId, CodeReadingPart part) {
        return part.order() == getParts(lessonId).size();
    }

    /** 完了済みstepから、教材固有のコード回路を復元します。 */
    public List<CodeReadingCircuitGroup> createCircuitGroups(
            String lessonId,
            LessonProgress progress
    ) {
        return definition(lessonId).circuits().stream()
                .map(circuit -> createCircuitGroup(lessonId, circuit, progress))
                .toList();
    }

    private CodeReadingCircuitGroup createCircuitGroup(
            String lessonId,
            CodeReadingCircuitDefinition definition,
            LessonProgress progress
    ) {
        CodeReadingPart currentPart = getPartForStep(
                lessonId,
                progress.getCurrentStepId()
        );
        String actionableStepId = findActionableStepId(progress, currentPart);
        List<CodeReadingCircuitBulb> bulbs = java.util.stream.IntStream
                .range(0, definition.stepIds().size())
                .mapToObj(index -> {
                    String stepId = definition.stepIds().get(index);
                    boolean completed = progress.getCompletedStepIds().contains(stepId);
                    boolean explaining = stepId.equals(progress.getCurrentStepId())
                            && completed
                            && progress.isAnswered()
                            && progress.isCorrect();
                    boolean actionable = stepId.equals(actionableStepId);
                    return new CodeReadingCircuitBulb(
                            stepId,
                            definition.codeLabels().get(index),
                            definition(lessonId).getStep(stepId).correctCard().id(),
                            definition(lessonId).getStep(stepId).correctCard().text(),
                            completed,
                            stepId.equals(progress.getCurrentStepId()),
                            actionable,
                            explaining,
                            !completed && !actionable
                    );
                })
                .toList();
        return new CodeReadingCircuitGroup(
                definition.id(),
                definition.codeLabel(),
                bulbs,
                bulbs.stream().anyMatch(CodeReadingCircuitBulb::current)
        );
    }

    /** 回答前はcurrent、正解後は同じPart内の直後stepだけを操作可能にします。 */
    public String findActionableStepId(
            LessonProgress progress,
            CodeReadingPart part
    ) {
        String currentStepId = progress.getCurrentStepId();
        if (!progress.isStepCompleted(currentStepId)) {
            return currentStepId;
        }
        if (!progress.isAnswered() || !progress.isCorrect()) {
            return null;
        }
        int currentIndex = part.stepIds().indexOf(currentStepId);
        if (currentIndex < 0 || currentIndex + 1 >= part.stepIds().size()) {
            return null;
        }
        String nextStepId = part.stepIds().get(currentIndex + 1);
        return progress.isStepCompleted(nextStepId) ? null : nextStepId;
    }

    private CodeReadingLessonDefinition definition(String lessonId) {
        return lessonCatalog.getDefinition(lessonId);
    }
}
