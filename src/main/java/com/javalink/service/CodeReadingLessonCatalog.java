package com.javalink.service;

import com.javalink.model.CodeReadingCardDefinition;
import com.javalink.model.CodeReadingCircuitDefinition;
import com.javalink.model.CodeReadingCodeLineDefinition;
import com.javalink.model.CodeReadingCodeTokenDefinition;
import com.javalink.model.CodeReadingExplanationEntry;
import com.javalink.model.CodeReadingExplanationSection;
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
                                                        + lessonId);
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
                                "Hello");
        }

        private static List<CodeReadingStepDefinition> createStage1Steps() {
                return List.of(
                                richStep("class-public", 1, "public", "public", "accessible", "外から使える",
                                                "アクセス修飾子",
                                                "publicは、クラスやメソッドをどこから使えるか決めるアクセス修飾子（access modifier）です。",
                                                publicExplanation(),
                                                "publicを付けると、どこからでも使えるようになります。",
                                                "今回は public = 外から使える と覚えれば大丈夫です。"),
                                richStep("class-keyword", 2, "class", "class", "declare-class", "クラスを作る",
                                                "クラスを作るキーワード",
                                                "classは、クラスを作ることをJavaへ伝えるキーワードです。",
                                                classExplanation(),
                                                "Javaでは、プログラムをクラスという単位で作ります。",
                                                "クラスは、プログラムの設計図のようなものです。",
                                                "今回は class = クラスを作る と覚えれば大丈夫です。"),
                                richStep("class-name", 3, "Main", "Main", "main-class-name", "クラスの名前",
                                                "クラス名",
                                                "Mainは、作成するクラスにつけた名前です。",
                                                mainExplanation(),
                                                "Mainという名前は自由に変更できます。",
                                                "publicなクラスでは、ファイル名とクラス名を同じにします。",
                                                "Mainは決まり文句ではなく、クラスにつける名前です。"),
                                richStep("class-open", 4, "{", "{", "block-start", "ここから始まる",
                                                "開始波かっこ", "クラスやメソッドの中身がここから始まることを表します。",
                                                blockStartExplanation(),
                                                "あとで必ず対応する } で閉じます。",
                                                "{ から対応する } までの範囲をブロックと呼びます。",
                                                "今回は { = ここから始まる と覚えれば大丈夫です。"),
                                richStep("main-public", 5, "public", "public", "accessible", "外から使える",
                                                "アクセス修飾子", "mainメソッドを外から呼び出せるようにします。",
                                                publicExplanation(),
                                                "Javaから呼び出せる開始地点にします。"),
                                richStep("static", 6, "static", "static", "without-instance", "インスタンスを作らなくても使える",
                                                "static修飾子", "インスタンスに属さないことを示します。",
                                                 staticExplanation(),
                                                "インスタンスを作らなくても使えます。", "newしなくても呼び出せます。"),
                                richStep("void", 7, "void", "void", "no-return", "戻り値を返さない",
                                                "戻り値の型", "このメソッド（動作）が、値を返さないことを示します。",
                                                voidExplanation(),
                                                "結果を返す必要がない場合に使います。"),
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
                                step("print-command", 12, "System.out.println", "System.out.println",
                                                "display-and-newline", "画面に表示して改行する",
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
                                                "Mainクラスの中身がここで終わります。"));
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
                        String... beginnerExplanations) {
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
                                standardExplanation(technicalTerm, technicalExplanation, beginnerExplanations),
                                true);
        }

        private static CodeReadingStepDefinition richStep(
                        String id,
                        int order,
                        String displayLabel,
                        String targetCode,
                        String cardId,
                        String cardText,
                        String technicalTerm,
                        String technicalExplanation,
                        List<CodeReadingExplanationSection> explanationSections,
                        String... beginnerExplanations) {
                return new CodeReadingStepDefinition(
                                id, order, displayLabel, "", targetCode, "",
                                displayLabel + " の意味はどれですか？",
                                new CodeReadingCardDefinition(cardId, cardText),
                                List.of(), technicalTerm, List.of(beginnerExplanations),
                                technicalExplanation, explanationSections, true);
        }

        private static List<CodeReadingExplanationSection> publicExplanation() {
                return List.of(
                                section("overview", "text", "アクセス修飾子　access modifier　",
                                                entry("", "クラスやメソッドを、", "どこから使えるか", "を決めます。")),
                                section(
                                                "content",
                                                "table",
                                                "アクセス修飾子の種類",
                                                entry("public", "🌎 どこからでも使える", "", ""),
                                                entry("protected", "📦 同じパッケージ＋子クラス", "", ""),
                                                entry("（指定なし）", "📦 同じパッケージだけ", "", ""),
                                                entry("private", "🔒 自分のクラスだけ", "", "")),
                                section("supplement", "text", "",
                                                entry("", "", "public", "が付いたクラス、メソッド、フィールドは、どこからでも使うことができます。"),
                                                entry("", "自分のクラスだけでなく、他のクラスからも利用できます。", "", "")));
        }

         private static List<CodeReadingExplanationSection> staticExplanation() {
                return List.of(
                                section("overview", "text", "static修飾子　static modifier",
                                                entry("", "インスタンス（実体）を作らなくても、クラスから直接メソッドやフィールドを", "利用できる", "ようにする修飾子です。")),
                                section(
                                                "content",
                                                "table",
                                                "static修飾子の特徴",
                                                entry("static", "インスタンス（実体）を作らなくても使える", "", ""),
                                                entry("（なし）", "インスタンス（実体）を作ってから使う", "", "")),
                                section("supplement", "text", "",
                                                entry("", "staticが付いたメソッドやフィールドを、", "静的メンバ（static member）", "と呼びます。")));
        }

        private static List<CodeReadingExplanationSection> voidExplanation() {
    return List.of(
            section(
                    "overview",
                    "text",
                    "戻り値の型",
                    entry(
                            "",
                            "",
                            "このメソッド（動作）が、値を返さないことを示します。",
                            ""
                    )
            ),
            section(
                    "content",
                    "table",
                    "内容",
                    entry(
                            "void を使う場合",
                            "処理だけを行います。",
                            "",
                            ""
                    ),
                    entry(
                            "void を使わない場合",
                            "処理した後に結果を返します（int・String・booleanなどの型を指定）。",
                            "",
                            " "
                    )
            ),
            section(
                    "supplement",
                    "text",
                    "",
                    entry(
                            "",
                            "画面に表示するだけの処理など、結果を返す必要がない場合に使います。",
                            "",
                            ""
                    )
            )
    );
}

        private static List<CodeReadingExplanationSection> classExplanation() {
                return List.of(
                                section("overview", "text", "クラス　class　",
                                                entry("", "", "「これからプログラムの設計図（クラス）を作ります」", "という意味です。")),
                                section(
    "content",
    "table",
    "🏠 家でたとえると・・・",
    entry(
    "設計図",
    "設計図を作り、その設計図をもとに家を建てます。",
    "",
    ""
),
entry(
    "クラス",
    "家の設計図にあたるものが、Javaでは「クラス」です。",
    "",
    ""
)
),
                                section("supplement", "text", "",
                                                entry("", " ", "class", "は、「これから設計図を作ります」とコンピューターに伝えるキーワードです。")
                                               ));
        }

        private static List<CodeReadingExplanationSection> mainExplanation() {
                return List.of(
                                section("overview", "text", "クラス名　class name",
                                                entry("", "クラス（設計図）に付ける名前です。", "", ""),
                                                entry("", "名前は自由に変更できます。今回は例として「", "Main", "」という名前にします。")),
                                section("content", "examples", "ファイル名とのルール",
                                                entry("", "publicなクラスでは、ファイル名とクラス名を同じにします。", "", ""),
                                                entry("Main.javaであれば・・・", "public class Main", "", ""),
                                                entry("Hello.javaであれば・・・", "public class Hello", "", "")),
                                section("supplement", "comparison", "",
                                                entry("Main：　", "クラス名です。自由に変更できます。", "", ""),
                                                entry("main：　", "Javaが最初に探す特別なメソッドです。変更できません。", "", "")));
        }

        private static List<CodeReadingExplanationSection> blockStartExplanation() {
                return List.of(
                                section("overview", "text", "{ 波かっこ　（開始）",
                                                entry("", "クラスやメソッドの中身が、", "ここから始まる", "ことを表します。")),
                                section("content", "diagram", "波かっこのルール",
                                                entry("{ と } はセット", "「{」を書いたら、あとで必ず対応する「}」を書きます。", "", "")),
                                section("supplement", "text", "",
                                                entry("", "「{」から「}」までをブロックと呼びます。", "", "")));
        }

        private static List<CodeReadingExplanationSection> standardExplanation(
                        String technicalTerm,
                        String technicalExplanation,
                        String... beginnerExplanations) {
                return List.of(
                                section("overview", "text", technicalTerm,
                                                entry("", "", "", "")),
                                section("content", "text", "",
                                                entry("", technicalExplanation, "", "")),
                                section("supplement", "text", "",
                                                java.util.Arrays.stream(beginnerExplanations)
                                                                .map(line -> entry("", line, "", ""))
                                                                .toArray(CodeReadingExplanationEntry[]::new)));
        }

        private static CodeReadingExplanationSection section(
                        String kind,
                        String layout,
                        String title,
                        CodeReadingExplanationEntry... entries) {
                return new CodeReadingExplanationSection(kind, layout, title, List.of(entries));
        }

        private static CodeReadingExplanationEntry entry(
                        String label,
                        String before,
                        String emphasis,
                        String after) {
                return new CodeReadingExplanationEntry(label, before, emphasis, after, false);
        }

        private static CodeReadingExplanationEntry highlightedEntry(String label, String text) {
                return new CodeReadingExplanationEntry(label, text, "", "", true);
        }

        private static List<CodeReadingCircuitDefinition> createStage1Circuits() {
                return List.of(
                                new CodeReadingCircuitDefinition(
                                                "class-declaration", "public class Main { }",
                                                List.of("class-public", "class-keyword", "class-name", "class-open"),
                                                List.of("public", "class", "Main", "{ }")),
                                new CodeReadingCircuitDefinition(
                                                "main-method", "public static void main(String[] args) { }",
                                                List.of("main-public", "static", "void", "main", "string-array", "args",
                                                                "main-open"),
                                                List.of("public", "static", "void", "main", "String[]", "args", "{ }")),
                                new CodeReadingCircuitDefinition(
                                                "print-statement", "System.out.println(\"Hello\");",
                                                List.of("print-command", "hello-string", "semicolon"),
                                                List.of("System.out.println", "\"Hello\"", ";")),
                                new CodeReadingCircuitDefinition(
                                                "block-closes", "mainメソッド終了 }　Mainクラス終了 }",
                                                List.of("main-close", "class-close"),
                                                List.of("mainメソッド終了 }", "Mainクラス終了 }")));
        }

        private static Map<String, List<CodeReadingCodeLineDefinition>> createStage1CodeLines() {
                return Map.of(
                                "part-1", List.of(line(
                                                List.of("class-public", "class-keyword", "class-name", "class-open"),
                                                "}", "")),
                                "part-2", List.of(new CodeReadingCodeLineDefinition(
                                                List.of(
                                                                token("main-public"), token("static"),
                                                                token("void"), token("main"),
                                                                new CodeReadingCodeTokenDefinition("string-array", "(",
                                                                                ""),
                                                                new CodeReadingCodeTokenDefinition("args", "", ")"),
                                                                token("main-open")),
                                                "}",
                                                "quiz-reading-code-line--part-2")),
                                "part-3", List.of(new CodeReadingCodeLineDefinition(
                                                List.of(
                                                                token("print-command"),
                                                                new CodeReadingCodeTokenDefinition("hello-string", "(",
                                                                                ")"),
                                                                token("semicolon")),
                                                "", "")),
                                "part-4", List.of(line(
                                                List.of("main-close", "class-close"), "", "")));
        }

        private static CodeReadingCodeLineDefinition line(
                        List<String> stepIds,
                        String trailingCode,
                        String cssClass) {
                return new CodeReadingCodeLineDefinition(
                                stepIds.stream().map(CodeReadingLessonCatalog::token).toList(),
                                trailingCode,
                                cssClass);
        }

        private static CodeReadingCodeTokenDefinition token(String stepId) {
                return CodeReadingCodeTokenDefinition.step(stepId);
        }

        private static List<CodeReadingPart> createStage1Parts() {
                return List.of(
                                new CodeReadingPart(
                                                "part-1", 1, "クラスを作る",
                                                List.of(),
                                                "public class Main {",
                                                List.of("class-public", "class-keyword", "class-name", "class-open"),
                                                List.of("public", "class", "Main", "{"),
                                                List.of(),
                                                "外から使えるMainクラスを作る"),
                                new CodeReadingPart(
                                                "part-2", 2, "mainメソッドを作る",
                                                List.of(
                                                                "public static void main(String[] args) は、Javaでプログラムを始めるための決まり文句です。",
                                                                "まずは構文として覚えてしまいましょう。"),
                                                "public static void main(String[] args) {",
                                                List.of("main-public", "static", "void", "main", "string-array", "args",
                                                                "main-open"),
                                                List.of("public", "static", "void", "main", "String[]", "args", "{"),
                                                List.of("Javaはこのmainメソッドを探して、ここから実行を始めます"),
                                                "Javaが最初に実行するmainメソッド"),
                                new CodeReadingPart(
                                                "part-3", 3, "「Hello」を表示する",
                                                List.of("画面へ文字を表示する命令を書きます。"),
                                                "System.out.println(\"Hello\");",
                                                List.of("print-command", "hello-string", "semicolon"),
                                                List.of("System.out.println", "\"Hello\"", ";"),
                                                List.of(
                                                                "System.out.println(...) は、かっこの中の内容を画面へ表示して改行します。",
                                                                ". は、左側のものが持つ機能へ順番につなぐ記号です。",
                                                                "( ) の中には、表示したい内容を書きます。"),
                                                "Helloと表示して改行する"),
                                new CodeReadingPart(
                                                "part-4", 4, "ブロックの終わりを確認する",
                                                List.of("mainメソッドとMainクラスを順番に閉じます。"),
                                                "}\n}",
                                                List.of("main-close", "class-close"),
                                                List.of("}", "}"),
                                                List.of(
                                                                "最初の } でmainメソッドを閉じます。",
                                                                "最後の } でMainクラスを閉じます。"),
                                                "mainメソッドとMainクラスを順番に終了する"));
        }
}
