import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a gym member.
 *
 * <p>A member has an auto-assigned id, a name, contact details, an active
 * {@link MembershipPlan}, a lifecycle {@link MembershipStatus}, a join date,
 * and a renewal date computed from the plan's duration. Members also keep an
 * internal list of attached {@link MemberNotifier} instances -- one notifier
 * per active communication channel (email, SMS, push, etc.).</p>
 *
 * <h3>Design Pattern Roles</h3>
 * <ul>
 *   <li><strong>Observer pattern -- carrier of observers.</strong> Each
 *       member owns a list of {@link MemberNotifier} instances that listen to
 *       gym-wide events. When the {@link Gym} publishes an event addressed to
 *       this member, every attached notifier is invoked.</li>
 *   <li><strong>State pattern (light).</strong> The member's lifecycle is
 *       governed by {@link MembershipStatus}'s transition rules; invalid
 *       transitions throw {@link IllegalArgumentException}.</li>
 * </ul>
 */
public class Member {

    // -----------------------------------------------------------------------
    // Auto-incrementing ID generator
    // -----------------------------------------------------------------------

    private static int idCounter = 0;

    // -----------------------------------------------------------------------
    // Instance fields
    // -----------------------------------------------------------------------

    private final int       id;
    private final String    name;
    private final String    email;
    private final String    phone;
    private final LocalDateTime joinedAt;

    private MembershipPlan       plan;
    private MembershipStatus     status;
    private LocalDate            renewalDate;

    private final List<MemberNotifier> notifiers = new ArrayList<>();

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    /**
     * Creates a new member in {@link MembershipStatus#PENDING} status, with
     * the supplied plan assigned and the renewal date computed from the plan's
     * duration.
     *
     * @param name  full name (must not be null or blank)
     * @param email email address (must not be null or blank)
     * @param phone phone number (may be blank but not null)
     * @param plan  the membership plan (must not be null)
     */
    public Member(String name, String email, String phone, MembershipPlan plan) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name must not be null or blank.");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email must not be null or blank.");
        }
        if (phone == null) {
            throw new IllegalArgumentException("Phone must not be null (use \"\" if unknown).");
        }
        if (plan == null) {
            throw new IllegalArgumentException("Plan must not be null.");
        }
        this.id          = ++idCounter;
        this.name        = name;
        this.email       = email;
        this.phone       = phone;
        this.joinedAt    = LocalDateTime.now();
        this.plan        = plan;
        this.status      = MembershipStatus.PENDING;
        this.renewalDate = LocalDate.now().plusMonths(plan.getDurationMonths());
    }

    // -----------------------------------------------------------------------
    // Accessors
    // -----------------------------------------------------------------------

    public int               getId()          { return id; }
    public String            getName()        { return name; }
    public String            getEmail()       { return email; }
    public String            getPhone()       { return phone; }
    public LocalDateTime     getJoinedAt()    { return joinedAt; }
    public MembershipPlan    getPlan()        { return plan; }
    public MembershipStatus  getStatus()      { return status; }
    public LocalDate         getRenewalDate() { return renewalDate; }

    // -----------------------------------------------------------------------
    // Mutators (validated)
    // -----------------------------------------------------------------------

    /**
     * Updates the membership status, enforcing the {@link MembershipStatus}
     * state machine. Side-effect: when moving back to {@code ACTIVE} after a
     * freeze or renewal, the renewal date is pushed forward by the plan
     * duration.
     *
     * @param newStatus the target status (must not be null)
     * @throws IllegalArgumentException if the transition is not allowed
     */
    public void setStatus(MembershipStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("Status must not be null.");
        }
        if (!this.status.canTransitionTo(newStatus)) {
            throw new IllegalArgumentException(
                    "Cannot transition from " + this.status + " to " + newStatus);
        }
        // Renewals and resumes push the renewal date forward.
        if ((this.status == MembershipStatus.EXPIRING || this.status == MembershipStatus.FROZEN)
                && newStatus == MembershipStatus.ACTIVE) {
            this.renewalDate = LocalDate.now().plusMonths(plan.getDurationMonths());
        }
        this.status = newStatus;
    }

    /**
     * Replaces the member's current plan. Useful when a member upgrades or
     * downgrades. Resets the renewal date based on the new plan's duration.
     *
     * @param newPlan the new plan (must not be null)
     */
    public void changePlan(MembershipPlan newPlan) {
        if (newPlan == null) {
            throw new IllegalArgumentException("Plan must not be null.");
        }
        this.plan = newPlan;
        this.renewalDate = LocalDate.now().plusMonths(newPlan.getDurationMonths());
    }

    // -----------------------------------------------------------------------
    // Observer attachment
    // -----------------------------------------------------------------------

    /**
     * Attaches a notifier so this member will receive events through it.
     * Adding the same notifier twice is a no-op.
     *
     * @param notifier the notifier to attach (must not be null)
     */
    public void attachNotifier(MemberNotifier notifier) {
        if (notifier == null) {
            throw new IllegalArgumentException("Notifier must not be null.");
        }
        if (!notifiers.contains(notifier)) {
            notifiers.add(notifier);
        }
    }

    /**
     * Detaches a notifier. Does nothing if the notifier was not attached.
     *
     * @param notifier the notifier to detach
     */
    public void detachNotifier(MemberNotifier notifier) {
        notifiers.remove(notifier);
    }

    /**
     * Returns an unmodifiable view of the notifiers currently attached.
     *
     * @return the live, unmodifiable list of notifiers
     */
    public List<MemberNotifier> getNotifiers() {
        return Collections.unmodifiableList(notifiers);
    }

    @Override
    public String toString() {
        return String.format(
                "Member[id=%d, name='%s', email=%s, status=%s, plan='%s', renewalDate=%s]",
                id, name, email, status, plan.getName(), renewalDate);
    }
}
