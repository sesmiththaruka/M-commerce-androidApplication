package lk.jiat.xpect.dto;

public class MyEventDTO {
    private String eventId;
    private String imagePath;
    private String eventName = "A";

    public MyEventDTO() {
    }

    public MyEventDTO(String eventId, String imagePath, String eventName) {
        this.eventId = eventId;
        this.imagePath = imagePath;
        this.eventName = eventName;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }
}
