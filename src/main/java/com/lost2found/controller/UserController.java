package com.lost2found.controller;

import com.lost2found.model.User;
import com.lost2found.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // ✅ Show Register Page
    @GetMapping("/user/register")
    public String showRegisterForm(Model model) {

        model.addAttribute("user", new User());

        return "register";
    }

    // ✅ Handle Registration
    @PostMapping("/register")
    public String registerUser(
            @ModelAttribute User user,
            @RequestParam("confirmPassword") String confirmPassword,
            Model model) {

        try {

            System.out.println("Register button clicked");

            // ✅ Username Check
            if (userRepository.existsByUsername(user.getUsername())) {

                model.addAttribute(
                        "error",
                        "❌ Username already exists!"
                );

                return "register";
            }

            // ✅ Email Check
            if (userRepository.existsByEmail(user.getEmail())) {

                model.addAttribute(
                        "error",
                        "❌ Email already registered!"
                );

                return "register";
            }

            // ✅ Password Match Check
            if (!user.getPassword().equals(confirmPassword)) {

                model.addAttribute(
                        "error",
                        "❌ Passwords do not match!"
                );

                return "register";
            }

            // ✅ Encode Password
            user.setPassword(
                    passwordEncoder.encode(user.getPassword())
            );

            // ✅ Save User
            userRepository.save(user);

            System.out.println("User Saved Successfully");

            // ✅ Success Message
            model.addAttribute(
                    "message",
                    "✅ Registration Successful! Please Login."
            );

            return "login";

        } catch (Exception e) {

            e.printStackTrace();

            model.addAttribute(
                    "error",
                    "❌ Something went wrong: " + e.getMessage()
            );

            return "register";
        }
    }
}