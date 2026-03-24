"""
students.py - Student management module
"""

from database import get_connection


def _print_table(rows, headers):
    if not rows:
        print("  No records found.")
        return
    col_widths = [len(h) for h in headers]
    data = [list(row) for row in rows]
    for row in data:
        for i, val in enumerate(row):
            col_widths[i] = max(col_widths[i], len(str(val)))
    fmt = "  " + "  ".join(f"{{:<{w}}}" for w in col_widths)
    sep = "  " + "  ".join("-" * w for w in col_widths)
    print(fmt.format(*headers))
    print(sep)
    for row in data:
        print(fmt.format(*[str(v) for v in row]))


def add_student():
    print("\n--- Add New Student ---")
    name        = input("Name        : ").strip()
    grade       = input("Grade/Class : ").strip()
    roll_number = input("Roll Number : ").strip()
    phone       = input("Phone       : ").strip()
    email       = input("Email       : ").strip()
    address     = input("Address     : ").strip()

    if not name or not grade or not roll_number:
        print("Name, Grade, and Roll Number are required.")
        return

    conn = get_connection()
    try:
        conn.execute(
            "INSERT INTO students (name, grade, roll_number, phone, email, address) "
            "VALUES (?, ?, ?, ?, ?, ?)",
            (name, grade, roll_number, phone, email, address)
        )
        conn.commit()
        print(f"Student '{name}' added successfully.")
    except Exception as e:
        print(f"Error: {e}")
    finally:
        conn.close()


def view_all_students():
    print("\n--- All Students ---")
    conn = get_connection()
    rows = conn.execute(
        "SELECT id, name, grade, roll_number, phone, email FROM students ORDER BY grade, roll_number"
    ).fetchall()
    conn.close()
    _print_table(rows, ["ID", "Name", "Grade", "Roll No.", "Phone", "Email"])


def search_student():
    print("\n--- Search Student ---")
    term = input("Enter name or roll number: ").strip()
    conn = get_connection()
    rows = conn.execute(
        "SELECT id, name, grade, roll_number, phone, email FROM students "
        "WHERE name LIKE ? OR roll_number LIKE ?",
        (f"%{term}%", f"%{term}%")
    ).fetchall()
    conn.close()
    _print_table(rows, ["ID", "Name", "Grade", "Roll No.", "Phone", "Email"])


def update_student():
    print("\n--- Update Student ---")
    sid = input("Enter Student ID to update: ").strip()
    conn = get_connection()
    row = conn.execute("SELECT * FROM students WHERE id = ?", (sid,)).fetchone()
    if not row:
        print("Student not found.")
        conn.close()
        return

    print(f"Editing: {row['name']} (leave blank to keep current value)")
    name    = input(f"Name [{row['name']}]        : ").strip() or row['name']
    grade   = input(f"Grade [{row['grade']}]       : ").strip() or row['grade']
    phone   = input(f"Phone [{row['phone']}]       : ").strip() or row['phone']
    email   = input(f"Email [{row['email']}]       : ").strip() or row['email']
    address = input(f"Address [{row['address']}]   : ").strip() or row['address']

    conn.execute(
        "UPDATE students SET name=?, grade=?, phone=?, email=?, address=? WHERE id=?",
        (name, grade, phone, email, address, sid)
    )
    conn.commit()
    conn.close()
    print("Student updated successfully.")


def delete_student():
    print("\n--- Delete Student ---")
    sid = input("Enter Student ID to delete: ").strip()
    conn = get_connection()
    row = conn.execute("SELECT name FROM students WHERE id = ?", (sid,)).fetchone()
    if not row:
        print("Student not found.")
        conn.close()
        return
    confirm = input(f"Delete '{row['name']}'? This will remove all related records. (yes/no): ").strip().lower()
    if confirm == "yes":
        conn.execute("DELETE FROM students WHERE id = ?", (sid,))
        conn.commit()
        print("Student deleted.")
    else:
        print("Cancelled.")
    conn.close()


def student_menu():
    actions = {
        "1": ("Add Student",        add_student),
        "2": ("View All Students",  view_all_students),
        "3": ("Search Student",     search_student),
        "4": ("Update Student",     update_student),
        "5": ("Delete Student",     delete_student),
        "0": ("Back",               None),
    }
    while True:
        print("\n====== Student Management ======")
        for key, (label, _) in actions.items():
            print(f"  {key}. {label}")
        choice = input("Select: ").strip()
        if choice == "0":
            break
        action = actions.get(choice)
        if action:
            action[1]()
        else:
            print("Invalid option.")
