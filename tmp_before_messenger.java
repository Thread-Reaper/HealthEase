package com.example.healthease.controllers;

import com.example.healthease.models.User;
import com.example.healthease.utils.DatabaseHandler;
import com.example.healthease.utils.RelayClient;
import java.io.IOException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MessengerController {
    @FXML private ListView<String> userListView;
    @FXML private ListView<String> messageListView;
    @FXML private TextField messageField;
    @FXML private Label titleLabel;
    // Network relay UI
    @FXML private TextField serverHostField;
    @FXML private TextField serverPortField;
    @FXML private TextField peerField;
    @FXML private Label networkStatusLabel;

    private User currentUser;
    private int selectedUserId = -1;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private final RelayClient relayClient = new RelayClient();

    public void setCurrentUser(User user) {
        this.currentUser = user;
        titleLabel.setText("Messenger - " + user.getUsername());
        loadUserList();
        startMessageChecker(); // Start checking for new messages

        // Setup relay client callbacks
        relayClient.setOnConnected(() -> javafx.application.Platform.runLater(() -> networkStatusLabel.setText("Online")));
        relayClient.setOnDisconnected(() -> javafx.application.Platform.runLater(() -> networkStatusLabel.setText("Offline")));
        relayClient.setOnMessage((from, text) -> javafx.application.Platform.runLater(() -> {
            String time = LocalDateTime.now().format(timeFormatter);
            messageListView.getItems().add(String.format("%s [%s]: %s", from, time, text));
            messageListView.scrollTo(messageListView.getItems().size() - 1);
        }));
    }

    private void loadUserList() {
        ObservableList<String> users = FXCollections.observableArrayList();
        String sql = "SELECT id, username, role FROM users WHERE id != ? ORDER BY username";

        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, currentUser.getId());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                users.add(String.format("%d - %s (%s)",
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("role")));
            }

            userListView.setItems(users);

            userListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    selectedUserId = Integer.parseInt(newVal.split(" - ")[0]);
                    loadMessages(selectedUserId);
                }
            });

        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load user list: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadMessages(int receiverId) {
        ObservableList<String> messages = FXCollections.observableArrayList();
        String sql = "SELECT m.*, u.username as sender_name FROM messages m " +
                "JOIN users u ON m.sender_id = u.id " +
                "WHERE (m.sender_id = ? AND m.receiver_id = ?) OR " +
                "(m.sender_id = ? AND m.receiver_id = ?) " +
                "ORDER BY m.timestamp";

        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, currentUser.getId());
            pstmt.setInt(2, receiverId);
            pstmt.setInt(3, receiverId);
            pstmt.setInt(4, currentUser.getId());

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String senderName = rs.getInt("sender_id") == currentUser.getId() ?
                        "You" : rs.getString("sender_name");
                String time = rs.getTimestamp("timestamp").toLocalDateTime().format(timeFormatter);
                messages.add(String.format("%s [%s]: %s",
                        senderName, time, rs.getString("content")));
            }

            messageListView.setItems(messages);
            if (!messages.isEmpty()) {
                messageListView.scrollTo(messages.size() - 1);
            }

        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load messages: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void startMessageChecker() {
        // Create a thread to periodically check for new messages
        Thread messageChecker = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(3000); // Check every 3 seconds
                    if (selectedUserId != -1) {
                        javafx.application.Platform.runLater(() -> {
                            loadMessages(selectedUserId);
                        });
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        messageChecker.setDaemon(true);
        messageChecker.start();
    }

    @FXML
    private void handleSendMessage() {
        String message = messageField.getText().trim();
        if (message.isEmpty()) {
            showAlert("Error", "Message cannot be empty", Alert.AlertType.WARNING);
            return;
        }
        boolean sentLocally = false;

        // Local DB messaging if a local user is selected
        if (selectedUserId != -1) {
            String sql = "INSERT INTO messages (sender_id, receiver_id, content, timestamp) VALUES (?, ?, ?, ?)";
            try (Connection conn = DatabaseHandler.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, currentUser.getId());
                pstmt.setInt(2, selectedUserId);
                pstmt.setString(3, message);
                pstmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
                pstmt.executeUpdate();
                sentLocally = true;
            } catch (SQLException e) {
                showAlert("Database Error", "Failed to send local message: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }

        // Network relay messaging if connected and peer entered
        if (relayClient.isConnected()) {
            String peer = peerField != null ? peerField.getText().trim() : "";
            if (!peer.isEmpty()) {
                try {
                    relayClient.sendMessage(peer, message);
                    sentLocally = true; // show in view
                } catch (IOException e) {
                    showAlert("Network Error", "Failed to send over relay: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        }

        if (sentLocally) {
            String time = LocalDateTime.now().format(timeFormatter);
            messageListView.getItems().add(String.format("You [%s]: %s", time, message));
            messageField.clear();
            messageListView.scrollTo(messageListView.getItems().size() - 1);
        } else {
            showAlert("Recipient Required", "Select a local user or enter a network peer.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void handleBack() {
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

    @FXML
    private void handleConnectRelay() {
        String host = serverHostField.getText().trim();
        String portText = serverPortField.getText().trim();
        if (host.isEmpty() || portText.isEmpty()) { showAlert("Relay", "Enter server host and port.", Alert.AlertType.INFORMATION); return; }
        int port;
        try { port = Integer.parseInt(portText); } catch (NumberFormatException e) { showAlert("Relay", "Invalid port.", Alert.AlertType.ERROR); return; }
        try {
            relayClient.connect(host, port, currentUser.getUsername());
        } catch (IOException e) {
            showAlert("Relay", "Failed to connect: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleDisconnectRelay() {
        relayClient.disconnect();
    }

    @FXML
    private void handleRefreshRelayUsers() {
        try { relayClient.requestUserList(); } catch (IOException ignored) {}
    }
}

