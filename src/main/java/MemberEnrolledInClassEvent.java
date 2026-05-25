/**
 * Published by {@link Gym} after a member has been enrolled in a class.
 *
 * <p>Carries both the {@link Member} and the {@link FitnessClass}.</p>
 */
public final class MemberEnrolledInClassEvent extends GymEvent {

    private final Member       member;
    private final FitnessClass fitnessClass;

    public MemberEnrolledInClassEvent(Member member, FitnessClass fitnessClass) {
        super("MEMBER_ENROLLED", buildMessage(member, fitnessClass));
        this.member       = member;
        this.fitnessClass = fitnessClass;
    }

    private static String buildMessage(Member m, FitnessClass c) {
        if (m == null) {
            throw new IllegalArgumentException(
                "Cannot create MemberEnrolledInClassEvent: member must not be null.");
        }
        if (c == null) {
            throw new IllegalArgumentException(
                "Cannot create MemberEnrolledInClassEvent: fitnessClass must not be null.");
        }
        return m.getName() + " enrolled in '" + c.getName() + "'";
    }

    public Member       getMember()       { return member; }
    public FitnessClass getFitnessClass() { return fitnessClass; }
}
