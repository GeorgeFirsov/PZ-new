package ru.mtuci.coursemanagement.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.mtuci.coursemanagement.service.UserService;
import ru.mtuci.coursemanagement.util.CsrfUtil;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String loginPage(HttpSession session, Model model) {
        model.addAttribute("csrf", CsrfUtil.getToken(session));
        return "login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String csrf,
            HttpSession session
    ) {
        if (!CsrfUtil.check(session, csrf)) {
            return "error";
        }

        if (!userService.login(username, password, session)) {
            return "login";
        }

        return "redirect:/";
    }

    @GetMapping("/register")
    public String registerPage(HttpSession session, Model model) {
        model.addAttribute("csrf", CsrfUtil.getToken(session));
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String csrf,
            HttpSession session
    ) {
        if (!CsrfUtil.check(session, csrf)) {
            return "error";
        }

        userService.register(username, password);
        return "redirect:/login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
