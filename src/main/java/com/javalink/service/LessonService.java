package com.javalink.service;

import com.javalink.model.Lesson;
import com.javalink.model.LessonStep;
import com.javalink.model.QuizQuestion;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 教材と学習ステップの取得を担当します。
 */
@Service
public class LessonService {

    private static final String MAIN_METHOD_LESSON_ID = "main-method-basic";
    public static final String HELLO_PROGRAM_LESSON_ID =
            "hello-program-reading";

    private final Map<String, Lesson> lessons;

    /**
     * QuizServiceが持つ既存問題を使って最初の教材を作ります。
     *
     * @param quizService 既存問題を管理するService
     */
    public LessonService(QuizService quizService) {
        Lesson mainMethodLesson = new Lesson(
                MAIN_METHOD_LESSON_ID,
                "mainメソッドを完成させよう",
                "mainメソッドを構成するキーワードを順番に学習します。",
                "public static void main(String[] args){\n\n}",
                List.of(
                        createStep(quizService, "public", 1, "public"),
                        createStep(quizService, "static", 2, "static"),
                        createStep(quizService, "void", 3, "void"),
                        createStep(quizService, "main", 4, "main"),
                        createStep(quizService, "string-array", 5, "String[]"),
                        createStep(quizService, "args", 6, "args")
                )
        );

        Lesson helloProgramLesson = new Lesson(
                HELLO_PROGRAM_LESSON_ID,
                "「Hello」と表示するプログラム",
                "完成プログラムを3つのステージに分け、コードを左から読みます。",
                """
                        public class Main {
                            public static void main(String[] args) {
                                System.out.println("Hello");
                            }
                        }
                        """,
                List.of(
                        createStep(
                                quizService, "class-public", "public",
                                1, "public"
                        ),
                        createStep(
                                quizService, "class-keyword", "class-keyword",
                                2, "class"
                        ),
                        createStep(
                                quizService, "class-name", "class-name",
                                3, "Main"
                        ),
                        createStep(
                                quizService, "class-open", "open-brace",
                                4, "{"
                        ),
                        createStep(
                                quizService, "main-public", "public",
                                5, "public"
                        ),
                        createStep(
                                quizService, "static", "static",
                                6, "static"
                        ),
                        createStep(
                                quizService, "void", "void",
                                7, "void"
                        ),
                        createStep(
                                quizService, "main", "main",
                                8, "main"
                        ),
                        createStep(
                                quizService, "string-array", "string-array",
                                9, "String[]"
                        ),
                        createStep(
                                quizService, "args", "args",
                                10, "args"
                        ),
                        createStep(
                                quizService, "main-open", "open-brace",
                                11, "{"
                        ),
                        createStep(
                                quizService, "system", "system",
                                12, "System"
                        ),
                        createStep(
                                quizService, "system-dot", "system-dot",
                                13, "."
                        ),
                        createStep(
                                quizService, "out", "out",
                                14, "out"
                        ),
                        createStep(
                                quizService, "out-dot", "out-dot",
                                15, "."
                        ),
                        createStep(
                                quizService, "println", "println",
                                16, "println"
                        ),
                        createStep(
                                quizService, "hello-string", "hello-string",
                                17, "\"Hello\""
                        ),
                        createStep(
                                quizService, "semicolon", "semicolon",
                                18, ";"
                        )
                )
        );

        lessons = Map.of(
                mainMethodLesson.id(), mainMethodLesson,
                helloProgramLesson.id(), helloProgramLesson
        );
    }

    /**
     * IDに対応する教材を返します。
     *
     * @param lessonId 教材ID
     * @return IDに対応する教材
     * @throws IllegalArgumentException 教材が見つからない場合
     */
    public Lesson getLesson(String lessonId) {
        Lesson lesson = lessons.get(lessonId);
        if (lesson == null) {
            throw new IllegalArgumentException(
                    "教材が見つかりません。lessonId: " + lessonId
            );
        }
        return lesson;
    }

    /**
     * 教材の最初のステップを返します。
     *
     * @param lessonId 教材ID
     * @return 最初の学習ステップ
     * @throws IllegalStateException 教材にステップがない場合
     */
    public LessonStep getFirstStep(String lessonId) {
        Lesson lesson = getLesson(lessonId);
        if (lesson.steps().isEmpty()) {
            throw new IllegalStateException(
                    "教材に学習ステップがありません。lessonId: " + lessonId
            );
        }
        return lesson.steps().get(0);
    }

    /**
     * 教材内のIDに対応するステップを返します。
     *
     * @param lessonId 教材ID
     * @param stepId   ステップID
     * @return IDに対応する学習ステップ
     * @throws IllegalArgumentException ステップが見つからない場合
     */
    public LessonStep getStep(String lessonId, String stepId) {
        return getLesson(lessonId).steps().stream()
                .filter(step -> step.id().equals(stepId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "学習ステップが見つかりません。lessonId: "
                                + lessonId + ", stepId: " + stepId
                ));
    }

    /**
     * 現在のステップの次にあるステップを返します。
     *
     * @param lessonId 教材ID
     * @param stepId   現在のステップID
     * @return 次のステップ。最後のステップの場合は空
     */
    public Optional<LessonStep> getNextStep(String lessonId, String stepId) {
        Lesson lesson = getLesson(lessonId);
        LessonStep currentStep = getStep(lessonId, stepId);
        int nextIndex = lesson.steps().indexOf(currentStep) + 1;

        if (nextIndex >= lesson.steps().size()) {
            return Optional.empty();
        }
        return Optional.of(lesson.steps().get(nextIndex));
    }

    /**
     * 既存のQuizQuestionを参照するLessonStepを作ります。
     */
    private LessonStep createStep(
            QuizService quizService,
            String questionId,
            int order,
            String displayLabel
    ) {
        QuizQuestion question = quizService.getQuestion(questionId);

        /*
         * QuizServiceは未知のIDで最初の問題を返す既存仕様のため、
         * 教材定義の誤りを見逃さないようIDの一致を確認します。
         */
        if (!question.id().equals(questionId)) {
            throw new IllegalStateException(
                    "教材で使用する問題が見つかりません。questionId: " + questionId
            );
        }

        return new LessonStep(
                question.id(),
                order,
                displayLabel,
                question.codeBefore(),
                question.targetCode(),
                question.codeAfter(),
                question,
                true
        );
    }

    /**
     * 同じ問題データを別ステージの一意なstepIdで再利用します。
     */
    private LessonStep createStep(
            QuizService quizService,
            String stepId,
            String questionId,
            int order,
            String displayLabel
    ) {
        QuizQuestion question = quizService.getQuestion(questionId);
        if (!question.id().equals(questionId)) {
            throw new IllegalStateException(
                    "教材で使用する問題が見つかりません。questionId: "
                            + questionId
            );
        }
        return new LessonStep(
                stepId,
                order,
                displayLabel,
                question.codeBefore(),
                question.targetCode(),
                question.codeAfter(),
                question,
                true
        );
    }
}
