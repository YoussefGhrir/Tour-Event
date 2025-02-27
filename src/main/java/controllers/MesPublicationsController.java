package controllers;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Publication;
import model.User;
import service.PublicationService;
import service.UserService;
import utils.SessionManager;

import java.io.ByteArrayInputStream;
import java.util.List;

public class MesPublicationsController {

    @FXML
    private TilePane publicationsTilePane;

    private final PublicationService publicationService = new PublicationService();
    private final UserService userService = new UserService();

    @FXML
    public void initialize() {
        // Obtenez l'utilisateur actuellement connecté
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            System.out.println("Aucun utilisateur connecté.");
            return;
        }

        // Récupération des publications pour cet utilisateur
        List<Publication> publications = publicationService.findByUserId((long) currentUser.getId_user());
        if (publications == null || publications.isEmpty()) {
            System.out.println("Aucune publication trouvée pour l'utilisateur connecté.");
            // Gestion des cas où il n'y a pas de publications
            return;
        }

        // Afficher les publications
        showPublications(publications);
    }

    /**
     * Affiche les publications dans une grille avec des cartes.
     * @param publications Liste des publications à afficher.
     */
    private void showPublications(List<Publication> publications) {
        publicationsTilePane.getChildren().clear(); // Réinitialise la grille

        for (Publication publication : publications) {
            VBox card = createPublicationCard(publication);
            publicationsTilePane.getChildren().add(card);
        }
    }

    /**
     * Crée une carte pour une publication donnée.
     * @param publication Publication pour laquelle créer une carte.
     * @return Une VBox contenant les informations de la publication.
     */
    private VBox createPublicationCard(Publication publication) {
        // Récupère les informations de l'utilisateur qui a créé la publication
        User publicationUser = userService.findById(publication.getUserId());
        String fullName = (publicationUser != null)
                ? publicationUser.getNom_user() + " " + publicationUser.getPrenom_user()
                : "Utilisateur inconnu";

        // Image de la publication
        ImageView imageView = new ImageView();
        if (publication.getImage() != null) {
            Image image = new Image(new ByteArrayInputStream(publication.getImage()));
            imageView.setImage(image);
        }
        imageView.setFitHeight(150);
        imageView.setFitWidth(150);
        imageView.setPreserveRatio(true);

        // Titre et description
        Text title = new Text(publication.getTitre());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        Text userFullNameText = new Text("Utilisateur : " + fullName);
        userFullNameText.setStyle("-fx-font-style: italic; -fx-font-size: 12px;");

        Text description = new Text(publication.getDescription());
        description.setWrappingWidth(150);

        // Création de la carte
        VBox card = new VBox(10, imageView, title, userFullNameText, description);
        card.setStyle("-fx-background-color: white; -fx-padding: 10; -fx-border-color: #e0e0e0; -fx-border-radius: 5;" +
                "-fx-background-radius: 5; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        card.setPadding(new Insets(10));

        // Ajout d'un événement de clic pour ouvrir les détails
        card.setOnMouseClicked(event -> openDetailsModal(publication, fullName));

        return card;
    }

    /**
     * Ouvre une fenêtre modale pour afficher les détails de la publication.
     * @param publication Publication dont les détails doivent être affichés.
     * @param fullName Nom complet de l'utilisateur lié à la publication.
     */
    private void openDetailsModal(Publication publication, String fullName) {
        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.setTitle("Détails de la Publication");

        // Image de la publication
        ImageView imageView = new ImageView();
        if (publication.getImage() != null) {
            Image image = new Image(new ByteArrayInputStream(publication.getImage()));
            imageView.setImage(image);
        }
        imageView.setFitHeight(300);
        imageView.setFitWidth(300);
        imageView.setPreserveRatio(true);

        // Texte détaillé
        Text title = new Text("Titre : " + publication.getTitre());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");

        Text userFullNameText = new Text("Utilisateur : " + fullName);
        userFullNameText.setStyle("-fx-font-style: italic; -fx-font-size: 14px;");

        Text description = new Text("Description : " + publication.getDescription());
        description.setWrappingWidth(300);
        description.setStyle("-fx-font-size: 14px;");

        Text typePublication = new Text("Type de publication : " + publication.getType());
        typePublication.setStyle("-fx-font-size: 12px;");

        Text dateCreation = new Text("Date de création : " + publication.getDateCreation().toString());
        dateCreation.setStyle("-fx-font-size: 12px;");

        // Bouton de fermeture
        Button closeButton = new Button("Fermer");
        closeButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 14px;");
        closeButton.setOnAction(e -> modalStage.close());

        // Contenu de la modale
        VBox modalContent = new VBox(15, imageView, title, userFullNameText, description, typePublication, dateCreation, closeButton);
        modalContent.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-alignment: center;");
        modalContent.setPadding(new Insets(20));

        // Scène et affichage
        Scene modalScene = new Scene(modalContent, 400, 600);
        modalStage.setScene(modalScene);
        modalStage.showAndWait();
    }
}