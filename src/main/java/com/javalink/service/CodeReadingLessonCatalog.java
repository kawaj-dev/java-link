package com.javalink.service;

import com.javalink.model.CodeReadingCardDefinition;
import com.javalink.model.CodeReadingCircuitDefinition;
import com.javalink.model.CodeReadingCodeLineDefinition;
import com.javalink.model.CodeReadingCodeTokenDefinition;
import com.javalink.model.CodeReadingExplanationEntry;
import com.javalink.model.CodeReadingExplanationSection;
import com.javalink.model.CodeReadingLessonDefinition;
import com.javalink.model.CodeReadingOfficialReference;
import com.javalink.model.CodeReadingOfficialSource;
import com.javalink.model.CodeReadingPart;
import com.javalink.model.CodeReadingStepDefinition;
import com.javalink.model.ExplanationSectionType;
import com.javalink.model.Lesson;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * コードリーディング教材に必要なLesson、問題、説明、表示構造を一元管理します。
 */
@Service
public class CodeReadingLessonCatalog {

        public static final String STAGE1_LESSON_ID = "hello-program-reading";
        public static final String STAGE2_LESSON_ID = "variable-program-reading";
        private static final String OFFICIAL_REFERENCES_TITLE =
                        "このページの技術的根拠：Javaの公式仕様・API";
        private static final String DEFAULT_LESSON_ID = STAGE1_LESSON_ID;
        private static final List<CodeReadingExplanationSection> SEMICOLON_EXPLANATION =
                        createSemicolonExplanation();
        private final Map<String, CodeReadingLessonDefinition> definitions;

