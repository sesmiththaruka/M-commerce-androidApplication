package lk.jiat.xpect.dto;

public class FavoriteDTO {
    private String eventUniqueId;
    private String userId;
    private String eventName;
    private String eventDescription;
    private String eventTime;

    private String eventDate;
    private String categoryName;
    private String imageUrl="sfg";
    private String eventLocation = "galle";
    private String ticketPrice="1000";
    public FavoriteDTO() {
    }

    public FavoriteDTO(String eventUniqueId, String userId, String eventName, String eventDescription, String eventTime, String eventDate, String categoryName, String imageUrl, String eventLocation, String ticketPrice) {
        this.eventUniqueId = eventUniqueId;
        this.userId = userId;
        this.eventName = eventName;
        this.eventDescription = eventDescription;
        this.eventTime = eventTime;
        this.eventDate = eventDate;
        this.categoryName = categoryName;
        this.imageUrl = imageUrl;
        this.eventLocation = eventLocation;
        this.ticketPrice = ticketPrice;
    }

    public String getEventUniqueId() {
        return eventUniqueId;
    }

    public void setEventUniqueId(String eventUniqueId) {
        this.eventUniqueId = eventUniqueId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    public String getEventTime() {
        return eventTime;
    }

    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getEventLocation() {
        return eventLocation;
    }

    public void setEventLocation(String eventLocation) {
        this.eventLocation = eventLocation;
    }

    public String getTicketPrice() {
        return ticketPrice;
    }

    public void setTicketPrice(String ticketPrice) {
        this.ticketPrice = ticketPrice;
    }
}
