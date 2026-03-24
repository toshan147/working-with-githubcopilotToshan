"""
books.py - Book management and library module
"""

from datetime import date, timedelta
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


def add_book():
    print("\n--- Add New Book ---")
    title   = input("Title          : ").strip()
    author  = input("Author         : ").strip()
    isbn    = input("ISBN           : ").strip()
    copies  = input("Total Copies   : ").strip()

    if not title or not author:
        print("Title and Author are required.")
        return

    try:
        copies = int(copies) if copies else 1
    except ValueError:
        copies = 1

    conn = get_connection()
    try:
        conn.execute(
            "INSERT INTO books (title, author, isbn, total_copies, available) VALUES (?, ?, ?, ?, ?)",
            (title, author, isbn or None, copies, copies)
        )
        conn.commit()
        print(f"Book '{title}' added successfully.")
    except Exception as e:
        print(f"Error: {e}")
    finally:
        conn.close()


def view_all_books():
    print("\n--- Book Catalog ---")
    conn = get_connection()
    rows = conn.execute(
        "SELECT id, title, author, isbn, total_copies, available FROM books ORDER BY title"
    ).fetchall()
    conn.close()
    _print_table(rows, ["ID", "Title", "Author", "ISBN", "Total", "Available"])


def search_book():
    print("\n--- Search Book ---")
    term = input("Enter title or author: ").strip()
    conn = get_connection()
    rows = conn.execute(
        "SELECT id, title, author, isbn, total_copies, available FROM books "
        "WHERE title LIKE ? OR author LIKE ?",
        (f"%{term}%", f"%{term}%")
    ).fetchall()
    conn.close()
    _print_table(rows, ["ID", "Title", "Author", "ISBN", "Total", "Available"])


def issue_book():
    print("\n--- Issue Book to Student ---")
    book_id    = input("Book ID      : ").strip()
    roll       = input("Student Roll : ").strip()
    days_input = input("Issue for how many days? [14]: ").strip()
    days       = int(days_input) if days_input.isdigit() else 14
    due_date   = str(date.today() + timedelta(days=days))

    conn = get_connection()

    book = conn.execute(
        "SELECT id, title, available FROM books WHERE id = ?", (book_id,)
    ).fetchone()
    if not book:
        print("Book not found.")
        conn.close()
        return
    if book['available'] <= 0:
        print(f"No available copies of '{book['title']}'.")
        conn.close()
        return

    student = conn.execute(
        "SELECT id, name FROM students WHERE roll_number = ?", (roll,)
    ).fetchone()
    if not student:
        print("Student not found.")
        conn.close()
        return

    # Check if student already has this book
    existing = conn.execute(
        "SELECT id FROM book_issues WHERE book_id=? AND student_id=? AND returned_on IS NULL",
        (book_id, student['id'])
    ).fetchone()
    if existing:
        print(f"{student['name']} already has this book issued.")
        conn.close()
        return

    conn.execute(
        "INSERT INTO book_issues (book_id, student_id, due_date) VALUES (?, ?, ?)",
        (book_id, student['id'], due_date)
    )
    conn.execute(
        "UPDATE books SET available = available - 1 WHERE id = ?", (book_id,)
    )
    conn.commit()
    conn.close()
    print(f"Book '{book['title']}' issued to {student['name']}. Due: {due_date}")


def return_book():
    print("\n--- Return Book ---")
    issue_id = input("Enter Issue ID (from active issues list): ").strip()

    conn = get_connection()
    row = conn.execute(
        """SELECT bi.id, b.title, b.id as book_id, s.name, bi.due_date
           FROM book_issues bi
           JOIN books b ON b.id = bi.book_id
           JOIN students s ON s.id = bi.student_id
           WHERE bi.id = ? AND bi.returned_on IS NULL""",
        (issue_id,)
    ).fetchone()

    if not row:
        print("Active issue not found for that ID.")
        conn.close()
        return

    today = str(date.today())
    conn.execute(
        "UPDATE book_issues SET returned_on = ? WHERE id = ?", (today, issue_id)
    )
    conn.execute(
        "UPDATE books SET available = available + 1 WHERE id = ?", (row['book_id'],)
    )
    conn.commit()
    conn.close()

    overdue = date.today() > date.fromisoformat(row['due_date'])
    msg = " (OVERDUE)" if overdue else ""
    print(f"Book '{row['title']}' returned by {row['name']} on {today}.{msg}")


def view_active_issues():
    print("\n--- Currently Issued Books ---")
    conn = get_connection()
    rows = conn.execute(
        """SELECT bi.id, b.title, s.name, s.roll_number, bi.issued_on, bi.due_date,
                  CASE WHEN bi.due_date < date('now') THEN 'OVERDUE' ELSE 'OK' END AS status
           FROM book_issues bi
           JOIN books b ON b.id = bi.book_id
           JOIN students s ON s.id = bi.student_id
           WHERE bi.returned_on IS NULL
           ORDER BY bi.due_date"""
    ).fetchall()
    conn.close()
    _print_table(rows, ["Issue ID", "Book", "Student", "Roll No.", "Issued On", "Due Date", "Status"])


def update_book():
    print("\n--- Update Book ---")
    bid = input("Enter Book ID to update: ").strip()
    conn = get_connection()
    row = conn.execute("SELECT * FROM books WHERE id = ?", (bid,)).fetchone()
    if not row:
        print("Book not found.")
        conn.close()
        return

    print(f"Editing: {row['title']} (leave blank to keep current value)")
    title  = input(f"Title [{row['title']}]   : ").strip() or row['title']
    author = input(f"Author [{row['author']}] : ").strip() or row['author']
    isbn   = input(f"ISBN [{row['isbn']}]     : ").strip() or row['isbn']

    conn.execute(
        "UPDATE books SET title=?, author=?, isbn=? WHERE id=?",
        (title, author, isbn, bid)
    )
    conn.commit()
    conn.close()
    print("Book updated successfully.")


def books_menu():
    actions = {
        "1": ("Add Book",           add_book),
        "2": ("View All Books",     view_all_books),
        "3": ("Search Book",        search_book),
        "4": ("Issue Book",         issue_book),
        "5": ("Return Book",        return_book),
        "6": ("View Active Issues", view_active_issues),
        "7": ("Update Book",        update_book),
        "0": ("Back",               None),
    }
    while True:
        print("\n====== Book Management ======")
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
