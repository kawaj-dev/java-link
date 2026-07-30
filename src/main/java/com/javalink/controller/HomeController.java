package com.javalink.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Java Linkのトップページを表示するControllerです。
 */
@Controller
public class HomeController {

    /**
     * 2つの学習メニューを表示します。
     *
     * @return 表示するHTMLの名前
     */
    @GetMapping("/")
    public String showHome() {
        return "home";
    }
}
