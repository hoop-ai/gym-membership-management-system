import java.time.LocalDate;

/**
 * Event raised when a member's recurring payment is due.
 *
 * <p>Carries the due date and the amount in addition to the inherited
 * timestamp / target / message fields. Always targets a specific member --
 * payment-due events are never broadcasts.</p>
 *
 * <h3>Design Pattern Role</h3>
 * <p>A concrete <strong>event data</strong> class in the Observer pattern.
 * Notifiers ({@link EmailMemberNotifier}, {@link SmsMemberNotifier},
 * {@link PushMemberNotifier}) receive instances of this class and format an
 * appropriate channel-specific message.</p>
 *
 * @see GymEvent
 */
public class PaymentDueEvent extends GymEvent {

    /** The day the payment must be received by. */
    private final LocalDate dueDate;

    /** The amount due, in the gym's local currency. */
    private final double amount;

    /**
     * @param targetMember the member who owes the payment (must not be null)
     * @param dueDate      the payment due date (must not be null)
     * @param amount       the amount due (must be non-negative)
     */
    public PaymentDueEvent(Member targetMember, LocalDate dueDate, double amount) {
        super(targetMember,
                String.format("Payment of %.2f is due on %s.", amount, dueDate));
        if (targetMember == null) {
            throw new IllegalArgumentException(
                    "PaymentDueEvent requires a target member (it is not a broadcast).");
        }
        if (dueDate == null) {
            throw new IllegalArgumentException("Due date must not be null.");
        }
        if (amount < 0) {
            throw new IllegalArgumentException("Amount must be non-negative, got: " + amount);
        }
        this.dueDate = dueDate;
        this.amount  = amount;
    }

    public LocalDate getDueDate() { return dueDate; }
    public double    getAmount()  { return amount; }

    @Override
    public String getType() {
        return "PAYMENT_DUE";
    }
}
