package com.example.healthease.controllers;

import com.example.healthease.models.User;
import com.example.healthease.utils.DatabaseHandler;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;

public class MessengerController {
    @FXML private ListView<String> userListView;
    @FXML private TextField searchField;
    @FXML private Label titleLabel;

    private User currentUser;
    private ObservableList<String> allUsers = FXCollections.observableArrayList();

    public void setCurrentUser(User user) {
        this.currentUser = user;
        titleLabel.setText("Messenger - " + user.getUsername());
        loadUserList();
        setupSearch();
        userListView.setOnMouseClicked(e -> { if (e.getClickCount() == 2) handleOpenChat(); });
    }

    private void setupSearch() {
        if (searchField == null) return;
        searchField.textProperty().addListener((obs, old, val) -> applyFilter(val));
    }

    private void applyFilter(String q) {
        if (q == null || q.isBlank()) {
            userListView.setItems(allUsers);
            return;
        }
        String needle = q.toLowerCase();
        ObservableList<String> filtered = FXCollections.observableArrayList();
        for (String s : allUsers) {
            if (s.toLowerCase().contains(needle)) filtered.add(s);
        }
        userListView.setItems(filtered);
    }

    private void loadUserList() {
        allUsers.clear();
        String sql = "SELECT id, username, role FROM users WHERE id != ? ORDER BY username COLLATE NOCASE";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, currentUser.getId());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                allUsers.add(String.format("%d - %s (%s)", rs.getInt("id"), rs.getString("username"), rs.getString("role")));
            }
            userListView.setItems(allUsers);
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load user list: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleOpenChat() {
        String sel = userListView.getSelectionModel().getSelectedItem();
        if (sel == null || sel.isBlank()) { showAlert("Open Chat", "Please select a user.", Alert.AlertType.INFORMATION); return; }
        int dash = sel.indexOf(" - ");
        if (dash < 0) return;
        int peerId = Integer.parseInt(sel.substring(0, dash));
        String namePart = sel.substring(dash + 3);
        String peerName = namePart.contains(" (") ? namePart.substring(0, namePart.indexOf(" (")) : namePart;
        openChatWindow(peerId, peerName);
    }

    private void openChatWindow(int peerId, String peerName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/healthease/ChatWindow.fxml"));
            Parent root = loader.load();
            ChatController ctrl = loader.getController();
            ctrl.setParticipants(currentUser, peerId, peerName);

            Stage stage = new Stage();
            stage.setTitle("Chat - " + currentUser.getUsername() + " ↔ " + peerName);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to open chat: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleBack() {
        Stage stage = (Stage) userListView.getScene().getWindow();
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