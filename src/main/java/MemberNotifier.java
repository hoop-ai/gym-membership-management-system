/**
 * Observer interface in the Observer design pattern -- represents one
 * channel (email, SMS, push, etc.) through which a particular {@link Member}
 * can be notified.
 *
 * <p>Each notifier wraps a specific member and knows the channel-specific
 * details (email address from {@code member.getEmail()}, phone number from
 * {@code member.getPhone()}, etc.). The {@link Gym} publishes
 * {@link GymEvent}s and dispatches them to attached notifiers; each notifier
 * decides whether the event applies to its member and formats the message
 * for its channel.</p>
 *
 * <h3>Design Pattern Role</h3>
 * <p><strong>Observer</strong> interface. The {@code Gym} acts as the
 * <em>Subject</em>; concrete implementations of {@code MemberNotifier} are
 * <em>Concrete Observers</em>.</p>
 *
 * <h3>SOLID Principles Demonstrated</h3>
 * <ul>
 *   <li><strong>Interface Segregation.</strong> Two methods only: identify
 *       the member, deliver the event. Notifiers are not forced to depend on
 *       channel-specific concerns of other notifiers.</li>
 *   <li><strong>Open/Closed.</strong> A new channel (e.g., a
 *       {@code DiscordMemberNotifier}) is a single new class -- existing
 *       notifiers, the {@code Gym}, and the events all stay untouched.</li>
 *   <li><strong>Liskov Substitution.</strong> Any implementation can be
 *       attached anywhere a {@code MemberNotifier} is expected.</li>
 * </ul>
 *
 * @see EmailMemberNotifier
 * @see SmsMemberNotifier
 * @see PushMemberNotifier
 * @see Gym
 * @see GymEvent
 */
public interface MemberNotifier {

    /**
     * Returns the member this notifier is registered to deliver events for.
     *
     * @return the wrapped member (never null)
     */
    Member getMember();

    /**
     * Returns a short, uppercase channel identifier
     * ({@code "EMAIL"}, {@code "SMS"}, {@code "PUSH"}, ...). Used by the
     * test demo and the GUI to label the notification log.
     *
     * @return a short channel name
     */
    String getChannel();

    /**
     * Receives one event from the {@link Gym}.
     *
     * <p>Implementations should ignore events that do not apply to their
     * member (i.e., those whose {@code targetMember} is set but does not
     * equal {@link #getMember()}). Broadcasts (where
     * {@code event.isBroadcast()} returns {@code true}) should be delivered
     * to every notifier.</p>
     *
     * @param event the event to deliver (never null)
     */
    void onEvent(GymEvent event);
}
