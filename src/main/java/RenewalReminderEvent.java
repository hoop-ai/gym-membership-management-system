import java.time.LocalDate;

/**
 * Event raised in the days leading up to a member's renewal date.
 *
 * <p>Carries the upcoming renewal date so the notifier can write a useful
 * message (e.g., "your membership expires in 14 days"). Always targets a
 * specific member.</p>
 *
 * <h3>Design Pattern Role</h3>
 * <p>Concrete event data in the Observer pattern -- see {@link GymEvent}.</p>
 */
public class RenewalReminderEvent extends GymEvent {

    /** The date the current membership expires. */
    private final LocalDate renewalDate;

    /**
     * @param targetMember the member whose plan is up for renewal (must not be null)
     * @param renewalDate  the upcoming renewal date (must not be null)
     */
    public RenewalReminderEvent(Member targetMember, LocalDate renewalDate) {
        super(targetMember,
                String.format("Your membership is up for renewal on %s.", renewalDate));
        if (targetMember == null) {
            throw new IllegalArgumentException(
                    "RenewalReminderEvent requires a target member (it is not a broadcast).");
        }
        if (renewalDate == null) {
            throw new IllegalArgumentException("Renewal date must not be null.");
        }
        this.renewalDate = renewalDate;
    }

    public LocalDate getRenewalDate() {
        return renewalDate;
    }

    @Override
    public String getType() {
        return "RENEWAL_REMINDER";
    }
}
