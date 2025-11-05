import java.util.*;

/**
 * Simple UPI Management System (in-memory)
 * - Register, Login (PIN), Balance, Add Money, Send Money, Transaction History
 * - For learning/demo purposes only (no real security or persistence)
 */
public class UPIManagement {

    static class User {
        String name;
        String upiId;
        String pin;            // NOTE: never store plain PINs in a real app
        double balance;
        List<String> history;

        User(String name, String upiId, String pin) {
            this.name = name;
            this.upiId = upiId;
            this.pin = pin;
            this.balance = 0.0;
            this.history = new ArrayList<>();
        }

        void addHistory(String entry) {
            String time = new Date().toString();
            history.add(time + " - " + entry);
        }
    }

    private static final Scanner sc = new Scanner(System.in);
    private static final Map<String, User> users = new HashMap<>();
    private static User currentUser = null;

    public static void main(String[] args) {
        seedSampleData();
        while (true) {
            if (currentUser == null) {
                guestMenu();
            } else {
                userMenu();
            }
        }
    }

    private static void seedSampleData() {
        User u = new User("SampleUser", "sample@upi", "1234");
        u.balance = 1000.0;
        u.addHistory("Seeded balance ₹1000");
        users.put(u.upiId, u);
    }

    private static void guestMenu() {
        System.out.println("\n=== UPI Management ===");
        System.out.println("1. Register");
        System.out.println("2. Login");
        System.out.println("3. Exit");
        System.out.print("Select: ");
        String choice = sc.nextLine().trim();
        switch (choice) {
            case "1": register(); break;
            case "2": login(); break;
            case "3": exitApp(); break;
            default: System.out.println("Invalid selection.");
        }
    }

    private static void userMenu() {
        System.out.println("\n=== Welcome, " + currentUser.name + " (" + currentUser.upiId + ") ===");
        System.out.println("1. View Balance");
        System.out.println("2. Add Money");
        System.out.println("3. Send Money");
        System.out.println("4. Transaction History");
        System.out.println("5. Logout");
        System.out.print("Select: ");
        String choice = sc.nextLine().trim();
        switch (choice) {
            case "1": viewBalance(); break;
            case "2": addMoney(); break;
            case "3": sendMoney(); break;
            case "4": showHistory(); break;
            case "5": logout(); break;
            default: System.out.println("Invalid selection.");
        }
    }

    // Registration
    private static void register() {
        System.out.print("Enter full name: ");
        String name = sc.nextLine().trim();
        if (name.isEmpty()) { System.out.println("Name cannot be empty."); return; }

        System.out.print("Choose UPI ID (example: name@upi): ");
        String upi = sc.nextLine().trim().toLowerCase();
        if (!upi.matches("[a-z0-9._+-]+@[a-z0-9.-]+")) {
            System.out.println("Invalid UPI ID format.");
            return;
        }
        if (users.containsKey(upi)) {
            System.out.println("UPI ID already exists. Try login or choose another UPI ID.");
            return;
        }

        System.out.print("Set 4-digit PIN: ");
        String pin = sc.nextLine().trim();
        if (!pin.matches("\\d{4}")) {
            System.out.println("PIN must be exactly 4 digits.");
            return;
        }

        User newUser = new User(name, upi, pin);
        newUser.addHistory("Account created");
        users.put(upi, newUser);
        System.out.println("Registered successfully. You can now login.");
    }

    // Login
    private static void login() {
        System.out.print("Enter UPI ID: ");
        String upi = sc.nextLine().trim().toLowerCase();
        User u = users.get(upi);
        if (u == null) {
            System.out.println("No account found for this UPI ID.");
            return;
        }
        System.out.print("Enter PIN: ");
        String pin = sc.nextLine().trim();
        if (!u.pin.equals(pin)) {
            System.out.println("Incorrect PIN.");
            return;
        }
        currentUser = u;
        System.out.println("Login successful. Welcome, " + currentUser.name + "!");
    }

    private static void viewBalance() {
        System.out.printf("Your balance: ₹%.2f%n", currentUser.balance);
    }

    private static void addMoney() {
        System.out.print("Enter amount to add (₹): ");
        String amtStr = sc.nextLine().trim();
        double amt;
        try {
            amt = Double.parseDouble(amtStr);
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount.");
            return;
        }
        if (amt <= 0) { System.out.println("Amount must be positive."); return; }
        currentUser.balance += amt;
        currentUser.addHistory("Added ₹" + String.format("%.2f", amt) + " to wallet");
        System.out.printf("₹%.2f added. New balance: ₹%.2f%n", amt, currentUser.balance);
    }

    private static void sendMoney() {
        System.out.print("Enter recipient UPI ID: ");
        String destUpi = sc.nextLine().trim().toLowerCase();
        if (destUpi.equals(currentUser.upiId)) {
            System.out.println("Cannot send to yourself.");
            return;
        }
        User recipient = users.get(destUpi);
        if (recipient == null) {
            System.out.println("Recipient not found.");
            return;
        }

        System.out.print("Enter amount to send (₹): ");
        String amtStr = sc.nextLine().trim();
        double amt;
        try {
            amt = Double.parseDouble(amtStr);
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount.");
            return;
        }
        if (amt <= 0) { System.out.println("Amount must be positive."); return; }
        if (amt > currentUser.balance) {
            System.out.println("Insufficient balance.");
            return;
        }

        System.out.print("Enter your 4-digit PIN to confirm: ");
        String pin = sc.nextLine().trim();
        if (!currentUser.pin.equals(pin)) {
            System.out.println("Incorrect PIN. Transaction cancelled.");
            return;
        }

        currentUser.balance -= amt;
        recipient.balance += amt;

        String tx = "Sent ₹" + String.format("%.2f", amt) + " to " + destUpi;
        String rx = "Received ₹" + String.format("%.2f", amt) + " from " + currentUser.upiId;

        currentUser.addHistory(tx);
        recipient.addHistory(rx);

        System.out.println("Transaction successful. " + tx);
        System.out.printf("Your new balance: ₹%.2f%n", currentUser.balance);
    }

    private static void showHistory() {
        System.out.println("=== Transaction History ===");
        if (currentUser.history.isEmpty()) {
            System.out.println("No transactions yet.");
            return;
        }
        // show last 10 entries (most recent first)
        int start = Math.max(0, currentUser.history.size() - 10);
        List<String> sub = currentUser.history.subList(start, currentUser.history.size());
        Collections.reverse(sub);
        for (String e : sub) {
            System.out.println(e);
        }
        Collections.reverse(sub); // restore order
    }

    private static void logout() {
        System.out.println("Logged out: " + currentUser.name);
        currentUser = null;
    }

    private static void exitApp() {
        System.out.println("Exiting. Goodbye!");
        sc.close();
        System.exit(0);
    }
}
