module com.example.healthease {
    // JavaFX modules
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires transitive javafx.graphics; // For HostServices

    // Java Standard modules
    requires transitive java.sql;
    requires java.desktop; // Required if you use java.awt (e.g. for fonts, images, or colors)
    // java.xml not strictly required now that Program AB is removed

    // Allow FXML to access these packages reflectively
    opens com.example.healthease.controllers to javafx.fxml;
    opens com.example.healthease.models to javafx.fxml;

    // Export packages for public access
    exports com.example.healthease.controllers;
    exports com.example.healthease.models;
    exports com.example.healthease.utils;
}
