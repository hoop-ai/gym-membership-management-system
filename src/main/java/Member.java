/**
 * A gym member. Immutable POJO identified by an auto-assigned id.
 *
 * <p>Members are created exclusively by {@link Gym#addMember(String, String)},
 * which assigns the id. Equality is based on id, so two members with the same
 * id (impossible in practice) are considered equal.
 */
public final class Member {

    private final int id;
    private final String name;
    private final String email;

    /**
     * Creates a Member. Invoked by {@link Gym} after id assignment.
     *
     * @param id    positive identifier assigned by Gym
     * @param name  non-blank display name
     * @param email non-blank email address
     */
    public Member(int id, String name, String email) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                "Cannot create member: id must be positive, got " + id + ".");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Cannot create member: name must not be blank.");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Cannot create member: email must not be blank.");
        }
        this.id = id;
        this.name = name.trim();
        this.email = email.trim();
    }

    public int getId()       { return id; }
    public String getName()  { return name; }
    public String getEmail() { return email; }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Member)) return false;
        return this.id == ((Member) other).id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return name + " (id=" + id + ", " + email + ")";
    }
}