        public CodeReadingLessonCatalog() {
                CodeReadingLessonDefinition stage1 = createStage1();
                CodeReadingLessonDefinition stage2 = createStage2();
                definitions = Map.of(
                                stage1.lessonId(), stage1,
                                stage2.lessonId(), stage2);
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

        private static CodeReadingLessonDefinition createStage2() {
                return new CodeReadingLessonDefinition(
                                STAGE2_LESSON_ID,
                                "Stage 2",
                                "変数を使って年齢を表示しよう",
                                """
                                                public class Main {
                                                    public static void main(String[] args) {
                                                        int age = 20;
                                                        System.out.println(age);
                                                    }
                                                }
                                                """,
                                createStage2Steps(),
                                createStage2Parts(),
                                createStage2Circuits(),
                                createStage2CodeLines(),
                                "20");
        }

        private static List<CodeReadingStepDefinition> createStage1Steps() {
                List<CodeReadingExplanationSection> blockEndExplanation = blockEndExplanation();
                return List.of(
                                richStep("class-public", 1, "public", "public", "accessible", "ほかの場所からも使える",
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
                                richStep("main-public", 5, "public", "public", "accessible", "ほかの場所からも使える",
                                                "アクセス修飾子", publicExplanation()),
                                richStep("static", 6, "static", "static", "without-instance", "インスタンスを作らなくても使える",
                                                "static修飾子", staticExplanation()),
                                richStep("void", 7, "void", "void", "no-return", "戻り値を返さない",
                                                "戻り値の型", voidExplanation()),
                                richStep("main", 8, "main", "main", "program-entry", "プログラム開始メソッド",
                                                "メソッド名", mainExplanation()),
                                richStep("string-array", 9, "String[]", "String[]", "multiple-strings", "文字列の配列",
                                                "引数の型", stringArrayExplanation()),
                                richStep("args", 10, "args", "args", "argument-variable", "変数の名前",
                                                "引数名", argsExplanation()),
                                richStep("main-open", 11, "{", "{", "block-start", "ここから始まる",
                                                "開始波かっこ", blockStartExplanation()),
                                richStep("print-command", 12, "System.out.println", "System.out.println",
                                                "display-and-newline", "画面に表示して改行する",
                                                "標準出力", systemOutPrintlnExplanation()),
                                richStep("hello-string", 13, "\"Hello\"", "\"Hello\"", "display-text", "表示する文字",
                                                "文字列リテラル", helloStringExplanation()),
                                richStep("semicolon", 14, ";", ";", "command-end", "文の終わり",
                                                "セミコロン", semicolonExplanation()),
                                richStep("main-close", 15, "}", "}", "close-main", "ここで終わる",
                                                "終了波かっこ", blockEndExplanation),
                                richStep("class-close", 16, "}", "}", "close-class", "ここで終わる",
                                                "終了波かっこ", blockEndExplanation));
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
                                tableSectionWithHeader(
                                                "アクセスできる範囲の違い",
                                                tableRow("書き方", "アクセスできる範囲"),
                                                tableRow("public", "🌎 ほかの場所からも使える"),
                                                tableRow("protected", "📦 同じパッケージ＋子クラス"),
                                                tableRow("（指定なし）", "📦 同じパッケージだけ"),
                                                tableRow("private", "🔒 自分のクラスだけ")),
                                textSection("ポイント",
                                                text("", "public", "が付いたクラス、メソッド、フィールドなどは、ほかのクラスからも利用できます。"),
                                                text("実際に利用できる範囲は、そのクラスやパッケージなどの条件にも関係します。")),
                                officialReferencesSection(
                                                OFFICIAL_REFERENCES_TITLE,
                                                jlsReference(
                                                                "§6.6",
                                                                "Access Control",
                                                                "Javaのアクセス制御について定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-6.html#jls-6.6"),
                                                jlsReference(
                                                                "§6.6.1",
                                                                "Determining Accessibility",
                                                                "public などで宣言された要素にアクセスできる条件を定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-6.html#jls-6.6.1")));
        }

        private static List<CodeReadingExplanationSection> staticExplanation() {
                return List.of(
                                textSection("static修飾子　static modifier",
                                                text("インスタンス（実体）を作らなくても、クラスから直接メソッドやフィールドを", "利用できる",
                                                                "ようにする修飾子です。")),
                                tableSectionWithHeader(
                                                "static修飾子の特徴",
                                                tableRow("書き方", "特徴"),
                                                tableRow("static", "インスタンス（実体）を作らなくても使える"),
                                                tableRow("（なし）", "インスタンス（実体）を作ってから使う")),
                                textSection("ポイント",
                                                text("Javaのプログラムを実行するとき、", "開始地点となる main メソッドにも、インスタンスを作らずに呼び出せるように static", " が付いています。")),
                                officialReferencesSection(
                                                OFFICIAL_REFERENCES_TITLE,
                                                jlsReference(
                                                                "§8.4.3.2",
                                                                "static Methods",
                                                                "static修飾子がメソッドをクラスメソッドとして定義し、特定のインスタンスを参照せずに呼び出されることを定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-8.html#jls-8.4.3.2"),
                                                jlsReference(
                                                                "§12.1.4",
                                                                "Invoke Test.main",
                                                                "Javaアプリケーションの開始時に呼び出されるmainメソッドについて定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-12.html#jls-12.1.4")));
        }

        private static List<CodeReadingExplanationSection> voidExplanation() {
                return List.of(
                                textSection("戻り値　return value",
                                                text("このメソッドが、", "値を返さない", "ことを表します。")),
                                tableSectionWithHeader(
                                                "戻り値の型の違い",
                                                tableRow("戻り値の型", "意味"),
                                                tableRow("void", "値を返さない"),
                                                tableRow("int", "int型の値を返す"),
                                                tableRow("String", "String型の値を返す")),
                                textSection("ポイント",
                                                text("画面に表示するだけの処理など、結果を返す必要がない場合に使います。")),
                                officialReferencesSection(
                                                OFFICIAL_REFERENCES_TITLE,
                                                jlsReference(
                                                                "§8.4.5",
                                                                "Method Result",
                                                                "メソッドが値を返さない場合に void キーワードを使用することを定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-8.html#jls-8.4.5"),
                                                jlsReference(
                                                                "§12.1.4",
                                                                "Invoke Test.main",
                                                                "プログラムの開始地点となる main メソッドが void で宣言されている必要があることを定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-12.html#jls-12.1.4")));
        }

        private static List<CodeReadingExplanationSection> mainExplanation() {
                return List.of(
                                textSection("メソッド名　method name",
                                                text("Javaが最初に実行する", "メソッドの名前",
                                                                "です。プログラムのスタート地点です。")),
                                tableSectionWithHeader(
                                                "mainメソッドとMainクラスの違い",
                                                tableRow("書き方", "意味"),
                                                tableRow("main", "Javaが最初に実行するメソッドの名前"),
                                                tableRow("Main", "自分で決めるクラスの名前")),
                                textSection("ポイント",
                                                text("", "main",
                                                                " はJavaのキーワードではありませんが、プログラムの開始に使われる決められたメソッド名です。")),
                                officialReferencesSection(
                                                OFFICIAL_REFERENCES_TITLE,
                                                jlsReference(
                                                                "§12.1.4",
                                                                "Invoke Test.main",
                                                                "プログラムの開始時に呼び出される main メソッドが備えるべき条件を定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-12.html#jls-12.1.4")));
        }

        private static List<CodeReadingExplanationSection> stringArrayExplanation() {
                return List.of(
                                textSection("配列型　array type",
                                                text("文字列（String）を複数まとめて扱うための型です。")),
                                tableSectionWithHeader(
                                                "String[]の意味",
                                                tableRow("書き方", "意味"),
                                                tableRow("String", "文字列を表す型です。"),
                                                tableRow("[]", "複数の値をまとめて扱う配列であることを表します。")),
                                textSection("ポイント",
                                                text("プログラムの実行時に、外から渡された文字列を受け取るために使います。")),
                                officialReferencesSection(
                                                OFFICIAL_REFERENCES_TITLE,
                                                jlsReference(
                                                                "§10.1",
                                                                "Array Types",
                                                                "配列型が要素の型とブラケット [] の組み合わせで構成されることを定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-10.html#jls-10.1"),
                                                jlsReference(
                                                                "§12.1.4",
                                                                "Invoke Test.main",
                                                                "main メソッドが引数として String の配列を受け取る必要があることを定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-12.html#jls-12.1.4")));
        }

        private static List<CodeReadingExplanationSection> argsExplanation() {
                return List.of(
                                textSection("引数名　argument name",
                                                text("渡された情報（引数）を受け取るための", "変数の名前", "です。")),
                                tableSectionWithHeader(
                                                "String[] と args の関係",
                                                tableRow("書き方", "意味"),
                                                tableRow("String[]", "受け取る値の型です"),
                                                tableRow("args", "受け取る変数の名前です")),
                                textSection("ポイント",
                                                text("", "args", " はJavaで決められたキーワードではなく、単なる「変数の名前」です。"),
                                                text("別の名前に変えても動きますが、", "args", "と書くのがJavaの世界で広く使われている慣習です。"),
                                                text("", "", "")),
                                officialReferencesSection(
                                                OFFICIAL_REFERENCES_TITLE,
                                                jlsReference(
                                                                "§12.1.4",
                                                                "Invoke Test.main",
                                                                "main メソッドが String の配列を形式パラメータとして1つ持つことを定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-12.html#jls-12.1.4")));
        }

        private static List<CodeReadingExplanationSection> classExplanation() {
                return List.of(
                                textSection("クラス　class　",
                                                text("", "「これからプログラムの設計図（クラス）を作ります」", "という意味です。")),
                                tableSection(
                                                "クラス宣言の基本形",
                                                tableRow("class クラス名 {\n}", ""),
                                                tableRow("書き方", "意味"),
                                                tableRow("class", "クラスを作る"),
                                                tableRow("クラス名", "作るクラスの名前（自分で決めることができます）"),
                                                tableRow("{ }", "クラスの中身を書く範囲")),
                                textSection("ポイント",
                                                text(" ", "class", "は、新しいクラスを定義するときに使うJavaのキーワードです。")),
                                officialReferencesSection(
                                                OFFICIAL_REFERENCES_TITLE,
                                                jlsReference(
                                                                "§8.1",
                                                                "Class Declarations",
                                                                "クラス宣言が新しいクラスを定義するものであることを定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-8.html#jls-8.1"),
                                                jlsReference(
                                                                "§3.9",
                                                                "Keywords",
                                                                "class がJavaの予約済キーワードであることを定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-3.html#jls-3.9")));
        }

        private static List<CodeReadingExplanationSection> classNameExplanation() {
                return List.of(
                                textSection("クラス名　class name",
                                                text("", "自分で決めるクラスの名前", "です。")),
                                textSection("名前を付けるときのルール",
                                                new CodeReadingExplanationEntry(
                                                                "1. 自由に決めてOK",
                                                                "予約語でなければ、好きな名前を付けられます。",
                                                                "", "", false),
                                                new CodeReadingExplanationEntry(
                                                                "2. 大文字で始めるのが慣習",
                                                                "", "Main や Test",
                                                                " のように、大文字で書き始めることが推奨されています。", false),
                                                new CodeReadingExplanationEntry(
                                                                "3. ファイル名と同じにする",
                                                                "", "public class Main と書いた場合、ファイル名は Main.java",
                                                                " にします。", false)),
                                textSection("ポイント",
                                                text("", "class", " キーワードの直後に書くことで、これから作るクラスの名前をJavaに伝えます。")),
                                officialReferencesSection(
                                                OFFICIAL_REFERENCES_TITLE,
                                                jlsReference(
                                                                "§8.1",
                                                                "Class Declarations",
                                                                "クラス宣言におけるTypeIdentifierが、そのクラスの名前を指定するものであることを定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-8.html#jls-8.1"),
                                                jlsReference(
                                                                "§7.6",
                                                                "Top Level Class and Interface Declarations",
                                                                "ファイルシステムを使用する場合の、publicなトップレベルクラスとファイル名の関係を定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-7.html#jls-7.6"),
                                                jlsReference(
                                                                "§6.1",
                                                                "Declarations",
                                                                "クラス名などに使われるJavaの命名規約について示しています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-6.html#jls-6.1")));
        }

        private static List<CodeReadingExplanationSection> blockStartExplanation() {
                return List.of(
                                textSection("セパレータ（左波括弧）　separator",
                                                text("クラスやメソッドの", "中身が、ここから始まる", "ことを表す記号です。")),
                                tableSection("波括弧のルール",
                                                tableRow("{ と } はセット", "{ で始まった範囲は、対応する } で終わります。")),
                                textSection("ポイント",
                                                text("閉じ忘れがあると、Javaはプログラムの構造を正しく理解できずエラーになります。")),
                                officialReferencesSection(
                                                OFFICIAL_REFERENCES_TITLE,
                                                jlsReference(
                                                                "§3.11",
                                                                "Separators",
                                                                "{ がJava言語において区切りを表す「セパレータ」という記号であることを定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-3.html#jls-3.11"),
                                                jlsReference(
                                                                "§8.1.7",
                                                                "Class Body and Member Declarations",
                                                                "クラスの本体が { で始まり } で終わることを規定しています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-8.html#jls-8.1.7"),
                                                jlsReference(
                                                                "§14.2",
                                                                "Blocks",
                                                                "メソッドの中身などに使われるブロックが { で始まり } で終わることを定義しています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-14.html#jls-14.2")));
        }

        private static List<CodeReadingExplanationSection> systemOutPrintlnExplanation() {
                return List.of(
                                textSection(
                                                "標準出力　standard output",
                                                text(
                                                                "標準出力に内容を表示し、",
                                                                "最後に改行します",
                                                                "。")),
                                tableSectionWithHeader(
                                                "System.out.println のしくみ",
                                                tableRow("書き方", "意味"),
                                                tableRow("System", "Java標準のクラスです"),
                                                tableRow("out", "標準出力を表すフィールドです"),
                                                tableRow("println", "内容を出力して改行するメソッドです")),
                                tableSectionWithHeader(
                                                "println と print の違い",
                                                tableRow("書き方", "違い"),
                                                tableRow("println", "表示したあと改行します"),
                                                tableRow("print", "表示したあと改行しません")),
                                officialReferencesSection(
                                                OFFICIAL_REFERENCES_TITLE,
                                                javaSeApiReference(
                                                                "System.out",
                                                                "Systemクラスのoutフィールドが、標準出力を表すPrintStream型のフィールドであることを定めています。",
                                                                "https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/System.html#out"),
                                                javaSeApiReference(
                                                                "PrintStream.println(String)",
                                                                "println(String)メソッドが内容を出力し、その後に行を終了させることを定めています。",
                                                                "https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/io/PrintStream.html#println(java.lang.String)")));
        }

        private static List<CodeReadingExplanationSection> helloStringExplanation() {
                return List.of(
                                textSection(
                                                "文字列リテラル　string literal",
                                                text(
                                                                "ダブルクォート \" \" で囲んで、",
                                                                "文字列を直接書いたもの",
                                                                "です。")),
                                tableSectionWithHeader(
                                                "ダブルクォート \" \" の中",
                                                tableRow("書き方", "意味"),
                                                tableRow("\"Hello\"", "英語を書くことができます。"),
                                                tableRow("\"こんにちは\"", "日本語を書くことができます。"),
                                                tableRow("\"Javaを勉強中\"", "文字を組み合わせて書くこともできます。"),
                                                tableRow("\"123\"", "数字も文字列として書くことができます。")),
                                textSection(
                                                "println との関係",
                                                text(
                                                                "\"Hello\" は println に渡す",
                                                                "引数",
                                                                "です。その内容が表示されます。")),
                                officialReferencesSection(
                                                OFFICIAL_REFERENCES_TITLE,
                                                jlsReference(
                                                                "§3.10.5",
                                                                "String Literals",
                                                                "ダブルクォートで囲まれた文字の並びが文字列リテラルであり、String型であることを定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-3.html#jls-3.10.5"),
                                                javaSeApiReference(
                                                                "java.lang.String",
                                                                "Javaプログラム内の文字列リテラルが、Stringクラスのインスタンスとして扱われることを示しています。",
                                                                "https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/String.html")));
        }

        private static List<CodeReadingExplanationSection> semicolonExplanation() {
                return SEMICOLON_EXPLANATION;
        }

        private static List<CodeReadingExplanationSection> createSemicolonExplanation() {
                return List.of(
                                textSection(
                                                "セパレータ（セミコロン）　separator",
                                                text(
                                                                "; は、Javaのコードで",
                                                                "文を区切るために使われる記号",
                                                                "です。")),
                                tableSection(
                                                "; が必要な場合と必要でない場合",
                                                tableRow3("種類", "例", ";"),
                                                tableRow3("式文・宣言文", "println(...); / int x = 1;", "必要"),
                                                tableRow3("return・break など", "return; / break;", "必要"),
                                                tableRow3("ブロック", "{ ... }", "不要"),
                                                tableRow3("クラス・メソッドの本体", "class Main { ... }", "不要")),
                                textSection(
                                                "ポイント",
                                                text(
                                                                "",
                                                                "if 文、while 文、for 文など、;",
                                                                "をつけずに書く部分もあります。")),
                                officialReferencesSection(
                                                OFFICIAL_REFERENCES_TITLE,
                                                jlsReference(
                                                                "§3.11",
                                                                "Separators",
                                                                "; がJava言語において区切りを表すセパレータの1つであることを定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-3.html#jls-3.11"),
                                                jlsReference(
                                                                "§14.8",
                                                                "Expression Statements",
                                                                "メソッド呼び出しなどの式の後ろに ; を書くことで、式文になることを定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-14.html#jls-14.8"),
                                                jlsReference(
                                                                "§14.5",
                                                                "Statements",
                                                                "Javaにはさまざまな種類の文があり、すべての文が ; で終わるわけではないことを示しています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-14.html#jls-14.5")));
        }

        private static List<CodeReadingExplanationSection> blockEndExplanation() {
                return List.of(
                                textSection(
                                                "セパレータ（右波括弧）　separator",
                                                text(
                                                                "} は、クラスやメソッドの",
                                                                "中身がここで終わる",
                                                                "ことを表す記号です。")),
                                textSection(
                                                "{ } を書くときのコツ",
                                                new CodeReadingExplanationEntry(
                                                                "{ }",
                                                                "「先に { } を書いてから中身を書く」という手法は、閉じ忘れによる構文エラーを防ぐための",
                                                                "一般的なプログラミング上の推奨習慣",
                                                                "です。",
                                                                false)),
                                textSection(
                                                "ポイント",
                                                new CodeReadingExplanationEntry(
                                                                "}",
                                                                "は、その範囲が始まったコードの先頭と",
                                                                "インデント（字下げ）の位置",
                                                                "をそろえて書くと、対応する範囲が分かりやすくなります。",
                                                                false)),
                                officialReferencesSection(
                                                OFFICIAL_REFERENCES_TITLE,
                                                jlsReference(
                                                                "§3.11",
                                                                "Separators",
                                                                "} がJava言語において区切りを表す「セパレータ」の1つであることを定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-3.html#jls-3.11"),
                                                jlsReference(
                                                                "§8.1.7",
                                                                "Class Body and Member Declarations",
                                                                "クラス本体（ClassBody）が { で始まり } で終わることを定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-8.html#jls-8.1.7"),
                                                jlsReference(
                                                                "§14.2",
                                                                "Blocks",
                                                                "メソッド本体などで使われるブロックが { で始まり } で終わることを定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-14.html#jls-14.2")));
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
                        sections.add(textSection("ポイント",
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

        private static CodeReadingExplanationSection tableSectionWithHeader(
                        String title,
                        CodeReadingExplanationEntry... rows) {
                return new CodeReadingExplanationSection(
                                ExplanationSectionType.TABLE,
                                title,
                                List.of(rows),
                                List.of(),
                                true);
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
                return new CodeReadingExplanationSection(
                                sectionType,
                                title,
                                List.of(entries),
                                List.of(),
                                false);
        }

        private static CodeReadingExplanationSection officialReferencesSection(
                        String title,
                        CodeReadingOfficialReference... references) {
                return new CodeReadingExplanationSection(
                                ExplanationSectionType.OFFICIAL_REFERENCES,
                                title,
                                List.of(),
                                List.of(references),
                                false);
        }

        private static CodeReadingOfficialReference jlsReference(
                        String sectionNumber,
                        String sectionTitle,
                        String description,
                        String uri) {
                return new CodeReadingOfficialReference(
                                CodeReadingOfficialSource.JLS,
                                "Java SE 21",
                                "JLS Java SE 21",
                                sectionNumber,
                                sectionTitle,
                                description,
                                URI.create(uri));
        }

        private static CodeReadingOfficialReference javaSeApiReference(
                        String sectionTitle,
                        String description,
                        String uri) {
                return new CodeReadingOfficialReference(
                                CodeReadingOfficialSource.JAVA_SE_API,
                                "Java SE 21",
                                "Java SE 21",
                                "API",
                                sectionTitle,
                                description,
                                URI.create(uri));
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

        private static List<CodeReadingStepDefinition> createStage2Steps() {
                List<CodeReadingExplanationSection> blockEnd = blockEndExplanation();
                List<CodeReadingExplanationSection> semicolon = semicolonExplanation();
                return List.of(
                                richStep("class-public", 1, "public", "public", "accessible", "ほかの場所からも使える",
                                                "アクセス修飾子", publicExplanation()),
                                richStep("class-keyword", 2, "class", "class", "declare-class", "クラスを作る",
                                                "クラスを作るキーワード", classExplanation()),
                                richStep("class-name", 3, "Main", "Main", "main-class-name", "クラスの名前",
                                                "クラス名", classNameExplanation()),
                                richStep("class-open", 4, "{", "{", "block-start", "ここから始まる",
                                                "開始波括弧", blockStartExplanation()),
                                richStep("main-public", 5, "public", "public", "accessible", "ほかの場所からも使える",
                                                "アクセス修飾子", publicExplanation()),
                                richStep("static", 6, "static", "static", "without-instance", "インスタンスを作らなくても使える",
                                                "static修飾子", staticExplanation()),
                                richStep("void", 7, "void", "void", "no-return", "戻り値を返さない",
                                                "戻り値の型", voidExplanation()),
                                richStep("main", 8, "main", "main", "program-entry", "プログラム開始メソッド",
                                                "メソッド名", mainExplanation()),
                                richStep("string-array", 9, "String[]", "String[]", "multiple-strings", "文字列の配列",
                                                "引数の型", stringArrayExplanation()),
                                richStep("args", 10, "args", "args", "argument-variable", "変数の名前",
                                                "引数名", argsExplanation()),
                                richStep("main-open", 11, "{", "{", "block-start", "ここから始まる",
                                                "開始波括弧", blockStartExplanation()),
                                richStep("int-type", 12, "int", "int", "integer-type", "整数を扱うデータ型",
                                                "整数型", intExplanation()),
                                richStep("age-declaration", 13, "age", "age", "variable-name", "変数の名前",
                                                "変数名", variableNameExplanation()),
                                richStep("assignment", 14, "=", "=", "assignment", "右側の値を左側に入れる",
                                                "代入演算子", assignmentExplanation()),
                                richStep("integer-literal", 15, "20", "20", "integer-value", "整数の値",
                                                "整数リテラル", integerLiteralExplanation()),
                                richStep("declaration-semicolon", 16, ";", ";", "command-end", "文の終わり",
                                                "セミコロン", semicolon),
                                richStep("print-command", 17, "System.out.println", "System.out.println", "display-and-newline",
                                                "画面に表示して改行する", "標準出力", systemOutPrintlnExplanation()),
                                richStep("age-use", 18, "age", "age", "use-variable-value", "変数の値を使う",
                                                "変数の利用", variableUseExplanation()),
                                richStep("print-semicolon", 19, ";", ";", "command-end", "文の終わり",
                                                "セミコロン", semicolon),
                                richStep("main-close", 20, "}", "}", "close-main", "ここで終わる",
                                                "終了波括弧", blockEnd),
                                richStep("class-close", 21, "}", "}", "close-class", "ここで終わる",
                                                "終了波括弧", blockEnd));
        }

        private static List<CodeReadingExplanationSection> intExplanation() {
                return List.of(
                                textSection("整数型　integral type", text("int は、小数点のない整数を扱うためのデータ型です。")),
                                dataTypeOverviewSection(),
                                basicDataTypesSection(),
                                textSection("",
                                                text("byte ～ boolean の8種類はプリミティブ型です。"),
                                                text("String は参照型（クラス型）です。")),
                                officialReferencesSection(OFFICIAL_REFERENCES_TITLE,
                                                jlsReference("§4.2.1", "Integral Types and Values",
                                                                "intが32ビットの符号付き整数型であり、扱える値の範囲を定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-4.html#jls-4.2.1"),
                                                jlsReference("§4.2", "Primitive Types and Values",
                                                                "8種類のプリミティブ型を定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-4.html#jls-4.2"),
                                                jlsReference("§4.3.3", "The Class String",
                                                                "Stringがクラス型の参照型であることを定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-4.html#jls-4.3.3")));
        }

        private static List<CodeReadingExplanationSection> variableNameExplanation() {
                return List.of(
                                textSection("変数名　variable name", text("変数につけた名前です。")),
                                variableOverviewSection(),
                                textSection("ポイント",
                                                new CodeReadingExplanationEntry(
                                                                "1. 基本的に自由",
                                                                "変数名は自分で決められます。",
                                                                "", "", false),
                                                new CodeReadingExplanationEntry(
                                                                "2. 予約語は使えない",
                                                                "", "int や class",
                                                                " などのJavaの予約語（keyword）は、変数名として使えません。", false),
                                                new CodeReadingExplanationEntry(
                                                                "3. 小文字から始めるのが慣習",
                                                                "Javaでは、変数名を小文字から始めるのが一般的です。",
                                                                "", "", false),
                                                new CodeReadingExplanationEntry(
                                                                "4. 複数の単語は2語目から大文字",
                                                                "複数の単語をつなげる場合は、2つ目以降の単語の先頭を大文字にします。\n例：",
                                                                "myAge", "", false)),
                                officialReferencesSection(OFFICIAL_REFERENCES_TITLE,
                                                jlsReference("§4.12", "Variables", "変数の種類と値の保持について定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-4.html#jls-4.12"),
                                                jlsReference("§3.8", "Identifiers", "変数名に使う識別子の規則を定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-3.html#jls-3.8"),
                                                jlsReference("§14.4.1", "Local Variable Declarators and Types", "ローカル変数の宣言子と型を定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-14.html#jls-14.4.1"),
                                                jlsReference("§6.1", "Declarations", "宣言とJavaの命名慣習について示しています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-6.html#jls-6.1")));
        }

        private static List<CodeReadingExplanationSection> assignmentExplanation() {
                return List.of(
                                textSection("代入演算子　assignment operator", text("右側の値を左側の変数に代入するための記号です。")),
                                tableSection("代入と初期化の違い",
                                                tableRow("", "変数を宣言するときに最初の値を入れることを「初期化」といいます。"),
                                                tableRow("", "初期化と代入では、どちらも = が使われます。"),
                                                tableRow3("種類", "コード", "意味"),
                                                tableRow3("初期化", "int age = 20;", "新しい変数を宣言するときに、最初の値を入れる"),
                                                tableRow3("代入", "age = 21;", "すでにある変数の値を、新しい値に変える")),
                                textSection("ポイント",
                                                text("Javaの = は、数学の「等しい」という意味ではありません。"),
                                                text("左右が等しいかを比較するときは == を使います。")),
                                officialReferencesSection(OFFICIAL_REFERENCES_TITLE,
                                                jlsReference("§15.26.1", "Simple Assignment Operator =", "単純代入演算子 = の動作を定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-15.html#jls-15.26.1"),
                                                jlsReference("§14.4", "Local Variable Declarations", "ローカル変数の宣言と初期化を定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-14.html#jls-14.4"),
                                                jlsReference("§15.21", "Equality Operators", "値が等しいかを比較する等価演算子を定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-15.html#jls-15.21")));
        }

        private static List<CodeReadingExplanationSection> integerLiteralExplanation() {
                return List.of(
                                textSection("整数リテラル　integer literal", text("プログラムの中に直接書かれた、整数の値です。")),
                                tableSection("リテラルとは",
                                                tableRow3("", "リテラルとは、プログラムの中に値を直接書いたものです。", ""),
                                                tableRow3("書き方", "種類", ""),
                                                tableRow3("20", "整数リテラル", ""),
                                                tableRow3("\"Hello\"", "文字列リテラル", "")),
                                tableSection("ポイント",
                                                tableRow3("", "書き方によって、Javaが扱うデータの種類が変わります。", ""),
                                                tableRow3("書き方", "データの種類", ""),
                                                tableRow3("20", "整数", ""),
                                                tableRow3("\"20\"", "文字列", "")),
                                officialReferencesSection(OFFICIAL_REFERENCES_TITLE,
                                                jlsReference("§3.10", "Literals", "Javaのリテラルの種類を定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-3.html#jls-3.10"),
                                                jlsReference("§3.10.1", "Integer Literals", "整数リテラルの書き方を定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-3.html#jls-3.10.1"),
                                                jlsReference("§15.8.1", "Lexical Literals", "式として評価されるリテラルを定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-15.html#jls-15.8.1")));
        }

        private static List<CodeReadingExplanationSection> variableUseExplanation() {
                return List.of(
                                variableUseOverviewSection(),
                                tableSection("変数を「宣言する」と「使う」の違い",
                                                tableRow3("種類", "コード", "意味"),
                                                tableRow3("宣言する", "int age = 20;", "変数を宣言して、最初の値を保存する"),
                                                tableRow3("使う", "System.out.println(age);", "変数に保存されている値を使う")),
                                tableSection("ポイント",
                                                tableRow("コード", "値の使い方"),
                                                tableRow("System.out.println(\"Hello\");", "直接書いた値を使う"),
                                                tableRow("System.out.println(age);", "変数に保存されている値を使う")),
                                officialReferencesSection(OFFICIAL_REFERENCES_TITLE,
                                                jlsReference("§6.5.6", "Meaning of Expression Names", "式の中の変数名が表す意味を定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-6.html#jls-6.5.6"),
                                                jlsReference("§15.1", "Evaluation, Denotation, and Result", "式の評価と結果について定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-15.html#jls-15.1"),
                                                jlsReference("§15.12", "Method Invocation Expressions", "メソッド呼び出し式と引数の評価を定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-15.html#jls-15.12")));
        }

        private static CodeReadingExplanationSection dataTypeOverviewSection() {
                return textSection("型（データ型）とは",
                                text("型（データ型）は、扱うデータの種類を決めるものです。"),
                                text("型は、「この箱（変数）にはどんな種類のものを入れるのか」を決める表示のようなものです。"));
        }

        private static CodeReadingExplanationSection basicDataTypesSection() {
                return tableSection("よく使う基本のデータ型",
                                tableRow3("分類", "型名", "格納するデータ"),
                                tableRow3("整数", "byte", "とても小さな整数"),
                                tableRow3("整数", "short", "小さな整数"),
                                tableRow3("整数", "int", "一般的な整数"),
                                tableRow3("整数", "long", "大きな整数"),
                                tableRow3("小数", "float", "精度の低い小数"),
                                tableRow3("小数", "double", "精度の高い小数"),
                                tableRow3("文字", "char", "1つの文字"),
                                tableRow3("真偽", "boolean", "true または false"),
                                tableRow3("文字列", "String", "文字の並び"));
        }

        private static CodeReadingExplanationSection variableOverviewSection() {
                return textSection("変数とは",
                                text("変数は、値を保存しておく記憶場所です。"),
                                text("📦 「", "値を入れておく箱", "」と考えるとイメージしやすくなります。"),
                                text("変数に保存した値は、あとから別の値に変えることができます。"));
        }

        private static CodeReadingExplanationSection variableUseOverviewSection() {
                return textSection("変数の利用　variable use",
                                text("変数の名前を書くことで、保存されている値を使うことができます。"));
        }

        private static CodeReadingExplanationEntry comparisonEntry3(
                        String label,
                        String code,
                        String description) {
                return new CodeReadingExplanationEntry(label, code, description, "", false);
        }

        private static CodeReadingExplanationEntry tableRow3(String first, String second, String third) {
                return new CodeReadingExplanationEntry(first, second, third, "", false);
        }

        private static CodeReadingExplanationEntry highlightedTableRow3(String first, String second, String third) {
                return new CodeReadingExplanationEntry(first, second, third, "", true);
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
                                                "Mainというクラスを宣言し、その中身を始める"),
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
                                                "mainメソッドを宣言し、その中身を始める"),
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

        private static List<CodeReadingCircuitDefinition> createStage2Circuits() {
                return List.of(
                                new CodeReadingCircuitDefinition("class-declaration", "public class Main {",
                                                List.of("class-public", "class-keyword", "class-name", "class-open"),
                                                List.of("public", "class", "Main", "{")),
                                new CodeReadingCircuitDefinition("main-method", "public static void main(String[] args) {",
                                                List.of("main-public", "static", "void", "main", "string-array", "args", "main-open"),
                                                List.of("public", "static", "void", "main", "String[]", "args", "{")),
                                new CodeReadingCircuitDefinition("variable-declaration", "int age = 20;",
                                                List.of("int-type", "age-declaration", "assignment", "integer-literal", "declaration-semicolon"),
                                                List.of("int", "age", "=", "20", ";")),
                                new CodeReadingCircuitDefinition("variable-output", "System.out.println(age);",
                                                List.of("print-command", "age-use", "print-semicolon"),
                                                List.of("System.out.println", "age", ";")),
                                new CodeReadingCircuitDefinition("block-closes", "mainメソッド終了 }　Mainクラス終了 }",
                                                List.of("main-close", "class-close"),
                                                List.of("mainメソッド終了 }", "Mainクラス終了 }")));
        }

        private static Map<String, List<CodeReadingCodeLineDefinition>> createStage2CodeLines() {
                return Map.of(
                                "part-1", List.of(line(List.of("class-public", "class-keyword", "class-name", "class-open"), "", "")),
                                "part-2", List.of(new CodeReadingCodeLineDefinition(
                                                List.of(token("main-public"), token("static"), token("void"), token("main"),
                                                                new CodeReadingCodeTokenDefinition("string-array", "(", ""),
                                                                new CodeReadingCodeTokenDefinition("args", "", ")"), token("main-open")),
                                                "", "quiz-reading-code-line--part-2")),
                                "part-3", List.of(line(List.of("int-type", "age-declaration", "assignment", "integer-literal", "declaration-semicolon"), "", "")),
                                "part-4", List.of(new CodeReadingCodeLineDefinition(
                                                List.of(token("print-command"), new CodeReadingCodeTokenDefinition("age-use", "(", ")"), token("print-semicolon")),
                                                "", "")),
                                "part-5", List.of(line(List.of("main-close", "class-close"), "", "")));
        }

        private static List<CodeReadingPart> createStage2Parts() {
                return List.of(
                                new CodeReadingPart("part-1", 1, "クラスを作る", List.of(), "public class Main {",
                                                List.of("class-public", "class-keyword", "class-name", "class-open"),
                                                List.of("public", "class", "Main", "{"), List.of(),
                                                "Mainというクラスを宣言し、その中身を始める"),
                                new CodeReadingPart("part-2", 2, "mainメソッドを作る", List.of(), "public static void main(String[] args) {",
                                                List.of("main-public", "static", "void", "main", "string-array", "args", "main-open"),
                                                List.of("public", "static", "void", "main", "String[]", "args", "{"), List.of(),
                                                "mainメソッドを宣言し、その中身を始める"),
                                new CodeReadingPart("part-3", 3, "変数を作る", List.of("整数を保存する変数を作ります。"), "int age = 20;",
                                                List.of("int-type", "age-declaration", "assignment", "integer-literal", "declaration-semicolon"),
                                                List.of("int", "age", "=", "20", ";"), List.of(),
                                                "int型の変数ageを宣言し、20で初期化する"),
                                new CodeReadingPart("part-4", 4, "変数を表示する", List.of("変数に保存した値を表示します。"), "System.out.println(age);",
                                                List.of("print-command", "age-use", "print-semicolon"),
                                                List.of("System.out.println", "age", ";"), List.of(),
                                                "ageに保存されている値を表示して改行する"),
                                new CodeReadingPart("part-5", 5, "プログラムを閉じる", List.of(), "}\n}",
                                                List.of("main-close", "class-close"), List.of("}", "}"), List.of(),
                                                "mainメソッドとMainクラスを順番に終了する"));
        }
}
