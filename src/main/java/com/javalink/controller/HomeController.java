package com.javalink.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Java Linkのトップページを表示するControllerです。
 */
@Controller
public class HomeController {

    /**
     * 「コードを左から読む」を中心としたトップ画面を表示します。
     *
     * @return 表示するHTMLの名前
     */
    @GetMapping("/")
    public String showHome() {
        return "home";
    }
}
