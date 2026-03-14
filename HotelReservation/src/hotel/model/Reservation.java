package hotel.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Reservation {
    public enum Status { CONFIRMED, CHECKED_IN, CHECKED_OUT, CANCELLED }

    private static int counter = 5000;

    private final int reservationId;
    private final Customer customer;
    private final Room room;
    private final LocalDate checkIn;
    private final LocalDate checkOut;
    private Status status;

    public Reservation(Customer customer, Room room, LocalDate checkIn, LocalDate checkOut) {
        this.reservationId = ++counter;
        this.customer = customer;
        this.room = room;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.status = Status.CONFIRMED;
    }

    public int getReservationId()   { return reservationId; }
    public Customer getCustomer()   { return customer; }
    public Room getRoom()           { return room; }
    public LocalDate getCheckIn()   { return checkIn; }
    public LocalDate getCheckOut()  { return checkOut; }
    public Status getStatus()       { return status; }
    public void setStatus(Status s) { this.status = s; }

    public long getNights() {
        return ChronoUnit.DAYS.between(checkIn, checkOut);
    }

    public double getTotalAmount() {
        return getNights() * room.getType().getPricePerNight();
    }

    @Override
    public String toString() {
        return String.format(
            "Booking #%d | %s | Room %d (%s) | %s → %s | %d night(s) | ₹%.0f | %s",
            reservationId, customer.getName(), room.getRoomNumber(),
            room.getType(), checkIn, checkOut, getNights(),
            getTotalAmount(), status);
    }
}
