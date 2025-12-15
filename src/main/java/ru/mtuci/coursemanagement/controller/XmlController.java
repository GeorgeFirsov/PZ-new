package ru.mtuci.coursemanagement.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.mtuci.coursemanagement.util.CsrfUtil;

@RestController
public class XmlController {

    @PostMapping(
            value = "/api/xml/parse",
            consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE}
    )
    public String parse(
            @RequestBody String xml,
            @RequestParam String csrf,
            HttpSession session
    ) {
        if (!CsrfUtil.check(session, csrf)) {
            return "CSRF error";
        }

        return "XML processed";
    }
}
