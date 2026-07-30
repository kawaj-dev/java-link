package com.javalink.controller;

import com.javalink.model.LessonViewModel;
import com.javalink.model.ProgramRunResult;
import com.javalink.model.QuizOption;
import com.javalink.model.QuizQuestion;
import com.javalink.service.CodeReadingService;
import com.javalink.service.CodeReadingCourseService;
import com.javalink.service.LessonEngine;
import com.javalink.service.LessonProgressService;
import com.javalink.service.LessonRunService;
import com.javalink.service.LessonViewModelService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * クイズ画面の表示と回答の受け取りを担当します。
 */
@Controller
public class QuizController {

    public static final String LESSON_VIEW_MODEL_ATTRIBUTE = "lessonViewModel";
    public static final String PROGRAM_RUN_RESULT_ATTRIBUTE = "programRunResult";
    public static final String SHUFFLED_OPTIONS_ATTRIBUTE = "shuffledOptions";
    public static final String READING_ITEMS_ATTRIBUTE = "readingItems";

    private static final String MAIN_METHOD_LESSON_ID =
            com.javalink.service.LessonService.HELLO_PROGRAM_LESSON_ID;

    private final LessonEngine lessonEngine;
    private final CodeReadingCourseService codeReadingCourseService;
    private final CodeReadingService codeReadingService;
    private final LessonProgressService lessonProgressService;
    private final LessonRunService lessonRunService;
    private final LessonViewModelService lessonViewModelService;

    /**
     * Spring Bootから教材処理を行うServiceを受け取ります。
     *
     * @param lessonEngine           回答判定と進捗更新を行うクラス
     * @param lessonViewModelService 教材画面用ViewModelを作るクラス
     */
    public QuizController(
            LessonEngine lessonEngine,
            CodeReadingCourseService codeReadingCourseService,
            CodeReadingService codeReadingService,
            LessonRunService lessonRunService,
            LessonProgressService lessonProgressService,
            LessonViewModelService lessonViewModelService
    ) {
        this.lessonEngine = lessonEngine;
        this.codeReadingCourseService = codeReadingCourseService;
        this.codeReadingService = codeReadingService;
        this.lessonRunService = lessonRunService;
        this.lessonProgressService = lessonProgressService;
        this.lessonViewModelService = lessonViewModelService;
    }

    /**
     * ブラウザで「/quiz」を開いたときに呼ばれます。
     *
     * @param model   HTMLへ渡すデータを入れる箱
     * @param session 教材の学習進捗を保持するHTTPセッション
     * @return 表示するHTMLファイルの名前
     */
    @GetMapping("/quiz")
    public String showQuiz(
            Model model,
            HttpSession session
    ) {
        LessonViewModel lessonViewModel =
                lessonViewModelService.createViewModel(
                        session,
                        MAIN_METHOD_LESSON_ID
                );
        addLessonViewModelToModel(lessonViewModel, model, session);
        return "quiz";
    }

    /**
     * 完了した教材の安全な疑似実行結果を表示します。
     */
    @PostMapping("/quiz/run")
    public String runLesson(
            Model model,
            HttpSession session
    ) {
        ProgramRunResult runResult =
                lessonRunService.runLesson(
                        session,
                        MAIN_METHOD_LESSON_ID
                );
        LessonViewModel lessonViewModel =
                lessonViewModelService.createViewModel(
                        session,
                        MAIN_METHOD_LESSON_ID
                );

        addLessonViewModelToModel(lessonViewModel, model, session);
        model.addAttribute(PROGRAM_RUN_RESULT_ATTRIBUTE, runResult);
        return "quiz";
    }

    /**
     * 選択肢がクリックされたときに回答を判定します。
     *
     * @param selectedOption 選択された回答のID
     * @param model          HTMLへ渡すデータを入れる箱
     * @param session        教材の学習進捗を保持するHTTPセッション
     * @return 表示するHTMLファイルの名前
     */
    @PostMapping("/quiz/answer")
    public String checkAnswer(
            @RequestParam("selectedOption") String selectedOption,
            Model model,
            HttpSession session
    ) {
        codeReadingCourseService.answerCurrentItem(
                        session,
                        MAIN_METHOD_LESSON_ID,
                        selectedOption
                );
        LessonViewModel lessonViewModel =
                lessonViewModelService.createViewModel(
                        session,
                        MAIN_METHOD_LESSON_ID
                );

        addLessonViewModelToModel(lessonViewModel, model, session);
        return "quiz";
    }

    /**
     * 正解内容を確認した利用者を次の問題へ進めます。
     */
    @PostMapping("/quiz/next")
    public String moveToNextQuestion(
            Model model,
            HttpSession session
    ) {
        codeReadingCourseService.moveToNextStage(
                session,
                MAIN_METHOD_LESSON_ID
        );
        LessonViewModel lessonViewModel =
                lessonViewModelService.createViewModel(
                        session,
                        MAIN_METHOD_LESSON_ID
                );
        addLessonViewModelToModel(lessonViewModel, model, session);
        return "quiz";
    }

