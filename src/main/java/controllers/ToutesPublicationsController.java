package controllers;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Comment;
import model.CommentReaction;
import model.Publication;
import model.User;
import service.CommentService;
import service.PublicationService;
import service.UserService;
import utils.SessionManager;

import java.io.ByteArrayInputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class ToutesPublicationsController {

    @FXML
    private TilePane publicationsTilePane;

    private final PublicationService publicationService = new PublicationService();
    private final UserService userService = new UserService();
    private final CommentService commentService = new CommentService();
    private final int currentUserId = SessionManager.getCurrentUserId();
    // Variable pour stocker le commentaire actuellement affiché dans la modale
    private Comment currentReactionsModalComment;

    // Conteneur de la liste des réactions dans la modale
    private VBox currentReactionsModalContent;
    @FXML
    private Button refreshButton; // Bouton Rafraîchir


    @FXML
    public void initialize() {
        if (publicationsTilePane == null) {
            System.err.println("Le composant publicationsTilePane n'a pas été correctement initialisé. Veuillez vérifier le fichier FXML.");
            return;
        }

        List<Publication> publications = publicationService.getAllPublications(currentUserId);

        if (publications.isEmpty()) {
            Label emptyMessage = new Label("Aucune publication disponible.");
            emptyMessage.setStyle("-fx-font-size: 16px; -fx-text-fill: #757575; -fx-font-weight: bold;");
            publicationsTilePane.getChildren().add(emptyMessage);
        } else {
            displayPublications(publications);
        }

        // Action du bouton "Rafraîchir"
        if (refreshButton != null) {
            refreshButton.setOnAction(event -> refreshPublications());
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

        ImageView imageView = new ImageView();
        if (publication.getImage() != null) {
            Image image = new Image(new ByteArrayInputStream(publication.getImage()));
            imageView.setImage(image);
            imageView.setFitWidth(200);
            imageView.setFitHeight(150);
            card.getChildren().add(imageView);
        }

        Text titleText = new Text(publication.getTitre());
        titleText.setFont(Font.font("Arial", 18));
        titleText.setFill(Color.web("#0078d7"));

        Text descriptionText = new Text(publication.getDescription());
        descriptionText.setFont(Font.font("Arial", 14));
        descriptionText.setFill(Color.web("#666666"));

        Button detailsButton = new Button("Voir les détails");
        detailsButton.setOnAction(event -> openPublicationDetails(publication));

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

        GridPane header = new GridPane();
        header.setHgap(10);

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

        if (publication.getImage() != null) {
            ImageView imageView = new ImageView(new Image(new ByteArrayInputStream(publication.getImage())));
            imageView.setFitWidth(300);
            imageView.setFitHeight(200);
            modalContent.getChildren().add(imageView);
        }

        Text titleText = new Text(publication.getTitre());
        titleText.setFont(Font.font("Arial", 20));
        titleText.setFill(Color.web("#0078d7"));

        Text typeText = new Text("Type : " + publication.getType().name());
        typeText.setFont(Font.font("Arial", 16));
        typeText.setFill(Color.web("#555555"));

        Text descriptionText = new Text(publication.getDescription());
        descriptionText.setFont(Font.font("Arial", 14));
        descriptionText.setFill(Color.web("#333333"));
        descriptionText.setWrappingWidth(380);

        // Liste des commentaires avec un ScrollPane
        VBox commentsContainer = new VBox();
        commentsContainer.setSpacing(10);
        commentsContainer.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #d3d3d3; -fx-border-width: 1;");

        List<Comment> comments = commentService.getCommentsByPublicationId(publication.getId());
        for (Comment comment : comments) {
            VBox commentBox = createCommentBox(comment, commentsContainer);
            commentsContainer.getChildren().add(commentBox);
        }

        // Wrapping commentsContainer dans un ScrollPane
        ScrollPane commentsScrollPane = new ScrollPane(commentsContainer);
        commentsScrollPane.setFitToWidth(true);
        commentsScrollPane.setPrefHeight(300); // Hauteur limitée
        commentsScrollPane.setStyle("-fx-background-color: #ffffff;");

        // Ajouter un champ pour ajouter un commentaire
        HBox addCommentBox = new HBox(10);
        addCommentBox.setSpacing(10);
        addCommentBox.setPadding(new Insets(10));
        TextField commentField = new TextField();
        commentField.setPromptText("Ajouter un commentaire...");
        Button addCommentButton = new Button("Publier");
        addCommentButton.setOnAction(event -> {
            String content = commentField.getText().trim();
            if (!content.isEmpty()) {
                Comment newComment = new Comment(publication.getId(), currentUserId, content);
                commentService.addComment(newComment);
                VBox newCommentBox = createCommentBox(newComment, commentsContainer);
                commentsContainer.getChildren().add(newCommentBox);
                commentField.clear();

                refreshPublications(); // Actualise les publications après un ajout
            }
        });
        addCommentBox.getChildren().addAll(commentField, addCommentButton);

        modalContent.getChildren().addAll(header, titleText, typeText, descriptionText, commentsScrollPane, addCommentBox);

        Scene scene = new Scene(modalContent, 500, 700);
        modal.setScene(scene);
        modal.showAndWait();
    }
    private VBox createCommentBox(Comment comment, VBox commentsContainer) {
        VBox commentBox = new VBox();
        commentBox.setSpacing(5);
        commentBox.setPadding(new Insets(10));
        commentBox.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1; -fx-background-color: #ffffff;");

        // Nom de l'utilisateur ayant publié le commentaire
        Label usernameLabel = new Label(getUserFullName(comment.getUserId()));
        usernameLabel.setFont(Font.font("Arial", 14));
        usernameLabel.setTextFill(Color.DARKBLUE);

        // Date du commentaire
        Label commentDate = new Label(formatPublicationDate(comment.getDatePosted()));
        commentDate.setFont(Font.font("Arial", 10));
        commentDate.setTextFill(Color.GRAY);

        // Contenu du commentaire
        Text commentContent = new Text(comment.getContent());
        commentContent.setFont(Font.font("Arial", 12));
        commentContent.setWrappingWidth(500); // Ajuster la largeur maximale

        // Réactions existantes (totals uniquement)
        Map<String, Integer> reactions = commentService.getReactionsByCommentId(comment.getId()); // Charge le total des réactions
        HBox reactionTotalsBox = new HBox();
        reactionTotalsBox.setSpacing(10);

        if (!reactions.isEmpty()) {
            for (Map.Entry<String, Integer> reaction : reactions.entrySet()) {
                Label reactionLabel = new Label(reaction.getKey() + ": " + reaction.getValue());
                reactionLabel.setFont(Font.font("Arial", 12));
                reactionLabel.setTextFill(Color.GRAY);
                reactionTotalsBox.getChildren().add(reactionLabel);
            }
        }

        // Ajouter le bouton "Voir réactions"
        Button viewReactionsButton = new Button("Voir réactions");
        viewReactionsButton.setStyle("-fx-font-size: 12px;");
        viewReactionsButton.setOnAction(event -> openReactionDetailsModal(comment));

        // Ajouter des boutons de réaction (émojis)
        HBox reactionButtons = new HBox(10);
        reactionButtons.setPadding(new Insets(5, 0, 0, 0));

        Button likeButton = createReactionButton("😊", "like", comment, reactionTotalsBox);
        Button hahaButton = createReactionButton("😂", "haha", comment, reactionTotalsBox);
        Button sadButton = createReactionButton("😢", "sad", comment, reactionTotalsBox);
        Button angryButton = createReactionButton("😠", "angry", comment, reactionTotalsBox);

        reactionButtons.getChildren().addAll(likeButton, hahaButton, sadButton, angryButton);

        // Actions accessibles à l'auteur du commentaire : Modifier et Supprimer
        if (comment.getUserId() == currentUserId) {
            HBox userActionsBox = new HBox();
            userActionsBox.setSpacing(10);

            // Bouton Modifier
            Button editButton = new Button("Modifier");
            editButton.setStyle("-fx-background-color: #ffc107; -fx-text-fill: black;");
            editButton.setOnAction(event -> {
                // Ouvrir une fenêtre modale pour modifier le commentaire
                openEditCommentModal(comment, commentContent);
            });

            // Bouton Supprimer
            Button deleteButton = new Button("Supprimer");
            deleteButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
            deleteButton.setOnAction(event -> {
                // Supprimer le commentaire via le service
                commentService.deleteComment(comment.getId());
                // Supprimer dynamiquement de l'interface
                commentsContainer.getChildren().remove(commentBox);
            });

            // Ajouter les boutons Modifier et Supprimer
            userActionsBox.getChildren().addAll(editButton, deleteButton);

            // Ajouter les actions des utilisateurs à la boîte principale
            reactionButtons.getChildren().add(userActionsBox);
        }

        // Ajouter tous les éléments dans le conteneur de commentaire
        commentBox.getChildren().addAll(usernameLabel, commentDate, commentContent, reactionTotalsBox, reactionButtons, viewReactionsButton);

        return commentBox;
    }
    private void openReactionDetailsModal(Comment comment) {
        // Définir le commentaire sur lequel la modale va se concentrer
        currentReactionsModalComment = comment;

        // Créer une nouvelle fenêtre modale
        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setTitle("Réactions sur ce commentaire");

        VBox modalContent = new VBox();
        modalContent.setSpacing(10);
        modalContent.setPadding(new Insets(20));

        // Titre de la modale
        Label headerLabel = new Label("Détails des réactions pour ce commentaire");
        headerLabel.setFont(Font.font("Arial", 18));
        headerLabel.setTextFill(Color.DARKBLUE);

        // Conteneur pour la liste des utilisateurs ayant réagi
        currentReactionsModalContent = new VBox(); // Assigner le conteneur à la variable globale
        currentReactionsModalContent.setSpacing(10);
        currentReactionsModalContent.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-width: 1;");

        // Charger les réactions initiales
        refreshReactionsContainer(currentReactionsModalContent, comment);

        // Bouton pour fermer la fenêtre
        Button closeButton = new Button("Fermer");
        closeButton.setOnAction(event -> {
            modal.close();
            currentReactionsModalComment = null; // Réinitialiser la variable après fermeture
            currentReactionsModalContent = null; // Réinitialiser le conteneur
        });

        modalContent.getChildren().addAll(headerLabel, currentReactionsModalContent, closeButton);

        Scene modalScene = new Scene(modalContent, 400, 300); // Taille de la modale
        modal.setScene(modalScene);
        modal.show();
    }
    private Button createReactionButton(String emoji, String reactionType, Comment comment, HBox reactionTotalsBox) {
        Button reactionButton = new Button(emoji);
        reactionButton.setStyle("-fx-font-size: 14px; -fx-padding: 5px 10px;");

        reactionButton.setOnAction(event -> {
            try {
                // Ajouter ou mettre à jour la réaction de l'utilisateur
                commentService.addReaction(comment.getId(), currentUserId, reactionType);

                // Rafraîchir dynamiquement les totaux de réactions
                refreshReactionTotals(comment.getId(), reactionTotalsBox);

                // Si une modale est ouverte pour ce commentaire, rafraîchir son contenu
                if (currentReactionsModalComment != null && currentReactionsModalComment.getId() == comment.getId()) {
                    refreshReactionsContainer(currentReactionsModalContent, comment);
                }
            } catch (Exception e) {
                System.err.println("Erreur lors de l'ajout de la réaction : " + e.getMessage());
            }
        });

        return reactionButton;
    }
    /**
     * Met à jour dynamiquement les totaux des réactions dans le commentaire.
     *
     * @param commentId L'identifiant du commentaire à actualiser.
     * @param reactionTotalsBox Le conteneur (HBox) affichant les totaux des réactions.
     */
    private void refreshReactionTotals(int commentId, HBox reactionTotalsBox) {
        // Nettoyer les anciens totaux
        reactionTotalsBox.getChildren().clear();

        // Récupérer les nouveaux totaux des réactions depuis le backend
        Map<String, Integer> reactions = commentService.getReactionsByCommentId(commentId);

        // Réafficher les nouveaux totaux avec des labels
        if (!reactions.isEmpty()) {
            for (Map.Entry<String, Integer> reaction : reactions.entrySet()) {
                Label reactionLabel = new Label(reaction.getKey() + ": " + reaction.getValue());
                reactionLabel.setFont(Font.font("Arial", 12)); // Style du texte
                reactionLabel.setTextFill(Color.GRAY); // Couleur du texte
                reactionTotalsBox.getChildren().add(reactionLabel); // Ajouter au conteneur
            }
        }
    }
    private void refreshReactionsContainer(VBox reactionsContainer, Comment comment) {
        reactionsContainer.getChildren().clear(); // Nettoyer le conteneur avant le rechargement

        // Charger toutes les réactions pour ce commentaire
        List<CommentReaction> reactionsWithUsers = commentService.getReactionsWithUsers(comment.getId());

        // Ajouter chaque utilisateur ayant réagi
        for (CommentReaction reaction : reactionsWithUsers) {
            String userName = getUserFullName(reaction.getUserId());
            Label reactionLabel = new Label(userName + " a réagi avec " + reaction.getReactionType());
            reactionLabel.setFont(Font.font("Arial", 12));
            reactionsContainer.getChildren().add(reactionLabel);
        }
    }

    /**
     * Rafraîchir les commentaires d'une publication (utilisé après une réaction).
     */
    private void refreshComments() {
        publicationsTilePane.getChildren().clear();
        List<Publication> publications = publicationService.getAllPublications(currentUserId);
        displayPublications(publications);
    }
    private String getUserFullName(int userId) {
        try {
            User user = userService.findById(userId);
            if (user != null) {
                return user.getFullName();
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération de l'utilisateur : " + e.getMessage());
        }
        return "Utilisateur inconnu";
    }

    private String formatPublicationDate(java.time.LocalDateTime date) {
        if (date != null) {
            return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        }
        return "Date inconnue";
    }
    private void openEditCommentModal(Comment comment, Text commentContent) {
        // Créer une fenêtre modale
        Stage editModal = new Stage();
        editModal.initModality(Modality.APPLICATION_MODAL);
        editModal.setTitle("Modifier le commentaire");

        // Conteneur principal de la modale
        VBox modalLayout = new VBox();
        modalLayout.setSpacing(10);
        modalLayout.setPadding(new Insets(10));

        // Champ de saisie pré-rempli avec le contenu actuel du commentaire
        TextField commentField = new TextField(comment.getContent());

        // Bouton pour enregistrer les modifications
        Button saveButton = new Button("Enregistrer");
        saveButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");

        saveButton.setOnAction(event -> {
            String updatedContent = commentField.getText().trim();
            if (!updatedContent.isEmpty()) {
                // Mettre à jour dans l'objet et dans la base de données
                comment.setContent(updatedContent);
                commentService.updateComment(comment);

                // Mettre à jour dynamiquement le texte dans l'interface
                commentContent.setText(updatedContent);

                // Fermer la fenêtre modale
                editModal.close();
            }
        });

        modalLayout.getChildren().addAll(new Label("Modifier le commentaire :"), commentField, saveButton);

        // Afficher la fenêtre
        Scene modalScene = new Scene(modalLayout, 300, 150);
        editModal.setScene(modalScene);
        editModal.show();
    }
    private void refreshPublications() {
        try {
            System.out.println("Rafraîchissement des publications...");
            List<Publication> updatedPublications = publicationService.getAllPublications(currentUserId);

            // Vérifiez si des publications sont disponibles
            if (updatedPublications.isEmpty()) {
                publicationsTilePane.getChildren().clear();
                Label emptyMessage = new Label("Aucune nouvelle publication disponible.");
                emptyMessage.setStyle("-fx-font-size: 16px; -fx-text-fill: #757575; -fx-font-weight: bold;");
                publicationsTilePane.getChildren().add(emptyMessage);
            } else {
                displayPublications(updatedPublications); // Réutilisation de la méthode existante
            }
        } catch (Exception e) {
            System.err.println("Erreur de rafraîchissement : " + e.getMessage());
        }
    }
    private void refreshCommentsForPublication(int publicationId, VBox commentsContainer) {
        // Nettoyer le conteneur des commentaires existants
        commentsContainer.getChildren().clear();

        // Obtenir les commentaires mis à jour pour la publication
        List<Comment> comments = commentService.getCommentsByPublicationId(publicationId);

        // Recréer chaque commentaire dans le conteneur
        for (Comment comment : comments) {
            VBox commentBox = createCommentBox(comment, commentsContainer);
            commentsContainer.getChildren().add(commentBox);
        }
    }
}