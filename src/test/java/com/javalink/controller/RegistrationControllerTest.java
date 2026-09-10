package com.javalink.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RegistrationController.class)
class RegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void アカウント作成画面に必要な入力欄と案内を表示する() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(content().string(containsString("表示名")))
                .andExpect(content().string(containsString("name=\"displayName\"")))
                .andExpect(content().string(containsString("メールアドレス")))
                .andExpect(content().string(containsString("type=\"email\"")))
                .andExpect(content().string(containsString("パスワード")))
                .andExpect(content().string(containsString("type=\"password\"")))
                .andExpect(content().string(containsString(">アカウントを作成</button>")))
                .andExpect(content().string(containsString("href=\"/login\"")))
                .andExpect(content().string(containsString("ログイン画面へ戻る")));
    }
}
