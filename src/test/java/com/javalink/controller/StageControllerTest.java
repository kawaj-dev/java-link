package com.javalink.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;
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

@WebMvcTest(StageController.class)
class StageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void コード欄をカード欄より先に表示する() throws Exception {
        String html = mockMvc.perform(get("/stage1"))
                .andExpect(status().isOk())
                .andExpect(view().name("stage1"))
                .andExpect(content().string(containsString("id=\"stage1-code-block\"")))
                .andExpect(content().string(containsString("id=\"stage1-card-list\"")))
                .andExpect(content().string(not(containsString("id=\"selected-code\""))))
                .andExpect(content().string(not(containsString("id=\"stage-run-button\""))))
                .andReturn().getResponse().getContentAsString();

        assertTrue(html.indexOf("id=\"stage1-code-block\"")
                < html.indexOf("id=\"stage1-card-list\""));
    }

    @Test
    void カードを一枚ずつ即時判定する() throws Exception {
        mockMvc.perform(get("/stage1"))
                .andExpect(content().string(containsString(
                        "stage1CorrectOrder = [\"public\", \"class\", \"WelcomeSign\", \"{\", \"}\"]")))
                .andExpect(content().string(containsString(
                        "stage1CorrectOrder[stage1SelectedCodes.length]")))
                .andExpect(content().string(containsString("flyStage1Card(card)")))
                .andExpect(content().string(containsString("showStage1Error(card)")))
                .andExpect(content().string(containsString(
                        "そのカードではありません。もう一度選んでみよう！")))
                .andExpect(content().string(containsString("stage1-fixed-card")));
    }

    @Test
    void 完成コードとStage2リンクとリセット処理を持つ() throws Exception {
        mockMvc.perform(get("/stage1"))
                .andExpect(content().string(containsString("public class</span> WelcomeSign {")))
                .andExpect(content().string(containsString("✨ クラス完成！")))
                .andExpect(content().string(containsString("WelcomeSignクラスができました。")))
                .andExpect(content().string(containsString(
                        "次は、このクラスの中に<br>mainメソッドを作っていきます。")))
                .andExpect(content().string(not(containsString("✨ コード完成！"))))
                .andExpect(content().string(not(containsString("<strong>正解！</strong>"))))
                .andExpect(content().string(containsString("▶ Stage2へ進む")))
                .andExpect(content().string(containsString(
                        "id=\"stage2-link\" class=\"stage-run-button stage-next-button\" href=\"/stage2\" hidden")))
                .andExpect(content().string(containsString("stage1CodeCompleteMessage.hidden = true")))
                .andExpect(content().string(containsString("stage2Link.hidden = false")))
                .andExpect(content().string(containsString("stage2Link.hidden = true")))
                .andExpect(content().string(containsString("stage1FixedCards.replaceChildren()")))
                .andExpect(content().string(containsString("shuffleStage1Cards()")));
    }
}
