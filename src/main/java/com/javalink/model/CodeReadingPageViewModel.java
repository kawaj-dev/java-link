package com.javalink.model;

import java.util.List;
import java.util.Objects;

/**
 * 「コードを左から読む」の各画面に必要な情報をまとめます。
 */
public record CodeReadingPageViewModel(
        CodeReadingPhase phase,
        String stageName,
        String learningGoal,
        String completedCode,
        CodeReadingPart currentPart,
        int partNumber,
        int totalPartCount,
        LessonStep currentStep,
        List<String> partStepIds,
        int partCompletedCount,
        boolean partCompleted,
        boolean lastPart,
        List<String> completionNotes,
        List<CodeReadingCircuitGroup> circuitGroups,
        List<CodeReadingCodeLine> codeLines,
        List<CodeReadingPart> summaryParts,
        boolean runEnabled
) {

    public CodeReadingPageViewModel {
        Objects.requireNonNull(phase, "phase must not be null");
        Objects.requireNonNull(stageName, "stageName must not be null");
        Objects.requireNonNull(learningGoal, "learningGoal must not be null");
        Objects.requireNonNull(completedCode, "completedCode must not be null");
        partStepIds = List.copyOf(partStepIds);
        completionNotes = List.copyOf(completionNotes);
        circuitGroups = List.copyOf(circuitGroups);
        codeLines = List.copyOf(codeLines);
        summaryParts = List.copyOf(summaryParts);
    }
}
