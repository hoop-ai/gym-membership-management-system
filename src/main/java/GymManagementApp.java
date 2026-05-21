import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * Menu-driven console driver for the Gym Membership Management System.
 *
 * <p>Lets the user interact with the same engine the {@link Main} test demo
 * and the Swing GUI use. The console app makes the patterns visible from a
 * terminal -- handy when a presentation room only has SSH or a barebones
 * shell.</p>
 *
 * <p><strong>How to run:</strong></p>
 * <pre>
 *   javac -d bin src/main/java/*.java
 *   java -cp bin GymManagementApp
 * </pre>
 */
public class GymManagementApp {

    private final Gym gym;
    private final Scanner scanner;
    private boolean running;

    public GymManagementApp() {
        this.gym = new Gym("Demo Gym");
        this.scanner = new Scanner(System.in);
        this.running = true;
        seedPlans();
    }

    public static void main(String[] args) {
        new GymManagementApp().run();
    }

    /** Loads three sample plans so the user does not start from an empty catalogue. */
    private void seedPlans() {
        gym.registerPlan(new MembershipPlan.Builder("Basic Monthly")
                .durationMonths(1).monthlyFee(29.99).accessTier(AccessTier.BASIC).build());
        gym.registerPlan(new MembershipPlan.Builder("Standard Six-Month")
                .durationMonths(6).monthlyFee(49.99).accessTier(AccessTier.STANDARD)
                .includesClass("Yoga").includesClass("Spinning").freezeDaysPerYear(30).build());
        gym.registerPlan(new MembershipPlan.Builder("Premium Annual")
                .durationMonths(12).monthlyFee(89.99).accessTier(AccessTier.PREMIUM)
                .includesClass("Yoga").includesClass("Spinning").includesClass("HIIT")
                .guestPassesPerMonth(4).freezeDaysPerYear(60).personalTrainerIncluded(true).build());
    }

    // -----------------------------------------------------------------------
    // Main loop
    // -----------------------------------------------------------------------

    public void run() {
        printWelcome();
        while (running) {
            printMenu();
            int choice = readInt("Your choice: ");
            switch (choice) {
                case 1:  listPlans(); break;
                case 2:  buildPlanInteractively(); break;
                case 3:  enrolMember(); break;
                case 4:  listMembers(); break;
                case 5:  changeStatus(); break;
                case 6:  attachNotifier(); break;
                case 7:  publishPaymentDue(); break;
                case 8:  publishRenewalReminder(); break;
                case 9:  publishClassCancellation(); break;
                case 10: publishPromotion(); break;
                case 11: System.out.println(); System.out.println(gym.getSummary()); break;
                case 0:  running = false; break;
                default: System.out.println("Invalid choice."); break;
            }
        }
        System.out.println();
        System.out.println("Goodbye. Stay strong.");
        scanner.close();
    }

    // -----------------------------------------------------------------------
    // Menu rendering
    // -----------------------------------------------------------------------

    private void printWelcome() {
        System.out.println();
        System.out.println("==========================================================");
        System.out.println("  SEN3006 -- Gym Membership Management System (Console)");
        System.out.println("==========================================================");
        System.out.println("  Patterns: Builder + Observer");
        System.out.println("  Engine:   pure Java, zero external dependencies");
        System.out.println("==========================================================");
    }

    private void printMenu() {
        System.out.println();
        System.out.println("----------------------------------------------------------");
        System.out.println("  MAIN MENU");
        System.out.println("----------------------------------------------------------");
        System.out.println("  Plans:");
        System.out.println("    1. List plans");
        System.out.println("    2. Build a new plan (interactive Builder demo)");
        System.out.println("  Members:");
        System.out.println("    3. Enrol a member");
        System.out.println("    4. List members");
        System.out.println("    5. Change a member's status");
        System.out.println("    6. Attach a notifier to a member");
        System.out.println("  Notifications:");
        System.out.println("    7. Publish payment-due (targeted)");
        System.out.println("    8. Publish renewal reminder (targeted)");
        System.out.println("    9. Publish class cancellation (broadcast)");
        System.out.println("   10. Publish promotion (broadcast)");
        System.out.println("  Info:");
        System.out.println("   11. Show summary");
        System.out.println("    0. Exit");
        System.out.println("----------------------------------------------------------");
    }

    // -----------------------------------------------------------------------
    // Plan actions
    // -----------------------------------------------------------------------

    private void listPlans() {
        System.out.println("\nPlans on offer:");
        for (MembershipPlan p : gym.getAllPlans()) {
            System.out.println("  " + p);
        }
    }

