package com.javalink.controller;

import java.util.Optional;

import com.javalink.entity.UserAccount;
import com.javalink.service.AuthenticatedUserService;
import com.javalink.service.UserAuthenticationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Java Linkのログイン画面を表示するControllerです。
 */
@Controller
public class LoginController {

    private static final String LOGIN_ERROR_MESSAGE =
            "メールアドレスまたはパスワードが正しくありません。";

    private final UserAuthenticationService userAuthenticationService;
    private final AuthenticatedUserService authenticatedUserService;

    public LoginController(
            UserAuthenticationService userAuthenticationService,
            AuthenticatedUserService authenticatedUserService) {
        this.userAuthenticationService = userAuthenticationService;
        this.authenticatedUserService = authenticatedUserService;
    }

    /**
     * ブラウザで「/login」を開いたときにログイン画面を表示します。
     *
     * @return 表示するHTMLの名前
     */
    @GetMapping("/login")
    public String showLogin() {
        return "login";
    }

    /**
     * 入力されたメールアドレスとパスワードを照合します。
     *
     * @param email    メールアドレス
     * @param password パスワード
     * @param model    HTMLへ渡すデータを入れる箱
     * @return 認証成功時はトップ画面、失敗時はログイン画面
     */
    @PostMapping("/login")
    public String login(
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            Model model,
            HttpSession session) {
        Optional<UserAccount> authenticatedUser =
                userAuthenticationService.authenticate(email, password);
        if (authenticatedUser.isPresent()) {
            authenticatedUserService.storeAuthenticatedUser(
                    session,
                    authenticatedUser.get());
            return "redirect:/";
        }

        model.addAttribute("loginError", LOGIN_ERROR_MESSAGE);
        model.addAttribute("email", email);
        return "login";
    }
}
