package ru.mtuci.coursemanagement.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.mtuci.coursemanagement.model.Student;
import ru.mtuci.coursemanagement.repository.StudentRepository;
import ru.mtuci.coursemanagement.util.CsrfUtil;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class StudentController {
    private final StudentRepository repo;

    @GetMapping("/students")
    public String studentsPage(Model model, HttpSession session) {
        String username = (String) session.getAttribute("username");
        log.info("STUDENTS_PAGE_ACCESS: Пользователь {} просматривает список студентов", username);
        
        model.addAttribute("students", repo.findAll());
        model.addAttribute("student", new Student());
        model.addAttribute("csrf", CsrfUtil.getToken(session));
        return "students";
    }

    @PostMapping("/students")
    public String createStudent(@ModelAttribute Student st,
                                @RequestParam String csrf,
                                HttpSession session,
                                Model model) {

        String username = (String) session.getAttribute("username");
        
        if (!CsrfUtil.check(session, csrf)) {
            log.warn("CSRF_FAILURE_STUDENT: Неверный CSRF токен при создании студента пользователем {}", username);
            model.addAttribute("error", "CSRF токен неверный");
            model.addAttribute("students", repo.findAll());
            model.addAttribute("student", new Student());
            model.addAttribute("csrf", CsrfUtil.getToken(session));
            return "students";
        }

        repo.save(st);
        log.info("STUDENT_CREATED: Пользователь {} создал студента: {}", username, st.getName());
        return "redirect:/students";
    }

    @GetMapping("/api/students")
    @ResponseBody
    public List<Student> all() {
        return repo.findAll();
    }

    @GetMapping("/api/students/{id}")
    @ResponseBody
    public ResponseEntity<Student> one(@PathVariable Long id, HttpSession s) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/api/students/{id}")
    @ResponseBody
    public ResponseEntity<Student> update(@PathVariable Long id, @RequestBody Student payload) {
        return repo.findById(id).map(st -> {
            st.setName(payload.getName());
            st.setEmail(payload.getEmail());
            st.setUserId(payload.getUserId());
            return ResponseEntity.ok(repo.save(st));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/api/students/{id}")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}