package com.javalink.controller;

import com.javalink.model.CodeReadingPageViewModel;
import com.javalink.model.CodeReadingFlowState;
import com.javalink.model.CodeReadingPhase;
import com.javalink.model.LessonProgress;
import com.javalink.service.CodeReadingCourseService;
import com.javalink.service.CodeReadingFlowService;
import com.javalink.service.CodeReadingLessonCatalog;
import com.javalink.service.LessonProgressService;
import com.javalink.service.LessonService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
class QuizControllerTest {

    private static final String LESSON_ID =
            LessonService.HELLO_PROGRAM_LESSON_ID;

    private final MockMvc mockMvc;

    @Autowired
    private CodeReadingCourseService courseService;

    @Autowired
    private LessonProgressService progressService;

    @Autowired
    private CodeReadingFlowService flowService;

    QuizControllerTest(@Autowired WebApplicationContext context) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void Stage2導入画面から独立した教材を開始できる() throws Exception {
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(get("/quiz")
                        .param("lessonId", "variable-program-reading")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Stage 2")))
                .andExpect(content().string(containsString("変数を使って年齢を表示しよう")))
                .andExpect(content().string(containsString("int age = 20;")));

        mockMvc.perform(post("/quiz/start")
                        .param("lessonId", "variable-program-reading")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/quiz?lessonId=variable-program-reading"));

        mockMvc.perform(get("/quiz")
                        .param("lessonId", "variable-program-reading")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Part 1 / 5")));
    }

    @Test
    void Stage3導入画面は完成コードと学習テーマを表示する() throws Exception {
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(get("/quiz")
                        .param("lessonId", CodeReadingLessonCatalog.STAGE3_LESSON_ID)
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Stage 3")))
                .andExpect(content().string(containsString("演算子を使って計算しよう")))
                .andExpect(content().string(containsString(
                        "変数に入れた値を使って、計算するコードを読めるようになろう"
                )))
                .andExpect(content().string(containsString("int c = a + b;")))
                .andExpect(content().string(containsString("System.out.println(c);")));
    }

    @Test
    void 初回GETは導入画面と完成コードだけを表示する() throws Exception {
        mockMvc.perform(get("/quiz"))
                .andExpect(status().isOk())
                .andExpect(view().name("quiz"))
                .andExpect(model().attribute("phase", CodeReadingPhase.INTRO))
                .andExpect(model().attributeExists("codeReadingPage"))
                .andExpect(content().string(containsString("完成コード")))
                .andExpect(content().string(containsString("Main.java")))
                .andExpect(content().string(containsString("quiz-intro-code")))
                .andExpect(content().string(containsString("public class Main {\n    public static void main(String[] args) {\n        System.out.println(\"Hello\");\n    }\n}")))
                .andExpect(content().string(containsString("スタート")));
    }

    @Test
    void スタートでPart1の先頭へ進みリダイレクトする() throws Exception {
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/quiz/start").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/quiz"));

        mockMvc.perform(get("/quiz").session(session))
                .andExpect(status().isOk())
                .andExpect(model().attribute("phase", CodeReadingPhase.LEARNING))
                .andExpect(model().attributeExists("lessonViewModel"))
                .andExpect(content().string(containsString("Part 1 / 4")))
                .andExpect(content().string(containsString("クラスを作る")));

        assertEquals(
                "class-public",
                progressService.getProgress(session, LESSON_ID)
                        .getCurrentStepId()
        );
    }

    @Test
    void 全Part共通で正解後も現在stepに留まり確認画面を表示する() throws Exception {
        MockHttpSession session = new MockHttpSession();
        courseService.startLearning(session, LESSON_ID);

        mockMvc.perform(post("/quiz/answer")
                        .session(session)
                        .param("selectedOption", "accessible"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/quiz"));

        LessonProgress progress = progressService.getProgress(session, LESSON_ID);
        assertEquals("class-public", progress.getCurrentStepId());
        assertTrue(progress.isStepCompleted("class-public"));
        assertTrue(progress.isAnswered());
        assertTrue(progress.isCorrect());

        mockMvc.perform(get("/quiz").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("正解です！"))))
                .andExpect(content().string(containsString("アクセス修飾子")))
                .andExpect(content().string(not(containsString("quiz-reading-step-heading"))))
                .andExpect(content().string(not(containsString("次の問題へ"))))
                .andExpect(content().string(containsString("data-answer-enabled=\"true\"")))
                .andExpect(content().string(containsString("ほかの場所からも使える")))
                .andExpect(content().string(containsString("アクセスできる範囲の違い")))
                .andExpect(content().string(containsString("data-section-type=\"text\"")))
                .andExpect(content().string(containsString("data-section-type=\"table\"")))
                .andExpect(content().string(not(containsString("data-section-layout"))))
                .andExpect(content().string(containsString("（指定なし）")))
                .andExpect(content().string(containsString("ほかの場所からも")))
                .andExpect(content().string(not(containsString("今回はこれだけ"))));
    }

    @Test
    void classとMainの正解後に図とQAndAを表示する() throws Exception {
        MockHttpSession session = new MockHttpSession();
        courseService.startLearning(session, LESSON_ID);

        courseService.answerCurrentItem(session, LESSON_ID, "accessible");
        courseService.moveToNextItem(session, LESSON_ID);
        courseService.answerCurrentItem(session, LESSON_ID, "declare-class");

        mockMvc.perform(get("/quiz").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("クラス宣言の基本形")))
                .andExpect(content().string(containsString("ポイント")))
                .andExpect(content().string(containsString("class クラス名")))
                .andExpect(content().string(containsString(
                        "作るクラスの名前（自分で決めることができます）"
                )))
                .andExpect(content().string(not(containsString("家でたとえると"))));

        courseService.moveToNextItem(session, LESSON_ID);
        courseService.answerCurrentItem(session, LESSON_ID, "main-class-name");

        mockMvc.perform(get("/quiz").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("名前を付けるときのルール")))
                .andExpect(content().string(containsString("自分で決めるクラスの名前")))
                .andExpect(content().string(containsString("自由に決めてOK")))
                .andExpect(content().string(containsString("大文字で始めるのが慣習")))
                .andExpect(content().string(containsString("Main や Test")))
                .andExpect(content().string(containsString("Main.java")))
                .andExpect(content().string(containsString(
                        "これから作るクラスの名前をJavaに伝えます。"
                )))
                .andExpect(content().string(not(containsString("大文字で始めるのがマナー"))))
                .andExpect(content().string(not(containsString(
                        "作成するプログラムの部品（クラス）に付ける固有の名前です。"
                ))));

        mockMvc.perform(post("/quiz/answer/interactive")
                        .session(session)
                        .param("targetStepId", "class-open")
                        .param("selectedOption", "block-start"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correct").value(true))
                .andExpect(jsonPath("$.explanationSections[0].title")
                        .value("セパレータ（左波括弧）　separator"))
                .andExpect(jsonPath("$.explanationSections[2].entries[0].before")
                        .value("閉じ忘れがあると、Javaはプログラムの構造を正しく理解できずエラーになります。"))
                .andExpect(jsonPath("$.explanationSections[3].officialReferences.length()").value(3))
                .andExpect(jsonPath("$.explanationSections.length()").value(4));
    }

    @Test
    void 次の問題へで同じPart内の次stepへ進み回答状態を初期化する() throws Exception {
        MockHttpSession session = new MockHttpSession();
        courseService.startLearning(session, LESSON_ID);
        courseService.answerCurrentItem(session, LESSON_ID, "accessible");

        mockMvc.perform(post("/quiz/item/next").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/quiz"));

        LessonProgress progress = progressService.getProgress(session, LESSON_ID);
        assertEquals("class-keyword", progress.getCurrentStepId());
        assertFalse(progress.isAnswered());
        assertFalse(progress.isCorrect());
        assertEquals("", progress.getSelectedOptionId());
    }

    @Test
    void 学習画面には共通レイアウトと完了ラベルがある() throws Exception {
        MockHttpSession session = new MockHttpSession();
        courseService.startLearning(session, LESSON_ID);

        mockMvc.perform(get("/quiz").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(
                        "data-interactive-answer-url=\"/quiz/answer/interactive\""
                )))
                .andExpect(content().string(containsString(
                        "quiz-circuit-code-button"
                )))
                .andExpect(content().string(containsString(
                        "data-code-step=\"class-public\""
                )))
                .andExpect(content().string(containsString(
                        "value=\"accessible\""
                )))
                .andExpect(content().string(not(containsString(
                        "ほかの場所からも使える。知識スイッチを入れる"
                ))))
                .andExpect(content().string(containsString(
                        "data-current-meaning-slot"
                )))
                .andExpect(content().string(containsString(
                        "quiz-circuit-code-button--next"
                )))
                .andExpect(content().string(containsString(
                        "data-bulb-step=\"class-open\""
                )))
                .andExpect(content().string(containsString(
                        "/js/quiz-reading.js"
                )))
                .andExpect(content().string(containsString("data-explanation-sections")))
                .andExpect(content().string(not(containsString("data-explanation-technical"))))
                .andExpect(content().string(not(containsString("data-explanation-beginner"))))
                .andExpect(content().string(not(containsString("今回読むコード"))))
                .andExpect(content().string(containsString("data-part-complete")))
                .andExpect(content().string(containsString("data-circuit-group=\"class-declaration\"")))
                .andExpect(content().string(not(containsString("data-circuit-group=\"main-method\""))))
                .andExpect(content().string(containsString("quiz-code-circuit-connector")))
                .andExpect(content().string(containsString("理解済み　次のPartへ")))
                .andExpect(content().string(not(containsString("data-part-progress"))))
                .andExpect(content().string(not(containsString("完了 0 / 4"))))
                .andExpect(content().string(not(containsString("QUESTION 1 / 4"))))
                .andExpect(content().string(not(containsString("コード回路</h3>"))))
                .andExpect(content().string(not(containsString("quiz-reading-step-heading"))))
                .andExpect(content().string(not(containsString("public の意味はどれですか？"))));
    }

    @Test
    void 対話回答はサーバー判定結果をJSONで返す() throws Exception {
        MockHttpSession session = new MockHttpSession();
        courseService.startLearning(session, LESSON_ID);

        mockMvc.perform(post("/quiz/answer/interactive")
                        .session(session)
                        .param("targetStepId", "class-public")
                        .param("selectedOption", "accessible"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correct").value(true))
                .andExpect(jsonPath("$.answeredStepId").value("class-public"))
                .andExpect(jsonPath("$.meaning").value("ほかの場所からも使える"))
                .andExpect(jsonPath("$.completedCount").value(1))
                .andExpect(jsonPath("$.partCompleted").value(false))
                .andExpect(jsonPath("$.technicalTerm").value("アクセス修飾子"))
                .andExpect(jsonPath("$.technicalExplanation").doesNotExist())
                .andExpect(jsonPath("$.beginnerExplanations").doesNotExist())
                .andExpect(jsonPath("$.explanationSections[1].sectionType").value("table"))
                .andExpect(jsonPath("$.explanationSections[1].tableHeader").value(true))
                .andExpect(jsonPath("$.explanationSections[1].kind").doesNotExist())
                .andExpect(jsonPath("$.explanationSections[1].entries[0].label")
                        .value("書き方"))
                .andExpect(jsonPath("$.explanationSections[1].entries[0].before")
                        .value("アクセスできる範囲"))
                .andExpect(jsonPath("$.explanationSections[1].entries[2].label")
                        .value("protected"))
                .andExpect(jsonPath("$.explanationSections[3].sectionType")
                        .value("official-references"))
                .andExpect(jsonPath("$.explanationSections[3].officialReferences.length()")
                        .value(2))
                .andExpect(jsonPath("$.explanationSections[3].officialReferences[0].source")
                        .value("jls"))
                .andExpect(jsonPath("$.explanationSections[3].officialReferences[0].version")
                        .value("Java SE 21"))
                .andExpect(jsonPath("$.explanationSections[3].officialReferences[0].sourceName")
                        .value("JLS Java SE 21"))
                .andExpect(jsonPath("$.explanationSections[3].officialReferences[0].sectionNumber")
                        .value("§6.6"))
                .andExpect(jsonPath("$.explanationSections[3].officialReferences[0].sectionTitle")
                        .value("Access Control"))
                .andExpect(jsonPath("$.explanationSections[3].officialReferences[0].description")
                        .value("Javaのアクセス制御について定めています。"))
                .andExpect(jsonPath("$.explanationSections[3].officialReferences[0].uri")
                        .value("https://docs.oracle.com/javase/specs/jls/se21/html/jls-6.html#jls-6.6"))
                .andExpect(jsonPath("$.nextStepId").value("class-keyword"));

        assertEquals(
                "class-public",
                progressService.getProgress(session, LESSON_ID)
                        .getCurrentStepId()
        );
    }

    @Test
    void 直後のコードは一回のクリックで移動と回答を完了する() throws Exception {
        MockHttpSession session = new MockHttpSession();
        courseService.startLearning(session, LESSON_ID);
        courseService.answerCircuitStep(
                session, LESSON_ID, "class-public", "accessible"
        );

        mockMvc.perform(post("/quiz/answer/interactive")
                        .session(session)
                        .param("targetStepId", "class-keyword")
                        .param("selectedOption", "declare-class"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correct").value(true))
                .andExpect(jsonPath("$.answeredStepId").value("class-keyword"))
                .andExpect(jsonPath("$.completedCount").value(2))
                .andExpect(jsonPath("$.nextStepId").value("class-name"));

        LessonProgress progress = progressService.getProgress(session, LESSON_ID);
        assertEquals("class-keyword", progress.getCurrentStepId());
        assertTrue(progress.isStepCompleted("class-public"));
        assertTrue(progress.isStepCompleted("class-keyword"));
    }

    @Test
    void step飛ばしと完了済みstepの再送信を拒否する() throws Exception {
        MockHttpSession session = new MockHttpSession();
        courseService.startLearning(session, LESSON_ID);
        courseService.answerCircuitStep(
                session, LESSON_ID, "class-public", "accessible"
        );

        mockMvc.perform(post("/quiz/answer/interactive")
                        .session(session)
                        .param("targetStepId", "class-name")
                        .param("selectedOption", "main-class-name"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correct").value(false));
        mockMvc.perform(post("/quiz/answer/interactive")
                        .session(session)
                        .param("targetStepId", "class-public")
                        .param("selectedOption", "accessible"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correct").value(false));

        LessonProgress progress = progressService.getProgress(session, LESSON_ID);
        assertEquals("class-public", progress.getCurrentStepId());
        assertFalse(progress.isStepCompleted("class-name"));
        assertEquals(1, progress.getCompletedStepIds().size());
    }

    @Test
    void 次stepでも不正なcardIdは進捗を変更しない() throws Exception {
        MockHttpSession session = new MockHttpSession();
        courseService.startLearning(session, LESSON_ID);
        courseService.answerCircuitStep(
                session, LESSON_ID, "class-public", "accessible"
        );

        mockMvc.perform(post("/quiz/answer/interactive")
                        .session(session)
                        .param("targetStepId", "class-keyword")
                        .param("selectedOption", "no-return"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correct").value(false));

        LessonProgress progress = progressService.getProgress(session, LESSON_ID);
        assertEquals("class-public", progress.getCurrentStepId());
        assertFalse(progress.isStepCompleted("class-keyword"));
    }

    @Test
    void 未回答スイッチは各stepのコードを教材定義から表示する() throws Exception {
        MockHttpSession classSession = new MockHttpSession();
        courseService.startLearning(classSession, LESSON_ID);
        courseService.answerCurrentItem(classSession, LESSON_ID, "accessible");
        courseService.moveToNextItem(classSession, LESSON_ID);

        mockMvc.perform(get("/quiz").session(classSession))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("data-code-step=\"class-keyword\"")))
                .andExpect(content().string(not(containsString(
                        "クラスを作る。知識スイッチを入れる"
                ))));

        courseService.answerCurrentItem(classSession, LESSON_ID, "declare-class");
        courseService.moveToNextItem(classSession, LESSON_ID);
        mockMvc.perform(get("/quiz").session(classSession))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("data-code-step=\"class-name\"")));

        MockHttpSession printSession = new MockHttpSession();
        completePart1(printSession);
        courseService.moveToNextPart(printSession, LESSON_ID);
        completePart2(printSession);
        courseService.moveToNextPart(printSession, LESSON_ID);
        mockMvc.perform(get("/quiz").session(printSession))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(
                        "data-code-step=\"print-command\""
                )));
    }

    @Test
    void 不正解の対話回答は進捗を増やさない() throws Exception {
        MockHttpSession session = new MockHttpSession();
        courseService.startLearning(session, LESSON_ID);

        mockMvc.perform(post("/quiz/answer/interactive")
                        .session(session)
                        .param("targetStepId", "class-public")
                        .param("selectedOption", "no-return"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correct").value(false))
                .andExpect(jsonPath("$.completedCount").value(0));

        assertEquals(
                "class-public",
                progressService.getProgress(session, LESSON_ID)
                        .getCurrentStepId()
        );
    }

    @Test
    void 更新後は完了済みカードと点灯状態を復元する() throws Exception {
        MockHttpSession session = new MockHttpSession();
        courseService.startLearning(session, LESSON_ID);
        courseService.answerCurrentItem(session, LESSON_ID, "accessible");

        mockMvc.perform(get("/quiz").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(
                        "quiz-reading-code-meaning--visible"
                )))
                .andExpect(content().string(containsString("ほかの場所からも使える")))
                .andExpect(content().string(containsString(
                        "quiz-part-circuit-step--completed"
                )))
                .andExpect(content().string(not(containsString("正解です！"))))
                .andExpect(content().string(containsString("アクセス修飾子")))
                .andExpect(content().string(not(containsString("quiz-reading-step-heading"))))
                .andExpect(content().string(not(containsString("次の問題へ"))))
                .andExpect(content().string(containsString("data-answer-enabled=\"true\"")));
    }

    @Test
    void 更新後は完了説明中次操作未到達の四状態を復元する() throws Exception {
        MockHttpSession session = new MockHttpSession();
        courseService.startLearning(session, LESSON_ID);
        courseService.answerCircuitStep(
                session, LESSON_ID, "class-public", "accessible"
        );
        courseService.answerCircuitStep(
                session, LESSON_ID, "class-keyword", "declare-class"
        );

        mockMvc.perform(get("/quiz").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(
                        "quiz-circuit-code-button--completed"
                )))
                .andExpect(content().string(containsString(
                        "quiz-circuit-code-button--explaining"
                )))
                .andExpect(content().string(containsString(
                        "quiz-circuit-code-button--next"
                )))
                .andExpect(content().string(containsString(
                        "quiz-circuit-code-button--locked"
                )))
                .andExpect(content().string(containsString(
                        "aria-current=\"step\""
                )))
                .andExpect(content().string(containsString(
                        "aria-pressed=\"true\""
                )))
                .andExpect(content().string(not(containsString("次の問題へ"))));
    }

    @Test
    void 不正解では同じstepに留まる() throws Exception {
        MockHttpSession session = new MockHttpSession();
        courseService.startLearning(session, LESSON_ID);

        mockMvc.perform(post("/quiz/answer")
                        .session(session)
                        .param("selectedOption", "no-return"))
                .andExpect(status().is3xxRedirection());

        LessonProgress progress = progressService.getProgress(session, LESSON_ID);
        assertEquals("class-public", progress.getCurrentStepId());
        assertTrue(progress.isAnswered());
        assertFalse(progress.isCorrect());
    }

    @Test
    void Part最後の正解後は同じPartに留まる() {
        MockHttpSession session = new MockHttpSession();
        completePart1(session);

        LessonProgress progress = progressService.getProgress(session, LESSON_ID);
        assertEquals("class-open", progress.getCurrentStepId());
        assertTrue(progress.isStepCompleted("class-open"));
        assertTrue(progress.isAnswered());
        assertTrue(progress.isCorrect());
    }

    @Test
    void 完了したPartだけ次のPartへ進める() throws Exception {
        MockHttpSession session = new MockHttpSession();
        completePart1(session);

        mockMvc.perform(post("/quiz/part/next").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/quiz"));

        assertEquals(
                "main-public",
                progressService.getProgress(session, LESSON_ID)
                        .getCurrentStepId()
        );
    }

    @Test
    void 未完了では次Partへ進まない() throws Exception {
        MockHttpSession session = new MockHttpSession();
        courseService.startLearning(session, LESSON_ID);

        mockMvc.perform(post("/quiz/part/next").session(session))
                .andExpect(status().is3xxRedirection());

        assertEquals(
                "class-public",
                progressService.getProgress(session, LESSON_ID)
                        .getCurrentStepId()
        );
    }

    @Test
    void 最終Part完了後に次のコードへ進むとまとめ画面になる() throws Exception {
        MockHttpSession session = new MockHttpSession();
        completeAllParts(session);

        assertEquals(
                "class-close",
                progressService.getProgress(session, LESSON_ID)
                        .getCurrentStepId()
        );

        mockMvc.perform(post("/quiz/part/next").session(session))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get("/quiz").session(session))
                .andExpect(status().isOk())
                .andExpect(model().attribute("phase", CodeReadingPhase.SUMMARY))
                .andExpect(content().string(not(containsString(">SUMMARY<"))))
                .andExpect(content().string(not(containsString("今日読めるようになったコード"))))
                .andExpect(content().string(containsString("Helloと表示して改行する")));
    }

    @Test
    void 更新後もPart完了待機状態をセッションから復元する() throws Exception {
        MockHttpSession session = new MockHttpSession();
        completePart1(session);

        mockMvc.perform(get("/quiz").session(session))
                .andExpect(status().isOk())
                .andExpect(model().attribute("phase", CodeReadingPhase.LEARNING))
                .andExpect(content().string(containsString("public</code>")))
                .andExpect(content().string(containsString("ほかの場所からも使える")))
                .andExpect(content().string(containsString("次のコードへ")))
                .andExpect(content().string(not(containsString(
                        "MainはJavaの固定名ではなく"
                ))))
                .andExpect(content().string(not(containsString(
                        "今回は分かりやすくMain"
                ))));
    }

    @Test
    void Part2以降もPart1と同じレイアウトと正解後停止を使用する() throws Exception {
        MockHttpSession session = new MockHttpSession();
        completePart1(session);
        courseService.moveToNextPart(session, LESSON_ID);

        mockMvc.perform(get("/quiz").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(
                        "quiz-reading-code-structure"
                )))
                .andExpect(content().string(containsString(
                        "data-code-step=\"main-public\""
                )))
                .andExpect(content().string(containsString(
                        "data-quiz-answer-form=\"true\""
                )))
                .andExpect(content().string(not(containsString("今回読むコード"))))
                .andExpect(content().string(containsString("data-circuit-group=\"main-method\"")))
                .andExpect(content().string(not(containsString("data-circuit-group=\"class-declaration\""))))
                .andExpect(content().string(not(containsString("QUESTION 1 / 7"))))
                .andExpect(content().string(not(containsString("quiz-reading-step-heading"))))
                .andExpect(content().string(not(containsString("public の意味はどれですか？"))))
                .andExpect(content().string(not(containsString("現在の用語："))))
                .andExpect(content().string(containsString("data-part-complete")));

        courseService.answerCurrentItem(session, LESSON_ID, "accessible");
        LessonProgress progress = progressService.getProgress(session, LESSON_ID);
        assertEquals("main-public", progress.getCurrentStepId());
        assertTrue(progress.isAnswered());
        assertTrue(progress.isCorrect());

        mockMvc.perform(get("/quiz").session(session))
                .andExpect(content().string(not(containsString("正解です！"))))
                .andExpect(content().string(containsString("アクセス修飾子")))
                .andExpect(content().string(not(containsString("quiz-reading-step-heading"))))
                .andExpect(content().string(not(containsString("次の問題へ"))))
                .andExpect(content().string(containsString("quiz-part-circuit-step--completed")))
                .andExpect(content().string(containsString("ほかの場所からも使える")))
                .andExpect(content().string(containsString("data-part-complete")));
    }

    @Test
    void Part2は七問で不正解では進まず正解ごとに点灯する() throws Exception {
        MockHttpSession session = new MockHttpSession();
        completePart1(session);
        courseService.moveToNextPart(session, LESSON_ID);

        mockMvc.perform(post("/quiz/answer/interactive")
                        .session(session)
                        .param("targetStepId", "main-public")
                        .param("selectedOption", "no-return"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correct").value(false))
                .andExpect(jsonPath("$.completedCount").value(0))
                .andExpect(jsonPath("$.partCompleted").value(false));

        LessonProgress afterIncorrect =
                progressService.getProgress(session, LESSON_ID);
        assertFalse(afterIncorrect.isStepCompleted("main-public"));

        mockMvc.perform(get("/quiz").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("data-part-complete")));

        mockMvc.perform(post("/quiz/answer/interactive")
                        .session(session)
                        .param("targetStepId", "main-public")
                        .param(
                                "selectedOption",
                                "accessible"
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correct").value(true))
                .andExpect(jsonPath("$.completedCount").value(1))
                .andExpect(jsonPath("$.partCompleted").value(false));

        courseService.moveToNextItem(session, LESSON_ID);
        mockMvc.perform(post("/quiz/answer/interactive")
                        .session(session)
                        .param("targetStepId", "static")
                        .param("selectedOption", "without-instance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correct").value(true))
                .andExpect(jsonPath("$.completedCount").value(2))
                .andExpect(jsonPath("$.partCompleted").value(false));

        mockMvc.perform(get("/quiz").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(
                        "data-bulb-step=\"main-public\""
                )))
                .andExpect(content().string(containsString(
                        "quiz-part-circuit-step--completed"
                )))
                .andExpect(content().string(containsString(
                        "quiz-reading-code-meaning--visible"
                )));

        courseService.moveToNextItem(session, LESSON_ID);
        answerAll(
                session,
                "no-return", "program-entry", "multiple-strings",
                "argument-variable", "block-start"
        );
        assertEquals(
                7,
                progressService.getProgress(session, LESSON_ID)
                        .getCompletedStepIds().stream()
                        .filter(serviceStep -> serviceStep.equals("main-public")
                                || serviceStep.equals("static")
                                || serviceStep.equals("void")
                                || serviceStep.equals("main")
                                || serviceStep.equals("string-array")
                                || serviceStep.equals("args")
                                || serviceStep.equals("main-open"))
                        .count()
        );
    }

    @Test
    void Part3の完了画面に括弧とドットの補足を表示する() throws Exception {
        MockHttpSession session = new MockHttpSession();
        completePart1(session);
        courseService.moveToNextPart(session, LESSON_ID);
        completePart2(session);
        courseService.moveToNextPart(session, LESSON_ID);
        answerAll(session, "display-and-newline", "display-text", "command-end");

        mockMvc.perform(get("/quiz").session(session))
                .andExpect(content().string(containsString("System.out.println(...) は、かっこの中の内容を画面へ表示して改行します。")))
                .andExpect(content().string(containsString(". は、左側のものが持つ機能へ順番につなぐ記号です。")));
    }

    @Test
    void Part4の同じ閉じ括弧をstepIdで別々に配置して復元する() throws Exception {
        MockHttpSession session = new MockHttpSession();
        completePart1(session);
        courseService.moveToNextPart(session, LESSON_ID);
        completePart2(session);
        courseService.moveToNextPart(session, LESSON_ID);
        answerAll(session, "display-and-newline", "display-text", "command-end");
        courseService.moveToNextPart(session, LESSON_ID);
        courseService.answerCurrentItem(session, LESSON_ID, "close-main");

        mockMvc.perform(get("/quiz").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(
                        "data-step-id=\"main-close\""
                )))
                .andExpect(content().string(containsString(
                        "data-code-step=\"main-close\""
                )))
                .andExpect(content().string(containsString(
                        "data-code-step=\"class-close\""
                )))
                .andExpect(content().string(containsString(
                        "mainメソッド終了"
                )))
                .andExpect(content().string(containsString(
                        "quiz-part-circuit-step--completed"
                )));
    }

    @Test
    void まとめ画面でも導入と同じIDE風完成コードを表示する() throws Exception {
        MockHttpSession session = new MockHttpSession();
        completeAllParts(session);
        courseService.moveToNextPart(session, LESSON_ID);

        mockMvc.perform(get("/quiz").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("quiz-ide-window")))
                .andExpect(content().string(containsString("Main.java")))
                .andExpect(content().string(containsString("System.out.println(\"Hello\");")))
                .andExpect(content().string(not(containsString(">SUMMARY<"))))
                .andExpect(content().string(not(containsString("今日読めるようになったコード"))))
                .andExpect(content().string(containsString("quiz-summary-development-flow")))
                .andExpect(content().string(containsString("▶ Run")))
                .andExpect(content().string(containsString("プログラムを実行します")))
                .andExpect(content().string(not(containsString("data-summary-terminal"))));
    }

    @Test
    void Run成功後は段階表示用TERMINAL構造を表示する() throws Exception {
        MockHttpSession session = new MockHttpSession();
        completeAllParts(session);
        courseService.moveToNextPart(session, LESSON_ID);

        mockMvc.perform(post("/quiz/run").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("data-summary-process")))
                .andExpect(content().string(containsString("data-process-primary")))
                .andExpect(content().string(containsString("data-summary-terminal")))
                .andExpect(content().string(containsString("data-terminal-primary")))
                .andExpect(content().string(not(containsString("data-terminal-explanation"))))
                .andExpect(content().string(containsString("data-run-complete")))
                .andExpect(content().string(containsString("data-console-output=\"Hello\"")))
                .andExpect(content().string(containsString("プログラムが正常に実行されました")))
                .andExpect(content().string(not(containsString("▶ Run"))))
                .andExpect(content().string(containsString("/js/quiz-summary-run.js")));
    }

    @Test
    void Stage1完了画面は再挑戦とStage2への2操作だけを表示する() throws Exception {
        MockHttpSession session = new MockHttpSession();
        flowService.showSummary(session, LESSON_ID);

        mockMvc.perform(get("/quiz").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("このStageをもう一度")))
                .andExpect(content().string(containsString("Stage 2へ →")))
                .andExpect(content().string(not(containsString("Stage 1からやり直す"))))
                .andExpect(content().string(not(containsString("最初からやり直す"))));
    }

    @Test
    void Stage2完了画面は再挑戦とStage3への遷移とStage1からの再開を表示する() throws Exception {
        MockHttpSession session = new MockHttpSession();
        flowService.showSummary(
                session,
                CodeReadingLessonCatalog.STAGE2_LESSON_ID
        );

        mockMvc.perform(get("/quiz")
                        .param(
                                "lessonId",
                                CodeReadingLessonCatalog.STAGE2_LESSON_ID
                        )
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("このStageをもう一度")))
                .andExpect(content().string(containsString("Stage 1からやり直す")))
                .andExpect(content().string(containsString("Stage 3へ →")))
                .andExpect(content().string(not(containsString("最初からやり直す"))));
    }

    @Test
    void Stage3完了画面は存在しないStage4への遷移を表示しない() throws Exception {
        MockHttpSession session = new MockHttpSession();
        flowService.showSummary(
                session,
                CodeReadingLessonCatalog.STAGE3_LESSON_ID
        );

        mockMvc.perform(get("/quiz")
                        .param("lessonId", CodeReadingLessonCatalog.STAGE3_LESSON_ID)
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("このStageをもう一度")))
                .andExpect(content().string(containsString("Stage 1からやり直す")))
                .andExpect(content().string(not(containsString("Stage 4へ →"))));
    }

    @Test
    void Stage3をもう一度はStage1とStage2の進捗を維持する() throws Exception {
        MockHttpSession session = new MockHttpSession();
        courseService.startLearning(session, LESSON_ID);
        courseService.startLearning(
                session,
                CodeReadingLessonCatalog.STAGE2_LESSON_ID
        );
        courseService.startLearning(
                session,
                CodeReadingLessonCatalog.STAGE3_LESSON_ID
        );

        mockMvc.perform(post("/quiz/reset")
                        .param(
                                "lessonId",
                                CodeReadingLessonCatalog.STAGE3_LESSON_ID
                        )
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(
                        "/quiz?lessonId="
                                + CodeReadingLessonCatalog.STAGE3_LESSON_ID
                ));

        assertEquals(
                CodeReadingPhase.LEARNING,
                flowService.getState(session, LESSON_ID).phase()
        );
        assertEquals(
                CodeReadingPhase.LEARNING,
                flowService.getState(
                        session,
                        CodeReadingLessonCatalog.STAGE2_LESSON_ID
                ).phase()
        );
        assertEquals(
                CodeReadingPhase.INTRO,
                flowService.getState(
                        session,
                        CodeReadingLessonCatalog.STAGE3_LESSON_ID
                ).phase()
        );
    }

    @Test
    void Stage1からやり直すは全Stageを初期化してStage1へ戻る() throws Exception {
        MockHttpSession session = new MockHttpSession();
        courseService.startLearning(session, LESSON_ID);
        courseService.startLearning(
                session,
                CodeReadingLessonCatalog.STAGE2_LESSON_ID
        );
        courseService.startLearning(
                session,
                CodeReadingLessonCatalog.STAGE3_LESSON_ID
        );

        mockMvc.perform(post("/quiz/reset-all").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/quiz"));

        assertEquals(
                CodeReadingPhase.INTRO,
                flowService.getState(session, LESSON_ID).phase()
        );
        assertEquals(
                CodeReadingPhase.INTRO,
                flowService.getState(
                        session,
                        CodeReadingLessonCatalog.STAGE2_LESSON_ID
                ).phase()
        );
        assertEquals(
                CodeReadingPhase.INTRO,
                flowService.getState(
                        session,
                        CodeReadingLessonCatalog.STAGE3_LESSON_ID
                ).phase()
        );
    }

    @Test
    void リセットで問題進捗と画面フェーズが導入へ戻る() throws Exception {
        MockHttpSession session = new MockHttpSession();
        completePart1(session);

        mockMvc.perform(post("/quiz/reset").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/quiz"));

        mockMvc.perform(get("/quiz").session(session))
                .andExpect(model().attribute("phase", CodeReadingPhase.INTRO));
        LessonProgress progress = progressService.getProgress(session, LESSON_ID);
        assertEquals("class-public", progress.getCurrentStepId());
        assertTrue(progress.getCompletedStepIds().isEmpty());
    }

    @Test
    void 旧stepを含むセッションは安全に導入へ戻る() throws Exception {
        MockHttpSession session = new MockHttpSession();
        LessonProgress oldProgress = new LessonProgress(
                LESSON_ID,
                "main-method-declaration"
        );
        oldProgress.completeStep("main-method-declaration");
        Map<String, LessonProgress> progressMap = new LinkedHashMap<>();
        progressMap.put(LESSON_ID, oldProgress);
        session.setAttribute(
                LessonProgressService.LESSON_PROGRESS_MAP,
                progressMap
        );
        Map<String, CodeReadingFlowState> flowMap = new LinkedHashMap<>();
        flowMap.put(
                LESSON_ID,
                new CodeReadingFlowState(LESSON_ID, CodeReadingPhase.LEARNING)
        );
        session.setAttribute(CodeReadingFlowService.CODE_READING_FLOW_MAP, flowMap);

        mockMvc.perform(get("/quiz").session(session))
                .andExpect(status().isOk())
                .andExpect(model().attribute("phase", CodeReadingPhase.INTRO))
                .andExpect(content().string(containsString("スタート")));

        assertEquals(
                "class-public",
                progressService.getProgress(session, LESSON_ID)
                        .getCurrentStepId()
        );
    }

    private void completePart1(MockHttpSession session) {
        courseService.startLearning(session, LESSON_ID);
        courseService.answerCurrentItem(session, LESSON_ID, "accessible");
        courseService.moveToNextItem(session, LESSON_ID);
        courseService.answerCurrentItem(session, LESSON_ID, "declare-class");
        courseService.moveToNextItem(session, LESSON_ID);
        courseService.answerCurrentItem(session, LESSON_ID, "main-class-name");
        courseService.moveToNextItem(session, LESSON_ID);
        courseService.answerCurrentItem(session, LESSON_ID, "block-start");
    }

    private void completeAllParts(MockHttpSession session) {
        completePart1(session);
        courseService.moveToNextPart(session, LESSON_ID);

        completePart2(session);
        courseService.moveToNextPart(session, LESSON_ID);

        answerAll(session, "display-and-newline", "display-text", "command-end");
        courseService.moveToNextPart(session, LESSON_ID);

        answerAll(session, "close-main", "close-class");
    }

    private void completePart2(MockHttpSession session) {
        answerAll(
                session,
                "accessible",
                "without-instance",
                "no-return",
                "program-entry",
                "multiple-strings",
                "argument-variable",
                "block-start"
        );
    }

    private void answerAll(MockHttpSession session, String... optionIds) {
        for (String optionId : optionIds) {
            courseService.answerCurrentItem(session, LESSON_ID, optionId);
            courseService.moveToNextItem(session, LESSON_ID);
        }
    }
}
