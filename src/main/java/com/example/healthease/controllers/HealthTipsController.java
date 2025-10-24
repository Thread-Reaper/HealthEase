package com.example.healthease.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;

import java.util.*;

public class HealthTipsController {

    @FXML private TextArea questionInput;
    @FXML private ListView<String> tipsList; // Used to show the answer lines
    @FXML private ListView<String> faqList;  // New: shows 20+ FAQ questions
    @FXML private TextArea answerArea;       // New: shows the selected FAQ answer

    private final LinkedHashMap<String, String> faqMap = new LinkedHashMap<>();

    @FXML
    private void initialize() {
        seedFaqs();
        if (faqList != null) {
            faqList.setItems(FXCollections.observableArrayList(faqMap.keySet()));
            faqList.getSelectionModel().selectedItemProperty().addListener((obs, o, q) -> showAnswer(q));
            // Select first by default
            if (!faqMap.isEmpty()) faqList.getSelectionModel().select(0);
        }
    }

    private void seedFaqs() {
        faqMap.clear();
        faqMap.put("How do I start exercising safely as a beginner?",
                "Start with 10–20 minutes of brisk walking 3–4x/week. Add light bodyweight moves (squats, wall push-ups, glute bridges). Focus on form, not speed. Increase total time by ~10% per week.");
        faqMap.put("What is a balanced plate for meals?",
                "Half non‑starchy vegetables, a quarter lean protein (chicken, fish, tofu, beans), a quarter whole grains or starchy veg, plus water. Add fruit or yogurt if hungry.");
        faqMap.put("Tips to lose weight sustainably?",
                "Aim for a small calorie deficit (300–500 kcal/day). Prioritize protein and fiber, limit sugary drinks, move daily (8–10k steps), and strength train 2x/week.");
        faqMap.put("How much water should I drink?",
                "A practical target is 6–8 glasses per day. More if it’s hot or you’re active. Use urine color (pale straw) as a simple guide.");
        faqMap.put("I sit all day. How can I stay active?",
                "Set a timer to stand and move 2–3 minutes every 60–90 minutes. Add a short walk after meals. Try desk stretches and evening walks.");
        faqMap.put("How many hours of sleep do adults need?",
                "Most adults do best with 7–9 hours. Keep a consistent schedule, reduce screens 60 minutes before bed, and keep your room dark, cool, and quiet.");
        faqMap.put("Healthy snack ideas?",
                "Greek yogurt with berries, fruit and nuts, hummus with carrots, boiled eggs, whole‑grain crackers with cheese, or a protein smoothie.");
        faqMap.put("How often should I strength train?",
                "2–3 nonconsecutive days per week. 6–10 sets per muscle group weekly is a solid start. Cover push, pull, legs, and core.");
        faqMap.put("Cardio vs. strength—what mix is good?",
                "Aim for 150 min/week moderate cardio (or 75 min vigorous) plus 2+ strength sessions. Mix depends on your goals; combine both for health.");
        faqMap.put("Any tips to manage stress?",
                "Try 4‑7‑8 breathing (4s inhale, 7s hold, 8s exhale) for 3–5 minutes. Schedule short walks, limit late caffeine, and keep a simple wind‑down routine.");
        faqMap.put("Breakfast ideas for energy?",
                "Oats + milk + nuts/fruit; eggs with whole‑grain toast and veggies; Greek yogurt parfait; smoothie with fruit, spinach, and protein.");
        faqMap.put("How to build a daily movement habit?",
                "Attach it to an existing routine (after brushing teeth, take a 10‑minute walk). Keep it small and consistent, and track streaks.");
        faqMap.put("How to avoid injury when working out?",
                "Warm up 5–10 minutes, progress gradually (≈10%/week), focus on technique, and rest when sore. If sharp pain persists, seek professional advice.");
        faqMap.put("What’s a good pre‑workout snack?",
                "1–2 hours before: fruit + yogurt, toast with peanut butter, or a small rice bowl with chicken. Keep it light if training soon.");
        faqMap.put("How much protein do I need?",
                "General guidance: 1.2–2.0 g/kg/day depending on activity and goals. Spread across meals for better satiety and muscle support.");
        faqMap.put("How to cut down on sugar?",
                "Swap sugary drinks for water/sparkling water, choose fruit over sweets, and read labels. Keep desserts planned (e.g., once or twice weekly)." );
        faqMap.put("Best way to track progress?",
                "Pick 2–3 metrics: energy, sleep quality, steps, workout consistency, or waist measurement. Review weekly; look for trends, not perfection.");
        faqMap.put("Healthy lunchbox ideas?",
                "Whole‑grain wrap with chicken/tofu and veggies; rice + beans + salsa + avocado; quinoa salad with chickpeas and roasted veggies.");
        faqMap.put("How to stay motivated long term?",
                "Set small process goals (walk 15 min daily), celebrate consistency, and plan for imperfect days. Make it enjoyable—music, a buddy, or outdoors.");
        faqMap.put("Simple home workout plan?",
                "3x/week: 3 rounds of squats, push‑ups (or wall), rows (band/backpack), hip hinges, and planks. 8–15 reps each, rest 1 minute between moves.");
    }

    private void showAnswer(String question) {
        if (question == null) return;
        String answer = faqMap.getOrDefault(question, "No answer available.");
        if (answerArea != null) {
            answerArea.setText(answer + "\n\nNote: General wellness guidance only—consult a professional for personal medical advice.");
        } else {
            // Fallback: show in tipsList if answerArea is not present
            tipsList.getItems().setAll(Arrays.asList(answer.split("\\r?\\n")));
        }
    }

    @FXML
    private void handleGetTips() {
        String query = questionInput != null ? questionInput.getText().trim().toLowerCase() : "";
        if (query.isEmpty()) {
            if (tipsList != null) tipsList.getItems().setAll("Please enter a health-related question or select an FAQ on the left.");
            return;
        }

        // Very simple keyword match to best FAQ
        String best = null; int bestScore = 0;
        for (String q : faqMap.keySet()) {
            int score = 0;
            for (String token : query.split("\\s+")) {
                if (token.length() > 3 && q.toLowerCase().contains(token)) score++;
            }
            if (score > bestScore) { bestScore = score; best = q; }
        }
        if (best == null) best = faqMap.keySet().iterator().next();
        showAnswer(best);
        if (faqList != null) faqList.getSelectionModel().select(best);
    }
}
