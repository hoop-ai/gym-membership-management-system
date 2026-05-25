/**
 * Observer side of the Observer pattern.
 *
 * <p><strong>Pattern role: Observer interface.</strong> Anything that wants
 * to be notified of {@link Gym} events implements this interface and
 * registers via {@link Gym#addObserver(GymEventObserver)}. The subject
 * ({@link Gym}) is the single point of publication; observers never publish.
 * </p>
 *
 * <p>Concrete observers in this project:</p>
 * <ul>
 *   <li>{@link ConsoleObserver} — prints to stdout.</li>
 *   <li>{@link AuditFileObserver} — appends to a log file.</li>
 *   <li>{@link InMemoryJournalObserver} — keeps an in-memory list for later
 *       inspection.</li>
 * </ul>
 *
 * <p>Implementations must not throw checked exceptions and should not throw
 * unchecked ones either — they swallow or log internally. The contract is:
 * "react to events; never break the publisher."</p>
 */
public interface GymEventObserver {

    /**
     * Called by the {@link Gym} for every event published.
     *
     * @param event the event; never {@code null}
     */
    void onEvent(GymEvent event);
}
