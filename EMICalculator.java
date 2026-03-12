import java.util.Scanner;

public class EMICalculator {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=============================");
        System.out.println("      EMI CALCULATOR         ");
        System.out.println("=============================");

        System.out.print("Enter Principal Amount (₹): ");
        double principal = scanner.nextDouble();

        System.out.print("Enter Annual Interest Rate (%): ");
        double annualRate = scanner.nextDouble();

        System.out.print("Enter Loan Tenure (in months): ");
        int tenureMonths = scanner.nextInt();

        scanner.close();

        // Monthly interest rate
        double monthlyRate = annualRate / (12 * 100);

        // EMI = P * r * (1+r)^n / ((1+r)^n - 1)
        double factor = Math.pow(1 + monthlyRate, tenureMonths);
        double emi = (principal * monthlyRate * factor) / (factor - 1);

        double totalPayment = emi * tenureMonths;
        double totalInterest = totalPayment - principal;

        System.out.println("\n=============================");
        System.out.println("         RESULTS             ");
        System.out.println("=============================");
        System.out.printf("Monthly EMI        : ₹ %.2f%n", emi);
        System.out.printf("Total Payment      : ₹ %.2f%n", totalPayment);
        System.out.printf("Total Interest Paid: ₹ %.2f%n", totalInterest);
        System.out.println("=============================");
    }
}
