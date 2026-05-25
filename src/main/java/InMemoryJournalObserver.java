import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Concrete observer that accumulates events in memory for later inspection.
 *
 * <p>Used by {@link Main} to dump every event captured during the demo run.
 * In a real application, an in-memory journal might be used for tests, for
 * an admin "recent activity" panel, or for replaying events.</p>
 */
public final class InMemoryJournalObserver implements GymEventObserver {

    private final List<GymEvent> journal = new ArrayList<GymEvent>();

    @Override
    public void onEvent(GymEvent event) {
        if (event == null) return;
        journal.add(event);
    }

    /** @return unmodifiable view of every event captured so far. */
    public List<GymEvent> getJournal() {
        return Collections.unmodifiableList(journal);
    }

    /** @return total events recorded. */
    public int size() {
        return journal.size();
    }

    /** Clears the journal. */
    public void clear() {
        journal.clear();
    }
}
