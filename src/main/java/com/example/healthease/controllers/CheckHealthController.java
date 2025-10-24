package com.example.healthease.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class CheckHealthController {
    @FXML private TextField ageField;
    @FXML private TextField weightField;
    @FXML private TextField heightField;
    @FXML private TextArea resultArea;

    @FXML
    public void evaluateHealth() {
        try {
            int age = Integer.parseInt(ageField.getText());
            double weight = Double.parseDouble(weightField.getText());
            double height = Double.parseDouble(heightField.getText());

            double bmi = weight / Math.pow(height / 100.0, 2);
            StringBuilder result = new StringBuilder("Health Summary:\n\n");
            result.append("Age: ").append(age).append("\n");
            result.append("BMI: ").append(String.format("%.2f", bmi)).append(" - ");
            if (bmi < 18.5) result.append("Underweight\n");
            else if (bmi < 24.9) result.append("Normal\n");
            else if (bmi < 29.9) result.append("Overweight\n");
            else result.append("Obese\n");

            result.append("\nGeneral tips: maintain a balanced diet, be physically active, and consult a professional for personalized advice.");
            resultArea.setText(result.toString());
        } catch (Exception e) {
            resultArea.setText("Please enter age, weight (kg), and height (cm) correctly.");
        }
    }
}

