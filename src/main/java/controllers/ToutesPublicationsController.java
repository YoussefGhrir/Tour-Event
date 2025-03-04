package controllers;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Publication;
import model.User;
import service.PublicationService;
import service.UserService;
import utils.SessionManager;

import java.io.ByteArrayInputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ToutesPublicationsController {

    @FXML
    private TilePane publicationsTilePane;

    private final PublicationService publicationService = new PublicationService();
    private final UserService userService = new UserService();
    private final int currentUserId = SessionManager.getCurrentUserId(); // Identifiant de l'utilisateur connecté

    @FXML
    public void initialize() {
        // Vérifier que publicationsTilePane est initialisé
        if (publicationsTilePane == null) {
            System.err.println("Le composant publicationsTilePane n'a pas été correctement initialisé. Veuillez vérifier le fichier FXML.");
            return; // Pour éviter une erreur critique
        }

        // Charger les publications depuis la base de données
        List<Publication> publications = publicationService.getAllPublications(currentUserId);

        if (publications.isEmpty()) {
            Label emptyMessage = new Label("Aucune publication disponible.");
            emptyMessage.setStyle("-fx-font-size: 16px; -fx-text-fill: #757575; -fx-font-weight: bold;");
            publicationsTilePane.getChildren().add(emptyMessage);
        } else {
            displayPublications(publications);
        }
    }

    private void displayPublications(List<Publication> publications) {
        publicationsTilePane.getChildren().clear();
        for (Publication publication : publications) {
            VBox card = createPublicationCard(publication);
            publicationsTilePane.getChildren().add(card);
        }
    }

    private VBox createPublicationCard(Publication publication) {
        VBox card = new VBox();
        card.setSpacing(10);
        card.setPadding(new Insets(15));
        card.setStyle(
                "-fx-background-color: #f9f9f9; " +
                        "-fx-border-radius: 10; " +
                        "-fx-background-radius: 10; " +
                        "-fx-border-color: #cccccc; " +
                        "-fx-border-width: 1;"
        );

        // Image associée à la publication
        ImageView imageView = new ImageView();
        if (publication.getImage() != null) {
            Image image = new Image(new ByteArrayInputStream(publication.getImage()));
            imageView.setImage(image);
            imageView.setFitWidth(200);
            imageView.setFitHeight(150);
            card.getChildren().add(imageView);
        }

        // Titre de la publication
        Text titleText = new Text(publication.getTitre());
        titleText.setFont(Font.font("Arial", 18));
        titleText.setFill(Color.web("#0078d7"));

        // Description
        Text descriptionText = new Text(publication.getDescription());
        descriptionText.setFont(Font.font("Arial", 14));
        descriptionText.setFill(Color.web("#666666"));

        Button detailsButton = new Button("Voir les détails");
        detailsButton.setOnAction(event -> openPublicationDetails(publication));

        // Ajout des éléments dans l'ordre
        card.getChildren().addAll(titleText, descriptionText, detailsButton);
        return card;
    }

    private void openPublicationDetails(Publication publication) {
        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setTitle("Détails de la publication");

        VBox modalContent = new VBox();
        modalContent.setSpacing(15);
        modalContent.setPadding(new Insets(20));

        // Haut de la modal : nom de l'utilisateur à gauche, date de création à droite
        GridPane header = new GridPane();
        header.setHgap(10);

        // Obtenir le nom complet de l'utilisateur
        String username = getUserFullName(publication.getUserId());

        Label userLabel = new Label("Utilisateur : " + username);
        userLabel.setFont(Font.font("Arial", 14));
        userLabel.setTextFill(Color.web("#555555"));
        GridPane.setConstraints(userLabel, 0, 0);

        Label dateLabel = new Label("Créé le : " + formatPublicationDate(publication.getDateCreation()));
        dateLabel.setFont(Font.font("Arial", 14));
        dateLabel.setTextFill(Color.web("#777777"));
        GridPane.setConstraints(dateLabel, 1, 0);

        header.getChildren().addAll(userLabel, dateLabel);

        // Ajouter une image si elle existe
        if (publication.getImage() != null) {
            ImageView imageView = new ImageView(new Image(new ByteArrayInputStream(publication.getImage())));
            imageView.setFitWidth(300);
            imageView.setFitHeight(200);
            modalContent.getChildren().add(imageView);
        }

        // Titre de la publication
        Text titleText = new Text(publication.getTitre());
        titleText.setFont(Font.font("Arial", 20));
        titleText.setFill(Color.web("#0078d7"));

        // Type de publication
        Text typeText = new Text("Type : " + publication.getType());
        typeText.setFont(Font.font("Arial", 16));
        typeText.setFill(Color.web("#555555"));

        // Description de la publication
        Text descriptionText = new Text(publication.getDescription());
        descriptionText.setFont(Font.font("Arial", 14));
        descriptionText.setFill(Color.web("#333333"));

        // Ajouter les éléments au contenu de la modal
        modalContent.getChildren().addAll(header, titleText, typeText, descriptionText);

        // Afficher la fenêtre modale
        Scene scene = new Scene(modalContent, 400, 500);
        modal.setScene(scene);
        modal.showAndWait();
    }

    private String getUserFullName(int userId) {
        // Appel à UserService pour obtenir le nom complet de l'utilisateur
        try {
            User user = userService.findById(userId);
            if (user != null) {
                return user.getFullName(); // Utilise la méthode dédiée de la classe User
            }
            System.err.println("Utilisateur introuvable pour l'ID : " + userId);
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération du nom complet de l'utilisateur : " + e.getMessage());
        }
        return "Utilisateur inconnu"; // Valeur par défaut si une erreur survient
    }

    private String formatPublicationDate(java.time.LocalDateTime date) {
        if (date != null) {
            return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        }
        return "Date inconnue";
    }
}