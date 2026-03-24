"""
database.py - SQLite database setup and connection manager
"""

import sqlite3
import os

DB_PATH = os.path.join(os.path.dirname(__file__), "school.db")


def get_connection():
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    conn.execute("PRAGMA foreign_keys = ON")
    return conn


def initialize_db():
    conn = get_connection()
    c = conn.cursor()

    # Students table
    c.execute("""
        CREATE TABLE IF NOT EXISTS students (
            id          INTEGER PRIMARY KEY AUTOINCREMENT,
            name        TEXT NOT NULL,
            grade       TEXT NOT NULL,
            roll_number TEXT UNIQUE NOT NULL,
            phone       TEXT,
            email       TEXT,
            address     TEXT,
            created_at  TEXT DEFAULT (date('now'))
        )
    """)

    # Teachers table
    c.execute("""
        CREATE TABLE IF NOT EXISTS teachers (
            id         INTEGER PRIMARY KEY AUTOINCREMENT,
            name       TEXT NOT NULL,
            subject    TEXT NOT NULL,
            phone      TEXT,
            email      TEXT,
            salary     REAL DEFAULT 0.0,
            joined_at  TEXT DEFAULT (date('now'))
        )
    """)

    # Attendance table
    c.execute("""
        CREATE TABLE IF NOT EXISTS attendance (
            id         INTEGER PRIMARY KEY AUTOINCREMENT,
            student_id INTEGER NOT NULL,
            date       TEXT NOT NULL,
            status     TEXT NOT NULL CHECK(status IN ('Present','Absent','Late')),
            FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
            UNIQUE(student_id, date)
        )
    """)

    # Books table
    c.execute("""
        CREATE TABLE IF NOT EXISTS books (
            id            INTEGER PRIMARY KEY AUTOINCREMENT,
            title         TEXT NOT NULL,
            author        TEXT NOT NULL,
            isbn          TEXT UNIQUE,
            total_copies  INTEGER DEFAULT 1,
            available     INTEGER DEFAULT 1,
            added_at      TEXT DEFAULT (date('now'))
        )
    """)

    # Book issues table (who borrowed which book)
    c.execute("""
        CREATE TABLE IF NOT EXISTS book_issues (
            id          INTEGER PRIMARY KEY AUTOINCREMENT,
            book_id     INTEGER NOT NULL,
            student_id  INTEGER NOT NULL,
            issued_on   TEXT DEFAULT (date('now')),
            due_date    TEXT NOT NULL,
            returned_on TEXT,
            FOREIGN KEY (book_id)    REFERENCES books(id)    ON DELETE CASCADE,
            FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
        )
    """)

    # Payments table
    c.execute("""
        CREATE TABLE IF NOT EXISTS payments (
            id           INTEGER PRIMARY KEY AUTOINCREMENT,
            student_id   INTEGER NOT NULL,
            amount       REAL NOT NULL,
            payment_type TEXT NOT NULL,
            description  TEXT,
            paid_on      TEXT DEFAULT (date('now')),
            status       TEXT DEFAULT 'Paid' CHECK(status IN ('Paid','Pending','Overdue')),
            FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
        )
    """)

    conn.commit()
    conn.close()
    print("Database initialized successfully.")
