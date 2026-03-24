package hotel;

import hotel.model.*;
import hotel.model.Employee.Role;
import hotel.service.HotelService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/**
 * Hotel Reservation System -- Console UI
 * Hotel: Grand Metro Hotel, Main Market, Metro City
 * Capacity: 100 rooms | 20 employees | ~10 customers/day
 */
public class Main {

    private static final HotelService service = new HotelService();
    private static final Scanner scanner = new Scanner(System.in);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public static void main(String[] args) {
        printBanner();
        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readInt("Enter choice: ");
            switch (choice) {
                case 1  -> showRooms();
                case 2  -> makeReservation();
                case 3  -> performCheckIn();
                case 4  -> performCheckOut();
                case 5  -> cancelReservation();
                case 6  -> viewAllReservations();
                case 7  -> viewActiveReservations();
                case 8  -> viewEmployees();
                case 9  -> viewCustomers();
                case 10 -> occupancyReport();
                case 11 -> revenueReport();
                case 0  -> {
                    System.out.println("\nThank you for using Hotel Reservation System. Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
        scanner.close();
    }

    // ---------------------------------------------------------------- banner
    private static void printBanner() {
        System.out.println("========================================================");
        System.out.println("        GRAND METRO HOTEL - RESERVATION SYSTEM          ");
        System.out.println("        Main Market, Metro City  |  Est. 2010           ");
        System.out.println("      100 Rooms  |  20 Staff  |  Open 24x7              ");
        System.out.println("========================================================");
    }

    // ---------------------------------------------------------------- menus
    private static void printMainMenu() {
        System.out.println("\n------------------ MAIN MENU ------------------");
        System.out.println(" 1. View All Rooms / Available Rooms");
        System.out.println(" 2. Make a Reservation");
        System.out.println(" 3. Check-In Guest");
        System.out.println(" 4. Check-Out Guest");
        System.out.println(" 5. Cancel Reservation");
        System.out.println(" 6. View All Reservations");
        System.out.println(" 7. View Active Reservations");
        System.out.println(" 8. View Employees");
        System.out.println(" 9. View Registered Customers");
        System.out.println("10. Occupancy Report");
        System.out.println("11. Revenue Report");
        System.out.println(" 0. Exit");
        System.out.println("-----------------------------------------------");
    }

    // ---------------------------------------------------------------- room
    private static void showRooms() {
        System.out.println("\n--- ROOM LISTING ---");
        System.out.println("1. All Rooms");
        System.out.println("2. Available Rooms Only");
        System.out.println("3. Available by Type");
        int c = readInt("Choice: ");

        List<Room> list = switch (c) {
            case 1 -> service.getAllRooms();
            case 2 -> service.getAvailableRooms();
            case 3 -> {
                RoomType type = selectRoomType();
                yield service.getAvailableRoomsByType(type);
            }
            default -> { System.out.println("Invalid."); yield List.of(); }
        };

        if (list.isEmpty()) {
            System.out.println("No rooms found.");
            return;
        }
        list.forEach(System.out::println);
        System.out.println("Total: " + list.size() + " room(s)");
    }

    // ---------------------------------------------------------- reservation
    private static void makeReservation() {
        System.out.println("\n--- MAKE RESERVATION ---");

        System.out.print("Guest Name                    : ");
        String name  = scanner.nextLine().trim();
        System.out.print("Phone Number                  : ");
        String phone = scanner.nextLine().trim();
        System.out.print("ID Proof (Aadhaar/Passport/DL): ");
        String id    = scanner.nextLine().trim();

        Customer customer = new Customer(name, phone, id);

        // Show available rooms grouped by type
        System.out.println("\nAvailable Room Types:");
        for (RoomType type : RoomType.values()) {
            long count = service.getAvailableRoomsByType(type).size();
            System.out.printf("  %-7s -- %2d available @ Rs %.0f/night%n",
                    type, count, type.getPricePerNight());
        }

        int roomNo   = readInt("Enter Room Number        : ");
        LocalDate ci = readDate("Check-In  (dd-MM-yyyy)   : ");
        LocalDate co = readDate("Check-Out (dd-MM-yyyy)   : ");

        try {
            Reservation res = service.makeReservation(customer, roomNo, ci, co);
            System.out.println("\n[SUCCESS] Reservation confirmed!");
            System.out.println(res);
            System.out.printf("  Total payable: Rs %.0f%n", res.getTotalAmount());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // --------------------------------------------------------------- check-in
    private static void performCheckIn() {
        int id = readInt("\nEnter Reservation ID to Check-In: ");
        try {
            service.checkIn(id);
            System.out.println("[SUCCESS] Guest checked in for booking #" + id);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------- check-out
    private static void performCheckOut() {
        int id = readInt("\nEnter Reservation ID to Check-Out: ");
        try {
            service.getAllReservations().stream()
                   .filter(r -> r.getReservationId() == id)
                   .findFirst()
                   .ifPresent(r -> System.out.printf(
                       "Bill for Booking #%d: %d night(s) x Rs %.0f = Rs %.0f%n",
                       id, r.getNights(),
                       r.getRoom().getType().getPricePerNight(),
                       r.getTotalAmount()));

            service.checkOut(id);
            System.out.println("[SUCCESS] Guest checked out. Thank you for staying with us!");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ----------------------------------------------------------- cancellation
    private static void cancelReservation() {
        int id = readInt("\nEnter Reservation ID to Cancel: ");
        try {
            service.cancelReservation(id);
            System.out.println("[SUCCESS] Reservation #" + id + " has been cancelled.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------ list views
    private static void viewAllReservations() {
        List<Reservation> list = service.getAllReservations();
        System.out.println("\n--- ALL RESERVATIONS (" + list.size() + ") ---");
        if (list.isEmpty()) { System.out.println("No reservations found."); return; }
        list.forEach(System.out::println);
    }

    private static void viewActiveReservations() {
        List<Reservation> list = service.getActiveReservations();
        System.out.println("\n--- ACTIVE RESERVATIONS (" + list.size() + ") ---");
        if (list.isEmpty()) { System.out.println("No active reservations."); return; }
        list.forEach(System.out::println);
    }

    private static void viewEmployees() {
        System.out.println("\n--- EMPLOYEES ---");
        System.out.println("1. All Employees");
        System.out.println("2. By Role");
        int c = readInt("Choice: ");
        if (c == 1) {
            service.getAllEmployees().forEach(System.out::println);
        } else if (c == 2) {
            System.out.println("Roles: MANAGER, RECEPTIONIST, HOUSEKEEPING, SECURITY, CHEF");
            System.out.print("Enter role: ");
            String roleStr = scanner.nextLine().trim().toUpperCase();
            try {
                Role role = Role.valueOf(roleStr);
                service.getEmployeesByRole(role).forEach(System.out::println);
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid role.");
            }
        }
    }

    private static void viewCustomers() {
        List<Customer> list = service.getAllCustomers();
        System.out.println("\n--- REGISTERED CUSTOMERS (" + list.size() + ") ---");
        if (list.isEmpty()) { System.out.println("No customers registered yet."); return; }
        list.forEach(System.out::println);
    }

    // ---------------------------------------------------------------- reports
    private static void occupancyReport() {
        service.printOccupancyReport();
    }

    private static void revenueReport() {
        System.out.println("\n=== REVENUE REPORT ===");
        System.out.printf("Revenue from checked-out guests : Rs %.0f%n", service.totalRevenue());
        System.out.printf("Active bookings (pending value) : Rs %.0f%n",
            service.getActiveReservations().stream()
                   .mapToDouble(Reservation::getTotalAmount).sum());
    }

    // ---------------------------------------------------------------- helpers
    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private static LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return LocalDate.parse(scanner.nextLine().trim(), DATE_FMT);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Use dd-MM-yyyy (e.g., 15-03-2026).");
            }
        }
    }

    private static RoomType selectRoomType() {
        System.out.println("Room types: 1-SINGLE  2-DOUBLE  3-DELUXE  4-SUITE");
        int c = readInt("Choose type: ");
        return switch (c) {
            case 1 -> RoomType.SINGLE;
            case 2 -> RoomType.DOUBLE;
            case 3 -> RoomType.DELUXE;
            default -> RoomType.SUITE;
        };
    }
}
