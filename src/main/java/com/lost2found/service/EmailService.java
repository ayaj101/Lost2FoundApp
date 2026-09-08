package com.lost2found.service;

import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
public class EmailService {

        @Value("${resend.api.key:}")
        private String resendApiKey;

        @Value("${resend.from:Lost2Found <onboarding@resend.dev>}")
        private String resendFrom;

        private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build();

    // ⭐ NEW → OTP EMAIL SENDING SUPPORT
    public boolean sendOtpEmail(String toEmail, String otp) {
        return sendEmail(toEmail, "Lost2Found - Password Reset OTP",
            "Your OTP for password reset is: " + otp + "\n\nThis OTP is valid for 10 minutes.");
    }

    // ⭐ Optional: For match notifications
    public boolean sendMatchNotification(String toEmail, String lostTitle, String foundTitle, String type) {
        return sendEmail(toEmail, "Lost2Found - " + type,
                "A new match has been detected!\n\nLost Item: " + lostTitle +
                        "\nFound Item: " + foundTitle + "\nMatch Type: " + type +
                        "\n\nPlease login to view more details.");
    }

    private boolean sendEmail(String toEmail, String subject, String text) {
        if (resendApiKey == null || resendApiKey.isBlank()) {
            System.err.println("⚠️ RESEND_API_KEY is not configured");
            return false;
        }

        String json = "{\"from\":\"" + escape(resendFrom) + "\",\"to\":[\"" +
                escape(toEmail) + "\"],\"subject\":\"" + escape(subject) +
                "\",\"text\":\"" + escape(text) + "\"}";
        Request request = new Request.Builder()
                .url("https://api.resend.com/emails")
                .addHeader("Authorization", "Bearer " + resendApiKey)
                .post(RequestBody.create(json, MediaType.parse("application/json")))
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.err.println("⚠️ Resend email failed: " + response.code());
                return false;
            }
            return true;
        } catch (IOException ex) {
            System.err.println("⚠️ Resend email failed: " + ex.getMessage());
            return false;
        }
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }
}
