package com.example.healthease.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;

import java.util.LinkedHashMap;
import java.util.Map;

public class HealthTipsController {

    @FXML private ComboBox<String> categoryCombo;
    @FXML private ListView<String> questionList;
    @FXML private TextArea answerArea;

    private final LinkedHashMap<String, LinkedHashMap<String, String>> faqsByCategory = new LinkedHashMap<>();

    @FXML
    private void initialize() {
        seedFaqs();
        ObservableList<String> cats = FXCollections.observableArrayList(faqsByCategory.keySet());
        if (categoryCombo != null) {
            categoryCombo.setItems(cats);
            if (!cats.isEmpty()) categoryCombo.getSelectionModel().select(0);
            categoryCombo.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> loadQuestions(n));
        }
        // Load first category questions
        if (!cats.isEmpty()) loadQuestions(cats.get(0));
    }

    private void loadQuestions(String category) {
        if (category == null) return;
        LinkedHashMap<String, String> map = faqsByCategory.get(category);
        if (map == null) return;
        if (questionList != null) {
            questionList.setItems(FXCollections.observableArrayList(map.keySet()));
            if (!map.isEmpty()) questionList.getSelectionModel().select(0);
            questionList.getSelectionModel().selectedItemProperty().addListener((obs, o, q) -> showAnswer(category, q));
        }
        // Show first answer
        if (!map.isEmpty()) showAnswer(category, map.keySet().iterator().next());
    }

    private void showAnswer(String category, String question) {
        if (category == null || question == null) return;
        LinkedHashMap<String, String> map = faqsByCategory.get(category);
        if (map == null) return;
        String answer = map.getOrDefault(question, "No answer available.");
        if (answerArea != null) {
            answerArea.setText(answer);
        }
    }

    private void seedFaqs() {
        // Categories: Nutrition, Fitness, Sleep, Stress, Hydration, Weight Management
        LinkedHashMap<String, String> nutrition = new LinkedHashMap<>();
        nutrition.put("What is a balanced plate?", "Aim for half vegetables, a quarter lean protein (chicken, fish, tofu, beans), and a quarter whole grains or starchy veg. Add water and fruit.");
        nutrition.put("How much protein do I need?", "General guide: 1.2 to 2.0 g per kg body weight per day, depending on activity and goals.");
        nutrition.put("Healthy breakfast ideas?", "Oats with milk and fruit, eggs with whole grain toast and veggies, Greek yogurt parfait, or a fruit-protein smoothie.");
        nutrition.put("Best snacks for energy?", "Fruit with nuts or yogurt, hummus with carrots, boiled eggs, whole grain crackers with cheese, or a protein smoothie.");
        nutrition.put("How to reduce sugar?", "Swap sugary drinks for water or sparkling water, choose fruit over sweets, read labels, and plan treats instead of daily snacking.");
        nutrition.put("Are carbs bad?", "No. Choose mostly fiber-rich carbs like oats, brown rice, potatoes, beans, fruit, and vegetables. Adjust portions for your goals.");
        nutrition.put("Do I need supplements?", "Food first. Supplements can help fill gaps (e.g., vitamin D, omega-3) but ask a professional if you have medical conditions.");
        nutrition.put("How to eat out and stay healthy?", "Prioritize protein and vegetables, choose baked or grilled options, limit creamy sauces, and watch sugary drinks.");
        nutrition.put("What about intermittent fasting?", "It can help some people manage calories. Focus on overall quality, protein, and consistency. Not required for results.");
        nutrition.put("How to increase fiber?", "Add fruit, vegetables, legumes, and whole grains. Increase gradually and drink water to reduce stomach discomfort.");
        faqsByCategory.put("Nutrition", nutrition);

        LinkedHashMap<String, String> fitness = new LinkedHashMap<>();
        fitness.put("Beginner exercise plan?", "Start with walking 15 to 20 minutes most days plus 2 short strength sessions: squats, push-ups (wall if needed), rows, and planks.");
        fitness.put("How often to strength train?", "Aim for 2 to 3 nonconsecutive days per week. Cover push, pull, legs, and core. Focus on good form.");
        fitness.put("Cardio vs strength balance?", "A practical mix is 150 minutes per week of moderate cardio plus 2 or more strength sessions. Adjust for goals.");
        fitness.put("Warm-up ideas?", "5 to 10 minutes of easy cardio and dynamic mobility: leg swings, hip circles, arm circles, light bodyweight moves.");
        fitness.put("How to avoid injury?", "Progress gradually (about 10 percent per week), use controlled form, and rest when you feel sharp pain or excessive fatigue.");
        fitness.put("At-home workouts?", "Use bodyweight circuits: squats, lunges, push-ups, hip hinges, rows with bands or backpack, and planks.");
        fitness.put("How to build consistency?", "Attach workouts to a routine time, start small, track sessions, and keep rest days. Consistency beats perfection.");
        fitness.put("Soreness vs pain?", "Mild soreness after new training is normal and fades in 1 to 3 days. Sharp joint pain suggests backing off and adjusting.");
        fitness.put("How to improve flexibility?", "Do light dynamic mobility before workouts and short stretches after. Train full range of motion in strength work.");
        fitness.put("HIIT tips?", "Keep intervals short at first (20 to 30 seconds hard, 60 to 90 seconds easy). 6 to 10 rounds are plenty for beginners.");
        faqsByCategory.put("Fitness", fitness);

        LinkedHashMap<String, String> sleep = new LinkedHashMap<>();
        sleep.put("How many hours do I need?", "Most adults feel best with 7 to 9 hours per night. Aim for a consistent sleep and wake time.");
        sleep.put("How to fall asleep faster?", "Reduce screens 60 minutes before bed, dim lights, keep room cool and dark, and use a short wind-down routine.");
        sleep.put("Best sleep environment?", "Dark, quiet, cool (around 18 to 20 C). Use blackout curtains, white noise, and a comfortable mattress.");
        sleep.put("Napping tips?", "Short naps of 10 to 20 minutes earlier in the day can boost alertness. Avoid long naps late in the afternoon.");
        sleep.put("Caffeine timing?", "Avoid caffeine within 6 to 8 hours of bedtime. Watch hidden sources like tea, soda, energy drinks, and chocolate.");
        sleep.put("Weekends and sleep?", "Keep a similar schedule on weekends. Large shifts can make Monday harder and disrupt rhythm.");
        sleep.put("Waking up at night?", "Keep lights low, avoid screens, and try calm breathing. If awake for long, get up briefly and do something relaxing.");
        sleep.put("Exercise effect on sleep?", "Regular activity improves sleep quality. Intense evening workouts can delay sleep for some people.");
        sleep.put("Food and sleep?", "Large meals late can disturb sleep. A light snack with protein and carbs (yogurt and fruit) may help some.");
        sleep.put("Tracking sleep?", "Wearables can be helpful trends but are imperfect. Focus on how you feel and daily energy.");
        faqsByCategory.put("Sleep", sleep);

        LinkedHashMap<String, String> stress = new LinkedHashMap<>();
        stress.put("Quick stress relief?", "Try 4-7-8 breathing for 3 to 5 minutes, a short walk, sunlight, or a brief journal to clear your head.");
        stress.put("Daily habits to reduce stress?", "Regular movement, consistent sleep, outdoor time, and simple routines help build resilience.");
        stress.put("How to start mindfulness?", "Begin with 5 minutes of relaxed breathing or a short guided session. Focus on the breath and let thoughts pass.");
        stress.put("Managing workload?", "Use a short task list, set realistic priorities, time-box focused work, and include small breaks.");
        stress.put("Evening wind-down?", "Dim lights, avoid heavy screens, stretch gently, read paper books, and set tomorrow's plan in 5 minutes.");
        stress.put("Caffeine and stress?", "Too much caffeine can increase anxiety. Keep total moderate and avoid late intake.");
        stress.put("Social support?", "Talk with friends or family, join a group, or schedule a weekly check-in. Connection lowers stress.");
        stress.put("Overthinking tips?", "Write thoughts quickly on paper, set a 10-minute worry window, then return to action or a walk.");
        stress.put("Digital overload?", "Turn off nonessential notifications, batch messages, and set phone-free times like meals and before bed.");
        stress.put("Professional help?", "If stress affects daily life for weeks, consider speaking with a counselor or healthcare professional.");
        faqsByCategory.put("Stress", stress);

        LinkedHashMap<String, String> hydration = new LinkedHashMap<>();
        hydration.put("How much water per day?", "A practical goal is about 6 to 8 glasses. Drink more in heat or activity. Pale yellow urine is a simple guide.");
        hydration.put("Hydration for workouts?", "Drink a glass 1 to 2 hours before, sip during longer sessions, and rehydrate after. Add electrolytes for heavy sweating.");
        hydration.put("Signs of dehydration?", "Dark urine, dry mouth, headache, low energy, and dizziness. Drink water and rest in a cool place.");
        hydration.put("Can I count tea and coffee?", "Yes, they contribute to fluid intake, but large amounts of caffeine can increase bathroom trips for some.");
        hydration.put("Water or sports drink?", "Water is fine for most. Use sports drinks for long, sweaty sessions or very hot conditions.");
        hydration.put("Hydration at work?", "Keep a bottle on your desk and set small sip reminders every 60 to 90 minutes.");
        hydration.put("Does fruit help hydration?", "Yes. Foods like watermelon, oranges, cucumber, and soups add fluids to your day.");
        hydration.put("Clear skin and water?", "Hydration supports skin health, but overall diet, sleep, and stress also matter.");
        hydration.put("How to build the habit?", "Drink a glass after waking, with each meal, and before workouts. Keep water visible.");
        hydration.put("Too much water?", "Excess can dilute electrolytes. Listen to thirst and avoid forcing extreme amounts in short periods.");
        faqsByCategory.put("Hydration", hydration);

        LinkedHashMap<String, String> weight = new LinkedHashMap<>();
        weight.put("How to lose weight safely?", "Create a small calorie deficit (about 300 to 500 kcal per day), prioritize protein and fiber, and be active daily.");
        weight.put("Best diet for weight loss?", "The one you can stick to. Choose whole foods, adequate protein, and meals you enjoy to stay consistent.");
        weight.put("Role of strength training?", "Lifting helps keep muscle while losing fat. Aim for 2 to 3 sessions per week alongside walking and activity.");
        weight.put("Tracking progress?", "Use 2 to 3 metrics: energy, sleep, steps, workout consistency, or waist measurement. Review weekly trends.");
        weight.put("Plateaus?", "Normal. Check portions, steps, sleep, and stress. Make one small change and give it 1 to 2 weeks.");
        weight.put("Cheat meals?", "Plan treats so they fit your week. Avoid all-or-nothing swings; focus on overall balance.");
        weight.put("Late-night eating?", "Keep dinner satisfying with protein and fiber. If hungry later, choose a light protein-carb snack.");
        weight.put("Calories and accuracy?", "Labels can vary. Use portion awareness and adjust based on progress, not perfection.");
        weight.put("Walking and fat loss?", "Daily steps improve calorie burn and health. Combine with strength training and nutrition basics.");
        weight.put("How fast should I lose?", "A steady 0.25 to 0.75 kg per week is realistic for many people. Faster loss is harder to maintain.");
        faqsByCategory.put("Weight Management", weight);
    }
}