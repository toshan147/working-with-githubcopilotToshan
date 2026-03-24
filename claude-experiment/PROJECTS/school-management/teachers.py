"""
teachers.py - Teacher management module
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


def add_teacher():
    print("\n--- Add New Teacher ---")
    name    = input("Name    : ").strip()
    subject = input("Subject : ").strip()
    phone   = input("Phone   : ").strip()
    email   = input("Email   : ").strip()
    salary  = input("Salary  : ").strip()

    if not name or not subject:
        print("Name and Subject are required.")
        return

    try:
        salary = float(salary) if salary else 0.0
    except ValueError:
        print("Invalid salary. Setting to 0.")
        salary = 0.0

    conn = get_connection()
    try:
        conn.execute(
            "INSERT INTO teachers (name, subject, phone, email, salary) VALUES (?, ?, ?, ?, ?)",
            (name, subject, phone, email, salary)
        )
        conn.commit()
        print(f"Teacher '{name}' added successfully.")
    except Exception as e:
        print(f"Error: {e}")
    finally:
        conn.close()


def view_all_teachers():
    print("\n--- All Teachers ---")
    conn = get_connection()
    rows = conn.execute(
        "SELECT id, name, subject, phone, email, salary, joined_at FROM teachers ORDER BY name"
    ).fetchall()
    conn.close()
    _print_table(rows, ["ID", "Name", "Subject", "Phone", "Email", "Salary", "Joined"])


def search_teacher():
    print("\n--- Search Teacher ---")
    term = input("Enter name or subject: ").strip()
    conn = get_connection()
    rows = conn.execute(
        "SELECT id, name, subject, phone, email, salary FROM teachers "
        "WHERE name LIKE ? OR subject LIKE ?",
        (f"%{term}%", f"%{term}%")
    ).fetchall()
    conn.close()
    _print_table(rows, ["ID", "Name", "Subject", "Phone", "Email", "Salary"])


def update_teacher():
    print("\n--- Update Teacher ---")
    tid = input("Enter Teacher ID to update: ").strip()
    conn = get_connection()
    row = conn.execute("SELECT * FROM teachers WHERE id = ?", (tid,)).fetchone()
    if not row:
        print("Teacher not found.")
        conn.close()
        return

    print(f"Editing: {row['name']} (leave blank to keep current value)")
    name    = input(f"Name [{row['name']}]      : ").strip() or row['name']
    subject = input(f"Subject [{row['subject']}]: ").strip() or row['subject']
    phone   = input(f"Phone [{row['phone']}]    : ").strip() or row['phone']
    email   = input(f"Email [{row['email']}]    : ").strip() or row['email']
    salary_input = input(f"Salary [{row['salary']}] : ").strip()

    try:
        salary = float(salary_input) if salary_input else row['salary']
    except ValueError:
        salary = row['salary']

    conn.execute(
        "UPDATE teachers SET name=?, subject=?, phone=?, email=?, salary=? WHERE id=?",
        (name, subject, phone, email, salary, tid)
    )
    conn.commit()
    conn.close()
    print("Teacher updated successfully.")


def delete_teacher():
    print("\n--- Delete Teacher ---")
    tid = input("Enter Teacher ID to delete: ").strip()
    conn = get_connection()
    row = conn.execute("SELECT name FROM teachers WHERE id = ?", (tid,)).fetchone()
    if not row:
        print("Teacher not found.")
        conn.close()
        return
    confirm = input(f"Delete teacher '{row['name']}'? (yes/no): ").strip().lower()
    if confirm == "yes":
        conn.execute("DELETE FROM teachers WHERE id = ?", (tid,))
        conn.commit()
        print("Teacher deleted.")
    else:
        print("Cancelled.")
    conn.close()


def salary_report():
    print("\n--- Salary Report ---")
    conn = get_connection()
    rows = conn.execute(
        "SELECT name, subject, salary FROM teachers ORDER BY salary DESC"
    ).fetchall()
    conn.close()
    _print_table(rows, ["Name", "Subject", "Salary"])
    if rows:
        total = sum(r['salary'] for r in rows)
        print(f"\n  Total Monthly Salary Bill: {total:,.2f}")


def teachers_menu():
    actions = {
        "1": ("Add Teacher",        add_teacher),
        "2": ("View All Teachers",  view_all_teachers),
        "3": ("Search Teacher",     search_teacher),
        "4": ("Update Teacher",     update_teacher),
        "5": ("Delete Teacher",     delete_teacher),
        "6": ("Salary Report",      salary_report),
        "0": ("Back",               None),
    }
    while True:
        print("\n====== Teacher Management ======")
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
