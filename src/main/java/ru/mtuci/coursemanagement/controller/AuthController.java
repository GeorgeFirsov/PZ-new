package ru.mtuci.coursemanagement.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.mtuci.coursemanagement.model.User;
import ru.mtuci.coursemanagement.service.LoginAttemptService;
import ru.mtuci.coursemanagement.service.UserService;
import ru.mtuci.coursemanagement.util.CsrfUtil;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class AuthController {
    private final UserService users;
    private final LoginAttemptService loginAttemptService;

    @GetMapping("/login")
    public String loginPage(HttpServletRequest req, Model model) {
        HttpSession s = req.getSession(true);
        model.addAttribute("csrf", CsrfUtil.getToken(s));
        
        String ipAddress = getClientIpAddress(req);
        String username = req.getParameter("username");
        
        if (username != null && !username.isEmpty()) {
            if (loginAttemptService.isBlocked(username, ipAddress)) {
                int remainingTime = loginAttemptService.getRemainingLockoutTime(username, ipAddress);
                model.addAttribute("error", "Аккаунт заблокирован. Попробуйте через " + remainingTime + " минут.");
            } else {
                int remainingAttempts = loginAttemptService.getRemainingAttempts(username, ipAddress);
                if (remainingAttempts < 3) {
                    model.addAttribute("warning", "Осталось " + remainingAttempts + " попыток");
                }
            }
        }
        
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

        String ipAddress = getClientIpAddress(req);
        
        if (loginAttemptService.isBlocked(username, ipAddress)) {
            int remainingTime = loginAttemptService.getRemainingLockoutTime(username, ipAddress);
            model.addAttribute("error", "Аккаунт заблокирован. Попробуйте через " + remainingTime + " минут.");
            model.addAttribute("csrf", CsrfUtil.getToken(s));
            return "login";
        }

        Optional<User> opt = users.findByUsername(username);
        if (opt.isPresent()) {
            User u = opt.get();
            if (users.checkPassword(password, u.getPassword())) {
                loginAttemptService.loginSucceeded(username, ipAddress);
                s.setAttribute("username", username);
                s.setAttribute("role", u.getRole());
                return "redirect:/";
            }
        }

        loginAttemptService.loginFailed(username, ipAddress);
        
        int remainingAttempts = loginAttemptService.getRemainingAttempts(username, ipAddress);
        if (remainingAttempts > 0) {
            model.addAttribute("error", "Неверные данные. Осталось попыток: " + remainingAttempts);
        } else {
            int remainingTime = loginAttemptService.getRemainingLockoutTime(username, ipAddress);
            model.addAttribute("error", "Аккаунт заблокирован. Попробуйте через " + remainingTime + " минут.");
        }
        
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
                           @RequestParam String csrf,
                           HttpServletRequest req,
                           Model model) {

        HttpSession s = req.getSession(true);
        if (!CsrfUtil.check(s, csrf)) {
            model.addAttribute("error", "CSRF токен неверный");
            model.addAttribute("csrf", CsrfUtil.getToken(s));
            return "login";
        }

        if (username == null || username.trim().isEmpty() || username.length() < 3) {
            model.addAttribute("error", "Логин должен быть не менее 3 символов");
            model.addAttribute("csrf", CsrfUtil.getToken(s));
            return "login";
        }

        if (password == null || password.trim().isEmpty() || password.length() < 6) {
            model.addAttribute("error", "Пароль должен быть не менее 6 символов");
            model.addAttribute("csrf", CsrfUtil.getToken(s));
            return "login";
        }

        Optional<User> existingUser = users.findByUsername(username);
        if (existingUser.isPresent()) {
            model.addAttribute("error", "Пользователь с таким логином уже существует");
            model.addAttribute("csrf", CsrfUtil.getToken(s));
            return "login";
        }

        users.save(new User(null, username.trim(), password, "STUDENT"));
        return "redirect:/login";
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }
        
        return ipAddress;
    }
}