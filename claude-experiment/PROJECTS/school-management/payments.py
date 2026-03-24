"""
payments.py - Fee and payment management module
"""

from datetime import date
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


PAYMENT_TYPES = ["Tuition Fee", "Exam Fee", "Library Fee", "Sports Fee", "Transport Fee", "Other"]


def add_payment():
    print("\n--- Record Payment ---")
    roll = input("Student Roll Number: ").strip()

    conn = get_connection()
    student = conn.execute(
        "SELECT id, name, grade FROM students WHERE roll_number = ?", (roll,)
    ).fetchone()
    if not student:
        print("Student not found.")
        conn.close()
        return

    print(f"\nStudent: {student['name']} (Grade: {student['grade']})")
    print("Payment Types:")
    for i, pt in enumerate(PAYMENT_TYPES, 1):
        print(f"  {i}. {pt}")

    pt_choice = input("Select type (number): ").strip()
    try:
        payment_type = PAYMENT_TYPES[int(pt_choice) - 1]
    except (ValueError, IndexError):
        payment_type = "Other"

    amount      = input("Amount         : ").strip()
    description = input("Description    : ").strip()
    status_inp  = input("Status (1=Paid 2=Pending 3=Overdue) [1]: ").strip() or "1"
    status_map  = {"1": "Paid", "2": "Pending", "3": "Overdue"}
    status      = status_map.get(status_inp, "Paid")
    paid_on     = input(f"Date (YYYY-MM-DD) [today={date.today()}]: ").strip() or str(date.today())

    try:
        amount = float(amount)
    except ValueError:
        print("Invalid amount.")
        conn.close()
        return

    conn.execute(
        "INSERT INTO payments (student_id, amount, payment_type, description, paid_on, status) "
        "VALUES (?, ?, ?, ?, ?, ?)",
        (student['id'], amount, payment_type, description, paid_on, status)
    )
    conn.commit()
    conn.close()
    print(f"Payment of {amount:,.2f} recorded for {student['name']} ({status}).")


def view_student_payments():
    print("\n--- Student Payment History ---")
    roll = input("Student Roll Number: ").strip()

    conn = get_connection()
    student = conn.execute(
        "SELECT id, name, grade FROM students WHERE roll_number = ?", (roll,)
    ).fetchone()
    if not student:
        print("Student not found.")
        conn.close()
        return

    rows = conn.execute(
        "SELECT id, payment_type, amount, description, paid_on, status FROM payments "
        "WHERE student_id = ? ORDER BY paid_on DESC",
        (student['id'],)
    ).fetchall()
    conn.close()

    print(f"\nPayments for {student['name']} (Grade: {student['grade']}):")
    _print_table(rows, ["ID", "Type", "Amount", "Description", "Date", "Status"])

    if rows:
        total_paid = sum(r['amount'] for r in rows if r['status'] == 'Paid')
        total_due  = sum(r['amount'] for r in rows if r['status'] in ('Pending', 'Overdue'))
        print(f"\n  Total Paid: {total_paid:,.2f}  |  Total Due: {total_due:,.2f}")


def view_all_payments():
    print("\n--- All Payments ---")
    conn = get_connection()
    rows = conn.execute(
        """SELECT p.id, s.name, s.roll_number, p.payment_type, p.amount, p.paid_on, p.status
           FROM payments p
           JOIN students s ON s.id = p.student_id
           ORDER BY p.paid_on DESC"""
    ).fetchall()
    conn.close()
    _print_table(rows, ["ID", "Student", "Roll No.", "Type", "Amount", "Date", "Status"])


def pending_fees():
    print("\n--- Pending / Overdue Fees ---")
    conn = get_connection()
    rows = conn.execute(
        """SELECT p.id, s.name, s.roll_number, s.grade, p.payment_type, p.amount, p.paid_on, p.status
           FROM payments p
           JOIN students s ON s.id = p.student_id
           WHERE p.status IN ('Pending', 'Overdue')
           ORDER BY p.status, s.name"""
    ).fetchall()
    conn.close()
    _print_table(rows, ["ID", "Student", "Roll No.", "Grade", "Type", "Amount", "Due Date", "Status"])

    if rows:
        total = sum(r['amount'] for r in rows)
        print(f"\n  Total Outstanding: {total:,.2f}")


def mark_payment_paid():
    print("\n--- Mark Payment as Paid ---")
    pid = input("Enter Payment ID: ").strip()

    conn = get_connection()
    row = conn.execute(
        """SELECT p.id, s.name, p.amount, p.status FROM payments p
           JOIN students s ON s.id = p.student_id WHERE p.id = ?""",
        (pid,)
    ).fetchone()
    if not row:
        print("Payment not found.")
        conn.close()
        return
    if row['status'] == 'Paid':
        print(f"Payment already marked as Paid.")
        conn.close()
        return

    today = input(f"Payment date [today={date.today()}]: ").strip() or str(date.today())
    conn.execute(
        "UPDATE payments SET status='Paid', paid_on=? WHERE id=?", (today, pid)
    )
    conn.commit()
    conn.close()
    print(f"Payment of {row['amount']:,.2f} for {row['name']} marked as Paid.")


def revenue_summary():
    print("\n--- Revenue Summary by Payment Type ---")
    conn = get_connection()
    rows = conn.execute(
        """SELECT payment_type,
                  COUNT(*) AS count,
                  SUM(CASE WHEN status='Paid' THEN amount ELSE 0 END) AS collected,
                  SUM(CASE WHEN status!='Paid' THEN amount ELSE 0 END) AS outstanding
           FROM payments
           GROUP BY payment_type
           ORDER BY collected DESC"""
    ).fetchall()
    conn.close()
    _print_table(rows, ["Type", "Count", "Collected", "Outstanding"])
    if rows:
        total_collected   = sum(r['collected']   for r in rows)
        total_outstanding = sum(r['outstanding'] for r in rows)
        print(f"\n  Grand Total Collected  : {total_collected:,.2f}")
        print(f"  Grand Total Outstanding: {total_outstanding:,.2f}")


def payments_menu():
    actions = {
        "1": ("Record Payment",         add_payment),
        "2": ("View Student Payments",  view_student_payments),
        "3": ("View All Payments",      view_all_payments),
        "4": ("Pending / Overdue Fees", pending_fees),
        "5": ("Mark Payment as Paid",   mark_payment_paid),
        "6": ("Revenue Summary",        revenue_summary),
        "0": ("Back",                   None),
    }
    while True:
        print("\n====== Payment Management ======")
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
