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
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * コードリーディング教材に必要なLesson、問題、説明、表示構造を一元管理します。
 */
@Service
public class CodeReadingLessonCatalog {

        public static final String STAGE1_LESSON_ID = "hello-program-reading";
        public static final String STAGE2_LESSON_ID = "variable-program-reading";
        public static final String STAGE3_LESSON_ID = "operator-program-reading";
        public static final String STAGE4_LESSON_ID = "conditional-program-reading";
        public static final String STAGE5_LESSON_ID = "loop-program-reading";
        public static final String STAGE6_LESSON_ID = "array-program-reading";
        public static final String STAGE7_LESSON_ID = "method-program-reading";
        public static final String STAGE8_LESSON_ID = "car-instance-reading";
        private static final String OFFICIAL_REFERENCES_TITLE =
                        "このページの技術的根拠：Javaの公式仕様・API";
        private static final String DISPLAY_AND_NEWLINE_CARD = "画面に表示して改行する";
        private static final String VARIABLE_VALUE_USE_CARD = "変数の値を使う";
        private static final String PUBLIC_CARD = "ほかの場所からも使える";
        private static final String CLASS_CARD = "クラスを作る";
        private static final String CLASS_NAME_CARD = "クラスの名前";
        private static final String BLOCK_START_CARD = "ここから始まる";
        private static final String STATIC_CARD = "インスタンスを作らなくても使える";
        private static final String VOID_CARD = "戻り値を返さない";
        private static final String MAIN_CARD = "プログラム開始メソッド";
        private static final String STRING_ARRAY_CARD = "文字列の配列";
        private static final String ARGS_CARD = "変数の名前";
        private static final String DEFAULT_LESSON_ID = STAGE1_LESSON_ID;
        private static final List<CodeReadingExplanationSection> PUBLIC_EXPLANATION =
                        publicExplanation();
        private static final List<CodeReadingExplanationSection> CLASS_EXPLANATION =
                        classExplanation();
        private static final List<CodeReadingExplanationSection> CLASS_NAME_EXPLANATION =
                        classNameExplanation();
        private static final List<CodeReadingExplanationSection> BLOCK_START_EXPLANATION =
                        blockStartExplanation();
        private static final List<CodeReadingExplanationSection> STATIC_EXPLANATION =
                        staticExplanation();
        private static final List<CodeReadingExplanationSection> VOID_EXPLANATION =
                        voidExplanation();
        private static final List<CodeReadingExplanationSection> MAIN_EXPLANATION =
                        mainExplanation();
        private static final List<CodeReadingExplanationSection> STRING_ARRAY_EXPLANATION =
                        stringArrayExplanation();
        private static final List<CodeReadingExplanationSection> ARGS_EXPLANATION =
                        argsExplanation();
        private static final List<CodeReadingExplanationSection> SYSTEM_OUT_PRINTLN_EXPLANATION =
                        systemOutPrintlnExplanation();
        private static final List<CodeReadingExplanationSection> SEMICOLON_EXPLANATION =
                        createSemicolonExplanation();
        private static final List<CodeReadingExplanationSection> BLOCK_END_EXPLANATION =
                        blockEndExplanation();
        private final List<CodeReadingLessonDefinition> definitionsInOrder;
        private final Map<String, CodeReadingLessonDefinition> definitions;

        public CodeReadingLessonCatalog() {
                CodeReadingLessonDefinition stage1 = createStage1();
                CodeReadingLessonDefinition stage2 = createStage2();
                CodeReadingLessonDefinition stage3 = createStage3(stage2);
                CodeReadingLessonDefinition stage4 = createStage4(stage2);
                CodeReadingLessonDefinition stage5 = createStage5(stage2);
                CodeReadingLessonDefinition stage6 = createStage6(stage2, stage5);
                CodeReadingLessonDefinition stage7 = createStage7(stage2, stage3);
                CodeReadingLessonDefinition stage8 = createStage8(stage2, stage4);
                definitionsInOrder = List.of(stage1, stage2, stage3, stage4, stage5, stage6, stage7, stage8);
                definitions = definitionsInOrder.stream().collect(Collectors.toUnmodifiableMap(
                                CodeReadingLessonDefinition::lessonId,
                                Function.identity()));
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
                return definitionsInOrder.stream()
                                .map(CodeReadingLessonDefinition::toLesson)
                                .toList();
        }

