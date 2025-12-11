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
import ru.mtuci.coursemanagement.security.CsrfTokenUtil;
import ru.mtuci.coursemanagement.service.UserService;

import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthController {
    private final UserService users;

    @GetMapping("/login")
    public String loginPage(HttpServletRequest req, Model model) {
        HttpSession session = req.getSession(true);
        String csrf = CsrfTokenUtil.getOrCreateToken(session);
        model.addAttribute("_csrf", csrf);
        return "login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam String username,
                          @RequestParam String password,
                          @RequestParam("_csrf") String csrfToken,
                          HttpServletRequest req,
                          Model model) {
        HttpSession session = req.getSession(false);
        if (!CsrfTokenUtil.isTokenValid(session, csrfToken)) {
            model.addAttribute("error", "Неверный CSRF-токен");
            return "login";
        }

        Optional<User> opt = users.findByUsername(username);
        if (opt.isPresent()) {
            User u = opt.get();
            if (u.getPassword().equals(password)) {
                log.info("User {} logged in", username);
                HttpSession s = req.getSession(true);
                s.setAttribute("username", username);
                s.setAttribute("role", u.getRole());
                return "redirect:/";
            }
        }
        model.addAttribute("error", "Неверные учетные данные");
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        if (s != null) {
            s.invalidate();
        }
        return "redirect:/login";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam(required = false, defaultValue = "STUDENT") String role,
                           @RequestParam("_csrf") String csrfToken,
                           HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (!CsrfTokenUtil.isTokenValid(session, csrfToken)) {
            return "redirect:/login";
        }
        users.save(new User(null, username, password, role));
        return "redirect:/login";
    }
}
