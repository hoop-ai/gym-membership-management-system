import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A scheduled fitness class such as "Yoga Flow" or "Spin Express".
 *
 * <p><strong>Pattern role: Builder (Creational, GoF).</strong> Construction is
 * only possible via the nested {@link Builder}. The class has two required
 * fields ({@code name}, {@code instructor}) and eight optional fields with
 * sensible defaults. A telescoping-constructor approach would require dozens
 * of overloads; setter-based mutation would break immutability. The Builder
 * pattern gives readable call sites and an immutable result.</p>
 *
 * <p>All fields except the enrolled-members list are {@code final}. The
 * enrolled list is mutable but only via package-visible methods, and is
 * exposed externally only as an unmodifiable view. Members are added and
 * removed exclusively by {@link Gym}, which is the single point of event
 * publication.</p>
 */
public final class FitnessClass {

    // -- Immutable schedule + metadata -------------------------------------
    private final String       name;
    private final String       instructor;
    private final DayOfWeek    dayOfWeek;
    private final LocalTime    startTime;
    private final int          durationMinutes;
    private final int          capacity;
    private final String       room;
    private final Difficulty   difficulty;
    private final List<String> equipment;
    private final String       description;

    // -- Mutable enrolment list (managed by Gym only) ----------------------
    private final List<Member> enrolledMembers = new ArrayList<>();

    private FitnessClass(Builder b) {
        this.name            = b.name;
        this.instructor      = b.instructor;
        this.dayOfWeek       = b.dayOfWeek;
        this.startTime       = b.startTime;
        this.durationMinutes = b.durationMinutes;
        this.capacity        = b.capacity;
        this.room            = b.room;
        this.difficulty      = b.difficulty;
        // defensive copy + unmodifiable wrap
        this.equipment       = Collections.unmodifiableList(new ArrayList<String>(b.equipment));
        this.description     = b.description;
    }

    // -- Accessors ---------------------------------------------------------
    public String       getName()            { return name; }
    public String       getInstructor()      { return instructor; }
    public DayOfWeek    getDayOfWeek()       { return dayOfWeek; }
    public LocalTime    getStartTime()       { return startTime; }
    public int          getDurationMinutes() { return durationMinutes; }
    public int          getCapacity()        { return capacity; }
    public String       getRoom()            { return room; }
    public Difficulty   getDifficulty()      { return difficulty; }
    public List<String> getEquipment()       { return equipment; }
    public String       getDescription()     { return description; }

    // -- Enrolment queries -------------------------------------------------

    /** @return true when the class has reached its capacity. */
    public boolean isFull() {
        return enrolledMembers.size() >= capacity;
    }

    /** @return true when the given member is enrolled (by id). */
    public boolean hasMember(Member m) {
        if (m == null) return false;
        for (Member enrolled : enrolledMembers) {
            if (enrolled.getId() == m.getId()) return true;
        }
        return false;
    }

    /** @return unmodifiable view of currently enrolled members. */
    public List<Member> getEnrolledMembers() {
        return Collections.unmodifiableList(enrolledMembers);
    }

    /** @return current enrolment count. */
    public int getEnrolmentCount() {
        return enrolledMembers.size();
    }

    // -- Enrolment mutation (called only by Gym; package-private-ish) -----
    //
    // The default package means we cannot enforce "package-private" usefully,
    // so we document the contract. Gym is the single point of publication.

    /**
     * Adds a member to this class. Intended for use by {@link Gym} only.
     * Validates capacity and duplicate enrolment.
     */
    void addMember(Member m) {
        if (m == null) {
            throw new IllegalArgumentException(
                "Cannot enrol: member must not be null.");
        }
        if (hasMember(m)) {
            throw new IllegalArgumentException(
                "Cannot enrol " + m.getName() + " in '" + name +
                "': already enrolled.");
        }
        if (isFull()) {
            throw new IllegalArgumentException(
                "Cannot enrol " + m.getName() + " in '" + name +
                "': class is full (capacity " + capacity + ").");
        }
        enrolledMembers.add(m);
    }

    /**
     * Removes a member from this class. Intended for use by {@link Gym}.
     * Silently does nothing if the member was not enrolled — Gym validates
     * before publishing the drop event.
     */
    void removeMember(Member m) {
        if (m == null) return;
        // remove by id, not by identity
        for (int i = 0; i < enrolledMembers.size(); i++) {
            if (enrolledMembers.get(i).getId() == m.getId()) {
                enrolledMembers.remove(i);
                return;
            }
        }
    }

