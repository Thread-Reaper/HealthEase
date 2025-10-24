package com.example.healthease.controllers;

import com.example.healthease.models.User;
import com.example.healthease.utils.DatabaseHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MealPlanController implements UserAwareController {

    @FXML private TextField ageField;
    @FXML private ComboBox<String> genderCombo;
    @FXML private ComboBox<String> activityLevelCombo;
    @FXML private ComboBox<String> goalCombo;
    @FXML private ComboBox<String> dietPreferenceCombo;
    @FXML private TextField allergiesField;

    @FXML private Label breakfastLabel;
    @FXML private Label lunchLabel;
    @FXML private Label dinnerLabel;
    @FXML private Label snacksLabel;

    private User currentUser;

    @Override
    public void setUser(User user) {
        this.currentUser = user;
    }

    @FXML
    public void initialize() {
        genderCombo.getItems().addAll("Male", "Female", "Other");
        activityLevelCombo.getItems().addAll("Low", "Moderate", "High");
        goalCombo.getItems().addAll("Weight Loss", "Maintain", "Gain Muscle");
        dietPreferenceCombo.getItems().addAll("Vegetarian", "Non-Vegetarian", "Vegan", "Keto");
    }

    @FXML
    private void generateMealPlan() {
        String ageText = ageField.getText();
        String gender = genderCombo.getValue();
        String activityLevel = activityLevelCombo.getValue();
        String goal = goalCombo.getValue();
        String dietPref = dietPreferenceCombo.getValue();
        String allergiesText = allergiesField.getText();

        if (ageText == null || ageText.isBlank() || gender == null || activityLevel == null || goal == null || dietPref == null) {
            setOutputs("Please fill all required fields.", "", "", "");
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageText);
        } catch (NumberFormatException e) {
            setOutputs("Invalid age entered.", "", "", "");
            return;
        }

        Set<String> allergies = parseAllergies(allergiesText);

        int targetCalories = estimateCalories(age, gender, activityLevel, goal);
        Map<String, String> meals = buildMeals(dietPref, targetCalories, allergies, goal, gender, age);

        setOutputs(meals.get("breakfast"), meals.get("lunch"), meals.get("dinner"), meals.get("snacks"));

        persistMealPlanIfPossible(meals);
    }

    private void setOutputs(String b, String l, String d, String s) {
        breakfastLabel.setText(b);
        lunchLabel.setText(l);
        dinnerLabel.setText(d);
        snacksLabel.setText(s);
    }

    private Set<String> parseAllergies(String text) {
        Set<String> a = new HashSet<>();
        if (text != null && !text.isBlank()) {
            for (String part : text.toLowerCase().split(",")) {
                String t = part.trim();
                if (!t.isEmpty()) a.add(t);
            }
        }
        return a;
    }

    // Estimated daily calories using USDA-style age/gender/activity brackets,
    // then adjusted for goal. Values are conservative midpoints.
    private int estimateCalories(int age, String gender, String activity, String goal) {
        int base; // midpoint estimate per bracket
        boolean male = "Male".equalsIgnoreCase(gender);

        if (age <= 3) base = male ? 1100 : 1000;
        else if (age <= 8) base = male ? 1550 : 1450;
        else if (age <= 13) base = male ? 2050 : 1900;
        else if (age <= 18) base = male ? 2600 : 2100;
        else if (age <= 30) base = male ? 2700 : 2100;
        else if (age <= 50) base = male ? 2550 : 2000;
        else base = male ? 2300 : 1800;

        switch (activity) {
            case "Low": base -= 200; break;
            case "Moderate": /* no change */ break;
            case "High": base += 300; break;
            default: break;
        }

        switch (goal) {
            case "Weight Loss": base -= 400; break; // modest deficit
            case "Gain Muscle": base += 300; break; // slight surplus
            default: break; // maintain
        }

        return Math.max(base, 1200); // floor for safety
    }

    private Map<String, String> buildMeals(String diet, int kcal, Set<String> allergies,
                                           String goal, String gender, int age) {
        Map<String, String> m = new HashMap<>();

        // Distribute calories across meals
        double b = 0.25, l = 0.35, d = 0.30, s = 0.10; // breakfast/lunch/dinner/snacks
        int bk = (int) Math.round(kcal * b);
        int lk = (int) Math.round(kcal * l);
        int dk = (int) Math.round(kcal * d);
        int sk = (int) Math.round(kcal * s);

        // Choose meal templates with portions and adjust for common allergies
        String breakfast; String lunch; String dinner; String snacks;
        switch (diet) {
            case "Vegetarian":
                breakfast = String.format("%d kcal: Greek yogurt (200g) with oats (40g) and berries (100g)", bk);
                lunch = String.format("%d kcal: Paneer (120g) tikka, brown rice (120g cooked), salad", lk);
                dinner = String.format("%d kcal: Lentil dal (1 cup) with mixed veg saute and chapati (2)", dk);
                snacks = String.format("%d kcal: Fruit (1 medium) + nuts (20g)", sk);
                break;
            case "Vegan":
                breakfast = String.format("%d kcal: Tofu scramble (150g tofu) with whole-grain toast (2) and avocado (50g)", bk);
                lunch = String.format("%d kcal: Quinoa bowl (150g cooked) with chickpeas (100g) and veggies", lk);
                dinner = String.format("%d kcal: Red beans (1 cup) with brown rice (120g cooked) and greens", dk);
                snacks = String.format("%d kcal: Hummus (50g) with carrots/cucumber", sk);
                break;
            case "Keto":
                breakfast = String.format("%d kcal: Eggs (3) with avocado (80g) and spinach in olive oil", bk);
                lunch = String.format("%d kcal: Grilled chicken (150g) salad with olive oil dressing", lk);
                dinner = String.format("%d kcal: Salmon (150g) with asparagus and butter", dk);
                snacks = String.format("%d kcal: Cheese (40g) and olives (30g)", sk);
                break;
            default: // Non-Vegetarian
                breakfast = String.format("%d kcal: Omelette (2 eggs) with whole-grain toast (2) and fruit (1)", bk);
                lunch = String.format("%d kcal: Chicken breast (150g) with quinoa (140g cooked) and salad", lk);
                dinner = String.format("%d kcal: Fish (150g) curry with brown rice (150g cooked) and veg", dk);
                snacks = String.format("%d kcal: Greek yogurt (150g) or mixed nuts (20g)", sk);
        }

        if (contains(allergies, "nuts")) {
            breakfast = breakfast.replace("nuts", "seeds");
            snacks = snacks.replace("nuts", "seeds");
        }
        if (contains(allergies, "dairy")) {
            breakfast = breakfast.replace("Greek yogurt", "soy yogurt");
            lunch = lunch.replace("Paneer", "tofu");
            snacks = snacks.replace("Cheese", "olives").replace("Greek yogurt", "soy yogurt");
        }
        if (contains(allergies, "gluten")) {
            breakfast = breakfast.replace("toast", "gluten-free toast").replace("chapati", "gluten-free flatbread");
            lunch = lunch.replace("toast", "gluten-free toast");
            dinner = dinner.replace("chapati", "gluten-free flatbread");
        }

        if ("Weight Loss".equals(goal)) {
            snacks = snacks + "; prioritize high-volume, low-calorie foods";
        } else if ("Gain Muscle".equals(goal)) {
            breakfast += "; add whey/plant protein shake";
            dinner += "; include extra protein serving";
        }

        if (age > 50 && "Female".equalsIgnoreCase(gender)) {
            lunch += "; ensure calcium + vitamin D sources";
        }

        m.put("breakfast", breakfast);
        m.put("lunch", lunch);
        m.put("dinner", dinner);
        m.put("snacks", snacks);
        return m;
    }

    private boolean contains(Set<String> set, String key) {
        for (String s : set) {
            if (s.contains(key)) return true;
        }
        return false;
    }

    private void persistMealPlanIfPossible(Map<String, String> meals) {
        if (currentUser == null) return;
        String sql = "INSERT INTO meal_plans (user_id, breakfast, lunch, dinner, snacks) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, currentUser.getId());
            ps.setString(2, meals.get("breakfast"));
            ps.setString(3, meals.get("lunch"));
            ps.setString(4, meals.get("dinner"));
            ps.setString(5, meals.get("snacks"));
            ps.executeUpdate();
        } catch (SQLException e) {
            snacksLabel.setText(snacksLabel.getText() + "\n(Note: failed to save plan: " + e.getMessage() + ")");
        }
    }
}
