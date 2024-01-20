package lk.jiat.xpect.entity;

public class User {
    private String userId;
    private String FCMToken;

    public User() {
    }

    public User(String userId, String FCMToken) {
        this.userId = userId;
        this.FCMToken = FCMToken;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFCMToken() {
        return FCMToken;
    }

    public void setFCMToken(String FCMToken) {
        this.FCMToken = FCMToken;
    }
}
