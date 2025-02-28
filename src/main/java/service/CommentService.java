package service;

import model.Comment;
import utils.MyDatabse;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentService {

    private final Connection con;

    public CommentService() {
        con = MyDatabse.getInstance().getCon();
    }

    // Ajout d'un commentaire
    public void addComment(Comment comment) {
        String query = "INSERT INTO comments (publication_id, user_id, content, date_posted) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setInt(1, comment.getPublicationId());
            stmt.setInt(2, comment.getUserId());
            stmt.setString(3, comment.getContent());
            stmt.setTimestamp(4, Timestamp.valueOf(comment.getDatePosted()));
            stmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // Récupérer les commentaires pour une publication
    public List<Comment> getCommentsByPublicationId(int publicationId) {
        String query = "SELECT * FROM comments WHERE publication_id = ?";
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
            ex.printStackTrace();
        }
        return comments;
    }
}