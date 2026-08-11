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
import com.javalink.model.ExplanationSectionType;
import com.javalink.model.Lesson;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
                                                publicExplanation()),
                                richStep("class-keyword", 2, "class", "class", "declare-class", "クラスを作る",
                                                "クラスを作るキーワード",
                                                classExplanation()),
                                richStep("class-name", 3, "Main", "Main", "main-class-name", "クラスの名前",
                                                "クラス名",
                                                classNameExplanation()),
                                richStep("class-open", 4, "{", "{", "block-start", "ここから始まる",
                                                "開始波かっこ", blockStartExplanation()),
                                richStep("main-public", 5, "public", "public", "accessible", "外から使える",
                                                "アクセス修飾子", publicExplanation()),
                                richStep("static", 6, "static", "static", "without-instance", "インスタンスを作らなくても使える",
                                                "static修飾子", staticExplanation()),
                                richStep("void", 7, "void", "void", "no-return", "戻り値を返さない",
                                                "戻り値の型", voidExplanation()),
                                richStep("main", 8, "main", "main", "program-entry", "プログラム開始メソッド",
                                                "メソッド名", mainExplanation()),
                                richStep("string-array", 9, "String[]", "String[]", "multiple-strings", "文字列の配列",
                                                "引数の型", stringArrayExplanation()),
                                richStep("args", 10, "args", "args", "argument-variable", "受け取った値の名前",
                                                "引数名", argsExplanation()),
                                richStep("main-open", 11, "{", "{", "block-start", "ここから始まる",
                                                "開始波かっこ", blockStartExplanation()),
                                richStep("print-command", 12, "System.out.println", "System.out.println",
                                                "display-and-newline", "画面に表示して改行する",
                                                "標準出力", systemOutPrintlnExplanation()),
                                richStep("hello-string", 13, "\"Hello\"", "\"Hello\"", "display-text", "表示する文字",
                                                "文字列リテラル", helloStringExplanation()),
                                richStep("semicolon", 14, ";", ";", "command-end", "命令の終わり",
                                                "セミコロン", semicolonExplanation()),
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
                        List<CodeReadingExplanationSection> explanationSections) {
                return new CodeReadingStepDefinition(
                                id, order, displayLabel, "", targetCode, "",
                                displayLabel + " の意味はどれですか？",
                                new CodeReadingCardDefinition(cardId, cardText),
                                List.of(), technicalTerm, explanationSections, true);
        }

        private static List<CodeReadingExplanationSection> publicExplanation() {
                return List.of(
                                textSection("アクセス修飾子　access modifier　",
                                                text("クラスやメソッドを、", "どこから使えるか", "を決めます。")),
                                tableSection(
                                                "アクセス修飾子の種類",
                                                tableRow("public", "🌎 どこからでも使える"),
                                                tableRow("protected", "📦 同じパッケージ＋子クラス"),
                                                tableRow("（指定なし）", "📦 同じパッケージだけ"),
                                                tableRow("private", "🔒 自分のクラスだけ")),
                                textSection("",
                                                text("", "public", "が付いたクラス、メソッド、フィールドは、どこからでも使うことができます。"),
                                                text("自分のクラスだけでなく、他のクラスからも利用できます。")));
        }

        private static List<CodeReadingExplanationSection> staticExplanation() {
                return List.of(
                                textSection("static修飾子　static modifier",
                                                text("インスタンス（実体）を作らなくても、クラスから直接メソッドやフィールドを", "利用できる",
                                                                "ようにする修飾子です。")),
                                tableSection(
                                                "static修飾子の特徴",
                                                tableRow("static", "インスタンス（実体）を作らなくても使える"),
                                                tableRow("（なし）", "インスタンス（実体）を作ってから使う")),
                                textSection("",
                                                text("staticが付いたメソッドやフィールドを、", "静的メンバ（static member）", "と呼びます。")));
        }

        private static List<CodeReadingExplanationSection> voidExplanation() {
                return List.of(
                                textSection("戻り値の型",
                                                text("", "このメソッド（動作）が、値を返さないことを示します。", "")),
                                tableSection(
                                                "内容",
                                                tableRow("void を使う場合", "処理だけを行います。"),
                                                tableRow("void を使わない場合", "処理した後に結果を返します（int・String・booleanなどの型を指定）。")),
                                textSection("",
                                                text("画面に表示するだけの処理など、結果を返す必要がない場合に使います。")));
        }

        private static List<CodeReadingExplanationSection> mainExplanation() {
                return List.of(
                                textSection("メソッド名 Method name",
                                                text("Javaが最初に実行する", "メソッド（動作）の名前",
                                                                "です。プログラムのスタート地点です。")),
                                tableSection(
                                                "mainメソッドとMainクラスの違い",
                                                tableRow("main", "Javaが最初に実行するメソッド（動作）の名前です。変更できません。"),
                                                tableRow("Main", "クラス（設計図）の名前です。自由に変更できます。")),
                                textSection("",
                                                text("プログラムを実行すると、Javaは最初に", "main",
                                                                "メソッド（動作）を実行します。")));
        }

        private static List<CodeReadingExplanationSection> stringArrayExplanation() {
                return List.of(
                                textSection("引数の型 Argument type",
                                                text("mainメソッドが受け取る", "引数（渡される値）の型", "です。")),
                                tableSection(
                                                "String[]の意味",
                                                tableRow("String", "文字列を表す型です。"),
                                                tableRow("[]", "複数の値をまとめて扱う配列であることを表します。")),
                                textSection("",
                                                text("", "String[]", "は、文字列を複数まとめて扱う配列型です。")));
        }

        private static List<CodeReadingExplanationSection> argsExplanation() {
                return List.of(
                                textSection("引数名 Argument name",
                                                text("mainメソッドが受け取る", "引数（渡される値）につけられた名前",
                                                                "です。")),
                                tableSection(
                                                "String[] と args の関係",
                                                tableRow("String[]", "受け取る値の型です。"),
                                                tableRow("args", "受け取った値につける名前です。")),
                                textSection("",
                                                text("", "args", "という名前は変更できます。")));
        }

        private static List<CodeReadingExplanationSection> classExplanation() {
                return List.of(
                                textSection("クラス　class　",
                                                text("", "「これからプログラムの設計図（クラス）を作ります」", "という意味です。")),
                                tableSection(
                                                "🏠 家でたとえると・・・",
                                                tableRow("設計図", "設計図を作り、その設計図をもとに家を建てます。"),
                                                tableRow("クラス", "家の設計図にあたるものが、Javaでは「クラス」です。")),
                                textSection("",
                                                text(" ", "class", "は、「これから設計図を作ります」とコンピューターに伝えるキーワードです。")));
        }

        private static List<CodeReadingExplanationSection> classNameExplanation() {
                return List.of(
                                textSection("クラス名　class name",
                                                text("クラス（設計図）に付ける名前です。"),
                                                text("名前は自由に変更できます。今回は例として「", "Main", "」という名前にします。")),
                                examplesSection("ファイル名とのルール",
                                                note("publicなクラスでは、ファイル名とクラス名を同じにします。"),
                                                example("Main.javaであれば・・・", "public class Main"),
                                                example("Hello.javaであれば・・・", "public class Hello")),
                                comparisonSection("",
                                                comparisonEntry("Main：　", "クラス名です。自由に変更できます。"),
                                                comparisonEntry("main：　", "Javaが最初に探す特別なメソッドです。変更できません。")));
        }

        private static List<CodeReadingExplanationSection> blockStartExplanation() {
                return List.of(
                                textSection("{ 波かっこ　（開始）",
                                                text("クラスやメソッドの中身が、", "ここから始まる", "ことを表します。")),
                                diagramSection("波かっこのルール",
                                                diagramRow("{ と } はセット", "「{」を書いたら、あとで必ず対応する「}」を書きます。")),
                                textSection("",
                                                text("「{」から「}」までをブロックと呼びます。")));
        }

        private static List<CodeReadingExplanationSection> systemOutPrintlnExplanation() {
                return List.of(
                                textSection(
                                                "標準出力　standard output",
                                                text(
                                                                "標準出力に内容を表示し、",
                                                                "最後に改行します",
                                                                "。")),
                                tableSection(
                                                "System.out.println のしくみ",
                                                tableRow("System", "クラス　システムに関する機能を持ちます。"),
                                                tableRow("out", "フィールド　標準出力を表します。"),
                                                tableRow("println", "メソッド　内容を出力して改行します。")),
                                tableSection(
                                                "println と print の違い",
                                                tableRow("println", "表示したあと改行します。"),
                                                tableRow("print", "表示したあと改行しません。")));
        }

        private static List<CodeReadingExplanationSection> helloStringExplanation() {
                return List.of(
                                textSection(
                                                "文字列リテラル　string literal",
                                                text(
                                                                "ダブルクォート「\" \"」で囲んで、",
                                                                "文字列を直接書いたもの",
                                                                "です。")),
                                tableSection(
                                                "ダブルクォート「\" \"」の中",
                                                tableRow("\"Hello\"", "英字を書くことができます。"),
                                                tableRow("\"こんにちは\"", "日本語を書くことができます。"),
                                                tableRow("\"Javaを勉強中\"", "文字を組み合わせて書くこともできます。"),
                                                tableRow("\"123\"", "数字も文字列として書くことができます。")),
                                textSection(
                                                "println との関係",
                                                text(
                                                                "「\"Hello\"」が",
                                                                "printlnに渡され",
                                                                "、その内容が表示されます。")));
        }

        private static List<CodeReadingExplanationSection> semicolonExplanation() {
                return List.of(
                                textSection(
                                                "セミコロン　semicolon",
                                                text(
                                                                "Javaでは、文の末尾に「;」を付けます。",
                                                                "",
                                                                "")),
                                textSection(
                                                "セミコロン「 ; 」を付けないケース",
                                                text(
                                                                "Javaのすべての構文の末尾に「;」を付けるわけではありません。",
                                                                "",
                                                                ""),
                                                text(
                                                                "if (条件) { } や while (条件) { } のように、「;」を付けない部分もあります。コメントにも「;」は必要ありません。",
                                                                "",
                                                                "")));
        }

        private static List<CodeReadingExplanationSection> standardExplanation(
                        String technicalTerm,
                        String technicalExplanation,
                        String... beginnerExplanations) {
                List<CodeReadingExplanationSection> sections = new ArrayList<>();
                if (!technicalTerm.isBlank()) {
                        sections.add(textSection(technicalTerm, text("")));
                }
                if (!technicalExplanation.isBlank()) {
                        sections.add(textSection("", text(technicalExplanation)));
                }
                if (beginnerExplanations.length > 0) {
                        sections.add(textSection("",
                                        java.util.Arrays.stream(beginnerExplanations)
                                                        .map(CodeReadingLessonCatalog::text)
                                                        .toArray(CodeReadingExplanationEntry[]::new)));
                }
                return List.copyOf(sections);
        }

        private static CodeReadingExplanationSection textSection(
                        String title,
                        CodeReadingExplanationEntry... entries) {
                return section(ExplanationSectionType.TEXT, title, entries);
        }

        private static CodeReadingExplanationSection tableSection(
                        String title,
                        CodeReadingExplanationEntry... rows) {
                return section(ExplanationSectionType.TABLE, title, rows);
        }

        private static CodeReadingExplanationSection diagramSection(
                        String title,
                        CodeReadingExplanationEntry... rows) {
                return section(ExplanationSectionType.DIAGRAM, title, rows);
        }

        private static CodeReadingExplanationSection examplesSection(
                        String title,
                        CodeReadingExplanationEntry... examples) {
                return section(ExplanationSectionType.EXAMPLES, title, examples);
        }

        private static CodeReadingExplanationSection qaSection(
                        String title,
                        CodeReadingExplanationEntry... entries) {
                return section(ExplanationSectionType.QA, title, entries);
        }

        private static CodeReadingExplanationSection comparisonSection(
                        String title,
                        CodeReadingExplanationEntry... entries) {
                return section(ExplanationSectionType.COMPARISON, title, entries);
        }

        private static CodeReadingExplanationSection listSection(
                        String title,
                        CodeReadingExplanationEntry... items) {
                return section(ExplanationSectionType.LIST, title, items);
        }

        private static CodeReadingExplanationSection section(
                        ExplanationSectionType sectionType,
                        String title,
                        CodeReadingExplanationEntry... entries) {
                return new CodeReadingExplanationSection(sectionType, title, List.of(entries));
        }

        private static CodeReadingExplanationEntry text(
                        String before,
                        String emphasis,
                        String after) {
                return new CodeReadingExplanationEntry("", before, emphasis, after, false);
        }

        private static CodeReadingExplanationEntry text(String content) {
                return text(content, "", "");
        }

        private static CodeReadingExplanationEntry tableRow(String label, String description) {
                return new CodeReadingExplanationEntry(label, description, "", "", false);
        }

        private static CodeReadingExplanationEntry highlightedTableRow(String label, String description) {
                return new CodeReadingExplanationEntry(label, description, "", "", true);
        }

        private static CodeReadingExplanationEntry diagramRow(String source, String destination) {
                return new CodeReadingExplanationEntry(source, destination, "", "", false);
        }

        private static CodeReadingExplanationEntry example(String label, String code) {
                return new CodeReadingExplanationEntry(label, code, "", "", false);
        }

        private static CodeReadingExplanationEntry note(String content) {
                return new CodeReadingExplanationEntry("", content, "", "", false);
        }

        private static CodeReadingExplanationEntry qaEntry(String label, String content) {
                return new CodeReadingExplanationEntry(label, content, "", "", false);
        }

        private static CodeReadingExplanationEntry comparisonEntry(String label, String content) {
                return new CodeReadingExplanationEntry(label, content, "", "", false);
        }

        private static CodeReadingExplanationEntry listItem(
                        String before,
                        String emphasis,
                        String after) {
                return new CodeReadingExplanationEntry("", before, emphasis, after, false);
        }

        private static List<CodeReadingCircuitDefinition> createStage1Circuits() {
                return List.of(
                                new CodeReadingCircuitDefinition(
                                                "class-declaration", "public class Main { ",
                                                List.of("class-public", "class-keyword", "class-name", "class-open"),
                                                List.of("public", "class", "Main", "{ ")),
                                new CodeReadingCircuitDefinition(
                                                "main-method", "public static void main(String[] args) { ",
                                                List.of("main-public", "static", "void", "main", "string-array", "args",
                                                                "main-open"),
                                                List.of("public", "static", "void", "main", "String[]", "args", "{ ")),
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
                                                "", "")),
                                "part-2", List.of(new CodeReadingCodeLineDefinition(
                                                List.of(
                                                                token("main-public"), token("static"),
                                                                token("void"), token("main"),
                                                                new CodeReadingCodeTokenDefinition("string-array", "(",
                                                                                ""),
                                                                new CodeReadingCodeTokenDefinition("args", "", ")"),
                                                                token("main-open")),
                                                "",
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
