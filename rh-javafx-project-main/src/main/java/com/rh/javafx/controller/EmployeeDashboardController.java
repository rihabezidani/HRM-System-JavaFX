package com.rh.javafx.controller;

import com.rh.javafx.MainApp;
import com.rh.javafx.model.BulletinPaie;
import com.rh.javafx.model.Conge;
import com.rh.javafx.model.Conge.StatutConge;
import com.rh.javafx.model.Employe;
import com.rh.javafx.service.AuthService;
import com.rh.javafx.service.BulletinPaieService;
import com.rh.javafx.service.CongeService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Controller for Employee Dashboard
 * Allows employees to manage their own leave requests and view payslips
 */
public class EmployeeDashboardController {

    // Employee Info Labels
    @FXML
    private Label welcomeLabel;
    @FXML
    private Label employeeNameLabel;
    @FXML
    private Label employeeMatriculeLabel;
    @FXML
    private Label employeeEmailLabel;
    @FXML
    private Label employeePosteLabel;
    @FXML
    private Label employeeDepartementLabel;
    @FXML
    private Label employeeCongesRestantsLabel;

    // Conges Table
    @FXML
    private TableView<Conge> congesTable;
    @FXML
    private TableColumn<Conge, LocalDate> congeDateDebutColumn;
    @FXML
    private TableColumn<Conge, LocalDate> congeDateFinColumn;
    @FXML
    private TableColumn<Conge, Integer> congeDureeColumn;
    @FXML
    private TableColumn<Conge, String> congeTypeColumn;
    @FXML
    private TableColumn<Conge, String> congeMotifColumn;
    @FXML
    private TableColumn<Conge, String> congeStatutColumn;
    @FXML
    private TableColumn<Conge, Void> congeActionsColumn;
    @FXML
    private ComboBox<String> congeFilterCombo;

    // Bulletins Table
    @FXML
    private TableView<BulletinPaie> bulletinsTable;
    @FXML
    private TableColumn<BulletinPaie, String> bulletinPeriodeColumn;
    @FXML
    private TableColumn<BulletinPaie, LocalDate> bulletinDateEmissionColumn;
    @FXML
    private TableColumn<BulletinPaie, BigDecimal> bulletinSalaireBrutColumn;
    @FXML
    private TableColumn<BulletinPaie, BigDecimal> bulletinPrimesColumn;
    @FXML
    private TableColumn<BulletinPaie, BigDecimal> bulletinRetenuesColumn;
    @FXML
    private TableColumn<BulletinPaie, BigDecimal> bulletinSalaireNetColumn;
    @FXML
    private TableColumn<BulletinPaie, Void> bulletinActionsColumn;

    private final CongeService congeService = new CongeService();
    private final BulletinPaieService bulletinService = new BulletinPaieService();
    private Employe currentEmploye;

    @FXML
    public void initialize() {
        currentEmploye = AuthService.getCurrentEmploye();
        if (currentEmploye == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun employé connecté");
            handleLogout();
            return;
        }

        setupEmployeeInfo();
        setupCongesTable();
        setupBulletinsTable();
        setupCongeFilter();
        loadAllData();
    }

    private void setupEmployeeInfo() {
        welcomeLabel.setText("Bienvenue, " + currentEmploye.getPrenom() + " !");
        employeeNameLabel.setText(currentEmploye.getNomComplet());
        employeeMatriculeLabel.setText(currentEmploye.getMatricule());
        employeeEmailLabel.setText(currentEmploye.getEmail());
        employeePosteLabel.setText(currentEmploye.getPoste());
        employeeDepartementLabel.setText(currentEmploye.getDepartement());
        employeeCongesRestantsLabel.setText(currentEmploye.getJoursCongesRestants() + " jours");
    }

