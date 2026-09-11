package com.javalink.controller;

import com.javalink.service.UserRegistrationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Java Linkのアカウント作成画面を表示するControllerです。
 */
@Controller
public class RegistrationController {

    private final UserRegistrationService userRegistrationService;

    public RegistrationController(UserRegistrationService userRegistrationService) {
        this.userRegistrationService = userRegistrationService;
    }

    /**
     * ブラウザで「/register」を開いたときにアカウント作成画面を表示します。
     *
     * @return 表示するHTMLの名前
     */
    @GetMapping("/register")
    public String showRegistration() {
        return "register";
    }

    /**
     * 入力された内容でアカウントを登録します。
     *
     * @param displayName 表示名
     * @param email       メールアドレス
     * @param password    パスワード
     * @param model       HTMLへ渡すデータを入れる箱
     * @return 登録成功時はログイン画面、重複時はアカウント作成画面
     */
    @PostMapping("/register")
    public String register(
            @RequestParam("displayName") String displayName,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            Model model) {
        try {
            userRegistrationService.register(displayName, email, password);
            return "redirect:/login";
        } catch (IllegalArgumentException exception) {
            model.addAttribute("registrationError", exception.getMessage());
            return "register";
        }
    }
}
