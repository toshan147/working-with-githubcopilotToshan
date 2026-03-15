from flask import Flask, render_template, request, jsonify
import json, os
from datetime import datetime

app = Flask(__name__)
DATA_FILE = "train_data.json"

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
    "SL": {"name": "Sleeper",  "fare_per_km": 0.5,  "seats": 72},
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

def load_data():
    if os.path.exists(DATA_FILE):
        with open(DATA_FILE) as f:
            return json.load(f)
    trains = {}
    for i in range(1, 101):
        src, dst = ROUTES[(i - 1) % len(ROUTES)]
        dist = get_distance(src, dst)
        train_no = str(10000 + i)
        trains[train_no] = {
            "number": train_no,
            "name": f"{TRAIN_NAMES[(i-1) % len(TRAIN_NAMES)]} {i:03d}",
            "source": src, "destination": dst, "distance_km": dist,
            "departure": f"{6 + (i % 16):02d}:00",
            "classes": {
                cls: {
                    "fare": round(dist * info["fare_per_km"], 2),
                    "total_seats": info["seats"],
                    "available_seats": info["seats"],
                } for cls, info in CLASSES.items()
            },
        }
    data = {"trains": trains, "bookings": {}, "next_pnr": 1000}
    with open(DATA_FILE, "w") as f:
        json.dump(data, f, indent=2)
    return data

def save_data(data):
    with open(DATA_FILE, "w") as f:
        json.dump(data, f, indent=2)

# ── Routes ──────────────────────────────────────────────

@app.route("/")
def index():
    return render_template("index.html")

@app.route("/api/cities")
def cities():
    data = load_data()
    cities_set = set()
    for t in data["trains"].values():
        cities_set.add(t["source"])
        cities_set.add(t["destination"])
    return jsonify(sorted(cities_set))

@app.route("/api/search")
def search():
    src = request.args.get("from", "").strip().title()
    dst = request.args.get("to", "").strip().title()
    data = load_data()
    results = [
        t for t in data["trains"].values()
        if t["source"].lower() == src.lower() and t["destination"].lower() == dst.lower()
    ]
    return jsonify(results)

@app.route("/api/trains")
def all_trains():
    data = load_data()
    return jsonify(list(data["trains"].values()))

@app.route("/api/book", methods=["POST"])
def book():
    body = request.json
    data = load_data()
    train_no = body.get("train_no")
    cls      = body.get("cls")
    name     = body.get("name", "").strip()
    age      = body.get("age", "").strip()

    if not all([train_no, cls, name, age]):
        return jsonify({"error": "All fields are required."}), 400

    t = data["trains"].get(train_no)
    if not t:
        return jsonify({"error": "Train not found."}), 404
    if cls not in t["classes"]:
        return jsonify({"error": "Invalid class."}), 400
    if t["classes"][cls]["available_seats"] == 0:
        return jsonify({"error": "No seats available."}), 409

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
    return jsonify(booking)

@app.route("/api/pnr/<pnr>")
def pnr_status(pnr):
    data = load_data()
    b = data["bookings"].get(pnr)
    if not b:
        return jsonify({"error": "PNR not found."}), 404
    return jsonify(b)

@app.route("/api/cancel/<pnr>", methods=["POST"])
def cancel(pnr):
    data = load_data()
    b = data["bookings"].get(pnr)
    if not b:
        return jsonify({"error": "PNR not found."}), 404
    if b["status"] == "CANCELLED":
        return jsonify({"error": "Already cancelled."}), 409

    b["status"] = "CANCELLED"
    data["trains"][b["train_no"]]["classes"][b["class"]]["available_seats"] += 1
    save_data(data)
    refund = round(b["fare"] * 0.75, 2)
    return jsonify({**b, "refund": refund})

@app.route("/api/my-bookings")
def my_bookings():
    name = request.args.get("name", "").strip().lower()
    data = load_data()
    found = [b for b in data["bookings"].values() if b["passenger_name"].lower() == name]
    return jsonify(found)

if __name__ == "__main__":
    app.run(debug=True, port=5000)
