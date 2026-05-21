import java.time.LocalDate;

/**
 * Event raised when a scheduled group class is cancelled.
 *
 * <p>A class cancellation can be either a broadcast (no specific target --
 * every interested member is notified) or targeted at the members who were
 * registered for that class. The constructor accepts {@code null} for the
 * target member to model the broadcast case.</p>
 *
 * <h3>Design Pattern Role</h3>
 * <p>Concrete event data in the Observer pattern -- see {@link GymEvent}.</p>
 */
public class ClassCancelledEvent extends GymEvent {

    /** The class name (e.g., {@code "Yoga"}, {@code "Spinning"}). */
    private final String className;

    /** The date on which the class was scheduled. */
    private final LocalDate classDate;

    /**
     * @param targetMember the affected member, or {@code null} for a broadcast
     * @param className    the class name (must not be null or blank)
     * @param classDate    the originally-scheduled date (must not be null)
     */
    public ClassCancelledEvent(Member targetMember, String className, LocalDate classDate) {
        super(targetMember,
                String.format("The %s class scheduled for %s has been cancelled.",
                        className, classDate));
        if (className == null || className.isBlank()) {
            throw new IllegalArgumentException("Class name must not be null or blank.");
        }
        if (classDate == null) {
            throw new IllegalArgumentException("Class date must not be null.");
        }
        this.className = className;
        this.classDate = classDate;
    }

    public String    getClassName() { return className; }
    public LocalDate getClassDate() { return classDate; }

    @Override
    public String getType() {
        return "CLASS_CANCELLED";
    }
}
