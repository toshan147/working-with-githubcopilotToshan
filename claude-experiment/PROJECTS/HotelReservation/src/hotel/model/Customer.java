package hotel.model;

public class Customer {
    private static int counter = 1000;

    private final int customerId;
    private final String name;
    private final String phone;
    private final String idProof;   // Aadhaar / Passport / DL

    public Customer(String name, String phone, String idProof) {
        this.customerId = ++counter;
        this.name = name;
        this.phone = phone;
        this.idProof = idProof;
    }

    public int getCustomerId()  { return customerId; }
    public String getName()     { return name; }
    public String getPhone()    { return phone; }
    public String getIdProof()  { return idProof; }

    @Override
    public String toString() {
        return String.format("Customer #%d | %s | Ph: %s | ID: %s",
                customerId, name, phone, idProof);
    }
}
