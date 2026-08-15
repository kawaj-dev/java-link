package com.javalink.controller;

import com.javalink.model.CodeReadingPageViewModel;
import com.javalink.model.CodeReadingPhase;
import com.javalink.model.CodeReadingAnswerResponse;
import com.javalink.model.LessonViewModel;
import com.javalink.model.ProgramRunResult;
import com.javalink.model.QuizOption;
import com.javalink.service.CodeReadingCourseService;
import com.javalink.service.CodeReadingPageViewModelService;
import com.javalink.service.CodeReadingLessonCatalog;
import com.javalink.service.CodeReadingService;
import com.javalink.service.LessonRunService;
import com.javalink.service.LessonViewModelService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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

    private final CodeReadingLessonCatalog lessonCatalog;
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
            LessonRunService lessonRunService,
            CodeReadingLessonCatalog lessonCatalog
    ) {
        this.courseService = courseService;
        this.pageViewModelService = pageViewModelService;
        this.codeReadingService = codeReadingService;
        this.lessonViewModelService = lessonViewModelService;
        this.lessonRunService = lessonRunService;
        this.lessonCatalog = lessonCatalog;
    }

    /** 現在のセッション状態に対応する画面を表示します。 */
    @GetMapping("/quiz")
    public String showQuiz(
            @RequestParam(name = "lessonId", required = false) String lessonId,
            Model model,
            HttpSession session
    ) {
        addPageModel(model, session, resolveLessonId(lessonId));
        return "quiz";
    }

    /** 導入画面からPart 1の学習を開始します。 */
    @PostMapping("/quiz/start")
    public String start(@RequestParam(name = "lessonId", required = false) String lessonId, HttpSession session) {
        lessonId = resolveLessonId(lessonId);
        courseService.startLearning(session, lessonId);
        return redirectToQuiz(lessonId);
    }

    /** 現在の項目へ回答し、正解後も確認のため同じ項目に留まります。 */
    @PostMapping("/quiz/answer")
    public String answer(
            @RequestParam("selectedOption") String selectedOption,
            @RequestParam(name = "lessonId", required = false) String lessonId,
            HttpSession session
    ) {
        lessonId = resolveLessonId(lessonId);
        courseService.answerCurrentItem(
                session,
                lessonId,
                selectedOption
        );
        return redirectToQuiz(lessonId);
    }

    /** 正解内容を確認してから、同じPart内の次の用語へ進みます。 */
    @PostMapping("/quiz/item/next")
    public String moveToNextItem(@RequestParam(name = "lessonId", required = false) String lessonId, HttpSession session) {
        lessonId = resolveLessonId(lessonId);
        courseService.moveToNextItem(session, lessonId);
        return redirectToQuiz(lessonId);
    }

    /** 学習者自身の判断で、現在のPartを理解済みとして次へ進みます。 */
    @PostMapping("/quiz/part/understood")
    public String markCurrentPartUnderstood(@RequestParam(name = "lessonId", required = false) String lessonId, HttpSession session) {
        lessonId = resolveLessonId(lessonId);
        courseService.markCurrentPartUnderstoodAndAdvance(
                session,
                lessonId
        );
        return redirectToQuiz(lessonId);
    }

    /** JavaScript演出用に、Serviceが判定・保存した回答結果を返します。 */
    @PostMapping("/quiz/answer/interactive")
    @ResponseBody
    public CodeReadingAnswerResponse answerInteractive(
            @RequestParam("selectedOption") String selectedOption,
            @RequestParam("targetStepId") String targetStepId,
            @RequestParam(name = "lessonId", required = false) String lessonId,
            HttpSession session
    ) {
        lessonId = resolveLessonId(lessonId);
        return courseService.answerCircuitStep(
                session,
                lessonId,
                targetStepId,
                selectedOption
        );
    }

    /** JavaScriptなしでも回路上の操作を同じ検証経路で処理します。 */
    @PostMapping("/quiz/answer/circuit")
    public String answerCircuit(
            @RequestParam("selectedOption") String selectedOption,
            @RequestParam("targetStepId") String targetStepId,
            @RequestParam(name = "lessonId", required = false) String lessonId,
            HttpSession session
    ) {
        lessonId = resolveLessonId(lessonId);
        courseService.answerCircuitStep(
                session,
                lessonId,
                targetStepId,
                selectedOption
        );
        return redirectToQuiz(lessonId);
    }

    /** 完了したPartから次のPart、またはまとめ画面へ進みます。 */
    @PostMapping("/quiz/part/next")
    public String moveToNextPart(@RequestParam(name = "lessonId", required = false) String lessonId, HttpSession session) {
        lessonId = resolveLessonId(lessonId);
        courseService.moveToNextPart(session, lessonId);
        return redirectToQuiz(lessonId);
    }

    /** 完了済み教材を安全に疑似実行します。 */
    @PostMapping("/quiz/run")
    public String run(@RequestParam(name = "lessonId", required = false) String lessonId, Model model, HttpSession session) {
        lessonId = resolveLessonId(lessonId);
        ProgramRunResult result = lessonRunService.runLesson(
                session,
                lessonId
        );
        addPageModel(model, session, lessonId);
        model.addAttribute(PROGRAM_RUN_RESULT_ATTRIBUTE, result);
        return "quiz";
    }

    /** 問題進捗と画面フェーズを初期化して導入へ戻します。 */
    @PostMapping("/quiz/reset")
    public String reset(@RequestParam(name = "lessonId", required = false) String lessonId, HttpSession session) {
        lessonId = resolveLessonId(lessonId);
        courseService.reset(session, lessonId);
        return redirectToQuiz(lessonId);
    }

    /** すべてのStageの進捗を初期化してStage 1の導入へ戻します。 */
    @PostMapping("/quiz/reset-all")
    public String resetAll(HttpSession session) {
        lessonCatalog.getLessons().forEach(lesson ->
                courseService.reset(session, lesson.id())
        );
        return "redirect:/quiz";
    }

    private void addPageModel(Model model, HttpSession session, String lessonId) {
        CodeReadingPageViewModel page =
                pageViewModelService.create(session, lessonId);
        model.addAttribute(PAGE_VIEW_MODEL_ATTRIBUTE, page);
        model.addAttribute("lessonId", lessonId);
        model.addAttribute(
                "interactiveAnswerUrl",
                CodeReadingLessonCatalog.STAGE1_LESSON_ID.equals(lessonId)
                        ? "/quiz/answer/interactive"
                        : "/quiz/answer/interactive?lessonId=" + lessonId
        );
        model.addAttribute("phase", page.phase());

        if (page.phase() == CodeReadingPhase.LEARNING) {
            addLearningModel(model, session, page, lessonId);
        }
        if (page.phase() == CodeReadingPhase.SUMMARY) {
            LessonViewModel lessonViewModel =
                    lessonViewModelService.createViewModel(session, lessonId);
            model.addAttribute(
                    LESSON_VIEW_MODEL_ATTRIBUTE,
                    lessonViewModel
            );
            boolean firstStage = CodeReadingLessonCatalog.STAGE1_LESSON_ID
                    .equals(lessonId);
            var nextStage = lessonCatalog.getNextDefinition(lessonId);
            model.addAttribute("summaryFirstStage", firstStage);
            model.addAttribute(
                    "summaryNextLessonId",
                    nextStage.map(definition -> definition.lessonId()).orElse(null)
            );
            model.addAttribute(
                    "summaryNextStageName",
                    nextStage.map(definition -> definition.stageName()).orElse(null)
            );
        }
    }

    private String resolveLessonId(String lessonId) {
        String resolved = lessonId == null || lessonId.isBlank()
                ? lessonCatalog.getDefaultDefinition().lessonId()
                : lessonId;
        lessonCatalog.getDefinition(resolved);
        return resolved;
    }

    private String redirectToQuiz(String lessonId) {
        return CodeReadingLessonCatalog.STAGE1_LESSON_ID.equals(lessonId)
                ? "redirect:/quiz"
                : "redirect:/quiz?lessonId=" + lessonId;
    }

    private void addLearningModel(
            Model model,
            HttpSession session,
            CodeReadingPageViewModel page,
            String lessonId
    ) {
        LessonViewModel lessonViewModel =
                lessonViewModelService.createViewModel(session, lessonId);
        List<QuizOption> options = codeReadingService.createSelectionOptions(
                lessonId,
                lessonViewModel.progress()
        );

        model.addAttribute(LESSON_VIEW_MODEL_ATTRIBUTE, lessonViewModel);
        model.addAttribute(SHUFFLED_OPTIONS_ATTRIBUTE, options);
        model.addAttribute(
                READING_ITEMS_ATTRIBUTE,
                codeReadingService.createPartItems(
                        lessonId,
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
