package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDatabse {
    // Détails de connexion à la base de données
    private static final String URL = "jdbc:mysql://localhost:3306/devip_3";
    private static final String USERNAME = "root";
    private static final String PWD = "";

    private Connection con;

    // Singleton instance
    private static MyDatabse instance;

    // Constructeur privé pour empêcher l'instanciation externe
    public MyDatabse() {
        try {
            con = DriverManager.getConnection(URL, USERNAME, PWD);
            System.out.println("Connexion réussie à la base de données !");
        } catch (SQLException e) {
            System.err.println("Erreur de connexion à la base de données : " + e.getMessage());
        }
    }

    // Méthode pour récupérer l'instance unique de MyDatabase (Singleton)
    public static MyDatabse getInstance() {
        if (instance == null) {
            instance = new MyDatabse();
        }
        return instance;
    }

    // Retourne la connexion
    public Connection getCon() {
        return con;
    }

    // Méthode pour fermer la connexion à la base de données
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
