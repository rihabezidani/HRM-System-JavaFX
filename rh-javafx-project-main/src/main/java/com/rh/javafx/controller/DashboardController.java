package com.rh.javafx.controller;

import com.rh.javafx.MainApp;
import com.rh.javafx.model.BulletinPaie;
import com.rh.javafx.model.Conge;
import com.rh.javafx.model.Employe;
import com.rh.javafx.service.AuthService;
import com.rh.javafx.service.BulletinPaieService;
import com.rh.javafx.service.CongeService;
import com.rh.javafx.service.EmployeService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.util.Callback;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Controller for the Dashboard View
 */
public class DashboardController {

    // Services
    private final EmployeService employeService = new EmployeService();
    private final CongeService congeService = new CongeService();
    private final BulletinPaieService bulletinService = new BulletinPaieService();
    private final AuthService authService = new AuthService();

    // Top Bar
    @FXML
    private Label userNameLabel;

    @FXML
    private Button logoutButton;

    // Statistics Cards
    @FXML
    private Label totalEmployesLabel;

    @FXML
    private Label pendingCongesLabel;

    @FXML
    private Label totalBulletinsLabel;

    @FXML
    private Label averageSalaryLabel;

    // Tab Pane
    @FXML
    private TabPane mainTabPane;

    // Employees Tab
    @FXML
    private TableView<Employe> employesTable;

    @FXML
    private TableColumn<Employe, Integer> empIdColumn;

    @FXML
    private TableColumn<Employe, String> empMatriculeColumn;

    @FXML
    private TableColumn<Employe, String> empNomColumn;

    @FXML
    private TableColumn<Employe, String> empPrenomColumn;

    @FXML
    private TableColumn<Employe, String> empEmailColumn;

    @FXML
    private TableColumn<Employe, String> empPosteColumn;

    @FXML
    private TableColumn<Employe, BigDecimal> empSalaireColumn;

    @FXML
    private TableColumn<Employe, Integer> empCongesColumn;

    @FXML
    private TableColumn<Employe, Void> empActionsColumn;

    @FXML
    private TextField empSearchField;

    // Leaves Tab
    @FXML
    private TableView<Conge> congesTable;

    @FXML
    private TableColumn<Conge, Integer> congeIdColumn;

    @FXML
    private TableColumn<Conge, String> congeEmployeColumn;

    @FXML
    private TableColumn<Conge, LocalDate> congeDebutColumn;

    @FXML
    private TableColumn<Conge, LocalDate> congeFinColumn;

    @FXML
    private TableColumn<Conge, String> congeTypeColumn;

    @FXML
    private TableColumn<Conge, Integer> congeDureeColumn;

    @FXML
    private TableColumn<Conge, String> congeStatutColumn;

    @FXML
    private TableColumn<Conge, Void> congeActionsColumn;

    @FXML
    private ComboBox<String> congeFilterCombo;

    // Payslips Tab
    @FXML
    private TableView<BulletinPaie> bulletinsTable;

    @FXML
    private TableColumn<BulletinPaie, Integer> bulletinIdColumn;

    @FXML
    private TableColumn<BulletinPaie, String> bulletinEmployeColumn;

    @FXML
    private TableColumn<BulletinPaie, String> bulletinPeriodeColumn;

    @FXML
    private TableColumn<BulletinPaie, BigDecimal> bulletinBrutColumn;

    @FXML
    private TableColumn<BulletinPaie, BigDecimal> bulletinPrimesColumn;

    @FXML
    private TableColumn<BulletinPaie, BigDecimal> bulletinRetenuesColumn;

    @FXML
    private TableColumn<BulletinPaie, BigDecimal> bulletinNetColumn;

    @FXML
    private TableColumn<BulletinPaie, Void> bulletinActionsColumn;

    @FXML
    public void initialize() {
        // Set user name
        if (AuthService.getCurrentUser() != null) {
            userNameLabel.setText(AuthService.getCurrentUser().getNomComplet());
        }

        // Initialize statistics
        updateStatistics();

        // Initialize tables
        initializeEmployesTable();
        initializeCongesTable();
        initializeBulletinsTable();

        // Load data
        loadAllData();
    }

    private void updateStatistics() {
        totalEmployesLabel.setText(String.valueOf(employeService.getTotalEmployes()));
        pendingCongesLabel.setText(String.valueOf(congeService.getPendingCongesCount()));
        totalBulletinsLabel.setText(String.valueOf(bulletinService.getTotalBulletinsCount()));

        BigDecimal avgSalary = employeService.getAverageSalary();
        averageSalaryLabel.setText(String.format("%.2f DH", avgSalary));
    }

    // ========== EMPLOYEES TAB ==========

