package com.javalink.controller;

import com.javalink.model.CodeReadingPageViewModel;
import com.javalink.model.CodeReadingPhase;
import com.javalink.model.LessonViewModel;
import com.javalink.model.ProgramRunResult;
import com.javalink.model.QuizOption;
import com.javalink.service.CodeReadingCourseService;
import com.javalink.service.CodeReadingPageViewModelService;
import com.javalink.service.CodeReadingService;
import com.javalink.service.LessonRunService;
import com.javalink.service.LessonService;
import com.javalink.service.LessonViewModelService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 「コードを左から読む」画面の入力と画面遷移を担当します。
 * 正解判定やPart完了判定はServiceへ任せます。
 */
@Controller
public class QuizController {

    public static final String PAGE_VIEW_MODEL_ATTRIBUTE =
            "codeReadingPage";
    public static final String LESSON_VIEW_MODEL_ATTRIBUTE =
            "lessonViewModel";
    public static final String PROGRAM_RUN_RESULT_ATTRIBUTE =
            "programRunResult";
    public static final String SHUFFLED_OPTIONS_ATTRIBUTE =
            "shuffledOptions";
    public static final String READING_ITEMS_ATTRIBUTE = "readingItems";

    private static final String LESSON_ID =
            LessonService.HELLO_PROGRAM_LESSON_ID;

    private final CodeReadingCourseService courseService;
    private final CodeReadingPageViewModelService pageViewModelService;
    private final CodeReadingService codeReadingService;
    private final LessonViewModelService lessonViewModelService;
    private final LessonRunService lessonRunService;

    public QuizController(
            CodeReadingCourseService courseService,
            CodeReadingPageViewModelService pageViewModelService,
            CodeReadingService codeReadingService,
            LessonViewModelService lessonViewModelService,
            LessonRunService lessonRunService
    ) {
        this.courseService = courseService;
        this.pageViewModelService = pageViewModelService;
        this.codeReadingService = codeReadingService;
        this.lessonViewModelService = lessonViewModelService;
        this.lessonRunService = lessonRunService;
    }

    /** 現在のセッション状態に対応する画面を表示します。 */
    @GetMapping("/quiz")
    public String showQuiz(Model model, HttpSession session) {
        addPageModel(model, session);
        return "quiz";
    }

    /** 導入画面からPart 1の学習を開始します。 */
    @PostMapping("/quiz/start")
    public String start(HttpSession session) {
        courseService.startLearning(session, LESSON_ID);
        return "redirect:/quiz";
    }

    /** 現在の項目へ回答し、正解時は同じPart内だけ自動進行します。 */
    @PostMapping("/quiz/answer")
    public String answer(
            @RequestParam("selectedOption") String selectedOption,
            HttpSession session
    ) {
        courseService.answerCurrentItem(
                session,
                LESSON_ID,
                selectedOption
        );
        return "redirect:/quiz";
    }

    /** 完了したPartから次のPart、またはまとめ画面へ進みます。 */
    @PostMapping("/quiz/part/next")
    public String moveToNextPart(HttpSession session) {
        courseService.moveToNextPart(session, LESSON_ID);
        return "redirect:/quiz";
    }

    /** 完了済み教材を安全に疑似実行します。 */
    @PostMapping("/quiz/run")
    public String run(Model model, HttpSession session) {
        ProgramRunResult result = lessonRunService.runLesson(
                session,
                LESSON_ID
        );
        addPageModel(model, session);
        model.addAttribute(PROGRAM_RUN_RESULT_ATTRIBUTE, result);
        return "quiz";
    }

    /** 問題進捗と画面フェーズを初期化して導入へ戻します。 */
    @PostMapping("/quiz/reset")
    public String reset(HttpSession session) {
        courseService.reset(session, LESSON_ID);
        return "redirect:/quiz";
    }

    private void addPageModel(Model model, HttpSession session) {
        CodeReadingPageViewModel page =
                pageViewModelService.create(session, LESSON_ID);
        model.addAttribute(PAGE_VIEW_MODEL_ATTRIBUTE, page);
        model.addAttribute("phase", page.phase());

        if (page.phase() == CodeReadingPhase.LEARNING) {
            addLearningModel(model, session, page);
        }
        if (page.phase() == CodeReadingPhase.SUMMARY) {
            LessonViewModel lessonViewModel =
                    lessonViewModelService.createViewModel(session, LESSON_ID);
            model.addAttribute(
                    LESSON_VIEW_MODEL_ATTRIBUTE,
                    lessonViewModel
            );
        }
    }

    private void addLearningModel(
            Model model,
            HttpSession session,
            CodeReadingPageViewModel page
    ) {
        LessonViewModel lessonViewModel =
                lessonViewModelService.createViewModel(session, LESSON_ID);
        List<QuizOption> options = codeReadingService.createSelectionOptions(
                LESSON_ID,
                lessonViewModel.progress()
        );

        model.addAttribute(LESSON_VIEW_MODEL_ATTRIBUTE, lessonViewModel);
        model.addAttribute(SHUFFLED_OPTIONS_ATTRIBUTE, options);
        model.addAttribute(
                READING_ITEMS_ATTRIBUTE,
                codeReadingService.createPartItems(
                        LESSON_ID,
                        lessonViewModel.progress(),
                        page.currentPart()
                )
        );
        model.addAttribute("answered", lessonViewModel.progress().isAnswered());
        model.addAttribute("correct", lessonViewModel.progress().isCorrect());
        model.addAttribute(
                "selectedOption",
                lessonViewModel.progress().getSelectedOptionId()
        );
    }
}
