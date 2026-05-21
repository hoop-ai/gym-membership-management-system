import java.util.ArrayList;
import java.util.List;

/**
 * Concrete observer that "sends" gym events as push notifications.
 *
 * <p>This demonstration notifier formats events as the short, title-plus-body
 * payloads that a real push-notification service (APNS, FCM, OneSignal,
 * ...) would expect, then appends the formatted message to an in-memory
 * log. Like the email and SMS notifiers, no real service is contacted; the
 * log is what makes the Observer pattern visible to the user.</p>
 *
 * @see MemberNotifier
 */
public class PushMemberNotifier implements MemberNotifier {

    private final Member member;
    private final List<String> sentLog = new ArrayList<>();

    public PushMemberNotifier(Member member) {
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
        return "PUSH";
    }

    @Override
    public void onEvent(GymEvent event) {
        if (event == null) return;
        if (!event.isBroadcast() && !event.getTargetMember().equals(member)) return;

        // Push payloads are typically title + body.
        String title = titleFor(event);
        String body  = event.getMessage();

        String formatted = String.format(
                "[PUSH -> %s] %s | %s",
                member.getName(), title, body);

        sentLog.add(formatted);
        System.out.println(formatted);
    }

    /** Maps the event type to a short, human-friendly notification title. */
    private String titleFor(GymEvent event) {
        switch (event.getType()) {
            case "PAYMENT_DUE":      return "Payment reminder";
            case "RENEWAL_REMINDER": return "Renewal coming up";
            case "CLASS_CANCELLED":  return "Class update";
            case "PROMOTION":        return "Offer for you";
            default:                 return event.getType();
        }
    }

    public List<String> getSentLog() {
        return java.util.Collections.unmodifiableList(sentLog);
    }
}
