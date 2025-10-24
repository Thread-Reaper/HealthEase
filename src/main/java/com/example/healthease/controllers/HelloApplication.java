package com.example.healthease.controllers;

import com.example.healthease.utils.DatabaseHandler;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HelloApplication extends Application {
    private HostServices hostServices;

    @Override
    public void start(Stage stage) throws Exception {
        hostServices = getHostServices();
        DatabaseHandler.initializeDatabase();

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/healthease/LOGIN.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        LoginController loginController = fxmlLoader.getController();
        loginController.setHostServices(hostServices);

        stage.setTitle("HealthEase Login");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}