package com.lost2found.controller;

import com.lost2found.model.User;
import com.lost2found.repository.UserRepository;
import com.lost2found.service.NotificationService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;

import java.security.Principal;

@Controller
public class HomeController {

    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public HomeController(UserRepository userRepository, NotificationService notificationService) {
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

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
    public String index(Model model, Principal principal) {
        User user = userRepository.findByUsernameIgnoreCase(principal.getName()).orElse(null);
        if (user != null) {
            model.addAttribute("notifications", notificationService.unreadFor(user));
        }
        return "index";  // Must match file name: index.html
    }


}
