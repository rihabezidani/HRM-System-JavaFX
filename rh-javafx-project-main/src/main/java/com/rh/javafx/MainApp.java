package com.rh.javafx;

import com.rh.javafx.util.HibernateUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Main JavaFX Application Class for HR Management System
 * Entry point for the desktop application
 */
public class MainApp extends Application {

    private static Stage primaryStage;
    private static Scene mainScene;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        primaryStage.setTitle("Système de Gestion RH");

        // Set application icon (optional)
        // primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/com/rh/javafx/images/app-icon.png")));

        // Load the login view
        showLoginView();

        primaryStage.show();
    }

    /**
     * Show the login view
     */
    public static void showLoginView() throws IOException {
        Parent root = FXMLLoader.load(MainApp.class.getResource("/com/rh/javafx/fxml/login.fxml"));
        mainScene = new Scene(root, 400, 500);
        mainScene.getStylesheets().add(MainApp.class.getResource("/com/rh/javafx/css/style.css").toExternalForm());
        primaryStage.setScene(mainScene);
        primaryStage.setResizable(false);
    }

    /**
     * Show the main dashboard view (for HR managers)
     */
    public static void showDashboard() throws IOException {
        Parent root = FXMLLoader.load(MainApp.class.getResource("/com/rh/javafx/fxml/dashboard.fxml"));
        mainScene = new Scene(root, 1200, 700);
        mainScene.getStylesheets().add(MainApp.class.getResource("/com/rh/javafx/css/style.css").toExternalForm());
        primaryStage.setScene(mainScene);
        primaryStage.setResizable(true);
        primaryStage.centerOnScreen();
    }

    /**
     * Show the employee dashboard view (for employees)
     */
    public static void showEmployeeDashboard() throws IOException {
        Parent root = FXMLLoader.load(MainApp.class.getResource("/com/rh/javafx/fxml/employee-dashboard.fxml"));
        mainScene = new Scene(root, 1200, 700);
        mainScene.getStylesheets().add(MainApp.class.getResource("/com/rh/javafx/css/style.css").toExternalForm());
        primaryStage.setScene(mainScene);
        primaryStage.setResizable(true);
        primaryStage.centerOnScreen();
    }

    /**
     * Get the primary stage
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    @Override
    public void stop() {
        // Cleanup resources when application closes
        HibernateUtil.shutdown();
        System.out.println("Application fermée. Ressources libérées.");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
