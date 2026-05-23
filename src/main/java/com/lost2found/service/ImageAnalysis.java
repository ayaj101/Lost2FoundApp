package com.lost2found.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

@Service
public class ImageAnalysis {

    @Value("${openai.api.key}")
    private String openAiKey;

    @Value("${openai.vision.url}")
    private String visionUrl;

    @Value("${openai.vision.model}")
    private String model;

    private final OkHttpClient http = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Uses OpenAI Vision model to generate a natural-language description
     * of the image at the provided path.
     */
    public String describeWithAI(Path imagePath) throws IOException {
        try {
            byte[] imageBytes = Files.readAllBytes(imagePath);
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);

            // Build JSON request for multimodal input (image + text instruction)
            String bodyJson = """
                {
                  "model": "%s",
                  "input": [
                    {
                      "role": "user",
                      "content": [
                        {"type": "input_text", "text": "Describe the key objects and details in this image in one short sentence."},
                        {"type": "input_image", "image_data": "%s"}
                      ]
                    }
                  ]
                }
                """.formatted(model, base64Image);

            RequestBody body = RequestBody.create(bodyJson, MediaType.parse("application/json"));
            Request request = new Request.Builder()
                    .url(visionUrl)
                    .addHeader("Authorization", "Bearer " + openAiKey)
                    .post(body)
                    .build();

            try (Response response = http.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Vision API call failed: " + response.code() + " " + response.message());
                }

                String json = response.body().string();
                JsonNode root = mapper.readTree(json);
                String caption = root.path("output").get(0).path("content").get(0).path("text").asText();

                if (caption == null || caption.isBlank()) {
                    caption = "Image contains an object (AI could not describe precisely)";
                }

                System.out.println("🧠 AI Caption: " + caption);
                return caption;
            }

        } catch (Exception e) {
            System.err.println("⚠️ AI caption generation failed: " + e.getMessage());
            return describeLocal(imagePath); // fallback
        }
    }

    /**
     * Local fallback — used if AI call fails or no internet.
     * You can later integrate OpenCV here.
     */
    public String describeLocal(Path imagePath) {
        // For now, return simple placeholder text
        String fileName = imagePath.getFileName().toString();
        return "Image file (" + fileName + ") — local fallback description.";
    }
}