    private void buildPlanInteractively() {
        System.out.println("\nBuild a new plan (Builder pattern demo).");
        String name = readLine("Display name: ");
        try {
            MembershipPlan.Builder b = new MembershipPlan.Builder(name);
            b.durationMonths(readInt("Duration in months: "));
            b.monthlyFee(readDouble("Monthly fee: "));
            b.accessTier(readAccessTier("Access tier (BASIC / STANDARD / PREMIUM): "));
            int classCount = readInt("How many included classes? (0 for none): ");
            for (int i = 0; i < classCount; i++) {
                b.includesClass(readLine("  class " + (i + 1) + ": "));
            }
            b.guestPassesPerMonth(readInt("Guest passes per month (0 for none): "));
            b.freezeDaysPerYear(readInt("Freeze days per year (0 for none): "));
            b.personalTrainerIncluded(readLine("Personal trainer included? (y/n): ").startsWith("y"));
            MembershipPlan plan = b.build();
            gym.registerPlan(plan);
            System.out.println("Registered: " + plan);
        } catch (IllegalArgumentException e) {
            System.out.println("Could not build plan: " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Member actions
    // -----------------------------------------------------------------------

    private void enrolMember() {
        String name      = readLine("Member name: ");
        String email     = readLine("Email: ");
        String phone     = readLine("Phone (blank if none): ");
        listPlans();
        String planName  = readLine("Plan name: ");
        try {
            Member m = gym.enrolMember(name, email, phone, planName);
            System.out.println("Enrolled: " + m);
        } catch (IllegalArgumentException e) {
            System.out.println("Could not enrol: " + e.getMessage());
        }
    }

    private void listMembers() {
        System.out.println("\nMembers:");
        for (Member m : gym.getAllMembers()) {
            System.out.println("  " + m);
        }
    }

    private void changeStatus() {
        int id = readInt("Member ID: ");
        try {
            Member m = gym.getMember(id);
            System.out.println("Current status: " + m.getStatus());
            System.out.println("Statuses: PENDING, ACTIVE, EXPIRING, EXPIRED, FROZEN, CANCELLED");
            String s = readLine("New status: ").toUpperCase();
            gym.changeMemberStatus(id, MembershipStatus.valueOf(s));
            System.out.println("New status: " + m.getStatus());
        } catch (IllegalArgumentException e) {
            System.out.println("Could not change status: " + e.getMessage());
        }
    }

    private void attachNotifier() {
        int id = readInt("Member ID: ");
        try {
            Member m = gym.getMember(id);
            String channel = readLine("Channel (EMAIL / SMS / PUSH): ").toUpperCase();
            switch (channel) {
                case "EMAIL": m.attachNotifier(new EmailMemberNotifier(m)); break;
                case "SMS":   m.attachNotifier(new SmsMemberNotifier(m));   break;
                case "PUSH":  m.attachNotifier(new PushMemberNotifier(m));  break;
                default:      System.out.println("Unknown channel."); return;
            }
            System.out.println("Attached " + channel + " notifier to " + m.getName() + ".");
        } catch (IllegalArgumentException e) {
            System.out.println("Could not attach: " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Notification actions
    // -----------------------------------------------------------------------

    private void publishPaymentDue() {
        int id = readInt("Member ID: ");
        LocalDate due = readDate("Due date (YYYY-MM-DD): ");
        double amount = readDouble("Amount: ");
        try {
            gym.publishPaymentDue(id, due, amount);
        } catch (IllegalArgumentException e) {
            System.out.println("Could not publish: " + e.getMessage());
        }
    }

    private void publishRenewalReminder() {
        int id = readInt("Member ID: ");
        try {
            gym.publishRenewalReminder(id);
        } catch (IllegalArgumentException e) {
            System.out.println("Could not publish: " + e.getMessage());
        }
    }

    private void publishClassCancellation() {
        String name = readLine("Class name: ");
        LocalDate date = readDate("Class date (YYYY-MM-DD): ");
        try {
            gym.publishClassCancellation(name, date);
        } catch (IllegalArgumentException e) {
            System.out.println("Could not publish: " + e.getMessage());
        }
    }

    private void publishPromotion() {
        double discount = readDouble("Discount percent (0-100): ");
        String message = readLine("Marketing message: ");
        try {
            gym.publishPromotion(discount, message);
        } catch (IllegalArgumentException e) {
            System.out.println("Could not publish: " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Input helpers
    // -----------------------------------------------------------------------

    private String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a whole number.");
            }
        }
    }

    private double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Double.parseDouble(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number.");
            }
        }
    }

    private LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return LocalDate.parse(scanner.nextLine().trim());
            } catch (DateTimeParseException e) {
                System.out.println("Please enter a date in YYYY-MM-DD format.");
            }
        }
    }

    private AccessTier readAccessTier(String prompt) {
        while (true) {
            try {
                return AccessTier.valueOf(readLine(prompt).toUpperCase());
            } catch (IllegalArgumentException e) {
                System.out.println("Please enter BASIC, STANDARD, or PREMIUM.");
            }
        }
    }
}
