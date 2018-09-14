package de.bonito.dashboard.controller;

import de.bonito.dashboard.DashboardApplication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class IndexController {

    String defaultName = "Anonym";

    @RequestMapping("/")
    public String index( Model model) {
        model.addAttribute("counter", DashboardApplication.getCounter());

        return "index";
    }
}