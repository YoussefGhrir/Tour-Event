package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.Role;
import service.UserService;
import model.User;
import java.util.Optional;

public class AjouterPersonne {

    @FXML
    private TextField nomUserField;
    @FXML
    private TextField prenomUserField;
    @FXML
    private TextField emailUserField;
    @FXML
    private TextField teleUserField;
    @FXML
    private TextField ageUserField;
    @FXML
    private PasswordField passwordUserField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private ComboBox<String> roleUserComboBox;
    @FXML
    private Label errorLabel;

    private UserService userService;

    @FXML
    public void initialize() {
        // Initialisation de la combo box des rôles
        roleUserComboBox.getItems().addAll("ADMIN", "PARTICIPANT", "ORGANISATEUR");
        userService = new UserService();

        // Masquer le label d'erreur initialement
        if (errorLabel != null) {
            errorLabel.setVisible(false);
        }
    }

    @FXML
    public void handleAddUser() {
        try {
            // Récupération des données
            String nom = nomUserField.getText().trim();
            String prenom = prenomUserField.getText().trim();
            String email = emailUserField.getText().trim();
            String telephone = teleUserField.getText().trim();
            String password = passwordUserField.getText();
            String confirmPassword = confirmPasswordField.getText();
            String roleStr = roleUserComboBox.getValue();

            // Validation basique des champs vides ou non sélectionnés
            if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() ||
                    telephone.isEmpty() || roleStr == null || password.isEmpty()) {
                showError("Tous les champs sont obligatoires");
                return;
            }

            // Vérification de correspondance des mots de passe
            if (!password.equals(confirmPassword)) {
                showError("Les mots de passe ne correspondent pas");
                return;
            }

            // Validation de l'âge
            int age;
            try {
                age = Integer.parseInt(ageUserField.getText().trim());
            } catch (NumberFormatException e) {
                showError("L'âge doit être un nombre valide");
                return;
            }

            // Création de l'utilisateur
            User user = new User(nom, prenom, age, Role.valueOf(roleStr), email, password, telephone);

            // Appel au service avec gestion des erreurs
            try {
                boolean success = userService.addWithValidation(user);
                if (success) {
                    showSuccessAlert("Utilisateur ajouté avec succès!");
                    resetFields();
                }
            } catch (IllegalArgumentException e) {
                showError(e.getMessage());
            }

        } catch (Exception e) {
            showError("Une erreur est survenue: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
        } else {
            showAlert("Erreur", message, Alert.AlertType.ERROR);
        }
    }

    private void showSuccessAlert(String message) {
        showAlert("Succès", message, Alert.AlertType.INFORMATION);
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void resetFields() {
        nomUserField.clear();
        prenomUserField.clear();
        emailUserField.clear();
        teleUserField.clear();
        ageUserField.clear();
        passwordUserField.clear();
        confirmPasswordField.clear();
        roleUserComboBox.getSelectionModel().clearSelection();
        if (errorLabel != null) {
            errorLabel.setVisible(false);
        }
    }

    @FXML
    public void handleReturn() {
        // Si des modifications ont été commencées, demander confirmation
        if (!nomUserField.getText().isEmpty() || !prenomUserField.getText().isEmpty()) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirmation");
            confirmAlert.setHeaderText(null);
            confirmAlert.setContentText("Voulez-vous vraiment quitter ? Les données non enregistrées seront perdues.");

            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() != ButtonType.OK) {
                return; // L'utilisateur a annulé
            }
        }
    }
}