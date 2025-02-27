package model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class User {

    private int id_user;
    private String nom_user;
    private String prenom_user;
    private int age_user;
    private Role role_user;
    private String mail_user;
    private String password_user;
    private String tele_user;

    private final StringProperty fullNameProperty = new SimpleStringProperty();

    public User() {}

    public User(int id_user, String nom_user, String prenom_user, int age_user, Role role_user, String mail_user, String password_user, String tele_user) {
        this.id_user = id_user;
        this.nom_user = nom_user;
        this.prenom_user = prenom_user;
        this.age_user = age_user;
        this.role_user = role_user;
        this.mail_user = mail_user;
        this.password_user = password_user;
        this.tele_user = tele_user;
        this.fullNameProperty.set(nom_user + " " + prenom_user);
    }

    public User(String nom_user, String prenom_user, int age_user, Role role_user, String mail_user, String password_user, String tele_user) {
        this.nom_user = nom_user;
        this.prenom_user = prenom_user;
        this.age_user = age_user;
        this.role_user = role_user;
        this.mail_user = mail_user;
        this.password_user = password_user;
        this.tele_user = tele_user;
        this.fullNameProperty.set(nom_user + " " + prenom_user);
    }

    public int getId_user() {
        return id_user;
    }

    public void setId_user(int id_user) {
        this.id_user = id_user;
    }

    public String getNom_user() {
        return nom_user;
    }

    public void setNom_user(String nom_user) {
        this.nom_user = nom_user;
        updateFullName();
    }

    public String getPrenom_user() {
        return prenom_user;
    }

    public void setPrenom_user(String prenom_user) {
        this.prenom_user = prenom_user;
        updateFullName();
    }
    public boolean isAdmin() {
        return this.role_user == Role.ADMIN;
    }

    public boolean isOrganisateur() {
        return this.role_user == Role.ORGANISATEUR;
    }
    public boolean isParticipant() {
        return this.role_user == Role.PARTICIPANT;
    }

    public int getAge_user() {
        return age_user;
    }

    public void setAge_user(int age_user) {
        this.age_user = age_user;
    }

    public Role getRole_user() {
        return role_user;
    }

    public void setRole_user(Role role_user) {
        this.role_user = role_user;
    }

    public String getMail_user() {
        return mail_user;
    }

    public void setMail_user(String mail_user) {
        this.mail_user = mail_user;
    }

    public String getPassword_user() {
        return password_user;
    }

    public void setPassword_user(String password_user) {
        this.password_user = password_user;
    }

    public String getTele_user() {
        return tele_user;
    }

    public void setTele_user(String tele_user) {
        this.tele_user = tele_user;
    }

    public StringProperty fullNameProperty() {
        return fullNameProperty;
    }

    public String getFullName() {
        return nom_user + " " + prenom_user;
    }

    public void setFullName(String fullName) {
        this.fullNameProperty.set(fullName);
    }

    private void updateFullName() {
        this.fullNameProperty.set(nom_user + " " + prenom_user);
    }

    @Override
    public String toString() {
        return "User{" +
                "id_user=" + id_user +
                ", nom_user='" + nom_user + '\'' +
                ", prenom_user='" + prenom_user + '\'' +
                ", age_user=" + age_user +
                ", role_user=" + role_user +
                ", mail_user='" + mail_user + '\'' +
                ", password_user='" + password_user + '\'' +
                ", tele_user='" + tele_user + '\'' +
                '}';
    }


}