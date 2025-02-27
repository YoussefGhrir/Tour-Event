package controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import model.User;
import service.UserService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class AfficherUsers {

    @FXML
    private TableView<User> userTable;
    @FXML
    private TableColumn<User, String> nomCol;
    @FXML
    private TableColumn<User, String> prenomCol;
    @FXML
    private TableColumn<User, String> emailCol;
    @FXML
    private TableColumn<User, String> teleCol;
    @FXML
    private TableColumn<User, Integer> ageCol;
    @FXML
    private TableColumn<User, String> roleCol;
    @FXML
    private TableColumn<User, Void> actionsCol;
    @FXML
    private TextField filterTextField;
    @FXML
    private Label errorLabel;

    private ObservableList<User> userObservableList;
    private UserService userService;

    @FXML
    public void initialize() {
        userService = new UserService();
        setupTableColumns();
        loadUsers();
        setupFilter();
        addEditDeleteButtons();
    }

    private void setupTableColumns() {
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom_user")); // Affiche le champ 'nom_user'
        prenomCol.setCellValueFactory(new PropertyValueFactory<>("prenom_user")); // Affiche le champ 'prenom_user'
        emailCol.setCellValueFactory(new PropertyValueFactory<>("mail_user")); // Affiche le champ 'mail_user'
        teleCol.setCellValueFactory(new PropertyValueFactory<>("tele_user")); // Affiche le champ 'tele_user'
        ageCol.setCellValueFactory(new PropertyValueFactory<>("age_user")); // Affiche l'âge
        roleCol.setCellValueFactory(user ->
                new SimpleStringProperty(user.getValue().getRole_user().name()) // Convertit le rôle en String
        );
    }

    private void loadUsers() {
        try {
            List<User> users = userService.display();

            if (users.isEmpty()) {
                showError("Aucun utilisateur trouvé");
                return;
            }

            userObservableList = FXCollections.observableArrayList(users);
            userTable.setItems(userObservableList);

            if (errorLabel != null) {
                errorLabel.setVisible(false);
            }

        } catch (Exception e) {
            showError("Une erreur est survenue: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupFilter() {
        filterTextField.textProperty().addListener((obs, oldVal, newVal) -> {
            String filterText = newVal.toLowerCase();

            if (filterText.isEmpty()) {
                userTable.setItems(userObservableList); // Reset to full list
            } else {
                ObservableList<User> filteredList = FXCollections.observableArrayList();
                for (User user : userObservableList) {
                    if (user.getNom_user().toLowerCase().contains(filterText) ||
                            user.getPrenom_user().toLowerCase().contains(filterText) ||
                            user.getMail_user().toLowerCase().contains(filterText)) {
                        filteredList.add(user);
                    }
                }
                userTable.setItems(filteredList); // Set filtered list
            }
        });
    }

    private void addEditDeleteButtons() {
        actionsCol.setCellFactory(new Callback<TableColumn<User, Void>, TableCell<User, Void>>() {
            @Override
            public TableCell<User, Void> call(final TableColumn<User, Void> param) {
                return new TableCell<User, Void>() {
                    private final Button editBtn = new Button("✎");
                    private final Button deleteBtn = new Button("✖");

                    {
                        editBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 5;");
                        deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-background-radius: 5;");

                        editBtn.setOnAction(e -> {
                            User user = getTableView().getItems().get(getIndex());
                            openEditDialog(user);
                        });

                        deleteBtn.setOnAction(e -> {
                            User user = getTableView().getItems().get(getIndex());
                            handleDelete(user);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
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
        });
    }

    private void openEditDialog(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DetailsUser.fxml"));
            Parent root = loader.load();

            DetailsUser controller = loader.getController();
            controller.setUserData(user);

            Stage stage = new Stage();
            stage.setTitle("Détails Utilisateur");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadUsers(); // Refresh table after edit
        } catch (IOException e) {
            showError("Erreur d'ouverture de la fenêtre: " + e.getMessage());
        }
    }

    private void handleDelete(User user) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Suppression d'utilisateur");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer " + user.getNom_user() + " ?");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            userService.delete(user.getId_user());
            loadUsers();
            showAlert("Succès", "Utilisateur supprimé avec succès", Alert.AlertType.INFORMATION);
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void handleFilter() {
        String filterText = filterTextField.getText().toLowerCase();

        if (filterText.isEmpty()) {
            userTable.setItems(userObservableList); // Reset to full list
        } else {
            ObservableList<User> filteredList = FXCollections.observableArrayList();
            for (User user : userObservableList) {
                if (user.getNom_user().toLowerCase().contains(filterText) ||
                        user.getPrenom_user().toLowerCase().contains(filterText) ||
                        user.getMail_user().toLowerCase().contains(filterText)) {
                    filteredList.add(user);
                }
            }
            userTable.setItems(filteredList); // Set filtered list
        }
    }

    @FXML
    public void handleReturn() {
        // Close the current window
        userTable.getScene().getWindow().hide();
    }

    @FXML
    private void handleRefresh() {
        loadUsers();
        filterTextField.clear();
        errorLabel.setVisible(false);
    }
}
