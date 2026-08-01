package com.javalink.model;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Stageごとに異なるコードリーディング教材の表示・構成を定義します。
 */
public record CodeReadingLessonDefinition(
        String lessonId,
        String stageName,
        String learningGoal,
        String completedCode,
        List<CodeReadingStepDefinition> steps,
        List<CodeReadingPart> parts,
        List<CodeReadingCircuitDefinition> circuits,
        Map<String, List<CodeReadingCodeLineDefinition>> codeLinesByPart,
        String consoleOutput
) {

    public CodeReadingLessonDefinition {
        Objects.requireNonNull(lessonId, "lessonId must not be null");
        Objects.requireNonNull(stageName, "stageName must not be null");
        Objects.requireNonNull(learningGoal, "learningGoal must not be null");
        Objects.requireNonNull(completedCode, "completedCode must not be null");
        steps = List.copyOf(Objects.requireNonNull(steps, "steps must not be null"));
        parts = List.copyOf(Objects.requireNonNull(parts, "parts must not be null"));
        circuits = List.copyOf(Objects.requireNonNull(
                circuits,
                "circuits must not be null"
        ));
        codeLinesByPart = Map.copyOf(Objects.requireNonNull(
                codeLinesByPart,
                "codeLinesByPart must not be null"
        ));
        Objects.requireNonNull(consoleOutput, "consoleOutput must not be null");

        if (parts.isEmpty()) {
            throw new IllegalArgumentException("parts must not be empty");
        }
        if (steps.isEmpty()) {
            throw new IllegalArgumentException("steps must not be empty");
        }
        validateUniqueIds(steps, parts, circuits);
        validateStepCoverage(steps, parts, circuits, codeLinesByPart);
    }

    private static void validateUniqueIds(
            List<CodeReadingStepDefinition> steps,
            List<CodeReadingPart> parts,
            List<CodeReadingCircuitDefinition> circuits
    ) {
        Set<String> partIds = new HashSet<>();
        Set<String> stepIds = new HashSet<>();
        for (CodeReadingStepDefinition step : steps) {
            if (!stepIds.add(step.id())) {
                throw new IllegalArgumentException(
                        "stepIdが重複しています。stepId: " + step.id()
                );
            }
        }
        for (CodeReadingPart part : parts) {
            if (!partIds.add(part.id())) {
                throw new IllegalArgumentException(
                        "Part IDが重複しています。partId: " + part.id()
                );
            }
        }

        Set<String> circuitIds = new HashSet<>();
        for (CodeReadingCircuitDefinition circuit : circuits) {
            if (!circuitIds.add(circuit.id())) {
                throw new IllegalArgumentException(
                        "回路IDが重複しています。circuitId: " + circuit.id()
                );
            }
        }
    }

    private static void validateStepCoverage(
            List<CodeReadingStepDefinition> steps,
            List<CodeReadingPart> parts,
            List<CodeReadingCircuitDefinition> circuits,
            Map<String, List<CodeReadingCodeLineDefinition>> codeLinesByPart
    ) {
        Set<String> definedStepIds = new HashSet<>();
        steps.forEach(step -> definedStepIds.add(step.id()));
        Set<String> partStepIds = new HashSet<>();
        parts.forEach(part -> partStepIds.addAll(part.stepIds()));
        Set<String> circuitStepIds = new HashSet<>();
        circuits.forEach(circuit -> circuitStepIds.addAll(circuit.stepIds()));
        if (!definedStepIds.equals(partStepIds)
                || !partStepIds.equals(circuitStepIds)) {
            throw new IllegalArgumentException(
                    "Step、Part、コード回路のstepIdが一致していません。"
            );
        }

        Set<String> codeLineStepIds = new HashSet<>();
        for (CodeReadingPart part : parts) {
            List<CodeReadingCodeLineDefinition> lines =
                    codeLinesByPart.get(part.id());
            if (lines == null || lines.isEmpty()) {
                throw new IllegalArgumentException(
                        "Partのコード表示がありません。partId: " + part.id()
                );
            }
            lines.forEach(line -> line.tokens().forEach(token ->
                    codeLineStepIds.add(token.stepId())
            ));
        }
        if (!definedStepIds.equals(codeLineStepIds)) {
            throw new IllegalArgumentException(
                    "Stepとコード表示行のstepIdが一致していません。"
            );
        }
    }

    public Lesson toLesson() {
        return new Lesson(
                lessonId,
                stageName,
                learningGoal,
                completedCode,
                steps.stream()
                        .map(CodeReadingStepDefinition::toLessonStep)
                        .toList()
        );
    }

    public CodeReadingStepDefinition getStep(String stepId) {
        return steps.stream()
                .filter(step -> step.id().equals(stepId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "教材Stepが見つかりません。lessonId: " + lessonId
                                + ", stepId: " + stepId
                ));
    }
}
