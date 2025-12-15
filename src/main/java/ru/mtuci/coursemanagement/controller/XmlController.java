package ru.mtuci.coursemanagement.controller;

import jakarta.servlet.http.HttpSession;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.mtuci.coursemanagement.util.CsrfUtil;

import java.io.StringReader;

@RestController
public class XmlController {

    @PostMapping(value = "/api/xml/parse", consumes = {MediaType.TEXT_XML_VALUE, MediaType.APPLICATION_XML_VALUE})
    public String parse(@RequestBody String xml,
                        @RequestParam String csrf,
                        HttpSession session) throws Exception {

        if (!CsrfUtil.check(session, csrf)) {
            return "CSRF error";
        }

        SAXReader reader = new SAXReader();
        Document doc = reader.read(new StringReader(xml));
        return doc.getRootElement().getText();
    }
}
