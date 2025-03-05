package service;

import model.Publication;
import model.PublicationType;
import utils.MyDatabse;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PublicationService implements IService<Publication> {

    private final Connection con;

    public PublicationService() {
        // Connexion via Singleton
        this.con = MyDatabse.getInstance().getCon();
    }

    /**
     * Récupère l'ID du type de publication en fonction de son nom.
     */
    public int getTypeId(PublicationType type) {
        String sql = "SELECT id_type FROM publication_type WHERE nom_type = ?";
        try (PreparedStatement preparedStatement = con.prepareStatement(sql)) {
            preparedStatement.setString(1, type.name());
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_type");
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de l'ID pour le type : " + type + ": " + e.getMessage());
        }
        throw new IllegalArgumentException("Type de publication inconnu : " + type);
    }

    /**
     * Récupère toutes les publications sauf celles de l'utilisateur connecté.
     * @param currentUserId Identifiant de l'utilisateur connecté.
     */
    public List<Publication> getAllPublications(int currentUserId) {
        List<Publication> publications = new ArrayList<>();
        String query = """
            SELECT p.id, p.titre, p.description, p.image, p.user_id, p.date_creation, pt.nom_type
            FROM publication p
            JOIN publication_type pt ON p.type_id = pt.id_type
            WHERE p.user_id != ?
        """;

        try (PreparedStatement preparedStatement = con.prepareStatement(query)) {
            preparedStatement.setInt(1, currentUserId);

            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    publications.add(mapResultSetToPublication(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des publications (hors utilisateur connecté) : " + e.getMessage());
        }

        return publications;
    }

    /**
     * Mappe un objet `ResultSet` à une entité `Publication`.
     */
    private Publication mapResultSetToPublication(ResultSet rs) throws SQLException {
        Publication publication = new Publication();
        publication.setId(rs.getInt("id"));
        publication.setTitre(rs.getString("titre"));
        publication.setDescription(rs.getString("description"));
        publication.setType(PublicationType.valueOf(rs.getString("nom_type").toUpperCase())); // Convertit le type en enum
        publication.setImage(rs.getBytes("image"));
        publication.setUserId(rs.getInt("user_id"));
        publication.setDateCreation(rs.getTimestamp("date_creation").toLocalDateTime());
        return publication;
    }

    @Override
    public void add(Publication publication) {
        if (publication == null) {
            throw new IllegalArgumentException("La publication est invalide (null).");
        }

        String checkExistenceQuery = "SELECT COUNT(*) FROM publication WHERE titre = ?";
        String insertQuery = "INSERT INTO publication (titre, description, type_id, image, user_id) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement checkStatement = con.prepareStatement(checkExistenceQuery);
             PreparedStatement insertStatement = con.prepareStatement(insertQuery)) {

            // Vérifier si une publication existe déjà avec le même titre
            checkStatement.setString(1, publication.getTitre());
            try (ResultSet rs = checkStatement.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new IllegalStateException("Une publication avec ce titre existe déjà !");
                }
            }

            // Ajouter une nouvelle publication
            insertStatement.setString(1, publication.getTitre());
            insertStatement.setString(2, publication.getDescription());
            insertStatement.setInt(3, getTypeId(publication.getType()));
            insertStatement.setBytes(4, publication.getImage());
            insertStatement.setInt(5, publication.getUserId());

            int rowsAffected = insertStatement.executeUpdate();
            if (rowsAffected <= 0) {
                throw new SQLException("Aucune ligne affectée lors de l'ajout de la publication.");
            }
            System.out.println("Publication ajoutée avec succès !");
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout de la publication : " + e.getMessage());
        }
    }

    @Override
    public void update(Publication publication) {
        if (publication == null || publication.getId() <= 0) {
            throw new IllegalArgumentException("Publication invalide pour la mise à jour.");
        }

        String sql = "UPDATE publication SET titre = ?, description = ?, type_id = ?, image = ?, user_id = ? WHERE id = ?";

        try (PreparedStatement preparedStatement = con.prepareStatement(sql)) {
            preparedStatement.setString(1, publication.getTitre());
            preparedStatement.setString(2, publication.getDescription());
            preparedStatement.setInt(3, getTypeId(publication.getType()));
            preparedStatement.setBytes(4, publication.getImage());
            preparedStatement.setInt(5, publication.getUserId());
            preparedStatement.setInt(6, publication.getId());

            int rowsUpdated = preparedStatement.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Publication mise à jour avec succès !");
            } else {
                System.err.println("Aucune modification. La publication n'existe peut-être pas.");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour : " + e.getMessage());
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM publication WHERE id = ?";

        try (PreparedStatement preparedStatement = con.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);

            int rowsDeleted = preparedStatement.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("Publication supprimée avec succès !");
            } else {
                System.err.println("Aucune publication trouvée avec l'ID : " + id);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression : " + e.getMessage());
        }
    }

    @Override
    public List<Publication> display() {
        return getAllPublications(0); // Par défaut : affichage avec exclusion d'aucun utilisateur
    }

    /**
     * Récupère les publications liées à un utilisateur donné.
     * @param userId Identifiant de l'utilisateur.
     * @return Liste des publications de cet utilisateur.
     */
    public List<Publication> findByUserId(int userId) { // Correction du type de paramètre en int
        List<Publication> publications = new ArrayList<>();
        String query = """
        SELECT p.id, p.titre, p.description, p.image, p.user_id, p.date_creation, pt.nom_type
        FROM publication p
        JOIN publication_type pt ON p.type_id = pt.id_type
        WHERE p.user_id = ?
    """;

        try (PreparedStatement preparedStatement = con.prepareStatement(query)) {
            preparedStatement.setInt(1, userId); // Utilise l'ID utilisateur en tant qu'int.

            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    publications.add(mapResultSetToPublication(rs)); // Map les résultats en objets Publication.
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des publications par utilisateur : " + e.getMessage());
        }

        return publications;
    }
    public List<Publication> filterByType(int typeId) {
        List<Publication> publications = new ArrayList<>();
        String sql = """
        SELECT p.id, p.titre, p.description, p.image, p.user_id, p.date_creation, pt.nom_type
        FROM publication p
        JOIN publication_type pt ON p.type_id = pt.id_type
        WHERE p.type_id = ?
    """;

        try (PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setInt(1, typeId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    publications.add(mapResultSetToPublication(rs));
                }
            }
        } catch (SQLException ex) {
            System.err.println("Erreur lors du filtrage des publications par type : " + ex.getMessage());
        }
        return publications;
    }

    public List<Publication> searchByTitle(String title) {
        List<Publication> publications = new ArrayList<>();
        String sql = """
        SELECT p.id, p.titre, p.description, p.image, p.user_id, p.date_creation, pt.nom_type
        FROM publication p
        JOIN publication_type pt ON p.type_id = pt.id_type
        WHERE p.titre LIKE ?
    """;

        try (PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setString(1, "%" + title + "%");
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    publications.add(mapResultSetToPublication(rs));
                }
            }
        } catch (SQLException ex) {
            System.err.println("Erreur lors de la recherche des publications par titre : " + ex.getMessage());
        }
        return publications;
    }

    public List<Publication> sortByTitle(boolean ascending) {
        List<Publication> publications = new ArrayList<>();
        String sql = """
        SELECT p.id, p.titre, p.description, p.image, p.user_id, p.date_creation, pt.nom_type
        FROM publication p
        JOIN publication_type pt ON p.type_id = pt.id_type
        ORDER BY p.titre %s
    """.formatted(ascending ? "ASC" : "DESC");

        try (PreparedStatement statement = con.prepareStatement(sql)) {
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    publications.add(mapResultSetToPublication(rs));
                }
            }
        } catch (SQLException ex) {
            System.err.println("Erreur lors du tri des publications par titre : " + ex.getMessage());
        }
        return publications;
    }

    /**
     * Récupère tous les types de publication disponibles.
     * @return Une liste contenant les noms des types de publication.
     */
    public List<String> getAllPublicationTypes() {
        List<String> types = new ArrayList<>();
        String sql = "SELECT nom_type FROM publication_type";

        try (PreparedStatement statement = con.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                types.add(rs.getString("nom_type"));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des types de publications : " + e.getMessage());
        }
        return types;
    }
}