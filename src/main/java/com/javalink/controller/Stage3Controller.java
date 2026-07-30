package com.javalink.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Lesson 1 の Stage3 画面を表示します。
 */
@Controller
public class Stage3Controller {

    @GetMapping("/stage3")
    public String showStage3() {
        return "stage3";
    }
}
