package com.lost2found.controller;

import com.lost2found.model.User;
import com.lost2found.repository.UserRepository;
import com.lost2found.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/forgot-password")
public class ForgotPasswordController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    private Map<String, String> otpMap = new HashMap<>();


    @GetMapping("/send-otp/{username}")
    public String sendOtp(@PathVariable String username) {
        username = username.trim();
        System.out.println("🟡 Checking username: " + username);
        System.out.println("🔍 Received username from URL: [" + username + "]");

        Optional<User> userOpt = userRepository.findByUsernameIgnoreCase(username);


        if (userOpt.isEmpty()) {
            System.out.println("🟢 All usernames in DB:");
            for (User u : userRepository.findAll()) {
                System.out.println("➡️ [" + u.getUsername() + "]");
            }
            System.out.println("❌ User not found for username: " + username);
            return "not_exists";
        }

        // ✅ Generate OTP
        String otp = String.format("%06d", new Random().nextInt(999999));
        otpMap.put(username.toLowerCase(), otp);

        User user = userOpt.get();
        String email = user.getEmail();

        emailService.sendOtpEmail(email, otp);
        System.out.println("✅ OTP sent to: " + email);

        return "exists";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String username,
                                @RequestParam String otp,
                                @RequestParam String newPassword) {
        username = username.trim();

        String savedOtp = otpMap.get(username.toLowerCase());
        if (savedOtp == null || !savedOtp.equals(otp)) {
            return "invalid_otp";
        }

        Optional<User> userOpt = userRepository.findByUsernameIgnoreCase(username);
        if (userOpt.isEmpty()) {
            return "not_exists";
        }

        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        otpMap.remove(username.toLowerCase());
        System.out.println("✅ Password reset successful for " + username);
        return "success";
    }
}