    @PostMapping("/quiz/stage/next")
    public String moveToNextStage(
            Model model,
            HttpSession session
    ) {
        codeReadingCourseService.moveToNextStage(
                session,
                MAIN_METHOD_LESSON_ID
        );
        LessonViewModel lessonViewModel =
                lessonViewModelService.createViewModel(
                        session,
                        MAIN_METHOD_LESSON_ID
                );
        addLessonViewModelToModel(lessonViewModel, model, session);
        return "quiz";
    }

    @PostMapping("/quiz/previous")
    public String moveToPreviousQuestion(
            Model model,
            HttpSession session
    ) {
        lessonEngine.moveToPreviousStep(session, MAIN_METHOD_LESSON_ID);
        LessonViewModel lessonViewModel =
                lessonViewModelService.createViewModel(
                        session,
                        MAIN_METHOD_LESSON_ID
                );
        addLessonViewModelToModel(lessonViewModel, model, session);
        return "quiz";
    }

    @PostMapping("/quiz/reset")
    public String resetLesson(
            Model model,
            HttpSession session
    ) {
        lessonProgressService.resetProgress(
                session,
                MAIN_METHOD_LESSON_ID
        );
        LessonViewModel lessonViewModel =
                lessonViewModelService.createViewModel(
                        session,
                        MAIN_METHOD_LESSON_ID
                );
        addLessonViewModelToModel(lessonViewModel, model, session);
        return "quiz";
    }

    /**
     * LessonViewModelと既存画面が参照する互換属性をModelへ入れます。
     *
     * @param lessonViewModel 最新の教材画面情報
     * @param model           HTMLへ渡すデータを入れる箱
     */
    private void addLessonViewModelToModel(
            LessonViewModel lessonViewModel,
            Model model,
            HttpSession session
    ) {
        QuizQuestion question = lessonViewModel.currentStep().question();
        List<QuizOption> shuffledOptions =
                codeReadingService.createSelectionOptions(
                        MAIN_METHOD_LESSON_ID,
                        lessonViewModel.progress()
                );
        Set<String> selectionOptionIds = shuffledOptions.stream()
                .map(QuizOption::id)
                .collect(Collectors.toSet());
        LessonEngine.NavigationState navigation =
                lessonEngine.getNavigationState(
                        session,
                        MAIN_METHOD_LESSON_ID
                );
        var currentStage = codeReadingService.getStageForStep(
                lessonViewModel.currentStep().id()
        );
        var stageItems = codeReadingService.createCurrentStageItems(
                MAIN_METHOD_LESSON_ID,
                lessonViewModel.progress()
        );
        boolean stageComplete = codeReadingService.isStageCompleted(
                currentStage,
                lessonViewModel.progress()
        );

        model.addAttribute(
                LESSON_VIEW_MODEL_ATTRIBUTE,
                lessonViewModel
        );
        model.addAttribute("question", question);
        model.addAttribute(
                SHUFFLED_OPTIONS_ATTRIBUTE,
                shuffledOptions
        );
        model.addAttribute("selectionOptionIds", selectionOptionIds);
        model.addAttribute(
                READING_ITEMS_ATTRIBUTE,
                stageItems
        );
        model.addAttribute(
                "allReadingItems",
                codeReadingService.createItems(
                        MAIN_METHOD_LESSON_ID,
                        lessonViewModel.progress()
                )
        );
        model.addAttribute("currentStage", currentStage);
        model.addAttribute(
                "stageStates",
                codeReadingService.createStageStates(
                        lessonViewModel.progress()
                )
        );
        model.addAttribute("stageComplete", stageComplete);
        model.addAttribute(
                "hasNextStage",
                stageComplete
                        && !codeReadingService.isLastStage(currentStage)
        );
        model.addAttribute(
                "stageCount",
                codeReadingService.getStages().size()
        );
        model.addAttribute(
                "questionNumber",
                lessonViewModel.currentStep().order()
        );
        model.addAttribute("questionCount", lessonViewModel.totalCount());
        model.addAttribute(
                "answered",
                lessonViewModel.progress().isAnswered()
        );
        model.addAttribute(
                "correct",
                lessonViewModel.progress().isCorrect()
        );
        model.addAttribute(
                "completed",
                lessonViewModel.codeComplete()
        );
        model.addAttribute(
                "selectedOption",
                lessonViewModel.progress().getSelectedOptionId()
        );
        model.addAttribute(
                "hasPreviousStep",
                navigation.hasPreviousStep()
        );
        model.addAttribute("hasNextStep", navigation.hasNextStep());
        model.addAttribute("reviewMode", navigation.reviewMode());
        model.addAttribute("showNextStep", navigation.canMoveNext());
        model.addAttribute("correctOption", navigation.correctOption());
        model.addAttribute(
                "energizedSteps",
                lessonViewModel.completedCount()
        );
    }
}
