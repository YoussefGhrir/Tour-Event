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

public class OrganisateurDashboard {
    @FXML
    private Button addPublicationButton;
    @FXML
    private Button myPublicationsButton;

    @FXML
    private StackPane contentArea;

    private Button currentActiveButton;

    @FXML
    public void initialize() {
        // Vérifier si un utilisateur est connecté
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            System.out.println("Aucun utilisateur connecté.");
            // Redirection vers la page de connexion
            redirectToLogin();
            return;
        }

        System.out.println("Utilisateur connecté : " + currentUser.getNom_user());

        // Définir "Mes Publications" comme bouton actif par défaut
        currentActiveButton = myPublicationsButton;
        updateButtonStyles();

        // Charger directement la vue "Mes Publications"
        try {
            loadContent("/MesPublications.fxml");
        } catch (IOException e) {

            System.err.println("Erreur lors du chargement de la vue MesPublications : " + e.getMessage());
        }
    }

    private void updateButtonStyles() {
        String defaultStyle = "-fx-background-color: transparent; -fx-text-fill: #333333; -fx-font-weight: bold; " +
                "-fx-background-radius: 5; -fx-border-color: #e0e0e0; -fx-border-radius: 5;";
        String activeStyle = "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-background-radius: 5;";

        addPublicationButton.setStyle(defaultStyle);
        myPublicationsButton.setStyle(defaultStyle); // Style par défaut pour les boutons

        if (currentActiveButton != null) {
            currentActiveButton.setStyle(activeStyle); // Appliquer le style actif au bouton actif
        }
    }

    @FXML
    private void handleAddPublication() throws IOException {
        currentActiveButton = addPublicationButton;
        updateButtonStyles();
        loadContent("/AddPublicationOrganisateur.fxml");
    }

    @FXML
    private void handleLogout() throws IOException {
        // Déconnecter l'utilisateur
        SessionManager.clearSession();

        // Redirection vers la page de connexion
        redirectToLogin();
    }

    @FXML
    private void handleMyPublications() throws IOException {
        currentActiveButton = myPublicationsButton; // Mettre à jour le bouton actif
        updateButtonStyles(); // Appliquer les styles mis à jour
        loadContent("/MesPublications.fxml"); // Naviguer vers la vue "Mes Publications"
    }

    private void loadContent(String fxmlPath) throws IOException {
        // Vérifier si contentArea est bien initialisé
        if (contentArea == null) {
            throw new IllegalStateException("Le composant contentArea n'est pas initialisé. Vérifiez le fichier FXML.");
        }
        contentArea.getChildren().clear(); // Effacer le contenu existant
        Parent content = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxmlPath)));
        contentArea.getChildren().add(content); // Ajouter le nouveau contenu
    }

    private void redirectToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/LoginUser.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) addPublicationButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}