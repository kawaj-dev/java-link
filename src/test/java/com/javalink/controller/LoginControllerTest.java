package com.javalink.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Optional;

import com.javalink.entity.UserAccount;
import com.javalink.service.UserAuthenticationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(LoginController.class)
class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserAuthenticationService userAuthenticationService;

    @Test
    void ログイン画面に必要な入力欄と案内を表示する() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(content().string(containsString("メールアドレス")))
                .andExpect(content().string(containsString("type=\"email\"")))
                .andExpect(content().string(containsString("パスワード")))
                .andExpect(content().string(containsString("type=\"password\"")))
                .andExpect(content().string(containsString(">ログイン</button>")))
                .andExpect(content().string(containsString("href=\"/register\"")))
                .andExpect(content().string(containsString("アカウントを作成")));
    }

    @Test
    void ログイン成功時は入力値をServiceへ渡してトップ画面へ移動する() throws Exception {
        given(userAuthenticationService.authenticate(
                "learner@example.com",
                "password"))
                .willReturn(Optional.of(new UserAccount()));

        mockMvc.perform(post("/login")
                        .param("email", "learner@example.com")
                        .param("password", "password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(userAuthenticationService).authenticate(
                "learner@example.com",
                "password");
    }

    @Test
    void ログイン失敗時は共通エラーを設定してログイン画面を再表示する() throws Exception {
        given(userAuthenticationService.authenticate(
                "learner@example.com",
                "incorrect"))
                .willReturn(Optional.empty());

        mockMvc.perform(post("/login")
                        .param("email", "learner@example.com")
                        .param("password", "incorrect"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attribute(
                        "loginError",
                        "メールアドレスまたはパスワードが正しくありません。"));
    }
}
