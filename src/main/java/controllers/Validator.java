package controllers;

import model.Role;

public class Validator {

    // Validation du nom (ne peut pas être vide et ne doit pas dépasser 100 caractères)
    public static boolean validateName(String name) {
        return name != null && !name.trim().isEmpty() && name.length() <= 100;
    }

    // Validation du prénom (ne peut pas être vide et ne doit pas dépasser 100 caractères)
    public static boolean validatePrenom(String prenom) {
        return prenom != null && !prenom.trim().isEmpty() && prenom.length() <= 100;
    }

    // Validation de l'email
    public static boolean validateEmail(String email) {
        String regex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email != null && email.matches(regex);
    }

    // Validation du numéro de téléphone (8 chiffres)
    public static boolean validatePhone(String phone) {
        return phone != null && phone.matches("\\d{8}"); // Vérifie que le téléphone contient 8 chiffres
    }

    // Validation de l'âge (doit être entre 18 et 100 ans)
    public static boolean validateAge(int age) {
        return age >= 18 && age <= 100;
    }

    // Validation du mot de passe (minimum 8 caractères, doit contenir des chiffres, des lettres majuscules et minuscules)
    public static boolean validatePassword(String password) {
        String regex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).{8,}$";
        return password != null && password.matches(regex);
    }

    // Validation du rôle (doit être un rôle valide)
    public static boolean validateRole(String role) {
        try {
            Role.valueOf(role); // Si le rôle n'est pas valide, cela lancera une exception
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
