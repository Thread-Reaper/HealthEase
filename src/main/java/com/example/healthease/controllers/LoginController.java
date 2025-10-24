package com.example.healthease.controllers;

import com.example.healthease.models.User;
import com.example.healthease.utils.AuthService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.application.HostServices;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button forgotPasswordButton;
    @FXML private Button createAccountButton;

    private HostServices hostServices;

    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Username and password are required", Alert.AlertType.ERROR);
            return;
        }

        User user = AuthService.authenticate(username, password);
        if (user != null) {
            navigateToHomeScreen(user);
        } else {
            showAlert("Login Failed", "Invalid username or password", Alert.AlertType.ERROR);
        }
    }

    private void navigateToHomeScreen(User user) {
        try {
            Stage currentStage = (Stage) loginButton.getScene().getWindow();
            currentStage.close();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/healthease/Home.fxml"));
            Parent root = loader.load();

            HomeController homeController = loader.getController();
            homeController.setCurrentUser(user);
            homeController.setHostServices(hostServices);

            Stage homeStage = new Stage();
            homeStage.setTitle("HealthEase - Welcome " + user.getUsername());
            homeStage.setScene(new Scene(root));
            homeStage.setResizable(false);
            homeStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load home screen", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleForgotPassword(ActionEvent event) {
        showAlert("Password Recovery", "Contact admin@healthease.com", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleCreateAccount(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/healthease/Register.fxml"));
            Parent root = loader.load();

            Stage registerStage = new Stage();
            registerStage.setTitle("Create Account");
            registerStage.setScene(new Scene(root));
            registerStage.setResizable(false);
            registerStage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to load registration form", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}