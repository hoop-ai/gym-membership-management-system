/**
 * Published by {@link Gym} after a member has been removed.
 *
 * <p>Carries the {@link Member} that was removed. The removal also implicitly
 * drops the member from every class they were enrolled in, but those drops
 * are NOT published as separate {@link MemberDroppedFromClassEvent} events —
 * the spec keeps the demo output compact. A future revision could choose to
 * publish them; that's a one-line change in {@link Gym#removeMember(int)}.</p>
 */
public final class MemberRemovedEvent extends GymEvent {

    private final Member member;

    public MemberRemovedEvent(Member member) {
        super("MEMBER_REMOVED", buildMessage(member));
        this.member = member;
    }

    private static String buildMessage(Member m) {
        if (m == null) {
            throw new IllegalArgumentException(
                "Cannot create MemberRemovedEvent: member must not be null.");
        }
        return "Member removed: " + m.getName() + " (id=" + m.getId() + ")";
    }

    public Member getMember() { return member; }
}
