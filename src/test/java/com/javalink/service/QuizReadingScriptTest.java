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

    private String readScript() throws IOException {
        try (InputStream stream = getClass().getResourceAsStream(
                "/static/js/quiz-reading.js"
        )) {
            assertNotNull(stream);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
