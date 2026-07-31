package com.javalink.service;

import com.javalink.model.QuizOption;
import com.javalink.model.QuizQuestion;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 問題の用意と回答の判定を担当します。
 */
@Service
public class QuizService {

    /*
     * 問題は一覧で管理します。
     * 将来はここへQuizQuestionを追加することで問題数を増やせます。
     */
    private final List<QuizQuestion> questions = List.of(
            new QuizQuestion(
                    "public",
                    "",
                    "public",
                    " static void main(String[] args)",
                    "public の意味はどれですか？",
                    List.of(
                            new QuizOption("accessible", "外から使える"),
                            new QuizOption("no-return", "戻り値を返さない"),
                            new QuizOption("create-instance", "インスタンスを作る"),
                            new QuizOption("repeat", "繰り返す")
                    ),
                    "accessible"
            ),
            new QuizQuestion(
                    "static",
                    "public ",
                    "static",
                    " void main(String[] args)",
                    "static の意味はどれですか？",
                    List.of(
                            new QuizOption("without-instance", "インスタンスを作らなくても使える"),
                            new QuizOption("accessible", "外から使える"),
                            new QuizOption("no-return", "戻り値を返さない"),
                            new QuizOption("repeat", "繰り返す")
                    ),
                    "without-instance"
            ),
            new QuizQuestion(
                    "void",
                    "public static ",
                    "void",
                    " main(String[] args)",
                    "void の意味はどれですか？",
                    List.of(
                            new QuizOption("no-return", "戻り値を返さない"),
                            new QuizOption("accessible", "外から使える"),
                            new QuizOption("string-array", "文字列の配列"),
                            new QuizOption("repeat", "プログラムを繰り返す")
                    ),
                    "no-return"
            ),
            new QuizQuestion(
                    "main",
                    "public static void ",
                    "main",
                    "(String[] args)",
                    "main の意味",
                    List.of(
                            new QuizOption("program-entry", "プログラム開始メソッド"),
                            new QuizOption("create-instance", "クラスから実物を作る"),
                            new QuizOption("display-string", "文字列を表示する"),
                            new QuizOption("end-process", "処理を終了する")
                    ),
                    "program-entry"
            ),
            new QuizQuestion(
                    "string-array",
                    "public static void main(",
                    "String[]",
                    " args)",
                    "String[] の意味",
                    List.of(
                            new QuizOption("multiple-strings", "文字列の配列"),
                            new QuizOption("one-integer", "整数を1つ入れる変数"),
                            new QuizOption("no-return", "戻り値を返さない指定"),
                            new QuizOption("inherit-class", "クラスを継承する指定")
                    ),
                    "multiple-strings"
            ),
            new QuizQuestion(
                    "args",
                    "public static void main(String[] ",
                    "args",
                    ")",
                    "args の意味",
                    List.of(
                            new QuizOption("argument-variable", "受け取った値の名前"),
                            new QuizOption("string-type", "文字列のデータ型"),
                            new QuizOption("program-entry", "プログラムの開始地点"),
                            new QuizOption("create-instance", "インスタンスを作る命令")
                    ),
                    "argument-variable"
            ),
            new QuizQuestion(
                    "class-keyword",
                    "public ",
                    "class",
                    " Main {",
                    "class の意味",
                    List.of(
                            new QuizOption("declare-class", "クラスを作る"),
                            new QuizOption("program-entry", "プログラム開始メソッド")
                    ),
                    "declare-class"
            ),
            new QuizQuestion(
                    "class-name",
                    "public class ",
                    "Main",
                    " {",
                    "Main の意味",
                    List.of(
                            new QuizOption("main-class-name", "クラスの名前"),
                            new QuizOption("argument-variable", "受け取った値の名前")
                    ),
                    "main-class-name"
            ),
            new QuizQuestion(
                    "open-brace",
                    "",
                    "{",
                    "",
                    "{ の意味",
                    List.of(
                            new QuizOption("block-start", "ここから始まる"),
                            new QuizOption("command-end", "命令の終わり")
                    ),
                    "block-start"
            ),
            new QuizQuestion(
                    "system",
                    "",
                    "System",
                    ".out.println(\"Hello\");",
                    "System の意味",
                    List.of(
                            new QuizOption("java-system", "Javaの基本機能"),
                            new QuizOption("display-text", "表示する文字")
                    ),
                    "java-system"
            ),
            new QuizQuestion(
                    "system-dot",
                    "System",
                    ".",
                    "out.println(\"Hello\");",
                    ". の意味",
                    List.of(
                            new QuizOption("system-member-access", "中にあるものを使う"),
                            new QuizOption("command-end", "命令の終わり")
                    ),
                    "system-member-access"
            ),
            new QuizQuestion(
                    "out",
                    "System.",
                    "out",
                    ".println(\"Hello\");",
                    "out の意味",
                    List.of(
                            new QuizOption("standard-output", "文字を出力するためのもの"),
                            new QuizOption("java-system", "Javaの基本機能")
                    ),
                    "standard-output"
            ),
            new QuizQuestion(
                    "out-dot",
                    "System.out",
                    ".",
                    "println(\"Hello\");",
                    ". の意味",
                    List.of(
                            new QuizOption("out-member-access", "中にあるものを使う"),
                            new QuizOption("display-line", "表示して改行する")
                    ),
                    "out-member-access"
            ),
            new QuizQuestion(
                    "println",
                    "System.out.",
                    "println",
                    "(\"Hello\");",
                    "println の意味",
                    List.of(
                            new QuizOption("display-line", "表示して改行する"),
                            new QuizOption("standard-output", "文字を出力するためのもの")
                    ),
                    "display-line"
            ),
            new QuizQuestion(
                    "hello-string",
                    "System.out.println(",
                    "\"Hello\"",
                    ");",
                    "\"Hello\" の意味",
                    List.of(
                            new QuizOption("display-text", "表示する文字"),
                            new QuizOption("main-class-name", "クラスの名前")
                    ),
                    "display-text"
            ),
            new QuizQuestion(
                    "semicolon",
                    "System.out.println(\"Hello\")",
                    ";",
                    "",
                    "; の意味",
                    List.of(
                            new QuizOption("command-end", "命令の終わり"),
                            new QuizOption("block-start", "ここから始まる")
                    ),
                    "command-end"
            ),
            new QuizQuestion(
                    "print-command",
                    "",
                    "System.out.println",
                    "(\"Hello\");",
                    "System.out.println の意味",
                    List.of(
                            new QuizOption(
                                    "display-and-newline",
                                    "画面に表示して改行する"
                            ),
                            new QuizOption("declare-class", "クラスを作る"),
                            new QuizOption("program-entry", "プログラムを始める"),
                            new QuizOption("receive-values", "値を受け取る")
                    ),
                    "display-and-newline"
            ),
            new QuizQuestion(
                    "main-close",
                    "",
                    "}",
                    "",
                    "最初の } の意味",
                    List.of(
                            new QuizOption("close-main", "mainメソッド終了"),
                            new QuizOption("close-class", "Mainクラス終了"),
                            new QuizOption("block-start", "ここから始まる"),
                            new QuizOption("command-end", "命令の終わり")
                    ),
                    "close-main"
            ),
            new QuizQuestion(
                    "class-close",
                    "",
                    "}",
                    "",
                    "最後の } の意味",
                    List.of(
                            new QuizOption("close-class", "Mainクラス終了"),
                            new QuizOption("close-main", "mainメソッド終了"),
                            new QuizOption("block-start", "ここから始まる"),
                            new QuizOption("command-end", "命令の終わり")
                    ),
                    "close-class"
            )
    );

