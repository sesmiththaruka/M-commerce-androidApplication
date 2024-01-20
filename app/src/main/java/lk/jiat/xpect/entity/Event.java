package lk.jiat.xpect.entity;

import java.util.Date;

public class Event {
    private String name;
    private String description;
    private String date;
    private String time;
    private String eventUniqueId;
    private String userId;
    private int categoryId;
    private int typeId;

    public Event() {
    }

    public Event(String eventUniqueId, String userId, int categoryId, int typeId) {
        this.eventUniqueId = eventUniqueId;
        this.userId = userId;
        this.categoryId = categoryId;
        this.typeId = typeId;
    }

    public Event(String name, String description, String date, String time, String eventUniqueId, String userId, int categoryId, int typeId) {
        this.name = name;
        this.description = description;
        this.date = date;
        this.time = time;
        this.eventUniqueId = eventUniqueId;
        this.userId = userId;
        this.categoryId = categoryId;
        this.typeId = typeId;
    }



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
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

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }
}
