package ru.mtuci.coursemanagement.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.mtuci.coursemanagement.service.PluginLoader;
import ru.mtuci.coursemanagement.util.CsrfUtil;

@Controller
@RequiredArgsConstructor
public class IndexController {
    private final PluginLoader loader;

    @GetMapping("/")
    public String index(HttpSession session, Model model) {
    model.addAttribute("csrf", CsrfUtil.getToken(session));
    return "index";
}

}
