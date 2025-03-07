package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.FileChooser;
import model.Publication;
import model.PublicationType;
import model.User;
import service.HuggingFaceService;
import service.PublicationService;
import utils.SessionManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import net.coobird.thumbnailator.Thumbnails;
import java.io.ByteArrayOutputStream;

public class AddPublicationParticipantController {

    @FXML private TextField titreField;
    @FXML private Label titreErrorLabel; // Label pour les erreurs de titre
    @FXML private TextArea descriptionField;
    @FXML private Label descriptionErrorLabel; // Label pour les erreurs de description
    @FXML private ComboBox<PublicationType> typeComboBox;
    @FXML private Label typeErrorLabel; // Label pour les erreurs de type
    @FXML private VBox covoiturageSection; // Section spécifique au covoiturage
    @FXML private ImageView imageView;
    @FXML private Label imageLabel;
    @FXML private Label imageErrorLabel; // Label pour les erreurs d'image
    @FXML private Button addImageButton;

    private User currentUser;
    private final PublicationService publicationService = new PublicationService();
    private byte[] imageBytes;

    @FXML
    public void initialize() {
        currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            System.out.println("Aucun utilisateur connecté.");
            return;
        }

        // Initialiser le ComboBox avec les types de publication autorisés
        typeComboBox.getItems().setAll(PublicationType.NORMAL, PublicationType.COVOITURAGE);

        // Masquer la section covoiturage par défaut
        covoiturageSection.setVisible(false);

        // Gérer la visibilité de la section covoiturage en fonction du type sélectionné
        typeComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            covoiturageSection.setVisible(newVal == PublicationType.COVOITURAGE);
        });

        // Gestion du glisser-déposer d'image
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



        // Créer la publication
        try {
            Publication publication;
            if (selectedType == PublicationType.NORMAL) {
                publication = new Publication(titre, description, imageBytes, userId);

            } else if (selectedType == PublicationType.COVOITURAGE) {
                publication = new Publication(titre, description, imageBytes, userId);
                publication.setType(PublicationType.COVOITURAGE);
            } else {
                throw new IllegalArgumentException("Type de publication non valide.");
            }

            publicationService.add(publication); // Cette méthode peut lancer RuntimeException
            showAlert("Succès", "Publication créée", "La publication a été créée avec succès.");
            clearForm(); // Réinitialiser les champs après un ajout réussi

        } catch (RuntimeException e) {
            // Gérer les erreurs spécifiques (duplication, problème d'ajout, etc.)
            showAlert("Erreur", "Ajout échoué", e.getMessage());
        } catch (Exception e) {
            // Capturer toutes les autres exceptions (problèmes inattendus)
            showAlert("Erreur", "Erreur inattendue", "Une erreur s'est produite lors de la création de la publication.");
            e.printStackTrace();
        }
    }

    // Réinitialiser le formulaire après un ajout réussi
    private void clearForm() {
        titreField.clear();
        descriptionField.clear();
        typeComboBox.setValue(null);
        imageView.setImage(null);
        imageBytes = null;
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    @FXML
    private void handleGenerateTitle() {
        String description = descriptionField.getText();
        if (description != null && !description.isEmpty()) {
            System.out.println("Description saisie : " + description); // Log de la description

            HuggingFaceService huggingFaceService = new HuggingFaceService();
            String generatedTitle = huggingFaceService.generateTitle(description);

            System.out.println("Titre généré : " + generatedTitle); // Log du titre généré

            titreField.setText(generatedTitle); // Remplir le champ de titre
        } else {
            System.err.println("Erreur : La description est vide."); // Log d'erreur
            showAlert("Erreur", "Description vide", "Veuillez saisir une description pour générer un titre.");
        }
    }
}
