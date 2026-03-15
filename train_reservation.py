import json
import os
from datetime import datetime

DATA_FILE = "train_data.json"

# --- Data Initialization ---

TRAIN_NAMES = [
    "Rajdhani Express", "Shatabdi Express", "Duronto Express", "Garib Rath",
    "Jan Shatabdi", "Humsafar Express", "Tejas Express", "Vande Bharat",
    "Sampark Kranti", "Intercity Express"
]

ROUTES = [
    ("Delhi", "Mumbai"), ("Delhi", "Chennai"), ("Delhi", "Kolkata"),
    ("Mumbai", "Bengaluru"), ("Chennai", "Bengaluru"), ("Kolkata", "Patna"),
    ("Delhi", "Jaipur"), ("Mumbai", "Pune"), ("Bengaluru", "Hyderabad"),
    ("Chennai", "Hyderabad")
]

CLASSES = {
    "SL": {"name": "Sleeper", "fare_per_km": 0.5,  "seats": 72},
    "3A": {"name": "3rd AC",   "fare_per_km": 1.2,  "seats": 64},
    "2A": {"name": "2nd AC",   "fare_per_km": 1.8,  "seats": 46},
    "1A": {"name": "1st AC",   "fare_per_km": 3.0,  "seats": 18},
}

DISTANCES = {
    ("Delhi", "Mumbai"): 1384, ("Delhi", "Chennai"): 2194,
    ("Delhi", "Kolkata"): 1472, ("Mumbai", "Bengaluru"): 981,
    ("Chennai", "Bengaluru"): 346, ("Kolkata", "Patna"): 532,
    ("Delhi", "Jaipur"): 268, ("Mumbai", "Pune"): 149,
    ("Bengaluru", "Hyderabad"): 574, ("Chennai", "Hyderabad"): 626,
}


def get_distance(src, dst):
    return DISTANCES.get((src, dst)) or DISTANCES.get((dst, src)) or 500


def init_data():
    """Create train_data.json with 100 trains if it doesn't exist."""
    if os.path.exists(DATA_FILE):
        with open(DATA_FILE) as f:
            return json.load(f)

    trains = {}
    for i in range(1, 101):
        name_idx = (i - 1) % len(TRAIN_NAMES)
        route_idx = (i - 1) % len(ROUTES)
        src, dst = ROUTES[route_idx]
        dist = get_distance(src, dst)
        train_no = f"{10000 + i}"

        trains[train_no] = {
            "number": train_no,
            "name": f"{TRAIN_NAMES[name_idx]} {i:03d}",
            "source": src,
            "destination": dst,
            "distance_km": dist,
            "departure": f"{6 + (i % 16):02d}:00",
            "classes": {
                cls: {
                    "fare": round(dist * info["fare_per_km"], 2),
                    "total_seats": info["seats"],
                    "available_seats": info["seats"],
                }
                for cls, info in CLASSES.items()
            },
        }

    data = {"trains": trains, "bookings": {}, "next_pnr": 1000}
    save_data(data)
    return data


def save_data(data):
    with open(DATA_FILE, "w") as f:
        json.dump(data, f, indent=2)


# --- Display Helpers ---

def print_header(title):
    print("\n" + "=" * 60)
    print(f"  {title}")
    print("=" * 60)


def print_train(t):
    print(f"\n  Train No : {t['number']}")
    print(f"  Name     : {t['name']}")
    print(f"  Route    : {t['source']} → {t['destination']}  ({t['distance_km']} km)")
    print(f"  Departs  : {t['departure']}")
    print(f"  {'Class':<6}  {'Name':<10}  {'Fare (₹)':>10}  {'Available':>10}")
    print(f"  {'-'*44}")
    for cls, info in t["classes"].items():
        print(f"  {cls:<6}  {CLASSES[cls]['name']:<10}  {info['fare']:>10.2f}  {info['available_seats']:>10}")


# --- Features ---

def search_trains(data):
    print_header("Search Trains")
    src = input("  From (city): ").strip().title()
    dst = input("  To   (city): ").strip().title()

    results = [
        t for t in data["trains"].values()
        if t["source"].lower() == src.lower() and t["destination"].lower() == dst.lower()
    ]

    if not results:
        print(f"\n  No trains found from {src} to {dst}.")
        return

    print(f"\n  Found {len(results)} train(s):\n")
    for t in results:
        print_train(t)


