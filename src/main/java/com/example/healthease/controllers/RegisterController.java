package com.example.healthease.controllers;

import com.example.healthease.utils.AuthService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class RegisterController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField emailField;
    @FXML private Button registerButton;
    @FXML private Button backButton;

    @FXML
    private void handleRegister() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String email = emailField.getText();

        if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
            showAlert("Error", "All fields are required", Alert.AlertType.ERROR);
            return;
        }

        if (AuthService.registerUser(username, password, email)) {
            showAlert("Success", "Registration successful!", Alert.AlertType.INFORMATION);
            handleBack();
        } else {
            showAlert("Error", "Registration failed. Username or email may already exist.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleBack() {
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}