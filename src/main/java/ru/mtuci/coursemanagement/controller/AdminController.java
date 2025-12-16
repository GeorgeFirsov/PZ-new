package ru.mtuci.coursemanagement.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.mtuci.coursemanagement.service.UserService;
import ru.mtuci.coursemanagement.util.CsrfUtil;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AdminController {
    private final UserService userService;
    
    @GetMapping("/admin/users")
    public String usersPage(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        String role = (String) session.getAttribute("role");
        
        if (!"ADMIN".equals(role)) {
            log.warn("UNAUTHORIZED_ADMIN_ACCESS: Пользователь {} попытался получить доступ к админке", username);
            return "redirect:/";
        }
        
        log.info("ADMIN_ACCESS: Администратор {} просматривает список пользователей", username);
        model.addAttribute("users", userService.findAll());
        model.addAttribute("csrf", CsrfUtil.getToken(session));
        return "admin-users";
    }
    
    @PostMapping("/admin/users/change-role")
    public String changeUserRole(@RequestParam Long userId,
                               @RequestParam String newRole,
                               @RequestParam String csrf,
                               HttpSession session,
                               Model model) {
        
        String adminUsername = (String) session.getAttribute("username");
        String adminRole = (String) session.getAttribute("role");
        
        if (!"ADMIN".equals(adminRole)) {
            log.warn("UNAUTHORIZED_ROLE_CHANGE: Пользователь {} попытался изменить роль", adminUsername);
            return "redirect:/";
        }
        
        if (!CsrfUtil.check(session, csrf)) {
            log.warn("CSRF_FAILURE_ADMIN: Неверный CSRF токен при изменении роли администратором {}", adminUsername);
            model.addAttribute("error", "CSRF токен неверный");
            return "redirect:/admin/users";
        }
        
        if (!isValidRole(newRole)) {
            log.warn("INVALID_ROLE: Администратор {} указал недопустимую роль {}", adminUsername, newRole);
            model.addAttribute("error", "Недопустимая роль");
            return "redirect:/admin/users";
        }
        
        userService.changeUserRole(userId, newRole);
        log.info("ROLE_CHANGE: Администратор {} изменил роль пользователя {} на {}", adminUsername, userId, newRole);
        return "redirect:/admin/users";
    }
    
    private boolean isValidRole(String role) {
        return role.equals("STUDENT") || role.equals("TEACHER") || role.equals("ADMIN");
    }
}