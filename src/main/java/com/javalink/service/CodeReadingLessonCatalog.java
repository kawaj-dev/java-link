package com.javalink.service;

import com.javalink.model.CodeReadingCardDefinition;
import com.javalink.model.CodeReadingCircuitDefinition;
import com.javalink.model.CodeReadingCodeLineDefinition;
import com.javalink.model.CodeReadingCodeTokenDefinition;
import com.javalink.model.CodeReadingLessonDefinition;
import com.javalink.model.CodeReadingPart;
import com.javalink.model.CodeReadingStepDefinition;
import com.javalink.model.Lesson;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * コードリーディング教材に必要なLesson、問題、説明、表示構造を一元管理します。
 */
@Service
public class CodeReadingLessonCatalog {

    public static final String STAGE1_LESSON_ID = "hello-program-reading";
    private static final String DEFAULT_LESSON_ID = STAGE1_LESSON_ID;
    private final Map<String, CodeReadingLessonDefinition> definitions;

    public CodeReadingLessonCatalog() {
        CodeReadingLessonDefinition stage1 = createStage1();
        definitions = Map.of(stage1.lessonId(), stage1);
    }

    public CodeReadingLessonDefinition getDefinition(String lessonId) {
        CodeReadingLessonDefinition definition = definitions.get(lessonId);
        if (definition == null) {
            throw new IllegalArgumentException(
                    "コードリーディング教材が見つかりません。lessonId: "
                            + lessonId
            );
        }
        return definition;
    }

    public CodeReadingLessonDefinition getDefaultDefinition() {
        return getDefinition(DEFAULT_LESSON_ID);
    }

    public Lesson getLesson(String lessonId) {
        return getDefinition(lessonId).toLesson();
    }

    public List<Lesson> getLessons() {
        return definitions.values().stream()
                .map(CodeReadingLessonDefinition::toLesson)
                .toList();
    }

    public boolean supports(String lessonId) {
        return definitions.containsKey(lessonId);
    }

    private static CodeReadingLessonDefinition createStage1() {
        return new CodeReadingLessonDefinition(
                STAGE1_LESSON_ID,
                "Stage 1",
                "「Hello」と表示するプログラムを読めるようになろう",
                """
                        public class Main {
                            public static void main(String[] args) {
                                System.out.println("Hello");
                            }
                        }
                        """,
                createStage1Steps(),
                createStage1Parts(),
                createStage1Circuits(),
                createStage1CodeLines(),
                "Hello"
        );
    }

    private static List<CodeReadingStepDefinition> createStage1Steps() {
        return List.of(
                step("class-public", 1, "public", "public", "accessible", "外から使える",
                        "アクセス修飾子", "外から使える範囲を指定します。",
                        "ほかのクラスから使えるようにします。"),
                step("class-keyword", 2, "class", "class", "declare-class", "クラスを作る",
                        "クラス宣言のキーワード", "クラスの定義を始めるキーワードです。",
                        "プログラムの設計図を作るときに使います。"),
                step("class-name", 3, "Main", "Main", "main-class-name", "クラスの名前",
                        "クラス名", "作成するクラスを識別する名前です。",
                        "このクラスにつけた名前です。"),
                step("class-open", 4, "{", "{", "block-start", "ここから始まる",
                        "開始波かっこ", "クラスのブロック開始を表します。",
                        "クラスの中身がここから始まります。"),
                step("main-public", 5, "public", "public", "accessible", "外から使える",
                        "アクセス修飾子", "mainメソッドを外から呼び出せるようにします。",
                        "Javaから呼び出せる開始地点にします。"),
                step("static", 6, "static", "static", "without-instance", "インスタンスを作らなくても使える",
                        "static修飾子", "インスタンスに属さないことを示します。",
                        "インスタンスを作らなくても使えます。", "newしなくても呼び出せます。"),
                step("void", 7, "void", "void", "no-return", "戻り値を返さない",
                        "戻り値の型", "このメソッドが値を返さないことを示します。",
                        "戻り値を返しません。"),
                step("main", 8, "main", "main", "program-entry", "プログラム開始メソッド",
                        "メソッド名", "Javaが実行開始時に探す名前です。",
                        "Javaはここから実行を始めます。"),
                step("string-array", 9, "String[]", "String[]", "multiple-strings", "文字列の配列",
                        "引数の型", "文字列を複数まとめて扱う型です。",
                        "文字列の配列です。"),
                step("args", 10, "args", "args", "argument-variable", "受け取った値の名前",
                        "引数名", "mainメソッドが受け取る値につけた名前です。",
                        "受け取った値につける名前です。"),
                step("main-open", 11, "{", "{", "block-start", "ここから始まる",
                        "開始波かっこ", "mainメソッドのブロック開始を表します。",
                        "mainメソッドの中身がここから始まります。"),
                step("print-command", 12, "System.out.println", "System.out.println", "display-and-newline", "画面に表示して改行する",
                        "画面へ表示する命令", "標準出力へ内容を表示して改行する命令です。",
                        "かっこの中の内容を画面へ表示して改行します。",
                        "ドットは左側のものが持つ機能へ順番につなぎます。"),
                step("hello-string", 13, "\"Hello\"", "\"Hello\"", "display-text", "表示する文字",
                        "文字列リテラル", "ダブルクォートで囲んだ文字列の値です。",
                        "printlnに渡す文字列です。"),
                step("semicolon", 14, ";", ";", "command-end", "命令の終わり",
                        "セミコロン", "Javaの文が終わる位置を表します。",
                        "この命令がここで終わることを表します。"),
                step("main-close", 15, "}", "}", "close-main", "mainメソッド終了",
                        "終了波かっこ", "mainメソッドのブロックを閉じます。",
                        "mainメソッドの中身がここで終わります。"),
                step("class-close", 16, "}", "}", "close-class", "Mainクラス終了",
                        "終了波かっこ", "Mainクラスのブロックを閉じます。",
                        "Mainクラスの中身がここで終わります。")
        );
    }

