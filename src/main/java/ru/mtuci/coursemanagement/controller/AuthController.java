package ru.mtuci.coursemanagement.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.mtuci.coursemanagement.model.User;
import ru.mtuci.coursemanagement.service.UserService;
import ru.mtuci.coursemanagement.util.CsrfUtil;

import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthController {
    private final UserService users;

    @GetMapping("/login")
    public String loginPage(HttpServletRequest req, Model model) {
        HttpSession s = req.getSession(true);
        model.addAttribute("csrf", CsrfUtil.getToken(s));
        return "login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam String username,
                          @RequestParam String password,
                          @RequestParam String csrf,
                          HttpServletRequest req,
                          Model model) {

        HttpSession s = req.getSession(true);
        if (!CsrfUtil.check(s, csrf)) {
            model.addAttribute("error", "CSRF токен неверный");
            model.addAttribute("csrf", CsrfUtil.getToken(s));
            return "login";
        }

        Optional<User> opt = users.findByUsername(username);
        if (opt.isPresent()) {
            User u = opt.get();
            if (users.checkPassword(password, u.getPassword())) {
                log.info("User {} logged in", username);

                s.setAttribute("username", username);
                s.setAttribute("role", u.getRole());
                return "redirect:/";
            }
        }

        log.warn("Failed login attempt for username: {}", username);
        model.addAttribute("error", "Неверные учетные данные");
        model.addAttribute("csrf", CsrfUtil.getToken(s));
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        if (s != null) s.invalidate();
        return "redirect:/login";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam(required = false, defaultValue = "STUDENT") String role,
                           @RequestParam String csrf,
                           HttpServletRequest req,
                           Model model) {

        HttpSession s = req.getSession(true);
        if (!CsrfUtil.check(s, csrf)) {
            model.addAttribute("error", "CSRF токен неверный");
            model.addAttribute("csrf", CsrfUtil.getToken(s));
            return "login";
        }

        users.save(new User(null, username, password, role));
        log.info("New user registered: {} with role: {}", username, role);
        return "redirect:/login";
    }
}