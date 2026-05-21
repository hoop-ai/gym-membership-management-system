import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Pre-built scenarios that mirror the data setups in {@link Main}'s test
 * sections. The GUI binds these to menu items so the professor can load a
 * canonical demo with one click and then explore the same data set
 * interactively.
 *
 * <p>Each method first calls {@link #clearAll(Gym)} so the demos compose
 * cleanly even if the user has been clicking around beforehand.</p>
 */
public final class DemoScenarios {

    private DemoScenarios() {}

    /** Removes every member from the gym (the plan catalogue is preserved). */
    public static void clearAll(Gym gym) {
        List<Integer> ids = new ArrayList<>();
        for (Member m : gym.getAllMembers()) ids.add(m.getId());
        for (int id : ids) gym.removeMember(id);
    }

    /**
     * Registers the three sample plans on an empty gym, no-op if they
     * already exist.
     */
    public static void seedPlans(Gym gym) {
        if (gym.getAllPlans().size() >= 3) return;
        gym.registerPlan(new MembershipPlan.Builder("Basic Monthly")
                .durationMonths(1).monthlyFee(29.99).accessTier(AccessTier.BASIC).build());
        gym.registerPlan(new MembershipPlan.Builder("Standard Six-Month")
                .durationMonths(6).monthlyFee(49.99).accessTier(AccessTier.STANDARD)
                .includesClass("Yoga").includesClass("Spinning").freezeDaysPerYear(30).build());
        gym.registerPlan(new MembershipPlan.Builder("Premium Annual")
                .durationMonths(12).monthlyFee(89.99).accessTier(AccessTier.PREMIUM)
                .includesClass("Yoga").includesClass("Spinning").includesClass("HIIT")
                .includesClass("Pilates").guestPassesPerMonth(4).freezeDaysPerYear(60)
                .personalTrainerIncluded(true).build());
    }

    /** Mirrors Main test 2: three members with mixed channels and a couple of events. */
    public static void loadObserverDemo(Gym gym) {
        seedPlans(gym);
        clearAll(gym);

        Member alice = gym.enrolMember("Alice Aydin",   "alice@example.com", "+90-555-0001", "Premium Annual");
        Member bob   = gym.enrolMember("Bob Bektas",    "bob@example.com",   "+90-555-0002", "Standard Six-Month");
        Member chloe = gym.enrolMember("Chloe Celikel", "chloe@example.com", "+90-555-0003", "Basic Monthly");

        alice.attachNotifier(new EmailMemberNotifier(alice));
        alice.attachNotifier(new PushMemberNotifier(alice));

        bob.attachNotifier(new SmsMemberNotifier(bob));

        chloe.attachNotifier(new EmailMemberNotifier(chloe));
        chloe.attachNotifier(new SmsMemberNotifier(chloe));
        chloe.attachNotifier(new PushMemberNotifier(chloe));

        gym.publishPaymentDue(alice.getId(), LocalDate.now().plusDays(7), 89.99);
        bob.setStatus(MembershipStatus.ACTIVE);
        bob.setStatus(MembershipStatus.EXPIRING);
        gym.publishRenewalReminder(bob.getId());
        gym.publishClassCancellation("Spinning", LocalDate.now().plusDays(1));
        gym.publishPromotion(20.0, "20% off Premium plans until the end of the month!");
    }

    /** Mirrors Main test 3: a single member ready for status-transition demos. */
    public static void loadLifecycleDemo(Gym gym) {
        seedPlans(gym);
        clearAll(gym);
        gym.enrolMember("Dilan Demir", "dilan@example.com", "", "Standard Six-Month");
    }

    /** A small mixed-status snapshot used as a generic "load some data" option. */
    public static void loadIntegrationDemo(Gym gym) {
        seedPlans(gym);
        clearAll(gym);

        Member g1 = gym.enrolMember("Guest One",   "g1@example.com", "", "Basic Monthly");
        Member g2 = gym.enrolMember("Guest Two",   "g2@example.com", "", "Standard Six-Month");
        Member g3 = gym.enrolMember("Guest Three", "g3@example.com", "", "Premium Annual");
        Member g4 = gym.enrolMember("Guest Four",  "g4@example.com", "", "Premium Annual");

        g1.attachNotifier(new EmailMemberNotifier(g1));
        g2.attachNotifier(new SmsMemberNotifier(g2));
        g3.attachNotifier(new PushMemberNotifier(g3));
        g4.attachNotifier(new EmailMemberNotifier(g4));
        g4.attachNotifier(new SmsMemberNotifier(g4));

        g1.setStatus(MembershipStatus.ACTIVE);
        g2.setStatus(MembershipStatus.ACTIVE);
        g2.setStatus(MembershipStatus.FROZEN);
        g3.setStatus(MembershipStatus.ACTIVE);
        g4.setStatus(MembershipStatus.ACTIVE);
        g4.setStatus(MembershipStatus.EXPIRING);

        gym.publishPaymentDue(g1.getId(), LocalDate.now().plusDays(5), 29.99);
        gym.publishRenewalReminder(g4.getId());
        gym.publishPromotion(15.0, "Refer a friend and save 15%.");
    }
}
