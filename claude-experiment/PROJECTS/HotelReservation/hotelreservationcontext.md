# Hotel Reservation System — Session Context

## What We Built

A Java console + Swing GUI application for a hotel with the following specs:
- **100 rooms** across 4 types
- **20 employees** across 5 roles
- **~10 customers/day** walk-in average
- **Location:** Main Market, Metro City
- **Hotel name:** Grand Metro Hotel

---

## Project Structure

```
HotelReservation/
  src/
    hotel/
      model/
        RoomType.java       — enum: SINGLE / DOUBLE / DELUXE / SUITE with prices
        Room.java           — room number, type, availability flag
        Customer.java       — auto-incremented ID, name, phone, ID proof
        Employee.java       — employee ID, name, role (enum), salary
        Reservation.java    — links customer + room + dates, computes nights & bill
      service/
        HotelService.java   — all business logic (no UI code here)
      ui/
        HotelApp.java       — Swing GUI (the colorful desktop app)
      Main.java             — original console/terminal UI (still works)
  out/                      — compiled .class files
  hotelreservationcontext.md
```

---

## Room Distribution (100 total)

| Rooms       | Type    | Count | Price/Night |
|-------------|---------|-------|-------------|
| 101 – 140   | SINGLE  | 40    | Rs 1,500    |
| 201 – 240   | DOUBLE  | 40    | Rs 2,500    |
| 301 – 315   | DELUXE  | 15    | Rs 4,000    |
| 401 – 405   | SUITE   | 5     | Rs 7,500    |

---

## Employee List (20 total, pre-seeded in HotelService)

| Role          | Count | Salary/month |
|---------------|-------|--------------|
| MANAGER       | 1     | Rs 75,000    |
| RECEPTIONIST  | 4     | Rs 35,000    |
| HOUSEKEEPING  | 7     | Rs 22,000    |
| SECURITY      | 4     | Rs 28,000    |
| CHEF          | 4     | Rs 38,000    |

---

## Key Classes & Responsibilities

### `RoomType.java`
Enum with `pricePerNight` and `description` fields.

### `Room.java`
Holds `roomNumber`, `RoomType`, and `isAvailable` boolean.
`setAvailable()` is called during booking / checkout / cancellation.

### `Customer.java`
Static counter starts at 1000 — each new customer gets `customerId = ++counter`.
Fields: name, phone, idProof.

### `Employee.java`
Static counter starts at 200. Inner enum `Role` defines the 5 roles.

### `Reservation.java`
Static counter starts at 5000 — booking IDs start at 5001.
Holds references to `Customer` and `Room`.
`getNights()` uses `ChronoUnit.DAYS.between(checkIn, checkOut)`.
`getTotalAmount()` = nights × room price.
Status enum: `CONFIRMED → CHECKED_IN → CHECKED_OUT` (or `CANCELLED` from any active state).

### `HotelService.java`
- `initRooms()` — populates 100 rooms on startup
- `initEmployees()` — seeds 20 named employees on startup
- `makeReservation()` — validates dates, checks availability, flips room to unavailable
- `checkIn()` — requires status == CONFIRMED
- `checkOut()` — requires status == CHECKED_IN, frees the room
- `cancelReservation()` — cannot cancel CHECKED_OUT; frees the room
- `printOccupancyReport()` — prints to console
- `totalRevenue()` — sums CHECKED_OUT reservations only

---

## How to Compile

```bash
cd HotelReservation
javac -encoding UTF-8 -d out src/hotel/model/*.java src/hotel/service/*.java src/hotel/ui/*.java src/hotel/Main.java
```

Or using find (works on bash/git-bash):
```bash
find src -name "*.java" | xargs javac -encoding UTF-8 -d out
```

---

## How to Run

**Swing GUI (recommended for employees):**
```bash
java -cp out hotel.ui.HotelApp
```

**Console UI (terminal fallback):**
```bash
java -cp out hotel.Main
```

Date format for both UIs: `dd-MM-yyyy` (e.g. `15-03-2026`)

---

## Swing GUI — Tab Layout

| Tab           | Who uses it            | What it does                                              |
|---------------|------------------------|-----------------------------------------------------------|
| Reservations  | Receptionist           | New booking form, check-in, check-out, cancel — all via buttons on a live table |
| Rooms         | Receptionist / Housekeeping / Security | Browse all 100 rooms, filter by type or availability. Green row = available, red = occupied |
| Employees     | Manager                | Staff directory filtered by role; mini stat cards show headcount per role |
| Reports       | Manager                | Live stat cards (occupied %, revenue), room-type breakdown, reservation status summary |

---

## UI Design Details (HotelApp.java)

- **Header:** Navy → purple gradient, hotel name in gold, live date badge
- **Tabs:** Each tab has its own accent color; selected tab glows
- **Buttons (`FlatButton`):** Gradient pill shape, hover-brightens, custom painted (no system L&F)
- **Tables:** Alternating white / indigo-tinted rows, custom navy header
- **Status badges:** Colored filled labels — CONFIRMED=blue, CHECKED IN=green, CHECKED OUT=grey, CANCELLED=red
- **Room rows:** Green-tinted = available, red-tinted = occupied
- **Role badges:** Outlined colored labels per role
- **Stat cards (Reports):** Gradient colored cards with large value text, live-updatable JLabels
- **Footer:** Dark charcoal bar with hotel tagline
- **`RoundBorder`:** Custom `AbstractBorder` for rounded outlines used throughout
- **No external libraries** — pure Java Swing + AWT

---

## Known Notes / Things to Watch

- The system is **in-memory only** — data resets every time the app restarts. To persist data, a file-based save (e.g. serialization or CSV) or a database (SQLite/MySQL) would need to be added.
- Compilation must use `-encoding UTF-8` on Windows to avoid character encoding errors.
- The console `Main.java` is still functional as a fallback but the Swing GUI (`HotelApp.java`) is the primary UI.
- Booking IDs start at 5001, Customer IDs at 1001, Employee IDs at 201.

---

## Possible Next Steps (not yet done)

- [ ] Save/load data to a file (JSON or CSV) so bookings survive restarts
- [ ] Search/filter bookings by guest name or date range in the Reservations tab
- [ ] Add a login screen (Manager vs Receptionist role-based access)
- [ ] Print invoice / receipt as a PDF or text file on checkout
- [ ] Connect to a database (JDBC + SQLite is easy to add with no server needed)
