package model;

import java.time.LocalDateTime;

public class Comment {

    private int id;
    private int publicationId;
    private int userId;
    private String content;
    private LocalDateTime datePosted;
    private boolean isReported;
    private String reportReason; // Raison du signalement


    public Comment() {
        this.isReported = false; // Par défaut, le commentaire n’est pas signalé
        this.reportReason = "";
    }

    public Comment(int publicationId, int userId, String content) {
        this.publicationId = publicationId;
        this.userId = userId;
        this.content = content;
        this.datePosted = LocalDateTime.now();
        this.isReported = false;
        this.reportReason = "";
    }


    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPublicationId() {
        return publicationId;
    }

    public void setPublicationId(int publicationId) {
        this.publicationId = publicationId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getDatePosted() {
        return datePosted;
    }

    public void setDatePosted(LocalDateTime datePosted) {
        this.datePosted = datePosted;
    }
    public boolean isReported() {
        return isReported;
    }

    public void setReported(boolean reported) {
        isReported = reported;
    }

    public String getReportReason() {
        return reportReason;
    }

    public void setReportReason(String reportReason) {
        this.reportReason = reportReason;
    }

}