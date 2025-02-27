package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import service.UserService;
import model.User;
import java.io.IOException;

public class LostPassword {

    @FXML
    private TextField emailField;
    @FXML
    private Label messageLabel;
    @FXML
    private Button submitButton;
    @FXML
    private Hyperlink backToLoginLink;

    private UserService userService;

    @FXML
    public void initialize() {
        userService = new UserService();
        messageLabel.setVisible(false);
    }

    @FXML
    void handleSubmit(ActionEvent event) {
        // Récupérer l'email
        String email = emailField.getText();

        // Validation de l'email
        if (email.isEmpty()) {
            showMessage("Veuillez entrer votre adresse email.", true);
            return;
        }

        if (!Validator.validateEmail(email)) {
            showMessage("Format d'email invalide.", true);
            return;
        }

        // Vérifier si l'email existe dans la base de données
        User user = userService.findByEmail(email);
        if (user == null) {
            showMessage("Aucun compte n'est associé à cette adresse email.", true);
            return;
        }

        // Ici, vous implémenteriez la logique pour envoyer un email de réinitialisation
        // Par exemple, générer un token, l'enregistrer en base de données, et envoyer un email

        // Pour cet exemple, nous affichons simplement un message de succès
        showMessage("Instructions envoyées ! Veuillez vérifier votre boîte de réception.", false);

        // Désactiver le bouton pour éviter les soumissions multiples
        submitButton.setDisable(true);
    }

    @FXML
    void handleBackToLogin(ActionEvent event) {
        try {
            // Charger la page de connexion
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/LoginUser.fxml"));
            Parent root = loader.load();

            // Utiliser la même fenêtre
            Stage currentStage = (Stage) backToLoginLink.getScene().getWindow();
            currentStage.setTitle("Connexion");
            currentStage.setScene(new Scene(root));

        } catch (IOException e) {
            showMessage("Erreur lors du chargement de la page de connexion: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    private void showMessage(String message, boolean isError) {
        messageLabel.setText(message);

        if (isError) {
            messageLabel.setStyle("-fx-text-fill: #ff0000;"); // Rouge pour erreur
        } else {
            messageLabel.setStyle("-fx-text-fill: #4CAF50;"); // Vert pour succès
        }

        messageLabel.setVisible(true);
    }
}