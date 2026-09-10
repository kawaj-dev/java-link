package com.javalink.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Java Linkのアカウント作成画面を表示するControllerです。
 */
@Controller
public class RegistrationController {

    /**
     * ブラウザで「/register」を開いたときにアカウント作成画面を表示します。
     *
     * @return 表示するHTMLの名前
     */
    @GetMapping("/register")
    public String showRegistration() {
        return "register";
    }
}
