package model;

public class PublicationRating {
    private int id; // ID unique pour chaque rating
    private int userId; // ID de l'utilisateur
    private int publicationId; // ID de la publication
    private int rating; // Valeur de l'évaluation (1 à 5)

    // Constructeurs
    public PublicationRating() {}

    public PublicationRating(int userId, int publicationId, int rating) {
        this.userId = userId;
        this.publicationId = publicationId;
        this.rating = rating;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getPublicationId() {
        return publicationId;
    }

    public void setPublicationId(int publicationId) {
        this.publicationId = publicationId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    @Override
    public String toString() {
        return "PublicationRating{" +
                "id=" + id +
                ", userId=" + userId +
                ", publicationId=" + publicationId +
                ", rating=" + rating +
                '}';
    }
}