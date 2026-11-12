package com.javalink.controller;

import com.javalink.model.Lesson;
import com.javalink.service.CodeReadingLessonCatalog;
import com.javalink.service.CodeReadingStageProgressService;
import com.javalink.service.CodeReadingStageProgressService.StageProgress;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 「コードを左から読む」のStage目次を表示するControllerです。
 */
@Controller
public class CodeReadingIndexController {

    private static final Map<String, String> STAGE_DESCRIPTIONS = Map.of(
            CodeReadingLessonCatalog.STAGE1_LESSON_ID,
            "クラスとmainメソッドを作り、文字列を画面に表示する基本のコードを学びます。",
            CodeReadingLessonCatalog.STAGE2_LESSON_ID,
            "int型の変数に整数を代入し、その値を利用して表示する流れを学びます。",
            CodeReadingLessonCatalog.STAGE3_LESSON_ID,
            "変数と演算子を使って計算し、その結果を表示するコードを学びます。",
            CodeReadingLessonCatalog.STAGE4_LESSON_ID,
            "if・elseによる条件分岐と、equals()による文字列の比較を学びます。"
    );

    private final CodeReadingLessonCatalog lessonCatalog;
    private final CodeReadingStageProgressService stageProgressService;

    public CodeReadingIndexController(
            CodeReadingLessonCatalog lessonCatalog,
            CodeReadingStageProgressService stageProgressService
    ) {
        this.lessonCatalog = lessonCatalog;
        this.stageProgressService = stageProgressService;
    }

    @GetMapping("/quiz/stages")
    public String showIndex(Model model, HttpSession session) {
        Map<String, StageProgress> progressByLessonId =
                stageProgressService.getStageProgresses(session);
        List<StageIndexItem> stages = lessonCatalog.getLessons().stream()
                .map(lesson -> toStageIndexItem(
                        lesson,
                        progressByLessonId.getOrDefault(
                                lesson.id(),
                                new StageProgress(
                                        CodeReadingStageProgressService
                                                .StageLearningStatus.NOT_STARTED,
                                        0
                                )
                        )
                ))
                .toList();
        model.addAttribute("stages", stages);
        return "code-reading-index";
    }

    private StageIndexItem toStageIndexItem(
            Lesson lesson,
            StageProgress progress
    ) {
        return new StageIndexItem(
                lesson.id(),
                lesson.title(),
                lesson.description(),
                STAGE_DESCRIPTIONS.getOrDefault(lesson.id(), lesson.description()),
                progress.status().getLabel(),
                progress.status().getCssClass(),
                progress.percentage()
        );
    }

    public record StageIndexItem(
            String lessonId,
            String stageName,
            String title,
            String description,
            String statusLabel,
            String statusCssClass,
            int progressPercentage
    ) {
    }
}
