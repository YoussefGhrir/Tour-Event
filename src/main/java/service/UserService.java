package service;

import model.User;
import utils.MyDatabse;
import controllers.Validator;
import model.Role;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService implements IService<User> {

    private Connection con;

    public UserService() {
        con = MyDatabse.getInstance().getCon();
    }

    // Méthode pour vérifier l'unicité de l'email
    private boolean isEmailUnique(String email, int userId) {
        String query;
        if (userId == 0) { // Nouvel utilisateur
            query = "SELECT COUNT(*) FROM user WHERE mail_user = ?";
        } else { // Mise à jour
            query = "SELECT COUNT(*) FROM user WHERE mail_user = ? AND id_user != ?";
        }
        try (PreparedStatement preparedStatement = con.prepareStatement(query)) {
            preparedStatement.setString(1, email);
            if (userId != 0) {
                preparedStatement.setInt(2, userId);
            }
            ResultSet rs = preparedStatement.executeQuery();
            return rs.next() && rs.getInt(1) == 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification de l'email : " + e.getMessage());
            return false;
        }
    }

    // Méthode pour vérifier l'unicité du numéro de téléphone
    private boolean isPhoneUnique(String phone, int userId) {
        String query;
        if (userId == 0) {
            query = "SELECT COUNT(*) FROM user WHERE tele_user = ?";
        } else {
            query = "SELECT COUNT(*) FROM user WHERE tele_user = ? AND id_user != ?";
        }
        try (PreparedStatement preparedStatement = con.prepareStatement(query)) {
            preparedStatement.setString(1, phone);
            if (userId != 0) {
                preparedStatement.setInt(2, userId);
            }
            ResultSet rs = preparedStatement.executeQuery();
            return rs.next() && rs.getInt(1) == 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification du téléphone : " + e.getMessage());
            return false;
        }
    }

    // Méthode de validation générale avant l'ajout ou la mise à jour
    private boolean validateUser(User user) throws IllegalArgumentException {
        // Validation des différents champs de l'utilisateur
        if (!Validator.validateName(user.getNom_user())) {
            throw new IllegalArgumentException("Nom invalide. Le nom ne peut pas être vide et doit comporter moins de 100 caractères.");
        }
        if (!Validator.validatePrenom(user.getPrenom_user())) {
            throw new IllegalArgumentException("Prénom invalide. Le prénom ne peut pas être vide et doit comporter moins de 100 caractères.");
        }
        if (!Validator.validateAge(user.getAge_user())) {
            throw new IllegalArgumentException("Âge invalide. L'âge doit être entre 18 et 100 ans.");
        }
        if (!Validator.validateRole(user.getRole_user().toString())) {
            throw new IllegalArgumentException("Rôle invalide. Veuillez sélectionner un rôle valide.");
        }
        if (!Validator.validatePassword(user.getPassword_user())) {
            throw new IllegalArgumentException("Mot de passe invalide. Le mot de passe doit contenir au moins 8 caractères, incluant majuscules, minuscules, chiffres et caractères spéciaux.");
        }
        // Vérification de l'unicité de l'email et du téléphone

        if (!isEmailUnique(user.getMail_user(), user.getId_user())) {
            throw new IllegalArgumentException("Cet email est déjà utilisé par un autre utilisateur.");
        }
        if (!isPhoneUnique(user.getTele_user(), user.getId_user())) {
            throw new IllegalArgumentException("Ce numéro de téléphone est déjà utilisé par un autre utilisateur.");
        }


        return true;
    }

    // Nouvelle méthode pour l'ajout avec validation et retour de statut
    public boolean addWithValidation(User user) throws IllegalArgumentException {
        validateUser(user);

        String sql = "INSERT INTO `user` (`nom_user`, `prenom_user`, `age_user`, `role_user`, `mail_user`, `password_user`, `tele_user`) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            preparedStatement.setString(1, user.getNom_user());
            preparedStatement.setString(2, user.getPrenom_user());
            preparedStatement.setInt(3, user.getAge_user());
            preparedStatement.setString(4, user.getRole_user().toString());
            preparedStatement.setString(5, user.getMail_user());
            preparedStatement.setString(6, user.getPassword_user());
            preparedStatement.setString(7, user.getTele_user());
            int result = preparedStatement.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            throw new IllegalArgumentException("Erreur lors de l'ajout de l'utilisateur : " + e.getMessage());
        }
    }

    @Override
    public void add(User user) {
        // Validation avant l'ajout
        try {
            validateUser(user);

            String sql = "INSERT INTO `user` (`nom_user`, `prenom_user`, `age_user`, `role_user`, `mail_user`, `password_user`, `tele_user`) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            preparedStatement.setString(1, user.getNom_user());
            preparedStatement.setString(2, user.getPrenom_user());
            preparedStatement.setInt(3, user.getAge_user());
            preparedStatement.setString(4, user.getRole_user().toString());
            preparedStatement.setString(5, user.getMail_user());
            preparedStatement.setString(6, user.getPassword_user());
            preparedStatement.setString(7, user.getTele_user());
            preparedStatement.executeUpdate();
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout de l'utilisateur : " + e.getMessage());
        }
    }

    @Override
    public void update(User user) {
        try {
            validateUser(user);

            String sql = "UPDATE user SET nom_user = ?, prenom_user = ?, age_user = ?, role_user = ?, mail_user = ?, password_user = ?, tele_user = ? WHERE id_user = ?";
            try (PreparedStatement preparedStatement = con.prepareStatement(sql)) {
                preparedStatement.setString(1, user.getNom_user());
                preparedStatement.setString(2, user.getPrenom_user());
                preparedStatement.setInt(3, user.getAge_user());
                preparedStatement.setString(4, user.getRole_user().toString());
                preparedStatement.setString(5, user.getMail_user());
                preparedStatement.setString(6, user.getPassword_user());
                preparedStatement.setString(7, user.getTele_user());
                preparedStatement.setInt(8, user.getId_user());
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour de l'utilisateur : " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }


    @Override
    public void delete(int id) {
        String sql = "DELETE FROM `user` WHERE `id_user` = ?";

        try {
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            preparedStatement.setInt(1, id);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de l'utilisateur : " + e.getMessage());
        }
    }

    @Override
    public List<User> display() {
        String query = "SELECT * FROM `user`";
        List<User> users = new ArrayList<>();

        try {
            Statement statement = con.createStatement();
            ResultSet rs = statement.executeQuery(query);
            while (rs.next()) {
                User user = new User();
                user.setId_user(rs.getInt("id_user"));
                user.setNom_user(rs.getString("nom_user"));
                user.setPrenom_user(rs.getString("prenom_user"));
                user.setAge_user(rs.getInt("age_user"));
                user.setRole_user(Role.valueOf(rs.getString("role_user")));
                user.setMail_user(rs.getString("mail_user"));
                user.setPassword_user(rs.getString("password_user"));
                user.setTele_user(rs.getString("tele_user"));
                users.add(user);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'affichage des utilisateurs : " + e.getMessage());
        }

        return users;
    }

    public User findByEmail(String email) {
        String query = "SELECT * FROM `user` WHERE `mail_user` = ?";
        try (PreparedStatement preparedStatement = con.prepareStatement(query)) {
            preparedStatement.setString(1, email);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setId_user(rs.getInt("id_user"));
                user.setNom_user(rs.getString("nom_user"));
                user.setPrenom_user(rs.getString("prenom_user"));
                user.setAge_user(rs.getInt("age_user"));
                user.setRole_user(Role.valueOf(rs.getString("role_user")));
                user.setMail_user(rs.getString("mail_user"));
                user.setPassword_user(rs.getString("password_user"));
                user.setTele_user(rs.getString("tele_user"));

                // Log pour vérifier l'utilisateur récupéré
                System.out.println("Utilisateur récupéré : " + user);
                return user;
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche de l'utilisateur : " + e.getMessage());
        }
        return null;
    }
    public User findById(int id) {
        System.out.println("Tentative de récupération de l'utilisateur avec l'ID : " + id);
        User user = null;
        String query = "SELECT * FROM user WHERE id_user = ?"; // Utilisez id_user ici

        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                user = new User();
                user.setId_user(rs.getInt("id_user"));
                user.setNom_user(rs.getString("nom_user"));
                user.setPrenom_user(rs.getString("prenom_user"));
                user.setAge_user(rs.getInt("age_user"));
                user.setRole_user(Role.valueOf(rs.getString("role_user")));
                user.setMail_user(rs.getString("mail_user"));
                user.setPassword_user(rs.getString("password_user"));
                user.setTele_user(rs.getString("tele_user"));
                System.out.println("Utilisateur récupéré : " + user.getFullName());
            } else {
                System.out.println("Aucun utilisateur trouvé avec l'ID : " + id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Erreur lors de la récupération de l'utilisateur : " + e.getMessage());
        }

        return user;
    }

}
