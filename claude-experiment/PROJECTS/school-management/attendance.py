"""
attendance.py - Student attendance management module
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


def mark_attendance():
    print("\n--- Mark Attendance ---")
    att_date = input(f"Date (YYYY-MM-DD) [today={date.today()}]: ").strip() or str(date.today())

    conn = get_connection()
    students = conn.execute(
        "SELECT id, name, grade, roll_number FROM students ORDER BY grade, roll_number"
    ).fetchall()

    if not students:
        print("No students found. Please add students first.")
        conn.close()
        return

    print(f"\nMarking attendance for {att_date}")
    print("Status options: P=Present  A=Absent  L=Late  (default=Present)")

    records = []
    for s in students:
        status_input = input(f"  [{s['grade']}] {s['roll_number']} - {s['name']}: ").strip().upper() or "P"
        status_map = {"P": "Present", "A": "Absent", "L": "Late",
                      "PRESENT": "Present", "ABSENT": "Absent", "LATE": "Late"}
        status = status_map.get(status_input, "Present")
        records.append((s['id'], att_date, status))

    try:
        conn.executemany(
            "INSERT INTO attendance (student_id, date, status) VALUES (?, ?, ?) "
            "ON CONFLICT(student_id, date) DO UPDATE SET status=excluded.status",
            records
        )
        conn.commit()
        print(f"Attendance saved for {len(records)} students on {att_date}.")
    except Exception as e:
        print(f"Error: {e}")
    finally:
        conn.close()


def view_attendance_by_date():
    print("\n--- View Attendance by Date ---")
    att_date = input(f"Date (YYYY-MM-DD) [today={date.today()}]: ").strip() or str(date.today())

    conn = get_connection()
    rows = conn.execute(
        """SELECT s.roll_number, s.name, s.grade, a.status
           FROM attendance a
           JOIN students s ON s.id = a.student_id
           WHERE a.date = ?
           ORDER BY s.grade, s.roll_number""",
        (att_date,)
    ).fetchall()
    conn.close()

    print(f"\nAttendance for {att_date}:")
    _print_table(rows, ["Roll No.", "Name", "Grade", "Status"])

    if rows:
        total   = len(rows)
        present = sum(1 for r in rows if r['status'] == 'Present')
        absent  = sum(1 for r in rows if r['status'] == 'Absent')
        late    = sum(1 for r in rows if r['status'] == 'Late')
        print(f"\n  Summary: Total={total}  Present={present}  Absent={absent}  Late={late}")


def view_student_attendance():
    print("\n--- View Student Attendance Report ---")
    roll = input("Enter Roll Number: ").strip()

    conn = get_connection()
    student = conn.execute(
        "SELECT id, name, grade FROM students WHERE roll_number = ?", (roll,)
    ).fetchone()

    if not student:
        print("Student not found.")
        conn.close()
        return

    rows = conn.execute(
        "SELECT date, status FROM attendance WHERE student_id = ? ORDER BY date DESC",
        (student['id'],)
    ).fetchall()
    conn.close()

    print(f"\nAttendance for {student['name']} (Grade: {student['grade']}):")
    _print_table(rows, ["Date", "Status"])

    if rows:
        total   = len(rows)
        present = sum(1 for r in rows if r['status'] == 'Present')
        pct     = round((present / total) * 100, 1)
        print(f"\n  Attendance: {present}/{total} days ({pct}%)")


def attendance_summary():
    print("\n--- Attendance Summary (All Students) ---")
    conn = get_connection()
    rows = conn.execute(
        """SELECT s.roll_number, s.name, s.grade,
                  COUNT(a.id)                                        AS total_days,
                  SUM(CASE WHEN a.status='Present' THEN 1 ELSE 0 END) AS present,
                  SUM(CASE WHEN a.status='Absent'  THEN 1 ELSE 0 END) AS absent,
                  SUM(CASE WHEN a.status='Late'    THEN 1 ELSE 0 END) AS late,
                  ROUND(100.0 * SUM(CASE WHEN a.status='Present' THEN 1 ELSE 0 END)
                        / NULLIF(COUNT(a.id), 0), 1)                 AS pct
           FROM students s
           LEFT JOIN attendance a ON s.id = a.student_id
           GROUP BY s.id
           ORDER BY s.grade, s.roll_number"""
    ).fetchall()
    conn.close()
    _print_table(rows, ["Roll No.", "Name", "Grade", "Total", "Present", "Absent", "Late", "% Present"])


def attendance_menu():
    actions = {
        "1": ("Mark Attendance",            mark_attendance),
        "2": ("View Attendance by Date",    view_attendance_by_date),
        "3": ("View Student Report",        view_student_attendance),
        "4": ("Overall Summary",            attendance_summary),
        "0": ("Back",                       None),
    }
    while True:
        print("\n====== Attendance Management ======")
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
