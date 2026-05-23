package com.lost2found.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // ⭐ NEW → OTP EMAIL SENDING SUPPORT
    public void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Lost2Found - Password Reset OTP");
        message.setText("Your OTP for password reset is: " + otp + "\n\n"
                + "This OTP is valid for 10 minutes.");

        mailSender.send(message);
        System.out.println("📧 OTP Email sent to: " + toEmail);
    }

    // ⭐ Optional: For match notifications
    public void sendMatchNotification(String toEmail, String lostTitle, String foundTitle, String type) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Lost2Found - " + type);

        message.setText(
                "A new match has been detected!\n\n" +
                        "Lost Item: " + lostTitle + "\n" +
                        "Found Item: " + foundTitle + "\n" +
                        "Match Type: " + type + "\n\n" +
                        "Please login to view more details."
        );

        mailSender.send(message);
        System.out.println("📧 Match Email sent to: " + toEmail);
    }
}
