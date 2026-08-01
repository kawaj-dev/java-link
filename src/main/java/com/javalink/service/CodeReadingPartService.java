package com.javalink.service;

import com.javalink.model.CodeReadingCircuitBulb;
import com.javalink.model.CodeReadingCircuitGroup;
import com.javalink.model.CodeReadingPart;
import com.javalink.model.LessonProgress;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 「Hello」と表示するプログラムを、意味のまとまりごとに管理します。
 */
@Service
public class CodeReadingPartService {

    private static final List<CircuitDefinition> CIRCUIT_DEFINITIONS = List.of(
            new CircuitDefinition(
                    "class-declaration",
                    "public class Main { }",
                    List.of("class-public", "class-keyword", "class-name", "class-open"),
                    List.of("public", "class", "Main", "{ }")
            ),
            new CircuitDefinition(
                    "main-method",
                    "public static void main(String[] args) { }",
                    List.of("main-public", "static", "void", "main", "string-array", "args", "main-open"),
                    List.of("public", "static", "void", "main", "String[]", "args", "{ }")
            ),
            new CircuitDefinition(
                    "print-statement",
                    "System.out.println(\"Hello\");",
                    List.of("print-command", "hello-string", "semicolon"),
                    List.of("System.out.println", "\"Hello\"", ";")
            ),
            new CircuitDefinition(
                    "block-closes",
                    "mainメソッド終了 }　Mainクラス終了 }",
                    List.of("main-close", "class-close"),
                    List.of("mainメソッド終了 }", "Mainクラス終了 }")
            )
    );

    private static final List<CodeReadingPart> HELLO_PARTS = List.of(
            new CodeReadingPart(
                    "part-1",
                    1,
                    "クラスを作る",
                    List.of(
                            "Mainという名前のクラスを作ります。",
                            "Mainという名前は自由に変更できます。",
                            "今回は分かりやすくMainという名前を使います。"
                    ),
                    "public class Main {",
                    List.of(
                            "class-public",
                            "class-keyword",
                            "class-name",
                            "class-open"
                    ),
                    List.of("public", "class", "Main", "{"),
                    List.of(
                            "MainはJavaの固定名ではなく、自分で付けられるクラス名です。",
                            "{ から } までがMainクラスのブロックです。"
                    ),
                    "外から使えるMainクラスを作る"
            ),
            new CodeReadingPart(
                    "part-2",
                    2,
                    "mainメソッドを作る",
                    List.of(
                            "public static void main(String[] args) は、Javaでプログラムを始めるための決まり文句です。",
                            "Javaはこのmainメソッドから実行を始めます。"
                    ),
                    "public static void main(String[] args) {",
                    List.of(
                            "main-public", "static", "void", "main",
                            "string-array", "args", "main-open"
                    ),
                    List.of("public", "static", "void", "main", "String[]", "args", "{"),
                    List.of(
                            "public static void main(String[] args) { は、mainメソッドを始める決まり文句です。"
                    ),
                    "Javaが最初に実行するmainメソッド"
            ),
            new CodeReadingPart(
                    "part-3",
                    3,
                    "「Hello」を表示する",
                    List.of("画面へ文字を表示する命令を書きます。"),
                    "System.out.println(\"Hello\");",
                    List.of("print-command", "hello-string", "semicolon"),
                    List.of("System.out.println", "(\"Hello\")", ";"),
                    List.of(
                            "System.out.println(...) は、かっこの中の内容を画面へ表示して改行します。",
                            ". は、左側のものが持つ機能へ順番につなぐ記号です。",
                            "( ) の中には、表示したい内容を書きます。"
                    ),
                    "Helloと表示して改行する"
            ),
            new CodeReadingPart(
                    "part-4",
                    4,
                    "ブロックの終わりを確認する",
                    List.of("mainメソッドとMainクラスを順番に閉じます。"),
                    "}\n}",
                    List.of("main-close", "class-close"),
                    List.of("}", "}"),
                    List.of(
                            "最初の } でmainメソッドを閉じます。",
                            "最後の } でMainクラスを閉じます。"
                    ),
                    "mainメソッドとMainクラスを順番に終了する"
            )
    );

    public List<CodeReadingPart> getParts() {
        return HELLO_PARTS;
    }

    public CodeReadingPart getPart(String partId) {
        return HELLO_PARTS.stream()
                .filter(part -> part.id().equals(partId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Partが見つかりません。partId: " + partId
                ));
    }

    public CodeReadingPart getPartForStep(String stepId) {
        return HELLO_PARTS.stream()
                .filter(part -> part.stepIds().contains(stepId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "ステップに対応するPartが見つかりません。stepId: "
                                + stepId
                ));
    }

    public boolean isLastPart(CodeReadingPart part) {
        return part.order() == HELLO_PARTS.size();
    }

    /**
     * 完了済みstepから、Javaコードの意味単位でまとめた回路を作ります。
     */
    public List<CodeReadingCircuitGroup> createCircuitGroups(
            LessonProgress progress
    ) {
        return CIRCUIT_DEFINITIONS.stream()
                .map(definition -> {
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
                })
                .toList();
    }

    private record CircuitDefinition(
            String id,
            String codeLabel,
            List<String> stepIds,
            List<String> codeLabels
    ) {
        private CircuitDefinition {
            if (stepIds.size() != codeLabels.size()) {
                throw new IllegalArgumentException(
                        "回路のstepIdとコード表示は同じ数が必要です。"
                );
            }
        }
    }
}
