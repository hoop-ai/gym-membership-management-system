/**
 * Published by {@link Gym} after a member has been dropped from a class.
 *
 * <p>Carries both the {@link Member} and the {@link FitnessClass}.</p>
 */
public final class MemberDroppedFromClassEvent extends GymEvent {

    private final Member       member;
    private final FitnessClass fitnessClass;

    public MemberDroppedFromClassEvent(Member member, FitnessClass fitnessClass) {
        super("MEMBER_DROPPED", buildMessage(member, fitnessClass));
        this.member       = member;
        this.fitnessClass = fitnessClass;
    }

    private static String buildMessage(Member m, FitnessClass c) {
        if (m == null) {
            throw new IllegalArgumentException(
                "Cannot create MemberDroppedFromClassEvent: member must not be null.");
        }
        if (c == null) {
            throw new IllegalArgumentException(
                "Cannot create MemberDroppedFromClassEvent: fitnessClass must not be null.");
        }
        return m.getName() + " dropped from '" + c.getName() + "'";
    }

    public Member       getMember()       { return member; }
    public FitnessClass getFitnessClass() { return fitnessClass; }
}
