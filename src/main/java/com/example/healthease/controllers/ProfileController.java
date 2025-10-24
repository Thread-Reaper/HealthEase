package com.example.healthease.controllers;

import com.example.healthease.models.User;
import com.example.healthease.utils.AuthService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class ProfileController {
    @FXML private Label usernameLabel;
    @FXML private Label emailLabel;
    @FXML private Label roleLabel;

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;

    private User currentUser;

    public void setCurrentUser(User user) {
        this.currentUser = user;
        updateProfileInfo();
    }

    private void updateProfileInfo() {
        if (currentUser == null) return;
        usernameLabel.setText("Username: " + currentUser.getUsername());
        emailLabel.setText("Email: " + currentUser.getEmail());
        roleLabel.setText("Role: " + currentUser.getRole());

        if (usernameField != null) usernameField.setText(currentUser.getUsername());
        if (emailField != null) emailField.setText(currentUser.getEmail());
    }

    @FXML
    private void handleSaveProfile() {
        if (currentUser == null) return;

        String newUsername = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String newEmail = emailField.getText() == null ? "" : emailField.getText().trim();

        if (newUsername.isEmpty() || newEmail.isEmpty()) {
            showAlert("Error", "Name and email cannot be empty.", Alert.AlertType.ERROR);
            return;
        }

        boolean noChanges = newUsername.equals(currentUser.getUsername()) && newEmail.equals(currentUser.getEmail());
        if (noChanges) {
            showAlert("Info", "No changes to save.", Alert.AlertType.INFORMATION);
            return;
        }

        boolean updated = AuthService.updateUserProfile(currentUser.getId(), newUsername, newEmail);
        if (updated) {
            currentUser = new User(currentUser.getId(), newUsername, newEmail, currentUser.getRole());
            updateProfileInfo();
            showAlert("Success", "Profile updated successfully.", Alert.AlertType.INFORMATION);
        } else {
            showAlert("Error", "Failed to update profile. Username or email may already exist.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleChangePassword() {
        if (currentUser == null) return;

        String currentPassword = currentPasswordField.getText() == null ? "" : currentPasswordField.getText();
        String newPassword = newPasswordField.getText() == null ? "" : newPasswordField.getText();

        if (currentPassword.isEmpty() || newPassword.isEmpty()) {
            showAlert("Error", "Current and new password are required.", Alert.AlertType.ERROR);
            return;
        }
        if (newPassword.length() < 4) {
            showAlert("Error", "New password must be at least 4 characters.", Alert.AlertType.ERROR);
            return;
        }

        boolean changed = AuthService.changePassword(currentUser.getId(), currentPassword, newPassword);
        if (changed) {
            currentPasswordField.clear();
            newPasswordField.clear();
            showAlert("Success", "Password changed successfully.", Alert.AlertType.INFORMATION);
        } else {
            showAlert("Error", "Current password is incorrect or change failed.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleBack() {
        Stage stage = (Stage) usernameLabel.getScene().getWindow();
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