package service;

import model.PublicationRating;
import utils.MyDatabse;

import java.sql.*;

public class PublicationRatingService {

    private final Connection con;

    public PublicationRatingService() {
        this.con = MyDatabse.getInstance().getCon();
    }

    // Ajouter ou mettre à jour une note (avec un objet PublicationRating)
    public void addOrUpdateRating(PublicationRating rating) throws SQLException {
        String queryCheck = "SELECT * FROM publication_rating WHERE user_id = ? AND publication_id = ?";
        PreparedStatement psCheck = con.prepareStatement(queryCheck);
        psCheck.setInt(1, rating.getUserId());
        psCheck.setInt(2, rating.getPublicationId());
        ResultSet rs = psCheck.executeQuery();

        if (rs.next()) { // Si une évaluation existe déjà
            String queryUpdate = "UPDATE publication_rating SET rating = ? WHERE user_id = ? AND publication_id = ?";
            PreparedStatement psUpdate = con.prepareStatement(queryUpdate);
            psUpdate.setInt(1, rating.getRating());
            psUpdate.setInt(2, rating.getUserId());
            psUpdate.setInt(3, rating.getPublicationId());
            psUpdate.executeUpdate();
        } else { // Ajouter une nouvelle évaluation
            String queryInsert = "INSERT INTO publication_rating (user_id, publication_id, rating) VALUES (?, ?, ?)";
            PreparedStatement psInsert = con.prepareStatement(queryInsert);
            psInsert.setInt(1, rating.getUserId());
            psInsert.setInt(2, rating.getPublicationId());
            psInsert.setInt(3, rating.getRating());
            psInsert.executeUpdate();
        }
    }

    // Ajouter ou mettre à jour une note avec des paramètres individuels
    public void addOrUpdateRating(int userId, int publicationId, int rating) throws SQLException {
        String queryCheck = "SELECT * FROM publication_rating WHERE user_id = ? AND publication_id = ?";
        try (PreparedStatement psCheck = con.prepareStatement(queryCheck)) {
            psCheck.setInt(1, userId);
            psCheck.setInt(2, publicationId);
            ResultSet rs = psCheck.executeQuery();

            if (rs.next()) { // Mise à jour si une note existe
                String queryUpdate = "UPDATE publication_rating SET rating = ? WHERE user_id = ? AND publication_id = ?";
                try (PreparedStatement psUpdate = con.prepareStatement(queryUpdate)) {
                    psUpdate.setInt(1, rating);
                    psUpdate.setInt(2, userId);
                    psUpdate.setInt(3, publicationId);
                    psUpdate.executeUpdate();
                }
            } else { // Sinon, insérer une nouvelle note
                String queryInsert = "INSERT INTO publication_rating (user_id, publication_id, rating) VALUES (?, ?, ?)";
                try (PreparedStatement psInsert = con.prepareStatement(queryInsert)) {
                    psInsert.setInt(1, userId);
                    psInsert.setInt(2, publicationId);
                    psInsert.setInt(3, rating);
                    psInsert.executeUpdate();
                }
            }
        }
    }

    // Récupérer la moyenne des notes pour une publication
    public float getAverageRating(int publicationId) throws SQLException {
        String query = "SELECT AVG(rating) AS avg_rating FROM publication_rating WHERE publication_id = ?";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, publicationId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getFloat("avg_rating");
            }
        }
        return 0.0f; // Retour par défaut
    }

    // Vérifier si un utilisateur a déjà noté une publication
    public boolean hasUserRated(int userId, int publicationId) throws SQLException {
        String query = "SELECT * FROM publication_rating WHERE user_id = ? AND publication_id = ?";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, userId);
            ps.setInt(2, publicationId);
            ResultSet rs = ps.executeQuery();

            return rs.next(); // Retourne vrai si une note est trouvée
        }
    }

    // Récupérer la note d'un utilisateur pour une publication
    public int getUserRating(int userId, int publicationId) throws SQLException {
        String query = "SELECT rating FROM publication_rating WHERE user_id = ? AND publication_id = ?";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, userId);
            ps.setInt(2, publicationId);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("rating"); // Retourne la note
            }
        }
        return 0; // Aucun rating trouvé
    }

    // Récupérer le nombre total de votes pour une publication
    public int getTotalVotes(int publicationId) throws SQLException {
        String query = "SELECT COUNT(*) AS total FROM publication_rating WHERE publication_id = ?";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, publicationId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
        }
        return 0; // Retourne 0 par défaut
    }
}