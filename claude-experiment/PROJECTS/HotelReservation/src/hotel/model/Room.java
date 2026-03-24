package hotel.model;

public class Room {
    private final int roomNumber;
    private final RoomType type;
    private boolean isAvailable;

    public Room(int roomNumber, RoomType type) {
        this.roomNumber = roomNumber;
        this.type = type;
        this.isAvailable = true;
    }

    public int getRoomNumber()       { return roomNumber; }
    public RoomType getType()        { return type; }
    public boolean isAvailable()     { return isAvailable; }
    public void setAvailable(boolean available) { this.isAvailable = available; }

    @Override
    public String toString() {
        return String.format("Room %3d | %-7s | %-20s | ₹%.0f/night | %s",
                roomNumber, type.name(), type.getDescription(),
                type.getPricePerNight(), isAvailable ? "AVAILABLE" : "OCCUPIED");
    }
}
