package com.lost2found.controller;

import com.lost2found.model.User;
import com.lost2found.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
public class LoginController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Show login page
    @GetMapping("/login")
    public String loginPage() {
        return "Login"; // Login.html in templates folder
    }

    // Handle login form submission
    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        Model model) {

        // Retrieve user from DB using Optional
        Optional<User> optionalUser = userRepository.findByUsernameIgnoreCase(username);

        // Check if user exists and password matches
        if (optionalUser.isPresent() && passwordEncoder.matches(password, optionalUser.get().getPassword())
        ) {
            // Successful login → redirect to index.html
            return "/index";
        } else {
            // Login failed → show error message on login page
            model.addAttribute("error", "Invalid username or password!");
            return "Login";
        }
    }


}