package lk.jiat.xpect.dto;

public class UserDTO {
    private int id;
    private String name;
    public String bio;
    private String firebaseUserId;


    public UserDTO() {
    }

    public UserDTO(int id, String name, String bio, String firebaseUserId) {
        this.id = id;
        this.name = name;
        this.bio = bio;
        this.firebaseUserId = firebaseUserId;

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getFirebaseUserId() {
        return firebaseUserId;
    }

    public void setFirebaseUserId(String firebaseUserId) {
        this.firebaseUserId = firebaseUserId;
    }


}
