package com.javalink.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Java Linkのログイン画面を表示するControllerです。
 */
@Controller
public class LoginController {

    /**
     * ブラウザで「/login」を開いたときにログイン画面を表示します。
     *
     * @return 表示するHTMLの名前
     */
    @GetMapping("/login")
    public String showLogin() {
        return "login";
    }
}
