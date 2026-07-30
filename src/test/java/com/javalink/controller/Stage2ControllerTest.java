package com.javalink.controller;

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

@WebMvcTest(Stage2Controller.class)
class Stage2ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void stage2を正常に表示できる() throws Exception {
        mockMvc.perform(get("/stage2"))
                .andExpect(status().isOk())
                .andExpect(view().name("stage2"))
                .andExpect(content().string(containsString("Stage 2")))
                .andExpect(content().string(containsString("href=\"/stage3\"")))
                .andExpect(content().string(containsString("mainメソッドを完成させよう")));
    }

    @Test
    void 必要なカードと正解順序を表示できる() throws Exception {
        mockMvc.perform(get("/stage2"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("data-code=\"public\"")))
                .andExpect(content().string(containsString("data-code=\"static\"")))
                .andExpect(content().string(containsString("data-code=\"void\"")))
                .andExpect(content().string(containsString("data-code=\"main\"")))
                .andExpect(content().string(containsString("data-code=\"(String[] args)\"")))
                .andExpect(content().string(containsString("data-code=\"{\"")))
                .andExpect(content().string(containsString("data-code=\"}\"")))
                .andExpect(content().string(containsString(
                        "correctOrder = [\"public\", \"static\", \"void\", \"main\", \"(String[] args)\", \"{\", \"}\"]")));
    }

    @Test
    void カードを1枚ずつ即時判定する処理を持つ() throws Exception {
        mockMvc.perform(get("/stage2"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("<h3>回答</h3>"))))
                .andExpect(content().string(not(containsString("id=\"selected-code\""))))
                .andExpect(content().string(not(containsString("id=\"stage-check-button\""))))
                .andExpect(content().string(containsString("id=\"code-cursor\"")))
                .andExpect(content().string(containsString("const expectedCode = correctOrder[selectedCodes.length]")))
                .andExpect(content().string(containsString("card.dataset.code !== expectedCode")))
                .andExpect(content().string(containsString("animateCard(card)")))
                .andExpect(content().string(containsString("addFixedCard(card)")))
                .andExpect(content().string(containsString("document.createElement(\"span\")")))
                .andExpect(content().string(containsString("stage2-fixed-card")))
                .andExpect(content().string(containsString("card.getBoundingClientRect()")))
                .andExpect(content().string(containsString("codeCursor.getBoundingClientRect()")))
                .andExpect(content().string(not(containsString("assembledCodeArea.textContent = \"public static void main"))))
                .andExpect(content().string(containsString("正解！")))
                .andExpect(content().string(containsString("そのカードではありません。もう一度選んでみよう！")))
                .andExpect(content().string(containsString("showIncorrectCard(card)")))
                .andExpect(content().string(containsString("item.disabled = true")))
                .andExpect(content().string(containsString("card.disabled = false")))
                .andExpect(content().string(containsString("resetButton.addEventListener")))
                .andExpect(content().string(containsString("shuffleCards()")));
    }

    @Test
    void やり直しで上部コード欄を初期状態へ戻す() throws Exception {
        mockMvc.perform(get("/stage2"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("methodCards.replaceChildren()")))
                .andExpect(content().string(containsString("closingCards.replaceChildren()")))
                .andExpect(content().string(containsString("methodCardLine.appendChild(codeCursor)")))
                .andExpect(content().string(containsString("animationId++")))
                .andExpect(content().string(containsString("stage-flying-card")));
    }
}
