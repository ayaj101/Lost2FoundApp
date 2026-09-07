package com.lost2found.controller;

import com.lost2found.model.User;
import com.lost2found.repository.UserRepository;
import com.lost2found.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@RestController
@RequestMapping("/forgot-password")
public class ForgotPasswordController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    private static final int MAX_OTP_ATTEMPTS = 5;
    private static final long OTP_VALIDITY_SECONDS = 600;
    private final SecureRandom secureRandom = new SecureRandom();
    private final ConcurrentMap<String, OtpEntry> otpMap = new ConcurrentHashMap<>();


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
        String otp = String.format("%06d", secureRandom.nextInt(1_000_000));
        otpMap.put(username.toLowerCase(), new OtpEntry(otp, Instant.now().plusSeconds(OTP_VALIDITY_SECONDS), 0));

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

        String normalizedUsername = username.toLowerCase();
        OtpEntry entry = otpMap.get(normalizedUsername);
        if (entry == null || Instant.now().isAfter(entry.expiresAt())
                || entry.attempts() >= MAX_OTP_ATTEMPTS) {
            otpMap.remove(normalizedUsername);
            return "invalid_otp";
        }
        if (!entry.otp().equals(otp)) {
            otpMap.put(normalizedUsername, new OtpEntry(entry.otp(), entry.expiresAt(), entry.attempts() + 1));
            return "invalid_otp";
        }

        Optional<User> userOpt = userRepository.findByUsernameIgnoreCase(username);
        if (userOpt.isEmpty()) {
            return "not_exists";
        }

        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        otpMap.remove(normalizedUsername);
        System.out.println("✅ Password reset successful for " + username);
        return "success";
    }

    private record OtpEntry(String otp, Instant expiresAt, int attempts) {}
}