    private static CodeReadingStepDefinition step(
            String id,
            int order,
            String displayLabel,
            String targetCode,
            String cardId,
            String cardText,
            String technicalTerm,
            String technicalExplanation,
            String... beginnerExplanations
    ) {
        return new CodeReadingStepDefinition(
                id,
                order,
                displayLabel,
                "",
                targetCode,
                "",
                displayLabel + " の意味はどれですか？",
                new CodeReadingCardDefinition(cardId, cardText),
                List.of(),
                technicalTerm,
                List.of(beginnerExplanations),
                technicalExplanation,
                true
        );
    }

    private static List<CodeReadingCircuitDefinition> createStage1Circuits() {
        return List.of(
                new CodeReadingCircuitDefinition(
                        "class-declaration", "public class Main { }",
                        List.of("class-public", "class-keyword", "class-name", "class-open"),
                        List.of("public", "class", "Main", "{ }")
                ),
                new CodeReadingCircuitDefinition(
                        "main-method", "public static void main(String[] args) { }",
                        List.of("main-public", "static", "void", "main", "string-array", "args", "main-open"),
                        List.of("public", "static", "void", "main", "String[]", "args", "{ }")
                ),
                new CodeReadingCircuitDefinition(
                        "print-statement", "System.out.println(\"Hello\");",
                        List.of("print-command", "hello-string", "semicolon"),
                        List.of("System.out.println", "\"Hello\"", ";")
                ),
                new CodeReadingCircuitDefinition(
                        "block-closes", "mainメソッド終了 }　Mainクラス終了 }",
                        List.of("main-close", "class-close"),
                        List.of("mainメソッド終了 }", "Mainクラス終了 }")
                )
        );
    }

    private static Map<String, List<CodeReadingCodeLineDefinition>>
            createStage1CodeLines() {
        return Map.of(
                "part-1", List.of(line(
                        List.of("class-public", "class-keyword", "class-name", "class-open"),
                        "}", ""
                )),
                "part-2", List.of(new CodeReadingCodeLineDefinition(
                        List.of(
                                token("main-public"), token("static"),
                                token("void"), token("main"),
                                new CodeReadingCodeTokenDefinition("string-array", "(", ""),
                                new CodeReadingCodeTokenDefinition("args", "", ")"),
                                token("main-open")
                        ),
                        "}",
                        "quiz-reading-code-line--part-2"
                )),
                "part-3", List.of(new CodeReadingCodeLineDefinition(
                        List.of(
                                token("print-command"),
                                new CodeReadingCodeTokenDefinition("hello-string", "(", ")"),
                                token("semicolon")
                        ),
                        "", ""
                )),
                "part-4", List.of(line(
                        List.of("main-close", "class-close"), "", ""
                ))
        );
    }

    private static CodeReadingCodeLineDefinition line(
            List<String> stepIds,
            String trailingCode,
            String cssClass
    ) {
        return new CodeReadingCodeLineDefinition(
                stepIds.stream().map(CodeReadingLessonCatalog::token).toList(),
                trailingCode,
                cssClass
        );
    }

    private static CodeReadingCodeTokenDefinition token(String stepId) {
        return CodeReadingCodeTokenDefinition.step(stepId);
    }

    private static List<CodeReadingPart> createStage1Parts() {
        return List.of(
                new CodeReadingPart(
                        "part-1", 1, "クラスを作る",
                        List.of(
                                "Mainという名前のクラスを作ります。",
                                "Mainという名前は自由に変更できます。",
                                "今回は分かりやすくMainという名前を使います。"
                        ),
                        "public class Main {",
                        List.of("class-public", "class-keyword", "class-name", "class-open"),
                        List.of("public", "class", "Main", "{"),
                        List.of(
                                "MainはJavaの固定名ではなく、自分で付けられるクラス名です。",
                                "{ から } までがMainクラスのブロックです。"
                        ),
                        "外から使えるMainクラスを作る"
                ),
                new CodeReadingPart(
                        "part-2", 2, "mainメソッドを作る",
                        List.of(
                                "public static void main(String[] args) は、Javaでプログラムを始めるための決まり文句です。",
                                "Javaはこのmainメソッドから実行を始めます。"
                        ),
                        "public static void main(String[] args) {",
                        List.of("main-public", "static", "void", "main", "string-array", "args", "main-open"),
                        List.of("public", "static", "void", "main", "String[]", "args", "{"),
                        List.of("public static void main(String[] args) { は、mainメソッドを始める決まり文句です。"),
                        "Javaが最初に実行するmainメソッド"
                ),
                new CodeReadingPart(
                        "part-3", 3, "「Hello」を表示する",
                        List.of("画面へ文字を表示する命令を書きます。"),
                        "System.out.println(\"Hello\");",
                        List.of("print-command", "hello-string", "semicolon"),
                        List.of("System.out.println", "\"Hello\"", ";"),
                        List.of(
                                "System.out.println(...) は、かっこの中の内容を画面へ表示して改行します。",
                                ". は、左側のものが持つ機能へ順番につなぐ記号です。",
                                "( ) の中には、表示したい内容を書きます。"
                        ),
                        "Helloと表示して改行する"
                ),
                new CodeReadingPart(
                        "part-4", 4, "ブロックの終わりを確認する",
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
    }
}
