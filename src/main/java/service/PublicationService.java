package service;

import model.Publication;
import model.PublicationType;
import utils.DatabaseManager;
import utils.MyDatabse;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PublicationService implements IService<Publication> {

    private Connection con;

    public PublicationService() {
        con = MyDatabse.getInstance().getCon();
    }

    public int getTypeId(PublicationType type) {
        String sql = "SELECT id_type FROM publication_type WHERE nom_type = ?";
        try (PreparedStatement preparedStatement = con.prepareStatement(sql)) {
            preparedStatement.setString(1, type.name());
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                return rs.getInt("id_type");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de l'ID pour le type : " + type + " - " + e.getMessage());
        }
        throw new IllegalArgumentException("Type de publication inconnu : " + type);
    }

    @Override
    public void add(Publication publication) {
        if (publication == null) {
            throw new RuntimeException("La publication est invalide (null).");
        }

        // Vérifier si un titre existe déjà
        String checkExistenceQuery = "SELECT COUNT(*) FROM publication WHERE titre = ?";
        try (PreparedStatement checkStatement = DatabaseManager.getInstance().getCon().prepareStatement(checkExistenceQuery)) {
            checkStatement.setString(1, publication.getTitre());
            try (ResultSet rs = checkStatement.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new RuntimeException("Une publication avec ce titre existe déjà.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur SQL lors de la vérification du titre : " + e.getMessage());
        }

        // Récupérer l'ID du type de publication directement depuis la DB
        String getTypeIdQuery = "SELECT id_type FROM publication_type WHERE nom_type = ?";
        int typeId;
        try (PreparedStatement getTypeIdStatement = DatabaseManager.getInstance().getCon().prepareStatement(getTypeIdQuery)) {
            getTypeIdStatement.setString(1, publication.getType().name());
            try (ResultSet rs = getTypeIdStatement.executeQuery()) {
                if (rs.next()) {
                    typeId = rs.getInt("id_type");
                } else {
                    throw new RuntimeException("Le type de publication spécifié n'existe pas : " + publication.getType());
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur SQL lors de la récupération du type de publication : " + e.getMessage());
        }

        // Ajouter la publication dans la base
        String insertQuery = "INSERT INTO publication (titre, description, type_id, image, user_id) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement insertStatement = DatabaseManager.getInstance().getCon().prepareStatement(insertQuery)) {
            insertStatement.setString(1, publication.getTitre());
            insertStatement.setString(2, publication.getDescription());
            insertStatement.setInt(3, typeId); // Utilise l'ID du type récupéré à partir de la DB
            insertStatement.setBytes(4, publication.getImage());
            insertStatement.setInt(5, publication.getUserId());

            int rowsAffected = insertStatement.executeUpdate();
            if (rowsAffected <= 0) {
                throw new RuntimeException("Erreur lors de la création de la publication.");
            }
            System.out.println("Publication ajoutée avec succès !");
        } catch (SQLException e) {
            throw new RuntimeException("Erreur SQL lors de l'insertion : " + e.getMessage());
        }
    }
    @Override
    public void update(Publication publication) {
        // Vérification initiale des paramètres
        if (publication == null) {
            System.err.println("Erreur : La publication à mettre à jour ne peut pas être null.");
            return;
        }
        if (publication.getId() <= 0) {
            System.err.println("Erreur : L'ID de la publication est invalide.");
            return;
        }

        String sql = "UPDATE publication SET titre = ?, description = ?, type_id = ?, image = ?, user_id = ? WHERE id = ?";

        try (PreparedStatement preparedStatement = con.prepareStatement(sql)) {
            // Préparer les paramètres de la requête
            preparedStatement.setString(1, publication.getTitre());
            preparedStatement.setString(2, publication.getDescription());

            // Conversion dynamique du type en ID
            int typeId = getTypeId(publication.getType());
            if (typeId <= 0) {
                System.err.println("Erreur : Type de publication invalide ou non trouvé.");
                return;
            }
            preparedStatement.setInt(3, typeId);

            // Gestion de l'image (peut être null)
            preparedStatement.setBytes(4, publication.getImage());

            // ID de l'utilisateur affecté
            preparedStatement.setInt(5, publication.getUserId());

            // ID de la publication (condition WHERE)
            preparedStatement.setInt(6, publication.getId());

            // Exécution de la mise à jour
            int rowsUpdated = preparedStatement.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Mise à jour réussie : La publication avec ID " + publication.getId() + " a été mise à jour.");
            } else {
                System.err.println("Aucune publication mise à jour. Vérifiez que l'ID existe dans la base de données.");
            }

        } catch (SQLException e) {
            // Gestion des erreurs SQL détaillée
            System.err.println("Erreur SQL lors de la mise à jour de la publication : " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }
    @Override
    public void delete(int id) {
        String sql = "DELETE FROM publication WHERE id = ?";
        try (PreparedStatement preparedStatement = con.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de la publication : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public List<Publication> display() {
        List<Publication> publications = new ArrayList<>();
        String query = """
            SELECT p.id, p.titre, p.description, p.image, p.user_id, p.date_creation, pt.nom_type
            FROM publication p
            JOIN publication_type pt ON p.type_id = pt.id_type
        """;

        try (Statement statement = con.createStatement();
             ResultSet rs = statement.executeQuery(query)) {
            while (rs.next()) {
                Publication publication = new Publication();
                publication.setId(rs.getInt("id"));
                publication.setTitre(rs.getString("titre"));
                publication.setDescription(rs.getString("description"));
                publication.setType(PublicationType.valueOf(rs.getString("nom_type")));
                publication.setImage(rs.getBytes("image"));
                publication.setUserId(rs.getInt("user_id"));
                publication.setDateCreation(rs.getTimestamp("date_creation").toLocalDateTime());
                publications.add(publication);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'affichage des publications : " + e.getMessage());
            e.printStackTrace();
        }
        return publications;
    }

    public List<Publication> findByUserId(Long userId) {
        List<Publication> publications = new ArrayList<>();
        String query = "SELECT p.id, p.titre, p.description, p.image, p.user_id, p.date_creation, pt.nom_type " +
                "FROM publication p " +
                "JOIN publication_type pt ON p.type_id = pt.id_type " +
                "WHERE p.user_id = ?";
        try (PreparedStatement preparedStatement = con.prepareStatement(query)) {
            preparedStatement.setLong(1, userId);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    Publication publication = new Publication();
                    publication.setId(rs.getInt("id"));
                    publication.setTitre(rs.getString("titre"));
                    publication.setDescription(rs.getString("description"));
                    String typeName = rs.getString("nom_type").toUpperCase(); // Convertir en majuscules
                    publication.setType(PublicationType.valueOf(typeName));
                    publication.setImage(rs.getBytes("image"));
                    publication.setUserId(rs.getInt("user_id"));
                    publication.setDateCreation(rs.getTimestamp("date_creation").toLocalDateTime());
                    publications.add(publication);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche des publications par utilisateur : " + e.getMessage());
            e.printStackTrace();
        }
        return publications;
    }

}