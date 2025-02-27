package model;


public class Parking {
    private int idPark; // ID du parking
    private String nomPark; // Nom du parking

    // Constructeur par défaut
    public Parking() {}

    // Constructeur avec paramètres
    public Parking(int idPark, String nomPark) {
        this.idPark = idPark;
        this.nomPark = nomPark;
    }

    // Getters et setters
    public int getIdPark() {
        return idPark;
    }

    public void setIdPark(int idPark) {
        this.idPark = idPark;
    }

    public String getNomPark() {
        return nomPark;
    }

    public void setNomPark(String nomPark) {
        this.nomPark = nomPark;
    }
}