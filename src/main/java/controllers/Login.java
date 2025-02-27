package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.User;
import service.UserService;
import utils.SessionManager;

import java.io.IOException;

public class Login {

    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;
    @FXML
    private Button loginButton;
    @FXML
    private Hyperlink registerLink;
    @FXML
    private Hyperlink forgotPasswordLink;

    private UserService userService;

    @FXML
    public void initialize() {
        userService = new UserService();
        errorLabel.setVisible(false);
    }

    @FXML
    void handleLogin(ActionEvent event) {
        String email = emailField.getText();
        String password = passwordField.getText();

        // Validation des champs
        if (email.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs.");
            return;
        }

        // Récupérer l'utilisateur
        User user = userService.findByEmail(email);
        if (user == null || !user.getPassword_user().equals(password)) {
            showError("Email ou mot de passe incorrect.");
            return;
        }

        // Stocker l'utilisateur dans SessionManager
        SessionManager.setCurrentUser(user);

        // Redirection en fonction du rôle
        try {
            String fxmlFile;
            switch (user.getRole_user().toString()) {
                case "ADMIN":
                    fxmlFile = "/AdminDashboard.fxml";
                    break;
                case "ORGANISATEUR":
                    fxmlFile = "/OrganisateurDashboard.fxml";
                    break;
                case "PARTICIPANT":
                    fxmlFile = "/ParticipantDashboard.fxml";
                    break;
                default:
                    showError("Rôle non reconnu.");
                    return;
            }

            // Charger la nouvelle scène
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            // Afficher la nouvelle scène
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            showError("Erreur lors du chargement de la page: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void handleRegister(ActionEvent event) {
        try {
            // Charger la page d'inscription
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/RegisterUser.fxml"));
            Parent root = loader.load();

            // Utiliser la même fenêtre pour l'inscription au lieu d'en créer une nouvelle
            Stage currentStage = (Stage) registerLink.getScene().getWindow();
            currentStage.setTitle("Inscription");
            currentStage.setScene(new Scene(root));

        } catch (IOException e) {
            showError("Erreur lors du chargement de la page d'inscription: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void handleForgotPassword(ActionEvent event) {
        try {
            // Charger la page de récupération de mot de passe
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/LostPassword.fxml"));
            Parent root = loader.load();

            // Utiliser la même fenêtre
            Stage currentStage = (Stage) forgotPasswordLink.getScene().getWindow();
            currentStage.setTitle("Récupération de mot de passe");
            currentStage.setScene(new Scene(root));

        } catch (IOException e) {
            showError("Erreur lors du chargement de la page de récupération: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void showSuccessMessage(String message) {
        errorLabel.setStyle("-fx-text-fill: #4CAF50;"); // Couleur verte pour succès
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void showError(String message) {
        errorLabel.setStyle("-fx-text-fill: #ff0000;"); // Couleur rouge pour erreur
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}