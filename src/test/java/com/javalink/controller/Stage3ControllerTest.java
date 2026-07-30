package com.javalink.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@WebMvcTest(Stage3Controller.class)
class Stage3ControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void stage3を表示できる() throws Exception {
        mockMvc.perform(get("/stage3"))
                .andExpect(status().isOk())
                .andExpect(view().name("stage3"))
                .andExpect(content().string(containsString("Stage 3")))
                .andExpect(content().string(containsString("画面に文字を表示しよう")));
    }

    @Test
    void Javaことば図鑑を開閉できる構造を持つ() throws Exception {
        mockMvc.perform(get("/stage3"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("📖 Javaことば図鑑")))
                .andExpect(content().string(containsString("id=\"java-glossary-panel\"")))
                .andExpect(content().string(containsString("id=\"java-glossary-overlay\"")))
                .andExpect(content().string(containsString(
                        "id=\"java-glossary-search\"")))
                .andExpect(content().string(containsString(
                        "placeholder=\"用語を検索\"")))
                .andExpect(content().string(containsString(
                        "id=\"java-glossary-no-results\"")))
                .andExpect(content().string(containsString(
                        "該当する用語がありません")))
                .andExpect(content().string(containsString("📐 クラス")))
                .andExpect(content().string(containsString("🏷 フィールド")))
                .andExpect(content().string(containsString("⚙ メソッド")))
                .andExpect(content().string(containsString("🌍 public")))
                .andExpect(content().string(containsString("🔒 private")))
                .andExpect(content().string(containsString("📥 getter")))
                .andExpect(content().string(containsString("✏ setter")))
                .andExpect(content().string(containsString(
                        "overlay.addEventListener(\"click\", closeGlossary)")))
                .andExpect(content().string(containsString(
                        "closeButton.addEventListener(\"click\", closeGlossary)")))
                .andExpect(content().string(containsString(
                        "searchInput.addEventListener(\"input\", () => updateSearchResults())")))
                .andExpect(content().string(containsString(
                        "cardText.includes(keyword)")));
    }

    @Test
    void 三枚のカードと即時判定を持つ() throws Exception {
        mockMvc.perform(get("/stage3"))
                .andExpect(content().string(containsString(
                        "<span class=\"stage3-syntax-keyword\">public</span> class WelcomeSign {")))
                .andExpect(content().string(containsString(
                        "<span class=\"stage3-syntax-keyword\">public static void</span> main(String[] args) {")))
                .andExpect(content().string(not(containsString(
                        "stage-card-keyword stage2-fixed-card\">public"))))
                .andExpect(content().string(not(containsString(
                        "stage-card-symbol stage2-fixed-card\">}"))))
                .andExpect(content().string(containsString("data-code=\"System.out.println\"")))
                .andExpect(content().string(containsString("data-code=\"(&quot;Welcome!&quot;)\"")))
                .andExpect(content().string(containsString("data-code=\";\"")))
                .andExpect(content().string(containsString(
                        "id=\"stage3-output-cards\" class=\"stage2-fixed-card-list\"")))
                .andExpect(content().string(containsString(
                        "id=\"stage3-code-cursor\" class=\"stage2-code-cursor\"")))
                .andExpect(content().string(containsString("stage3CorrectOrder")))
                .andExpect(content().string(containsString("stage3CorrectOrder[stage3SelectedCodes.length]")))
                .andExpect(content().string(containsString("flyStage3Card(card)")))
                .andExpect(content().string(containsString("showStage3Error(card)")));
    }

    @Test
    void 完成コードと実行結果とリセット処理を持つ() throws Exception {
        mockMvc.perform(get("/stage3"))
                .andExpect(content().string(containsString("stage3-fixed-card")))
                .andExpect(content().string(containsString("✨ コード完成！")))
                .andExpect(content().string(containsString("stage3-cards-complete")))
                .andExpect(content().string(containsString("stage3-fade-out")))
                .andExpect(content().string(containsString("stage3-fade-in")))
                .andExpect(content().string(containsString("System.out.println(\"Welcome!\");")))
                .andExpect(content().string(containsString(
                        "hidden style=\"display: none;\">System.out.println(\"Welcome!\");")))
                .andExpect(content().string(containsString(
                        "stage3JavaCode.style.display = \"\"")))
                .andExpect(content().string(containsString(
                        "id=\"stage3-run\" class=\"stage-run-button\" type=\"button\"")))
                .andExpect(content().string(containsString(
                        "disabled aria-disabled=\"true\">▶ 実行する")))
                .andExpect(content().string(not(containsString("プログラムが完成しました。"))))
                .andExpect(content().string(not(containsString("Javaタウンを動かしてみよう！"))))
                .andExpect(content().string(not(containsString("表示する命令が完成しました。"))))
                .andExpect(content().string(not(containsString("プログラムのスタート地点ができました。"))))
                .andExpect(content().string(containsString("id=\"stage3-power-link\"")))
                .andExpect(content().string(not(containsString(
                        "id=\"stage3-center-power-area\""))))
                .andExpect(content().string(containsString(
                        "class=\"stage3-code-status-flow\"")))
                .andExpect(content().string(containsString(
                        "id=\"stage3-incomplete-status\"")))
                .andExpect(content().string(containsString(
                        "id=\"stage3-complete-status\"")))
                .andExpect(content().string(containsString("コード未完成")))
                .andExpect(content().string(containsString(
                        "class=\"stage-actions stage3-editor-actions\"")))
                .andExpect(content().string(containsString("id=\"stage3-power-line\"")))
                .andExpect(content().string(containsString("id=\"stage3-power-flow\"")))
                .andExpect(content().string(not(containsString(
                        "id=\"stage3-power-path\""))))
                .andExpect(content().string(containsString(
                        "line.setAttribute(\"x1\", x1)")))
                .andExpect(content().string(containsString(
                        "line.setAttribute(\"y1\", y1)")))
                .andExpect(content().string(containsString(
                        "line.setAttribute(\"x2\", x2)")))
                .andExpect(content().string(containsString(
                        "line.setAttribute(\"y2\", y2)")))
                .andExpect(content().string(containsString("connectStage3Power(version)")))
                .andExpect(content().string(containsString("stage3Run.disabled = !enabled")))
                .andExpect(content().string(containsString(
                        "stage3Run.setAttribute(\"aria-disabled\", String(!enabled))")))
                .andExpect(content().string(containsString("Lesson 1 Complete!")))
                .andExpect(content().string(containsString("Welcome Signに明かりが灯りました！")))
                .andExpect(content().string(containsString("stage3OutputCards.replaceChildren()")))
                .andExpect(content().string(containsString("stage3CardCode.hidden = false")))
                .andExpect(content().string(containsString("stage3JavaCode.hidden = true")))
                .andExpect(content().string(containsString(
                        "stage3JavaCode.style.display = \"none\"")))
                .andExpect(content().string(containsString("setStage3CodeCompleted(false)")))
                .andExpect(content().string(containsString("window.clearTimeout(stage3RunTimer)")))
                .andExpect(content().string(containsString("shuffleStage3Cards()")));
    }
    @Test
    void 実行結果をコンソールからJavaタウンの順に表示する() throws Exception {
        String html = mockMvc.perform(get("/stage3"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(
                        "id=\"stage3-execution-results\" class=\"stage3-execution-results\" hidden")))
                .andExpect(content().string(containsString("💻 プログラム実行結果")))
                .andExpect(content().string(containsString("コンソール")))
                .andExpect(content().string(containsString("Welcome!")))
                .andExpect(content().string(containsString("🏡 Javaタウンの変化")))
                .andExpect(content().string(containsString("class=\"stage3-town-result\"")))
                .andExpect(content().string(containsString("class=\"town-building\"")))
                .andExpect(content().string(containsString("Lesson 1 Complete!")))
                .andExpect(content().string(containsString("Welcome Signに")))
                .andExpect(content().string(containsString("🏡 Java Linkトップへ戻る")))
                .andExpect(content().string(containsString("stage3ExecutionResults.hidden = false")))
                .andExpect(content().string(containsString("stage3ExecutionResults.hidden = true")))
                .andReturn().getResponse().getContentAsString();

        assertTrue(html.indexOf("id=\"stage3-console\"") < html.indexOf("id=\"stage3-complete\""));
    }

    @Test
    void PCでは実行結果とコード作成を二列に配置し狭い画面では一列にする() throws Exception {
        String html = mockMvc.perform(get("/stage3"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("class=\"stage3-workspace\"")))
                .andExpect(content().string(containsString(
                        "class=\"stage3-editor-pane\" aria-label=\"コード作成エリア\"")))
                .andExpect(content().string(containsString(
                        "class=\"stage3-output-pane\" aria-label=\"実行とJavaタウンエリア\"")))
                .andExpect(content().string(not(containsString(
                        "class=\"stage3-center-power-area\""))))
                .andExpect(content().string(not(containsString("id=\"stage3-output-guide\""))))
                .andExpect(content().string(not(containsString("プログラムが完成しました。"))))
                .andExpect(content().string(not(containsString("Javaタウンを動かしてみよう！"))))
                .andReturn().getResponse().getContentAsString();

        int editorStart = html.indexOf("class=\"stage3-editor-pane\"");
        int outputStart = html.indexOf("class=\"stage3-output-pane\"");
        assertTrue(editorStart < outputStart);
        assertTrue(editorStart < html.indexOf("id=\"stage3-code-block\""));
        assertTrue(outputStart < html.indexOf("id=\"stage3-result\""));
        assertTrue(outputStart < html.indexOf("id=\"stage3-run\""));
        assertTrue(html.indexOf("id=\"stage3-run\"")
                < html.indexOf("id=\"stage3-execution-results\""));
        assertTrue(outputStart < html.indexOf("id=\"stage3-console\""));
        assertTrue(outputStart < html.indexOf("id=\"stage3-complete\""));
        assertTrue(editorStart < html.indexOf("id=\"stage3-complete-status\""));
        assertTrue(html.indexOf("id=\"stage3-complete-status\"")
                < html.indexOf("id=\"stage3-code-heading\""));
        assertTrue(html.indexOf("id=\"stage3-incomplete-status\"")
                < html.indexOf("id=\"stage3-complete-status\""));
        assertTrue(editorStart < html.indexOf("href=\"/stage2\""));
        assertTrue(editorStart < html.indexOf("id=\"stage3-reset\""));

        try (InputStream input = getClass().getResourceAsStream("/static/css/style.css")) {
            assertNotNull(input);
            String css = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(css.contains(".stage3-workspace"));
            assertTrue(css.contains("width: min(1440px, calc(100vw - 48px))"));
            assertTrue(css.contains(
                    "grid-template-columns: minmax(0, 1fr) minmax(0, 1fr)"));
            assertTrue(!css.contains(".stage3-center-power-area"));
            assertTrue(css.contains(".stage3-editor-pane {\n    grid-column: 1;"));
            assertTrue(css.contains(".stage3-output-pane {\n    grid-column: 2;"));
            assertTrue(css.contains("grid-template-rows: 72px auto auto"));
            assertTrue(css.contains(".stage3-code-status-flow"));
            assertTrue(css.contains(".stage3-code-status--active"));
            assertTrue(css.contains(".stage3-code-status--inactive"));
            assertTrue(css.contains(".stage3-code-status-flow--connected"));
            assertTrue(css.contains("@media (max-width: 900px)"));
            assertTrue(css.contains("flex-direction: column"));
            assertTrue(css.contains(".stage3-editor-pane {\n        order: 1;"));
            assertTrue(css.contains(".stage3-output-pane {\n        order: 2;"));
            assertTrue(css.contains(".stage3-output-pane {\n        margin-top: 24px;"));
            assertTrue(css.contains(".stage3-power-link"));
            assertTrue(css.contains(".stage3-power-link--flowing"));
            assertTrue(css.contains(".stage3-power-link--connected"));
            assertTrue(css.contains(".stage3-power-line--base"));
            assertTrue(css.contains(".stage3-power-line--flow"));
            assertTrue(css.contains(".stage3-run-area .stage-run-button:disabled"));
            assertTrue(css.contains("cursor: not-allowed"));
            assertTrue(css.contains(
                    ".stage3-power-line--flow {\n        animation-duration: 0.7s;"));
        }
    }
}
