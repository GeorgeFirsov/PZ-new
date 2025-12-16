package ru.mtuci.coursemanagement.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import ru.mtuci.coursemanagement.model.Course;
import ru.mtuci.coursemanagement.repository.CourseRepository;
import ru.mtuci.coursemanagement.service.CourseService;
import ru.mtuci.coursemanagement.util.CsrfUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Controller
@RequiredArgsConstructor
public class CourseController {

    private final CourseRepository repo;
    private final CourseService service;
    private final RestTemplate secureRestTemplate;

    @GetMapping("/courses")
    public String coursesPage(Model model, HttpSession session) {
        String username = (String) session.getAttribute("username");
        log.info("COURSES_PAGE_ACCESS: Пользователь {} просматривает список курсов", username);
        
        model.addAttribute("courses", repo.findAll());
        model.addAttribute("course", new Course());
        model.addAttribute("csrf", CsrfUtil.getToken(session));
        return "courses";
    }

    @PostMapping("/courses")
    public String createCourse(@ModelAttribute Course c,
                               @RequestParam String csrf,
                               HttpSession session,
                               Model model) {

        String username = (String) session.getAttribute("username");
        
        if (!CsrfUtil.check(session, csrf)) {
            log.warn("CSRF_FAILURE_COURSE: Неверный CSRF токен при создании курса пользователем {}", username);
            model.addAttribute("error", "CSRF токен неверный");
            model.addAttribute("courses", repo.findAll());
            model.addAttribute("course", new Course());
            model.addAttribute("csrf", CsrfUtil.getToken(session));
            return "courses";
        }

        repo.save(c);
        log.info("COURSE_CREATED: Пользователь {} создал курс: {}", username, c.getTitle());
        return "redirect:/courses";
    }

    @GetMapping("/api/courses")
    @ResponseBody
    public List<Course> all() {
        return repo.findAll();
    }

    @GetMapping("/api/courses/{id}")
    @ResponseBody
    public ResponseEntity<Course> one(@PathVariable Long id) {
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/api/courses/{id}")
    @ResponseBody
    public ResponseEntity<Course> update(@PathVariable Long id,
                                         @RequestBody Course payload) {

        return repo.findById(id).map(c -> {
            c.setTitle(payload.getTitle());
            c.setDescription(payload.getDescription());
            c.setTeacherId(payload.getTeacherId());
            return ResponseEntity.ok(repo.save(c));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/api/courses/search")
    @ResponseBody
    public List<Course> search(@RequestParam String title) {
        log.info("COURSE_SEARCH: Поиск курсов по названию: {}", title);
        return service.searchByTitle(title);
    }

    @GetMapping("/api/courses/import")
    @ResponseBody
    public ResponseEntity<String> importFromUrl(@RequestParam String url,
                                                HttpSession session) {
        
        String username = (String) session.getAttribute("username");
        String role = (String) session.getAttribute("role");
        
        if (!"TEACHER".equals(role) && !"ADMIN".equals(role)) {
            log.warn("UNAUTHORIZED_IMPORT: Пользователь {} попытался импортировать курсы без прав", username);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Доступ запрещен. Требуется роль TEACHER или ADMIN");
        }
        
        if (!isValidUrl(url)) {
            log.warn("INVALID_IMPORT_URL: Пользователь {} указал невалидный URL: {}", username, url);
            return ResponseEntity.badRequest()
                    .body("Недопустимый URL. Разрешены только HTTP/HTTPS ссылки на внешние ресурсы");
        }
        
        if (isInternalUrl(url)) {
            log.warn("SSRF_ATTEMPT: Пользователь {} попытался получить доступ к внутреннему ресурсу: {}", username, url);
            return ResponseEntity.badRequest()
                    .body("Доступ к внутренним ресурсам запрещен");
        }
        
        try {
            String json = secureRestTemplate.getForObject(url, String.class);
            log.info("COURSE_IMPORT_SUCCESS: Пользователь {} импортировал курсы из URL: {}", username, url);
            return ResponseEntity.ok("Курсы успешно импортированы");
        } catch (Exception e) {
            log.error("COURSE_IMPORT_FAILED: Ошибка при импорте курсов пользователем {} из URL {}: {}", 
                     username, url, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при импорте курсов");
        }
    }
    
    private boolean isValidUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        
        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            return "http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme);
        } catch (URISyntaxException e) {
            return false;
        }
    }
    
    private boolean isInternalUrl(String url) {
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            
            if (host == null) {
                return false;
            }
            
            String lowerHost = host.toLowerCase();
            
            return lowerHost.equals("localhost") ||
                   lowerHost.equals("127.0.0.1") ||
                   lowerHost.equals("::1") ||
                   lowerHost.equals("0.0.0.0") ||
                   lowerHost.startsWith("192.168.") ||
                   lowerHost.startsWith("10.") ||
                   lowerHost.startsWith("172.16.") ||
                   lowerHost.startsWith("172.17.") ||
                   lowerHost.startsWith("172.18.") ||
                   lowerHost.startsWith("172.19.") ||
                   lowerHost.startsWith("172.20.") ||
                   lowerHost.startsWith("172.21.") ||
                   lowerHost.startsWith("172.22.") ||
                   lowerHost.startsWith("172.23.") ||
                   lowerHost.startsWith("172.24.") ||
                   lowerHost.startsWith("172.25.") ||
                   lowerHost.startsWith("172.26.") ||
                   lowerHost.startsWith("172.27.") ||
                   lowerHost.startsWith("172.28.") ||
                   lowerHost.startsWith("172.29.") ||
                   lowerHost.startsWith("172.30.") ||
                   lowerHost.startsWith("172.31.") ||
                   lowerHost.endsWith(".local") ||
                   lowerHost.endsWith(".internal");
        } catch (URISyntaxException e) {
            return true;
        }
    }
}