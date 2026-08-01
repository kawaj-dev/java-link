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
                .map(circuit -> createCircuitGroup(circuit, progress))
                .toList();
    }

    private CodeReadingCircuitGroup createCircuitGroup(
            CodeReadingCircuitDefinition definition,
            LessonProgress progress
    ) {
        List<CodeReadingCircuitBulb> bulbs = java.util.stream.IntStream
                .range(0, definition.stepIds().size())
                .mapToObj(index -> {
                    String stepId = definition.stepIds().get(index);
                    return new CodeReadingCircuitBulb(
                            stepId,
                            definition.codeLabels().get(index),
                            progress.getCompletedStepIds().contains(stepId),
                            stepId.equals(progress.getCurrentStepId())
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

    private CodeReadingLessonDefinition definition(String lessonId) {
        return lessonCatalog.getDefinition(lessonId);
    }
}