    private void initializeEmployesTable() {
        empIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        empMatriculeColumn.setCellValueFactory(new PropertyValueFactory<>("matricule"));
        empNomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        empPrenomColumn.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        empEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        empPosteColumn.setCellValueFactory(new PropertyValueFactory<>("poste"));
        empSalaireColumn.setCellValueFactory(new PropertyValueFactory<>("salaireBase"));
        empCongesColumn.setCellValueFactory(new PropertyValueFactory<>("joursCongesRestants"));

        // Format salary column
        empSalaireColumn.setCellFactory(column -> new TableCell<Employe, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f DH", item));
                }
            }
        });

        // Add action buttons
        addEmployeActionButtons();

        // Search functionality
        empSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                loadEmployes();
            } else {
                employesTable.setItems(FXCollections.observableArrayList(
                        employeService.searchEmployes(newVal.trim())));
            }
        });
    }

    private void addEmployeActionButtons() {
        Callback<TableColumn<Employe, Void>, TableCell<Employe, Void>> cellFactory =
                new Callback<TableColumn<Employe, Void>, TableCell<Employe, Void>>() {
            @Override
            public TableCell<Employe, Void> call(final TableColumn<Employe, Void> param) {
                return new TableCell<Employe, Void>() {
                    private final Button editBtn = new Button("Modifier");
                    private final Button deleteBtn = new Button("Supprimer");

                    {
                        editBtn.setOnAction(event -> {
                            Employe employe = getTableView().getItems().get(getIndex());
                            handleEditEmploye(employe);
                        });

                        deleteBtn.setOnAction(event -> {
                            Employe employe = getTableView().getItems().get(getIndex());
                            handleDeleteEmploye(employe);
                        });

                        editBtn.getStyleClass().add("button");
                        deleteBtn.getStyleClass().add("button-danger");
                        editBtn.setStyle("-fx-font-size: 11px; -fx-padding: 5px 10px;");
                        deleteBtn.setStyle("-fx-font-size: 11px; -fx-padding: 5px 10px;");
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            HBox buttons = new HBox(5, editBtn, deleteBtn);
                            setGraphic(buttons);
                        }
                    }
                };
            }
        };

        empActionsColumn.setCellFactory(cellFactory);
    }

    @FXML
    private void handleAddEmploye() {
        showEmployeDialog(null);
    }

    private void handleEditEmploye(Employe employe) {
        showEmployeDialog(employe);
    }

    private void handleDeleteEmploye(Employe employe) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer l'employé");
        alert.setContentText("Voulez-vous vraiment supprimer " + employe.getNomComplet() + "?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    employeService.deleteEmploye(employe.getId());
                    loadEmployes();
                    updateStatistics();
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Employé supprimé avec succès");
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
                }
            }
        });
    }

    private void showEmployeDialog(Employe employe) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(employe == null ? "Nouvel Employé" : "Modifier Employé");
        dialog.setHeaderText(employe == null ? "Ajouter un nouvel employé" : "Modifier les informations de l'employé");

        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Create input fields
        TextField matriculeField = new TextField(employe != null ? employe.getMatricule() : "");
        TextField nomField = new TextField(employe != null ? employe.getNom() : "");
        TextField prenomField = new TextField(employe != null ? employe.getPrenom() : "");
        TextField emailField = new TextField(employe != null ? employe.getEmail() : "");
        TextField telephoneField = new TextField(employe != null ? employe.getTelephone() : "");
        TextField posteField = new TextField(employe != null ? employe.getPoste() : "");
        TextField departementField = new TextField(employe != null ? employe.getDepartement() : "");
        TextField salaireField = new TextField(employe != null ? employe.getSalaireBase().toString() : "");

        // DatePickers for new fields
        DatePicker dateEmbaucheField = new DatePicker(employe != null ? employe.getDateEmbauche() : java.time.LocalDate.now());
        DatePicker dateNaissanceField = new DatePicker(employe != null ? employe.getDateNaissance() : null);

        // TextField for leave days with default value
        TextField congesField = new TextField(employe != null ? String.valueOf(employe.getJoursCongesRestants()) : "18");

        // Password field for employee login
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText(employe != null ? "Laisser vide pour ne pas changer" : "Mot de passe pour se connecter");

        // Set prompt texts
        matriculeField.setPromptText("Ex: EMP001");
        nomField.setPromptText("Ex: Dupont");
        prenomField.setPromptText("Ex: Jean");
        emailField.setPromptText("Ex: jean.dupont@email.com");
        telephoneField.setPromptText("Ex: 0600000000");
        posteField.setPromptText("Ex: Développeur");
        departementField.setPromptText("Ex: Informatique");
        salaireField.setPromptText("Ex: 10000.00");
        congesField.setPromptText("Default: 18 jours");

        // Add fields to grid
        int row = 0;
        grid.add(new Label("Matricule:*"), 0, row);
        grid.add(matriculeField, 1, row++);
        grid.add(new Label("Nom:*"), 0, row);
        grid.add(nomField, 1, row++);
        grid.add(new Label("Prénom:*"), 0, row);
        grid.add(prenomField, 1, row++);
        grid.add(new Label("Email:*"), 0, row);
        grid.add(emailField, 1, row++);
        grid.add(new Label("Téléphone:"), 0, row);
        grid.add(telephoneField, 1, row++);
        grid.add(new Label("Poste:"), 0, row);
        grid.add(posteField, 1, row++);
        grid.add(new Label("Département:"), 0, row);
        grid.add(departementField, 1, row++);
        grid.add(new Label("Date d'embauche:*"), 0, row);
        grid.add(dateEmbaucheField, 1, row++);
        grid.add(new Label("Date de naissance:"), 0, row);
        grid.add(dateNaissanceField, 1, row++);
        grid.add(new Label("Salaire de base:*"), 0, row);
        grid.add(salaireField, 1, row++);
        grid.add(new Label("Jours de congés:*"), 0, row);
        grid.add(congesField, 1, row++);
        grid.add(new Label("Mot de passe:" + (employe == null ? "*" : "")), 0, row);
        grid.add(passwordField, 1, row++);

        Label noteLabel = new Label("* Champs obligatoires");
        noteLabel.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 11px;");
        grid.add(noteLabel, 0, row, 2, 1);

        dialog.getDialogPane().setContent(grid);

        // Validation and save handler
        dialog.showAndWait().ifPresent(response -> {
            if (response == saveButtonType) {
                try {
                    // Validate required fields
                    StringBuilder errors = new StringBuilder();

                    if (matriculeField.getText().trim().isEmpty()) {
                        errors.append("- Le matricule est obligatoire\n");
                    }
                    if (nomField.getText().trim().isEmpty()) {
                        errors.append("- Le nom est obligatoire\n");
                    }
                    if (prenomField.getText().trim().isEmpty()) {
                        errors.append("- Le prénom est obligatoire\n");
                    }
                    if (emailField.getText().trim().isEmpty()) {
                        errors.append("- L'email est obligatoire\n");
                    } else if (!emailField.getText().trim().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                        errors.append("- L'email n'est pas valide\n");
                    }
                    if (salaireField.getText().trim().isEmpty()) {
                        errors.append("- Le salaire de base est obligatoire\n");
                    }
                    if (dateEmbaucheField.getValue() == null) {
                        errors.append("- La date d'embauche est obligatoire\n");
                    } else if (dateEmbaucheField.getValue().isAfter(java.time.LocalDate.now())) {
                        errors.append("- La date d'embauche ne peut pas être dans le futur\n");
                    }
                    if (congesField.getText().trim().isEmpty()) {
                        errors.append("- Les jours de congés sont obligatoires\n");
                    }

                    // Validate birth date if provided
                    if (dateNaissanceField.getValue() != null) {
                        if (dateNaissanceField.getValue().isAfter(java.time.LocalDate.now())) {
                            errors.append("- La date de naissance ne peut pas être dans le futur\n");
                        }
                        long age = java.time.temporal.ChronoUnit.YEARS.between(
                            dateNaissanceField.getValue(), java.time.LocalDate.now());
                        if (age < 18) {
                            errors.append("- L'employé doit avoir au moins 18 ans\n");
                        }
                        if (age > 100) {
                            errors.append("- La date de naissance semble incorrecte\n");
                        }
                    }

                    // Validate salary
                    try {
                        BigDecimal salaire = new BigDecimal(salaireField.getText().trim());
                        if (salaire.compareTo(BigDecimal.ZERO) <= 0) {
                            errors.append("- Le salaire doit être supérieur à 0\n");
                        }
                        if (salaire.compareTo(new BigDecimal("1000000")) > 0) {
                            errors.append("- Le salaire semble trop élevé\n");
                        }
                    } catch (NumberFormatException e) {
                        errors.append("- Le salaire doit être un nombre valide\n");
                    }

                    // Validate leave days
                    try {
                        int jours = Integer.parseInt(congesField.getText().trim());
                        if (jours < 0) {
                            errors.append("- Les jours de congés ne peuvent pas être négatifs\n");
                        }
                        if (jours > 365) {
                            errors.append("- Les jours de congés semblent trop élevés\n");
                        }
                    } catch (NumberFormatException e) {
                        errors.append("- Les jours de congés doivent être un nombre entier\n");
                    }

                    // Validate password (required for new employees)
                    if (employe == null && passwordField.getText().trim().isEmpty()) {
                        errors.append("- Le mot de passe est obligatoire pour les nouveaux employés\n");
                    }
                    if (!passwordField.getText().trim().isEmpty() && passwordField.getText().length() < 6) {
                        errors.append("- Le mot de passe doit contenir au moins 6 caractères\n");
                    }

                    // Show errors if any
                    if (errors.length() > 0) {
                        showAlert(Alert.AlertType.ERROR, "Erreurs de validation",
                            "Veuillez corriger les erreurs suivantes:\n\n" + errors.toString());
                        return;
                    }

                    // Save employee
                    Employe emp = employe != null ? employe : new Employe();
                    emp.setMatricule(matriculeField.getText().trim());
                    emp.setNom(nomField.getText().trim());
                    emp.setPrenom(prenomField.getText().trim());
                    emp.setEmail(emailField.getText().trim());
                    emp.setTelephone(telephoneField.getText().trim());
                    emp.setPoste(posteField.getText().trim());
                    emp.setDepartement(departementField.getText().trim());
                    emp.setSalaireBase(new BigDecimal(salaireField.getText().trim()));
                    emp.setDateEmbauche(dateEmbaucheField.getValue());
                    emp.setDateNaissance(dateNaissanceField.getValue());
                    emp.setJoursCongesRestants(Integer.parseInt(congesField.getText().trim()));

                    // Set password if provided
                    if (!passwordField.getText().trim().isEmpty()) {
                        String hashedPassword = hashPassword(passwordField.getText());
                        emp.setMotDePasseHash(hashedPassword);
                    }

                    employeService.saveEmploye(emp);
                    loadEmployes();
                    updateStatistics();
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Employé enregistré avec succès");

                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur de format",
                        "Veuillez vérifier que les champs numériques sont corrects.");
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Une erreur est survenue: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    // ========== LEAVES TAB ==========

    private void initializeCongesTable() {
        congeIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        congeEmployeColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getEmploye().getNomComplet()));
        congeDebutColumn.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
        congeFinColumn.setCellValueFactory(new PropertyValueFactory<>("dateFin"));
        congeTypeColumn.setCellValueFactory(new PropertyValueFactory<>("typeConge"));
        congeDureeColumn.setCellValueFactory(new PropertyValueFactory<>("dureeJours"));
        congeStatutColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getStatut().getLabel()));

        addCongeActionButtons();

        // Filter combo
        congeFilterCombo.setItems(FXCollections.observableArrayList(
                "Tous", "En Attente", "Approuvé", "Rejeté"));
        congeFilterCombo.setValue("Tous");
        congeFilterCombo.setOnAction(e -> filterConges());
    }

    private void addCongeActionButtons() {
        Callback<TableColumn<Conge, Void>, TableCell<Conge, Void>> cellFactory =
                new Callback<TableColumn<Conge, Void>, TableCell<Conge, Void>>() {
            @Override
            public TableCell<Conge, Void> call(final TableColumn<Conge, Void> param) {
                return new TableCell<Conge, Void>() {
                    private final Button approveBtn = new Button("Approuver");
                    private final Button rejectBtn = new Button("Rejeter");
                    private final Button editBtn = new Button("Modifier");
                    private final Button deleteBtn = new Button("Supprimer");

                    {
                        approveBtn.setOnAction(event -> {
                            Conge conge = getTableView().getItems().get(getIndex());
                            handleApproveConge(conge);
                        });

                        rejectBtn.setOnAction(event -> {
                            Conge conge = getTableView().getItems().get(getIndex());
                            handleRejectConge(conge);
                        });

                        editBtn.setOnAction(event -> {
                            Conge conge = getTableView().getItems().get(getIndex());
                            handleEditConge(conge);
                        });

                        deleteBtn.setOnAction(event -> {
                            Conge conge = getTableView().getItems().get(getIndex());
                            handleDeleteConge(conge);
                        });

                        approveBtn.getStyleClass().add("button-success");
                        rejectBtn.getStyleClass().add("button-danger");
                        editBtn.getStyleClass().add("button-primary");
                        deleteBtn.getStyleClass().add("button-danger");
                        approveBtn.setStyle("-fx-font-size: 11px; -fx-padding: 5px 10px;");
                        rejectBtn.setStyle("-fx-font-size: 11px; -fx-padding: 5px 10px;");
                        editBtn.setStyle("-fx-font-size: 11px; -fx-padding: 5px 10px;");
                        deleteBtn.setStyle("-fx-font-size: 11px; -fx-padding: 5px 10px;");
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            Conge conge = getTableView().getItems().get(getIndex());
                            if (conge.estEnAttente()) {
                                HBox buttons = new HBox(5, approveBtn, rejectBtn, editBtn, deleteBtn);
                                setGraphic(buttons);
                            } else {
                                // For approved/rejected leaves, only show delete button
                                HBox buttons = new HBox(5, deleteBtn);
                                setGraphic(buttons);
                            }
                        }
                    }
                };
            }
        };

        congeActionsColumn.setCellFactory(cellFactory);
    }

    @FXML
    private void handleAddConge() {
        showCongeDialog();
    }

    private void handleApproveConge(Conge conge) {
        try {
            congeService.approuverConge(conge.getId());
            filterConges(); // Reload with current filter
            loadEmployes();
            updateStatistics();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Congé approuvé");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    private void handleRejectConge(Conge conge) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Rejeter le congé");
        dialog.setHeaderText("Motif du rejet");
        dialog.setContentText("Veuillez entrer le motif du rejet:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(motif -> {
            try {
                congeService.rejeterConge(conge.getId(), motif);
                filterConges(); // Reload with current filter
                updateStatistics();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Congé rejeté");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
            }
        });
    }

    private void handleEditConge(Conge conge) {
        showEditCongeDialog(conge);
    }

    private void handleDeleteConge(Conge conge) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation de suppression");
        confirmAlert.setHeaderText("Supprimer le congé");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer ce congé?\n" +
                "Employé: " + conge.getEmploye().getNomComplet() + "\n" +
                "Période: " + conge.getDateDebut() + " au " + conge.getDateFin());

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                congeService.deleteConge(conge.getId());
                filterConges(); // Reload with current filter
                loadEmployes();
                updateStatistics();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Congé supprimé avec succès");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Une erreur est survenue lors de la suppression:\n" + e.getMessage());
            }
        }
    }

    private void showCongeDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Nouvelle Demande de Congé");

        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<Employe> employeCombo = new ComboBox<>();
        employeCombo.setItems(FXCollections.observableArrayList(employeService.getAllEmployes()));
        employeCombo.setConverter(new javafx.util.StringConverter<Employe>() {
            @Override
            public String toString(Employe employe) {
                return employe != null ? employe.getNomComplet() : "";
            }

            @Override
            public Employe fromString(String string) {
                return null;
            }
        });
        employeCombo.setPromptText("Sélectionner un employé");

        DatePicker dateDebutPicker = new DatePicker();
        DatePicker dateFinPicker = new DatePicker();
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.setItems(FXCollections.observableArrayList(
                "Congé Annuel", "Congé Maladie", "Congé Sans Solde", "Autre"));
        typeCombo.setPromptText("Sélectionner un type");
        TextField motifField = new TextField();
        motifField.setPromptText("Raison de la demande");

        grid.add(new Label("Employé:*"), 0, 0);
        grid.add(employeCombo, 1, 0);
        grid.add(new Label("Date Début:*"), 0, 1);
        grid.add(dateDebutPicker, 1, 1);
        grid.add(new Label("Date Fin:*"), 0, 2);
        grid.add(dateFinPicker, 1, 2);
        grid.add(new Label("Type:*"), 0, 3);
        grid.add(typeCombo, 1, 3);
        grid.add(new Label("Motif:"), 0, 4);
        grid.add(motifField, 1, 4);

        Label noteLabel = new Label("* Champs obligatoires");
        noteLabel.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 11px;");
        grid.add(noteLabel, 0, 5, 2, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(response -> {
            if (response == saveButtonType) {
                try {
                    // Comprehensive validation
                    StringBuilder errors = new StringBuilder();

                    // Validate employee selection
                    if (employeCombo.getValue() == null) {
                        errors.append("- Veuillez sélectionner un employé\n");
                    }

                    // Validate date debut
                    if (dateDebutPicker.getValue() == null) {
                        errors.append("- La date de début est obligatoire\n");
                    }

                    // Validate date fin
                    if (dateFinPicker.getValue() == null) {
                        errors.append("- La date de fin est obligatoire\n");
                    }

                    // Validate date logic (fin after debut)
                    if (dateDebutPicker.getValue() != null && dateFinPicker.getValue() != null) {
                        if (dateFinPicker.getValue().isBefore(dateDebutPicker.getValue())) {
                            errors.append("- La date de fin doit être après la date de début\n");
                        }
                        if (dateDebutPicker.getValue().isBefore(LocalDate.now())) {
                            errors.append("- La date de début ne peut pas être dans le passé\n");
                        }
                    }

                    // Validate type selection
                    if (typeCombo.getValue() == null || typeCombo.getValue().trim().isEmpty()) {
                        errors.append("- Veuillez sélectionner un type de congé\n");
                    }

                    // Show errors if any
                    if (errors.length() > 0) {
                        showAlert(Alert.AlertType.ERROR, "Erreurs de validation",
                            "Veuillez corriger les erreurs suivantes:\n\n" + errors.toString());
                        return;
                    }

                    // All validation passed, create leave request
                        // Calculate requested duration (inclusive)
                        int duree = (int) java.time.temporal.ChronoUnit.DAYS.between(
                            dateDebutPicker.getValue(), dateFinPicker.getValue()) + 1;
                        int joursRestants = employeCombo.getValue().getJoursCongesRestants();
                        if (duree > joursRestants) {
                        showAlert(Alert.AlertType.ERROR, "Erreur",
                            "L'employé n'a pas assez de jours de congé restants.\n" +
                            "Jours demandés: " + duree + " jours\n" +
                            "Jours restants: " + joursRestants + " jours");
                        return;
                        }

                        congeService.demanderConge(
                            employeCombo.getValue().getId(),
                            dateDebutPicker.getValue(),
                            dateFinPicker.getValue(),
                            typeCombo.getValue(),
                            motifField.getText());
                    filterConges(); // Reload with current filter
                    updateStatistics();
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Demande de congé créée avec succès");

                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Une erreur est survenue lors de la création du congé:\n" + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    private void showEditCongeDialog(Conge conge) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Modifier la Demande de Congé");

        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Employee field (read-only)
        TextField employeField = new TextField(conge.getEmploye().getNomComplet());
        employeField.setEditable(false);
        employeField.setDisable(true);

        DatePicker dateDebutPicker = new DatePicker(conge.getDateDebut());
        DatePicker dateFinPicker = new DatePicker(conge.getDateFin());
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.setItems(FXCollections.observableArrayList(
                "Congé Annuel", "Congé Maladie", "Congé Sans Solde", "Autre"));
        typeCombo.setValue(conge.getTypeConge());
        TextField motifField = new TextField(conge.getMotif());
        motifField.setPromptText("Raison de la demande");

        grid.add(new Label("Employé:"), 0, 0);
        grid.add(employeField, 1, 0);
        grid.add(new Label("Date Début:*"), 0, 1);
        grid.add(dateDebutPicker, 1, 1);
        grid.add(new Label("Date Fin:*"), 0, 2);
        grid.add(dateFinPicker, 1, 2);
        grid.add(new Label("Type:*"), 0, 3);
        grid.add(typeCombo, 1, 3);
        grid.add(new Label("Motif:"), 0, 4);
        grid.add(motifField, 1, 4);

        Label noteLabel = new Label("* Champs obligatoires | Seuls les congés en attente peuvent être modifiés");
        noteLabel.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 11px;");
        grid.add(noteLabel, 0, 5, 2, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(response -> {
            if (response == saveButtonType) {
                try {
                    // Validation
                    StringBuilder errors = new StringBuilder();

                    if (dateDebutPicker.getValue() == null) {
                        errors.append("- La date de début est obligatoire\n");
                    }

                    if (dateFinPicker.getValue() == null) {
                        errors.append("- La date de fin est obligatoire\n");
                    }

                    if (dateDebutPicker.getValue() != null && dateFinPicker.getValue() != null) {
                        if (dateFinPicker.getValue().isBefore(dateDebutPicker.getValue())) {
                            errors.append("- La date de fin doit être après la date de début\n");
                        }
                    }

                    if (typeCombo.getValue() == null || typeCombo.getValue().trim().isEmpty()) {
                        errors.append("- Veuillez sélectionner un type de congé\n");
                    }

                    if (errors.length() > 0) {
                        showAlert(Alert.AlertType.ERROR, "Erreurs de validation",
                            "Veuillez corriger les erreurs suivantes:\n\n" + errors.toString());
                        return;
                    }

                        // Validate that the updated duration doesn't exceed remaining days
                        int oldDuree = conge.getDureeJours();
                        int newDuree = (int) java.time.temporal.ChronoUnit.DAYS.between(
                            dateDebutPicker.getValue(), dateFinPicker.getValue()) + 1;
                        int joursRestants = conge.getEmploye().getJoursCongesRestants();
                        // available = remaining + old duration (we release the old days)
                        int disponible = joursRestants + oldDuree;
                        if (newDuree > disponible) {
                        showAlert(Alert.AlertType.ERROR, "Erreur",
                            "Impossible de modifier le congé: l'employé n'a pas assez de jours restants.\n" +
                            "Jours demandés: " + newDuree + " jours\n" +
                            "Disponibles (restants + ancien congé): " + disponible + " jours");
                        return;
                        }

                        // Update the conge object
                        conge.setDateDebut(dateDebutPicker.getValue());
                        conge.setDateFin(dateFinPicker.getValue());
                        conge.setTypeConge(typeCombo.getValue());
                        conge.setMotif(motifField.getText());

                        // Save changes
                        congeService.updateConge(conge);
                    filterConges(); // Reload with current filter
                    updateStatistics();
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Congé modifié avec succès");

                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Une erreur est survenue lors de la modification du congé:\n" + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    private void filterConges() {
        String filter = congeFilterCombo.getValue();
        if (filter.equals("Tous")) {
            loadConges();
        } else {
            Conge.StatutConge statut = switch (filter) {
                case "En Attente" -> Conge.StatutConge.EN_ATTENTE;
                case "Approuvé" -> Conge.StatutConge.APPROUVE;
                case "Rejeté" -> Conge.StatutConge.REJETE;
                default -> null;
            };
            if (statut != null) {
                congesTable.setItems(FXCollections.observableArrayList(
                        congeService.getCongesByStatut(statut)));
                // Ensure the table redraws immediately after items change
                congesTable.refresh();
            }
        }
    }

    // ========== PAYSLIPS TAB ==========

    private void initializeBulletinsTable() {
        bulletinIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        bulletinEmployeColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getEmploye().getNomComplet()));
        bulletinPeriodeColumn.setCellValueFactory(new PropertyValueFactory<>("periode"));
        bulletinBrutColumn.setCellValueFactory(new PropertyValueFactory<>("salaireBrut"));
        bulletinPrimesColumn.setCellValueFactory(new PropertyValueFactory<>("primes"));
        bulletinRetenuesColumn.setCellValueFactory(new PropertyValueFactory<>("retenues"));
        bulletinNetColumn.setCellValueFactory(new PropertyValueFactory<>("salaireNet"));

        // Format currency columns
        formatCurrencyColumn(bulletinBrutColumn);
        formatCurrencyColumn(bulletinPrimesColumn);
        formatCurrencyColumn(bulletinRetenuesColumn);
        formatCurrencyColumn(bulletinNetColumn);

        addBulletinActionButtons();
    }

    private void formatCurrencyColumn(TableColumn<BulletinPaie, BigDecimal> column) {
        column.setCellFactory(col -> new TableCell<BulletinPaie, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f DH", item));
                }
            }
        });
    }

    private void addBulletinActionButtons() {
        Callback<TableColumn<BulletinPaie, Void>, TableCell<BulletinPaie, Void>> cellFactory =
                new Callback<TableColumn<BulletinPaie, Void>, TableCell<BulletinPaie, Void>>() {
            @Override
            public TableCell<BulletinPaie, Void> call(final TableColumn<BulletinPaie, Void> param) {
                return new TableCell<BulletinPaie, Void>() {
                    private final Button viewBtn = new Button("Voir");
                    private final Button editBtn = new Button("Modifier");
                    private final Button deleteBtn = new Button("Supprimer");

                    {
                        viewBtn.setOnAction(event -> {
                            BulletinPaie bulletin = getTableView().getItems().get(getIndex());
                            handleViewBulletin(bulletin);
                        });

                        editBtn.setOnAction(event -> {
                            BulletinPaie bulletin = getTableView().getItems().get(getIndex());
                            handleEditBulletin(bulletin);
                        });

                        deleteBtn.setOnAction(event -> {
                            BulletinPaie bulletin = getTableView().getItems().get(getIndex());
                            handleDeleteBulletin(bulletin);
                        });

                        viewBtn.getStyleClass().add("button");
                        editBtn.getStyleClass().add("button-primary");
                        deleteBtn.getStyleClass().add("button-danger");
                        viewBtn.setStyle("-fx-font-size: 11px; -fx-padding: 5px 10px;");
                        editBtn.setStyle("-fx-font-size: 11px; -fx-padding: 5px 10px;");
                        deleteBtn.setStyle("-fx-font-size: 11px; -fx-padding: 5px 10px;");
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            HBox buttons = new HBox(5, viewBtn, editBtn, deleteBtn);
                            setGraphic(buttons);
                        }
                    }
                };
            }
        };

        bulletinActionsColumn.setCellFactory(cellFactory);
    }

    @FXML
    private void handleAddBulletin() {
        showBulletinDialog();
    }

    private void handleViewBulletin(BulletinPaie bulletin) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Bulletin de Paie");
        alert.setHeaderText("Détails du bulletin");

        TextArea textArea = new TextArea(bulletin.genererRecu());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        textArea.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12px;");

        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }

    private void handleEditBulletin(BulletinPaie bulletin) {
        showEditBulletinDialog(bulletin);
    }

    private void handleDeleteBulletin(BulletinPaie bulletin) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation de suppression");
        confirmAlert.setHeaderText("Supprimer le bulletin de paie");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer ce bulletin?\n" +
                "Employé: " + bulletin.getEmploye().getNomComplet() + "\n" +
                "Période: " + bulletin.getPeriode() + "\n" +
                "Salaire Net: " + bulletin.getSalaireNet() + " DH");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                bulletinService.deleteBulletin(bulletin.getId());
                loadBulletins();
                updateStatistics();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Bulletin supprimé avec succès");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Une erreur est survenue lors de la suppression:\n" + e.getMessage());
            }
        }
    }

    private void showBulletinDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Générer Bulletin de Paie");

        ButtonType saveButtonType = new ButtonType("Générer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<Employe> employeCombo = new ComboBox<>();
        employeCombo.setItems(FXCollections.observableArrayList(employeService.getAllEmployes()));
        employeCombo.setConverter(new javafx.util.StringConverter<Employe>() {
            @Override
            public String toString(Employe employe) {
                return employe != null ? employe.getNomComplet() : "";
            }

            @Override
            public Employe fromString(String string) {
                return null;
            }
        });
        employeCombo.setPromptText("Sélectionner un employé");

        TextField periodeField = new TextField();
        periodeField.setPromptText("Ex: 2026-01 ou Janvier 2026");
        TextField primesField = new TextField("0");
        primesField.setPromptText("Ex: 500.00");
        TextField retenuesField = new TextField("0");
        retenuesField.setPromptText("Ex: 200.00");

        grid.add(new Label("Employé:*"), 0, 0);
        grid.add(employeCombo, 1, 0);
        grid.add(new Label("Période:*"), 0, 1);
        grid.add(periodeField, 1, 1);
        grid.add(new Label("Primes:*"), 0, 2);
        grid.add(primesField, 1, 2);
        grid.add(new Label("Retenues:*"), 0, 3);
        grid.add(retenuesField, 1, 3);

        Label noteLabel = new Label("* Champs obligatoires");
        noteLabel.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 11px;");
        grid.add(noteLabel, 0, 4, 2, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(response -> {
            if (response == saveButtonType) {
                try {
                    // Comprehensive validation
                    StringBuilder errors = new StringBuilder();

                    // Validate employee selection
                    if (employeCombo.getValue() == null) {
                        errors.append("- Veuillez sélectionner un employé\n");
                    }

                    // Validate periode
                    if (periodeField.getText().trim().isEmpty()) {
                        errors.append("- La période est obligatoire\n");
                    }

                    // Validate primes
                    BigDecimal primes = null;
                    try {
                        primes = new BigDecimal(primesField.getText().trim());
                        if (primes.compareTo(BigDecimal.ZERO) < 0) {
                            errors.append("- Les primes ne peuvent pas être négatives\n");
                        }
                        if (primes.compareTo(new BigDecimal("1000000")) > 0) {
                            errors.append("- Le montant des primes semble trop élevé\n");
                        }
                    } catch (NumberFormatException e) {
                        errors.append("- Les primes doivent être un nombre valide\n");
                    }

                    // Validate retenues
                    BigDecimal retenues = null;
                    try {
                        retenues = new BigDecimal(retenuesField.getText().trim());
                        if (retenues.compareTo(BigDecimal.ZERO) < 0) {
                            errors.append("- Les retenues ne peuvent pas être négatives\n");
                        }
                        if (retenues.compareTo(new BigDecimal("1000000")) > 0) {
                            errors.append("- Le montant des retenues semble trop élevé\n");
                        }
                    } catch (NumberFormatException e) {
                        errors.append("- Les retenues doivent être un nombre valide\n");
                    }

                    // Validate that retenues don't exceed salary + primes
                    if (employeCombo.getValue() != null && primes != null && retenues != null) {
                        BigDecimal total = employeCombo.getValue().getSalaireBase().add(primes);
                        if (retenues.compareTo(total) > 0) {
                            errors.append("- Les retenues ne peuvent pas dépasser le salaire brut + primes\n");
                        }
                    }

                    // Show errors if any
                    if (errors.length() > 0) {
                        showAlert(Alert.AlertType.ERROR, "Erreurs de validation",
                            "Veuillez corriger les erreurs suivantes:\n\n" + errors.toString());
                        return;
                    }

                    // All validation passed, generate payslip
                    bulletinService.genererBulletin(
                            employeCombo.getValue().getId(),
                            periodeField.getText().trim(),
                            new BigDecimal(primesField.getText().trim()),
                            new BigDecimal(retenuesField.getText().trim()));
                    loadBulletins();
                    updateStatistics();
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Bulletin de paie généré avec succès");

                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Une erreur est survenue lors de la génération du bulletin:\n" + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    private void showEditBulletinDialog(BulletinPaie bulletin) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Modifier Bulletin de Paie");

        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Employee field (read-only)
        TextField employeField = new TextField(bulletin.getEmploye().getNomComplet());
        employeField.setEditable(false);
        employeField.setDisable(true);

        // Period field (read-only)
        TextField periodeField = new TextField(bulletin.getPeriode());
        periodeField.setEditable(false);
        periodeField.setDisable(true);

        TextField primesField = new TextField(bulletin.getPrimes().toString());
        primesField.setPromptText("0.00");

        TextField retenuesField = new TextField(bulletin.getRetenues().toString());
        retenuesField.setPromptText("0.00");

        grid.add(new Label("Employé:"), 0, 0);
        grid.add(employeField, 1, 0);
        grid.add(new Label("Période:"), 0, 1);
        grid.add(periodeField, 1, 1);
        grid.add(new Label("Primes:*"), 0, 2);
        grid.add(primesField, 1, 2);
        grid.add(new Label("Retenues:*"), 0, 3);
        grid.add(retenuesField, 1, 3);

        Label noteLabel = new Label("* Champs obligatoires");
        noteLabel.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 11px;");
        grid.add(noteLabel, 0, 4, 2, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(response -> {
            if (response == saveButtonType) {
                try {
                    // Validation
                    StringBuilder errors = new StringBuilder();

                    // Validate primes
                    BigDecimal primes = null;
                    try {
                        primes = new BigDecimal(primesField.getText().trim());
                        if (primes.compareTo(BigDecimal.ZERO) < 0) {
                            errors.append("- Les primes ne peuvent pas être négatives\n");
                        }
                        if (primes.compareTo(new BigDecimal("1000000")) > 0) {
                            errors.append("- Le montant des primes semble trop élevé\n");
                        }
                    } catch (NumberFormatException e) {
                        errors.append("- Les primes doivent être un nombre valide\n");
                    }

                    // Validate retenues
                    BigDecimal retenues = null;
                    try {
                        retenues = new BigDecimal(retenuesField.getText().trim());
                        if (retenues.compareTo(BigDecimal.ZERO) < 0) {
                            errors.append("- Les retenues ne peuvent pas être négatives\n");
                        }
                        if (retenues.compareTo(new BigDecimal("1000000")) > 0) {
                            errors.append("- Le montant des retenues semble trop élevé\n");
                        }
                    } catch (NumberFormatException e) {
                        errors.append("- Les retenues doivent être un nombre valide\n");
                    }

                    // Validate that retenues don't exceed salary + primes
                    if (primes != null && retenues != null) {
                        BigDecimal total = bulletin.getSalaireBrut().add(primes);
                        if (retenues.compareTo(total) > 0) {
                            errors.append("- Les retenues ne peuvent pas dépasser le salaire brut + primes\n");
                        }
                    }

                    // Show errors if any
                    if (errors.length() > 0) {
                        showAlert(Alert.AlertType.ERROR, "Erreurs de validation",
                            "Veuillez corriger les erreurs suivantes:\n\n" + errors.toString());
                        return;
                    }

                    // Update the bulletin object
                    bulletin.setPrimes(new BigDecimal(primesField.getText().trim()));
                    bulletin.setRetenues(new BigDecimal(retenuesField.getText().trim()));

                    // Save changes
                    bulletinService.updateBulletin(bulletin);
                    loadBulletins();
                    updateStatistics();
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Bulletin modifié avec succès");

                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Une erreur est survenue lors de la modification du bulletin:\n" + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    // ========== DATA LOADING ==========

    private void loadAllData() {
        loadEmployes();
        loadConges();
        loadBulletins();
    }

    private void loadEmployes() {
        try {
            List<Employe> employes = employeService.getAllEmployes();
            employesTable.setItems(FXCollections.observableArrayList(employes));
            // Force table to refresh so UI shows immediate changes
            employesTable.refresh();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des employés");
        }
    }

    private void loadConges() {
        try {
            List<Conge> conges = congeService.getAllConges();
            congesTable.setItems(FXCollections.observableArrayList(conges));
            // Force table to refresh so UI shows immediate changes
            congesTable.refresh();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des congés");
        }
    }

    private void loadBulletins() {
        try {
            List<BulletinPaie> bulletins = bulletinService.getAllBulletins();
            bulletinsTable.setItems(FXCollections.observableArrayList(bulletins));
            // Force table to refresh so UI shows immediate changes
            bulletinsTable.refresh();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des bulletins");
        }
    }

    // ========== UTILITY METHODS ==========

    @FXML
    private void handleLogout() {
        try {
            authService.logout();
            MainApp.showLoginView();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Hash a password using SHA-256
     */
    private String hashPassword(String password) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("Erreur lors du hachage du mot de passe", e);
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
