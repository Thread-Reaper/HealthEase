package com.example.healthease.controllers;

import com.example.healthease.models.User;
import com.example.healthease.utils.DatabaseHandler;
import com.example.healthease.utils.RelayClient;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ChatController {
    @FXML private Label titleLabel;
    @FXML private ListView<String> messageListView;
    @FXML private TextField messageField;
    @FXML private Label networkStatusLabel;

    private User currentUser;
    private int peerUserId;
    private String peerUsername;

    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private final RelayClient relayClient = new RelayClient();

    private static final String RELAY_HOST = "127.0.0.1"; // hard-coded
    private static final int RELAY_PORT = 5050;            // hard-coded

    public void setParticipants(User me, int peerId, String peerName) {
        this.currentUser = me;
        this.peerUserId = peerId;
        this.peerUsername = peerName;
        titleLabel.setText("Chat with " + peerName);
        setupRelay();
        loadMessages();
        startMessageChecker();
    }

    private void setupRelay() {
        relayClient.setOnConnected(() -> javafx.application.Platform.runLater(() -> networkStatusLabel.setText("Online")));
        relayClient.setOnDisconnected(() -> javafx.application.Platform.runLater(() -> networkStatusLabel.setText("Offline")));
        relayClient.setOnMessage((from, text) -> javafx.application.Platform.runLater(() -> {
            String time = LocalDateTime.now().format(timeFormatter);
            messageListView.getItems().add(String.format("%s [%s]: %s", from, time, text));
            messageListView.scrollTo(messageListView.getItems().size() - 1);
        }));
        // Auto-connect
        new Thread(() -> {
            try { relayClient.connect(RELAY_HOST, RELAY_PORT, currentUser.getUsername()); } catch (IOException ignored) {}
        }, "Relay-Autoconnect").start();
    }

    @FXML
    private void handleSendMessage() {
        String message = messageField.getText().trim();
        if (message.isEmpty()) return;

        // Save locally
        String sql = "INSERT INTO messages (sender_id, receiver_id, content, timestamp) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, currentUser.getId());
            pstmt.setInt(2, peerUserId);
            pstmt.setString(3, message);
            pstmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to send message: " + e.getMessage(), Alert.AlertType.ERROR);
        }

        // Relay if connected
        if (relayClient.isConnected() && peerUsername != null && !peerUsername.isBlank()) {
            try { relayClient.sendMessage(peerUsername, message); } catch (IOException ignored) {}
        }

        String time = LocalDateTime.now().format(timeFormatter);
        messageListView.getItems().add(String.format("You [%s]: %s", time, message));
        messageField.clear();
        messageListView.scrollTo(messageListView.getItems().size() - 1);
    }

    private void loadMessages() {
        ObservableList<String> messages = FXCollections.observableArrayList();
        String sql = "SELECT m.*, u.username as sender_name FROM messages m " +
                "JOIN users u ON m.sender_id = u.id " +
                "WHERE (m.sender_id = ? AND m.receiver_id = ?) OR " +
                "(m.sender_id = ? AND m.receiver_id = ?) " +
                "ORDER BY m.timestamp";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, currentUser.getId());
            pstmt.setInt(2, peerUserId);
            pstmt.setInt(3, peerUserId);
            pstmt.setInt(4, currentUser.getId());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String senderName = rs.getInt("sender_id") == currentUser.getId() ? "You" : rs.getString("sender_name");
                String time = rs.getTimestamp("timestamp").toLocalDateTime().format(timeFormatter);
                messages.add(String.format("%s [%s]: %s", senderName, time, rs.getString("content")));
            }
            messageListView.setItems(messages);
            if (!messages.isEmpty()) messageListView.scrollTo(messages.size() - 1);
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load messages: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void startMessageChecker() {
        Thread t = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(2000);
                    javafx.application.Platform.runLater(this::loadMessages);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }, "Chat-Refresher");
        t.setDaemon(true);
        t.start();
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) messageField.getScene().getWindow();
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