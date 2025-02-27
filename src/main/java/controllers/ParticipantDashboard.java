package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import model.User;
import utils.SessionManager;

import java.io.IOException;
import java.util.Objects;

public class ParticipantDashboard {
    @FXML
    private Button addPublicationButton; // Bouton pour ajouter une publication
    @FXML
    private Button myPublicationsButton; // Bouton pour mes publications
    @FXML
    private Button logoutButton; // Bouton de déconnexion
    @FXML
    private StackPane contentArea; // Zone de contenu dynamique

    private Button currentActiveButton; // Bouton actuellement sélectionné

    @FXML
    public void initialize() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            System.out.println("Aucun utilisateur connecté.");
            redirectToLogin();
            return;
        }

        System.out.println("Utilisateur connecté : " + currentUser.getNom_user());
        currentActiveButton = addPublicationButton; // Définir un bouton actif par défaut
        updateButtonStyles(); // Styles des boutons
    }

    private void updateButtonStyles() {
        String defaultStyle = "-fx-background-color: transparent; -fx-text-fill: #333; -fx-font-weight: bold; " +
                "-fx-background-radius: 5; -fx-border-color: #e0e0e0; -fx-border-radius: 5;";
        String activeStyle = "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;";

        addPublicationButton.setStyle(defaultStyle);
        myPublicationsButton.setStyle(defaultStyle);

        if (currentActiveButton != null) {
            currentActiveButton.setStyle(activeStyle);
        }
    }

    @FXML
    private void handleAddPublication() throws IOException {
        currentActiveButton = addPublicationButton;
        updateButtonStyles();
        loadContent("/AddPublicationParticipant.fxml"); // Vue pour ajouter une publication
    }

    @FXML
    private void handleMyPublications() throws IOException {
        currentActiveButton = myPublicationsButton;
        updateButtonStyles();
        loadContent("/MesPublications.fxml"); // Vue pour mes publications
    }

    @FXML
    private void handleLogout() throws IOException {
        SessionManager.clearSession();
        redirectToLogin();
    }

    private void loadContent(String fxmlPath) throws IOException {
        contentArea.getChildren().clear();
        Parent content = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxmlPath)));
        contentArea.getChildren().add(content);
    }

    private void redirectToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/LoginUser.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}