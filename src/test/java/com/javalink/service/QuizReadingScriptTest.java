package com.javalink.service;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuizReadingScriptTest {

    @Test
    void 正解時はカードを複製せずstepIdで電球を点灯する() throws IOException {
        String script = readScript();

        assertFalse(script.contains("cloneNode"));
        assertFalse(script.contains("flyCard"));
        assertFalse(script.contains("quiz-reading-flying-card"));
        assertFalse(script.contains("switchOn"));
        assertTrue(script.contains("data-bulb-step"));
        assertTrue(script.contains("data-current-meaning-slot"));
        assertTrue(script.contains("quiz-part-circuit-step--lighting"));
        assertTrue(script.contains("result.nextStepId"));
        assertTrue(script.contains("dataset.answerEnabled"));
        assertTrue(script.contains("quiz-circuit-code-button--explaining"));
        assertTrue(script.contains("quiz-circuit-code-button--next"));
        assertTrue(script.contains("quiz-circuit-code-button--locked"));
        assertTrue(script.contains("aria-current"));
        assertFalse(script.contains("window.location.reload"));
    }

    @Test
    void 配線通電処理は存在しない() throws IOException {
        String script = readScript();

        assertFalse(script.contains("quiz-code-circuit-connector--energizing"));
        assertFalse(script.contains("quiz-part-circuit-step--power-entry"));
        assertFalse(script.contains("energizeCircuit"));
    }

    @Test
    void 構造化された説明をサーバー応答から描画する() throws IOException {
        String script = readScript();

        assertTrue(script.contains("result.explanationSections"));
        assertTrue(script.contains("section.sectionType"));
        assertTrue(script.contains("section.sectionType || \"text\""));
        assertTrue(script.contains("dataset.sectionType"));
        assertTrue(script.contains("createOfficialReferences"));
        assertTrue(script.contains("section.officialReferences"));
        assertTrue(script.contains("link.target = \"_blank\""));
        assertTrue(script.contains("link.rel = \"noopener noreferrer\""));
        assertTrue(script.contains("reference.description"));
        assertTrue(script.contains("${reference.sourceName} ${reference.sectionNumber} — ${reference.sectionTitle}"));
        assertFalse(script.contains("innerHTML"));
        assertFalse(script.contains("section.layout"));
        assertFalse(script.contains("dataset.sectionLayout"));
        assertFalse(script.contains("result.technicalExplanation"));
        assertFalse(script.contains("result.beginnerExplanations"));
        assertFalse(script.contains("data-explanation-technical"));
        assertFalse(script.contains("data-explanation-beginner"));
        assertTrue(script.contains("createExplanationSection"));
        assertTrue(script.contains("createAccessTable"));
        assertTrue(script.contains("quiz-reading-access-row--header"));
        assertTrue(script.contains("section.tableHeader === true"));
        assertTrue(script.contains("createDiagram"));
        assertTrue(script.contains("createQa"));

        String template = readTemplate();
        assertTrue(template.contains("data-section-type"));
        assertFalse(template.contains("data-section-layout"));
        assertTrue(template.contains("section.sectionType.value != 'table'"));
        assertTrue(template.contains("section.sectionType.value == 'official-references'"));
        assertTrue(template.contains("target=\"_blank\""));
        assertTrue(template.contains("rel=\"noopener noreferrer\""));
        assertTrue(template.contains("reference.description"));
        assertTrue(template.contains("${reference.sourceName} ${reference.sectionNumber} — ${reference.sectionTitle}"));
        assertTrue(template.contains("quiz-reading-access-row--header"));
        assertTrue(template.contains("(section.tableHeader and entryStat.first) or entry.tableHeader"));
        assertTrue(script.contains("entry.tableHeader === true"));
        assertTrue(template.contains("style.css(v='stage3-reading-6')"));
        assertTrue(template.contains("quiz-reading.js(v='stage3-reading-6')"));
        assertFalse(template.contains("continuous-circuit-1"));
    }

    @Test
    void 共通Explanationのtable表示はStage2限定CSSに依存しない() throws IOException {
        String style = readStyle();

        assertFalse(style.contains("[data-lesson-id=\"variable-program-reading\"]\n"
                + "    .quiz-reading-learning-aid[data-section-title=\"代入と初期化の違い\"]"));
        assertFalse(style.contains("[data-lesson-id=\"variable-program-reading\"]\n"
                + "    .quiz-reading-learning-aid-layout--table:is("));
        assertFalse(style.contains("[data-lesson-id=\"variable-program-reading\"]\n"
                + "    .quiz-reading-learning-aid[data-section-title=\"変数を「宣言する」と「使う」の違い\"]"));
        assertTrue(style.contains(".quiz-focus-learning\n"
                + "    .quiz-reading-learning-aid[data-section-title=\"代入と初期化の違い\"]"));
        assertTrue(style.contains(".quiz-focus-learning\n"
                + "    .quiz-reading-learning-aid[data-section-title=\"変数を「宣言する」と「使う」の違い\"]"));
    }

    private String readScript() throws IOException {
        try (InputStream stream = getClass().getResourceAsStream(
                "/static/js/quiz-reading.js"
        )) {
            assertNotNull(stream);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private String readTemplate() throws IOException {
        try (InputStream stream = getClass().getResourceAsStream(
                "/templates/quiz.html"
        )) {
            assertNotNull(stream);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private String readStyle() throws IOException {
        try (InputStream stream = getClass().getResourceAsStream(
                "/static/css/style.css"
        )) {
            assertNotNull(stream);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8)
                    .replace("\r\n", "\n")
                    .replace('\r', '\n');
        }
    }
}