    private void setupCongesTable() {
        congeDateDebutColumn.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
        congeDateFinColumn.setCellValueFactory(new PropertyValueFactory<>("dateFin"));
        congeDureeColumn.setCellValueFactory(new PropertyValueFactory<>("dureeJours"));
        congeTypeColumn.setCellValueFactory(new PropertyValueFactory<>("typeConge"));
        congeMotifColumn.setCellValueFactory(new PropertyValueFactory<>("motif"));
        congeStatutColumn.setCellValueFactory(cellData -> {
            Conge conge = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(conge.getStatut().toString());
        });

        // Style the status column
        congeStatutColumn.setCellFactory(column -> new TableCell<Conge, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "EN_ATTENTE":
                            setStyle("-fx-text-fill: #F59E0B; -fx-font-weight: bold;");
                            break;
                        case "APPROUVE":
                            setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold;");
                            break;
                        case "REJETE":
                            setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
                            break;
                    }
                }
            }
        });

        addCongeActionButtons();
    }

    private void addCongeActionButtons() {
        Callback<TableColumn<Conge, Void>, TableCell<Conge, Void>> cellFactory =
                new Callback<TableColumn<Conge, Void>, TableCell<Conge, Void>>() {
            @Override
            public TableCell<Conge, Void> call(final TableColumn<Conge, Void> param) {
                return new TableCell<Conge, Void>() {
                    private final Button viewBtn = new Button("Voir");
                    private final Button editBtn = new Button("Modifier");
                    private final Button deleteBtn = new Button("Supprimer");

                    {
                        viewBtn.setOnAction(event -> {
                            Conge conge = getTableView().getItems().get(getIndex());
                            handleViewConge(conge);
                        });

                        editBtn.setOnAction(event -> {
                            Conge conge = getTableView().getItems().get(getIndex());
                            handleEditConge(conge);
                        });

                        deleteBtn.setOnAction(event -> {
                            Conge conge = getTableView().getItems().get(getIndex());
                            handleDeleteConge(conge);
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
                            Conge conge = getTableView().getItems().get(getIndex());
                            if (conge.estEnAttente()) {
                                // Pending leaves: show all buttons
                                HBox buttons = new HBox(5, viewBtn, editBtn, deleteBtn);
                                setGraphic(buttons);
                            } else {
                                // Approved/Rejected leaves: show only view button
                                HBox buttons = new HBox(5, viewBtn);
                                setGraphic(buttons);
                            }
                        }
                    }
                };
            }
        };

        congeActionsColumn.setCellFactory(cellFactory);
    }

    private void setupBulletinsTable() {
        bulletinPeriodeColumn.setCellValueFactory(new PropertyValueFactory<>("periode"));
        bulletinDateEmissionColumn.setCellValueFactory(new PropertyValueFactory<>("dateEmission"));
        bulletinSalaireBrutColumn.setCellValueFactory(new PropertyValueFactory<>("salaireBrut"));
        bulletinPrimesColumn.setCellValueFactory(new PropertyValueFactory<>("primes"));
        bulletinRetenuesColumn.setCellValueFactory(new PropertyValueFactory<>("retenues"));
        bulletinSalaireNetColumn.setCellValueFactory(new PropertyValueFactory<>("salaireNet"));

        addBulletinActionButtons();
    }

    private void addBulletinActionButtons() {
        Callback<TableColumn<BulletinPaie, Void>, TableCell<BulletinPaie, Void>> cellFactory =
                new Callback<TableColumn<BulletinPaie, Void>, TableCell<BulletinPaie, Void>>() {
            @Override
            public TableCell<BulletinPaie, Void> call(final TableColumn<BulletinPaie, Void> param) {
                return new TableCell<BulletinPaie, Void>() {
                    private final Button viewBtn = new Button("Voir Reçu");

                    {
                        viewBtn.setOnAction(event -> {
                            BulletinPaie bulletin = getTableView().getItems().get(getIndex());
                            handleViewBulletin(bulletin);
                        });

                        viewBtn.getStyleClass().add("button");
                        viewBtn.setStyle("-fx-font-size: 11px; -fx-padding: 5px 10px;");
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(viewBtn);
                        }
                    }
                };
            }
        };

        bulletinActionsColumn.setCellFactory(cellFactory);
    }

    @FXML
    private void handleAddConge() {
        showAddCongeDialog();
    }

    private void handleViewConge(Conge conge) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails de la demande de congé");
        alert.setHeaderText("Congé de " + currentEmploye.getNomComplet());

        String details = String.format(
                "Type: %s\n" +
                "Date début: %s\n" +
                "Date fin: %s\n" +
                "Durée: %d jours\n" +
                "Motif: %s\n" +
                "Statut: %s",
                conge.getTypeConge(),
                conge.getDateDebut(),
                conge.getDateFin(),
                conge.getDureeJours(),
                conge.getMotif(),
                conge.getStatut()
        );

        alert.setContentText(details);
        alert.showAndWait();
    }

    private void handleEditConge(Conge conge) {
        if (!conge.estEnAttente()) {
            showAlert(Alert.AlertType.WARNING, "Action impossible",
                    "Seules les demandes en attente peuvent être modifiées");
            return;
        }
        showEditCongeDialog(conge);
    }

    private void handleDeleteConge(Conge conge) {
        if (!conge.estEnAttente()) {
            showAlert(Alert.AlertType.WARNING, "Action impossible",
                    "Seules les demandes en attente peuvent être supprimées");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation de suppression");
        confirmAlert.setHeaderText("Supprimer la demande de congé");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer cette demande?\n" +
                "Période: " + conge.getDateDebut() + " au " + conge.getDateFin());

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                congeService.deleteConge(conge.getId());
                filterConges();
                updateEmployeeInfo();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Demande supprimée avec succès");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Une erreur est survenue lors de la suppression:\n" + e.getMessage());
            }
        }
    }

    private void handleViewBulletin(BulletinPaie bulletin) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Bulletin de Paie");
        alert.setHeaderText("Détails du bulletin - " + bulletin.getPeriode());

        TextArea textArea = new TextArea(bulletin.genererRecu());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        textArea.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12px;");

        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }

    private void showAddCongeDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Nouvelle Demande de Congé");

        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        DatePicker dateDebutPicker = new DatePicker();
        DatePicker dateFinPicker = new DatePicker();
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.setItems(FXCollections.observableArrayList(
                "Congé Annuel", "Congé Maladie", "Congé Sans Solde", "Autre"));
        typeCombo.setPromptText("Sélectionner un type");
        TextField motifField = new TextField();
        motifField.setPromptText("Raison de la demande");

        grid.add(new Label("Date Début:*"), 0, 0);
        grid.add(dateDebutPicker, 1, 0);
        grid.add(new Label("Date Fin:*"), 0, 1);
        grid.add(dateFinPicker, 1, 1);
        grid.add(new Label("Type:*"), 0, 2);
        grid.add(typeCombo, 1, 2);
        grid.add(new Label("Motif:"), 0, 3);
        grid.add(motifField, 1, 3);

        Label noteLabel = new Label("* Champs obligatoires | Congés restants: " +
                currentEmploye.getJoursCongesRestants() + " jours");
        noteLabel.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 11px;");
        grid.add(noteLabel, 0, 4, 2, 1);

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
                        if (dateDebutPicker.getValue().isBefore(LocalDate.now())) {
                            errors.append("- La date de début ne peut pas être dans le passé\n");
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

                        // Calculate requested duration (inclusive)
                        int duree = (int) java.time.temporal.ChronoUnit.DAYS.between(
                            dateDebutPicker.getValue(), dateFinPicker.getValue()) + 1;
                        int joursRestants = currentEmploye.getJoursCongesRestants();
                        if (duree > joursRestants) {
                        showAlert(Alert.AlertType.ERROR, "Erreur",
                            "Vous n'avez pas assez de jours de congé restants.\n" +
                                "Jours demandés: " + duree + " jours\n" +
                                "Jours restants: " + joursRestants + " jours");
                        return;
                        }

                        // Create leave request
                        congeService.demanderConge(
                            currentEmploye.getId(),
                            dateDebutPicker.getValue(),
                            dateFinPicker.getValue(),
                            typeCombo.getValue(),
                            motifField.getText());
                        filterConges();
                        updateEmployeeInfo();
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

        DatePicker dateDebutPicker = new DatePicker(conge.getDateDebut());
        DatePicker dateFinPicker = new DatePicker(conge.getDateFin());
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.setItems(FXCollections.observableArrayList(
                "Congé Annuel", "Congé Maladie", "Congé Sans Solde", "Autre"));
        typeCombo.setValue(conge.getTypeConge());
        TextField motifField = new TextField(conge.getMotif());
        motifField.setPromptText("Raison de la demande");

        grid.add(new Label("Date Début:*"), 0, 0);
        grid.add(dateDebutPicker, 1, 0);
        grid.add(new Label("Date Fin:*"), 0, 1);
        grid.add(dateFinPicker, 1, 1);
        grid.add(new Label("Type:*"), 0, 2);
        grid.add(typeCombo, 1, 2);
        grid.add(new Label("Motif:"), 0, 3);
        grid.add(motifField, 1, 3);

        Label noteLabel = new Label("* Champs obligatoires");
        noteLabel.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 11px;");
        grid.add(noteLabel, 0, 4, 2, 1);

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
                        int disponible = joursRestants + oldDuree;
                        if (newDuree > disponible) {
                        showAlert(Alert.AlertType.ERROR, "Erreur",
                            "Impossible de modifier le congé: vous n'avez pas assez de jours restants.\n" +
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
                        filterConges();
                        updateEmployeeInfo();
                        showAlert(Alert.AlertType.INFORMATION, "Succès", "Congé modifié avec succès");

                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur",
                            "Une erreur est survenue lors de la modification du congé:\n" + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    private void setupCongeFilter() {
        congeFilterCombo.setItems(FXCollections.observableArrayList(
                "Tous", "En Attente", "Approuvé", "Rejeté"));
        congeFilterCombo.setValue("Tous");
        congeFilterCombo.setOnAction(e -> filterConges());
    }

    private void filterConges() {
        String filter = congeFilterCombo.getValue();
        if (filter.equals("Tous")) {
            loadConges();
        } else {
            StatutConge statut = switch (filter) {
                case "En Attente" -> StatutConge.EN_ATTENTE;
                case "Approuvé" -> StatutConge.APPROUVE;
                case "Rejeté" -> StatutConge.REJETE;
                default -> null;
            };
            if (statut != null) {
                List<Conge> allConges = congeService.getCongesByEmploye(currentEmploye.getId());
                List<Conge> filteredConges = allConges.stream()
                        .filter(c -> c.getStatut() == statut)
                        .collect(java.util.stream.Collectors.toList());
                congesTable.setItems(FXCollections.observableArrayList(filteredConges));
                // Ensure the table redraws immediately after items change
                congesTable.refresh();
            }
        }
    }

    private void loadAllData() {
        loadConges();
        loadBulletins();
    }

    private void loadConges() {
        List<Conge> conges = congeService.getCongesByEmploye(currentEmploye.getId());
        congesTable.setItems(FXCollections.observableArrayList(conges));
        // Force table to refresh so UI shows immediate changes
        congesTable.refresh();
    }

    private void loadBulletins() {
        List<BulletinPaie> bulletins = bulletinService.getBulletinsByEmploye(currentEmploye.getId());
        bulletinsTable.setItems(FXCollections.observableArrayList(bulletins));
        // Force table to refresh so UI shows immediate changes
        bulletinsTable.refresh();
    }

    private void updateEmployeeInfo() {
        // Refresh employee data from database
        currentEmploye = new com.rh.javafx.service.EmployeService().getEmployeById(currentEmploye.getId());
        employeeCongesRestantsLabel.setText(currentEmploye.getJoursCongesRestants() + " jours");
    }

    @FXML
    private void handleLogout() {
        AuthService authService = new AuthService();
        authService.logout();
        try {
            MainApp.showLoginView();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
