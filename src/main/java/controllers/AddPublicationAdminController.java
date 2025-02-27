package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.FileChooser;
import model.Parking;
import model.Publication;
import model.PublicationType;
import model.User;
import service.PublicationService;
import service.ParkingService;
import utils.SessionManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.coobird.thumbnailator.Thumbnails;
import java.io.ByteArrayOutputStream;

public class AddPublicationAdminController {

    @FXML private TextField titreField;
    @FXML private Label titreErrorLabel; // Label pour les erreurs de titre
    @FXML private TextArea descriptionField;
    @FXML private Label descriptionErrorLabel; // Label pour les erreurs de description
    @FXML private ComboBox<PublicationType> typeComboBox;
    @FXML private Label typeErrorLabel; // Label pour les erreurs de type
    @FXML private VBox parkingSection;
    @FXML private ComboBox<String> parkingComboBox;
    @FXML private Label parkingErrorLabel; // Label pour les erreurs de parking
    @FXML private TextField searchParkingField;
    @FXML private ImageView imageView;
    @FXML private Label imageLabel;
    @FXML private Label imageErrorLabel; // Label pour les erreurs d'image
    @FXML private Button addImageButton;

    private User currentUser;
    private final PublicationService publicationService = new PublicationService();
    private byte[] imageBytes;
    private Map<String, Integer> parkingMap;

    @FXML
    public void initialize() {
        currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            System.out.println("Aucun utilisateur connecté.");
            return;
        }

        typeComboBox.getItems().setAll(PublicationType.NORMAL, PublicationType.PARKING);

        ParkingService parkingService = new ParkingService();
        List<Parking> parkings = parkingService.getAllParkings();
        parkingMap = new HashMap<>();

        for (Parking parking : parkings) {
            parkingMap.put(parking.getNomPark(), parking.getIdPark());
            parkingComboBox.getItems().add(parking.getNomPark());
        }

        typeComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            parkingSection.setVisible(newVal == PublicationType.PARKING);
        });

        imageLabel.setOnDragOver(event -> {
            if (event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        imageLabel.setOnDragDropped(event -> {
            Dragboard dragboard = event.getDragboard();
            boolean success = false;
            if (dragboard.hasFiles()) {
                try {
                    File file = dragboard.getFiles().get(0);
                    imageBytes = Files.readAllBytes(file.toPath());
                    Image image = new Image(file.toURI().toString());
                    imageView.setImage(image);
                    success = true;
                    imageErrorLabel.setText(""); // Effacer le message d'erreur si l'image est valide
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    @FXML
    private void handleAddImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            try {
                imageBytes = resizeImage(file, 800, 600);
                Image image = new Image(file.toURI().toString());
                imageView.setImage(image);
                imageErrorLabel.setText(""); // Effacer le message d'erreur si l'image est valide
            } catch (IOException e) {
                e.printStackTrace();
                imageErrorLabel.setText("Erreur : Impossible de lire ou redimensionner l'image.");
            }
        }
    }

    private byte[] resizeImage(File file, int width, int height) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Thumbnails.of(file)
                .size(width, height)
                .outputFormat("jpg")
                .toOutputStream(outputStream);
        return outputStream.toByteArray();
    }

    @FXML
    private void handleAddPublication() {
        if (currentUser == null) {
            showAlert("Erreur", "Utilisateur non connecté", "Veuillez vous connecter.");
            return;
        }

        // Réinitialiser les messages d'erreur
        titreErrorLabel.setText("");
        descriptionErrorLabel.setText("");
        typeErrorLabel.setText("");
        parkingErrorLabel.setText("");
        imageErrorLabel.setText("");

        // Récupérer les valeurs des champs
        String titre = titreField.getText();
        String description = descriptionField.getText();
        int userId = currentUser.getId_user();

        // Validation du titre
        if (titre == null || titre.isEmpty()) {
            titreErrorLabel.setText("Le titre est obligatoire.");
            return;
        }
        if (titre.length() > 100) {
            titreErrorLabel.setText("Le titre ne doit pas dépasser 100 caractères.");
            return;
        }

        // Validation de la description
        if (description == null || description.isEmpty()) {
            descriptionErrorLabel.setText("La description est obligatoire.");
            return;
        }
        if (description.length() > 500) {
            descriptionErrorLabel.setText("La description ne doit pas dépasser 500 caractères.");
            return;
        }

        // Validation de l'image
        if (imageBytes == null) {
            imageErrorLabel.setText("Veuillez sélectionner une image.");
            return;
        }


        // Validation du type de publication
        PublicationType selectedType = typeComboBox.getValue();
        if (selectedType == null) {
            typeErrorLabel.setText("Veuillez sélectionner un type de publication.");
            return;
        }

        // Validation du parking (si le type est PARKING)
        if (selectedType == PublicationType.PARKING) {
            String selectedParkingName = parkingComboBox.getValue();
            if (selectedParkingName == null) {
                parkingErrorLabel.setText("Veuillez sélectionner un parking.");
                return;
            }
        }

        // Créer la publication
        Publication publication;
        try {
            if (selectedType == PublicationType.NORMAL) {
                publication = new Publication(titre, description, imageBytes, userId);
            } else {
                int parkingId = parkingMap.get(parkingComboBox.getValue());
                publication = new Publication(titre, description, imageBytes, userId, parkingId);
            }

            publicationService.add(publication);
            showAlert("Succès", "Publication créée", "La publication a été créée avec succès.");
        } catch (Exception e) {
            showAlert("Erreur", "Erreur inattendue", "Une erreur s'est produite lors de la création de la publication.");
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}