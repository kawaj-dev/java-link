package com.javalink.service;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuizSummaryRunScriptTest {

    @Test
    void コンパイル実行結果完了を順番に表示する() throws IOException {
        String script = readScript();

        assertTrue(script.contains("Compiling..."));
        assertTrue(script.contains("Running..."));
        assertTrue(script.contains("data.consoleOutput")
                || script.contains("dataset.consoleOutput"));
        assertTrue(script.contains("プログラムが正常に実行されました")
                || script.contains("data-run-complete"));
        assertTrue(script.contains("prefers-reduced-motion: reduce"));
        assertTrue(script.contains("aria-busy"));
        assertTrue(script.contains("data-summary-process"));
        assertTrue(script.contains("processPanel.hidden = false"));
        assertTrue(script.contains("processPanel.hidden = true"));
        assertTrue(script.contains("terminal.hidden = true"));
        assertTrue(script.contains("terminal.hidden = false"));
        assertTrue(script.contains("reducedMotion ? 450 : 1400"));
        assertTrue(script.contains("reducedMotion ? 450 : 1200"));
        assertTrue(script.contains("reducedMotion ? 200 : 600"));
    }

    private String readScript() throws IOException {
        try (InputStream stream = getClass().getResourceAsStream(
                "/static/js/quiz-summary-run.js"
        )) {
            assertNotNull(stream);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
