import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Scripted narrated demo of the Gym Membership Management System.
 *
 * <p>This is the only entry point. Running it produces seven labelled
 * sections of output covering:</p>
 * <ol>
 *   <li>Gym setup and observer attachment.</li>
 *   <li>Building fitness classes via the Builder pattern.</li>
 *   <li>Adding members.</li>
 *   <li>Enrolling members in classes — events fan out to observers.</li>
 *   <li>Drops and removals.</li>
 *   <li>Three intentional errors, each caught and reported.</li>
 *   <li>Dump of the in-memory journal collected by
 *       {@link InMemoryJournalObserver}.</li>
 * </ol>
 *
 * <p>The demo completes in well under one second and always returns exit
 * code 0. No printStackTrace, no crash, no surprise.</p>
 */
public final class Main {

    private static final String HR_LINE =
        "========================================================================";

    private static final DateTimeFormatter TIME_FMT =
        DateTimeFormatter.ofPattern("HH:mm:ss");

    /** Truncate audit.log at the start so each run produces a fresh file. */
    private static final String AUDIT_PATH = "audit.log";

    public static void main(String[] args) {
        // Make stdout/stderr UTF-8 so em-dashes and check marks survive
        // redirection on Windows consoles that default to cp1252.
        try {
            System.setOut(new PrintStream(System.out, true, "UTF-8"));
            System.setErr(new PrintStream(System.err, true, "UTF-8"));
        } catch (UnsupportedEncodingException ignored) {
            // every JVM supports UTF-8; this catch satisfies the compiler.
        }

        // Reset audit.log so the demo always shows a fresh capture.
        resetAuditLog();

        printBanner();

        // ----------------------------------------------------------------
        // 1. Setting up the gym
        // ----------------------------------------------------------------
        printSection("1. Setting up the gym");

        Gym gym = new Gym("FitLife Centre");

        ConsoleObserver         console = new ConsoleObserver();
        AuditFileObserver       audit   = new AuditFileObserver(AUDIT_PATH);
        InMemoryJournalObserver journal = new InMemoryJournalObserver();

        gym.addObserver(console);
        gym.addObserver(audit);
        gym.addObserver(journal);

        System.out.println("Created gym: " + gym.getName());
        System.out.println("Attached " + gym.observerCount() +
            " observers: Console, AuditFile (" + audit.getPath() +
            "), InMemoryJournal");

        // ----------------------------------------------------------------
        // 2. Adding fitness classes (Builder pattern)
        // ----------------------------------------------------------------
        printSection("2. Adding fitness classes (Builder pattern)");

        FitnessClass yoga = new FitnessClass.Builder("Yoga Flow", "Sarah Lin")
            .dayOfWeek(DayOfWeek.MONDAY)
            .startTime(LocalTime.of(18, 30))
            .durationMinutes(60)
            .capacity(20)
            .room("Studio A")
            .difficulty(Difficulty.BEGINNER)
            .addEquipment("Yoga mat")
            .addEquipment("Block")
            .description("Slow-flow vinyasa, all levels welcome.")
            .build();

        FitnessClass spin = new FitnessClass.Builder("Spin Express", "Tom Reyes")
            .dayOfWeek(DayOfWeek.TUESDAY)
            .startTime(LocalTime.of(7, 0))
            .durationMinutes(30)
            .capacity(15)
            .room("Cycle Studio")
            .difficulty(Difficulty.INTERMEDIATE)
            .addEquipment("Indoor bike")
            .description("High-energy 30-minute spin.")
            .build();

        FitnessClass hiit = new FitnessClass.Builder("HIIT 45", "Aisha Patel")
            .dayOfWeek(DayOfWeek.WEDNESDAY)
            .startTime(LocalTime.of(19, 0))
            .durationMinutes(45)
            .capacity(2)                // small on purpose, used for the "full" error demo
            .room("Main floor")
            .difficulty(Difficulty.ADVANCED)
            .addEquipment("Dumbbells")
            .addEquipment("Box")
            .description("Interval training, advanced.")
            .build();

        gym.addClass(yoga);
        gym.addClass(spin);
        gym.addClass(hiit);

        // ----------------------------------------------------------------
        // 3. Adding members
        // ----------------------------------------------------------------
        printSection("3. Adding members");

        Member sarah = gym.addMember("Sarah Connor",  "sarah@fitlife.example");
        Member tom   = gym.addMember("Tom Hardy",     "tom@fitlife.example");
        Member aisha = gym.addMember("Aisha Tyler",   "aisha@fitlife.example");

        // ----------------------------------------------------------------
        // 4. Enrolling members in classes (Observer pattern)
        // ----------------------------------------------------------------
        printSection("4. Enrolling members in classes (Observer pattern)");

        gym.enrolMemberInClass(sarah.getId(), "Yoga Flow");
        gym.enrolMemberInClass(tom.getId(),   "Spin Express");
        gym.enrolMemberInClass(aisha.getId(), "HIIT 45");
        // case-insensitive lookup — passes "hiit 45" lowercase on purpose
        gym.enrolMemberInClass(sarah.getId(), "hiit 45");

        // ----------------------------------------------------------------
        // 5. Drops and removals
        // ----------------------------------------------------------------
        printSection("5. Drops and removals");

        gym.dropMemberFromClass(tom.getId(), "Spin Express");
        gym.removeMember(tom.getId());

        // ----------------------------------------------------------------
        // 6. Error handling demo
        // ----------------------------------------------------------------
        printSection("6. Error handling demo (each error is caught, no crash)");

        // 6a. Missing member id
        try {
            gym.getMember(99);
        } catch (IllegalArgumentException ex) {
            System.out.println("✓ Caught: " + ex.getMessage());
        }

        // 6b. Duplicate class name (case-insensitive)
        try {
            FitnessClass dup = new FitnessClass.Builder("yoga flow", "Other Instructor").build();
            gym.addClass(dup);
        } catch (IllegalArgumentException ex) {
            System.out.println("✓ Caught: " + ex.getMessage());
        }

        // 6c. Double-enrolment
        try {
            gym.enrolMemberInClass(sarah.getId(), "Yoga Flow");
        } catch (IllegalArgumentException ex) {
            System.out.println("✓ Caught: " + ex.getMessage());
        }

        // ----------------------------------------------------------------
        // 7. Final journal dump
        // ----------------------------------------------------------------
        printSection("7. Final journal dump (InMemoryJournalObserver)");

        System.out.println(journal.size() + " events captured:");
        for (GymEvent e : journal.getJournal()) {
            System.out.println("  [" + e.getTimestamp().format(TIME_FMT) + "] [" +
                padRight(e.getType(), 16) + "] " + e.getMessage());
        }

        // ----------------------------------------------------------------
        // Done
        // ----------------------------------------------------------------
        printSection("Done");
        System.out.println(AUDIT_PATH + " written with " + countLines(AUDIT_PATH) + " lines.");
    }

