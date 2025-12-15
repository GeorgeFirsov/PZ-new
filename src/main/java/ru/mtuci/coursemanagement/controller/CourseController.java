package ru.mtuci.coursemanagement.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.mtuci.coursemanagement.service.CourseService;
import ru.mtuci.coursemanagement.util.CsrfUtil;

@Controller
@RequiredArgsConstructor
@RequestMapping("/courses")
public class CourseController {

    private final CourseService courseService;

    @GetMapping
    public String list(Model model, HttpSession session) {
        model.addAttribute("courses", courseService.findAll());
        model.addAttribute("csrf", CsrfUtil.getToken(session));
        return "courses";
    }

    @PostMapping("/add")
    public String add(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String csrf,
            HttpSession session
    ) {
        if (!CsrfUtil.check(session, csrf)) {
            return "error";
        }

        courseService.save(title, description);
        return "redirect:/courses";
    }

    @PostMapping("/delete")
    public String delete(
            @RequestParam Long id,
            @RequestParam String csrf,
            HttpSession session
    ) {
        if (!CsrfUtil.check(session, csrf)) {
            return "error";
        }

        courseService.delete(id);
        return "redirect:/courses";
    }
}
