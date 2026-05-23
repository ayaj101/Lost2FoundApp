package com.lost2found.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String redirectToHome() {
        return "redirect:/home";
    }

    @GetMapping("/home")
    public String home() {
        return "Home";  // Make sure Home.html exists in src/main/resources/templates
    }
    @GetMapping("/register")
    public String register() {
        return "register";  // Make sure file name is Register.html (case-sensitive)
    }

    @GetMapping("/index")
    public String index() {
        return "index";  // Must match file name: index.html
    }


}