    /**
     * 最初に学習する問題を返します。
     *
     * @return 最初の問題
     */
    public QuizQuestion getFirstQuestion() {
        // Java 17でも使えるget(0)で一覧の先頭を取り出します。
        return questions.get(0);
    }

    /**
     * IDに対応する問題を返します。
     *
     * @param questionId 問題を区別するID
     * @return IDに対応する問題。見つからない場合は最初の問題
     */
    public QuizQuestion getQuestion(String questionId) {
        return questions.stream()
                .filter(question -> question.id().equals(questionId))
                .findFirst()
                .orElseGet(this::getFirstQuestion);
    }

    /**
     * 現在の問題の次にある問題を返します。
     *
     * @param question 現在表示している問題
     * @return 次の問題。最後の問題なら空
     */
    public Optional<QuizQuestion> getNextQuestion(QuizQuestion question) {
        int currentIndex = questions.indexOf(question);
        int nextIndex = currentIndex + 1;

        if (currentIndex < 0 || nextIndex >= questions.size()) {
            return Optional.empty();
        }
        return Optional.of(questions.get(nextIndex));
    }

    /**
     * 問題が全体の何問目かを返します。
     *
     * @param question 対象の問題
     * @return 1から始まる問題番号
     */
    public int getQuestionNumber(QuizQuestion question) {
        return questions.indexOf(question) + 1;
    }

    /**
     * 全問題数を返します。
     *
     * @return 登録されている問題数
     */
    public int getQuestionCount() {
        return questions.size();
    }

    /**
     * 選んだ回答が正解か判定します。
     *
     * @param question 問題
     * @param optionId 選んだ選択肢のID
     * @return 正解ならtrue、不正解ならfalse
     */
    public boolean isCorrect(QuizQuestion question, String optionId) {
        return question.correctOptionId().equals(optionId);
    }

    /**
     * 問題に登録されている正解選択肢を返します。
     *
     * @param question 対象の問題
     * @return 正解として登録されている選択肢
     */
    public QuizOption getCorrectOption(QuizQuestion question) {
        return question.options().stream()
                .filter(option ->
                        question.correctOptionId().equals(option.id())
                )
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "正解選択肢が見つかりません。questionId: "
                                + question.id()
                ));
    }
}
