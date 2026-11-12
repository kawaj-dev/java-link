package com.javalink.controller;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.javalink.service.CodeReadingLessonCatalog;
import com.javalink.service.CodeReadingStageProgressService;
import com.javalink.service.CodeReadingStageProgressService.StageLearningStatus;
import com.javalink.service.CodeReadingStageProgressService.StageProgress;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CodeReadingIndexController.class)
@Import(CodeReadingLessonCatalog.class)
class CodeReadingIndexControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CodeReadingStageProgressService stageProgressService;

    @BeforeEach
    void setUpProgress() {
        when(stageProgressService.getStageProgresses(any(HttpSession.class)))
                .thenReturn(Map.of(
                        "hello-program-reading", notStarted(),
                        "variable-program-reading", notStarted(),
                        "operator-program-reading", notStarted(),
                        "conditional-program-reading", notStarted()
                ));
    }

    @Test
    void 目次にStage1から4を教材定義の順番で表示する() throws Exception {
        mockMvc.perform(get("/quiz/stages"))
                .andExpect(status().isOk())
                .andExpect(view().name("code-reading-index"))
                .andExpect(model().attribute("stages", contains(
                        new CodeReadingIndexController.StageIndexItem(
                                "hello-program-reading",
                                "Stage 1",
                                "「Hello」と表示するプログラムを読めるようになろう",
                                "クラスとmainメソッドを作り、文字列を画面に表示する基本のコードを学びます。",
                                "未学習", "is-not-started", 0),
                        new CodeReadingIndexController.StageIndexItem(
                                "variable-program-reading",
                                "Stage 2",
                                "変数を使って年齢を表示しよう",
                                "int型の変数に整数を代入し、その値を利用して表示する流れを学びます。",
                                "未学習", "is-not-started", 0),
                        new CodeReadingIndexController.StageIndexItem(
                                "operator-program-reading",
                                "Stage 3",
                                "演算子を使って計算しよう",
                                "変数と演算子を使って計算し、その結果を表示するコードを学びます。",
                                "未学習", "is-not-started", 0),
                        new CodeReadingIndexController.StageIndexItem(
                                "conditional-program-reading",
                                "Stage 4",
                                "信号を見て行動を決めよう",
                                "if・elseによる条件分岐と、equals()による文字列の比較を学びます。",
                                "未学習", "is-not-started", 0)
                )))
                .andExpect(content().string(containsString("コードを左から読む")))
                .andExpect(content().string(containsString("学習の目次")));
    }

    @Test
    void 各Stageの既存URLへのリンクを表示する() throws Exception {
        mockMvc.perform(get("/quiz/stages"))
                .andExpect(status().isOk())
                .andExpect(result -> assertEquals(
                        4,
                        countOccurrences(
                                result.getResponse().getContentAsString(),
                                "class=\"code-reading-stage-card "
                        )
                ))
                .andExpect(content().string(containsString(
                        "href=\"/quiz?lessonId=hello-program-reading\"")))
                .andExpect(content().string(containsString(
                        "href=\"/quiz?lessonId=variable-program-reading\"")))
                .andExpect(content().string(containsString(
                        "href=\"/quiz?lessonId=operator-program-reading\"")))
                .andExpect(content().string(containsString(
                        "href=\"/quiz?lessonId=conditional-program-reading\"")));
    }

    @Test
    void 各Stageの学習状況と進捗率を表示する() throws Exception {
        when(stageProgressService.getStageProgresses(any(HttpSession.class)))
                .thenReturn(Map.of(
                        "hello-program-reading",
                        new StageProgress(StageLearningStatus.COMPLETED, 100),
                        "variable-program-reading",
                        new StageProgress(StageLearningStatus.IN_PROGRESS, 30),
                        "operator-program-reading", notStarted(),
                        "conditional-program-reading", notStarted()
                ));

        mockMvc.perform(get("/quiz/stages"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("✓ 学習完了")))
                .andExpect(content().string(containsString("学習中")))
                .andExpect(content().string(containsString("未学習")))
                .andExpect(content().string(containsString("100%")))
                .andExpect(content().string(containsString("30%")))
                .andExpect(content().string(containsString(
                        "aria-valuenow=\"30\""
                )));
    }

    private static int countOccurrences(String text, String target) {
        return (text.length() - text.replace(target, "").length()) / target.length();
    }

    private static StageProgress notStarted() {
        return new StageProgress(StageLearningStatus.NOT_STARTED, 0);
    }
}
