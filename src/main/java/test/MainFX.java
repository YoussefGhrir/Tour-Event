package test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import model.Role;
import model.User;
import service.UserService;
import utils.DatabaseManager;

public class MainFX extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Charger le fichier FXML pour LoginUser
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/LoginUser.fxml"));
        AnchorPane root = loader.load();

        // Créer la scène et l'afficher
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Register User");
        primaryStage.show();
    }

    public static void main(String[] args) {
        // Initialiser la base de données et créer les tables
        DatabaseManager dbManager = DatabaseManager.getInstance();

        // Créer les tables (si ce n'est pas déjà fait)
        dbManager.createTables();

        // Créer un administrateur par défaut avant de lancer l'application
        createDefaultAdmin();

        // Lancer l'application JavaFX
        launch(args);
    }

    private static void createDefaultAdmin() {
        UserService userService = new UserService();

        // Vérifier si un admin existe déjà
        User existingAdmin = userService.findByEmail("admin@example.com");
        if (existingAdmin == null) {
            // Créer l'utilisateur admin
            User admin = new User();
            admin.setNom_user("Admin");
            admin.setPrenom_user("User");
            admin.setAge_user(30);
            admin.setRole_user(Role.ADMIN);
            admin.setMail_user("admin@example.com");
            admin.setPassword_user("Admin@123"); // ⚠️ Idéalement devrait être haché
            admin.setTele_user("123456789");

            // Ajouter l'admin à la base de données
            userService.add(admin);
            System.out.println("Admin créé avec succès !");
        } else {
            System.out.println("Admin déjà existant.");
        }
    }
}