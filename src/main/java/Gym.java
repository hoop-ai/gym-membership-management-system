import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The {@code Gym} is the application's aggregate root and the single
 * point of event publication.
 *
 * <p><strong>Pattern role: Observer (Behavioral, GoF) — Subject.</strong>
 * Observers register via {@link #addObserver(GymEventObserver)} and receive
 * every event the gym publishes. The Gym is the ONLY class that calls
 * {@link GymEventObserver#onEvent(GymEvent)}. Events are never raised from
 * anywhere else — not from {@code Member}, not from {@code FitnessClass},
 * not from {@code Main}. Single point of publication = one place to look
 * when something is wrong.</p>
 *
 * <p>The gym owns:</p>
 * <ul>
 *   <li>a list of {@link Member}s (id-keyed; ids are assigned here);</li>
 *   <li>a list of {@link FitnessClass}es (name-keyed, case-insensitive);</li>
 *   <li>a list of {@link GymEventObserver}s.</li>
 * </ul>
 *
 * <p><strong>Case-insensitivity.</strong> Class-name lookups
 * ({@link #getFitnessClass(String)}, {@link #removeClass(String)},
 * {@link #enrolMemberInClass(int, String)}, etc.) match on case-folded
 * names — "Yoga Flow", "yoga flow", and "YOGA FLOW" all refer to the same
 * class. The class's own stored name is preserved as supplied.</p>
 */
public class Gym {

    private final String name;

    private final List<Member>           members   = new ArrayList<Member>();
    private final List<FitnessClass>     classes   = new ArrayList<FitnessClass>();
    private final List<GymEventObserver> observers = new ArrayList<GymEventObserver>();

    /** Next id to assign to a new member. Monotonic, never reused. */
    private int nextMemberId = 1;

    /**
     * Creates a gym.
     *
     * @param name non-blank gym name
     */
    public Gym(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Cannot create gym: name must not be blank.");
        }
        this.name = name.trim();
    }

    public String getName() { return name; }

    // =====================================================================
    // Observer registration
    // =====================================================================

    /**
     * Registers an observer. Adding the same observer twice is a no-op.
     *
     * @param o non-null observer
     */
    public void addObserver(GymEventObserver o) {
        if (o == null) {
            throw new IllegalArgumentException(
                "Cannot add observer: observer must not be null.");
        }
        if (!observers.contains(o)) {
            observers.add(o);
        }
    }

    /**
     * Unregisters an observer. Silently does nothing if the observer was
     * not registered.
     */
    public void removeObserver(GymEventObserver o) {
        if (o == null) return;
        observers.remove(o);
    }

    /** @return number of registered observers. */
    public int observerCount() {
        return observers.size();
    }

    // =====================================================================
    // Member management
    // =====================================================================

    /**
     * Adds a new member. The id is assigned by the gym (monotonic from 1).
     * Publishes a {@link MemberAddedEvent}.
     *
     * @param memberName  non-blank display name
     * @param email       non-blank email
     * @return the newly created member
     */
    public Member addMember(String memberName, String email) {
        if (memberName == null || memberName.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Cannot add member: name must not be blank.");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Cannot add member: email must not be blank.");
        }
        Member m = new Member(nextMemberId++, memberName, email);
        members.add(m);
        publish(new MemberAddedEvent(m));
        return m;
    }

    /**
     * Removes a member by id. Implicitly drops them from every class they
     * are enrolled in (those drops are NOT published as separate events).
     * Publishes a {@link MemberRemovedEvent}.
     */
    public void removeMember(int id) {
        Member m = getMember(id);
        for (FitnessClass c : classes) {
            if (c.hasMember(m)) {
                c.removeMember(m);
            }
        }
        members.remove(m);
        publish(new MemberRemovedEvent(m));
    }

    /**
     * Looks up a member by id.
     *
     * @throws IllegalArgumentException if no member has the given id
     */
    public Member getMember(int id) {
        for (Member m : members) {
            if (m.getId() == id) return m;
        }
        throw new IllegalArgumentException(
            "No member found with ID " + id + "." + knownIdsHint());
    }

    /** @return unmodifiable view of every current member. */
    public List<Member> getMembers() {
        return Collections.unmodifiableList(members);
    }

    private String knownIdsHint() {
        if (members.isEmpty()) {
            return " No members are registered.";
        }
        StringBuilder sb = new StringBuilder(" Known IDs: ");
        for (int i = 0; i < members.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(members.get(i).getId());
        }
        sb.append(".");
        return sb.toString();
    }

    // =====================================================================
    // Class management
    // =====================================================================

    /**
     * Adds a fitness class to the schedule. Validates non-null and that the
     * class name is not already in use (case-insensitive). Publishes a
     * {@link ClassAddedEvent}.
     */
    public void addClass(FitnessClass cls) {
        if (cls == null) {
            throw new IllegalArgumentException(
                "Cannot add class: class must not be null.");
        }
        if (findClassIgnoreCase(cls.getName()) != null) {
            throw new IllegalArgumentException(
                "Cannot add class: '" + cls.getName() + "' already exists.");
        }
        classes.add(cls);
        publish(new ClassAddedEvent(cls));
    }

    /**
     * Removes a class by name (case-insensitive). Publishes a
     * {@link ClassRemovedEvent}.
     *
     * @throws IllegalArgumentException if no class with that name exists
     */
    public void removeClass(String className) {
        FitnessClass c = getFitnessClass(className);
        classes.remove(c);
        publish(new ClassRemovedEvent(c));
    }

    /**
     * Looks up a class by name (case-insensitive).
     *
     * @throws IllegalArgumentException if no class with that name exists
     */
    public FitnessClass getFitnessClass(String className) {
        if (className == null || className.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Cannot look up class: name must not be blank.");
        }
        FitnessClass c = findClassIgnoreCase(className);
        if (c == null) {
            throw new IllegalArgumentException(
                "No class found with name '" + className + "'." + knownClassNamesHint());
        }
        return c;
    }

    /** @return unmodifiable view of every current class. */
    public List<FitnessClass> getClasses() {
        return Collections.unmodifiableList(classes);
    }

    /** @return the class matching name (case-insensitive), or null. */
    private FitnessClass findClassIgnoreCase(String className) {
        if (className == null) return null;
        String needle = className.trim();
        for (FitnessClass c : classes) {
            if (c.getName().equalsIgnoreCase(needle)) return c;
        }
        return null;
    }

    private String knownClassNamesHint() {
        if (classes.isEmpty()) {
            return " No classes are scheduled.";
        }
        StringBuilder sb = new StringBuilder(" Known classes: ");
        for (int i = 0; i < classes.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append("'").append(classes.get(i).getName()).append("'");
        }
        sb.append(".");
        return sb.toString();
    }

    // =====================================================================
    // Enrolment
    // =====================================================================

    /**
     * Enrols a member in a class. Publishes a
     * {@link MemberEnrolledInClassEvent} on success.
     *
     * @throws IllegalArgumentException if member or class is missing, the
     *         member is already enrolled, or the class is full
     */
    public void enrolMemberInClass(int memberId, String className) {
        Member m = getMember(memberId);
        FitnessClass c = getFitnessClass(className);
        if (c.hasMember(m)) {
            throw new IllegalArgumentException(
                "Cannot enrol " + m.getName() + " in '" + c.getName() +
                "': already enrolled.");
        }
        if (c.isFull()) {
            throw new IllegalArgumentException(
                "Cannot enrol " + m.getName() + " in '" + c.getName() +
                "': class is full (capacity " + c.getCapacity() + ").");
        }
        c.addMember(m);
        publish(new MemberEnrolledInClassEvent(m, c));
    }

    /**
     * Drops a member from a class. Publishes a
     * {@link MemberDroppedFromClassEvent} on success.
     *
     * @throws IllegalArgumentException if member or class is missing, or
     *         the member is not enrolled in the class
     */
    public void dropMemberFromClass(int memberId, String className) {
        Member m = getMember(memberId);
        FitnessClass c = getFitnessClass(className);
        if (!c.hasMember(m)) {
            throw new IllegalArgumentException(
                "Cannot drop " + m.getName() + " from '" + c.getName() +
                "': not enrolled.");
        }
        c.removeMember(m);
        publish(new MemberDroppedFromClassEvent(m, c));
    }

    // =====================================================================
    // Event publication — single point
    // =====================================================================

    /**
     * Single point of event publication for the entire system. Nobody else
     * calls {@link GymEventObserver#onEvent(GymEvent)}.
     */
    private void publish(GymEvent event) {
        for (GymEventObserver o : observers) {
            o.onEvent(event);
        }
    }
}
