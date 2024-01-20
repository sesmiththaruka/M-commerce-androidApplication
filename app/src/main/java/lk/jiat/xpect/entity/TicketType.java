package lk.jiat.xpect.entity;

public class TicketType {
    private String typeName;
    private double price;
    private int quantity;
    private String eventUniqueId;

    public TicketType() {
    }

    public TicketType(String typeName, double price, int quantity, String eventUniqueId) {
        this.typeName = typeName;
        this.price = price;
        this.quantity = quantity;
        this.eventUniqueId = eventUniqueId;
    }

    public TicketType(String typeName, double price, int quantity) {
        this.typeName = typeName;
        this.price = price;
        this.quantity = quantity;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getEventUniqueId() {
        return eventUniqueId;
    }

    public void setEventUniqueId(String eventUniqueId) {
        this.eventUniqueId = eventUniqueId;
    }

}
