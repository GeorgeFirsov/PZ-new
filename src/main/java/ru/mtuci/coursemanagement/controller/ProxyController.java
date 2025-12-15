package ru.mtuci.coursemanagement.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;
import ru.mtuci.coursemanagement.util.CsrfUtil;

@RestController
@RequestMapping("/proxy")
public class ProxyController {

    @PostMapping
    public String proxy(
            @RequestParam String url,
            @RequestParam String csrf,
            HttpSession session
    ) {
        if (!CsrfUtil.check(session, csrf)) {
            return "CSRF error";
        }

        return "Proxy disabled";
    }
}
