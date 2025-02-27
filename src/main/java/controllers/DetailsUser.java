package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.User;
import model.Role;
import service.UserService;
import controllers.Validator;

public class DetailsUser {

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private TextField teleField;
    @FXML private TextField ageField;
    @FXML private ComboBox<Role> roleCombo;
    @FXML private Label errorLabel;

    private User currentUser;
    private final UserService userService = new UserService();

    public void setUserData(User user) {
        this.currentUser = user;
        nomField.setText(user.getNom_user());
        prenomField.setText(user.getPrenom_user());
        emailField.setText(user.getMail_user());
        teleField.setText(user.getTele_user());
        ageField.setText(String.valueOf(user.getAge_user()));

        roleCombo.getItems().addAll(Role.values());
        roleCombo.setValue(user.getRole_user());
    }

    @FXML
    private void handleSave() {
        try {
            validateInputs();

            // Update user object
            currentUser.setNom_user(nomField.getText());
            currentUser.setPrenom_user(prenomField.getText());
            currentUser.setMail_user(emailField.getText());
            currentUser.setTele_user(teleField.getText());
            currentUser.setAge_user(Integer.parseInt(ageField.getText()));
            currentUser.setRole_user(roleCombo.getValue());

            // Call service to update user
            userService.update(currentUser);

            showAlert("Succès", "Modifications enregistrées!", Alert.AlertType.INFORMATION);
            closeWindow();
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            showError("Erreur inattendue : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void validateInputs() {
        if (!Validator.validateName(nomField.getText())) {
            throw new IllegalArgumentException("Nom invalide");
        }
        if (!Validator.validatePrenom(prenomField.getText())) {
            throw new IllegalArgumentException("Prénom invalide");
        }
        if (!Validator.validateEmail(emailField.getText())) {
            throw new IllegalArgumentException("Email invalide");
        }
        if (!Validator.validatePhone(teleField.getText())) {
            throw new IllegalArgumentException("Téléphone doit avoir 8 chiffres");
        }
        int age;
        try {
            age = Integer.parseInt(ageField.getText());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Âge doit être un nombre");
        }
        if (!Validator.validateAge(age)) {
            throw new IllegalArgumentException("Âge doit être entre 18 et 100");
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) nomField.getScene().getWindow();
        stage.close();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