    // ---------------------------------------------------------------------
    // Output helpers
    // ---------------------------------------------------------------------

    private static void printBanner() {
        System.out.println(HR_LINE);
        System.out.println("   GYM MEMBERSHIP MANAGEMENT SYSTEM — Builder + Observer Demo");
        System.out.println(HR_LINE);
    }

    /**
     * Prints a section header in the form {@code "--- N. Title ---..."} with
     * trailing dashes padded to a stable width (72 chars) so each header
     * lines up visually.
     */
    private static void printSection(String title) {
        final int width = 72;
        String head = "--- " + title + " ";
        StringBuilder sb = new StringBuilder();
        sb.append('\n').append(head);
        for (int i = head.length(); i < width; i++) sb.append('-');
        System.out.println(sb.toString());
    }

    private static String padRight(String s, int width) {
        if (s == null) s = "";
        if (s.length() >= width) return s;
        StringBuilder sb = new StringBuilder(s);
        for (int i = s.length(); i < width; i++) sb.append(' ');
        return sb.toString();
    }

    // ---------------------------------------------------------------------
    // audit.log helpers
    // ---------------------------------------------------------------------

    /** Removes any pre-existing audit.log so each run starts from zero. */
    private static void resetAuditLog() {
        File f = new File(AUDIT_PATH);
        if (f.exists() && !f.delete()) {
            System.err.println("[Main] warning: could not delete '"
                + AUDIT_PATH + "'. Continuing.");
        }
    }

    /** Counts lines in the given file, or returns 0 if it doesn't exist. */
    private static int countLines(String path) {
        File f = new File(path);
        if (!f.exists()) return 0;
        int count = 0;
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(f, "r");
            String line;
            while ((line = raf.readLine()) != null) {
                count++;
            }
        } catch (IOException ioe) {
            System.err.println("[Main] warning: could not read '"
                + path + "' (" + ioe.getMessage() + ")");
        } finally {
            if (raf != null) {
                try { raf.close(); } catch (IOException ignored) { /* best-effort */ }
            }
        }
        return count;
    }
}
