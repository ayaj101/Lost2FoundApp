package com.lost2found.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class EmbeddingService {

    private final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${openai.api.key}")
    private String openAiKey;

    @Value("${openai.embeddings.url}")
    private String embeddingsUrl;

    @Value("${openai.embeddings.model}")
    private String model;

    /**
     * Generates an embedding vector from text using OpenAI API
     * and returns it as a double array.
     */
    public double[] embed(String text) {
        if (text == null || text.isBlank()) {
            return new double[0];
        }

        try {
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            String jsonBody = mapper.writeValueAsString(
                    mapper.createObjectNode()
                            .put("model", model)
                            .put("input", text)
            );

            RequestBody body = RequestBody.create(jsonBody, JSON);
            Request request = new Request.Builder()
                    .url(embeddingsUrl)
                    .header("Authorization", "Bearer " + openAiKey)
                    .post(body)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    System.err.println("⚠️ Embedding API error: " + response.code() + " " + response.message());
                    return new double[0];
                }

                String responseBody = response.body().string();
                JsonNode root = mapper.readTree(responseBody);
                JsonNode embeddingNode = root.path("data").get(0).path("embedding");

                List<Double> list = new ArrayList<>();
                for (JsonNode node : embeddingNode) {
                    list.add(node.asDouble());
                }

                double[] vector = new double[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    vector[i] = list.get(i);
                }

                return vector;
            }

        } catch (IOException e) {
            System.err.println("⚠️ Embedding generation failed: " + e.getMessage());
            return new double[0];
        }
    }

    /**
     * Converts double[] embeddings to JSON string for DB storage.
     */
    public String toJson(double[] emb) {
        try {
            return mapper.writeValueAsString(emb);
        } catch (Exception e) {
            return "[]";
        }
    }

    /**
     * Parses stored JSON back to double[].
     */
    public double[] fromJson(String json) {
        try {
            if (json == null || json.isBlank()) return new double[0];
            JsonNode node = mapper.readTree(json);
            double[] arr = new double[node.size()];
            for (int i = 0; i < node.size(); i++) {
                arr[i] = node.get(i).asDouble();
            }
            return arr;
        } catch (Exception e) {
            System.err.println("⚠️ Failed to parse embedding JSON: " + e.getMessage());
            return new double[0];
        }
    }

    /**
     * Wrapper to get embedding and directly return JSON.
     */
    public String embedToJson(String text) {
        double[] vector = embed(text);
        return toJson(vector);
    }
}