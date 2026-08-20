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

@WebMvcTest(HomeController.class)
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void トップ画面にコードリーディングへの導線とコンセプトだけを表示する() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(content().string(containsString("href=\"/quiz\"")))
                .andExpect(content().string(not(containsString("href=\"/stage1\""))))
                .andExpect(content().string(not(containsString("Javaタウンをつくろう"))))
                .andExpect(content().string(not(containsString("home-menu-card--town"))))
                .andExpect(content().string(not(containsString("どちらの学習からでも自由に始められます。"))))
                .andExpect(content().string(containsString("src=\"/images/JavaLink_icon.png\"")))
                .andExpect(content().string(containsString("home-title-java\">Java")))
                .andExpect(content().string(containsString("home-title-link\">Link")))
                .andExpect(content().string(containsString("home-hero-circuit-ring")))
                .andExpect(content().string(containsString("home-hero-circuit-bulb")))
                .andExpect(content().string(containsString("home-side-circuit--yellow")))
                .andExpect(content().string(containsString("home-nav-icon--book")))
                .andExpect(content().string(containsString("home-nav-icon--help")))
                .andExpect(content().string(containsString("home-nav-icon--user")))
                .andExpect(content().string(containsString("home-card-icon--book")))
                .andExpect(content().string(containsString("コードを左から読む")))
                .andExpect(content().string(containsString("読んで、")))
                .andExpect(content().string(containsString("つなげて、")))
                .andExpect(content().string(containsString("動かそう。")))
                .andExpect(content().string(containsString("学習者の理解")))
                .andExpect(content().string(containsString("「前よりわかる」")));
    }
}
