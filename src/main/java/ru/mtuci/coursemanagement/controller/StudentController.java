package ru.mtuci.coursemanagement.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.mtuci.coursemanagement.service.StudentService;
import ru.mtuci.coursemanagement.util.CsrfUtil;

@Controller
@RequiredArgsConstructor
@RequestMapping("/students")
public class StudentController {

    private final StudentService studentService;

    @GetMapping
    public String list(Model model, HttpSession session) {
        model.addAttribute("students", studentService.findAll());
        model.addAttribute("csrf", CsrfUtil.getToken(session));
        return "students";
    }

    @PostMapping("/add")
    public String add(
            @RequestParam String name,
            @RequestParam String csrf,
            HttpSession session
    ) {
        if (!CsrfUtil.check(session, csrf)) {
            return "error";
        }

        studentService.add(name);
        return "redirect:/students";
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

        studentService.delete(id);
        return "redirect:/students";
    }
}
