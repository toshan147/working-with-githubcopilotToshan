package hotel.model;

public class Employee {
    public enum Role {
        MANAGER, RECEPTIONIST, HOUSEKEEPING, SECURITY, CHEF
    }

    private static int counter = 200;

    private final int employeeId;
    private final String name;
    private final Role role;
    private final double salary;

    public Employee(String name, Role role, double salary) {
        this.employeeId = ++counter;
        this.name = name;
        this.role = role;
        this.salary = salary;
    }

    public int getEmployeeId()  { return employeeId; }
    public String getName()     { return name; }
    public Role getRole()       { return role; }
    public double getSalary()   { return salary; }

    @Override
    public String toString() {
        return String.format("Emp #%d | %-20s | %-15s | ₹%.0f/month",
                employeeId, name, role, salary);
    }
}