def book_ticket(data):
    print_header("Book Ticket")
    train_no = input("  Enter Train Number: ").strip()

    if train_no not in data["trains"]:
        print("  Train not found.")
        return

    t = data["trains"][train_no]
    print_train(t)

    cls = input("\n  Choose class (SL/3A/2A/1A): ").strip().upper()
    if cls not in t["classes"]:
        print("  Invalid class.")
        return

    if t["classes"][cls]["available_seats"] == 0:
        print("  No seats available in this class.")
        return

    name = input("  Passenger Name: ").strip()
    age  = input("  Age: ").strip()

    pnr = str(data["next_pnr"])
    data["next_pnr"] += 1
    t["classes"][cls]["available_seats"] -= 1

    booking = {
        "pnr": pnr,
        "train_no": train_no,
        "train_name": t["name"],
        "source": t["source"],
        "destination": t["destination"],
        "departure": t["departure"],
        "class": cls,
        "class_name": CLASSES[cls]["name"],
        "fare": t["classes"][cls]["fare"],
        "passenger_name": name,
        "passenger_age": age,
        "booked_at": datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
        "status": "CONFIRMED",
    }
    data["bookings"][pnr] = booking
    save_data(data)

    print(f"\n  ✔ Booking Confirmed!")
    print(f"  PNR       : {pnr}")
    print(f"  Train     : {t['name']} ({train_no})")
    print(f"  Route     : {t['source']} → {t['destination']}")
    print(f"  Class     : {CLASSES[cls]['name']} ({cls})")
    print(f"  Fare      : ₹{booking['fare']:.2f}")
    print(f"  Passenger : {name}, Age {age}")


def check_pnr(data):
    print_header("PNR Status")
    pnr = input("  Enter PNR Number: ").strip()

    b = data["bookings"].get(pnr)
    if not b:
        print("  PNR not found.")
        return

    print(f"\n  PNR       : {b['pnr']}")
    print(f"  Status    : {b['status']}")
    print(f"  Train     : {b['train_name']} ({b['train_no']})")
    print(f"  Route     : {b['source']} → {b['destination']}")
    print(f"  Departure : {b['departure']}")
    print(f"  Class     : {b['class_name']} ({b['class']})")
    print(f"  Fare      : ₹{b['fare']:.2f}")
    print(f"  Passenger : {b['passenger_name']}, Age {b['passenger_age']}")
    print(f"  Booked At : {b['booked_at']}")


def cancel_ticket(data):
    print_header("Cancel Ticket")
    pnr = input("  Enter PNR Number: ").strip()

    b = data["bookings"].get(pnr)
    if not b:
        print("  PNR not found.")
        return

    if b["status"] == "CANCELLED":
        print("  Ticket already cancelled.")
        return

    confirm = input(f"  Cancel booking for {b['passenger_name']} on {b['train_name']}? (yes/no): ")
    if confirm.lower() != "yes":
        print("  Cancellation aborted.")
        return

    b["status"] = "CANCELLED"
    # Restore seat
    data["trains"][b["train_no"]]["classes"][b["class"]]["available_seats"] += 1
    save_data(data)

    refund = round(b["fare"] * 0.75, 2)
    print(f"\n  Ticket cancelled. Refund of ₹{refund:.2f} will be processed.")


def list_all_trains(data):
    print_header("All 100 Trains")
    print(f"  {'No.':<7} {'Train Name':<30} {'From':<15} {'To':<15} {'Departs'}")
    print(f"  {'-'*80}")
    for t in data["trains"].values():
        print(f"  {t['number']:<7} {t['name']:<30} {t['source']:<15} {t['destination']:<15} {t['departure']}")


def my_bookings(data):
    print_header("My Bookings")
    name = input("  Enter Passenger Name: ").strip().lower()
    found = [b for b in data["bookings"].values() if b["passenger_name"].lower() == name]

    if not found:
        print("  No bookings found.")
        return

    for b in found:
        print(f"\n  PNR {b['pnr']} | {b['status']} | {b['train_name']} | "
              f"{b['source']}→{b['destination']} | {b['class']} | ₹{b['fare']:.2f}")


# --- Main Menu ---

def main():
    print_header("Welcome to Railway Reservation System")
    data = init_data()
    print(f"  System loaded with {len(data['trains'])} trains.")

    menu = {
        "1": ("Search Trains",       search_trains),
        "2": ("Book Ticket",         book_ticket),
        "3": ("Check PNR Status",    check_pnr),
        "4": ("Cancel Ticket",       cancel_ticket),
        "5": ("View All 100 Trains", list_all_trains),
        "6": ("My Bookings",         my_bookings),
        "7": ("Exit",                None),
    }

    while True:
        print("\n  --- Main Menu ---")
        for key, (label, _) in menu.items():
            print(f"  {key}. {label}")

        choice = input("\n  Choose option: ").strip()

        if choice == "7":
            print("\n  Thank you for using Railway Reservation. Goodbye!\n")
            break
        elif choice in menu:
            _, fn = menu[choice]
            fn(data)
        else:
            print("  Invalid choice. Try again.")


if __name__ == "__main__":
    main()
