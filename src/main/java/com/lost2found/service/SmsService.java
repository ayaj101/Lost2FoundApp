package com.lost2found.service;

import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Base64;

@Service
public class SmsService {

    @Value("${twilio.account.sid:}")
    private String accountSid;

    @Value("${twilio.auth.token:}")
    private String authToken;

    @Value("${twilio.from:}")
    private String fromNumber;

    private final OkHttpClient client = new OkHttpClient();

    public boolean sendMatchSms(String toNumber, String itemTitle) {
        if (toNumber == null || toNumber.isBlank() || accountSid.isBlank()
                || authToken.isBlank() || fromNumber.isBlank()) {
            System.err.println("⚠️ Twilio SMS is not configured or recipient has no phone number");
            return false;
        }

        String message = "Lost2Found: A possible match was found for " + itemTitle
                + ". Log in and open Matches to review it.";
        RequestBody body = new FormBody.Builder()
                .add("To", toNumber)
                .add("From", fromNumber)
                .add("Body", message)
                .build();
        String credentials = Base64.getEncoder().encodeToString(
                (accountSid + ":" + authToken).getBytes());
        Request request = new Request.Builder()
                .url("https://api.twilio.com/2010-04-01/Accounts/" + accountSid + "/Messages.json")
                .header("Authorization", "Basic " + credentials)
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.err.println("⚠️ Twilio SMS failed: " + response.code());
                return false;
            }
            return true;
        } catch (IOException ex) {
            System.err.println("⚠️ Twilio SMS failed: " + ex.getMessage());
            return false;
        }
    }
}
