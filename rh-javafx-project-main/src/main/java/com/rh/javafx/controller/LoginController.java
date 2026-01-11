package com.rh.javafx.controller;

import com.rh.javafx.MainApp;
import com.rh.javafx.service.AuthService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.io.IOException;

/**
 * Controller for the Login View
 */
public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Label errorLabel;

    @FXML
    private Hyperlink createAccountLink;

    private final AuthService authService = new AuthService();

    @FXML
    public void initialize() {
        // Initialize default admin account if needed
        authService.initializeDefaultAdmin();

        // Hide error label initially
        errorLabel.setVisible(false);

        // Add Enter key support for login
        passwordField.setOnKeyPressed(this::handleKeyPress);
        emailField.setOnKeyPressed(this::handleKeyPress);
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        // Validation
        if (email.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs");
            return;
        }

        // Attempt login
        try {
            boolean success = authService.login(email, password);
            if (success) {
                // Navigate to appropriate dashboard based on user type
                if (AuthService.isHRManager()) {
                    MainApp.showDashboard();
                } else if (AuthService.isEmploye()) {
                    MainApp.showEmployeeDashboard();
                }
            } else {
                showError("Email ou mot de passe incorrect");
                passwordField.clear();
            }
        } catch (Exception e) {
            showError("Erreur de connexion: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCreateAccount() {
        // Show account creation dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Créer un compte");
        dialog.setHeaderText("Nouveau compte Responsable RH");

        // Set the button types
        ButtonType createButtonType = new ButtonType("Créer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        // Create the form
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField nomField = new TextField();
        nomField.setPromptText("Nom");
        TextField prenomField = new TextField();
        prenomField.setPromptText("Prénom");
        TextField newEmailField = new TextField();
        newEmailField.setPromptText("Email");
        TextField telephoneField = new TextField();
        telephoneField.setPromptText("Téléphone");
        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("Mot de passe");
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirmer mot de passe");

        grid.add(new Label("Nom:"), 0, 0);
        grid.add(nomField, 1, 0);
        grid.add(new Label("Prénom:"), 0, 1);
        grid.add(prenomField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(newEmailField, 1, 2);
        grid.add(new Label("Téléphone:"), 0, 3);
        grid.add(telephoneField, 1, 3);
        grid.add(new Label("Mot de passe:"), 0, 4);
        grid.add(newPasswordField, 1, 4);
        grid.add(new Label("Confirmer:"), 0, 5);
        grid.add(confirmPasswordField, 1, 5);

        dialog.getDialogPane().setContent(grid);

        // Handle the result
        dialog.showAndWait().ifPresent(response -> {
            if (response == createButtonType) {
                String nom = nomField.getText().trim();
                String prenom = prenomField.getText().trim();
                String email = newEmailField.getText().trim();
                String telephone = telephoneField.getText().trim();
                String password = newPasswordField.getText();
                String confirmPassword = confirmPasswordField.getText();

                // Validation
                if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    showError("Veuillez remplir tous les champs obligatoires");
                    return;
                }

                if (!password.equals(confirmPassword)) {
                    showError("Les mots de passe ne correspondent pas");
                    return;
                }

                if (password.length() < 6) {
                    showError("Le mot de passe doit contenir au moins 6 caractères");
                    return;
                }

                // Create account
                try {
                    authService.createAccount(nom, prenom, email, password, telephone);
                    showSuccess("Compte créé avec succès! Vous pouvez maintenant vous connecter.");
                } catch (Exception e) {
                    showError(e.getMessage());
                }
            }
        });
    }

    private void handleKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            handleLogin();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: #f44336;");
        errorLabel.setVisible(true);
    }

    private void showSuccess(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: #4CAF50;");
        errorLabel.setVisible(true);
    }
}
