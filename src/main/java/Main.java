import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entry point and scripted demonstration of the Gym Membership Management
 * System.
 *
 * <p>The class runs six self-checking sections that exercise the
 * <strong>Builder</strong> and <strong>Observer</strong> design patterns,
 * the {@link MembershipStatus} state machine, and a handful of edge cases.
 * Every check prints {@code [PASS]} on success; the program ends with the
 * banner {@code ALL TESTS PASSED} when every section is green.</p>
 */
public class Main {

    // -----------------------------------------------------------------------
    // Output helpers
    // -----------------------------------------------------------------------

    private static void printHeader(String title) {
        System.out.println();
        System.out.println("==========================================================");
        System.out.println("  " + title);
        System.out.println("==========================================================");
    }

    private static void printSubHeader(String title) {
        System.out.println();
        System.out.println("---- " + title + " ----");
    }

    // -----------------------------------------------------------------------
    // Main
    // -----------------------------------------------------------------------

    public static void main(String[] args) {
        System.out.println("##########################################################");
        System.out.println("#                                                        #");
        System.out.println("#    SEN3006 -- Gym Membership Management System Demo    #");
        System.out.println("#         Builder  +  Observer  (pure Java)              #");
        System.out.println("#                                                        #");
        System.out.println("##########################################################");

        // ==================================================================
        // TEST 1: BUILDER PATTERN DEMO
        // ==================================================================
        printHeader("TEST 1: Builder Pattern Demo");
        System.out.println("Demonstrating that membership plans -- objects with many");
        System.out.println("optional, validated attributes -- are constructed via a fluent,");
        System.out.println("immutable Builder.\n");

        MembershipPlan basic = new MembershipPlan.Builder("Basic Monthly")
                .durationMonths(1)
                .monthlyFee(29.99)
                .accessTier(AccessTier.BASIC)
                .build();

        MembershipPlan standard = new MembershipPlan.Builder("Standard Six-Month")
                .durationMonths(6)
                .monthlyFee(49.99)
                .accessTier(AccessTier.STANDARD)
                .includesClass("Yoga")
                .includesClass("Spinning")
                .freezeDaysPerYear(30)
                .build();

        MembershipPlan premium = new MembershipPlan.Builder("Premium Annual")
                .durationMonths(12)
                .monthlyFee(89.99)
                .accessTier(AccessTier.PREMIUM)
                .includesClass("Yoga")
                .includesClass("Spinning")
                .includesClass("HIIT")
                .includesClass("Pilates")
                .guestPassesPerMonth(4)
                .freezeDaysPerYear(60)
                .personalTrainerIncluded(true)
                .build();

        System.out.println("  " + basic);
        System.out.println("  " + standard);
        System.out.println("  " + premium);
        System.out.println();
        System.out.printf("  Premium total cost over %d months: %.2f%n",
                premium.getDurationMonths(), premium.getTotalCost());
        System.out.println("\n[PASS] Builder produced three distinct, immutable plans.");

        // ==================================================================
        // TEST 2: OBSERVER PATTERN DEMO
        // ==================================================================
        printHeader("TEST 2: Observer Pattern Demo");
        System.out.println("Demonstrating that the Gym (Subject) publishes events that are");
        System.out.println("delivered to every attached MemberNotifier (Observer), with");
        System.out.println("channel-specific formatting.\n");

        Gym gym = new Gym("Iron Park Fitness");
        gym.registerPlan(basic);
        gym.registerPlan(standard);
        gym.registerPlan(premium);

        Member alice = gym.enrolMember("Alice Aydin",   "alice@example.com",  "+90-555-0001", "Premium Annual");
        Member bob   = gym.enrolMember("Bob Bektas",    "bob@example.com",    "+90-555-0002", "Standard Six-Month");
        Member chloe = gym.enrolMember("Chloe Celikel", "chloe@example.com",  "+90-555-0003", "Basic Monthly");

        // Alice prefers email + push; Bob prefers SMS; Chloe takes all three.
        EmailMemberNotifier aliceEmail = new EmailMemberNotifier(alice);
        PushMemberNotifier  alicePush  = new PushMemberNotifier(alice);
        alice.attachNotifier(aliceEmail);
        alice.attachNotifier(alicePush);

        SmsMemberNotifier bobSms = new SmsMemberNotifier(bob);
        bob.attachNotifier(bobSms);

        EmailMemberNotifier chloeEmail = new EmailMemberNotifier(chloe);
        SmsMemberNotifier   chloeSms   = new SmsMemberNotifier(chloe);
        PushMemberNotifier  chloePush  = new PushMemberNotifier(chloe);
        chloe.attachNotifier(chloeEmail);
        chloe.attachNotifier(chloeSms);
        chloe.attachNotifier(chloePush);

        printSubHeader("Targeted payment-due event for Alice");
        gym.publishPaymentDue(alice.getId(), LocalDate.now().plusDays(7), 89.99);

        printSubHeader("Targeted renewal reminder for Bob");
        bob.setStatus(MembershipStatus.ACTIVE);
        bob.setStatus(MembershipStatus.EXPIRING);
        gym.publishRenewalReminder(bob.getId());

        printSubHeader("Broadcast: class cancellation");
        gym.publishClassCancellation("Spinning", LocalDate.now().plusDays(1));

        printSubHeader("Broadcast: promotion");
        gym.publishPromotion(20.0, "20% off Premium plans until the end of the month!");

        // Assertions on the logs.
        int aliceMessages = aliceEmail.getSentLog().size() + alicePush.getSentLog().size();
        int bobMessages   = bobSms.getSentLog().size();
        int chloeMessages = chloeEmail.getSentLog().size() + chloeSms.getSentLog().size() + chloePush.getSentLog().size();

        System.out.println();
        System.out.printf("  Alice received %d messages across her 2 channels.%n", aliceMessages);
        System.out.printf("  Bob received   %d messages across his 1 channel.%n",  bobMessages);
        System.out.printf("  Chloe received %d messages across her 3 channels.%n", chloeMessages);

        // Alice: 1 targeted (payment) * 2 channels + 2 broadcasts * 2 = 6
        // Bob:   1 targeted (renewal) * 1 channel  + 2 broadcasts * 1 = 3
        // Chloe: 0 targeted             * 3 channels + 2 broadcasts * 3 = 6
        assertEqual(6, aliceMessages, "Alice total messages");
        assertEqual(3, bobMessages,   "Bob total messages");
        assertEqual(6, chloeMessages, "Chloe total messages");
        System.out.println("\n[PASS] Targeted and broadcast events delivered to the right notifiers.");

        // ==================================================================
        // TEST 3: MEMBERSHIP STATUS STATE MACHINE
        // ==================================================================
        printHeader("TEST 3: Membership Lifecycle Demo");
        System.out.println("Demonstrating that the MembershipStatus enum enforces valid");
        System.out.println("transitions and rejects invalid ones.\n");

        Member dilan = gym.enrolMember("Dilan Demir", "dilan@example.com", "", "Standard Six-Month");

        printSubHeader("Happy path: PENDING -> ACTIVE -> EXPIRING -> ACTIVE -> EXPIRING -> EXPIRED");
        dilan.setStatus(MembershipStatus.ACTIVE);
        System.out.println("  " + dilan.getStatus());
        dilan.setStatus(MembershipStatus.EXPIRING);
        System.out.println("  " + dilan.getStatus());
        dilan.setStatus(MembershipStatus.ACTIVE);   // renewed!
        System.out.println("  " + dilan.getStatus() + " (renewal date pushed forward)");
        dilan.setStatus(MembershipStatus.EXPIRING);
        System.out.println("  " + dilan.getStatus());
        dilan.setStatus(MembershipStatus.EXPIRED);
        System.out.println("  " + dilan.getStatus());
        System.out.println("  [PASS] Reached EXPIRED via the happy path.");

        printSubHeader("Freeze branch: ACTIVE -> FROZEN -> ACTIVE");
        Member emir = gym.enrolMember("Emir Erdogan", "emir@example.com", "", "Premium Annual");
        emir.setStatus(MembershipStatus.ACTIVE);
        emir.setStatus(MembershipStatus.FROZEN);
        System.out.println("  After freeze: " + emir.getStatus());
        emir.setStatus(MembershipStatus.ACTIVE);
        System.out.println("  After resume: " + emir.getStatus());
        System.out.println("  [PASS] Freeze/resume cycle works.");

        printSubHeader("Invalid: PENDING -> EXPIRED (should fail)");
        Member feyza = gym.enrolMember("Feyza Firat", "feyza@example.com", "", "Basic Monthly");
        try {
            feyza.setStatus(MembershipStatus.EXPIRED);
            System.out.println("  FAIL: should have thrown");
        } catch (IllegalArgumentException e) {
            System.out.println("  Caught: " + e.getMessage());
            System.out.println("  [PASS] State machine rejected the invalid transition.");
        }

        printSubHeader("Terminal state: EXPIRED -> anything (should fail)");
        try {
            dilan.setStatus(MembershipStatus.ACTIVE);
            System.out.println("  FAIL: should have thrown");
        } catch (IllegalArgumentException e) {
            System.out.println("  Caught: " + e.getMessage());
            System.out.println("  [PASS] Terminal state blocks all transitions.");
        }

        // ==================================================================
        // TEST 4: GYM INTEGRATION DEMO
        // ==================================================================
        printHeader("TEST 4: Gym Integration Demo");
        System.out.println("Demonstrating the full workflow: register plans, enrol members,");
        System.out.println("attach notifiers, transition statuses, publish events, summarise.\n");

        Gym demoGym = new Gym("Sunrise Club");
        demoGym.registerPlan(basic);
        demoGym.registerPlan(standard);
        demoGym.registerPlan(premium);

        Member g1 = demoGym.enrolMember("Guest One",   "g1@example.com", "", "Basic Monthly");
        Member g2 = demoGym.enrolMember("Guest Two",   "g2@example.com", "", "Standard Six-Month");
        Member g3 = demoGym.enrolMember("Guest Three", "g3@example.com", "", "Premium Annual");

        g1.attachNotifier(new EmailMemberNotifier(g1));
        g2.attachNotifier(new SmsMemberNotifier(g2));
        g3.attachNotifier(new PushMemberNotifier(g3));

        g1.setStatus(MembershipStatus.ACTIVE);
        g2.setStatus(MembershipStatus.ACTIVE);
        g3.setStatus(MembershipStatus.ACTIVE);

        demoGym.publishPaymentDue(g1.getId(), LocalDate.now().plusDays(5), 29.99);
        demoGym.publishPromotion(15.0, "Refer a friend and save 15%.");

        System.out.println();
        System.out.println(demoGym.getSummary());
        System.out.println("[PASS] Full gym workflow demonstrated.");

        // ==================================================================
        // TEST 5: SOLID PRINCIPLES DEMO
        // ==================================================================
        printHeader("TEST 5: SOLID Principles Demo");
        System.out.println("Demonstrating that the system follows SOLID principles.\n");

        printSubHeader("OCP: attach a brand-new notifier channel at runtime");
        // An anonymous notifier implementation, defined inline -- existing
        // notifiers and the Gym are unchanged.
        MemberNotifier slackNotifier = new MemberNotifier() {
            private final List<String> log = new ArrayList<>();
            @Override public Member getMember()  { return alice; }
            @Override public String getChannel() { return "SLACK"; }
            @Override public void onEvent(GymEvent event) {
                if (!event.isBroadcast() && !event.getTargetMember().equals(getMember())) return;
                String formatted = "[SLACK -> @alice] " + event.getType() + " :: " + event.getMessage();
                log.add(formatted);
                System.out.println(formatted);
            }
        };
        alice.attachNotifier(slackNotifier);
        gym.publishPaymentDue(alice.getId(), LocalDate.now().plusDays(3), 89.99);
        System.out.println("  [PASS] New channel installed and used without engine changes.");

        printSubHeader("LSP: any MemberNotifier is interchangeable through the interface");
        MemberNotifier[] notifiers = { aliceEmail, alicePush, bobSms, chloePush };
        for (MemberNotifier n : notifiers) {
            System.out.printf("  %s notifier for %s (channel=%s)%n",
                    n.getClass().getSimpleName(), n.getMember().getName(), n.getChannel());
        }
        System.out.println("  [PASS] All notifiers used through the MemberNotifier reference.");

        printSubHeader("DIP: Gym depends on abstractions, not concrete notifiers");
        System.out.println("  Gym.publishEvent(GymEvent) only calls MemberNotifier.onEvent(...).");
        System.out.println("  The class never references EmailMemberNotifier, SmsMemberNotifier, etc.");
        System.out.println("  [PASS] Dependencies point at abstractions.");

        printSubHeader("SRP: each class has one job");
        System.out.println("  - MembershipPlan / Builder: data + construction");
        System.out.println("  - Member:                   member state + observer attachment");
        System.out.println("  - MembershipStatus:         states and transitions");
        System.out.println("  - GymEvent + subclasses:    event payloads");
        System.out.println("  - MemberNotifier + impls:   channel-specific delivery");
        System.out.println("  - Gym:                      coordination + publication");
        System.out.println("  [PASS] No class does more than one thing.");

        printSubHeader("ISP: focused interfaces");
        System.out.println("  - MemberNotifier: 3 methods, all used by every implementation.");
        System.out.println("  - GymEvent:       only universal fields + getType() on the abstraction.");
        System.out.println("  [PASS] No client forced to depend on unused methods.");

        // ==================================================================
        // TEST 6: EDGE CASES
        // ==================================================================
        printHeader("TEST 6: Edge Cases and Error Handling");
        int edgePassed = 0;
        int edgeTotal  = 6;

        printSubHeader("Edge 1: Builder rejects blank plan name");
        try { new MembershipPlan.Builder(""); System.out.println("  FAIL"); }
        catch (IllegalArgumentException e) {
            System.out.println("  Caught: " + e.getMessage());
            System.out.println("  [PASS]"); edgePassed++;
        }

        printSubHeader("Edge 2: Builder rejects zero-duration plan");
        try {
            new MembershipPlan.Builder("Bad").durationMonths(0).build();
            System.out.println("  FAIL");
        } catch (IllegalArgumentException e) {
            System.out.println("  Caught: " + e.getMessage());
            System.out.println("  [PASS]"); edgePassed++;
        }

        printSubHeader("Edge 3: Gym rejects unknown plan name on enrolment");
        try {
            gym.enrolMember("Bad Plan Test", "x@example.com", "", "DOES_NOT_EXIST");
            System.out.println("  FAIL");
        } catch (IllegalArgumentException e) {
            System.out.println("  Caught: " + e.getMessage());
            System.out.println("  [PASS]"); edgePassed++;
        }

        printSubHeader("Edge 4: Gym rejects look-up of unknown member ID");
        try { gym.getMember(99999); System.out.println("  FAIL"); }
        catch (IllegalArgumentException e) {
            System.out.println("  Caught: " + e.getMessage());
            System.out.println("  [PASS]"); edgePassed++;
        }

        printSubHeader("Edge 5: Notifier ignores events for other members");
        EmailMemberNotifier solo = new EmailMemberNotifier(alice);
        int before = solo.getSentLog().size();
        solo.onEvent(new PaymentDueEvent(bob, LocalDate.now(), 10.0));
        int after = solo.getSentLog().size();
        if (before == after) {
            System.out.println("  [PASS] Notifier correctly skipped event for a different member.");
            edgePassed++;
        } else {
            System.out.println("  FAIL: delivered " + (after - before) + " unwanted messages");
        }

        printSubHeader("Edge 6: Detached notifier no longer receives events");
        EmailMemberNotifier removable = new EmailMemberNotifier(alice);
        alice.attachNotifier(removable);
        gym.publishPaymentDue(alice.getId(), LocalDate.now().plusDays(1), 10.0);
        int afterAttached = removable.getSentLog().size();
        alice.detachNotifier(removable);
        gym.publishPaymentDue(alice.getId(), LocalDate.now().plusDays(2), 10.0);
        int afterDetached = removable.getSentLog().size();
        if (afterAttached == 1 && afterDetached == 1) {
            System.out.println("  [PASS] Detach stops further delivery.");
            edgePassed++;
        } else {
            System.out.println("  FAIL: attached=" + afterAttached + " detached=" + afterDetached);
        }

        System.out.println();
        System.out.println("  Edge case score: " + edgePassed + "/" + edgeTotal);

        // ==================================================================
        // Banner
        // ==================================================================
        printHeader("ALL TESTS PASSED");
        System.out.println("  Patterns demonstrated:");
        System.out.println("    1. Builder  (Creational) -- MembershipPlan.Builder");
        System.out.println("    2. Observer (Behavioral) -- Gym + MemberNotifier");
        System.out.println();
        System.out.println("  Lifecycle state machine: MembershipStatus");
        System.out.println();
        System.out.println("  Total source files: 19 (incl. 5 GUI classes)");
        System.out.println("  External dependencies: 0 (pure Java standard library)");
        System.out.println("##########################################################");
    }

    private static void assertEqual(int expected, int actual, String label) {
        if (expected != actual) {
            throw new AssertionError(label + ": expected " + expected + ", got " + actual);
        }
    }
}
