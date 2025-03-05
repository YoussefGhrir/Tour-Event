package controllers;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.Comment;
import model.CommentReaction;
import model.Publication;
import model.User;
import service.*;
import utils.MyDatabse;
import utils.SessionManager;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class ToutesPublicationsController {

    @FXML
    private TilePane publicationsTilePane;

    private final PublicationService publicationService = new PublicationService();
    private final UserService userService = new UserService();
    private final CommentService commentService = new CommentService();
    private final ToxicityService toxicityService = new ToxicityService(); // Service de toxicité
    private final int currentUserId = SessionManager.getCurrentUserId();
    private Comment currentReactionsModalComment;
    private VBox currentReactionsModalContent;
    @FXML
    private Button refreshButton;

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
        card.setStyle("-fx-background-color: #ffffff; " +
                "-fx-border-radius: 12; " +
                "-fx-background-radius: 12; " +
                "-fx-border-color: #dddddd; " +
                "-fx-border-width: 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);");

        card.setOnMouseEntered(event -> {
            card.setStyle("-fx-background-color: #f9f9f9; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 5);");
        });
        card.setOnMouseExited(event -> {
            card.setStyle("-fx-background-color: #ffffff; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);");
        });

        ImageView imageView = new ImageView();
        if (publication.getImage() != null) {
            Image image = new Image(new ByteArrayInputStream(publication.getImage()));
            imageView.setImage(image);
            imageView.setFitWidth(150);
            imageView.setFitHeight(100);
            imageView.setPreserveRatio(true);
            imageView.setStyle("-fx-border-radius: 8; -fx-background-radius: 8;");
            card.getChildren().add(imageView);
        }

        Text titleText = new Text(publication.getTitre());
        titleText.setFont(Font.font("Roboto", FontWeight.BOLD, 18));
        titleText.setFill(Color.web("#333333"));

        Text descriptionText = new Text(publication.getDescription());
        descriptionText.setFont(Font.font("Open Sans", 14));
        descriptionText.setFill(Color.web("#666666"));

        PublicationRatingService ratingService = new PublicationRatingService();
        float averageRating = 0.0f;
        int totalVotes = 0;

        try {
            averageRating = ratingService.getAverageRating(publication.getId());
            totalVotes = ratingService.getTotalVotes(publication.getId());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        HBox starsBox = createStarDisplay(averageRating);

        Label votesLabel = new Label(" (" + totalVotes + " votes)");
        votesLabel.setFont(Font.font("Arial", 12));
        votesLabel.setTextFill(Color.web("#777777"));

        HBox ratingBox = new HBox(5, starsBox, votesLabel);
        ratingBox.setAlignment(Pos.CENTER_LEFT);

        Button detailsButton = new Button("Voir les détails");
        detailsButton.setStyle("-fx-background-color: #1877F2; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-padding: 8 15; " +
                "-fx-border-radius: 8; " +
                "-fx-background-radius: 8;");

        detailsButton.setOnMouseEntered(event -> {
            detailsButton.setStyle("-fx-background-color: #1565C0; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 1);");
        });
        detailsButton.setOnMouseExited(event -> {
            detailsButton.setStyle("-fx-background-color: #1877F2; " +
                    "-fx-effect: none;");
        });

        detailsButton.setOnAction(event -> openPublicationDetails(publication));

        VBox textContainer = new VBox(titleText, descriptionText);
        textContainer.setSpacing(8);

        card.getChildren().addAll(textContainer, ratingBox, detailsButton);
        return card;
    }

    private HBox createStarDisplay(float averageRating) {
        HBox starBox = new HBox();
        starBox.setSpacing(2);

        for (int i = 1; i <= 5; i++) {
            Label star = new Label("★");
            star.setFont(Font.font("Arial", 16));

            if (i <= averageRating) {
                star.setTextFill(Color.GOLD);
            } else if (i - 1 < averageRating && i > averageRating) {
                star.setTextFill(Color.LIGHTGOLDENRODYELLOW);
            } else {
                star.setTextFill(Color.GREY);
            }

            starBox.getChildren().add(star);
        }

        return starBox;
    }

    private void openPublicationDetails(Publication publication) {
        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setTitle("Détails de la publication");

        VBox modalContent = new VBox();
        modalContent.setSpacing(15);
        modalContent.setPadding(new Insets(20));
        modalContent.setStyle("-fx-background-color: #f0f2f5; " +
                "-fx-border-radius: 12; " +
                "-fx-background-radius: 12;");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-padding: 10; " +
                "-fx-background-color: #ffffff; " +
                "-fx-border-radius: 8; " +
                "-fx-background-radius: 8; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 5);");

        String username = getUserFullName(publication.getUserId());

        Label userLabel = new Label(username);
        userLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 16));
        userLabel.setTextFill(Color.web("#333333"));

        Label dateLabel = new Label(formatPublicationDate(publication.getDateCreation()));
        dateLabel.setFont(Font.font("Open Sans", 12));
        dateLabel.setTextFill(Color.web("#777777"));

        VBox headerInfo = new VBox(userLabel, dateLabel);
        header.getChildren().add(headerInfo);

        if (publication.getImage() != null) {
            ImageView imageView = new ImageView(new Image(new ByteArrayInputStream(publication.getImage())));
            imageView.setFitWidth(400);
            imageView.setFitHeight(250);
            imageView.setStyle("-fx-border-radius: 8; -fx-background-radius: 8;");
            modalContent.getChildren().add(imageView);
        }

        Text titleText = new Text(publication.getTitre());
        titleText.setFont(Font.font("Roboto", FontWeight.BOLD, 22));
        titleText.setFill(Color.web("#333333"));

        Text descriptionText = new Text(publication.getDescription());
        descriptionText.setFont(Font.font("Open Sans", 14));
        descriptionText.setFill(Color.web("#333333"));
        descriptionText.setWrappingWidth(550);

        PublicationRatingService ratingService = new PublicationRatingService();
        Label averageRatingLabel = new Label();
        HBox starBox = new HBox();
        starBox.setSpacing(5);
        starBox.setPadding(new Insets(10));

        try {
            float averageRating = ratingService.getAverageRating(publication.getId());
            int totalVotes = ratingService.getTotalVotes(publication.getId());
            averageRatingLabel.setText("Note Moyenne : " +
                    (averageRating > 0 ? String.format("%.2f", averageRating) : "Aucune") +
                    " (" + totalVotes + " votes)");

            int userRating = ratingService.getUserRating(currentUserId, publication.getId());
            updateStars(starBox, userRating, ratingService, publication, averageRatingLabel);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Text starsTitle = new Text("Notez cette publication :");
        starsTitle.setFont(Font.font("Roboto", FontWeight.BOLD, 14));
        starsTitle.setFill(Color.GRAY);

        VBox starsContainer = new VBox(starsTitle, starBox, averageRatingLabel);
        starsContainer.setSpacing(5);
        starsContainer.setPadding(new Insets(10));

        VBox commentsContainer = new VBox();
        commentsContainer.setSpacing(10);
        commentsContainer.setStyle("-fx-background-color: #ffffff; " +
                "-fx-border-radius: 8; " +
                "-fx-padding: 10;");
        commentsContainer.setPadding(new Insets(10));
        refreshCommentsInModal(commentsContainer, publication.getId());

        ScrollPane commentsScrollPane = new ScrollPane(commentsContainer);
        commentsScrollPane.setFitToWidth(true);
        commentsScrollPane.setPrefHeight(500);
        commentsScrollPane.setStyle("-fx-background-color: transparent;");

        HBox addCommentBox = new HBox(10);
        addCommentBox.setSpacing(10);
        addCommentBox.setPadding(new Insets(10));
        addCommentBox.setStyle("-fx-background-color: #f0f2f5; -fx-border-radius: 20;");
        TextField commentField = new TextField();
        commentField.setPromptText("Écrivez un commentaire...");
        commentField.setStyle("-fx-background-color: transparent;");
        Button addCommentButton = new Button("Publier");
        addCommentButton.setStyle("-fx-background-color: #1877F2; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-padding: 8 15; " +
                "-fx-border-radius: 8; " +
                "-fx-background-radius: 8;");

        addCommentButton.setOnMouseEntered(event -> {
            addCommentButton.setStyle("-fx-background-color: #1565C0; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 1);");
        });
        addCommentButton.setOnMouseExited(event -> {
            addCommentButton.setStyle("-fx-background-color: #1877F2; " +
                    "-fx-effect: none;");
        });

        addCommentButton.setOnAction(event -> {
            String content = commentField.getText().trim();
            if (!content.isEmpty()) {
                addCommentWithToxicityCheck(publication.getId(), content, commentsContainer);
                commentField.clear();
            }
        });
        addCommentBox.getChildren().addAll(commentField, addCommentButton);

        modalContent.getChildren().addAll(
                header, titleText, descriptionText,
                starsContainer,
                commentsScrollPane,
                addCommentBox
        );

        Scene scene = new Scene(modalContent, 650, 950);
        modal.setScene(scene);
        modal.showAndWait();
    }

    private void updateStars(HBox starBox, int currentRating, PublicationRatingService ratingService,
                             Publication publication, Label averageRatingLabel) {
        starBox.getChildren().clear();

        for (int i = 1; i <= 5; i++) {
            Label star = new Label("★");
            star.setFont(Font.font("Arial", 26));
            star.setTextFill(i <= currentRating ? Color.GOLD : Color.GREY);

            final int rating = i;
            star.setOnMouseClicked(event -> {
                try {
                    ratingService.addOrUpdateRating(currentUserId, publication.getId(), rating);
                    updateStars(starBox, rating, ratingService, publication, averageRatingLabel);

                    float newAverageRating = ratingService.getAverageRating(publication.getId());
                    int newTotalVotes = ratingService.getTotalVotes(publication.getId());
                    averageRatingLabel.setText("Note Moyenne : " +
                            String.format("%.2f", newAverageRating) +
                            " (" + newTotalVotes + " votes)"
                    );
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });

            starBox.getChildren().add(star);
        }
    }

    private void refreshCommentsInModal(VBox commentsContainer, int publicationId) {
        List<Comment> comments = commentService.getCommentsByPublicationId(publicationId);
        commentsContainer.getChildren().clear();

        for (Comment comment : comments) {
            VBox commentBox = createCommentBox(comment, commentsContainer);
            commentsContainer.getChildren().add(commentBox);
            detectToxicityAsync(comment, commentBox);
        }
    }

    private VBox createCommentBox(Comment comment, VBox commentsContainer) {
        VBox commentBox = new VBox();
        commentBox.setSpacing(5);
        commentBox.setPadding(new Insets(10));
        commentBox.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1; -fx-background-color: #ffffff;");

        Label usernameLabel = new Label(getUserFullName(comment.getUserId()));
        usernameLabel.setFont(Font.font("Arial", 14));
        usernameLabel.setTextFill(Color.DARKBLUE);

        Label commentDate = new Label(formatPublicationDate(comment.getDatePosted()));
        commentDate.setFont(Font.font("Arial", 10));
        commentDate.setTextFill(Color.GRAY);

        Text commentContent = new Text(comment.getContent());
        commentContent.setFont(Font.font("Arial", 12));
        commentContent.setWrappingWidth(500);

        Button reportButton = new Button("Signaler");
        reportButton.setStyle("-fx-background-color: #ff4d4d; -fx-text-fill: white; -fx-font-weight: bold;");
        reportButton.setOnAction(event -> openReportModal(comment));

        HBox reactionTotalsBox = new HBox();
        reactionTotalsBox.setSpacing(10);

        Map<String, Integer> reactions = commentService.getReactionsByCommentId(comment.getId());
        if (!reactions.isEmpty()) {
            for (Map.Entry<String, Integer> reaction : reactions.entrySet()) {
                Label reactionLabel = new Label(reaction.getKey() + ": " + reaction.getValue());
                reactionLabel.setFont(Font.font("Arial", 12));
                reactionLabel.setTextFill(Color.GRAY);
                reactionTotalsBox.getChildren().add(reactionLabel);
            }
        }

        HBox reactionButtons = new HBox(10);
        reactionButtons.setPadding(new Insets(5, 0, 0, 0));

        Button likeButton = createReactionButton("😊", "like", comment, reactionTotalsBox);
        Button hahaButton = createReactionButton("😂", "haha", comment, reactionTotalsBox);
        Button sadButton = createReactionButton("😢", "sad", comment, reactionTotalsBox);
        Button angryButton = createReactionButton("😠", "angry", comment, reactionTotalsBox);

        reactionButtons.getChildren().addAll(likeButton, hahaButton, sadButton, angryButton);

        if (comment.getUserId() == currentUserId) {
            HBox userActionsBox = new HBox();
            userActionsBox.setSpacing(10);

            Button editButton = new Button("Modifier");
            editButton.setStyle("-fx-background-color: #ffc107; -fx-text-fill: black;");
            editButton.setOnAction(event -> openEditCommentModal(comment, commentContent));

            Button deleteButton = new Button("Supprimer");
            deleteButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
            deleteButton.setOnAction(event -> {
                commentService.deleteComment(comment.getId());
                commentsContainer.getChildren().remove(commentBox);
            });

            userActionsBox.getChildren().addAll(editButton, deleteButton);
            reactionButtons.getChildren().add(userActionsBox);
        }

        commentBox.getChildren().addAll(usernameLabel, commentDate, commentContent, reactionTotalsBox, reactionButtons, reportButton);
        return commentBox;
    }

    private void openReportModal(Comment comment) {
        Stage reportModal = new Stage();
        reportModal.initModality(Modality.APPLICATION_MODAL);
        reportModal.setTitle("Signaler un commentaire");

        VBox modalContent = new VBox();
        modalContent.setSpacing(10);
        modalContent.setPadding(new Insets(20));
        modalContent.setStyle("-fx-background-color: #f9f9f9;");

        Label instructionLabel = new Label("Veuillez indiquer un motif de signalement :");
        instructionLabel.setFont(Font.font("Arial", 14));
        instructionLabel.setTextFill(Color.BLACK);

        TextField reasonField = new TextField();
        reasonField.setPromptText("Entrez votre motif ici...");

        Button submitButton = new Button("Envoyer");
        submitButton.setStyle("-fx-background-color: #ff4d4d; -fx-text-fill: white; -fx-font-weight: bold;");
        submitButton.setOnAction(event -> {
                    String reason = reasonField.getText().trim();
                    if (!reason.isEmpty()) {
                        try {
                            commentService.reportComment(comment.getId(), reason);
                            System.out.println("Commentaire signalé : " + reason);
                            reportModal.close();
                        } catch (SQLException e) {
                            System.err.println("Erreur lors du signalement : " + e.getMessage());
                        }
                    }
                });

        modalContent.getChildren().addAll(instructionLabel, reasonField, submitButton);

        Scene scene = new Scene(modalContent, 300, 200);
        reportModal.setScene(scene);
        reportModal.showAndWait();
    }

    private Button createReactionButton(String emoji, String reactionType, Comment comment, HBox reactionTotalsBox) {
        Button reactionButton = new Button(emoji);
        reactionButton.setStyle("-fx-font-size: 14px; -fx-padding: 5px 10px;");

        reactionButton.setOnAction(event -> {
            try {
                commentService.addReaction(comment.getId(), currentUserId, reactionType);
                refreshReactionTotals(comment.getId(), reactionTotalsBox);
            } catch (Exception e) {
                System.err.println("Erreur lors de l'ajout de la réaction : " + e.getMessage());
            }
        });

        return reactionButton;
    }

    private void refreshReactionTotals(int commentId, HBox reactionTotalsBox) {
        reactionTotalsBox.getChildren().clear();
        Map<String, Integer> reactions = commentService.getReactionsByCommentId(commentId);

        if (!reactions.isEmpty()) {
            for (Map.Entry<String, Integer> reaction : reactions.entrySet()) {
                Label reactionLabel = new Label(reaction.getKey() + ": " + reaction.getValue());
                reactionLabel.setFont(Font.font("Arial", 12));
                reactionLabel.setTextFill(Color.GRAY);
                reactionTotalsBox.getChildren().add(reactionLabel);
            }
        }
    }
    private void detectToxicityAsync(Comment comment, VBox commentBox) {
        if (comment == null || comment.getContent() == null || comment.getContent().trim().isEmpty()) {
            System.out.println("Commentaire invalide détecté pour l'analyse de toxicité.");
            return;
        }

        PauseTransition pause = new PauseTransition(Duration.seconds(3)); // 3 secondes
        pause.setOnFinished(event -> {
            try {
                double toxicityScore = toxicityService.analyzeToxicity(comment.getContent());
                System.out.println("Score de toxicité pour \"" + comment.getContent() + "\" : " + toxicityScore);

                if (toxicityScore >= 0.8) { // Seuil augmenté à 0.8
                    commentBox.setStyle("-fx-background-color: #ffcccc;");
                    Label toxicLabel = new Label("Ce commentaire a été masqué pour contenu inapproprié.");
                    toxicLabel.setStyle("-fx-text-fill: red; -fx-font-style: italic;");
                    commentBox.getChildren().clear();
                    commentBox.getChildren().add(toxicLabel);
                }
            } catch (Exception e) {
                System.err.println("Erreur lors de la détection de la toxicité : " + e.getMessage());
            }
        });
        pause.play();
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
        Stage editModal = new Stage();
        editModal.initModality(Modality.APPLICATION_MODAL);
        editModal.setTitle("Modifier le commentaire");

        VBox modalLayout = new VBox();
        modalLayout.setSpacing(10);
        modalLayout.setPadding(new Insets(10));

        TextField commentField = new TextField(comment.getContent());

        Button saveButton = new Button("Enregistrer");
        saveButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");

        saveButton.setOnAction(event -> {
            String updatedContent = commentField.getText().trim();
            if (!updatedContent.isEmpty()) {
                comment.setContent(updatedContent);
                commentService.updateComment(comment);
                commentContent.setText(updatedContent);
                editModal.close();
            }
        });

        modalLayout.getChildren().addAll(new Label("Modifier le commentaire :"), commentField, saveButton);

        Scene modalScene = new Scene(modalLayout, 300, 150);
        editModal.setScene(modalScene);
        editModal.show();
    }

    private void refreshPublications() {
        try {
            System.out.println("Rafraîchissement des publications...");
            List<Publication> updatedPublications = publicationService.getAllPublications(currentUserId);

            if (updatedPublications.isEmpty()) {
                publicationsTilePane.getChildren().clear();
                Label emptyMessage = new Label("Aucune nouvelle publication disponible.");
                emptyMessage.setStyle("-fx-font-size: 16px; -fx-text-fill: #757575; -fx-font-weight: bold;");
                publicationsTilePane.getChildren().add(emptyMessage);
            } else {
                displayPublications(updatedPublications);
            }
        } catch (Exception e) {
            System.err.println("Erreur de rafraîchissement : " + e.getMessage());
        }
    }
    private void addCommentWithToxicityCheck(int publicationId, String commentText, VBox commentsContainer) {
        if (commentText == null || commentText.trim().isEmpty()) {
            showAlert("Erreur", "Veuillez écrire un commentaire avant de soumettre.", Alert.AlertType.WARNING);
            return;
        }

        try {
            double toxicityScore = toxicityService.analyzeToxicity(commentText);

            // Ajustement du seuil de toxicité
            if (toxicityScore >= 0.8) { // Seuil augmenté à 0.8
                showAlert("Commentaire Rejeté", "Votre commentaire est considéré comme toxique et ne peut pas être ajouté.", Alert.AlertType.ERROR);
                return;
            }

            Comment newComment = new Comment(publicationId, currentUserId, commentText);
            commentService.addComment(newComment);
            refreshCommentsInModal(commentsContainer, publicationId);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Une erreur est survenue lors de l'ajout du commentaire : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}