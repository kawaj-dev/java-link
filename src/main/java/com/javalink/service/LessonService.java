package com.javalink.service;

import com.javalink.model.Lesson;
import com.javalink.model.LessonStep;
import com.javalink.model.QuizQuestion;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 教材と学習ステップの取得を担当します。
 */
@Service
public class LessonService {

    private static final String MAIN_METHOD_LESSON_ID = "main-method-basic";
    public static final String HELLO_PROGRAM_LESSON_ID =
            CodeReadingLessonCatalog.STAGE1_LESSON_ID;

    private final Map<String, Lesson> lessons;

    /**
     * QuizServiceが持つ既存問題を使って最初の教材を作ります。
     *
     * @param quizService 既存問題を管理するService
     */
    public LessonService(
            QuizService quizService,
            CodeReadingLessonCatalog lessonCatalog
    ) {
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

        Map<String, Lesson> registeredLessons = new HashMap<>();
        registeredLessons.put(mainMethodLesson.id(), mainMethodLesson);
        lessonCatalog.getLessons().forEach(lesson ->
                registeredLessons.put(lesson.id(), lesson)
        );
        lessons = Map.copyOf(registeredLessons);
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

}
