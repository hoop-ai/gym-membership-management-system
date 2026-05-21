import java.util.ArrayList;
import java.util.List;

/**
 * Concrete observer that "sends" gym events as SMS messages.
 *
 * <p>Like {@link EmailMemberNotifier}, this is a demonstration notifier
 * with no real SMS gateway. The formatted message is appended to an
 * in-memory log; the GUI and the test demo read that log to prove the
 * Observer pattern is actually delivering events.</p>
 *
 * <p>SMS messages are intentionally <strong>terser</strong> than email --
 * the formatter truncates long event messages to 110 characters with a
 * trailing ellipsis, simulating real SMS payload limits.</p>
 *
 * @see MemberNotifier
 */
public class SmsMemberNotifier implements MemberNotifier {

    /** Maximum SMS payload length the formatter is willing to emit. */
    private static final int MAX_SMS_LENGTH = 110;

    private final Member member;
    private final List<String> sentLog = new ArrayList<>();

    public SmsMemberNotifier(Member member) {
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
        return "SMS";
    }

    @Override
    public void onEvent(GymEvent event) {
        if (event == null) return;
        if (!event.isBroadcast() && !event.getTargetMember().equals(member)) return;

        // Build the SMS body, truncated to a sensible length.
        String body = "(" + event.getType() + ") " + event.getMessage();
        if (body.length() > MAX_SMS_LENGTH) {
            body = body.substring(0, MAX_SMS_LENGTH - 3) + "...";
        }

        String formatted = String.format("[SMS -> %s] %s", phoneOrEmail(), body);
        sentLog.add(formatted);
        System.out.println(formatted);
    }

    /** Falls back to the email address as a routing token when no phone is on file. */
    private String phoneOrEmail() {
        return member.getPhone() == null || member.getPhone().isBlank()
                ? member.getEmail()
                : member.getPhone();
    }

    public List<String> getSentLog() {
        return java.util.Collections.unmodifiableList(sentLog);
    }
}
