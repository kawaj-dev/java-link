package com.javalink.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.javalink.service.UserRegistrationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(RegistrationController.class)
class RegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRegistrationService userRegistrationService;

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

    @Test
    void 登録成功時は入力値をServiceへ渡してログイン画面へ移動する() throws Exception {
        mockMvc.perform(post("/register")
                        .param("displayName", "Java Learner")
                        .param("email", "learner@example.com")
                        .param("password", "password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(userRegistrationService).register(
                "Java Learner",
                "learner@example.com",
                "password");
    }

    @Test
    void メール重複時はエラーメッセージを設定して登録画面を再表示する() throws Exception {
        given(userRegistrationService.register(
                "Java Learner",
                "learner@example.com",
                "password"))
                .willThrow(new IllegalArgumentException("Email is already registered."));

        mockMvc.perform(post("/register")
                        .param("displayName", "Java Learner")
                        .param("email", "learner@example.com")
                        .param("password", "password"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attribute(
                        "registrationError",
                        "Email is already registered."));
    }
}
