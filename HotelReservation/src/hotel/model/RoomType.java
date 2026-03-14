package hotel.model;

public enum RoomType {
    SINGLE(1500, "Single Bed"),
    DOUBLE(2500, "Double Bed"),
    DELUXE(4000, "Deluxe Double Bed"),
    SUITE(7500, "Luxury Suite");

    private final double pricePerNight;
    private final String description;

    RoomType(double pricePerNight, String description) {
        this.pricePerNight = pricePerNight;
        this.description = description;
    }

    public double getPricePerNight() { return pricePerNight; }
    public String getDescription()   { return description; }
}
