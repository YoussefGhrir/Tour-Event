package service;

import model.Comment;
import model.CommentReaction;
import utils.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentService {

    private final Connection con;

    public CommentService() {
        con = DatabaseManager.getInstance().getCon(); // Utilisation de la connexion depuis DatabaseManager
    }

    // Ajout d'un commentaire
    public void addComment(Comment comment) {
        String query = "INSERT INTO comment (publication_id, user_id, content, date_posted) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setInt(1, comment.getPublicationId());
            stmt.setInt(2, comment.getUserId());
            stmt.setString(3, comment.getContent());
            stmt.setTimestamp(4, Timestamp.valueOf(comment.getDatePosted()));
            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.err.println("Erreur lors de l'insertion du commentaire : " + ex.getMessage());
        }
    }

    // Récupérer les commentaires d'une publication
    public List<Comment> getCommentsByPublicationId(int publicationId) {
        String query = "SELECT * FROM comment WHERE publication_id = ? ORDER BY date_posted ASC";
        List<Comment> comments = new ArrayList<>();
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setInt(1, publicationId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Comment comment = new Comment();
                comment.setId(rs.getInt("id"));
                comment.setPublicationId(rs.getInt("publication_id"));
                comment.setUserId(rs.getInt("user_id"));
                comment.setContent(rs.getString("content"));
                comment.setDatePosted(rs.getTimestamp("date_posted").toLocalDateTime());
                comments.add(comment);
            }
        } catch (SQLException ex) {
            System.err.println("Erreur lors de la récupération des commentaires : " + ex.getMessage());
        }
        return comments;
    }

    // Mise à jour d'un commentaire
    public void updateComment(Comment comment) {
        String query = "UPDATE comment SET content = ? WHERE id = ?";
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, comment.getContent());
            stmt.setInt(2, comment.getId());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.err.println("Erreur lors de la mise à jour du commentaire : " + ex.getMessage());
        }
    }

    // Suppression d'un commentaire
    public void deleteComment(int commentId) {
        String query = "DELETE FROM comment WHERE id = ?";
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setInt(1, commentId);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.err.println("Erreur lors de la suppression du commentaire : " + ex.getMessage());
        }
    }
    public void addReaction(int commentId, int userId, String reactionType) {
        try {
            // Ajouter une réaction ou mettre à jour si elle existe déjà
            String query = "INSERT INTO comment_reaction (comment_id, user_id, reaction_type) " +
                    "VALUES (?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE reaction_type = ?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, commentId);               // Identifiant du commentaire
            ps.setInt(2, userId);                  // Identifiant de l'utilisateur
            ps.setString(3, reactionType);         // Type de réaction initiale
            ps.setString(4, reactionType);         // Nouvelle réaction si elle existe déjà
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout ou mise à jour de la réaction : " + e.getMessage());
        }
    }
    public Map<String, Integer> getReactionsByCommentId(int commentId) {
        Map<String, Integer> reactions = new HashMap<>();
        try {
            String query = "SELECT reaction_type, COUNT(*) as count FROM comment_reaction WHERE comment_id = ? GROUP BY reaction_type";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, commentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                reactions.put(rs.getString("reaction_type"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des réactions : " + e.getMessage());
        }
        return reactions;
    }
    public List<String> getUsersReacted(int commentId) {
        String query = "SELECT user_id, reaction_type FROM comment_reaction WHERE comment_id = ?";
        List<String> users = new ArrayList<>();
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setInt(1, commentId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String userReaction = "User ID: " + rs.getInt("user_id") + " (" + rs.getString("reaction_type") + ")";
                users.add(userReaction);
            }
        } catch (SQLException ex) {
            System.err.println("Erreur lors de la récupération des utilisateurs ayant réagi : " + ex.getMessage());
        }
        return users;
    }
    public List<CommentReaction> getReactionsWithUsers(int commentId) {
        List<CommentReaction> reactions = new ArrayList<>();
        try {
            // Correction de la table 'users' en 'user'
            String query = "SELECT cr.reaction_type, cr.user_id, u.nom_user, u.prenom_user FROM comment_reaction cr " +
                    "JOIN user u ON cr.user_id = u.id_user WHERE cr.comment_id = ?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, commentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                CommentReaction reaction = new CommentReaction(
                        commentId,
                        rs.getInt("user_id"),
                        rs.getString("reaction_type")
                );
                reactions.add(reaction);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des réactions avec utilisateurs : " + e.getMessage());
        }
        return reactions;
    }
}