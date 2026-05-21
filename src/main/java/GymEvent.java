import java.time.LocalDateTime;

/**
 * Abstract base for every event the {@link Gym} can publish.
 *
 * <p>Every event carries three universal pieces of information: when it was
 * raised, which member it concerns (or {@code null} for gym-wide
 * broadcasts), and a short human-readable message. Concrete subclasses add
 * the type-specific data they need (a due date and amount for payment-due,
 * a class name for cancellations, and so on).</p>
 *
 * <h3>Design Pattern Role -- Observer pattern</h3>
 * <p>{@code GymEvent} (and its subclasses) form the <strong>event data</strong>
 * carried from the {@code Subject} ({@link Gym}) to its {@code Observers}
 * ({@link MemberNotifier} instances). The Observer pattern allows the gym to
 * announce things to interested parties without knowing who they are or how
 * they handle the message, and it allows new channels (email, SMS, push)
 * and new event kinds to be added on either side independently.</p>
 *
 * <h3>SOLID Principles Demonstrated</h3>
 * <ul>
 *   <li><strong>Open/Closed.</strong> Adding a new event kind means adding a
 *       new subclass; no existing code changes.</li>
 *   <li><strong>Liskov Substitution.</strong> Every subclass honours the
 *       common contract (timestamp + targetMember + message), so notifiers
 *       can handle them uniformly.</li>
 *   <li><strong>Single Responsibility.</strong> Each subclass carries one
 *       kind of payload and nothing else.</li>
 * </ul>
 *
 * @see PaymentDueEvent
 * @see RenewalReminderEvent
 * @see ClassCancelledEvent
 * @see PromotionEvent
 * @see MemberNotifier
 * @see Gym
 */
public abstract class GymEvent {

    /** When the event was raised. */
    private final LocalDateTime timestamp;

    /**
     * The member the event targets, or {@code null} if the event is a
     * gym-wide broadcast (e.g., a promotion announcement).
     */
    private final Member targetMember;

    /** Short, human-readable summary. */
    private final String message;

    /**
     * Initialises the shared fields. Subclasses call this from their own
     * constructors.
     *
     * @param targetMember the member affected, or {@code null} for a broadcast
     * @param message      a short summary (must not be null)
     */
    protected GymEvent(Member targetMember, String message) {
        if (message == null) {
            throw new IllegalArgumentException("Message must not be null.");
        }
        this.timestamp    = LocalDateTime.now();
        this.targetMember = targetMember;
        this.message      = message;
    }

    public LocalDateTime getTimestamp()    { return timestamp; }
    public Member        getTargetMember() { return targetMember; }
    public String        getMessage()      { return message; }

    /**
     * Returns {@code true} when this event is a gym-wide broadcast (no
     * specific target member).
     *
     * @return true if {@link #getTargetMember()} is {@code null}
     */
    public boolean isBroadcast() {
        return targetMember == null;
    }

    /**
     * Short, uppercase identifier for this event type
     * ({@code "PAYMENT_DUE"}, {@code "RENEWAL_REMINDER"}, etc.). Concrete
     * subclasses supply their own value.
     *
     * @return the event-type identifier
     */
    public abstract String getType();

    @Override
    public String toString() {
        return String.format("[%s] %s -- %s",
                getType(),
                targetMember == null ? "broadcast" : targetMember.getName(),
                message);
    }
}
