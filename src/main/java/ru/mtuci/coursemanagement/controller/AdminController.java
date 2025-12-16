package ru.mtuci.coursemanagement.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.mtuci.coursemanagement.model.User;
import ru.mtuci.coursemanagement.service.UserService;
import ru.mtuci.coursemanagement.util.CsrfUtil;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class AdminController {
    private final UserService userService;
    
    @GetMapping("/admin/users")
    public String usersPage(HttpSession session, Model model) {
        String role = (String) session.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return "redirect:/";
        }
        
        List<User> users = userService.findAll();
        model.addAttribute("users", users);
        model.addAttribute("csrf", CsrfUtil.getToken(session));
        return "admin/users";
    }
    
    @PostMapping("/admin/users/change-role")
    public String changeUserRole(@RequestParam Long userId,
                               @RequestParam String newRole,
                               @RequestParam String csrf,
                               HttpSession session,
                               Model model) {
        
        String adminRole = (String) session.getAttribute("role");
        if (!"ADMIN".equals(adminRole)) {
            return "redirect:/";
        }
        
        if (!CsrfUtil.check(session, csrf)) {
            model.addAttribute("error", "CSRF токен неверный");
            return "redirect:/admin/users";
        }
        
        if (!isValidRole(newRole)) {
            model.addAttribute("error", "Недопустимая роль");
            return "redirect:/admin/users";
        }
        
        userService.changeUserRole(userId, newRole);
        return "redirect:/admin/users";
    }
    
    private boolean isValidRole(String role) {
        return role.equals("STUDENT") || role.equals("TEACHER") || role.equals("ADMIN");
    }
}