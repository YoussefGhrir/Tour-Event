package utils;

import model.User;

public class SessionManager {
    private static User currentUser;

    // Méthode pour définir l'utilisateur connecté
    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    // Méthode pour récupérer l'utilisateur connecté
    public static User getCurrentUser() {
        return currentUser;
    }

    // Méthode pour déconnecter l'utilisateur (vider la session)
    public static void clearSession() {
        currentUser = null;
    }
}