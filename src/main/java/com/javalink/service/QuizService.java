package com.javalink.service;

import com.javalink.model.QuizOption;
import com.javalink.model.QuizQuestion;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/** 旧main-method-basic教材の問題を管理します。コードリーディング教材はCatalog側で管理します。 */
@Service
public class QuizService {

    private final List<QuizQuestion> questions = List.of(
            new QuizQuestion(
                    "public", "", "public", " static void main(String[] args)",
                    "public の意味はどれですか？",
                    List.of(
                            new QuizOption("accessible", "外から使える"),
                            new QuizOption("no-return", "戻り値を返さない"),
                            new QuizOption("create-instance", "インスタンスを作る"),
                            new QuizOption("repeat", "繰り返す")
                    ), "accessible"
            ),
            new QuizQuestion(
                    "static", "public ", "static", " void main(String[] args)",
                    "static の意味はどれですか？",
                    List.of(
                            new QuizOption("without-instance", "インスタンスを作らなくても使える"),
                            new QuizOption("accessible", "外から使える"),
                            new QuizOption("no-return", "戻り値を返さない"),
                            new QuizOption("repeat", "繰り返す")
                    ), "without-instance"
            ),
            new QuizQuestion(
                    "void", "public static ", "void", " main(String[] args)",
                    "void の意味はどれですか？",
                    List.of(
                            new QuizOption("no-return", "戻り値を返さない"),
                            new QuizOption("accessible", "外から使える"),
                            new QuizOption("string-array", "文字列の配列"),
                            new QuizOption("repeat", "プログラムを繰り返す")
                    ), "no-return"
            ),
            new QuizQuestion(
                    "main", "public static void ", "main", "(String[] args)",
                    "main の意味",
                    List.of(
                            new QuizOption("program-entry", "プログラム開始メソッド"),
                            new QuizOption("create-instance", "クラスから実物を作る"),
                            new QuizOption("display-string", "文字列を表示する"),
                            new QuizOption("end-process", "処理を終了する")
                    ), "program-entry"
            ),
            new QuizQuestion(
                    "string-array", "public static void main(", "String[]", " args)",
                    "String[] の意味",
                    List.of(
                            new QuizOption("multiple-strings", "文字列の配列"),
                            new QuizOption("one-integer", "整数を1つ入れる変数"),
                            new QuizOption("no-return", "戻り値を返さない指定"),
                            new QuizOption("inherit-class", "クラスを継承する指定")
                    ), "multiple-strings"
            ),
            new QuizQuestion(
                    "args", "public static void main(String[] ", "args", ")",
                    "args の意味",
                    List.of(
                            new QuizOption("argument-variable", "受け取った値の名前"),
                            new QuizOption("string-type", "文字列のデータ型"),
                            new QuizOption("program-entry", "プログラムの開始地点"),
                            new QuizOption("create-instance", "インスタンスを作る命令")
                    ), "argument-variable"
            )
    );

    public QuizQuestion getFirstQuestion() {
        return questions.get(0);
    }

    /** 未知のIDでは既存互換のため最初の問題を返します。 */
    public QuizQuestion getQuestion(String questionId) {
        return questions.stream()
                .filter(question -> question.id().equals(questionId))
                .findFirst()
                .orElseGet(this::getFirstQuestion);
    }

    public Optional<QuizQuestion> getNextQuestion(QuizQuestion question) {
        int currentIndex = questions.indexOf(question);
        int nextIndex = currentIndex + 1;
        if (currentIndex < 0 || nextIndex >= questions.size()) {
            return Optional.empty();
        }
        return Optional.of(questions.get(nextIndex));
    }

    public int getQuestionNumber(QuizQuestion question) {
        return questions.indexOf(question) + 1;
    }

    public int getQuestionCount() {
        return questions.size();
    }

    public boolean isCorrect(QuizQuestion question, String optionId) {
        return question.correctOptionId().equals(optionId);
    }

    public QuizOption getCorrectOption(QuizQuestion question) {
        return question.options().stream()
                .filter(option -> question.correctOptionId().equals(option.id()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "正解選択肢が見つかりません。questionId: " + question.id()
                ));
    }
}
