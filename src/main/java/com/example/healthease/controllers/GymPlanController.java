package com.example.healthease.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class GymPlanController {

    @FXML
    private ComboBox<String> fitnessLevelCombo, goalCombo, timeCombo;

    @FXML
    private TextArea planTextArea;

    @FXML
    private Button generateButton;

    @FXML
    private void initialize() {
        fitnessLevelCombo.getItems().addAll("Beginner", "Intermediate", "Advanced");
        goalCombo.getItems().addAll("Weight Loss", "Muscle Gain", "Endurance");
        timeCombo.getItems().addAll("30 mins", "45 mins", "60 mins");

        generateButton.setOnAction(e -> {
            String level = fitnessLevelCombo.getValue();
            String goal = goalCombo.getValue();
            String time = timeCombo.getValue();

            if (level == null || goal == null || time == null) {
                planTextArea.setText("Please fill in all the fields.");
                return;
            }

            StringBuilder plan = new StringBuilder("Your Gym Plan\n\n");

            // Header
            plan.append("Level: ").append(level).append("\n")
                .append("Session: ").append(time).append("\n")
                .append("Goal: ").append(goal).append("\n\n");

            // Basic time parsing
            int minutes = time.contains("60") ? 60 : time.contains("45") ? 45 : 30;
            int warm = Math.max(5, Math.min(10, minutes/6));
            int cool = warm;
            int main = Math.max(10, minutes - warm - cool);

            // Warm-up
            plan.append("Warm-up ("+warm+" min): dynamic mobility + light cardio (rower/bike/treadmill).\n\n");

            // Main focus based on goal
            if (goal.equals("Weight Loss")) {
                plan.append("Main Focus: Moderate strength + conditioning.\n")
                    .append("- Strength Block ("+ (main/2) +" min): full-body circuit 2–3 rounds — Goblet Squat, Push-ups/Incline Push-ups, Dumbbell Row, Split Squat, Plank (30–45s). Minimal rest.\n")
                    .append("- Conditioning ("+ (main/2) +" min): intervals 30s hard / 60s easy on bike/rower or brisk incline walk.\n");
            } else if (goal.equals("Muscle Gain")) {
                plan.append("Main Focus: Hypertrophy with progressive overload.\n")
                    .append("- Primary Lifts: 3–4 sets of 6–12 reps — Squat/Leg Press, Bench/DB Press, Row/Pull-down.\n")
                    .append("- Accessories: 2–3 sets of 10–15 reps — RDL/Hamstring Curl, Lateral Raises, Curls/Triceps.\n")
                    .append("- Rest: 60–90s (primary up to 120s).\n");
            } else { // Endurance
                plan.append("Main Focus: Aerobic base + strength endurance.\n")
                    .append("- Cardio Steady ("+ (main*2/3) +" min): easy–moderate pace (Z2).\n")
                    .append("- Strength Endurance ("+ (main/3) +" min): 2–3 sets — Deadlift/KB Swing, Step-ups, Pull-ups/Rows, Core carry (Farmer/Front).\n");
            }

            // Level-specific guidance
            switch (level) {
                case "Beginner":
                    plan.append("\nTips for Beginners:\n")
                        .append("- Start with lighter loads; focus on form.\n")
                        .append("- 2–3 sessions/week with at least 1 rest day between.\n")
                        .append("- Keep RPE around 6–7 (leave 2–3 reps in reserve).\n");
                    break;
                case "Intermediate":
                    plan.append("\nTips for Intermediates:\n")
                        .append("- Add weight or reps weekly if last set is strong.\n")
                        .append("- 3–5 sessions/week depending on recovery.\n")
                        .append("- Mix steady cardio with 1 HIIT day for conditioning.\n");
                    break;
                default:
                    plan.append("\nTips for Advanced:\n")
                        .append("- Use periodization: heavy/volume weeks and deloads.\n")
                        .append("- Track lifts and aim for small weekly progress.\n")
                        .append("- Include mobility to maintain range of motion.\n");
            }

            // Cool-down and general guidelines
            plan.append("\nCool-down ("+cool+" min): easy cardio + stretch hips, chest, lats.\n")
                .append("Hydration & Recovery: 7–9h sleep, protein 1.6–2.0 g/kg, \n")
                .append(goal.equals("Weight Loss") ? "calorie deficit 300–500 kcal.\n" : goal.equals("Muscle Gain") ? "small surplus 250–350 kcal.\n" : "focus carbs around key sessions.\n");

            planTextArea.setText(plan.toString());
        });
    }
}