    @Override
    public String toString() {
        return "FitnessClass['" + name + "' by " + instructor +
            ", " + dayOfWeek + " " + startTime +
            ", " + durationMinutes + "min, cap " + capacity +
            ", " + difficulty + ", " + enrolledMembers.size() + "/" + capacity + " enrolled]";
    }

    // =====================================================================
    // Builder
    // =====================================================================

    /**
     * Builder for {@link FitnessClass}. Two required fields are taken in the
     * constructor; eight optional fields use fluent setters with sensible
     * defaults. {@link #build()} re-validates the combined state.
     */
    public static class Builder {

        // Required (immutable on the Builder once constructed)
        private final String name;
        private final String instructor;

        // Optional with defaults
        private DayOfWeek    dayOfWeek       = DayOfWeek.MONDAY;
        private LocalTime    startTime       = LocalTime.of(18, 0);
        private int          durationMinutes = 60;
        private int          capacity        = 20;
        private String       room            = "Main floor";
        private Difficulty   difficulty      = Difficulty.INTERMEDIATE;
        private List<String> equipment       = new ArrayList<String>();
        private String       description     = "";

        /**
         * Starts a builder with the two required fields.
         *
         * @param name        class name, non-blank
         * @param instructor  instructor name, non-blank
         */
        public Builder(String name, String instructor) {
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException(
                    "Cannot build class: name must not be blank.");
            }
            if (instructor == null || instructor.trim().isEmpty()) {
                throw new IllegalArgumentException(
                    "Cannot build class: instructor must not be blank.");
            }
            this.name = name.trim();
            this.instructor = instructor.trim();
        }

        public Builder dayOfWeek(DayOfWeek d) {
            if (d == null) {
                throw new IllegalArgumentException(
                    "Cannot build class: dayOfWeek must not be null.");
            }
            this.dayOfWeek = d;
            return this;
        }

        public Builder startTime(LocalTime t) {
            if (t == null) {
                throw new IllegalArgumentException(
                    "Cannot build class: startTime must not be null.");
            }
            this.startTime = t;
            return this;
        }

        public Builder durationMinutes(int m) {
            if (m < 15) {
                throw new IllegalArgumentException(
                    "Cannot build class: durationMinutes must be at least 15, got " + m + ".");
            }
            this.durationMinutes = m;
            return this;
        }

        public Builder capacity(int c) {
            if (c < 1) {
                throw new IllegalArgumentException(
                    "Cannot build class: capacity must be at least 1, got " + c + ".");
            }
            this.capacity = c;
            return this;
        }

        public Builder room(String r) {
            if (r == null || r.trim().isEmpty()) {
                throw new IllegalArgumentException(
                    "Cannot build class: room must not be blank.");
            }
            this.room = r.trim();
            return this;
        }

        public Builder difficulty(Difficulty d) {
            if (d == null) {
                throw new IllegalArgumentException(
                    "Cannot build class: difficulty must not be null.");
            }
            this.difficulty = d;
            return this;
        }

        public Builder addEquipment(String item) {
            if (item == null || item.trim().isEmpty()) {
                throw new IllegalArgumentException(
                    "Cannot build class: equipment item must not be blank.");
            }
            this.equipment.add(item.trim());
            return this;
        }

        public Builder description(String d) {
            if (d == null) {
                throw new IllegalArgumentException(
                    "Cannot build class: description must not be null (use \"\" for none).");
            }
            this.description = d;
            return this;
        }

        /**
         * Re-validates combined state and constructs the immutable
         * {@link FitnessClass}.
         */
        public FitnessClass build() {
            // Required validation again — defends against subclassing or reflection.
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException(
                    "Cannot build class: name must not be blank.");
            }
            if (instructor == null || instructor.trim().isEmpty()) {
                throw new IllegalArgumentException(
                    "Cannot build class: instructor must not be blank.");
            }
            if (durationMinutes < 15) {
                throw new IllegalArgumentException(
                    "Cannot build class: durationMinutes must be at least 15, got " +
                    durationMinutes + ".");
            }
            if (capacity < 1) {
                throw new IllegalArgumentException(
                    "Cannot build class: capacity must be at least 1, got " +
                    capacity + ".");
            }
            return new FitnessClass(this);
        }
    }
}
