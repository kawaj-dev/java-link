package com.javalink.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Stage画面を表示するControllerです。
 */
@Controller
public class StageController {

    /**
     * Stage 1の画面を表示します。
     *
     * @return 表示するHTMLの名前
     */
    @GetMapping("/stage1")
    public String showStage1() {
        return "stage1";
    }
}
