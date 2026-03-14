package hotel.service;

import hotel.model.*;
import hotel.model.Employee.Role;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Core service layer — manages rooms, employees, customers and reservations
 * for a 100-room hotel with 20 employees at the main market, metro city.
 */
public class HotelService {

    // ------------------------------------------------------------------ data
    private final List<Room>        rooms       = new ArrayList<>();
    private final List<Employee>    employees   = new ArrayList<>();
    private final List<Customer>    customers   = new ArrayList<>();
    private final List<Reservation> reservations = new ArrayList<>();

    // ------------------------------------------------------------------ init
    public HotelService() {
        initRooms();
        initEmployees();
    }

    /** Populate 100 rooms across four categories. */
    private void initRooms() {
        // Rooms 101-140  : Single  (40)
        for (int i = 101; i <= 140; i++) rooms.add(new Room(i, RoomType.SINGLE));
        // Rooms 201-240  : Double  (40)
        for (int i = 201; i <= 240; i++) rooms.add(new Room(i, RoomType.DOUBLE));
        // Rooms 301-315  : Deluxe  (15)
        for (int i = 301; i <= 315; i++) rooms.add(new Room(i, RoomType.DELUXE));
        // Rooms 401-405  : Suite   (5)
        for (int i = 401; i <= 405; i++) rooms.add(new Room(i, RoomType.SUITE));
    }

    /** Seed 20 employees across roles. */
    private void initEmployees() {
        addEmployee("Rajesh Kumar",    Role.MANAGER,       75000);
        addEmployee("Priya Sharma",    Role.RECEPTIONIST,  35000);
        addEmployee("Amit Singh",      Role.RECEPTIONIST,  35000);
        addEmployee("Sunita Verma",    Role.RECEPTIONIST,  35000);
        addEmployee("Deepak Yadav",    Role.HOUSEKEEPING,  22000);
        addEmployee("Meena Devi",      Role.HOUSEKEEPING,  22000);
        addEmployee("Ravi Gupta",      Role.HOUSEKEEPING,  22000);
        addEmployee("Pooja Nair",      Role.HOUSEKEEPING,  22000);
        addEmployee("Suresh Patel",    Role.HOUSEKEEPING,  22000);
        addEmployee("Kavita Mishra",   Role.HOUSEKEEPING,  22000);
        addEmployee("Mohan Tiwari",    Role.SECURITY,      28000);
        addEmployee("Ramesh Chauhan",  Role.SECURITY,      28000);
        addEmployee("Vinod Joshi",     Role.SECURITY,      28000);
        addEmployee("Anita Pandey",    Role.SECURITY,      28000);
        addEmployee("Sanjay Dubey",    Role.CHEF,          40000);
        addEmployee("Rekha Agarwal",   Role.CHEF,          38000);
        addEmployee("Vijay Saxena",    Role.CHEF,          38000);
        addEmployee("Nisha Rao",       Role.CHEF,          38000);
        addEmployee("Arun Trivedi",    Role.HOUSEKEEPING,  22000);
        addEmployee("Geeta Bajpai",    Role.RECEPTIONIST,  35000);
    }

    private void addEmployee(String name, Role role, double salary) {
        employees.add(new Employee(name, role, salary));
    }

    // ---------------------------------------------------------------- rooms
    public List<Room> getAllRooms() { return Collections.unmodifiableList(rooms); }

    public List<Room> getAvailableRooms() {
        return rooms.stream().filter(Room::isAvailable).collect(Collectors.toList());
    }

    public List<Room> getAvailableRoomsByType(RoomType type) {
        return rooms.stream()
                .filter(r -> r.isAvailable() && r.getType() == type)
                .collect(Collectors.toList());
    }

    public Optional<Room> findRoomByNumber(int number) {
        return rooms.stream().filter(r -> r.getRoomNumber() == number).findFirst();
    }

    // ----------------------------------------------------------- reservations
    public Reservation makeReservation(Customer customer, int roomNumber,
                                       LocalDate checkIn, LocalDate checkOut) {
        if (checkOut.isBefore(checkIn) || checkOut.isEqual(checkIn))
            throw new IllegalArgumentException("Check-out must be after check-in.");

        Room room = findRoomByNumber(roomNumber)
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomNumber));

        if (!room.isAvailable())
            throw new IllegalStateException("Room " + roomNumber + " is not available.");

        room.setAvailable(false);
        if (!customers.contains(customer)) customers.add(customer);

        Reservation res = new Reservation(customer, room, checkIn, checkOut);
        reservations.add(res);
        return res;
    }

    public void checkIn(int reservationId) {
        Reservation res = findReservation(reservationId);
        if (res.getStatus() != Reservation.Status.CONFIRMED)
            throw new IllegalStateException("Reservation is not in CONFIRMED state.");
        res.setStatus(Reservation.Status.CHECKED_IN);
    }

    public void checkOut(int reservationId) {
        Reservation res = findReservation(reservationId);
        if (res.getStatus() != Reservation.Status.CHECKED_IN)
            throw new IllegalStateException("Guest has not checked in yet.");
        res.setStatus(Reservation.Status.CHECKED_OUT);
        res.getRoom().setAvailable(true);
    }

    public void cancelReservation(int reservationId) {
        Reservation res = findReservation(reservationId);
        if (res.getStatus() == Reservation.Status.CHECKED_OUT)
            throw new IllegalStateException("Cannot cancel a completed reservation.");
        res.setStatus(Reservation.Status.CANCELLED);
        res.getRoom().setAvailable(true);
    }

    public List<Reservation> getAllReservations() {
        return Collections.unmodifiableList(reservations);
    }

    public List<Reservation> getActiveReservations() {
        return reservations.stream()
                .filter(r -> r.getStatus() == Reservation.Status.CONFIRMED
                          || r.getStatus() == Reservation.Status.CHECKED_IN)
                .collect(Collectors.toList());
    }

    private Reservation findReservation(int id) {
        return reservations.stream()
                .filter(r -> r.getReservationId() == id)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + id));
    }

    // ----------------------------------------------------------- employees
    public List<Employee> getAllEmployees() { return Collections.unmodifiableList(employees); }

    public List<Employee> getEmployeesByRole(Role role) {
        return employees.stream()
                .filter(e -> e.getRole() == role)
                .collect(Collectors.toList());
    }

    // ----------------------------------------------------------- customers
    public List<Customer> getAllCustomers() { return Collections.unmodifiableList(customers); }

    // ----------------------------------------------------------- reports
    public void printOccupancyReport() {
        long occupied = rooms.stream().filter(r -> !r.isAvailable()).count();
        System.out.printf("%n=== OCCUPANCY REPORT ===%n");
        System.out.printf("Total Rooms  : %d%n", rooms.size());
        System.out.printf("Occupied     : %d%n", occupied);
        System.out.printf("Available    : %d%n", rooms.size() - occupied);
        System.out.printf("Occupancy %%  : %.1f%%%n", (occupied * 100.0) / rooms.size());

        for (RoomType type : RoomType.values()) {
            long total = rooms.stream().filter(r -> r.getType() == type).count();
            long free  = rooms.stream().filter(r -> r.getType() == type && r.isAvailable()).count();
            System.out.printf("  %-7s : %2d total, %2d available%n", type, total, free);
        }
    }

    public double totalRevenue() {
        return reservations.stream()
                .filter(r -> r.getStatus() == Reservation.Status.CHECKED_OUT)
                .mapToDouble(Reservation::getTotalAmount)
                .sum();
    }
}
