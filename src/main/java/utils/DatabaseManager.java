package utils;

import java.sql.*;

public class DatabaseManager {
    // Détails de connexion à la base de données
    private static final String URL = "jdbc:mysql://localhost:3306/devip_3";
    private static final String USERNAME = "root";
    private static final String PWD = "";

    private Connection con;

    // Singleton instance
    private static DatabaseManager instance;

    // Constructeur privé
    private DatabaseManager() {
        try {
            con = DriverManager.getConnection(URL, USERNAME, PWD);
            System.out.println("Connexion réussie à la base de données !");
            createTables(); // Création automatique des tables
            insertSampleParkings(); // Insertion des parkings d'exemple
            insertSampleUsers(); // Insertion des utilisateurs d'exemple
        } catch (SQLException e) {
            System.err.println("Erreur de connexion à la base de données : " + e.getMessage());
        }
    }

    // Méthode pour récupérer l'instance unique
    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    // Retourne la connexion
    public Connection getCon() {
        return con;
    }

    // Création des tables si elles n'existent pas
    public void createTables() {
        try (Statement stmt = con.createStatement()) {

            // Création de la table User
            String createUserTable = """
            CREATE TABLE IF NOT EXISTS user (
                id_user INT PRIMARY KEY AUTO_INCREMENT,
                nom_user VARCHAR(100) NOT NULL,
                prenom_user VARCHAR(100) NOT NULL,
                age_user INT,
                role_user ENUM('ADMIN', 'ORGANISATEUR', 'PARTICIPANT') NOT NULL,
                mail_user VARCHAR(255) NOT NULL UNIQUE,
                password_user VARCHAR(255) NOT NULL,
                tele_user VARCHAR(20)
            );
            """;
            stmt.execute(createUserTable);

            // Création de la table PublicationType
            String createPublicationTypeTable = """
            CREATE TABLE IF NOT EXISTS publication_type (
                id_type INT AUTO_INCREMENT PRIMARY KEY,
                nom_type VARCHAR(50) UNIQUE NOT NULL
            );
            """;
            stmt.execute(createPublicationTypeTable);

            // Insertion des valeurs ENUM dans publication_type
            String[] types = {"NORMAL", "COVOITURAGE", "PARKING", "EVENEMENT"};
            for (String type : types) {
                stmt.executeUpdate("INSERT IGNORE INTO publication_type (nom_type) VALUES ('" + type + "')");
            }

            // Création de la table Parking
            String createParkingTable = """
            CREATE TABLE IF NOT EXISTS parking (
                idpark INT AUTO_INCREMENT PRIMARY KEY,
                nompark VARCHAR(50) NOT NULL
            );
            """;
            stmt.execute(createParkingTable);

            // Création de la table Publication
            String createPublicationTable = """
            CREATE TABLE IF NOT EXISTS publication (
                id INT AUTO_INCREMENT PRIMARY KEY,
                titre VARCHAR(100) NOT NULL,
                description TEXT NOT NULL,
                type_id INT NOT NULL,
                image LONGBLOB,
                parking_id INT,
                user_id INT NOT NULL,
                date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (type_id) REFERENCES publication_type(id_type) ON DELETE CASCADE,
                FOREIGN KEY (user_id) REFERENCES user(id_user) ON DELETE CASCADE,
                FOREIGN KEY (parking_id) REFERENCES parking(idpark) ON DELETE SET NULL
            );
            """;
            stmt.execute(createPublicationTable);

            // Création de la table Comment
            String createCommentTable = """
            CREATE TABLE IF NOT EXISTS comment (
                id INT AUTO_INCREMENT PRIMARY KEY,
                publication_id INT NOT NULL,
                user_id INT NOT NULL,
                content TEXT NOT NULL,
                date_posted TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (publication_id) REFERENCES publication(id) ON DELETE CASCADE,
                FOREIGN KEY (user_id) REFERENCES user(id_user) ON DELETE CASCADE
            );
            """;
            stmt.execute(createCommentTable);

            // Table pour les réactions
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS comment_reaction (" +
                            "id INT AUTO_INCREMENT PRIMARY KEY, " +
                            "comment_id INT NOT NULL, " +
                            "user_id INT NOT NULL, " +
                            "reaction_type VARCHAR(50) NOT NULL, " +
                            "UNIQUE (comment_id, user_id), " + // Contrainte d'unicité sur comment_id et user_id
                            "FOREIGN KEY (comment_id) REFERENCES comment(id) ON DELETE CASCADE, " + // Lien avec la table comment
                            "FOREIGN KEY (user_id) REFERENCES user(id_user) ON DELETE CASCADE" + // Lien avec la table user
                            ")"
            );

            System.out.println("Tables créées ou déjà existantes.");
        } catch (SQLException e) {
            System.err.println("Erreur lors de la création des tables : " + e.getMessage());
        }
    }

    // Insertion des parkings d'exemple
    private void insertSampleParkings() {
        String[] parkingNames = {"Parking A", "Parking B", "Parking C", "Parking D", "Parking E"};

        try (Statement stmt = con.createStatement()) {
            for (String name : parkingNames) {
                String checkParking = "SELECT COUNT(*) FROM parking WHERE nompark = '" + name + "'";
                ResultSet rs = stmt.executeQuery(checkParking);
                if (rs.next() && rs.getInt(1) == 0) {
                    String insertParking = "INSERT INTO parking (nompark) VALUES ('" + name + "')";
                    stmt.executeUpdate(insertParking);
                    System.out.println("Parking inséré : " + name);
                } else {
                    System.out.println("Parking déjà existant : " + name);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'insertion des parkings : " + e.getMessage());
        }
    }

    // Insertion des utilisateurs d'exemple
    private void insertSampleUsers() {
        try (Statement stmt = con.createStatement()) {
            String[] userInfos = {
                    "('Foulen', 'User1', 25, 'PARTICIPANT', 'foulen1@gmail.com', 'Qmar+2025', '08000001')",
                    "('Foulen', 'User2', 30, 'ORGANISATEUR', 'foulen2@gmail.com', 'Qmar+2025', '08000002')"
            };

            for (String info : userInfos) {
                String insertUser = "INSERT IGNORE INTO user (nom_user, prenom_user, age_user, role_user, mail_user, password_user, tele_user) VALUES " + info;
                stmt.executeUpdate(insertUser);
                System.out.println("Utilisateur inséré : " + info);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'insertion des utilisateurs : " + e.getMessage());
        }
    }

    // Méthode pour fermer la connexion proprement
    public void close() {
        try {
            if (con != null && !con.isClosed()) {
                con.close();
                System.out.println("Connexion fermée avec succès.");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la fermeture de la connexion : " + e.getMessage());
        }
    }
}