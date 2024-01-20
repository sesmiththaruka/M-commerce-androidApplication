package lk.jiat.xpect.model;

import java.util.ArrayList;

public class Event {
    private int id;
    private String eventName;
    private String eventDate;
    private String eventTime;
    private String eventLocation;
    private String ticketPrice;

    public Event(int id, String eventName, String eventDate, String eventTime, String eventLocation, String ticketPrice) {
        this.id = id;
        this.eventName = eventName;
        this.eventDate = eventDate;
        this.eventTime = eventTime;
        this.eventLocation = eventLocation;
        this.ticketPrice = ticketPrice;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public String getEventTime() {
        return eventTime;
    }

    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
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

    public static ArrayList<Event> getSampleEventList(){
        ArrayList<Event> sampleEventList = new ArrayList<>();
        sampleEventList.add(new Event(1,"El casino","2023-12-12","17:00","hikka","5000"));
        sampleEventList.add(new Event(2,"happy sin","2023-5-12","17:00","unawatuna","2000"));
        sampleEventList.add(new Event(2,"hikka festival","2023-5-12","17:00","unawatuna","2000"));
        sampleEventList.add(new Event(2,"hikka festival","2023-5-12","17:00","unawatuna","2000"));
        sampleEventList.add(new Event(2,"hikka festival","2023-5-12","17:00","unawatuna","2000"));
        sampleEventList.add(new Event(2,"hikka festival","2023-5-12","17:00","unawatuna","2000"));
        sampleEventList.add(new Event(2,"hikka festival","2023-5-12","17:00","unawatuna","2000"));
  return sampleEventList;
    }
}
