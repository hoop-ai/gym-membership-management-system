import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Abstract base for every event the {@link Gym} publishes.
 *
 * <p><strong>Pattern role: Observer (Behavioral, GoF) — event payload.</strong>
 * Each concrete subclass carries the payload specific to one kind of change
 * (member added, class removed, enrolment, etc.). Observers receive these
 * events through {@link GymEventObserver#onEvent(GymEvent)} and react however
 * they like (print, log to a file, journal in memory).</p>
 *
 * <p>All events are immutable. The {@link #timestamp} is set at construction
 * to {@link LocalDateTime#now()} — the time the event was raised, not the
 * time it is observed. The {@link #type} string is a stable identifier
 * ({@code "MEMBER_ADDED"}, {@code "CLASS_ADDED"}, etc.) chosen by the
 * concrete subclass.</p>
 */
public abstract class GymEvent {

    /** Formatter used in the default {@link #toString()} output. */
    private static final DateTimeFormatter TIME_FMT =
        DateTimeFormatter.ofPattern("HH:mm:ss");

    /** Time at which the event was raised. */
    protected final LocalDateTime timestamp;

    /** Stable identifier for the kind of event (set by subclass). */
    protected final String type;

    /** Human-readable summary of what happened. */
    protected final String message;

    /**
     * Initialises the shared fields. Concrete subclasses call this from
     * their own constructors.
     *
     * @param type    stable event identifier, non-blank
     * @param message human-readable summary, non-blank
     */
    protected GymEvent(String type, String message) {
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Cannot create event: type must not be blank.");
        }
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Cannot create event: message must not be blank.");
        }
        this.timestamp = LocalDateTime.now();
        this.type      = type;
        this.message   = message;
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public String        getType()      { return type; }
    public String        getMessage()   { return message; }

    /**
     * Default presentation: {@code [HH:MM:SS] [TYPE] message}.
     * Used by {@link ConsoleObserver} and {@link AuditFileObserver}.
     */
    @Override
    public String toString() {
        return String.format("[%s] [%s] %s",
            timestamp.format(TIME_FMT), type, message);
    }
}
