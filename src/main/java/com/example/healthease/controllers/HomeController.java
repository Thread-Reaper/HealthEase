package com.example.healthease.controllers;

import com.example.healthease.models.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.application.HostServices;
import javafx.event.ActionEvent;
import java.io.IOException;

public class HomeController {
    @FXML private Button communityButton;
    @FXML private Button healthTipsButton;
    @FXML private Button mealPlanButton;
    @FXML private Button checkHealthButton;
    @FXML private Button healthWebButton;
    @FXML private Button gymPlanningButton;
    @FXML private Button messageButton;
    @FXML private Button profileButton;
    @FXML private Button logoutButton;

    private User currentUser;
    private HostServices hostServices;

    public void setCurrentUser(User user) {
        this.currentUser = user;
        System.out.println("HomeController: User set to " + user.getUsername());
    }

    @FXML
    public void handleLogoutButton(ActionEvent event) {
        try {
            Stage currentStage = (Stage) logoutButton.getScene().getWindow();
            currentStage.close();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/healthease/LOGIN.fxml"));
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof LoginController && hostServices != null) {
                ((LoginController) controller).setHostServices(hostServices);
            }

            Stage loginStage = new Stage();
            loginStage.setTitle("HealthEase Login");
            loginStage.setScene(new Scene(root));
            loginStage.setResizable(false);
            loginStage.show();
        } catch (IOException e) {
            System.out.println("Error loading LOGIN.fxml on logout");
            e.printStackTrace();
        }
    }
    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    @FXML
    public void handleCommunityButton(ActionEvent event) {
        System.out.println("Community button clicked");
        openWebPage("https://www.example-health-community.com");
    }

    @FXML
    public void handleHealthWebButton(ActionEvent event) {
        System.out.println("HealthWeb button clicked");
        openWebPage("https://www.healthweb-resources.com");
    }

    @FXML
    public void handleMessageButton(ActionEvent event) {
        System.out.println("Message button clicked");
        loadMessenger();
    }

    @FXML
    public void handleProfileButton(ActionEvent event) {
        System.out.println("Profile button clicked");
        loadProfile();
    }

    @FXML
    public void handleHealthTipsButton(ActionEvent event) {
        System.out.println("HealthTips button clicked");
        loadFeatureWindow("/com/example/healthease/HealthTips.fxml", "Personalized Health Tips");
    }

    @FXML
    public void handleMealPlanButton(ActionEvent event) {
        System.out.println("MealPlan button clicked");
        loadFeatureWindow("/com/example/healthease/MealPlan.fxml", "Meal Planning");
    }

    @FXML
    public void handleGymPlanningButton(ActionEvent event) {
        System.out.println("GymPlanning button clicked");
        loadFeatureWindow("/com/example/healthease/GymPlan.fxml", "Gym Planning");
    }

    @FXML
    public void handleCheckHealthButton(ActionEvent event) {
        System.out.println("CheckHealth button clicked");
        loadFeatureWindow("/com/example/healthease/CheckHealth.fxml", "Health Check");
    }

    private void openWebPage(String url) {
        System.out.println("Opening URL: " + url);
        if (hostServices != null) {
            hostServices.showDocument(url);
        } else {
            System.out.println("HostServices is null!");
        }
    }

    private void loadFeatureWindow(String fxmlPath, String title) {
        try {
            System.out.println("Loading: " + fxmlPath);
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            if (loader.getController() instanceof UserAwareController) {
                ((UserAwareController) loader.getController()).setUser(currentUser);
            }

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.out.println("Error loading " + fxmlPath);
            e.printStackTrace();
        } catch (NullPointerException e) {
            System.out.println("FXML not found: " + fxmlPath);
            e.printStackTrace();
        }
    }

    private void loadMessenger() {
        try {
            System.out.println("Loading Messenger");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/healthease/Messenger.fxml"));
            Parent root = loader.load();

            MessengerController messengerController = loader.getController();
            messengerController.setCurrentUser(currentUser);

            Stage stage = new Stage();
            stage.setTitle("HealthEase Messenger - " + currentUser.getUsername());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.out.println("Error loading Messenger");
            e.printStackTrace();
        }
    }

    private void loadProfile() {
        try {
            System.out.println("Loading Profile");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/healthease/Profile.fxml"));
            Parent root = loader.load();

            ProfileController profileController = loader.getController();
            profileController.setCurrentUser(currentUser);

            Stage stage = new Stage();
            stage.setTitle("My Profile - " + currentUser.getUsername());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.out.println("Error loading Profile");
            e.printStackTrace();
        }
    }
}

interface UserAwareController {
    void setUser(User user);
}
