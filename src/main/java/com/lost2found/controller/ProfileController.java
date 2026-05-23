package com.lost2found.controller;

import com.lost2found.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.Map;

@RestController
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @GetMapping("/data")
    public Map<String, ? extends Serializable> getProfile(@AuthenticationPrincipal UserDetails userDetails, HttpSession session) {
        if (userDetails == null) {
            return Map.of("error", "Not logged in");
        }

        return userRepository.findByUsernameIgnoreCase(userDetails.getUsername())
                .map(user -> Map.of(
                        "id", user.getId(),
                        "username", user.getUsername(),
                        "email", user.getEmail(),
                        "phone", user.getPhone()   // ✅ include phone
                ))
                .orElse(Map.of("error", "User not found"));
    }


    // Update logged-in user's password
    @PostMapping("/update-password")
    public String updatePassword(@AuthenticationPrincipal UserDetails userDetails,
                                 @RequestParam String newPassword) {
        return userRepository.findByUsernameIgnoreCase(userDetails.getUsername())
                .map(user -> {
                    user.setPassword(passwordEncoder.encode(newPassword));
                    userRepository.save(user);
                    return "Password updated successfully!";
                })
                .orElse("User not found");
    }
}
