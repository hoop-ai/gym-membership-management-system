import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Coordinator class for the Gym Membership Management System.
 *
 * <p>{@code Gym} ties together the {@link Member}s, the catalogue of
 * {@link MembershipPlan}s, and the {@link GymEvent}-based notification fabric.
 * It plays two design-pattern roles at once:</p>
 *
 * <ul>
 *   <li><strong>Builder client.</strong> Receives plans that callers have
 *       built using {@link MembershipPlan.Builder}. The gym itself never
 *       constructs plans positionally -- it works entirely with fully-built
 *       immutable {@link MembershipPlan} instances.</li>
 *   <li><strong>Observer subject.</strong> Holds a global registry of every
 *       {@link MemberNotifier} attached to any member, and dispatches
 *       {@link GymEvent}s to them via {@link #publishEvent(GymEvent)}.
 *       Members never call notifiers directly -- the gym is the single
 *       publication point, which is the central guarantee of the Observer
 *       pattern.</li>
 * </ul>
 *
 * <h3>Why one publication point?</h3>
 * <p>Concentrating the publish step here keeps every notification path
 * uniform: filtering rules, gym-wide auditing, broadcast detection, and any
 * future cross-cutting policy (rate-limiting, quiet hours, opt-outs) live in
 * one place. Members and notifiers stay simple.</p>
 *
 * <h3>SOLID Principles Demonstrated</h3>
 * <ul>
 *   <li><strong>Single Responsibility.</strong> Coordinates -- delegates
 *       construction to {@link MembershipPlan.Builder}, delivery to
 *       {@link MemberNotifier}, and state transitions to {@link Member} /
 *       {@link MembershipStatus}.</li>
 *   <li><strong>Open/Closed.</strong> Adding a new plan, a new notifier
 *       channel, or a new event type requires zero changes here.</li>
 *   <li><strong>Dependency Inversion.</strong> Holds references to the
 *       abstractions ({@link MembershipPlan}, {@link Member},
 *       {@link MemberNotifier}, {@link GymEvent}) -- never to a concrete
 *       notifier or event subclass.</li>
 * </ul>
 */
public class Gym {

    private final String name;
    private final List<Member> members = new ArrayList<>();
    private final Map<String, MembershipPlan> planCatalogue = new HashMap<>();

    /** Append-only journal of every event published, used by tests and the GUI log. */
    private final List<GymEvent> eventJournal = new ArrayList<>();

    public Gym(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Gym name must not be null or blank.");
        }
        this.name = name;
    }

    public String getName() {
        return name;
    }

    // -----------------------------------------------------------------------
    // Plan catalogue (Builder client)
    // -----------------------------------------------------------------------

    /**
     * Registers an immutable {@link MembershipPlan} under its display name.
     * The plan must have been built via {@link MembershipPlan.Builder}.
     *
     * @param plan a fully-built plan
     */
    public void registerPlan(MembershipPlan plan) {
        if (plan == null) {
            throw new IllegalArgumentException("Plan must not be null.");
        }
        planCatalogue.put(plan.getName(), plan);
    }

    /**
     * Looks up a registered plan by name.
     *
     * @param name the plan name
     * @return the plan
     * @throws IllegalArgumentException if no plan with that name is registered
     */
    public MembershipPlan getPlan(String name) {
        MembershipPlan plan = planCatalogue.get(name);
        if (plan == null) {
            throw new IllegalArgumentException(
                    "No plan registered under name '" + name + "'. Available: "
                            + planCatalogue.keySet());
        }
        return plan;
    }

    public List<MembershipPlan> getAllPlans() {
        return new ArrayList<>(planCatalogue.values());
    }

    // -----------------------------------------------------------------------
    // Member management
    // -----------------------------------------------------------------------

    /**
     * Enrols a member onto a named plan and returns the new {@link Member}.
     *
     * @param name      the member's name
     * @param email     the member's email
     * @param phone     the member's phone number (may be empty)
     * @param planName  the name of an already-registered plan
     * @return the newly-created member, also added to this gym
     */
    public Member enrolMember(String name, String email, String phone, String planName) {
        MembershipPlan plan = getPlan(planName);
        Member m = new Member(name, email, phone, plan);
        members.add(m);
        return m;
    }

    public Member getMember(int id) {
        for (Member m : members) {
            if (m.getId() == id) return m;
        }
        throw new IllegalArgumentException("No member found with ID: " + id);
    }

    public List<Member> getAllMembers() {
        return Collections.unmodifiableList(members);
    }

    public void removeMember(int id) {
        Member m = getMember(id);
        members.remove(m);
    }

    public void changeMemberStatus(int memberId, MembershipStatus newStatus) {
        getMember(memberId).setStatus(newStatus);
    }

    // -----------------------------------------------------------------------
    // Observer pattern -- publish events to attached notifiers
    // -----------------------------------------------------------------------

    /**
     * Publishes one event to every relevant {@link MemberNotifier}.
     *
     * <ul>
     *   <li><strong>Targeted event</strong> (a specific {@code targetMember}):
     *       only that member's attached notifiers receive it.</li>
     *   <li><strong>Broadcast event</strong> (no target): every notifier
     *       attached to <strong>any</strong> member receives it.</li>
     * </ul>
     *
     * <p>Every event is also recorded in the gym-wide event journal, so the
     * GUI can render a chronological log.</p>
     *
     * @param event the event to publish (must not be null)
     */
    public void publishEvent(GymEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("Event must not be null.");
        }
        eventJournal.add(event);

        if (event.isBroadcast()) {
            // Broadcast -- everyone with at least one notifier hears it.
            for (Member m : members) {
                for (MemberNotifier n : m.getNotifiers()) {
                    n.onEvent(event);
                }
            }
        } else {
            // Targeted -- only the target member's notifiers fire.
            Member target = event.getTargetMember();
            for (MemberNotifier n : target.getNotifiers()) {
                n.onEvent(event);
            }
        }
    }

    /** Returns an unmodifiable view of every event ever published. */
    public List<GymEvent> getEventJournal() {
        return Collections.unmodifiableList(eventJournal);
    }

    // -----------------------------------------------------------------------
    // Convenience event publishers (Subject helpers)
    // -----------------------------------------------------------------------

    public void publishPaymentDue(int memberId, LocalDate dueDate, double amount) {
        publishEvent(new PaymentDueEvent(getMember(memberId), dueDate, amount));
    }

    public void publishRenewalReminder(int memberId) {
        Member m = getMember(memberId);
        publishEvent(new RenewalReminderEvent(m, m.getRenewalDate()));
    }

    public void publishClassCancellation(String className, LocalDate classDate) {
        publishEvent(new ClassCancelledEvent(null, className, classDate));
    }

    public void publishPromotion(double discountPercent, String message) {
        publishEvent(new PromotionEvent(discountPercent, message));
    }

    // -----------------------------------------------------------------------
    // Reporting
    // -----------------------------------------------------------------------

    /**
     * Returns a short, human-readable summary of the gym's current state.
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Gym: %s%n", name));
        sb.append(String.format("  Members:        %d%n", members.size()));
        sb.append(String.format("  Plans on offer: %d%n", planCatalogue.size()));
        sb.append(String.format("  Events emitted: %d%n", eventJournal.size()));
        for (MembershipStatus s : MembershipStatus.values()) {
            int count = 0;
            for (Member m : members) {
                if (m.getStatus() == s) count++;
            }
            if (count > 0) {
                sb.append(String.format("    - %s: %d%n", s, count));
            }
        }
        return sb.toString();
    }
}
