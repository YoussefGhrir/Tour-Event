package controllers;

import javafx.animation.FadeTransition;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.Parking;
import model.Publication;
import model.PublicationType;
import model.User;
import service.ParkingService;
import service.PublicationService;
import service.UserService;
import utils.SessionManager;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MesPublicationsController {

    @FXML
    private TilePane publicationsTilePane;

    private final PublicationService publicationService = new PublicationService();
    private final UserService userService = new UserService();

    @FXML
    public void initialize() {
        loadPublicationsAsync();
    }

    private void loadPublicationsAsync() {
        Task<List<Publication>> task = new Task<>() {
            @Override
            protected List<Publication> call() {
                User currentUser = SessionManager.getCurrentUser();
                if (currentUser != null) {
                    return publicationService.findByUserId((long) currentUser.getId_user());
                }
                return List.of();
            }
        };

        task.setOnSucceeded(event -> {
            List<Publication> publications = task.getValue();
            if (publications.isEmpty()) {
                Label noPublicationsMessage = new Label("Aucune publication disponible.");
                noPublicationsMessage.setStyle("-fx-font-size: 16px; -fx-text-fill: gray;");
                publicationsTilePane.getChildren().add(noPublicationsMessage);
            } else {
                showPublications(publications);
            }
        });

        task.setOnFailed(event -> {
            System.out.println("Erreur lors du chargement des publications.");
            Label errorLabel = new Label("Erreur lors du chargement des publications.");
            errorLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: red;");
            publicationsTilePane.getChildren().add(errorLabel);
        });

        Thread backgroundThread = new Thread(task);
        backgroundThread.setDaemon(true);
        backgroundThread.start();
    }

    private void showPublications(List<Publication> publications) {
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
                "-fx-background-color: #ffffff; " +
                        "-fx-border-radius: 10; " +
                        "-fx-background-radius: 10; " +
                        "-fx-border-color: #dddddd; " +
                        "-fx-border-width: 1.5; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 2);"
        );

        ImageView imageView = new ImageView();
        if (publication.getImage() != null) {
            Image image = new Image(new ByteArrayInputStream(publication.getImage()));
            imageView.setImage(image);
            imageView.setFitHeight(120);
            imageView.setFitWidth(200);
            imageView.setStyle("-fx-border-radius: 10; -fx-background-radius: 10;");
        }

        Text title = new Text(publication.getTitre());
        title.setFont(Font.font("Arial", 18));
        title.setFill(Color.web("#1E90FF"));
        title.setStyle("-fx-font-weight: bold;");

        Text type = new Text("Type: " + publication.getType());
        type.setFont(Font.font("Arial", 14));
        type.setFill(Color.web("#4CAF50"));

        Text description = new Text(publication.getDescription());
        description.setFont(Font.font("Arial", 14));
        description.setFill(Color.web("#333333"));

        Text dateText = new Text("Date: " + formatPublicationDate(publication.getDateCreation()));
        dateText.setFont(Font.font("Arial", 12));
        dateText.setFill(Color.web("#555555"));

        Button detailsButton = new Button("Voir les détails");
        detailsButton.setStyle(
                "-fx-background-color: #4CAF50; " +
                        "-fx-border-radius: 5; " +
                        "-fx-background-radius: 5; " +
                        "-fx-text-fill: white; -fx-font-weight: bold;"
        );

        detailsButton.setOnAction(event -> openDetailsModal(publication, getUserFullName(publication.getUserId())));

        if (publication.getImage() != null) {
            card.getChildren().add(imageView);
        }
        card.getChildren().addAll(title, type, description, dateText, detailsButton);
        return card;
    }

    private void openDetailsModal(Publication publication, String username) {
        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setTitle("Détails de la publication");

        VBox modalContent = new VBox();
        modalContent.setSpacing(15);
        modalContent.setPadding(new Insets(20));
        modalContent.setStyle(
                "-fx-background-color: #f5f5f5; " +
                        "-fx-border-radius: 10; " +
                        "-fx-background-radius: 10; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
        );

        Button closeButton = new Button("Fermer");
        closeButton.setStyle(
                "-fx-background-color: #9E9E9E; -fx-text-fill: white; -fx-font-size: 14px; " +
                        "-fx-font-weight: bold; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 8 12;"
        );
        closeButton.setOnAction(e -> modal.close());
        HBox topCloseButtonContainer = new HBox(closeButton);
        topCloseButtonContainer.setAlignment(Pos.CENTER);
        modalContent.getChildren().add(topCloseButtonContainer);

        HBox headerSection = new HBox();
        headerSection.setSpacing(10);
        headerSection.setAlignment(Pos.CENTER_LEFT);
        headerSection.setPadding(new Insets(10, 10, 10, 10));
        headerSection.setStyle(
                "-fx-background-color: white; -fx-border-radius: 10; -fx-background-radius: 10;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 1);"
        );

        Label userNameLabel = new Label(username);
        userNameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        Label creationDateLabel = new Label(formatPublicationDate(publication.getDateCreation()));
        creationDateLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
        headerSection.getChildren().addAll(userNameLabel, creationDateLabel);

        ImageView imageView = new ImageView();
        if (publication.getImage() != null) {
            Image publicationImage = new Image(new ByteArrayInputStream(publication.getImage()));
            imageView.setImage(publicationImage);
            imageView.setFitWidth(400);
            imageView.setFitHeight(300);
            imageView.setPreserveRatio(true);
        } else {
            Label noImageLabel = new Label("Aucune image disponible");
            noImageLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #b0b3b8;");
            modalContent.getChildren().add(noImageLabel);
        }

        HBox titleSection = new HBox();
        titleSection.setSpacing(10);
        Label titleLabel = new Label("Titre :");
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        Label titleValue = new Label(publication.getTitre());
        titleValue.setStyle("-fx-font-size: 14px; -fx-text-fill: #34495e;");
        titleSection.getChildren().addAll(titleLabel, titleValue);

        HBox typeSection = new HBox();
        typeSection.setSpacing(10);
        Label typeLabel = new Label("Type :");
        typeLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        Label typeValue = new Label(publication.getType().name());
        typeValue.setStyle("-fx-font-size: 14px; -fx-text-fill: #1565C0;");
        typeSection.getChildren().addAll(typeLabel, typeValue);

        VBox detailsSection = new VBox();
        detailsSection.setSpacing(5);
        Label detailsLabel = new Label("Détails :");
        detailsLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        Label detailsValue = new Label(publication.getDescription());
        detailsValue.setStyle("-fx-font-size: 14px; -fx-text-fill: #34495e;");
        detailsValue.setWrapText(true);
        detailsSection.getChildren().addAll(detailsLabel, detailsValue);

        Button modifyButton = new Button("Modifier");
        modifyButton.setStyle(
                "-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-size: 14px; " +
                        "-fx-font-weight: bold; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 8 12;"
        );

        Button deleteButton = new Button("Supprimer");
        deleteButton.setStyle(
                "-fx-background-color: #E57373; -fx-text-fill: white; -fx-font-size: 14px; " +
                        "-fx-font-weight: bold; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 8 12;"
        );

        modifyButton.setOnAction(e -> modifyPublication(publication, modal));

        deleteButton.setOnAction(e -> {
            publicationService.delete(publication.getId());
            modal.close();
            refreshPublications();
        });

        HBox buttonsSection = new HBox(10, modifyButton, deleteButton);
        buttonsSection.setAlignment(Pos.CENTER);

        modalContent.getChildren().addAll(headerSection, imageView, titleSection, typeSection, detailsSection, buttonsSection);

        Scene modalScene = new Scene(modalContent, 500, 600);
        modal.setScene(modalScene);
        modal.showAndWait();
    }

    private String getUserFullName(int userId) {
        User user = userService.findById(userId);
        return (user != null) ? user.getFullName() : "Utilisateur inconnu";
    }

    private String formatPublicationDate(Object dateObject) {
        try {
            if (dateObject instanceof java.util.Date) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
                return sdf.format(dateObject);
            } else if (dateObject instanceof java.time.LocalDateTime) {
                java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
                return ((java.time.LocalDateTime) dateObject).format(formatter);
            } else if (dateObject instanceof java.time.LocalDate) {
                java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy");
                return ((java.time.LocalDate) dateObject).format(formatter);
            } else if (dateObject == null) {
                return "Date inconnue";
            } else {
                return "Format de date non reconnu";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur lors du formatage de la date";
        }
    }

    private void modifyPublication(Publication publication, Stage parentModal) {
        parentModal.hide();

        Stage editStage = new Stage();
        VBox editContent = new VBox(10);
        editContent.setPadding(new Insets(20));
        editContent.setStyle("-fx-background-color: #ffffff; -fx-border-radius: 10;");

        Label titleLabel = new Label("Modifier Publication");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        TextField titreField = new TextField(publication.getTitre());
        TextArea descriptionField = new TextArea(publication.getDescription());
        descriptionField.setWrapText(true);

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");

        ComboBox<PublicationType> typeComboBox = new ComboBox<>();
        typeComboBox.getItems().clear();
        typeComboBox.getItems().addAll(PublicationType.values());
        typeComboBox.setValue(publication.getType());

        VBox parkingSection = new VBox(5);
        ComboBox<String> parkingComboBox = new ComboBox<>();
        parkingSection.getChildren().addAll(new Label("Parking associé :"), parkingComboBox);
        parkingSection.setVisible(publication.getType() == PublicationType.PARKING);

        ParkingService parkingService = new ParkingService();
        List<Parking> parkings = parkingService.getAllParkings();
        parkingComboBox.getItems().addAll(String.valueOf(parkings));
        parkingComboBox.setValue(String.valueOf(publication.getParkingId()));

        typeComboBox.setOnAction(e -> parkingSection.setVisible(typeComboBox.getValue() == PublicationType.PARKING));

        ImageView imageView = new ImageView();
        imageView.setFitWidth(200);
        imageView.setFitHeight(200);
        imageView.setPreserveRatio(true);
        Label imageErrorLabel = new Label();
        imageErrorLabel.setStyle("-fx-text-fill: red;");

        StackPane imagePane = new StackPane(imageView);
        imagePane.setStyle("-fx-border-color: #ccc; -fx-border-width: 2; -fx-border-radius: 5;");
        imagePane.setOnDragOver(event -> {
            if (event.getGestureSource() != imagePane && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
        });

        imagePane.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                File file = db.getFiles().get(0);
                try {
                    byte[] imageBytes = Files.readAllBytes(file.toPath());
                    publication.setImage(imageBytes);

                    Image image = new Image(file.toURI().toString());
                    imageView.setImage(image);
                    success = true;
                } catch (IOException ex) {
                    imageErrorLabel.setText("Erreur lors de l'importation de l'image.");
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });

        Button saveButton = new Button("Enregistrer");
        saveButton.setOnAction(e -> {
            if (titreField.getText().trim().isEmpty()) {
                errorLabel.setText("Le titre est obligatoire.");
                return;
            }

            if (descriptionField.getText().trim().isEmpty()) {
                errorLabel.setText("La description est obligatoire.");
                return;
            }

            if (typeComboBox.getValue() == PublicationType.PARKING && parkingComboBox.getValue() == null) {
                errorLabel.setText("Veuillez sélectionner un parking.");
                return;
            }

            publication.setTitre(titreField.getText().trim());
            publication.setDescription(descriptionField.getText().trim());
            publication.setType(typeComboBox.getValue());

            if (typeComboBox.getValue() == PublicationType.PARKING) {
                publication.setParkingId(Integer.parseInt(parkingComboBox.getValue()));
            }

            publicationService.update(publication);
            editStage.close();
            refreshPublications();
        });

        Button cancelButton = new Button("Annuler");
        cancelButton.setOnAction(e -> {
            editStage.close();
            parentModal.show();
        });

        HBox buttonBox = new HBox(10, saveButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER);

        editContent.getChildren().addAll(
                titleLabel,
                new Label("Titre"), titreField,
                new Label("Description"), descriptionField,
                new Label("Type de Publication"), typeComboBox,
                parkingSection,
                new Label("Image"), imagePane, imageErrorLabel,
                errorLabel,
                buttonBox
        );

        Scene scene = new Scene(editContent, 450, 600);
        editStage.setScene(scene);
        editStage.initModality(Modality.APPLICATION_MODAL);
        editStage.showAndWait();
    }

    private void refreshPublications() {
        publicationsTilePane.getChildren().clear();

        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            System.out.println("Aucun utilisateur connecté. Impossible de rafraîchir les publications.");
            return;
        }

        List<Publication> publications = publicationService.findByUserId((long) currentUser.getId_user());

        if (publications != null && !publications.isEmpty()) {
            showPublications(publications);
        } else {
            Label noPublicationsLabel = new Label("Aucune publication à afficher.");
            noPublicationsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #757575; -fx-font-weight: bold;");
            publicationsTilePane.getChildren().add(noPublicationsLabel);
        }
    }
}