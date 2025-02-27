package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.control.Alert;
import javafx.geometry.Insets;

public class DashboardContent {
    @FXML
    private Label titleLabel;

    @FXML
    private Button addUserQuickBtn;

    @FXML
    private Button viewUsersQuickBtn;

    @FXML
    private Button refreshBtn;

    private AdminDashboard mainController;

    @FXML
    public void initialize() {
        titleLabel.setText("Dashboard Admin");
    }

    public void setMainController(AdminDashboard mainController) {
        this.mainController = mainController;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
