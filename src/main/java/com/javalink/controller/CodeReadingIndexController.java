package com.javalink.controller;

import com.javalink.model.Lesson;
import com.javalink.service.CodeReadingLessonCatalog;
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

    public CodeReadingIndexController(CodeReadingLessonCatalog lessonCatalog) {
        this.lessonCatalog = lessonCatalog;
    }

    @GetMapping("/quiz/stages")
    public String showIndex(Model model) {
        List<StageIndexItem> stages = lessonCatalog.getLessons().stream()
                .map(this::toStageIndexItem)
                .toList();
        model.addAttribute("stages", stages);
        return "code-reading-index";
    }

    private StageIndexItem toStageIndexItem(Lesson lesson) {
        return new StageIndexItem(
                lesson.id(),
                lesson.title(),
                lesson.description(),
                STAGE_DESCRIPTIONS.getOrDefault(lesson.id(), lesson.description())
        );
    }

    public record StageIndexItem(
            String lessonId,
            String stageName,
            String title,
            String description
    ) {
    }
}
