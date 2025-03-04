package model;

import java.time.LocalDateTime;

public class Publication {

    private int id;
    private String titre;
    private String description;
    private PublicationType type;
    private byte[] image; // Stocker l'image sous forme binaire
    private Integer parkingId; // Remplacé par Integer pour permettre la gestion de null
    private int userId;
    private LocalDateTime dateCreation;

    // Constructeurs
    public Publication() {
        this.dateCreation = LocalDateTime.now();
    }

    public Publication(int id, String titre, String description, PublicationType type, byte[] image, Integer parkingId, int userId) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.type = type;
        this.image = image;
        this.parkingId = parkingId;
        this.userId = userId;
        this.dateCreation = LocalDateTime.now();
    }

    // Constructeur pour un post normal (tous les utilisateurs)
    public Publication(String titre, String description, byte[] image, int userId) {
        this.titre = titre;
        this.description = description;
        this.image = image;
        this.userId = userId;
        this.type = PublicationType.NORMAL; // Par défaut, un post normal
        this.dateCreation = LocalDateTime.now();
    }

    // Constructeur pour un post de type parking (Admin et Organisateur)
    public Publication(String titre, String description, byte[] image, int userId, Integer parkingId) {
        this(titre, description, image, userId); // Appel du constructeur de base
        this.parkingId = parkingId;
        this.type = PublicationType.PARKING; // Type de post : parking
    }

    // Constructeur pour un post de type événement (Organisateur uniquement)
    public Publication(String titre, String description, byte[] image, int userId, PublicationType type) {
        this(titre, description, image, userId); // Appel du constructeur de base
        if (type != PublicationType.EVENEMENT) {
            throw new IllegalArgumentException("Seuls les organisateurs peuvent créer des posts de type événement.");
        }
        this.type = type;
    }

    // Constructeur pour un post de type covoiturage (Participant uniquement)
    public Publication(String titre, String description, byte[] image, int userId, boolean isCovoiturage) {
        this(titre, description, image, userId); // Appel du constructeur de base
        if (!isCovoiturage) {
            throw new IllegalArgumentException("Les participants ne peuvent créer que des posts de type covoiturage ou normal.");
        }
        this.type = PublicationType.COVOITURAGE; // Type de post : covoiturage
    }
    private String userFullName;

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public PublicationType getType() {
        return type;
    }

    public void setType(PublicationType type) {
        this.type = type;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public Integer getParkingId() {
        return parkingId; // ID du parking associé à cette publication
    }

    public void setParkingId(Integer parkingId) {
        this.parkingId = parkingId; // Lien vers le parking
    }

    public int getUserId() {
        return userId; // ID de l'utilisateur qui a créé la publication
    }

    public void setUserId(int userId) {
        this.userId = userId; // Lien vers l'utilisateur
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }


}