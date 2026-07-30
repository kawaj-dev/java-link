package com.javalink.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Stage 2画面を表示するControllerです。
 */
@Controller
public class Stage2Controller {

    /**
     * mainメソッドを組み立てる画面を表示します。
     *
     * @return 表示するHTMLの名前
     */
    @GetMapping("/stage2")
    public String showStage2() {
        return "stage2";
    }
}
