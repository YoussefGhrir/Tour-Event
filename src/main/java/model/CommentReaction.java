package model;

public class CommentReaction {
    private int id;
    private int commentId;
    private int userId;
    private String reactionType;

    public CommentReaction() {}

    public CommentReaction(int commentId, int userId, String reactionType) {
        this.commentId = commentId;
        this.userId = userId;
        this.reactionType = reactionType;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCommentId() {
        return commentId;
    }

    public void setCommentId(int commentId) {
        this.commentId = commentId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getReactionType() {
        return reactionType;
    }

    public void setReactionType(String reactionType) {
        this.reactionType = reactionType;
    }
}