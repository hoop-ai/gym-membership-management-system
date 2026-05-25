/**
 * Published by {@link Gym} after a member has been successfully added.
 *
 * <p>Carries the {@link Member} that was added. Observers can use this for
 * onboarding logging, welcome emails (in a real system), or simply for an
 * audit trail.</p>
 */
public final class MemberAddedEvent extends GymEvent {

    private final Member member;

    public MemberAddedEvent(Member member) {
        super("MEMBER_ADDED", buildMessage(member));
        this.member = member;
    }

    private static String buildMessage(Member m) {
        if (m == null) {
            throw new IllegalArgumentException(
                "Cannot create MemberAddedEvent: member must not be null.");
        }
        return "Member added: " + m.getName() + " (id=" + m.getId() + ")";
    }

    public Member getMember() { return member; }
}
