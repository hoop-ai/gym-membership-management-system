import java.util.ArrayList;
import java.util.List;

/**
 * Concrete observer that "sends" gym events as emails.
 *
 * <p>This demo does not actually contact an SMTP server -- it appends every
 * formatted email to an in-memory log that the test demo, the console app
 * and the GUI all read. The log is what gives the Observer pattern its
 * visible payoff in this project.</p>
 *
 * @see MemberNotifier
 */
public class EmailMemberNotifier implements MemberNotifier {

    private final Member member;

    /** Append-only log of every email that has been formatted for this notifier. */
    private final List<String> sentLog = new ArrayList<>();

    public EmailMemberNotifier(Member member) {
        if (member == null) {
            throw new IllegalArgumentException("Member must not be null.");
        }
        this.member = member;
    }

    @Override
    public Member getMember() {
        return member;
    }

    @Override
    public String getChannel() {
        return "EMAIL";
    }

    @Override
    public void onEvent(GymEvent event) {
        if (event == null) return;
        // Skip events targeted at someone else; deliver broadcasts and own-member events.
        if (!event.isBroadcast() && !event.getTargetMember().equals(member)) return;

        String formatted = String.format(
                "[EMAIL -> %s] (%s) %s",
                member.getEmail(),
                event.getType(),
                event.getMessage());

        sentLog.add(formatted);
        System.out.println(formatted);
    }

    /**
     * Returns an unmodifiable view of every message this notifier has
     * formatted and "sent". Useful for tests and the GUI notification log.
     *
     * @return the (live, unmodifiable) sent-log
     */
    public List<String> getSentLog() {
        return java.util.Collections.unmodifiableList(sentLog);
    }
}
