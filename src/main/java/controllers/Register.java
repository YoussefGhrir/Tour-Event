package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Role;
import model.User;
import service.UserService;
import java.io.IOException;

public class Register {

    @FXML
    private TextField nomField;
    @FXML
    private TextField prenomField;
    @FXML
    private TextField ageField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField teleField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private ComboBox<String> roleComboBox;
    @FXML
    private Label errorLabel;
    @FXML
    private Hyperlink loginLink;

    private UserService userService;

    @FXML
    public void initialize() {
        userService = new UserService();
        errorLabel.setVisible(false);

        // Initialiser ComboBox avec les rôles disponibles pour l'inscription
        // (seulement ORGANISATEUR et PARTICIPANT, pas ADMIN)
        ObservableList<String> roles = FXCollections.observableArrayList(
                "ORGANISATEUR", "PARTICIPANT"
        );
        roleComboBox.setItems(roles);
        roleComboBox.setValue("PARTICIPANT"); // Valeur par défaut
    }

    @FXML
    void handleRegister(ActionEvent event) {
        try {
            // Récupérer les valeurs des champs
            String nom = nomField.getText();
            String prenom = prenomField.getText();
            String email = emailField.getText();
            String telephone = teleField.getText();
            String password = passwordField.getText();
            String confirmPassword = confirmPasswordField.getText();
            String roleString = roleComboBox.getValue();

            // Vérification des mots de passe
            if (!password.equals(confirmPassword)) {
                showError("Les mots de passe ne correspondent pas.");
                return;
            }

            // Vérification de l'âge
            int age;
            try {
                age = Integer.parseInt(ageField.getText());
            } catch (NumberFormatException e) {
                showError("L'âge doit être un nombre entier.");
                return;
            }

            // Validation du rôle sélectionné
            if (roleString == null || roleString.isEmpty()) {
                showError("Veuillez sélectionner un rôle.");
                return;
            }

            Role role = Role.valueOf(roleString);

            // Création de l'utilisateur
            User newUser = new User(
                    nom,
                    prenom,
                    age,
                    role,
                    email,
                    password,
                    telephone
            );

            // Tentative d'ajout avec validation
            boolean success = userService.addWithValidation(newUser);

            if (success) {
                // Redirection vers la page de login dans la même fenêtre
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/LoginUser.fxml"));
                Parent root = loader.load();

                // Utiliser la même fenêtre pour la connexion
                Stage currentStage = (Stage) nomField.getScene().getWindow();
                currentStage.setTitle("Connexion");
                currentStage.setScene(new Scene(root));

                // Optionnel : afficher un message de succès via le contrôleur de login
                Login loginController = loader.getController();
                loginController.showSuccessMessage("Inscription réussie! Veuillez vous connecter.");
            }

        } catch (IllegalArgumentException e) {
            // Gérer les erreurs de validation
            showError(e.getMessage());
        } catch (IOException e) {
            showError("Erreur lors du chargement de la page: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void handleLogin(ActionEvent event) {
        try {
            // Charger la page de connexion
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/LoginUser.fxml"));
            Parent root = loader.load();

            // Utiliser la même fenêtre pour la connexion
            Stage currentStage = (Stage) loginLink.getScene().getWindow();
            currentStage.setTitle("Connexion");
            currentStage.setScene(new Scene(root));

        } catch (IOException e) {
            showError("Erreur lors du chargement de la page de connexion: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}