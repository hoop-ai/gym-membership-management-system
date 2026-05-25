import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Concrete observer that appends every event to a log file.
 *
 * <p>Opens and closes the file on each event for simplicity — no resource
 * leaks, no flush worries. For a tiny demo this is fine; a production
 * system would buffer.</p>
 *
 * <p><strong>Important contract:</strong> this observer must never throw an
 * exception out of {@link #onEvent(GymEvent)}. If the file cannot be opened
 * or written, we print a single one-line warning to stderr and swallow the
 * error. The demo cannot crash because of a file write.</p>
 */
public final class AuditFileObserver implements GymEventObserver {

    /** Default path used by the no-arg constructor. */
    public static final String DEFAULT_PATH = "audit.log";

    private final String path;
    private boolean warnedOnce = false;

    /** Uses {@link #DEFAULT_PATH} ("audit.log" in the working directory). */
    public AuditFileObserver() {
        this(DEFAULT_PATH);
    }

    /**
     * Writes to a custom path.
     *
     * @param path non-blank file path
     */
    public AuditFileObserver(String path) {
        if (path == null || path.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Cannot create AuditFileObserver: path must not be blank.");
        }
        this.path = path.trim();
    }

    public String getPath() { return path; }

    @Override
    public void onEvent(GymEvent event) {
        if (event == null) return;
        BufferedWriter w = null;
        try {
            // append mode
            w = new BufferedWriter(new FileWriter(path, true));
            w.write(event.toString());
            w.newLine();
            w.flush();
        } catch (IOException ioe) {
            // Print once, then swallow further failures silently.
            if (!warnedOnce) {
                System.err.println("[AuditFileObserver] warning: could not write to '"
                    + path + "' (" + ioe.getMessage() + "). Continuing.");
                warnedOnce = true;
            }
        } finally {
            if (w != null) {
                try {
                    w.close();
                } catch (IOException ignored) {
                    // best-effort close
                }
            }
        }
    }
}
