package com.example.healthease.utils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class ChatService {
    private static final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    public static String askHealthQuestion(String question) {
        if (question == null) question = "";
        String q = question.trim();
        if (q.isEmpty()) return "Please enter a health-related question.";
        if (!isHealthRelated(q)) {
            return "Please ask a health-related question (nutrition, exercise, sleep, wellness).";
        }

        // Try local Ollama first
        String model = getenvOr("OLLAMA_MODEL", "llama3.2:3b-instruct");
        String systemPrompt = "You are a concise health & wellness assistant. " +
                "Answer only general health questions (nutrition, fitness, sleep, stress). " +
                "If a question is not about health, politely refuse. " +
                "Do not provide diagnoses or medical treatment. " +
                "Add: 'This is not medical advice.' at the end.";
        try {
            String body = "{" +
                    "\"model\":\"" + escape(model) + "\"," +
                    "\"prompt\":\"" + escape(systemPrompt + "\n\nUser: " + q + "\nAssistant:") + "\"," +
                    "\"stream\":false" +
                    "}";
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("http://127.0.0.1:11434/api/generate"))
                    .timeout(Duration.ofSeconds(20))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                String json = resp.body();
                String out = extractJsonField(json, "response");
                if (out != null && !out.isBlank()) return out.trim();
            }
        } catch (Exception ignored) {
        }

        // Try Hugging Face if token provided
        String hfToken = System.getenv("HF_TOKEN");
        String hfModel = getenvOr("HF_MODEL", "mistralai/Mistral-7B-Instruct-v0.2");
        if (hfToken != null && !hfToken.isBlank()) {
            try {
                String prompt = systemPrompt + "\n\nUser: " + q + "\nAssistant:";
                String payload = "{\"inputs\":\"" + escape(prompt) + "\",\"parameters\":{\"max_new_tokens\":256,\"temperature\":0.2}}";
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create("https://api-inference.huggingface.co/models/" + hfModel))
                        .timeout(Duration.ofSeconds(30))
                        .header("Authorization", "Bearer " + hfToken)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                        .build();
                HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
                if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                    String json = resp.body();
                    // HF returns an array of objects with 'generated_text'
                    String out = extractJsonField(json, "generated_text");
                    if (out != null && !out.isBlank()) return out.trim();
                }
            } catch (Exception ignored) {
            }
        }

        return "AI is not configured. To enable:\n" +
               "- Option A (local, free): Install Ollama and run a model, e.g. `ollama run llama3.2:3b-instruct`.\n" +
               "- Option B (cloud): Set HF_TOKEN env var for Hugging Face Inference API.\n" +
               "Meanwhile, try the built-in FAQ or rephrase your health question.\n\nThis is not medical advice.";
    }

    private static boolean isHealthRelated(String q) {
        String x = q.toLowerCase();
        String[] keys = {
                "health", "fitness", "exercise", "workout", "gym", "cardio", "strength",
                "nutrition", "diet", "calorie", "protein", "carb", "fat", "meal", "vitamin",
                "sleep", "stress", "hydration", "bmi", "wellness", "recovery", "mobility"
        };
        for (String k : keys) if (x.contains(k)) return true;
        // Also pass short questions (<= 3 words) to be helpful
        return x.split("\\s+").length <= 3;
    }

    private static String getenvOr(String key, String def) {
        String v = System.getenv(key);
        return (v == null || v.isBlank()) ? def : v;
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

    private static String extractJsonField(String json, String field) {
        // Very light extraction to avoid adding JSON deps
        String needle = '"' + field + '"' + ":";
        int i = json.indexOf(needle);
        if (i < 0) return null;
        int start = json.indexOf('"', i + needle.length());
        if (start < 0) return null;
        int end = json.indexOf('"', start + 1);
        if (end < 0) return null;
        return json.substring(start + 1, end);
    }
}