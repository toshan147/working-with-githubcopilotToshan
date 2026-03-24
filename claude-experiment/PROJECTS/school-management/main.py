"""
main.py - School Management System entry point
Run: python main.py
"""

import sys
import os

# Ensure the package directory is on the path
sys.path.insert(0, os.path.dirname(__file__))

from database  import initialize_db
from students  import student_menu
from attendance import attendance_menu
from teachers  import teachers_menu
from books     import books_menu
from payments  import payments_menu

BANNER = """
╔══════════════════════════════════════════╗
║       SCHOOL MANAGEMENT SYSTEM          ║
╚══════════════════════════════════════════╝
"""

MENU = {
    "1": ("Student Management",    student_menu),
    "2": ("Attendance Management", attendance_menu),
    "3": ("Teacher Management",    teachers_menu),
    "4": ("Book Management",       books_menu),
    "5": ("Payment Management",    payments_menu),
    "0": ("Exit",                  None),
}


def main():
    initialize_db()
    print(BANNER)
    while True:
        print("====== Main Menu ======")
        for key, (label, _) in MENU.items():
            print(f"  {key}. {label}")
        choice = input("\nSelect option: ").strip()
        if choice == "0":
            print("Goodbye!")
            break
        item = MENU.get(choice)
        if item:
            item[1]()
        else:
            print("Invalid option. Please try again.")


if __name__ == "__main__":
    main()
