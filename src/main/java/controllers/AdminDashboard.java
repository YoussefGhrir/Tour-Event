package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.Scene;
import model.User;
import utils.SessionManager; // Importer SessionManager

import java.io.IOException;
import java.util.Objects;

public class AdminDashboard {
    public Button addPublicationButton;
    @FXML
    private Button dashboardButton;
    @FXML
    private Button viewUsersButton;
    @FXML
    private Button addUserButton;
    @FXML
    private Button logoutButton;
    @FXML
    private Button viewFeedbackButton;
    @FXML
    private StackPane contentArea;

    private Button currentActiveButton;

    @FXML
    public void initialize() {
        // Récupérer l'utilisateur connecté depuis SessionManager
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            System.out.println("Aucun utilisateur connecté.");
            // Rediriger vers la page de connexion
            redirectToLogin();
            return;
        }

        System.out.println("Utilisateur connecté : " + currentUser.getNom_user());

        currentActiveButton = dashboardButton;
        updateButtonStyles();

        try {
            loadDashboardContent();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private Button viewReportedCommentsButton;

    @FXML
    public void handleViewReportedComments() throws IOException {
        currentActiveButton = viewReportedCommentsButton;
        updateButtonStyles();
        loadContent("/ReportedCommentsView.fxml"); // Charger la vue dédiée aux commentaires signalés
    }
    @FXML
    private void handleDashboard() throws IOException {
        currentActiveButton = dashboardButton;
        updateButtonStyles();
        loadDashboardContent();
    }

    @FXML
    public void handleViewUsers() throws IOException {
        currentActiveButton = viewUsersButton;
        updateButtonStyles();
        loadContent("/AfficherUsers.fxml");
    }

    @FXML
    public void handleAddPublication() throws IOException {
        currentActiveButton = viewUsersButton;
        updateButtonStyles();
        loadContent("/AddPublicationAdmin.fxml");
    }

    @FXML
    public void handleAddUser() throws IOException {
        currentActiveButton = addUserButton;
        updateButtonStyles();
        loadContent("/AjouterPersonne.fxml");
    }

    @FXML
    public void handleViewFeedback() throws IOException {
        currentActiveButton = viewFeedbackButton;
        updateButtonStyles();
        loadContent("/AfficherFeedback.fxml");
    }

    @FXML
    private void handleLogout() throws IOException {
        // Déconnecter l'utilisateur
        SessionManager.clearSession();

        // Rediriger vers la page de connexion
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/LoginUser.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    private void loadDashboardContent() throws IOException {
        contentArea.getChildren().clear();
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/DashboardContent.fxml")));
        Parent content = loader.load();
        contentArea.getChildren().add(content);
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
            Stage stage = (Stage) dashboardButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void updateButtonStyles() {
        String defaultStyle = "-fx-background-color: transparent; -fx-text-fill: #333333; -fx-font-weight: bold; " +
                "-fx-background-radius: 5; -fx-border-color: #e0e0e0; -fx-border-radius: 5;";
        String activeStyle = "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-background-radius: 5;";

        dashboardButton.setStyle(defaultStyle);
        viewUsersButton.setStyle(defaultStyle);
        addUserButton.setStyle(defaultStyle);
        viewFeedbackButton.setStyle(defaultStyle);
        viewReportedCommentsButton.setStyle(defaultStyle); // Ajout du bouton signalé

        if (currentActiveButton != null) {
            currentActiveButton.setStyle(activeStyle);
        }
    }
}