        public Optional<CodeReadingLessonDefinition> getNextDefinition(String lessonId) {
                for (int index = 0; index < definitionsInOrder.size(); index++) {
                        if (definitionsInOrder.get(index).lessonId().equals(lessonId)) {
                                return index + 1 < definitionsInOrder.size()
                                                ? Optional.of(definitionsInOrder.get(index + 1))
                                                : Optional.empty();
                        }
                }
                throw new IllegalArgumentException(
                                "コードリーディング教材が見つかりません。lessonId: " + lessonId);
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

        private static CodeReadingLessonDefinition createStage3(CodeReadingLessonDefinition stage2) {
                return new CodeReadingLessonDefinition(
                                STAGE3_LESSON_ID,
                                "Stage 3",
                                "演算子を使って計算しよう",
                                """
                                                public class Main {
                                                    public static void main(String[] args) {
                                                        int a = 10;
                                                        int b = 20;
                                                        int c = a + b;
                                                        System.out.println(c);
                                                    }
                                                }
                                                """,
                                createStage3Steps(stage2),
                                createStage3Parts(),
                                createStage3Circuits(),
                                createStage3CodeLines(),
                                "30");
        }

        private static CodeReadingLessonDefinition createStage4(CodeReadingLessonDefinition stage2) {
                return new CodeReadingLessonDefinition(
                                STAGE4_LESSON_ID,
                                "Stage 4",
                                "信号を見て行動を決めよう",
                                """
                                                public class Main {
                                                    public static void main(String[] args) {
                                                        String signal = "青";

                                                        if (signal.equals("青")) {
                                                            System.out.println("進む");
                                                        } else {
                                                            System.out.println("止まる");
                                                        }
                                                    }
                                                }
                                                """,
                                createStage4Steps(stage2),
                                createStage4Parts(),
                                createStage4Circuits(),
                                createStage4CodeLines(),
                                "進む");
        }

        private static CodeReadingLessonDefinition createStage5(CodeReadingLessonDefinition stage2) {
                return new CodeReadingLessonDefinition(
                                STAGE5_LESSON_ID,
                                "Stage 5",
                                "繰り返し処理を使って数字を表示しよう",
                                """
                                                public class Main {
                                                    public static void main(String[] args) {

                                                        for (int i = 1; i <= 5; i++) {
                                                            System.out.println(i);
                                                        }

                                                    }
                                                }
                                                """,
                                createStage5Steps(stage2),
                                createStage5Parts(),
                                createStage5Circuits(),
                                createStage5CodeLines(),
                                "1\n2\n3\n4\n5");
        }

        private static CodeReadingLessonDefinition createStage6(
                        CodeReadingLessonDefinition stage2,
                        CodeReadingLessonDefinition stage5) {
                return new CodeReadingLessonDefinition(
                                STAGE6_LESSON_ID,
                                "Stage 6",
                                "配列を使って複数の数字を表示しよう",
                                """
                                                public class Main {
                                                    public static void main(String[] args) {
                                                        int[] scores = {80, 90, 100};

                                                        for (int i = 0; i < scores.length; i++) {
                                                            System.out.println(scores[i]);
                                                        }
                                                    }
                                                }
                                                """,
                                createStage6Steps(stage2, stage5),
                                createStage6Parts(),
                                createStage6Circuits(),
                                createStage6CodeLines(),
                                "80\n90\n100");
        }

        private static CodeReadingLessonDefinition createStage7(
                        CodeReadingLessonDefinition stage2,
                        CodeReadingLessonDefinition stage3) {
                return new CodeReadingLessonDefinition(
                                STAGE7_LESSON_ID,
                                "Stage 7",
                                "メソッドを使って計算結果を表示しよう",
                                """
                                                public class Main {
                                                    public static void main(String[] args) {
                                                        int result = add(10, 20);
                                                        System.out.println(result);
                                                    }

                                                    public static int add(int a, int b) {
                                                        return a + b;
                                                    }
                                                }
                                                """,
                                createStage7Steps(stage2, stage3),
                                createStage7Parts(),
                                createStage7Circuits(),
                                createStage7CodeLines(),
                                "30");
        }

        private static CodeReadingLessonDefinition createStage8(
                        CodeReadingLessonDefinition stage2,
                        CodeReadingLessonDefinition stage4) {
                return new CodeReadingLessonDefinition(
                                STAGE8_LESSON_ID,
                                "Stage 8",
                                "クラスから2台の車を作ってみよう",
                                """
                                                public class Main {
                                                    public static void main(String[] args) {
                                                        Car car1 = new Car();
                                                        Car car2 = new Car();

                                                        car1.color = "red";
                                                        car2.color = "blue";

                                                        car1.showColor();
                                                        car2.showColor();
                                                    }
                                                }

                                                class Car {
                                                    String color;

                                                    void showColor() {
                                                        System.out.println(color);
                                                    }
                                                }
                                                """,
                                createStage8Steps(stage2, stage4),
                                createStage8Parts(),
                                createStage8Circuits(),
                                createStage8CodeLines(),
                                "red\nblue");
        }

        private static List<CodeReadingStepDefinition> createStage1Steps() {
                return List.of(
                                richStep("class-public", 1, "public", "public", "accessible", PUBLIC_CARD,
                                                "アクセス修飾子",
                                                PUBLIC_EXPLANATION),
                                richStep("class-keyword", 2, "class", "class", "declare-class", CLASS_CARD,
                                                "クラスを作るキーワード",
                                                CLASS_EXPLANATION),
                                richStep("class-name", 3, "Main", "Main", "main-class-name", CLASS_NAME_CARD,
                                                "クラス名",
                                                CLASS_NAME_EXPLANATION),
                                richStep("class-open", 4, "{", "{", "block-start", BLOCK_START_CARD,
                                                "開始波かっこ", BLOCK_START_EXPLANATION),
                                richStep("main-public", 5, "public", "public", "accessible", PUBLIC_CARD,
                                                "アクセス修飾子", PUBLIC_EXPLANATION),
                                richStep("static", 6, "static", "static", "without-instance", STATIC_CARD,
                                                "static修飾子", STATIC_EXPLANATION),
                                richStep("void", 7, "void", "void", "no-return", VOID_CARD,
                                                "戻り値の型", VOID_EXPLANATION),
                                richStep("main", 8, "main", "main", "program-entry", MAIN_CARD,
                                                "メソッド名", MAIN_EXPLANATION),
                                richStep("string-array", 9, "String[]", "String[]", "multiple-strings", STRING_ARRAY_CARD,
                                                "引数の型", STRING_ARRAY_EXPLANATION),
                                richStep("args", 10, "args", "args", "argument-variable", ARGS_CARD,
                                                "引数名", ARGS_EXPLANATION),
                                richStep("main-open", 11, "{", "{", "block-start", BLOCK_START_CARD,
                                                "開始波かっこ", BLOCK_START_EXPLANATION),
                                richStep("print-command", 12, "System.out.println", "System.out.println",
                                                "display-and-newline", DISPLAY_AND_NEWLINE_CARD,
                                                "標準出力", SYSTEM_OUT_PRINTLN_EXPLANATION),
                                richStep("hello-string", 13, "\"Hello\"", "\"Hello\"", "display-text", "表示する文字",
                                                "文字列リテラル", helloStringExplanation()),
                                richStep("semicolon", 14, ";", ";", "command-end", "文の終わり",
                                                "セミコロン", semicolonExplanation()),
                                richStep("main-close", 15, "}", "}", "close-main", "ここで終わる",
                                                "終了波かっこ", BLOCK_END_EXPLANATION),
                                richStep("class-close", 16, "}", "}", "close-class", "ここで終わる",
                                                "終了波かっこ", BLOCK_END_EXPLANATION));
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
                                                text("クラス、メソッド、if文、else節などの", "ブロックが、ここから始まる", "ことを表す記号です。")),
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
                                                                "} は、クラス、メソッド、if文、else節などの",
                                                                "ブロックがここで終わる",
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
                List<CodeReadingExplanationSection> semicolon = semicolonExplanation();
                return List.of(
                                richStep("class-public", 1, "public", "public", "accessible", PUBLIC_CARD,
                                                "アクセス修飾子", PUBLIC_EXPLANATION),
                                richStep("class-keyword", 2, "class", "class", "declare-class", CLASS_CARD,
                                                "クラスを作るキーワード", CLASS_EXPLANATION),
                                richStep("class-name", 3, "Main", "Main", "main-class-name", CLASS_NAME_CARD,
                                                "クラス名", CLASS_NAME_EXPLANATION),
                                richStep("class-open", 4, "{", "{", "block-start", BLOCK_START_CARD,
                                                "開始波括弧", BLOCK_START_EXPLANATION),
                                richStep("main-public", 5, "public", "public", "accessible", PUBLIC_CARD,
                                                "アクセス修飾子", PUBLIC_EXPLANATION),
                                richStep("static", 6, "static", "static", "without-instance", STATIC_CARD,
                                                "static修飾子", STATIC_EXPLANATION),
                                richStep("void", 7, "void", "void", "no-return", VOID_CARD,
                                                "戻り値の型", VOID_EXPLANATION),
                                richStep("main", 8, "main", "main", "program-entry", MAIN_CARD,
                                                "メソッド名", MAIN_EXPLANATION),
                                richStep("string-array", 9, "String[]", "String[]", "multiple-strings", STRING_ARRAY_CARD,
                                                "引数の型", STRING_ARRAY_EXPLANATION),
                                richStep("args", 10, "args", "args", "argument-variable", ARGS_CARD,
                                                "引数名", ARGS_EXPLANATION),
                                richStep("main-open", 11, "{", "{", "block-start", BLOCK_START_CARD,
                                                "開始波括弧", BLOCK_START_EXPLANATION),
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
                                                DISPLAY_AND_NEWLINE_CARD, "標準出力", SYSTEM_OUT_PRINTLN_EXPLANATION),
                                richStep("age-use", 18, "age", "age", "use-variable-value", VARIABLE_VALUE_USE_CARD,
                                                "変数の利用", variableUseExplanation()),
                                richStep("print-semicolon", 19, ";", ";", "command-end", "文の終わり",
                                                "セミコロン", semicolon),
                                richStep("main-close", 20, "}", "}", "close-main", "ここで終わる",
                                                "終了波括弧", BLOCK_END_EXPLANATION),
                                richStep("class-close", 21, "}", "}", "close-class", "ここで終わる",
                                                "終了波括弧", BLOCK_END_EXPLANATION));
        }

        private static List<CodeReadingStepDefinition> createStage3Steps(CodeReadingLessonDefinition stage2) {
                List<CodeReadingExplanationSection> variableName =
                                sharedExplanation(stage2, "age-declaration");
                List<CodeReadingExplanationSection> integerType =
                                sharedExplanation(stage2, "int-type");
                List<CodeReadingExplanationSection> assignment =
                                sharedExplanation(stage2, "assignment");
                List<CodeReadingExplanationSection> integerLiteral =
                                sharedExplanation(stage2, "integer-literal");
                List<CodeReadingExplanationSection> semicolon = semicolonExplanation();
                List<CodeReadingExplanationSection> operand =
                                sharedExplanation(stage2, "age-use");
                return List.of(
                                richStep("class-public", 1, "public", "public", "accessible", PUBLIC_CARD,
                                                "アクセス修飾子", PUBLIC_EXPLANATION),
                                richStep("class-keyword", 2, "class", "class", "declare-class", CLASS_CARD,
                                                "クラスを作るキーワード", CLASS_EXPLANATION),
                                richStep("class-name", 3, "Main", "Main", "main-class-name", CLASS_NAME_CARD,
                                                "クラス名", CLASS_NAME_EXPLANATION),
                                richStep("class-open", 4, "{", "{", "block-start", BLOCK_START_CARD,
                                                "開始波括弧", BLOCK_START_EXPLANATION),
                                richStep("main-public", 5, "public", "public", "accessible", PUBLIC_CARD,
                                                "アクセス修飾子", PUBLIC_EXPLANATION),
                                richStep("static", 6, "static", "static", "without-instance", STATIC_CARD,
                                                "static修飾子", STATIC_EXPLANATION),
                                richStep("void", 7, "void", "void", "no-return", VOID_CARD,
                                                "戻り値の型", VOID_EXPLANATION),
                                richStep("main", 8, "main", "main", "program-entry", MAIN_CARD,
                                                "メソッド名", MAIN_EXPLANATION),
                                richStep("string-array", 9, "String[]", "String[]", "multiple-strings", STRING_ARRAY_CARD,
                                                "引数の型", STRING_ARRAY_EXPLANATION),
                                richStep("args", 10, "args", "args", "argument-variable", ARGS_CARD,
                                                "引数名", ARGS_EXPLANATION),
                                richStep("main-open", 11, "{", "{", "block-start", BLOCK_START_CARD,
                                                "開始波括弧", BLOCK_START_EXPLANATION),
                                richStep("a-int", 12, "int", "int", "integer-type", "整数を扱うデータ型",
                                                "整数型", integerType),
                                richStep("a-name", 13, "a", "a", "variable-name", "変数の名前",
                                                "変数名", variableName),
                                richStep("a-assignment", 14, "=", "=", "assignment", "右側の値を左側に入れる",
                                                "代入演算子", assignment),
                                richStep("a-literal", 15, "10", "10", "integer-value", "整数の値",
                                                "整数リテラル", integerLiteral),
                                richStep("a-semicolon", 16, ";", ";", "command-end", "文の終わり",
                                                "セミコロン", semicolon),
                                richStep("b-int", 17, "int", "int", "integer-type", "整数を扱うデータ型",
                                                "整数型", integerType),
                                richStep("b-name", 18, "b", "b", "variable-name", "変数の名前",
                                                "変数名", variableName),
                                richStep("b-assignment", 19, "=", "=", "assignment", "右側の値を左側に入れる",
                                                "代入演算子", assignment),
                                richStep("b-literal", 20, "20", "20", "integer-value", "整数の値",
                                                "整数リテラル", integerLiteral),
                                richStep("b-semicolon", 21, ";", ";", "command-end", "文の終わり",
                                                "セミコロン", semicolon),
                                richStep("c-int", 22, "int", "int", "integer-type", "整数を扱うデータ型",
                                                "整数型", integerType),
                                richStep("c-name", 23, "c", "c", "variable-name", "変数の名前",
                                                "変数名", variableName),
                                richStep("c-assignment", 24, "=", "=", "assignment", "右側の値を左側に入れる",
                                                "代入演算子", assignment),
                                richStep("a-use", 25, "a", "a", "use-variable-value", VARIABLE_VALUE_USE_CARD,
                                                "変数の利用", operand),
                                richStep("addition", 26, "+", "+", "addition", "左右の数値を足す",
                                                "算術演算子", arithmeticOperatorExplanation()),
                                richStep("b-use", 27, "b", "b", "use-variable-value", VARIABLE_VALUE_USE_CARD,
                                                "変数の利用", operand),
                                richStep("c-semicolon", 28, ";", ";", "command-end", "文の終わり",
                                                "セミコロン", semicolon),
                                richStep("print-command", 29, "System.out.println", "System.out.println", "display-and-newline",
                                                DISPLAY_AND_NEWLINE_CARD, "標準出力",
                                                SYSTEM_OUT_PRINTLN_EXPLANATION),
                                richStep("c-use", 30, "c", "c", "use-variable-value", VARIABLE_VALUE_USE_CARD,
                                                "変数の利用", operand),
                                richStep("print-semicolon", 31, ";", ";", "command-end", "文の終わり",
                                                "セミコロン", semicolon),
                                richStep("main-close", 32, "}", "}", "close-main", "ここで終わる",
                                                "終了波括弧", BLOCK_END_EXPLANATION),
                                richStep("class-close", 33, "}", "}", "close-class", "ここで終わる",
                                                "終了波括弧", BLOCK_END_EXPLANATION));
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
                                                tableHeaderRow3("種類", "コード", "意味"),
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
                                                tableHeaderRow("書き方", "種類"),
                                                tableRow3("20", "整数リテラル", ""),
                                                tableRow3("\"Hello\"", "文字列リテラル", "")),
                                tableSection("ポイント",
                                                tableRow3("", "書き方によって、Javaが扱うデータの種類が変わります。", ""),
                                                tableHeaderRow("書き方", "データの種類"),
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
                                                tableHeaderRow3("種類", "コード", "意味"),
                                                tableRow3("宣言する", "int age = 20;", "変数を宣言して、最初の値を保存する"),
                                                tableRow3("使う", "System.out.println(age);", "変数に保存されている値を使う")),
                                tableSection("値を直接使う場合と、変数を使う場合",
                                                tableHeaderRow("書き方", "値の使い方"),
                                                tableRow("20", "値を直接書いて使う"),
                                                tableRow("age", "変数に保存されている値を使う"),
                                                tableRow("", "変数名を書いた場所では、その変数に保存されている値が使われます。")),
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
                return tableSectionWithHeader("よく使う基本のデータ型",
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

        private static CodeReadingExplanationEntry tableRow4(
                        String first,
                        String second,
                        String third,
                        String fourth) {
                return new CodeReadingExplanationEntry(first, second, third, fourth, false);
        }

        private static CodeReadingExplanationEntry tableHeaderRow(String first, String second) {
                return new CodeReadingExplanationEntry(first, second, "", "", "", true, false);
        }

        private static CodeReadingExplanationEntry tableHeaderRow3(String first, String second, String third) {
                return new CodeReadingExplanationEntry(first, second, third, "", "", true, false);
        }

        private static CodeReadingExplanationEntry highlightedTableRow3(String first, String second, String third) {
                return new CodeReadingExplanationEntry(first, second, third, "", true);
        }

        private static List<CodeReadingExplanationSection> sharedExplanation(
                        CodeReadingLessonDefinition definition,
                        String stepId) {
                return definition.getStep(stepId).explanationSections();
        }

        private static List<CodeReadingExplanationSection> arithmeticOperatorExplanation() {
                return List.of(
                                textSection("算術演算子　arithmetic operator",
                                                text("数値を使って計算するときに使う記号です。")),
                                tableSectionWithHeader("+ 演算子の2つの働き（数値の加算と文字列の結合）",
                                                tableRow4("働き", "コード例", "結果", "ルール"),
                                                tableRow4("数値の加算", "10 + 20", "30", "両方が数値なら、足し算をします。"),
                                                tableRow4("文字列の結合", "\"10\" + \"20\"", "\"1020\"",
                                                                "両方が文字列なら、文字をそのままつなぎます。"),
                                                tableRow4("文字列の結合", "10 + \"20\"", "\"1020\"",
                                                                "片方が String なら、もう片方も文字列に変換してつなぎます。")),
                                tableSectionWithHeader("代表的な算術演算子",
                                                tableRow4("演算子", "機能", "計算例", "結果"),
                                                tableRow4("+", "加算演算子（足し算）", "10 + 3", "13"),
                                                tableRow4("-", "減算演算子（引き算）", "10 - 3", "7"),
                                                tableRow4("*", "乗算演算子（掛け算）", "10 * 3", "30"),
                                                tableRow4("/", "除算演算子（割り算）", "10 / 3", "3"),
                                                tableRow4("%", "剰余演算子（割り算の余り）", "10 % 3", "1")),
                                textSection("",
                                                text("int同士の割り算では、小数部分は切り捨てられ、結果は0に近い方の整数になります。"),
                                                text("10 / 3 → 3")),
                                officialReferencesSection(OFFICIAL_REFERENCES_TITLE,
                                                jlsReference("§15.18", "Additive Operators",
                                                                "加算演算子と減算演算子の全体的な規則を定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-15.html#jls-15.18"),
                                                jlsReference("§15.18.1", "String Concatenation Operator +",
                                                                "文字列を + で結合するときの動作を定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-15.html#jls-15.18.1"),
                                                jlsReference("§15.18.2", "Additive Operators (+ and -) for Numeric Types",
                                                                "数値に対する加算演算子 + と減算演算子 - の動作を定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-15.html#jls-15.18.2"),
                                                jlsReference("§15.17", "Multiplicative Operators",
                                                                "乗算、除算、剰余の演算子と、整数除算の動作を定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-15.html#jls-15.17"),
                                                jlsReference("§15.17.2", "Division Operator /",
                                                                "整数同士の除算が0に向かって丸められることを定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-15.html#jls-15.17.2"),
                                                jlsReference("§15.17.3", "Remainder Operator %",
                                                                "剰余演算子 % の動作を定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-15.html#jls-15.17.3")));
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

        private static List<CodeReadingCircuitDefinition> createStage3Circuits() {
                return List.of(
                                new CodeReadingCircuitDefinition("class-declaration", "public class Main {",
                                                List.of("class-public", "class-keyword", "class-name", "class-open"),
                                                List.of("public", "class", "Main", "{")),
                                new CodeReadingCircuitDefinition("main-method", "public static void main(String[] args) {",
                                                List.of("main-public", "static", "void", "main", "string-array", "args", "main-open"),
                                                List.of("public", "static", "void", "main", "String[]", "args", "{")),
                                new CodeReadingCircuitDefinition("variable-a", "int a = 10;",
                                                List.of("a-int", "a-name", "a-assignment", "a-literal", "a-semicolon"),
                                                List.of("int", "a", "=", "10", ";")),
                                new CodeReadingCircuitDefinition("variable-b", "int b = 20;",
                                                List.of("b-int", "b-name", "b-assignment", "b-literal", "b-semicolon"),
                                                List.of("int", "b", "=", "20", ";")),
                                new CodeReadingCircuitDefinition("addition", "int c = a + b;",
                                                List.of("c-int", "c-name", "c-assignment", "a-use", "addition", "b-use", "c-semicolon"),
                                                List.of("int", "c", "=", "a", "+", "b", ";")),
                                new CodeReadingCircuitDefinition("result-output", "System.out.println(c);",
                                                List.of("print-command", "c-use", "print-semicolon"),
                                                List.of("System.out.println", "c", ";")),
                                new CodeReadingCircuitDefinition("block-closes", "mainメソッド終了 }　Mainクラス終了 }",
                                                List.of("main-close", "class-close"),
                                                List.of("mainメソッド終了 }", "Mainクラス終了 }")));
        }

        private static Map<String, List<CodeReadingCodeLineDefinition>> createStage3CodeLines() {
                return Map.of(
                                "part-1", List.of(line(
                                                List.of("class-public", "class-keyword", "class-name", "class-open"), "", "")),
                                "part-2", List.of(new CodeReadingCodeLineDefinition(
                                                List.of(token("main-public"), token("static"), token("void"), token("main"),
                                                                new CodeReadingCodeTokenDefinition("string-array", "(", ""),
                                                                new CodeReadingCodeTokenDefinition("args", "", ")"), token("main-open")),
                                                "", "quiz-reading-code-line--part-2")),
                                "part-3", List.of(line(
                                                List.of("a-int", "a-name", "a-assignment", "a-literal", "a-semicolon"), "", "")),
                                "part-4", List.of(line(
                                                List.of("b-int", "b-name", "b-assignment", "b-literal", "b-semicolon"), "", "")),
                                "part-5", List.of(line(
                                                List.of("c-int", "c-name", "c-assignment", "a-use", "addition", "b-use", "c-semicolon"), "", "")),
                                "part-6", List.of(new CodeReadingCodeLineDefinition(
                                                List.of(token("print-command"),
                                                                new CodeReadingCodeTokenDefinition("c-use", "(", ")"),
                                                                token("print-semicolon")),
                                                "", "")),
                                "part-7", List.of(line(List.of("main-close", "class-close"), "", "")));
        }

        private static List<CodeReadingPart> createStage3Parts() {
                return List.of(
                                new CodeReadingPart("part-1", 1, "クラスを作る", List.of(), "public class Main {",
                                                List.of("class-public", "class-keyword", "class-name", "class-open"),
                                                List.of("public", "class", "Main", "{"), List.of(),
                                                "Mainというクラスを宣言し、その中身を始める"),
                                new CodeReadingPart("part-2", 2, "mainメソッドを作る", List.of(), "public static void main(String[] args) {",
                                                List.of("main-public", "static", "void", "main", "string-array", "args", "main-open"),
                                                List.of("public", "static", "void", "main", "String[]", "args", "{"), List.of(),
                                                "mainメソッドを宣言し、その中身を始める"),
                                new CodeReadingPart("part-3", 3, "変数 a を作る", List.of("整数を保存する変数aを作ります。"), "int a = 10;",
                                                List.of("a-int", "a-name", "a-assignment", "a-literal", "a-semicolon"),
                                                List.of("int", "a", "=", "10", ";"), List.of(),
                                                "int型の変数aを宣言し、10で初期化する"),
                                new CodeReadingPart("part-4", 4, "変数 b を作る", List.of("整数を保存する変数bを作ります。"), "int b = 20;",
                                                List.of("b-int", "b-name", "b-assignment", "b-literal", "b-semicolon"),
                                                List.of("int", "b", "=", "20", ";"), List.of(),
                                                "int型の変数bを宣言し、20で初期化する"),
                                new CodeReadingPart("part-5", 5, "足し算する", List.of("変数aと変数bに保存した値を足します。"), "int c = a + b;",
                                                List.of("c-int", "c-name", "c-assignment", "a-use", "addition", "b-use", "c-semicolon"),
                                                List.of("int", "c", "=", "a", "+", "b", ";"), List.of(),
                                                "aとbの値を足し、結果を変数cに保存する"),
                                new CodeReadingPart("part-6", 6, "結果を表示する", List.of("変数cに保存した計算結果を表示します。"), "System.out.println(c);",
                                                List.of("print-command", "c-use", "print-semicolon"),
                                                List.of("System.out.println", "c", ";"), List.of(),
                                                "変数cに保存されている計算結果30を表示して改行する"),
                                new CodeReadingPart("part-7", 7, "プログラムを閉じる", List.of(), "}\n}",
                                                List.of("main-close", "class-close"), List.of("}", "}"), List.of(),
                                                "mainメソッドとMainクラスを順番に終了する"));
        }

        private static List<CodeReadingStepDefinition> createStage4Steps(
                        CodeReadingLessonDefinition stage2) {
                List<CodeReadingExplanationSection> variableName =
                                sharedExplanation(stage2, "age-declaration");
                List<CodeReadingExplanationSection> assignment =
                                sharedExplanation(stage2, "assignment");
                List<CodeReadingExplanationSection> variableUse =
                                sharedExplanation(stage2, "age-use");
                List<CodeReadingExplanationSection> semicolon = semicolonExplanation();
                return List.of(
                                richStep("class-public", 1, "public", "public", "accessible", PUBLIC_CARD,
                                                "アクセス修飾子", PUBLIC_EXPLANATION),
                                richStep("class-keyword", 2, "class", "class", "declare-class", CLASS_CARD,
                                                "クラスを作るキーワード", CLASS_EXPLANATION),
                                richStep("class-name", 3, "Main", "Main", "main-class-name", CLASS_NAME_CARD,
                                                "クラス名", CLASS_NAME_EXPLANATION),
                                richStep("class-open", 4, "{", "{", "block-start", BLOCK_START_CARD,
                                                "開始波かっこ", BLOCK_START_EXPLANATION),
                                richStep("main-public", 5, "public", "public", "accessible", PUBLIC_CARD,
                                                "アクセス修飾子", PUBLIC_EXPLANATION),
                                richStep("static", 6, "static", "static", "without-instance", STATIC_CARD,
                                                "static修飾子", STATIC_EXPLANATION),
                                richStep("void", 7, "void", "void", "no-return", VOID_CARD,
                                                "戻り値の型", VOID_EXPLANATION),
                                richStep("main", 8, "main", "main", "program-entry", MAIN_CARD,
                                                "メソッド名", MAIN_EXPLANATION),
                                richStep("string-array", 9, "String[]", "String[]", "multiple-strings", STRING_ARRAY_CARD,
                                                "引数の型", STRING_ARRAY_EXPLANATION),
                                richStep("args", 10, "args", "args", "argument-variable", ARGS_CARD,
                                                "引数名", ARGS_EXPLANATION),
                                richStep("main-open", 11, "{", "{", "block-start", BLOCK_START_CARD,
                                                "開始波かっこ", BLOCK_START_EXPLANATION),
                                richStep("signal-string-type", 12, "String", "String", "string-type",
                                                "文字列を扱うデータ型", "文字列型", stringTypeExplanation()),
                                richStep("signal-name", 13, "signal", "signal", "variable-name",
                                                "変数の名前", "変数名", variableName),
                                richStep("signal-assignment", 14, "=", "=", "assignment",
                                                "右側の値を左側に入れる", "代入演算子", assignment),
                                richStep("signal-blue-literal", 15, "\"青\"", "\"青\"", "string-value",
                                                "文字列の値", "文字列リテラル", stringLiteralExplanation("青")),
                                richStep("signal-semicolon", 16, ";", ";", "command-end",
                                                "文の終わり", "セミコロン", semicolon),
                                richStep("if-keyword", 17, "if", "if", "conditional-start",
                                                "条件がtrueのときだけ処理する", "if文", ifExplanation()),
                                richStep("condition-signal", 18, "signal", "signal", "use-variable-value",
                                                VARIABLE_VALUE_USE_CARD, "変数の利用", variableUse),
                                richStep("equals-method", 19, "equals", "equals", "compare-string-content",
                                                "文字列の内容が同じか比べる", "equalsメソッド", equalsExplanation()),
                                richStep("condition-blue-literal", 20, "\"青\"", "\"青\"", "comparison-value",
                                                "比較する文字列", "文字列リテラル", stringLiteralExplanation("青")),
                                richStep("if-open", 21, "{", "{", "block-start", BLOCK_START_CARD,
                                                "開始波かっこ", BLOCK_START_EXPLANATION),
                                richStep("true-print-command", 22, "System.out.println", "System.out.println",
                                                "display-and-newline", DISPLAY_AND_NEWLINE_CARD,
                                                "標準出力", SYSTEM_OUT_PRINTLN_EXPLANATION),
                                richStep("go-string", 23, "\"進む\"", "\"進む\"", "display-text",
                                                "表示する文字", "文字列リテラル", stringLiteralExplanation("進む")),
                                richStep("true-semicolon", 24, ";", ";", "command-end",
                                                "文の終わり", "セミコロン", semicolon),
                                richStep("if-close", 25, "}", "}", "close-if", "ここで終わる",
                                                "終了波かっこ", BLOCK_END_EXPLANATION),
                                richStep("else-keyword", 26, "else", "else", "conditional-fallback",
                                                "条件がfalseのときに処理する", "else節", elseExplanation()),
                                richStep("else-open", 27, "{", "{", "block-start", BLOCK_START_CARD,
                                                "開始波かっこ", BLOCK_START_EXPLANATION),
                                richStep("false-print-command", 28, "System.out.println", "System.out.println",
                                                "display-and-newline", DISPLAY_AND_NEWLINE_CARD,
                                                "標準出力", SYSTEM_OUT_PRINTLN_EXPLANATION),
                                richStep("stop-string", 29, "\"止まる\"", "\"止まる\"", "display-text",
                                                "表示する文字", "文字列リテラル", stringLiteralExplanation("止まる")),
                                richStep("false-semicolon", 30, ";", ";", "command-end",
                                                "文の終わり", "セミコロン", semicolon),
                                richStep("else-close", 31, "}", "}", "close-else", "ここで終わる",
                                                "終了波かっこ", BLOCK_END_EXPLANATION),
                                richStep("main-close", 32, "}", "}", "close-main", "ここで終わる",
                                                "終了波かっこ", BLOCK_END_EXPLANATION),
                                richStep("class-close", 33, "}", "}", "close-class", "ここで終わる",
                                                "終了波かっこ", BLOCK_END_EXPLANATION));
        }

        private static List<CodeReadingExplanationSection> stringTypeExplanation() {
                return List.of(
                                textSection("文字列型　String",
                                                text("複数の文字をまとめた文字列を扱うためのクラス型です。")),
                                textSection("ポイント",
                                                text("String は大文字の S で始めます。"),
                                                text("文字列の値はダブルクォート \" \" で囲みます。")),
                                officialReferencesSection(OFFICIAL_REFERENCES_TITLE,
                                                jlsReference("§4.3.3", "The Class String",
                                                                "Stringが文字列を表すクラス型であることを定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-4.html#jls-4.3.3")));
        }

        private static List<CodeReadingExplanationSection> stringLiteralExplanation(String value) {
                return List.of(
                                textSection("文字列リテラル　string literal",
                                                text("ダブルクォートで囲んで、文字列の値を直接書いたものです。")),
                                textSection("このコードでの値",
                                                text("\"" + value + "\" は文字列として扱われます。")),
                                officialReferencesSection(OFFICIAL_REFERENCES_TITLE,
                                                jlsReference("§3.10.5", "String Literals",
                                                                "Javaの文字列リテラルの書き方を定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-3.html#jls-3.10.5")));
        }

        private static List<CodeReadingExplanationSection> ifExplanation() {
                return List.of(
                                textSection("if文　if statement",
                                                text("かっこの中の条件が true のときだけ、直後のブロックを実行します。")),
                                tableSectionWithHeader("条件と実行される処理",
                                                tableRow("条件", "実行される処理"),
                                                tableRow("true", "if側の処理"),
                                                tableRow("false", "if側の処理は実行しない")),
                                textSection("ポイント",
                                                text("if は、条件によって実行する処理を分ける条件分岐です。")),
                                officialReferencesSection(OFFICIAL_REFERENCES_TITLE,
                                                jlsReference("§14.9.1", "The if-then Statement",
                                                                "条件がtrueのときに文を実行するif文の動作を定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-14.html#jls-14.9.1")));
        }

        private static List<CodeReadingExplanationSection> equalsExplanation() {
                return List.of(
                                textSection("equalsメソッド　equals method",
                                                text("2つの文字列の内容が同じかを調べ、trueまたはfalseを返します。")),
                                tableSectionWithHeader("文字列の比較",
                                                tableRow("書き方", "意味"),
                                                tableRow("signal.equals(\"青\")", "文字列の内容が同じか比較する"),
                                                tableRow("signal == \"青\"", "同じオブジェクトを参照しているか比較する")),
                                textSection("ポイント",
                                                text("文字列の内容を比較するときは、== ではなく equals() を使います。")),
                                officialReferencesSection(OFFICIAL_REFERENCES_TITLE,
                                                javaSeApiReference("String.equals(Object)",
                                                                "文字列の内容が等しいかを比較するequalsメソッドを説明しています。",
                                                                "https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/String.html#equals(java.lang.Object)")));
        }

        private static List<CodeReadingExplanationSection> elseExplanation() {
                return List.of(
                                textSection("else節　else clause",
                                                text("ifの条件が false のときに実行する処理を表します。")),
                                tableSectionWithHeader("ifとelseの分かれ方",
                                                tableRow("条件", "実行される処理"),
                                                tableRow("true", "if側"),
                                                tableRow("false", "else側")),
                                textSection("ポイント",
                                                text("この教材ではelse ifは扱わず、trueとfalseの2つに分けます。")),
                                officialReferencesSection(OFFICIAL_REFERENCES_TITLE,
                                                jlsReference("§14.9.2", "The if-then-else Statement",
                                                                "条件がfalseのときにelse側を実行する動作を定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-14.html#jls-14.9.2")));
        }

        private static List<CodeReadingCircuitDefinition> createStage4Circuits() {
                return List.of(
                                new CodeReadingCircuitDefinition("class-declaration", "public class Main {",
                                                List.of("class-public", "class-keyword", "class-name", "class-open"),
                                                List.of("public", "class", "Main", "{")),
                                new CodeReadingCircuitDefinition("main-method", "public static void main(String[] args) {",
                                                List.of("main-public", "static", "void", "main", "string-array", "args", "main-open"),
                                                List.of("public", "static", "void", "main", "String[]", "args", "{")),
                                new CodeReadingCircuitDefinition("signal-declaration", "String signal = \"青\";",
                                                List.of("signal-string-type", "signal-name", "signal-assignment",
                                                                "signal-blue-literal", "signal-semicolon"),
                                                List.of("String", "signal", "=", "\"青\"", ";")),
                                new CodeReadingCircuitDefinition("if-condition", "if (signal.equals(\"青\")) {",
                                                List.of("if-keyword", "condition-signal", "equals-method",
                                                                "condition-blue-literal", "if-open"),
                                                List.of("if", "signal", ".equals", "(\"青\")", "{")),
                                new CodeReadingCircuitDefinition("true-output", "System.out.println(\"進む\");",
                                                List.of("true-print-command", "go-string", "true-semicolon"),
                                                List.of("System.out.println", "\"進む\"", ";")),
                                new CodeReadingCircuitDefinition("else-start", "} else {",
                                                List.of("if-close", "else-keyword", "else-open"),
                                                List.of("}", "else", "{")),
                                new CodeReadingCircuitDefinition("false-output", "System.out.println(\"止まる\");",
                                                List.of("false-print-command", "stop-string", "false-semicolon"),
                                                List.of("System.out.println", "\"止まる\"", ";")),
                                new CodeReadingCircuitDefinition("else-close", "elseブロック終了 }",
                                                List.of("else-close"), List.of("elseブロック終了 }")),
                                new CodeReadingCircuitDefinition("block-closes", "mainメソッド終了 }　Mainクラス終了 }",
                                                List.of("main-close", "class-close"),
                                                List.of("mainメソッド終了 }", "Mainクラス終了 }")));
        }

        private static Map<String, List<CodeReadingCodeLineDefinition>> createStage4CodeLines() {
                return Map.of(
                                "part-1", List.of(line(
                                                List.of("class-public", "class-keyword", "class-name", "class-open"), "", "")),
                                "part-2", List.of(new CodeReadingCodeLineDefinition(
                                                List.of(token("main-public"), token("static"), token("void"), token("main"),
                                                                new CodeReadingCodeTokenDefinition("string-array", "(", ""),
                                                                new CodeReadingCodeTokenDefinition("args", "", ")"), token("main-open")),
                                                "", "quiz-reading-code-line--part-2")),
                                "part-3", List.of(line(List.of("signal-string-type", "signal-name",
                                                "signal-assignment", "signal-blue-literal", "signal-semicolon"), "", "")),
                                "part-4", List.of(new CodeReadingCodeLineDefinition(
                                                List.of(token("if-keyword"),
                                                                new CodeReadingCodeTokenDefinition("condition-signal", " (", ""),
                                                                new CodeReadingCodeTokenDefinition("equals-method", ".", "("),
                                                                new CodeReadingCodeTokenDefinition("condition-blue-literal", "", "))"),
                                                                new CodeReadingCodeTokenDefinition("if-open", " ", "")),
                                                "", "")),
                                "part-5", List.of(new CodeReadingCodeLineDefinition(
                                                List.of(token("true-print-command"),
                                                                new CodeReadingCodeTokenDefinition("go-string", "(", ")"),
                                                                token("true-semicolon")), "", "")),
                                "part-6", List.of(line(List.of("if-close", "else-keyword", "else-open"), "", "")),
                                "part-7", List.of(new CodeReadingCodeLineDefinition(
                                                List.of(token("false-print-command"),
                                                                new CodeReadingCodeTokenDefinition("stop-string", "(", ")"),
                                                                token("false-semicolon")), "", "")),
                                "part-8", List.of(line(List.of("else-close"), "", "")),
                                "part-9", List.of(line(List.of("main-close", "class-close"), "", "")));
        }

        private static List<CodeReadingPart> createStage4Parts() {
                return List.of(
                                new CodeReadingPart("part-1", 1, "クラスを作る", List.of(), "public class Main {",
                                                List.of("class-public", "class-keyword", "class-name", "class-open"),
                                                List.of("public", "class", "Main", "{"), List.of(),
                                                "Mainというクラスを宣言し、その中身を始める"),
                                new CodeReadingPart("part-2", 2, "mainメソッドを作る", List.of(),
                                                "public static void main(String[] args) {",
                                                List.of("main-public", "static", "void", "main", "string-array", "args", "main-open"),
                                                List.of("public", "static", "void", "main", "String[]", "args", "{"), List.of(),
                                                "mainメソッドを宣言し、その中身を始める"),
                                new CodeReadingPart("part-3", 3, "文字列を変数に代入する",
                                                List.of("信号を表す文字列を変数signalに保存します。"),
                                                "String signal = \"青\";",
                                                List.of("signal-string-type", "signal-name", "signal-assignment",
                                                                "signal-blue-literal", "signal-semicolon"),
                                                List.of("String", "signal", "=", "\"青\"", ";"), List.of(),
                                                "String型の変数signalを宣言し、文字列「青」で初期化する"),
                                new CodeReadingPart("part-4", 4, "文字列が一致するか判定する",
                                                List.of("ifで条件分岐を始め、signalの内容が「青」と同じかをequals()で調べます。"),
                                                "if (signal.equals(\"青\")) {",
                                                List.of("if-keyword", "condition-signal", "equals-method",
                                                                "condition-blue-literal", "if-open"),
                                                List.of("if", "signal", "equals", "\"青\"", "{"), List.of(),
                                                "ifで文字列の内容が一致するか判定し、true側のブロックを始める"),
                                new CodeReadingPart("part-5", 5, "条件がtrueの場合の処理",
                                                List.of("条件がtrueなので「進む」と表示します。"),
                                                "System.out.println(\"進む\");",
                                                List.of("true-print-command", "go-string", "true-semicolon"),
                                                List.of("System.out.println", "\"進む\"", ";"), List.of(),
                                                "ifの条件がtrueのときに「進む」と表示する"),
                                new CodeReadingPart("part-6", 6, "条件がfalseの場合の処理",
                                                List.of("ifの条件がfalseだった場合の処理へ分かれます。"),
                                                "} else {", List.of("if-close", "else-keyword", "else-open"),
                                                List.of("}", "else", "{"), List.of(),
                                                "if側のブロックを閉じ、条件がfalseのときに実行するelse側のブロックを始める"),
                                new CodeReadingPart("part-7", 7, "else側の処理",
                                                List.of("条件がfalseの場合は「止まる」と表示します。"),
                                                "System.out.println(\"止まる\");",
                                                List.of("false-print-command", "stop-string", "false-semicolon"),
                                                List.of("System.out.println", "\"止まる\"", ";"), List.of(),
                                                "else側で「止まる」と表示する"),
                                new CodeReadingPart("part-8", 8, "elseブロックを閉じる", List.of(), "}",
                                                List.of("else-close"), List.of("}"), List.of(),
                                                "else側の処理を書くブロックを終了する"),
                                new CodeReadingPart("part-9", 9, "プログラムを閉じる", List.of(), "}\n}",
                                                List.of("main-close", "class-close"), List.of("}", "}"), List.of(),
                                                "mainメソッドとMainクラスを順番に終了する"));
        }

        private static List<CodeReadingStepDefinition> createStage5Steps(
                        CodeReadingLessonDefinition stage2) {
                List<CodeReadingExplanationSection> integerType =
                                sharedExplanation(stage2, "int-type");
                List<CodeReadingExplanationSection> assignment =
                                sharedExplanation(stage2, "assignment");
                List<CodeReadingExplanationSection> variableUse = loopVariableUseExplanation();
                List<CodeReadingExplanationSection> semicolon = semicolonExplanation();
                return List.of(
                                richStep("class-public", 1, "public", "public", "accessible", PUBLIC_CARD,
                                                "アクセス修飾子", PUBLIC_EXPLANATION),
                                richStep("class-keyword", 2, "class", "class", "declare-class", CLASS_CARD,
                                                "クラスを作るキーワード", CLASS_EXPLANATION),
                                richStep("class-name", 3, "Main", "Main", "main-class-name", CLASS_NAME_CARD,
                                                "クラス名", CLASS_NAME_EXPLANATION),
                                richStep("class-open", 4, "{", "{", "block-start", BLOCK_START_CARD,
                                                "開始波かっこ", BLOCK_START_EXPLANATION),
                                richStep("main-public", 5, "public", "public", "accessible", PUBLIC_CARD,
                                                "アクセス修飾子", PUBLIC_EXPLANATION),
                                richStep("static", 6, "static", "static", "without-instance", STATIC_CARD,
                                                "static修飾子", STATIC_EXPLANATION),
                                richStep("void", 7, "void", "void", "no-return", VOID_CARD,
                                                "戻り値の型", VOID_EXPLANATION),
                                richStep("main", 8, "main", "main", "program-entry", MAIN_CARD,
                                                "メソッド名", MAIN_EXPLANATION),
                                richStep("string-array", 9, "String[]", "String[]", "multiple-strings", STRING_ARRAY_CARD,
                                                "引数の型", STRING_ARRAY_EXPLANATION),
                                richStep("args", 10, "args", "args", "argument-variable", ARGS_CARD,
                                                "引数名", ARGS_EXPLANATION),
                                richStep("main-open", 11, "{", "{", "block-start", BLOCK_START_CARD,
                                                "開始波かっこ", BLOCK_START_EXPLANATION),
                                richStep("for-keyword", 12, "for", "for", "repeat-processing",
                                                "条件を満たす間、処理を繰り返す", "for文", forExplanation()),
                                richStep("loop-int", 13, "int", "int", "integer-type", "整数を扱うデータ型",
                                                "整数型", integerType),
                                richStep("initial-i", 14, "i", "i", "loop-variable", "繰り返しで使う変数の名前",
                                                "for文の変数", loopVariableExplanation()),
                                richStep("initial-assignment", 15, "=", "=", "assignment", "右側の値を左側に入れる",
                                                "代入演算子", assignment),
                                richStep("initial-one", 16, "1", "1", "initial-value", "変数iの最初の値",
                                                "初期値", loopInitialValueExplanation()),
                                richStep("initial-separator", 17, ";", ";", "for-separator",
                                                "初期化と条件式を区切る", "for文の区切り", forSeparatorExplanation(true)),
                                richStep("condition-i", 18, "i", "i", "use-loop-variable", VARIABLE_VALUE_USE_CARD,
                                                "変数の利用", variableUse),
                                richStep("less-than-or-equal", 19, "<=", "<=", "less-than-or-equal",
                                                "左側が右側以下かを判定する", "比較演算子", lessThanOrEqualExplanation()),
                                richStep("condition-five", 20, "5", "5", "comparison-limit",
                                                "繰り返しを続ける上限の値", "比較する整数", loopLimitExplanation()),
                                richStep("condition-separator", 21, ";", ";", "for-separator",
                                                "条件式と更新式を区切る", "for文の区切り", forSeparatorExplanation(false)),
                                richStep("update-i", 22, "i", "i", "update-loop-variable",
                                                "値を更新する変数", "更新対象の変数", variableUse),
                                richStep("increment", 23, "++", "++", "increment-by-one",
                                                "変数iの値を1増やす", "インクリメント演算子", incrementExplanation()),
                                richStep("for-open", 24, "{", "{", "block-start", BLOCK_START_CARD,
                                                "開始波かっこ", BLOCK_START_EXPLANATION),
                                richStep("print-command", 25, "System.out.println", "System.out.println",
                                                "display-and-newline", DISPLAY_AND_NEWLINE_CARD,
                                                "標準出力", SYSTEM_OUT_PRINTLN_EXPLANATION),
                                richStep("print-i", 26, "i", "i", "use-loop-variable", VARIABLE_VALUE_USE_CARD,
                                                "変数の利用", variableUse),
                                richStep("print-semicolon", 27, ";", ";", "command-end", "文の終わり",
                                                "セミコロン", semicolon),
                                richStep("for-close", 28, "}", "}", "close-for", "for文のブロックを閉じる",
                                                "終了波かっこ", forBlockEndExplanation()),
                                richStep("main-close", 29, "}", "}", "close-main", "mainメソッドを閉じる",
                                                "終了波かっこ", BLOCK_END_EXPLANATION),
                                richStep("class-close", 30, "}", "}", "close-class", "Mainクラスを閉じる",
                                                "終了波かっこ", BLOCK_END_EXPLANATION));
        }

        private static List<CodeReadingExplanationSection> forExplanation() {
                return List.of(
                                textSection("for文　for statement",
                                                text("初期化、条件式、更新式を使い、条件を満たしている間、同じ処理を繰り返す構文です。")),
                                tableSectionWithHeader("for文の実行順序",
                                                tableRow("順序", "処理"),
                                                tableRow("1", "int i = 1 で最初の値を設定する（最初の1回だけ）"),
                                                tableRow("2", "i <= 5 を判定する"),
                                                tableRow("3", "trueならブロック内の処理を実行する"),
                                                tableRow("4", "i++ でiを1増やす"),
                                                tableRow("5", "再び条件式を判定し、falseなら終了する")),
                                textSection("ポイント",
                                                text("条件式は、ブロック内の処理を実行する前に毎回評価されます。"),
                                                text("iが6になると i <= 5 はfalseになり、6は表示されません。")),
                                officialReferencesSection(OFFICIAL_REFERENCES_TITLE,
                                                jlsReference("§14.14.1", "The basic for Statement",
                                                                "初期化、条件式、更新式を持つ基本for文の実行順序を定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-14.html#jls-14.14.1")));
        }

        private static List<CodeReadingExplanationSection> loopVariableExplanation() {
                return List.of(
                                textSection("for文の変数　loop variable",
                                                text("iは整数を保存する変数の名前です。Javaの予約語ではなく、別の有効な名前も付けられます。")),
                                textSection("このコードでの役割",
                                                text("iは1から始まり、繰り返すたびに1ずつ増えます。"),
                                                text("for文の初期化部分で宣言したiは、このfor文の制御部分とブロック内で使えます。")),
                                officialReferencesSection(OFFICIAL_REFERENCES_TITLE,
                                                jlsReference("§6.3", "Scope of a Declaration",
                                                                "基本for文で宣言したローカル変数の有効範囲を定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-6.html#jls-6.3"),
                                                jlsReference("§3.8", "Identifiers",
                                                                "変数名に使う識別子の規則を定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-3.html#jls-3.8")));
        }

        private static List<CodeReadingExplanationSection> loopInitialValueExplanation() {
                return List.of(
                                textSection("初期値　initial value",
                                                text("1は、繰り返しを始めるときに変数iへ入れる最初の値です。")),
                                textSection("実行されるタイミング",
                                                text("int i = 1 はfor文を開始するときに1回だけ実行され、繰り返すたびには実行されません。")),
                                officialReferencesSection(OFFICIAL_REFERENCES_TITLE,
                                                jlsReference("§14.14.1.1", "Initialization of the basic for Statement",
                                                                "基本for文の初期化が最初に1回実行されることを定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-14.html#jls-14.14.1.1")));
        }

        private static List<CodeReadingExplanationSection> forSeparatorExplanation(boolean afterInitialization) {
                String left = afterInitialization ? "初期化" : "条件式";
                String right = afterInitialization ? "条件式" : "更新式";
                return List.of(
                                textSection("for文のセミコロン",
                                                text("for文の制御部分で、" + left + "と" + right + "を区切る記号です。")),
                                textSection("文末のセミコロンとの違い",
                                                text("for (int i = 1; i <= 5; i++) の2つのセミコロンは、3つの役割を区切ります。"),
                                                text("System.out.println(i); のセミコロンは、その文の終わりを表します。")));
        }

        private static List<CodeReadingExplanationSection> loopVariableUseExplanation() {
                return List.of(
                                textSection("変数iの利用",
                                                text("iと書いた場所では、その時点で変数iに保存されている整数の値を使います。")),
                                tableSectionWithHeader("繰り返しによる値の変化",
                                                tableHeaderRow3("iの値", "i <= 5", "表示"),
                                                tableRow3("1", "true", "1"),
                                                tableRow3("2", "true", "2"),
                                                tableRow3("3", "true", "3"),
                                                tableRow3("4", "true", "4"),
                                                tableRow3("5", "true", "5"),
                                                tableRow3("6", "false", "表示しない")),
                                textSection("ポイント",
                                                text("System.out.println(i); は文字列の\"i\"ではなく、その時点のiの値を表示します。")),
                                officialReferencesSection(OFFICIAL_REFERENCES_TITLE,
                                                jlsReference("§6.5.6", "Meaning of Expression Names",
                                                                "式の中の変数名が表す意味を定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-6.html#jls-6.5.6")));
        }

        private static List<CodeReadingExplanationSection> lessThanOrEqualExplanation() {
                return List.of(
                                textSection("比較演算子 <=",
                                                text("左側の値が右側の値以下ならtrue、右側より大きければfalseを返します。")),
                                tableSectionWithHeader("< と <= の違い",
                                                tableHeaderRow3("式", "5を含むか", "trueになる範囲"),
                                                tableRow3("i < 5", "含まない", "iが5より小さい"),
                                                tableRow3("i <= 5", "含む", "iが5以下")),
                                textSection("このコードでの判定",
                                                text("i <= 5 はiが1から5の間はtrueで、iが6になるとfalseです。")),
                                officialReferencesSection(OFFICIAL_REFERENCES_TITLE,
                                                jlsReference("§15.20.1", "Numerical Comparison Operators <, <=, >, and >=",
                                                                "数値を比較する関係演算子の動作を定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-15.html#jls-15.20.1")));
        }

        private static List<CodeReadingExplanationSection> loopLimitExplanation() {
                return List.of(
                                textSection("比較する整数",
                                                text("5は、条件式 i <= 5 でiと比較する値です。")),
                                textSection("このコードでの役割",
                                                text("iが5のときも条件はtrueなので5を表示し、その後iが6になると繰り返しを終了します。")));
        }

        private static List<CodeReadingExplanationSection> incrementExplanation() {
                return List.of(
                                textSection("インクリメント演算子　++",
                                                text("変数の数値を1増やす演算子です。今回の更新式 i++ は、i = i + 1 と同じようにiを1増やします。")),
                                textSection("実行されるタイミング",
                                                text("ブロック内の処理を実行した後にi++が実行され、その後で再び i <= 5 を判定します。"),
                                                text("今回はfor文の更新式での役割を学び、前置と後置の違いには踏み込みません。")),
                                officialReferencesSection(OFFICIAL_REFERENCES_TITLE,
                                                jlsReference("§15.14.2", "The Postfix Increment Operator ++",
                                                                "後置インクリメント演算子++の動作を定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-15.html#jls-15.14.2"),
                                                jlsReference("§14.14.1.3", "Iteration of the basic for Statement",
                                                                "for文のブロック実行後に更新式が実行されることを定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-14.html#jls-14.14.1.3")));
        }

        private static List<CodeReadingExplanationSection> forBlockEndExplanation() {
                return List.of(
                                textSection("for文のブロック終了",
                                                text("この閉じ波括弧は、繰り返して実行する処理の範囲を閉じます。")),
                                textSection("閉じた後の流れ",
                                                text("ブロックを1回実行しただけではfor文全体は終了しません。"),
                                                text("i++を実行してから条件式をもう一度判定し、falseになったときにfor文を終了します。")),
                                officialReferencesSection(OFFICIAL_REFERENCES_TITLE,
                                                jlsReference("§14.14.1", "The basic for Statement",
                                                                "基本for文の繰り返し処理と終了条件を定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-14.html#jls-14.14.1")));
        }

        private static List<CodeReadingCircuitDefinition> createStage5Circuits() {
                return List.of(
                                new CodeReadingCircuitDefinition("class-declaration", "public class Main {",
                                                List.of("class-public", "class-keyword", "class-name", "class-open"),
                                                List.of("public", "class", "Main", "{")),
                                new CodeReadingCircuitDefinition("main-method", "public static void main(String[] args) {",
                                                List.of("main-public", "static", "void", "main", "string-array", "args", "main-open"),
                                                List.of("public", "static", "void", "main", "String[]", "args", "{")),
                                new CodeReadingCircuitDefinition("for-initialization", "for (int i = 1;",
                                                List.of("for-keyword", "loop-int", "initial-i", "initial-assignment",
                                                                "initial-one", "initial-separator"),
                                                List.of("for", "int", "i", "=", "1", ";")),
                                new CodeReadingCircuitDefinition("for-condition-update", "i <= 5; i++) {",
                                                List.of("condition-i", "less-than-or-equal", "condition-five",
                                                                "condition-separator", "update-i", "increment", "for-open"),
                                                List.of("i", "<=", "5", ";", "i", "++", "{")),
                                new CodeReadingCircuitDefinition("loop-output", "System.out.println(i);",
                                                List.of("print-command", "print-i", "print-semicolon"),
                                                List.of("System.out.println", "i", ";")),
                                new CodeReadingCircuitDefinition("block-closes",
                                                "for文終了 }　mainメソッド終了 }　Mainクラス終了 }",
                                                List.of("for-close", "main-close", "class-close"),
                                                List.of("for文終了 }", "mainメソッド終了 }", "Mainクラス終了 }")));
        }

        private static Map<String, List<CodeReadingCodeLineDefinition>> createStage5CodeLines() {
                return Map.of(
                                "part-1", List.of(line(
                                                List.of("class-public", "class-keyword", "class-name", "class-open"), "", "")),
                                "part-2", List.of(new CodeReadingCodeLineDefinition(
                                                List.of(token("main-public"), token("static"), token("void"), token("main"),
                                                                new CodeReadingCodeTokenDefinition("string-array", "(", ""),
                                                                new CodeReadingCodeTokenDefinition("args", "", ")"), token("main-open")),
                                                "", "quiz-reading-code-line--part-2")),
                                "part-3", List.of(new CodeReadingCodeLineDefinition(
                                                List.of(new CodeReadingCodeTokenDefinition("for-keyword", "", " ("),
                                                                token("loop-int"), token("initial-i"), token("initial-assignment"),
                                                                token("initial-one"), token("initial-separator")), "", "")),
                                "part-4", List.of(new CodeReadingCodeLineDefinition(
                                                List.of(token("condition-i"), token("less-than-or-equal"),
                                                                token("condition-five"), token("condition-separator"),
                                                                token("update-i"), new CodeReadingCodeTokenDefinition("increment", "", ")"),
                                                                token("for-open")), "", "")),
                                "part-5", List.of(new CodeReadingCodeLineDefinition(
                                                List.of(token("print-command"),
                                                                new CodeReadingCodeTokenDefinition("print-i", "(", ")"),
                                                                token("print-semicolon")), "", "")),
                                "part-6", List.of(line(List.of("for-close", "main-close", "class-close"), "", "")));
        }

        private static List<CodeReadingPart> createStage5Parts() {
                return List.of(
                                new CodeReadingPart("part-1", 1, "クラスを作る", List.of(), "public class Main {",
                                                List.of("class-public", "class-keyword", "class-name", "class-open"),
                                                List.of("public", "class", "Main", "{"), List.of(),
                                                "Mainというクラスを宣言し、その中身を始める"),
                                new CodeReadingPart("part-2", 2, "mainメソッドを作る", List.of(),
                                                "public static void main(String[] args) {",
                                                List.of("main-public", "static", "void", "main", "string-array", "args", "main-open"),
                                                List.of("public", "static", "void", "main", "String[]", "args", "{"), List.of(),
                                                "mainメソッドを宣言し、その中身を始める"),
                                new CodeReadingPart("part-3", 3, "for文を開始して初期化する",
                                                List.of("for文を開始し、変数iの最初の値を1に設定します。"),
                                                "for (int i = 1;",
                                                List.of("for-keyword", "loop-int", "initial-i", "initial-assignment",
                                                                "initial-one", "initial-separator"),
                                                List.of("for", "int", "i", "=", "1", ";"), List.of(),
                                                "for文を始め、繰り返しに使う変数iを1で初期化する"),
                                new CodeReadingPart("part-4", 4, "条件式と更新式を読む",
                                                List.of("iが5以下かを判定し、1回の処理後にiを1増やします。"),
                                                "i <= 5; i++) {",
                                                List.of("condition-i", "less-than-or-equal", "condition-five",
                                                                "condition-separator", "update-i", "increment", "for-open"),
                                                List.of("i", "<=", "5", ";", "i", "++", "{"), List.of(),
                                                "iが5以下の間、処理後にiを1増やして繰り返す"),
                                new CodeReadingPart("part-5", 5, "繰り返す処理を読む",
                                                List.of("繰り返すたびに、その時点のiの値を表示します。"),
                                                "System.out.println(i);",
                                                List.of("print-command", "print-i", "print-semicolon"),
                                                List.of("System.out.println", "i", ";"), List.of(),
                                                "繰り返すたびに変数iの現在の値を表示する"),
                                new CodeReadingPart("part-6", 6, "ブロックを閉じる",
                                                List.of("for文、mainメソッド、Mainクラスのブロックを順番に閉じます。"),
                                                "}\n}\n}",
                                                List.of("for-close", "main-close", "class-close"),
                                                List.of("}", "}", "}"), List.of(),
                                                "for文の更新と再判定を経て繰り返しを終え、プログラム全体を閉じる"));
        }

        private static List<CodeReadingStepDefinition> createStage6Steps(
                        CodeReadingLessonDefinition stage2,
                        CodeReadingLessonDefinition stage5) {
                List<CodeReadingExplanationSection> assignment =
                                sharedExplanation(stage2, "assignment");
                List<CodeReadingExplanationSection> integerType =
                                sharedExplanation(stage2, "int-type");
                List<CodeReadingExplanationSection> forStatement =
                                sharedExplanation(stage5, "for-keyword");
                List<CodeReadingExplanationSection> initialSeparator =
                                sharedExplanation(stage5, "initial-separator");
                List<CodeReadingExplanationSection> conditionSeparator =
                                sharedExplanation(stage5, "condition-separator");
                List<CodeReadingExplanationSection> increment =
                                sharedExplanation(stage5, "increment");
                List<CodeReadingExplanationSection> forBlockEnd =
                                sharedExplanation(stage5, "for-close");
                List<CodeReadingExplanationSection> indexVariable = arrayIndexVariableExplanation();
                List<CodeReadingExplanationSection> semicolon = semicolonExplanation();
                return List.of(
                                richStep("class-public", 1, "public", "public", "accessible", PUBLIC_CARD,
                                                "アクセス修飾子", PUBLIC_EXPLANATION),
                                richStep("class-keyword", 2, "class", "class", "declare-class", CLASS_CARD,
                                                "クラスを作るキーワード", CLASS_EXPLANATION),
                                richStep("class-name", 3, "Main", "Main", "main-class-name", CLASS_NAME_CARD,
                                                "クラス名", CLASS_NAME_EXPLANATION),
                                richStep("class-open", 4, "{", "{", "block-start", BLOCK_START_CARD,
                                                "開始波かっこ", BLOCK_START_EXPLANATION),
                                richStep("main-public", 5, "public", "public", "accessible", PUBLIC_CARD,
                                                "アクセス修飾子", PUBLIC_EXPLANATION),
                                richStep("static", 6, "static", "static", "without-instance", STATIC_CARD,
                                                "static修飾子", STATIC_EXPLANATION),
                                richStep("void", 7, "void", "void", "no-return", VOID_CARD,
                                                "戻り値の型", VOID_EXPLANATION),
                                richStep("main", 8, "main", "main", "program-entry", MAIN_CARD,
                                                "メソッド名", MAIN_EXPLANATION),
                                richStep("string-array", 9, "String[]", "String[]", "multiple-strings", STRING_ARRAY_CARD,
                                                "引数の型", STRING_ARRAY_EXPLANATION),
                                richStep("args", 10, "args", "args", "argument-variable", ARGS_CARD,
                                                "引数名", ARGS_EXPLANATION),
                                richStep("main-open", 11, "{", "{", "block-start", BLOCK_START_CARD,
                                                "開始波かっこ", BLOCK_START_EXPLANATION),
                                richStep("int-array-type", 12, "int[]", "int[]", "int-array-type",
                                                "int型の値を要素に持つ配列の型", "配列型", intArrayTypeExplanation()),
                                richStep("scores-name", 13, "scores", "scores", "array-reference-variable",
                                                "int配列を参照する変数の名前", "配列を参照する変数", scoresVariableExplanation()),
                                richStep("scores-assignment", 14, "=", "=", "assignment",
                                                "右側の配列を左側の変数から参照できるようにする", "代入演算子", assignment),
                                richStep("scores-initializer", 15, "{80, 90, 100}", "{80, 90, 100}",
                                                "array-initializer", "3つの整数を持つ配列を生成して初期化する",
                                                "配列初期化子", arrayInitializerExplanation()),
                                richStep("scores-semicolon", 16, ";", ";", "command-end", "文の終わり",
                                                "セミコロン", semicolon),
                                richStep("for-keyword", 17, "for", "for", "repeat-processing",
                                                "条件を満たす間、処理を繰り返す", "for文", forStatement),
                                richStep("loop-int", 18, "int", "int", "integer-type", "整数を扱うデータ型",
                                                "整数型", integerType),
                                richStep("initial-i", 19, "i", "i", "array-index-variable",
                                                "配列の位置を表すための変数", "インデックス用の変数", indexVariable),
                                richStep("initial-assignment", 20, "=", "=", "assignment",
                                                "右側の値を左側に入れる", "代入演算子", assignment),
                                richStep("initial-zero", 21, "0", "0", "first-array-index",
                                                "配列の最初の位置を表す値", "初期値", arrayInitialZeroExplanation()),
                                richStep("initial-separator", 22, ";", ";", "for-separator",
                                                "初期化と条件式を区切る", "for文の区切り", initialSeparator),
                                richStep("condition-i", 23, "i", "i", "use-array-index",
                                                "現在確認する配列の位置", "インデックスの利用", indexVariable),
                                richStep("less-than", 24, "<", "<", "less-than",
                                                "左側が右側より小さいかを判定する", "比較演算子", arrayLessThanExplanation()),
                                richStep("scores-length", 25, "scores.length", "scores.length", "array-length",
                                                "配列scoresの要素数を取得する", "配列のlengthフィールド", arrayLengthExplanation()),
                                richStep("condition-separator", 26, ";", ";", "for-separator",
                                                "条件式と更新式を区切る", "for文の区切り", conditionSeparator),
                                richStep("update-i", 27, "i", "i", "update-array-index",
                                                "次の位置へ進める変数", "更新対象の変数", indexVariable),
                                richStep("increment", 28, "++", "++", "increment-by-one",
                                                "変数iの値を1増やす", "インクリメント演算子", increment),
                                richStep("for-open", 29, "{", "{", "block-start", BLOCK_START_CARD,
                                                "開始波かっこ", BLOCK_START_EXPLANATION),
                                richStep("print-command", 30, "System.out.println", "System.out.println",
                                                "display-and-newline", DISPLAY_AND_NEWLINE_CARD,
                                                "標準出力", SYSTEM_OUT_PRINTLN_EXPLANATION),
                                richStep("scores-element", 31, "scores[i]", "scores[i]", "array-element-access",
                                                "i番目の配列要素を取得する", "配列要素へのアクセス", arrayElementExplanation()),
                                richStep("print-semicolon", 32, ";", ";", "command-end", "文の終わり",
                                                "セミコロン", semicolon),
                                richStep("for-close", 33, "}", "}", "close-for", "for文のブロックを閉じる",
                                                "終了波かっこ", forBlockEnd),
                                richStep("main-close", 34, "}", "}", "close-main", "mainメソッドを閉じる",
                                                "終了波かっこ", BLOCK_END_EXPLANATION),
                                richStep("class-close", 35, "}", "}", "close-class", "Mainクラスを閉じる",
                                                "終了波かっこ", BLOCK_END_EXPLANATION));
        }

        private static List<CodeReadingExplanationSection> intArrayTypeExplanation() {
                return List.of(
                                textSection("int配列型　int[]",
                                                text("int型の値を要素として持つ配列の型です。"),
                                                text("int型の変数は1つの整数値を扱いますが、int[]型の変数はint型の配列を参照できます。")),
                                textSection("ポイント",
                                                text("変数scoresが複数の整数を直接内包するのではなく、生成された配列を参照します。"),
                                                text("[]は、この型が配列型であることを表します。")),
                                officialReferencesSection(OFFICIAL_REFERENCES_TITLE,
                                                jlsReference("§10.1", "Array Types",
                                                                "配列型が要素型と次元から構成されることを定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-10.html#jls-10.1"),
                                                jlsReference("§4.3.1", "Objects",
                                                                "配列がオブジェクトとして扱われることを定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-4.html#jls-4.3.1")));
        }

        private static List<CodeReadingExplanationSection> scoresVariableExplanation() {
                return List.of(
                                textSection("配列を参照する変数　scores",
                                                text("scoresは、int型の配列を参照する変数の名前です。"),
                                                text("scoresという名前自体にJava固有の特別な意味はなく、有効な別の名前も付けられます。")),
                                textSection("このコードでの役割",
                                                text("scoresを通して、配列の要素数や各要素の値を利用します。")),
                                officialReferencesSection(OFFICIAL_REFERENCES_TITLE,
                                                jlsReference("§4.12", "Variables",
                                                                "変数が値を保持する記憶場所であることを定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-4.html#jls-4.12"),
                                                jlsReference("§3.8", "Identifiers",
                                                                "変数名に使う識別子の規則を定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-3.html#jls-3.8")));
        }

        private static List<CodeReadingExplanationSection> arrayInitializerExplanation() {
                return List.of(
                                textSection("配列初期化子　array initializer",
                                                text("{80, 90, 100}は、3つのint型の要素を持つ配列を生成し、それぞれの値で初期化します。"),
                                                text("変数宣言と同時に配列初期化子を使うため、このコードではnewを明示していません。")),
                                tableSectionWithHeader("インデックスと要素",
                                                tableHeaderRow3("インデックス", "書き方", "値"),
                                                tableRow3("0", "scores[0]", "80"),
                                                tableRow3("1", "scores[1]", "90"),
                                                tableRow3("2", "scores[2]", "100")),
                                textSection("ポイント",
                                                text("配列のインデックスは0から始まります。"),
                                                text("配列の長さは生成時に決まり、同じ配列の長さを後から変更することはできません。")),
                                officialReferencesSection(OFFICIAL_REFERENCES_TITLE,
                                                jlsReference("§10.6", "Array Initializers",
                                                                "配列初期化子の書き方と要素の初期化を定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-10.html#jls-10.6"),
                                                jlsReference("§15.10.1", "Array Creation Expressions",
                                                                "配列の生成と長さの決定について定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-15.html#jls-15.10.1")));
        }

        private static List<CodeReadingExplanationSection> arrayIndexVariableExplanation() {
                return List.of(
                                textSection("インデックス用の変数　i",
                                                text("iは、配列のどの位置を確認するかを表すための整数型の変数です。"),
                                                text("iはJavaの予約語ではなく、別の有効な変数名も使えます。")),
                                textSection("このコードでの変化",
                                                text("iは0から始まり、i++によって0、1、2と1ずつ増えます。"),
                                                text("配列の最初のインデックスが0なので、Stage 5の1ではなく0から開始します。")));
        }

        private static List<CodeReadingExplanationSection> arrayInitialZeroExplanation() {
                return List.of(
                                textSection("最初のインデックス　0",
                                                text("Javaの配列は0番目から始まるため、最初の要素scores[0]を取得するにはiを0で初期化します。")),
                                textSection("Stage 5との違い",
                                                text("Stage 5では数字1から表示するためiを1で始めました。"),
                                                text("今回は配列の先頭から順番に取得するためiを0で始めます。")),
                                officialReferencesSection(OFFICIAL_REFERENCES_TITLE,
                                                jlsReference("§10.4", "Array Access",
                                                                "配列の構成要素が0からlength-1までのインデックスで参照されることを定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-10.html#jls-10.4")));
        }

        private static List<CodeReadingExplanationSection> arrayLessThanExplanation() {
                return List.of(
                                textSection("比較演算子 <",
                                                text("左側の値が右側の値より小さい場合にtrueを返します。")),
                                textSection("このコードでの条件",
                                                text("i < scores.lengthは、iが配列の要素数3より小さい間だけtrueです。"),
                                                text("有効なインデックス0、1、2で処理し、iが3になるとfalseになって終了します。")),
                                textSection("<=にしない理由",
                                                text("i <= scores.lengthではiが3のときも処理され、存在しないscores[3]へのアクセスでArrayIndexOutOfBoundsExceptionが発生します。")),
                                officialReferencesSection(OFFICIAL_REFERENCES_TITLE,
                                                jlsReference("§15.20.1", "Numerical Comparison Operators <, <=, >, and >=",
                                                                "数値を比較する関係演算子の動作を定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-15.html#jls-15.20.1"),
                                                jlsReference("§15.10.4", "Run-Time Evaluation of Array Access Expressions",
                                                                "範囲外のインデックスで例外が発生することを定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-15.html#jls-15.10.4")));
        }

        private static List<CodeReadingExplanationSection> arrayLengthExplanation() {
                return List.of(
                                textSection("配列のlengthフィールド",
                                                text("scores.lengthは、配列scoresの要素数を表します。この配列の要素数は3です。")),
                                textSection("ポイント",
                                                text("配列のlengthはメソッドではないため、scores.length()ではなくscores.lengthと書きます。"),
                                                text("長さ3の配列で使用できるインデックスは0、1、2です。")),
                                officialReferencesSection(OFFICIAL_REFERENCES_TITLE,
                                                jlsReference("§10.7", "Array Members",
                                                                "配列がpublic finalなlengthフィールドを持つことを定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-10.html#jls-10.7")));
        }

        private static List<CodeReadingExplanationSection> arrayElementExplanation() {
                return List.of(
                                textSection("配列要素へのアクセス　scores[i]",
                                                text("変数iの現在の値をインデックスとして、配列scoresの要素を1つ取得します。"),
                                                text("scores[i]は文字列ではなく、配列から取得したint型の値です。")),
                                tableSectionWithHeader("iの値と取得する要素",
                                                tableHeaderRow3("i", "式", "取得する値"),
                                                tableRow3("0", "scores[i]", "80"),
                                                tableRow3("1", "scores[i]", "90"),
                                                tableRow3("2", "scores[i]", "100")),
                                textSection("表示の流れ",
                                                text("System.out.println(scores[i]);によって、80、90、100を1行ずつ表示します。")),
                                officialReferencesSection(OFFICIAL_REFERENCES_TITLE,
                                                jlsReference("§15.10.3", "Array Access Expressions",
                                                                "配列参照とインデックスを使って要素へアクセスする式を定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-15.html#jls-15.10.3")));
        }

        private static List<CodeReadingCircuitDefinition> createStage6Circuits() {
                return List.of(
                                new CodeReadingCircuitDefinition("class-declaration", "public class Main {",
                                                List.of("class-public", "class-keyword", "class-name", "class-open"),
                                                List.of("public", "class", "Main", "{")),
                                new CodeReadingCircuitDefinition("main-method", "public static void main(String[] args) {",
                                                List.of("main-public", "static", "void", "main", "string-array", "args", "main-open"),
                                                List.of("public", "static", "void", "main", "String[]", "args", "{")),
                                new CodeReadingCircuitDefinition("array-declaration", "int[] scores = {80, 90, 100};",
                                                List.of("int-array-type", "scores-name", "scores-assignment",
                                                                "scores-initializer", "scores-semicolon"),
                                                List.of("int[]", "scores", "=", "{80, 90, 100}", ";")),
                                new CodeReadingCircuitDefinition("for-initialization", "for (int i = 0;",
                                                List.of("for-keyword", "loop-int", "initial-i", "initial-assignment",
                                                                "initial-zero", "initial-separator"),
                                                List.of("for", "int", "i", "=", "0", ";")),
                                new CodeReadingCircuitDefinition("for-condition-update", "i < scores.length; i++) {",
                                                List.of("condition-i", "less-than", "scores-length", "condition-separator",
                                                                "update-i", "increment", "for-open"),
                                                List.of("i", "<", "scores.length", ";", "i", "++", "{")),
                                new CodeReadingCircuitDefinition("array-output", "System.out.println(scores[i]);",
                                                List.of("print-command", "scores-element", "print-semicolon"),
                                                List.of("System.out.println", "scores[i]", ";")),
                                new CodeReadingCircuitDefinition("block-closes",
                                                "for文終了 }　mainメソッド終了 }　Mainクラス終了 }",
                                                List.of("for-close", "main-close", "class-close"),
                                                List.of("for文終了 }", "mainメソッド終了 }", "Mainクラス終了 }")));
        }

        private static Map<String, List<CodeReadingCodeLineDefinition>> createStage6CodeLines() {
                return Map.of(
                                "part-1", List.of(line(
                                                List.of("class-public", "class-keyword", "class-name", "class-open"), "", "")),
                                "part-2", List.of(new CodeReadingCodeLineDefinition(
                                                List.of(token("main-public"), token("static"), token("void"), token("main"),
                                                                new CodeReadingCodeTokenDefinition("string-array", "(", ""),
                                                                new CodeReadingCodeTokenDefinition("args", "", ")"), token("main-open")),
                                                "", "quiz-reading-code-line--part-2")),
                                "part-3", List.of(line(List.of("int-array-type", "scores-name", "scores-assignment",
                                                "scores-initializer", "scores-semicolon"), "", "")),
                                "part-4", List.of(new CodeReadingCodeLineDefinition(
                                                List.of(new CodeReadingCodeTokenDefinition("for-keyword", "", " ("),
                                                                token("loop-int"), token("initial-i"), token("initial-assignment"),
                                                                token("initial-zero"), token("initial-separator")), "", "")),
                                "part-5", List.of(new CodeReadingCodeLineDefinition(
                                                List.of(token("condition-i"), token("less-than"), token("scores-length"),
                                                                token("condition-separator"), token("update-i"),
                                                                new CodeReadingCodeTokenDefinition("increment", "", ")"), token("for-open")),
                                                "", "")),
                                "part-6", List.of(new CodeReadingCodeLineDefinition(
                                                List.of(token("print-command"),
                                                                new CodeReadingCodeTokenDefinition("scores-element", "(", ")"),
                                                                token("print-semicolon")), "", "")),
                                "part-7", List.of(line(List.of("for-close", "main-close", "class-close"), "", "")));
        }

        private static List<CodeReadingPart> createStage6Parts() {
                return List.of(
                                new CodeReadingPart("part-1", 1, "クラスを作る", List.of(), "public class Main {",
                                                List.of("class-public", "class-keyword", "class-name", "class-open"),
                                                List.of("public", "class", "Main", "{"), List.of(),
                                                "Mainというクラスを宣言し、その中身を始める"),
                                new CodeReadingPart("part-2", 2, "mainメソッドを作る", List.of(),
                                                "public static void main(String[] args) {",
                                                List.of("main-public", "static", "void", "main", "string-array", "args", "main-open"),
                                                List.of("public", "static", "void", "main", "String[]", "args", "{"), List.of(),
                                                "mainメソッドを宣言し、その中身を始める"),
                                new CodeReadingPart("part-3", 3, "配列を宣言して初期化する",
                                                List.of("3つの整数を持つint配列を生成し、変数scoresから参照します。"),
                                                "int[] scores = {80, 90, 100};",
                                                List.of("int-array-type", "scores-name", "scores-assignment",
                                                                "scores-initializer", "scores-semicolon"),
                                                List.of("int[]", "scores", "=", "{80, 90, 100}", ";"), List.of(),
                                                "int配列を生成して80、90、100で初期化し、scoresから参照する"),
                                new CodeReadingPart("part-4", 4, "配列の先頭から繰り返す",
                                                List.of("配列の最初のインデックス0から繰り返しを始めます。"),
                                                "for (int i = 0;",
                                                List.of("for-keyword", "loop-int", "initial-i", "initial-assignment",
                                                                "initial-zero", "initial-separator"),
                                                List.of("for", "int", "i", "=", "0", ";"), List.of(),
                                                "for文を始め、配列の位置を表す変数iを0で初期化する"),
                                new CodeReadingPart("part-5", 5, "要素数まで繰り返す",
                                                List.of("iが配列の要素数より小さい間、処理後にiを1増やします。"),
                                                "i < scores.length; i++) {",
                                                List.of("condition-i", "less-than", "scores-length", "condition-separator",
                                                                "update-i", "increment", "for-open"),
                                                List.of("i", "<", "scores.length", ";", "i", "++", "{"), List.of(),
                                                "有効なインデックス0、1、2の間だけ繰り返す"),
                                new CodeReadingPart("part-6", 6, "配列の要素を表示する",
                                                List.of("現在のiをインデックスにして配列要素を取得し、1行ずつ表示します。"),
                                                "System.out.println(scores[i]);",
                                                List.of("print-command", "scores-element", "print-semicolon"),
                                                List.of("System.out.println", "scores[i]", ";"), List.of(),
                                                "scores[i]で取得した80、90、100を順番に表示する"),
                                new CodeReadingPart("part-7", 7, "ブロックを閉じる",
                                                List.of("for文、mainメソッド、Mainクラスのブロックを順番に閉じます。"),
                                                "}\n}\n}",
                                                List.of("for-close", "main-close", "class-close"),
                                                List.of("}", "}", "}"), List.of(),
                                                "for文の更新と再判定を経て繰り返しを終え、プログラム全体を閉じる"));
        }

        private static List<CodeReadingStepDefinition> createStage7Steps(
                        CodeReadingLessonDefinition stage2,
                        CodeReadingLessonDefinition stage3) {
                List<CodeReadingExplanationSection> integerType =
                                sharedExplanation(stage2, "int-type");
                List<CodeReadingExplanationSection> variableName =
                                sharedExplanation(stage2, "age-declaration");
                List<CodeReadingExplanationSection> assignment =
                                sharedExplanation(stage2, "assignment");
                List<CodeReadingExplanationSection> integerLiteral =
                                sharedExplanation(stage2, "integer-literal");
                List<CodeReadingExplanationSection> variableUse =
                                sharedExplanation(stage2, "age-use");
                List<CodeReadingExplanationSection> addition =
                                sharedExplanation(stage3, "addition");
                List<CodeReadingExplanationSection> semicolon = semicolonExplanation();
                List<CodeReadingExplanationSection> actualArgument = actualArgumentExplanation();
                List<CodeReadingExplanationSection> formalParameter = formalParameterExplanation();
                return List.of(
                                richStep("class-public", 1, "public", "public", "accessible", PUBLIC_CARD,
                                                "アクセス修飾子", PUBLIC_EXPLANATION),
                                richStep("class-keyword", 2, "class", "class", "declare-class", CLASS_CARD,
                                                "クラスを作るキーワード", CLASS_EXPLANATION),
                                richStep("class-name", 3, "Main", "Main", "main-class-name", CLASS_NAME_CARD,
                                                "クラス名", CLASS_NAME_EXPLANATION),
                                richStep("class-open", 4, "{", "{", "block-start", BLOCK_START_CARD,
                                                "開始波かっこ", BLOCK_START_EXPLANATION),
                                richStep("main-public", 5, "public", "public", "accessible", PUBLIC_CARD,
                                                "アクセス修飾子", PUBLIC_EXPLANATION),
                                richStep("main-static", 6, "static", "static", "without-instance", STATIC_CARD,
                                                "static修飾子", STATIC_EXPLANATION),
                                richStep("main-void", 7, "void", "void", "no-return", VOID_CARD,
                                                "戻り値の型", VOID_EXPLANATION),
                                richStep("main-name", 8, "main", "main", "program-entry", MAIN_CARD,
                                                "メソッド名", MAIN_EXPLANATION),
                                richStep("main-string-array", 9, "String[]", "String[]", "multiple-strings",
                                                STRING_ARRAY_CARD, "引数の型", STRING_ARRAY_EXPLANATION),
                                richStep("main-args", 10, "args", "args", "argument-variable", ARGS_CARD,
                                                "引数名", ARGS_EXPLANATION),
                                richStep("main-open", 11, "{", "{", "block-start", BLOCK_START_CARD,
                                                "開始波かっこ", BLOCK_START_EXPLANATION),
                                richStep("result-int", 12, "int", "int", "integer-type", "整数を扱うデータ型",
                                                "整数型", integerType),
                                richStep("result-name", 13, "result", "result", "variable-name", "変数の名前",
                                                "変数名", variableName),
                                richStep("result-assignment", 14, "=", "=", "assignment",
                                                "右側から返された値を左側に入れる", "代入演算子", assignment),
                                richStep("add-call", 15, "add", "add", "method-invocation",
                                                "addメソッドを呼び出す", "メソッド呼び出し", methodCallExplanation()),
                                richStep("actual-argument-10", 16, "10", "10", "actual-argument",
                                                "1番目の仮引数aへ渡す値", "実引数", actualArgument),
                                richStep("actual-argument-20", 17, "20", "20", "actual-argument",
                                                "2番目の仮引数bへ渡す値", "実引数", actualArgument),
                                richStep("result-semicolon", 18, ";", ";", "command-end", "文の終わり",
                                                "セミコロン", semicolon),
                                richStep("print-command", 19, "System.out.println", "System.out.println",
                                                "display-and-newline", DISPLAY_AND_NEWLINE_CARD,
                                                "標準出力", SYSTEM_OUT_PRINTLN_EXPLANATION),
                                richStep("result-use", 20, "result", "result", "use-variable-value",
                                                VARIABLE_VALUE_USE_CARD, "変数の利用", variableUse),
                                richStep("print-semicolon", 21, ";", ";", "command-end", "文の終わり",
                                                "セミコロン", semicolon),
                                richStep("main-close", 22, "}", "}", "close-main", "mainメソッドを閉じる",
                                                "終了波かっこ", BLOCK_END_EXPLANATION),
                                richStep("add-public", 23, "public", "public", "accessible", PUBLIC_CARD,
                                                "アクセス修飾子", PUBLIC_EXPLANATION),
                                richStep("add-static", 24, "static", "static", "without-instance", STATIC_CARD,
                                                "static修飾子", STATIC_EXPLANATION),
                                richStep("add-return-type", 25, "int", "int", "method-return-type",
                                                "int型の値を呼び出し元へ返す", "戻り値の型", methodReturnTypeExplanation()),
                                richStep("add-name", 26, "add", "add", "method-declaration-name",
                                                "足し算を行うメソッドの名前", "メソッド名", methodDefinitionNameExplanation()),
                                richStep("parameter-a-int", 27, "int", "int", "integer-type",
                                                "整数を受け取るデータ型", "仮引数の型", integerType),
                                richStep("parameter-a", 28, "a", "a", "formal-parameter",
                                                "1番目の実引数を受け取る変数", "仮引数", formalParameter),
                                richStep("parameter-b-int", 29, "int", "int", "integer-type",
                                                "整数を受け取るデータ型", "仮引数の型", integerType),
                                richStep("parameter-b", 30, "b", "b", "formal-parameter",
                                                "2番目の実引数を受け取る変数", "仮引数", formalParameter),
                                richStep("add-open", 31, "{", "{", "block-start", BLOCK_START_CARD,
                                                "開始波かっこ", BLOCK_START_EXPLANATION),
                                richStep("return-keyword", 32, "return", "return", "return-value",
                                                "計算結果を呼び出し元へ返す", "return文", returnExplanation()),
                                richStep("return-a", 33, "a", "a", "use-variable-value",
                                                VARIABLE_VALUE_USE_CARD, "仮引数の利用", variableUse),
                                richStep("return-addition", 34, "+", "+", "addition", "左右の数値を足す",
                                                "算術演算子", addition),
                                richStep("return-b", 35, "b", "b", "use-variable-value",
                                                VARIABLE_VALUE_USE_CARD, "仮引数の利用", variableUse),
                                richStep("return-semicolon", 36, ";", ";", "command-end", "文の終わり",
                                                "セミコロン", semicolon),
                                richStep("add-close", 37, "}", "}", "close-method", "addメソッドを閉じる",
                                                "終了波かっこ", BLOCK_END_EXPLANATION),
                                richStep("class-close", 38, "}", "}", "close-class", "Mainクラスを閉じる",
                                                "終了波かっこ", BLOCK_END_EXPLANATION));
        }

        private static List<CodeReadingExplanationSection> methodCallExplanation() {
                return List.of(
                                textSection("メソッド呼び出し　method invocation",
                                                text("add(10, 20)は、addメソッドを呼び出し、2つの値を渡す式です。"),
                                                text("呼び出し位置とメソッドの定義位置は離れていても、メソッド名と引数によって対応します。")),
                                textSection("処理の移動",
                                                text("mainメソッドの処理はいったんaddメソッドへ移ります。"),
                                                text("addメソッドがreturnで値を返すと、処理は呼び出し元へ戻り、その値30がresultへ代入されます。")),
                                officialReferencesSection(OFFICIAL_REFERENCES_TITLE,
                                                jlsReference("§15.12", "Method Invocation Expressions",
                                                                "メソッド呼び出し式の探索、引数の受け渡し、実行を定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-15.html#jls-15.12")));
        }

        private static List<CodeReadingExplanationSection> actualArgumentExplanation() {
                return List.of(
                                textSection("実引数　argument",
                                                text("メソッドを呼び出すときに、丸かっこの中で実際に渡す値です。"),
                                                text("10は仮引数aへ、20は仮引数bへ、左から同じ順番で渡されます。")),
                                officialReferencesSection(OFFICIAL_REFERENCES_TITLE,
                                                jlsReference("§15.12.4.2", "Evaluate Arguments",
                                                                "メソッド呼び出しで引数を左から評価して仮引数へ渡す処理を定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-15.html#jls-15.12.4.2")));
        }

        private static List<CodeReadingExplanationSection> methodReturnTypeExplanation() {
                return List.of(
                                textSection("戻り値の型　return type",
                                                text("メソッド名の前にあるintは、このメソッドがint型の値を呼び出し元へ返すことを表します。"),
                                                text("mainメソッドのvoidは値を返さないことを表しますが、addメソッドは計算結果を返すためintです。")),
                                officialReferencesSection(OFFICIAL_REFERENCES_TITLE,
                                                jlsReference("§8.4.5", "Method Result",
                                                                "メソッドが返す値の型とvoidを定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-8.html#jls-8.4.5")));
        }

        private static List<CodeReadingExplanationSection> methodDefinitionNameExplanation() {
                return List.of(
                                textSection("メソッド名　method name",
                                                text("addは、このメソッドにつけた名前です。Javaの予約語ではありません。"),
                                                text("呼び出し側のadd(10, 20)は、この定義を利用します。")),
                                textSection("呼び出しと定義",
                                                text("Javaでは、同じクラス内のメソッド定義をmainメソッドより後ろに書くこともできます。"),
                                                text("ソースコードを上から実行して定義行を通過するのではなく、呼び出されたときにaddメソッドの処理へ移ります。")));
        }

        private static List<CodeReadingExplanationSection> formalParameterExplanation() {
                return List.of(
                                textSection("仮引数　formal parameter",
                                                text("メソッドが呼び出されたときに、呼び出し元から渡された値を受け取る変数です。"),
                                                text("このコードではaが10を、bが20を受け取ります。")),
                                officialReferencesSection(OFFICIAL_REFERENCES_TITLE,
                                                jlsReference("§8.4.1", "Formal Parameters",
                                                                "メソッド宣言に記述する仮引数とその型を定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-8.html#jls-8.4.1")));
        }

        private static List<CodeReadingExplanationSection> returnExplanation() {
                return List.of(
                                textSection("return文　return statement",
                                                text("return a + b;は、aとbを足した結果をaddメソッドの呼び出し元へ返します。"),
                                                text("値を返すとaddメソッドの処理は終わり、mainメソッドのadd(10, 20)を呼び出した位置へ戻ります。")),
                                textSection("値の流れ",
                                                text("a + bの結果は30です。"),
                                                text("返された30はint result = add(10, 20);の右側の値となり、resultへ代入されます。")),
                                officialReferencesSection(OFFICIAL_REFERENCES_TITLE,
                                                jlsReference("§14.17", "The return Statement",
                                                                "return文が値を呼び出し元へ返してメソッドの実行を終了することを定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-14.html#jls-14.17")));
        }

        private static List<CodeReadingCircuitDefinition> createStage7Circuits() {
                return List.of(
                                new CodeReadingCircuitDefinition("class-declaration", "public class Main {",
                                                List.of("class-public", "class-keyword", "class-name", "class-open"),
                                                List.of("public", "class", "Main", "{")),
                                new CodeReadingCircuitDefinition("main-method", "public static void main(String[] args) {",
                                                List.of("main-public", "main-static", "main-void", "main-name",
                                                                "main-string-array", "main-args", "main-open"),
                                                List.of("public", "static", "void", "main", "String[]", "args", "{")),
                                new CodeReadingCircuitDefinition("method-call", "int result = add(10, 20);",
                                                List.of("result-int", "result-name", "result-assignment", "add-call",
                                                                "actual-argument-10", "actual-argument-20", "result-semicolon"),
                                                List.of("int", "result", "=", "add", "10", "20", ";")),
                                new CodeReadingCircuitDefinition("result-output", "System.out.println(result);",
                                                List.of("print-command", "result-use", "print-semicolon"),
                                                List.of("System.out.println", "result", ";")),
                                new CodeReadingCircuitDefinition("main-close", "mainメソッド終了 }",
                                                List.of("main-close"), List.of("mainメソッド終了 }")),
                                new CodeReadingCircuitDefinition("add-header", "public static int add(",
                                                List.of("add-public", "add-static", "add-return-type", "add-name"),
                                                List.of("public", "static", "int", "add")),
                                new CodeReadingCircuitDefinition("add-parameters", "int a, int b) {",
                                                List.of("parameter-a-int", "parameter-a", "parameter-b-int",
                                                                "parameter-b", "add-open"),
                                                List.of("int", "a", "int", "b", "{")),
                                new CodeReadingCircuitDefinition("return-result", "return a + b;",
                                                List.of("return-keyword", "return-a", "return-addition", "return-b",
                                                                "return-semicolon"),
                                                List.of("return", "a", "+", "b", ";")),
                                new CodeReadingCircuitDefinition("block-closes",
                                                "addメソッド終了 }　Mainクラス終了 }",
                                                List.of("add-close", "class-close"),
                                                List.of("addメソッド終了 }", "Mainクラス終了 }")));
        }

        private static Map<String, List<CodeReadingCodeLineDefinition>> createStage7CodeLines() {
                return Map.of(
                                "part-1", List.of(line(
                                                List.of("class-public", "class-keyword", "class-name", "class-open"), "", "")),
                                "part-2", List.of(new CodeReadingCodeLineDefinition(
                                                List.of(token("main-public"), token("main-static"), token("main-void"),
                                                                token("main-name"),
                                                                new CodeReadingCodeTokenDefinition("main-string-array", "(", ""),
                                                                new CodeReadingCodeTokenDefinition("main-args", "", ")"), token("main-open")),
                                                "", "quiz-reading-code-line--part-2")),
                                "part-3", List.of(new CodeReadingCodeLineDefinition(
                                                List.of(token("result-int"), token("result-name"), token("result-assignment"),
                                                                new CodeReadingCodeTokenDefinition("add-call", "", "("),
                                                                new CodeReadingCodeTokenDefinition("actual-argument-10", "", ","),
                                                                new CodeReadingCodeTokenDefinition("actual-argument-20", "", ")"),
                                                                token("result-semicolon")), "", "")),
                                "part-4", List.of(new CodeReadingCodeLineDefinition(
                                                List.of(token("print-command"),
                                                                new CodeReadingCodeTokenDefinition("result-use", "(", ")"),
                                                                token("print-semicolon")), "", "")),
                                "part-5", List.of(line(List.of("main-close"), "", "")),
                                "part-6", List.of(new CodeReadingCodeLineDefinition(
                                                List.of(token("add-public"), token("add-static"), token("add-return-type"),
                                                                new CodeReadingCodeTokenDefinition("add-name", "", "(")), "", "")),
                                "part-7", List.of(new CodeReadingCodeLineDefinition(
                                                List.of(token("parameter-a-int"),
                                                                new CodeReadingCodeTokenDefinition("parameter-a", "", ","),
                                                                token("parameter-b-int"),
                                                                new CodeReadingCodeTokenDefinition("parameter-b", "", ")"),
                                                                token("add-open")), "", "")),
                                "part-8", List.of(line(List.of("return-keyword", "return-a", "return-addition",
                                                "return-b", "return-semicolon"), "", "")),
                                "part-9", List.of(line(List.of("add-close", "class-close"), "", "")));
        }

        private static List<CodeReadingPart> createStage7Parts() {
                return List.of(
                                new CodeReadingPart("part-1", 1, "クラスを作る", List.of(), "public class Main {",
                                                List.of("class-public", "class-keyword", "class-name", "class-open"),
                                                List.of("public", "class", "Main", "{"), List.of(),
                                                "Mainというクラスを宣言し、その中身を始める"),
                                new CodeReadingPart("part-2", 2, "mainメソッドを作る", List.of(),
                                                "public static void main(String[] args) {",
                                                List.of("main-public", "main-static", "main-void", "main-name",
                                                                "main-string-array", "main-args", "main-open"),
                                                List.of("public", "static", "void", "main", "String[]", "args", "{"),
                                                List.of(), "mainメソッドを宣言し、その中身を始める"),
                                new CodeReadingPart("part-3", 3, "addメソッドを呼び出す",
                                                List.of("10と20を渡してaddメソッドを呼び出し、戻り値をresultへ代入します。"),
                                                "int result = add(10, 20);",
                                                List.of("result-int", "result-name", "result-assignment", "add-call",
                                                                "actual-argument-10", "actual-argument-20", "result-semicolon"),
                                                List.of("int", "result", "=", "add", "10", "20", ";"), List.of(),
                                                "addメソッドへ10と20を渡し、返された30をresultへ代入する"),
                                new CodeReadingPart("part-4", 4, "計算結果を表示する",
                                                List.of("addメソッドから返されてresultへ入った値を表示します。"),
                                                "System.out.println(result);",
                                                List.of("print-command", "result-use", "print-semicolon"),
                                                List.of("System.out.println", "result", ";"), List.of(),
                                                "変数resultの値30を表示する"),
                                new CodeReadingPart("part-5", 5, "mainメソッドを閉じる", List.of(), "}",
                                                List.of("main-close"), List.of("}"), List.of(),
                                                "mainメソッドの処理を閉じる"),
                                new CodeReadingPart("part-6", 6, "addメソッドを定義する",
                                                List.of("int型の値を返すstaticなaddメソッドの定義を始めます。"),
                                                "public static int add(",
                                                List.of("add-public", "add-static", "add-return-type", "add-name"),
                                                List.of("public", "static", "int", "add"), List.of(),
                                                "呼び出し元へint型の値を返すaddメソッドを宣言する"),
                                new CodeReadingPart("part-7", 7, "仮引数で値を受け取る",
                                                List.of("呼び出し元の10と20を、int型の仮引数aとbで受け取ります。"),
                                                "int a, int b) {",
                                                List.of("parameter-a-int", "parameter-a", "parameter-b-int",
                                                                "parameter-b", "add-open"),
                                                List.of("int", "a", "int", "b", "{"), List.of(),
                                                "2つのint型の仮引数aとbで実引数を受け取る"),
                                new CodeReadingPart("part-8", 8, "計算結果をreturnで返す",
                                                List.of("aとbを足した結果を呼び出し元へ返し、mainメソッドの処理へ戻ります。"),
                                                "return a + b;",
                                                List.of("return-keyword", "return-a", "return-addition", "return-b",
                                                                "return-semicolon"),
                                                List.of("return", "a", "+", "b", ";"), List.of(),
                                                "aとbを足した30を呼び出し元へ返す"),
                                new CodeReadingPart("part-9", 9, "プログラムを閉じる",
                                                List.of("addメソッドとMainクラスのブロックを順番に閉じます。"),
                                                "}\n}", List.of("add-close", "class-close"), List.of("}", "}"), List.of(),
                                                "addメソッドとMainクラスを閉じる"));
        }

        private static List<CodeReadingStepDefinition> createStage8Steps(
                        CodeReadingLessonDefinition stage2,
                        CodeReadingLessonDefinition stage4) {
                List<CodeReadingExplanationSection> variableName =
                                sharedExplanation(stage2, "age-declaration");
                List<CodeReadingExplanationSection> assignment =
                                sharedExplanation(stage2, "assignment");
                List<CodeReadingExplanationSection> stringType =
                                sharedExplanation(stage4, "signal-string-type");
                List<CodeReadingExplanationSection> carType = carTypeExplanation();
                List<CodeReadingExplanationSection> referenceVariable = referenceVariableExplanation();
                List<CodeReadingExplanationSection> instanceCreation = instanceCreationExplanation();
                List<CodeReadingExplanationSection> fieldAccess = fieldAccessExplanation();
                List<CodeReadingExplanationSection> colorLiteral = colorLiteralExplanation();
                List<CodeReadingExplanationSection> instanceMethodCall = instanceMethodCallExplanation();
                List<CodeReadingExplanationSection> carClassName = carClassNameExplanation();
                List<CodeReadingExplanationSection> colorField = colorFieldExplanation();
                List<CodeReadingExplanationSection> showColorMethod = showColorMethodExplanation();
                List<CodeReadingExplanationSection> fieldValueUse = fieldValueUseExplanation();
                return List.of(
                                richStep("class-public", 1, "public", "public", "accessible", PUBLIC_CARD,
                                                "アクセス修飾子", PUBLIC_EXPLANATION),
                                richStep("class-keyword", 2, "class", "class", "declare-class", CLASS_CARD,
                                                "クラスを作るキーワード", CLASS_EXPLANATION),
                                richStep("class-name", 3, "Main", "Main", "main-class-name", CLASS_NAME_CARD,
                                                "クラス名", CLASS_NAME_EXPLANATION),
                                richStep("class-open", 4, "{", "{", "block-start", BLOCK_START_CARD,
                                                "開始波かっこ", BLOCK_START_EXPLANATION),
                                richStep("main-public", 5, "public", "public", "accessible", PUBLIC_CARD,
                                                "アクセス修飾子", PUBLIC_EXPLANATION),
                                richStep("main-static", 6, "static", "static", "without-instance", STATIC_CARD,
                                                "static修飾子", STATIC_EXPLANATION),
                                richStep("main-void", 7, "void", "void", "no-return", VOID_CARD,
                                                "戻り値の型", VOID_EXPLANATION),
                                richStep("main-name", 8, "main", "main", "program-entry", MAIN_CARD,
                                                "メソッド名", MAIN_EXPLANATION),
                                richStep("main-string-array", 9, "String[]", "String[]", "multiple-strings",
                                                STRING_ARRAY_CARD, "引数の型", STRING_ARRAY_EXPLANATION),
                                richStep("main-args", 10, "args", "args", "argument-variable", ARGS_CARD,
                                                "引数名", ARGS_EXPLANATION),
                                richStep("main-open", 11, "{", "{", "block-start", BLOCK_START_CARD,
                                                "開始波かっこ", BLOCK_START_EXPLANATION),
                                richStep("car1-type", 12, "Car", "Car", "reference-type",
                                                "Car型の参照を扱う", "参照型", carType),
                                richStep("car1-name", 13, "car1", "car1", "reference-variable",
                                                "1台目を参照する変数", "参照変数", referenceVariable),
                                richStep("car1-assignment", 14, "=", "=", "assignment",
                                                "右側の参照を左側に入れる", "代入演算子", assignment),
                                richStep("car1-new", 15, "new", "new", "create-instance",
                                                "新しいインスタンスを生成する", "クラスインスタンス生成式", instanceCreation),
                                richStep("car1-constructor", 16, "Car()", "Car()", "constructor-call",
                                                "Carクラスからオブジェクトを作る", "コンストラクタ呼び出し", instanceCreation),
                                richStep("car1-semicolon", 17, ";", ";", "command-end", "文の終わり",
                                                "セミコロン", SEMICOLON_EXPLANATION),
                                richStep("car2-type", 18, "Car", "Car", "reference-type",
                                                "Car型の参照を扱う", "参照型", carType),
                                richStep("car2-name", 19, "car2", "car2", "reference-variable",
                                                "2台目を参照する変数", "参照変数", referenceVariable),
                                richStep("car2-assignment", 20, "=", "=", "assignment",
                                                "右側の参照を左側に入れる", "代入演算子", assignment),
                                richStep("car2-new", 21, "new", "new", "create-instance",
                                                "別のインスタンスを生成する", "クラスインスタンス生成式", instanceCreation),
                                richStep("car2-constructor", 22, "Car()", "Car()", "constructor-call",
                                                "Carクラスから別のオブジェクトを作る", "コンストラクタ呼び出し", instanceCreation),
                                richStep("car2-semicolon", 23, ";", ";", "command-end", "文の終わり",
                                                "セミコロン", SEMICOLON_EXPLANATION),
                                richStep("car1-field-owner", 24, "car1", "car1", "field-owner",
                                                "1台目のオブジェクトを参照する", "参照変数", referenceVariable),
                                richStep("car1-color-field", 25, "color", "color", "instance-field",
                                                "1台目のcolorフィールド", "フィールドアクセス", fieldAccess),
                                richStep("car1-color-assignment", 26, "=", "=", "assignment",
                                                "redをフィールドに入れる", "代入演算子", assignment),
                                richStep("red-literal", 27, "\"red\"", "\"red\"", "string-value",
                                                "文字列red", "文字列リテラル", colorLiteral),
                                richStep("car1-color-semicolon", 28, ";", ";", "command-end", "文の終わり",
                                                "セミコロン", SEMICOLON_EXPLANATION),
                                richStep("car2-field-owner", 29, "car2", "car2", "field-owner",
                                                "2台目のオブジェクトを参照する", "参照変数", referenceVariable),
                                richStep("car2-color-field", 30, "color", "color", "instance-field",
                                                "2台目のcolorフィールド", "フィールドアクセス", fieldAccess),
                                richStep("car2-color-assignment", 31, "=", "=", "assignment",
                                                "blueをフィールドに入れる", "代入演算子", assignment),
                                richStep("blue-literal", 32, "\"blue\"", "\"blue\"", "string-value",
                                                "文字列blue", "文字列リテラル", colorLiteral),
                                richStep("car2-color-semicolon", 33, ";", ";", "command-end", "文の終わり",
                                                "セミコロン", SEMICOLON_EXPLANATION),
                                richStep("car1-method-owner", 34, "car1", "car1", "method-owner",
                                                "1台目のオブジェクトを参照する", "参照変数", referenceVariable),
                                richStep("car1-show-color", 35, "showColor()", "showColor()", "instance-method-call",
                                                "1台目の色を表示する", "インスタンスメソッド呼び出し", instanceMethodCall),
                                richStep("car1-call-semicolon", 36, ";", ";", "command-end", "文の終わり",
                                                "セミコロン", SEMICOLON_EXPLANATION),
                                richStep("car2-method-owner", 37, "car2", "car2", "method-owner",
                                                "2台目のオブジェクトを参照する", "参照変数", referenceVariable),
                                richStep("car2-show-color", 38, "showColor()", "showColor()", "instance-method-call",
                                                "2台目の色を表示する", "インスタンスメソッド呼び出し", instanceMethodCall),
                                richStep("car2-call-semicolon", 39, ";", ";", "command-end", "文の終わり",
                                                "セミコロン", SEMICOLON_EXPLANATION),
                                richStep("main-close", 40, "}", "}", "close-method", "mainメソッドを閉じる",
                                                "終了波かっこ", BLOCK_END_EXPLANATION),
                                richStep("main-class-close", 41, "}", "}", "close-class", "Mainクラスを閉じる",
                                                "終了波かっこ", BLOCK_END_EXPLANATION),
                                richStep("car-class-keyword", 42, "class", "class", "declare-class", CLASS_CARD,
                                                "クラスを作るキーワード", CLASS_EXPLANATION),
                                richStep("car-class-name", 43, "Car", "Car", "car-class-name",
                                                "車を表すクラス名", "クラス名", carClassName),
                                richStep("car-class-open", 44, "{", "{", "block-start", BLOCK_START_CARD,
                                                "開始波かっこ", BLOCK_START_EXPLANATION),
                                richStep("color-string-type", 45, "String", "String", "string-type",
                                                "文字列を扱うデータ型", "文字列型", stringType),
                                richStep("color-field-name", 46, "color", "color", "instance-field",
                                                "インスタンスごとに色を持つ", "フィールド", colorField),
                                richStep("color-field-semicolon", 47, ";", ";", "command-end", "文の終わり",
                                                "セミコロン", SEMICOLON_EXPLANATION),
                                richStep("show-color-void", 48, "void", "void", "no-return", VOID_CARD,
                                                "戻り値の型", VOID_EXPLANATION),
                                richStep("show-color-name", 49, "showColor", "showColor", "instance-method",
                                                "色を表示するメソッド", "インスタンスメソッド", showColorMethod),
                                richStep("show-color-open", 50, "{", "{", "block-start", BLOCK_START_CARD,
                                                "開始波かっこ", BLOCK_START_EXPLANATION),
                                richStep("print-command", 51, "System.out.println", "System.out.println",
                                                "display-and-newline", DISPLAY_AND_NEWLINE_CARD,
                                                "標準出力メソッド", SYSTEM_OUT_PRINTLN_EXPLANATION),
                                richStep("print-color", 52, "color", "color", "field-value",
                                                "呼び出した車の色を使う", "フィールドの利用", fieldValueUse),
                                richStep("print-semicolon", 53, ";", ";", "command-end", "文の終わり",
                                                "セミコロン", SEMICOLON_EXPLANATION),
                                richStep("show-color-close", 54, "}", "}", "close-method",
                                                "showColorメソッドを閉じる", "終了波かっこ", BLOCK_END_EXPLANATION),
                                richStep("car-class-close", 55, "}", "}", "close-class", "Carクラスを閉じる",
                                                "終了波かっこ", BLOCK_END_EXPLANATION));
        }

        private static List<CodeReadingExplanationSection> carTypeExplanation() {
                return List.of(
                                textSection("参照型　reference type",
                                                text("Carは、このあと定義するCarクラスを型として使っています。"),
                                                text("Car型の変数には、Carクラスから生成したオブジェクトへの参照を保持できます。")),
                                textSection("クラスと変数",
                                                text("Carはオブジェクトの構造や動作を定義するクラスです。"),
                                                text("car1やcar2はオブジェクトそのものではなく、生成したインスタンスを参照する変数です。")),
                                officialReferencesSection(OFFICIAL_REFERENCES_TITLE,
                                                jlsReference("§4.3.1", "Objects",
                                                                "クラスインスタンスがオブジェクトであり、参照値を通して扱われることを定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-4.html#jls-4.3.1")));
        }

        private static List<CodeReadingExplanationSection> referenceVariableExplanation() {
                return List.of(
                                textSection("参照変数　reference variable",
                                                text("car1とcar2は、Carクラスから生成したインスタンスへの参照を保持する変数です。"),
                                                text("同じCarクラスから生成しても、2つの変数はそれぞれ別のインスタンスを参照します。")),
                                textSection("言葉の区別",
                                                text("car1そのものをインスタンスと呼ぶのではなく、car1はインスタンスを参照する変数です。")));
        }

        private static List<CodeReadingExplanationSection> instanceCreationExplanation() {
                return List.of(
                                textSection("クラスインスタンス生成式　class instance creation expression",
                                                text("new Car()は、プログラム内に新しいCarのインスタンスを生成します。"),
                                                text("現実の自動車を製造するのではなく、車を表すデータと動作を持つオブジェクトをメモリ上に作ります。")),
                                textSection("2回のnew",
                                                text("new Car()を2回実行するため、car1とcar2は互いに独立した2つのインスタンスを参照します。")),
                                officialReferencesSection(OFFICIAL_REFERENCES_TITLE,
                                                jlsReference("§15.9", "Class Instance Creation Expressions",
                                                                "newによるクラスインスタンスの生成とコンストラクタ呼び出しを定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-15.html#jls-15.9")));
        }

        private static List<CodeReadingExplanationSection> fieldAccessExplanation() {
                return List.of(
                                textSection("フィールドアクセス　field access",
                                                text("car1.colorは、car1が参照するインスタンスのcolorフィールドを指定します。"),
                                                text("car2.colorは別のインスタンスのcolorフィールドなので、互いに異なる値を保持できます。")),
                                officialReferencesSection(OFFICIAL_REFERENCES_TITLE,
                                                jlsReference("§15.11", "Field Access Expressions",
                                                                "オブジェクトへの参照を使ってフィールドへアクセスする式を定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-15.html#jls-15.11")));
        }

        private static List<CodeReadingExplanationSection> colorLiteralExplanation() {
                return List.of(
                                textSection("文字列リテラル　string literal",
                                                text("ダブルクォートで囲んだredとblueは、色を表すString型の値です。"),
                                                text("redはcar1のcolorへ、blueはcar2のcolorへそれぞれ代入されます。")));
        }

        private static List<CodeReadingExplanationSection> instanceMethodCallExplanation() {
                return List.of(
                                textSection("インスタンスメソッド呼び出し　instance method invocation",
                                                text("car1.showColor()は、car1が参照するインスタンスのshowColorメソッドを呼び出します。"),
                                                text("car2.showColor()では、car2が参照する別のインスタンスを対象に同じメソッドを呼び出します。")),
                                textSection("呼び出す対象",
                                                text("showColor()の処理は同じでも、呼び出し対象が異なるため、それぞれのcolorの値redとblueが表示されます。")),
                                officialReferencesSection(OFFICIAL_REFERENCES_TITLE,
                                                jlsReference("§15.12", "Method Invocation Expressions",
                                                                "対象となるオブジェクトを使ったメソッド呼び出しを定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-15.html#jls-15.12")));
        }

        private static List<CodeReadingExplanationSection> carClassNameExplanation() {
                return List.of(
                                textSection("クラス名　class name",
                                                text("Carは、車を表すオブジェクトの構造と動作を定義するクラスの名前です。"),
                                                text("このクラスには、色を保存するcolorフィールドと、色を表示するshowColorメソッドがあります。")));
        }

        private static List<CodeReadingExplanationSection> colorFieldExplanation() {
                return List.of(
                                textSection("インスタンスフィールド　instance field",
                                                text("colorは、Carの各インスタンスがそれぞれ持つString型のフィールドです。"),
                                                text("car1のcolorとcar2のcolorは別々に保存されるため、redとblueという異なる値を保持できます。")),
                                officialReferencesSection(OFFICIAL_REFERENCES_TITLE,
                                                jlsReference("§8.3.1.1", "static Fields",
                                                                "staticでないフィールドがクラスの各インスタンスに作られることを定めています。",
                                                                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-8.html#jls-8.3.1.1")));
        }

        private static List<CodeReadingExplanationSection> showColorMethodExplanation() {
                return List.of(
                                textSection("インスタンスメソッド　instance method",
                                                text("showColorはstaticが付いていないため、Carのインスタンスを対象に呼び出すメソッドです。"),
                                                text("呼び出されたインスタンスが持つcolorフィールドの値を表示します。")));
        }

        private static List<CodeReadingExplanationSection> fieldValueUseExplanation() {
                return List.of(
                                textSection("フィールドの利用",
                                                text("showColorメソッド内のcolorは、メソッドを呼び出した対象インスタンスのcolorフィールドを参照します。"),
                                                text("car1.showColor()ではred、car2.showColor()ではblueがSystem.out.printlnへ渡されます。")));
        }

        private static List<CodeReadingCircuitDefinition> createStage8Circuits() {
                return List.of(
                                new CodeReadingCircuitDefinition("main-class", "public class Main {",
                                                List.of("class-public", "class-keyword", "class-name", "class-open"),
                                                List.of("public", "class", "Main", "{")),
                                new CodeReadingCircuitDefinition("main-method", "public static void main(String[] args) {",
                                                List.of("main-public", "main-static", "main-void", "main-name",
                                                                "main-string-array", "main-args", "main-open"),
                                                List.of("public", "static", "void", "main", "String[]", "args", "{")),
                                new CodeReadingCircuitDefinition("car1-creation", "Car car1 = new Car();",
                                                List.of("car1-type", "car1-name", "car1-assignment", "car1-new",
                                                                "car1-constructor", "car1-semicolon"),
                                                List.of("Car", "car1", "=", "new", "Car()", ";")),
                                new CodeReadingCircuitDefinition("car2-creation", "Car car2 = new Car();",
                                                List.of("car2-type", "car2-name", "car2-assignment", "car2-new",
                                                                "car2-constructor", "car2-semicolon"),
                                                List.of("Car", "car2", "=", "new", "Car()", ";")),
                                new CodeReadingCircuitDefinition("car1-color", "car1.color = \"red\";",
                                                List.of("car1-field-owner", "car1-color-field", "car1-color-assignment",
                                                                "red-literal", "car1-color-semicolon"),
                                                List.of("car1", "color", "=", "\"red\"", ";")),
                                new CodeReadingCircuitDefinition("car2-color", "car2.color = \"blue\";",
                                                List.of("car2-field-owner", "car2-color-field", "car2-color-assignment",
                                                                "blue-literal", "car2-color-semicolon"),
                                                List.of("car2", "color", "=", "\"blue\"", ";")),
                                new CodeReadingCircuitDefinition("show-colors", "car1.showColor(); / car2.showColor();",
                                                List.of("car1-method-owner", "car1-show-color", "car1-call-semicolon",
                                                                "car2-method-owner", "car2-show-color", "car2-call-semicolon"),
                                                List.of("car1", "showColor()", ";", "car2", "showColor()", ";")),
                                new CodeReadingCircuitDefinition("main-closes", "mainメソッド終了 }　Mainクラス終了 }",
                                                List.of("main-close", "main-class-close"),
                                                List.of("mainメソッド終了 }", "Mainクラス終了 }")),
                                new CodeReadingCircuitDefinition("car-class", "class Car {",
                                                List.of("car-class-keyword", "car-class-name", "car-class-open"),
                                                List.of("class", "Car", "{")),
                                new CodeReadingCircuitDefinition("color-field", "String color;",
                                                List.of("color-string-type", "color-field-name", "color-field-semicolon"),
                                                List.of("String", "color", ";")),
                                new CodeReadingCircuitDefinition("show-color-method", "void showColor() {",
                                                List.of("show-color-void", "show-color-name", "show-color-open"),
                                                List.of("void", "showColor", "{")),
                                new CodeReadingCircuitDefinition("show-color-body", "System.out.println(color); と終了",
                                                List.of("print-command", "print-color", "print-semicolon",
                                                                "show-color-close", "car-class-close"),
                                                List.of("System.out.println", "color", ";", "}", "}")));
        }

        private static Map<String, List<CodeReadingCodeLineDefinition>> createStage8CodeLines() {
                return Map.ofEntries(
                                Map.entry("part-1", List.of(line(
                                                List.of("class-public", "class-keyword", "class-name", "class-open"), "", ""))),
                                Map.entry("part-2", List.of(new CodeReadingCodeLineDefinition(
                                                List.of(token("main-public"), token("main-static"), token("main-void"),
                                                                token("main-name"),
                                                                new CodeReadingCodeTokenDefinition("main-string-array", "(", ""),
                                                                new CodeReadingCodeTokenDefinition("main-args", "", ")"), token("main-open")),
                                                "", "quiz-reading-code-line--part-2"))),
                                Map.entry("part-3", List.of(new CodeReadingCodeLineDefinition(
                                                List.of(token("car1-type"), token("car1-name"), token("car1-assignment"),
                                                                token("car1-new"), token("car1-constructor"), token("car1-semicolon")),
                                                "", ""))),
                                Map.entry("part-4", List.of(new CodeReadingCodeLineDefinition(
                                                List.of(token("car2-type"), token("car2-name"), token("car2-assignment"),
                                                                token("car2-new"), token("car2-constructor"), token("car2-semicolon")),
                                                "", ""))),
                                Map.entry("part-5", List.of(new CodeReadingCodeLineDefinition(
                                                List.of(new CodeReadingCodeTokenDefinition("car1-field-owner", "", "."),
                                                                token("car1-color-field"), token("car1-color-assignment"),
                                                                token("red-literal"), token("car1-color-semicolon")), "", ""))),
                                Map.entry("part-6", List.of(new CodeReadingCodeLineDefinition(
                                                List.of(new CodeReadingCodeTokenDefinition("car2-field-owner", "", "."),
                                                                token("car2-color-field"), token("car2-color-assignment"),
                                                                token("blue-literal"), token("car2-color-semicolon")), "", ""))),
                                Map.entry("part-7", List.of(
                                                new CodeReadingCodeLineDefinition(
                                                                List.of(new CodeReadingCodeTokenDefinition("car1-method-owner", "", "."),
                                                                                token("car1-show-color"), token("car1-call-semicolon")), "", ""),
                                                new CodeReadingCodeLineDefinition(
                                                                List.of(new CodeReadingCodeTokenDefinition("car2-method-owner", "", "."),
                                                                                token("car2-show-color"), token("car2-call-semicolon")), "", ""))),
                                Map.entry("part-8", List.of(line(List.of("main-close", "main-class-close"), "", ""))),
                                Map.entry("part-9", List.of(line(
                                                List.of("car-class-keyword", "car-class-name", "car-class-open"), "", ""))),
                                Map.entry("part-10", List.of(line(
                                                List.of("color-string-type", "color-field-name", "color-field-semicolon"), "", ""))),
                                Map.entry("part-11", List.of(new CodeReadingCodeLineDefinition(
                                                List.of(token("show-color-void"),
                                                                new CodeReadingCodeTokenDefinition("show-color-name", "", "()"),
                                                                token("show-color-open")), "", ""))),
                                Map.entry("part-12", List.of(
                                                new CodeReadingCodeLineDefinition(
                                                                List.of(token("print-command"),
                                                                                new CodeReadingCodeTokenDefinition("print-color", "(", ")"),
                                                                                token("print-semicolon")), "", ""),
                                                line(List.of("show-color-close", "car-class-close"), "", ""))));
        }

        private static List<CodeReadingPart> createStage8Parts() {
                return List.of(
                                new CodeReadingPart("part-1", 1, "Mainクラスを作る", List.of(), "public class Main {",
                                                List.of("class-public", "class-keyword", "class-name", "class-open"),
                                                List.of("public", "class", "Main", "{"), List.of(),
                                                "Mainというクラスを宣言し、その中身を始める"),
                                new CodeReadingPart("part-2", 2, "mainメソッドを作る", List.of(),
                                                "public static void main(String[] args) {",
                                                List.of("main-public", "main-static", "main-void", "main-name",
                                                                "main-string-array", "main-args", "main-open"),
                                                List.of("public", "static", "void", "main", "String[]", "args", "{"),
                                                List.of(), "mainメソッドを宣言し、その中身を始める"),
                                new CodeReadingPart("part-3", 3, "1台目の車を表すオブジェクトを作る",
                                                List.of("new Car()でCarのインスタンスを生成し、その参照をcar1へ代入します。"),
                                                "Car car1 = new Car();",
                                                List.of("car1-type", "car1-name", "car1-assignment", "car1-new",
                                                                "car1-constructor", "car1-semicolon"),
                                                List.of("Car", "car1", "=", "new", "Car()", ";"), List.of(),
                                                "新しいCarインスタンスを生成し、参照を変数car1へ代入する"),
                                new CodeReadingPart("part-4", 4, "2台目の車を表すオブジェクトを作る",
                                                List.of("もう一度new Car()を実行し、1台目とは別のインスタンスを生成します。"),
                                                "Car car2 = new Car();",
                                                List.of("car2-type", "car2-name", "car2-assignment", "car2-new",
                                                                "car2-constructor", "car2-semicolon"),
                                                List.of("Car", "car2", "=", "new", "Car()", ";"), List.of(),
                                                "別のCarインスタンスを生成し、参照を変数car2へ代入する"),
                                new CodeReadingPart("part-5", 5, "1台目の色を保存する",
                                                List.of("car1が参照するインスタンスのcolorフィールドへredを代入します。"),
                                                "car1.color = \"red\";",
                                                List.of("car1-field-owner", "car1-color-field", "car1-color-assignment",
                                                                "red-literal", "car1-color-semicolon"),
                                                List.of("car1", "color", "=", "\"red\"", ";"), List.of(),
                                                "1台目のインスタンスだけが持つcolorへredを保存する"),
                                new CodeReadingPart("part-6", 6, "2台目の色を保存する",
                                                List.of("car2が参照する別のインスタンスのcolorフィールドへblueを代入します。"),
                                                "car2.color = \"blue\";",
                                                List.of("car2-field-owner", "car2-color-field", "car2-color-assignment",
                                                                "blue-literal", "car2-color-semicolon"),
                                                List.of("car2", "color", "=", "\"blue\"", ";"), List.of(),
                                                "2台目のインスタンスだけが持つcolorへblueを保存する"),
                                new CodeReadingPart("part-7", 7, "2台の色を順番に表示する",
                                                List.of("同じshowColorメソッドを、car1とcar2が参照する別々のインスタンスに対して呼び出します。"),
                                                "car1.showColor();\ncar2.showColor();",
                                                List.of("car1-method-owner", "car1-show-color", "car1-call-semicolon",
                                                                "car2-method-owner", "car2-show-color", "car2-call-semicolon"),
                                                List.of("car1", "showColor()", ";", "car2", "showColor()", ";"), List.of(),
                                                "car1ではred、car2ではblueを表示するため同じメソッドを順番に呼ぶ"),
                                new CodeReadingPart("part-8", 8, "Mainクラスを閉じる", List.of(), "}\n}",
                                                List.of("main-close", "main-class-close"), List.of("}", "}"), List.of(),
                                                "mainメソッドとMainクラスを閉じる"),
                                new CodeReadingPart("part-9", 9, "Carクラスを作る",
                                                List.of("車を表す各オブジェクトが持つデータと動作をCarクラスに定義します。"),
                                                "class Car {",
                                                List.of("car-class-keyword", "car-class-name", "car-class-open"),
                                                List.of("class", "Car", "{"), List.of(),
                                                "Carというクラスを宣言し、その中身を始める"),
                                new CodeReadingPart("part-10", 10, "色を保存するフィールドを作る",
                                                List.of("各Carインスタンスがそれぞれ文字列の色を保持できるようにします。"),
                                                "String color;",
                                                List.of("color-string-type", "color-field-name", "color-field-semicolon"),
                                                List.of("String", "color", ";"), List.of(),
                                                "各インスタンスが独立して持つString型のcolorフィールドを宣言する"),
                                new CodeReadingPart("part-11", 11, "色を表示するメソッドを作る",
                                                List.of("呼び出し対象のインスタンスが持つ色を表示するメソッドを定義します。"),
                                                "void showColor() {",
                                                List.of("show-color-void", "show-color-name", "show-color-open"),
                                                List.of("void", "showColor", "{"), List.of(),
                                                "戻り値を返さないインスタンスメソッドshowColorを宣言する"),
                                new CodeReadingPart("part-12", 12, "呼び出した車の色を表示して閉じる",
                                                List.of("呼び出し対象のインスタンスのcolorを表示し、メソッドとCarクラスを閉じます。"),
                                                "System.out.println(color);\n}\n}",
                                                List.of("print-command", "print-color", "print-semicolon",
                                                                "show-color-close", "car-class-close"),
                                                List.of("System.out.println", "color", ";", "}", "}"), List.of(),
                                                "呼び出し対象のcolorを表示し、showColorメソッドとCarクラスを閉じる"));
        }
}
