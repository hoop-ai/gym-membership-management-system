import java.util.EnumSet;
import java.util.Set;

/**
 * Represents the lifecycle states of a gym membership.
 *
 * <p>Each member's relationship with the gym evolves through a small set of
 * well-defined states. This enum models a <strong>finite state machine</strong>
 * where each state declares which target states it is allowed to transition
 * to. The state machine prevents impossible transitions (for example, an
 * expired membership cannot become pending again -- the member must sign up
 * for a brand-new plan).</p>
 *
 * <pre>
 *   PENDING ----> ACTIVE ----> EXPIRING ----> EXPIRED   (terminal)
 *                   |              |
 *                   v              v
 *                 FROZEN -----> ACTIVE
 *                   |
 *                   v
 *               CANCELLED      (terminal)
 * </pre>
 *
 * <h3>Design Pattern Role</h3>
 * <p>Acts as the <strong>State</strong> component in a lightweight State
 * pattern. Rather than creating separate state classes, the enum encapsulates
 * transition rules directly, which keeps the design simple while still
 * enforcing the workflow.</p>
 *
 * @see Member
 */
public enum MembershipStatus {

    /**
     * The member has signed up but has not yet been activated (e.g., awaiting
     * first payment, awaiting medical clearance, awaiting key-card).
     */
    PENDING {
        @Override
        protected Set<MembershipStatus> allowedTransitions() {
            return EnumSet.of(ACTIVE, CANCELLED);
        }
    },

    /**
     * The membership is fully live. Members can access the gym and receive
     * routine notifications (renewal reminders, class cancellations, etc.).
     */
    ACTIVE {
        @Override
        protected Set<MembershipStatus> allowedTransitions() {
            return EnumSet.of(EXPIRING, FROZEN, CANCELLED);
        }
    },

    /**
     * The membership is within its grace window (a configurable number of
     * days before the renewal date). The system uses this state to drive
     * renewal-reminder notifications.
     */
    EXPIRING {
        @Override
        protected Set<MembershipStatus> allowedTransitions() {
            return EnumSet.of(ACTIVE, EXPIRED, CANCELLED);
        }
    },

    /**
     * The membership has lapsed. <strong>Terminal state.</strong>
     */
    EXPIRED {
        @Override
        protected Set<MembershipStatus> allowedTransitions() {
            return EnumSet.noneOf(MembershipStatus.class);
        }
    },

    /**
     * The membership is temporarily on hold (medical leave, travel, etc.).
     * Returns to ACTIVE on resume; CANCELLED is also reachable if the member
     * decides not to continue.
     */
    FROZEN {
        @Override
        protected Set<MembershipStatus> allowedTransitions() {
            return EnumSet.of(ACTIVE, CANCELLED);
        }
    },

    /**
     * The member has cancelled the membership. <strong>Terminal state.</strong>
     */
    CANCELLED {
        @Override
        protected Set<MembershipStatus> allowedTransitions() {
            return EnumSet.noneOf(MembershipStatus.class);
        }
    };

    /**
     * Returns the set of states this state is permitted to transition to.
     *
     * @return an {@link EnumSet} of allowed target states
     */
    protected abstract Set<MembershipStatus> allowedTransitions();

    /**
     * Checks whether a transition from this state to {@code next} is allowed.
     *
     * @param next the target state
     * @return {@code true} if the transition is allowed, {@code false} otherwise
     */
    public boolean canTransitionTo(MembershipStatus next) {
        return allowedTransitions().contains(next);
    }
}
