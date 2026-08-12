package com.javalink.service;

import com.javalink.model.CodeReadingLessonDefinition;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodeReadingLessonCatalogTest {

    private final CodeReadingLessonCatalog catalog =
            new CodeReadingLessonCatalog();

    @Test
    void Stage1固有情報を一つの教材定義から取得できる() {
        CodeReadingLessonDefinition definition = catalog.getDefinition(
                LessonService.HELLO_PROGRAM_LESSON_ID
        );

        assertEquals("Stage 1", definition.stageName());
        assertEquals(4, definition.parts().size());
        assertEquals(4, definition.circuits().size());
        assertEquals(16, definition.steps().size());
        assertEquals("Hello", definition.consoleOutput());
        assertTrue(catalog.supports(LessonService.HELLO_PROGRAM_LESSON_ID));
    }

    @Test
    void Step定義に正解カードと正本の説明セクションを保持する() {
        var step = catalog.getDefinition(
                LessonService.HELLO_PROGRAM_LESSON_ID
        ).getStep("main-public");

        assertEquals("accessible", step.correctCard().id());
        assertEquals("ほかの場所からも使える", step.correctCard().text());
        assertEquals("アクセス修飾子", step.technicalTerm());
        assertEquals(
                java.util.List.of("text", "table", "text", "official-references"),
                step.explanationSections().stream()
                        .map(section -> section.sectionType().value()).toList()
        );
        assertTrue(step.explanationSections().stream()
                .flatMap(section -> section.entries().stream())
                .anyMatch(entry -> entry.emphasis().equals("public")));
        assertEquals("main-public", step.toLessonStep().id());
    }

    @Test
    void publicKeepsBeginnerSectionsAndAddsJavaSe21OfficialReferences() {
        var step = catalog.getDefinition(
                LessonService.HELLO_PROGRAM_LESSON_ID
        ).getStep("class-public");

        var sections = step.explanationSections();
        assertEquals("アクセス修飾子　access modifier　", sections.get(0).title());
        assertEquals("アクセスできる範囲の違い", sections.get(1).title());
        assertEquals("ポイント", sections.get(2).title());
        assertEquals("技術的根拠：Javaの公式仕様・API", sections.get(3).title());

        var references = sections.get(3).officialReferences();
        assertEquals(2, references.size());
        assertEquals(java.util.List.of("§6.6", "§6.6.1"), references.stream()
                .map(reference -> reference.sectionNumber()).toList());
        assertTrue(references.stream().allMatch(reference ->
                reference.source().value().equals("jls")
                        && reference.version().equals("Java SE 21")
                        && reference.sourceName().equals("JLS Java SE 21")
                        && reference.uri().getScheme().equals("https")
                        && reference.uri().getHost().equals("docs.oracle.com")));
        assertEquals(
                java.util.List.of(
                        "Javaのアクセス制御について定めています。",
                        "public などで宣言された要素にアクセスできる条件を定めています。"
                ),
                references.stream().map(reference -> reference.description()).toList()
        );
        assertFalse(references.stream().anyMatch(reference ->
                reference.source().value().equals("java-se-api")));
    }

    @Test
    void classUsesDeclarationBasicsAndAddsJavaSe21OfficialReferences() {
        var step = catalog.getDefinition(
                LessonService.HELLO_PROGRAM_LESSON_ID
        ).getStep("class-keyword");

        var sections = step.explanationSections();
        assertEquals("クラス　class　", sections.get(0).title());
        assertEquals("クラス宣言の基本形", sections.get(1).title());
        assertEquals("ポイント", sections.get(2).title());
        assertEquals("技術的根拠：Javaの公式仕様・API", sections.get(3).title());
        assertFalse(sections.stream().anyMatch(section -> section.title().contains("家でたとえる")));

        assertEquals(
                java.util.List.of(
                        "class クラス名 {　　}",
                        "class",
                        "クラス名",
                        "{ ～ }"),
                sections.get(1).entries().stream().map(entry -> entry.label()).toList()
        );
        assertEquals(
                java.util.List.of(
                        "",
                        "クラスを作る",
                        "作るクラスの名前（自分で決めることができます）",
                        "クラスの中身を書く範囲"),
                sections.get(1).entries().stream().map(entry -> entry.before()).toList()
        );

        var references = sections.get(3).officialReferences();
        assertEquals(2, references.size());
        assertEquals(java.util.List.of("§8.1", "§3.9"), references.stream()
                .map(reference -> reference.sectionNumber()).toList());
        assertEquals(java.util.List.of("Class Declarations", "Keywords"), references.stream()
                .map(reference -> reference.sectionTitle()).toList());
        assertTrue(references.stream().allMatch(reference ->
                reference.source().value().equals("jls")
                        && reference.version().equals("Java SE 21")
                        && reference.sourceName().equals("JLS Java SE 21")
                        && reference.uri().getScheme().equals("https")
                        && reference.uri().getHost().equals("docs.oracle.com")));
    }

    @Test
    void mainClassNameUsesNamingRulesAndAddsJavaSe21OfficialReferences() {
        var step = catalog.getDefinition(
                LessonService.HELLO_PROGRAM_LESSON_ID
        ).getStep("class-name");

        assertEquals("クラスの名前", step.correctCard().text());

        var sections = step.explanationSections();
        assertEquals(java.util.List.of("text", "text", "text", "official-references"),
                sections.stream().map(section -> section.sectionType().value()).toList());
        assertEquals(java.util.List.of(
                        "クラス名　class name",
                        "名前を付けるときのルール",
                        "ポイント",
                        "技術的根拠：Javaの公式仕様・API"),
                sections.stream().map(section -> section.title()).toList());

        assertEquals(java.util.List.of("自分で決めるクラスの名前"),
                sections.get(0).entries().stream().map(entry -> entry.emphasis()).toList());
        assertEquals(java.util.List.of(
                        "",
                        "Main や Test",
                        "public class Main と書いた場合、ファイル名は Main.java"),
                sections.get(1).entries().stream().map(entry -> entry.emphasis()).toList());
        assertEquals(java.util.List.of(
                        "1. 自由に決めてOK：",
                        "2. 大文字で始めるのが慣習：",
                        "3. ファイル名と同じにする："),
                sections.get(1).entries().stream().map(entry -> entry.label()).toList());
        assertEquals(1, sections.get(2).entries().size());
        assertEquals("class", sections.get(2).entries().get(0).emphasis());

        var references = sections.get(3).officialReferences();
        assertEquals(3, references.size());
        assertEquals(java.util.List.of("§8.1", "§7.6", "§6.1"), references.stream()
                .map(reference -> reference.sectionNumber()).toList());
        assertEquals(java.util.List.of(
                        "Class Declarations",
                        "Top Level Class and Interface Declarations",
                        "Declarations"),
                references.stream().map(reference -> reference.sectionTitle()).toList());
        assertTrue(references.stream().allMatch(reference ->
                reference.source().value().equals("jls")
                        && reference.version().equals("Java SE 21")
                        && reference.sourceName().equals("JLS Java SE 21")
                        && reference.uri().getScheme().equals("https")
                        && reference.uri().getHost().equals("docs.oracle.com")));
    }

    @Test
    void staticKeepsExistingFirstSectionsAndAddsJavaSe21OfficialReferences() {
        var step = catalog.getDefinition(
                LessonService.HELLO_PROGRAM_LESSON_ID
        ).getStep("static");

        var sections = step.explanationSections();
        assertEquals(java.util.List.of("text", "table", "text", "official-references"),
                sections.stream().map(section -> section.sectionType().value()).toList());
        assertEquals("static修飾子　static modifier", sections.get(0).title());
        assertEquals("static修飾子の特徴", sections.get(1).title());
        assertEquals("ポイント", sections.get(2).title());
        assertEquals(
                "Javaのプログラムを実行するとき、開始地点となる main メソッドにも、インスタンスを作らずに呼び出せるように static が付いています。",
                sections.get(2).entries().stream()
                        .map(entry -> entry.before() + entry.emphasis() + entry.after())
                        .collect(java.util.stream.Collectors.joining())
        );
        assertEquals(
                "開始地点となる main メソッドにも、インスタンスを作らずに呼び出せるように static",
                sections.get(2).entries().get(0).emphasis()
        );

        var references = sections.get(3).officialReferences();
        assertEquals(2, references.size());
        assertEquals(java.util.List.of("§8.4.3.2", "§12.1.4"), references.stream()
                .map(reference -> reference.sectionNumber()).toList());
        assertEquals(java.util.List.of("static Methods", "Invoke Test.main"), references.stream()
                .map(reference -> reference.sectionTitle()).toList());
        assertTrue(references.stream().allMatch(reference ->
                reference.source().value().equals("jls")
                        && reference.version().equals("Java SE 21")
                        && reference.sourceName().equals("JLS Java SE 21")
                        && reference.uri().getScheme().equals("https")
                        && reference.uri().getHost().equals("docs.oracle.com")));
    }

    @Test
    void voidUsesReturnValueComparisonAndAddsJavaSe21OfficialReferences() {
        var step = catalog.getDefinition(
                LessonService.HELLO_PROGRAM_LESSON_ID
        ).getStep("void");

        assertEquals("戻り値を返さない", step.correctCard().text());

        var sections = step.explanationSections();
        assertEquals(java.util.List.of("text", "table", "text", "official-references"),
                sections.stream().map(section -> section.sectionType().value()).toList());
        assertEquals(java.util.List.of(
                        "戻り値　return value",
                        "戻り値の違い",
                        "ポイント",
                        "技術的根拠：Javaの公式仕様・API"),
                sections.stream().map(section -> section.title()).toList());
        assertEquals("値を返さない", sections.get(0).entries().get(0).emphasis());
        assertEquals(java.util.List.of("void", "int", "String"), sections.get(1).entries().stream()
                .map(entry -> entry.label()).toList());
        assertEquals(java.util.List.of("値を返さない", "int 型の値を返す", "String 型の値を返す"),
                sections.get(1).entries().stream().map(entry -> entry.before()).toList());
        assertEquals(
                "画面に表示するだけの処理など、結果を返す必要がない場合に使います。",
                sections.get(2).entries().get(0).before()
        );

        var references = sections.get(3).officialReferences();
        assertEquals(2, references.size());
        assertEquals(java.util.List.of("§8.4.5", "§12.1.4"), references.stream()
                .map(reference -> reference.sectionNumber()).toList());
        assertEquals(java.util.List.of("Method Result", "Invoke Test.main"), references.stream()
                .map(reference -> reference.sectionTitle()).toList());
        assertTrue(references.stream().allMatch(reference ->
                reference.source().value().equals("jls")
                        && reference.version().equals("Java SE 21")
                        && reference.sourceName().equals("JLS Java SE 21")
                        && reference.uri().getScheme().equals("https")
                        && reference.uri().getHost().equals("docs.oracle.com")));
    }

    @Test
    void mainUsesMethodNameComparisonAndAddsJavaSe21OfficialReference() {
        var step = catalog.getDefinition(
                LessonService.HELLO_PROGRAM_LESSON_ID
        ).getStep("main");

        assertEquals("プログラム開始メソッド", step.correctCard().text());

        var sections = step.explanationSections();
        assertEquals(java.util.List.of("text", "table", "text", "official-references"),
                sections.stream().map(section -> section.sectionType().value()).toList());
        assertEquals(java.util.List.of(
                        "メソッド名　method name",
                        "mainメソッドとMainクラスの違い",
                        "ポイント",
                        "技術的根拠：Javaの公式仕様・API"),
                sections.stream().map(section -> section.title()).toList());
        assertEquals("メソッドの名前", sections.get(0).entries().get(0).emphasis());
        assertEquals(java.util.List.of("main", "Main"), sections.get(1).entries().stream()
                .map(entry -> entry.label()).toList());
        assertEquals(java.util.List.of(
                        "Javaが最初に実行するメソッドの名前",
                        "自分で決めるクラスの名前"),
                sections.get(1).entries().stream().map(entry -> entry.before()).toList());
        assertEquals("main", sections.get(2).entries().get(0).emphasis());
        assertEquals(
                " はJavaのキーワードではありませんが、プログラムの開始に使われる決められたメソッド名です。",
                sections.get(2).entries().get(0).after()
        );

        var references = sections.get(3).officialReferences();
        assertEquals(1, references.size());
        var reference = references.get(0);
        assertEquals("§12.1.4", reference.sectionNumber());
        assertEquals("Invoke Test.main", reference.sectionTitle());
        assertEquals("JLS Java SE 21", reference.sourceName());
        assertEquals("Java SE 21", reference.version());
        assertEquals("https", reference.uri().getScheme());
        assertEquals("docs.oracle.com", reference.uri().getHost());
        assertEquals(
                "https://docs.oracle.com/javase/specs/jls/se21/html/jls-12.html#jls-12.1.4",
                reference.uri().toString()
        );
    }

    @Test
    void stringArrayUsesArrayTypeExplanationAndAddsJavaSe21OfficialReferences() {
        var step = catalog.getDefinition(
                LessonService.HELLO_PROGRAM_LESSON_ID
        ).getStep("string-array");

        assertEquals("文字列の配列", step.correctCard().text());

        var sections = step.explanationSections();
        assertEquals(java.util.List.of("text", "table", "text", "official-references"),
                sections.stream().map(section -> section.sectionType().value()).toList());
        assertEquals(java.util.List.of(
                        "配列型　array type",
                        "String[]の意味",
                        "ポイント",
                        "技術的根拠：Javaの公式仕様・API"),
                sections.stream().map(section -> section.title()).toList());
        assertEquals(
                "文字列（String）を複数まとめて扱うための型です。",
                sections.get(0).entries().get(0).before()
        );
        assertEquals(java.util.List.of("String", "[]"), sections.get(1).entries().stream()
                .map(entry -> entry.label()).toList());
        assertEquals(java.util.List.of(
                        "文字列を表す型です。",
                        "複数の値をまとめて扱う配列であることを表します。"),
                sections.get(1).entries().stream().map(entry -> entry.before()).toList());
        assertEquals(
                "プログラムの実行時に、外から渡された文字列を受け取るために使います。",
                sections.get(2).entries().get(0).before()
        );

        var references = sections.get(3).officialReferences();
        assertEquals(2, references.size());
        assertEquals(java.util.List.of("§10.1", "§12.1.4"), references.stream()
                .map(reference -> reference.sectionNumber()).toList());
        assertEquals(java.util.List.of("Array Types", "Invoke Test.main"), references.stream()
                .map(reference -> reference.sectionTitle()).toList());
        assertTrue(references.stream().allMatch(reference ->
                reference.source().value().equals("jls")
                        && reference.version().equals("Java SE 21")
                        && reference.sourceName().equals("JLS Java SE 21")
                        && reference.uri().getScheme().equals("https")
                        && reference.uri().getHost().equals("docs.oracle.com")));
    }

    @Test
    void argsUsesArgumentNameComparisonAndAddsJavaSe21OfficialReference() {
        var step = catalog.getDefinition(
                LessonService.HELLO_PROGRAM_LESSON_ID
        ).getStep("args");

        assertEquals("変数の名前", step.correctCard().text());

        var sections = step.explanationSections();
        assertEquals(java.util.List.of("text", "table", "text", "official-references"),
                sections.stream().map(section -> section.sectionType().value()).toList());
        assertEquals(java.util.List.of(
                        "引数名　argument name",
                        "String[] と args の関係",
                        "ポイント",
                        "技術的根拠：Javaの公式仕様・API"),
                sections.stream().map(section -> section.title()).toList());
        assertEquals("変数の名前", sections.get(0).entries().get(0).emphasis());
        assertEquals(java.util.List.of("String[]", "args"), sections.get(1).entries().stream()
                .map(entry -> entry.label()).toList());
        assertEquals(java.util.List.of("受け取る値の型です", "受け取る変数の名前です"),
                sections.get(1).entries().stream().map(entry -> entry.before()).toList());
        assertEquals(java.util.List.of("args", "args", ""),
                sections.get(2).entries().stream().map(entry -> entry.emphasis()).toList());

        var references = sections.get(3).officialReferences();
        assertEquals(1, references.size());
        var reference = references.get(0);
        assertEquals("§12.1.4", reference.sectionNumber());
        assertEquals("Invoke Test.main", reference.sectionTitle());
        assertEquals("JLS Java SE 21", reference.sourceName());
        assertEquals("Java SE 21", reference.version());
        assertEquals("https", reference.uri().getScheme());
        assertEquals("docs.oracle.com", reference.uri().getHost());
    }

    @Test
    void bothOpeningBracesShareSeparatorExplanationAndOfficialReferences() {
        var definition = catalog.getDefinition(LessonService.HELLO_PROGRAM_LESSON_ID);
        var classOpen = definition.getStep("class-open");
        var mainOpen = definition.getStep("main-open");

        assertEquals("ここから始まる", classOpen.correctCard().text());
        assertEquals("ここから始まる", mainOpen.correctCard().text());
        assertEquals(classOpen.explanationSections(), mainOpen.explanationSections());

        var sections = classOpen.explanationSections();
        assertEquals(java.util.List.of("text", "table", "text", "official-references"),
                sections.stream().map(section -> section.sectionType().value()).toList());
        assertEquals(java.util.List.of(
                        "セパレータ（左波括弧）　separator",
                        "波括弧のルール",
                        "ポイント",
                        "技術的根拠：Javaの公式仕様・API"),
                sections.stream().map(section -> section.title()).toList());
        assertEquals("中身が、ここから始まる", sections.get(0).entries().get(0).emphasis());
        assertEquals("{ と } はセット", sections.get(1).entries().get(0).label());
        assertEquals("{ で始まった範囲は、対応する } で終わります。",
                sections.get(1).entries().get(0).before());
        assertEquals("閉じ忘れがあると、Javaはプログラムの構造を正しく理解できずエラーになります。",
                sections.get(2).entries().get(0).before());

        var references = sections.get(3).officialReferences();
        assertEquals(3, references.size());
        assertEquals(java.util.List.of("§3.11", "§8.1.7", "§14.2"), references.stream()
                .map(reference -> reference.sectionNumber()).toList());
        assertEquals(java.util.List.of(
                        "Separators",
                        "Class Body and Member Declarations",
                        "Blocks"),
                references.stream().map(reference -> reference.sectionTitle()).toList());
        assertTrue(references.stream().allMatch(reference ->
                reference.source().value().equals("jls")
                        && reference.version().equals("Java SE 21")
                        && reference.uri().getScheme().equals("https")
                        && reference.uri().getHost().equals("docs.oracle.com")));
    }

    @Test
    void 補足を担う第3Sectionの見出しをポイントに統一する() {
        var definition = catalog.getDefinition(LessonService.HELLO_PROGRAM_LESSON_ID);
        var targetStepIds = java.util.List.of(
                "class-public", "class-keyword", "class-name", "class-open",
                "main-public", "static", "void", "main", "string-array", "args", "main-open",
                "main-close", "class-close"
        );

        for (String stepId : targetStepIds) {
            var sections = definition.getStep(stepId).explanationSections();
            assertTrue(sections.size() >= 3, stepId);
            assertEquals("ポイント", sections.get(2).title(), stepId);
        }

        assertEquals(
                "技術的根拠：Javaの公式仕様・API",
                definition.getStep("class-public").explanationSections().get(3).title()
        );
        assertEquals(
                "技術的根拠：Javaの公式仕様・API",
                definition.getStep("class-keyword").explanationSections().get(3).title()
        );
        assertEquals(
                "println と print の違い",
                definition.getStep("print-command").explanationSections().get(2).title()
        );
        assertEquals(
                "println との関係",
                definition.getStep("hello-string").explanationSections().get(2).title()
        );
        assertEquals(2, definition.getStep("semicolon").explanationSections().size());
    }

    @Test
    void 未登録教材を明確に判定できる() {
        assertFalse(catalog.supports("stage-2-reading"));
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> catalog.getDefinition("stage-2-reading")
        );
        assertTrue(exception.getMessage().contains("stage-2-reading"));
    }

    @Test
    void 教材定義に種類別Helperが揃い文字列layoutを受け取らない() {
        Set<String> helperNames = Arrays.stream(
                        CodeReadingLessonCatalog.class.getDeclaredMethods()
                )
                .map(method -> method.getName())
                .collect(Collectors.toSet());

        assertTrue(helperNames.containsAll(Set.of(
                "textSection", "tableSection", "diagramSection",
                "examplesSection", "qaSection", "comparisonSection", "listSection",
                "officialReferencesSection", "jlsReference",
                "text", "tableRow", "highlightedTableRow", "diagramRow",
                "example", "note", "qaEntry", "comparisonEntry", "listItem"
        )));
        assertTrue(Arrays.stream(CodeReadingLessonCatalog.class.getDeclaredMethods())
                .filter(method -> method.getName().equals("section"))
                .noneMatch(method -> method.getParameterTypes()[0].equals(String.class)));
    }
}
