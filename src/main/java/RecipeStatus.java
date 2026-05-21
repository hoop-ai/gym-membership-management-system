import java.util.EnumSet;
import java.util.Set;

/**
 * Represents the lifecycle states of a recipe in the Recipe Management System.
 *
 * <p>This enum models a <strong>finite state machine</strong> where each state
 * defines a set of valid transitions. The state machine enforces the following
 * workflow used by home cooks while building, refining and finally serving a
 * recipe:</p>
 *
 * <pre>
 *   DRAFT ---------> TESTING ---------> APPROVED ---------> COOKED (terminal)
 *     |                 |                   |
 *     v                 v                   v
 *   PAUSED <-------- PAUSED              TESTING
 *     |
 *     v
 *    DRAFT
 * </pre>
 *
 * <h3>Design Pattern Role</h3>
 * <p>Acts as the <strong>State</strong> component in a lightweight State pattern.
 * Rather than creating separate state classes, the enum encapsulates transition
 * rules directly, keeping the design simple while still enforcing valid workflows.</p>
 *
 * <h3>SOLID Principles Demonstrated</h3>
 * <ul>
 *   <li><strong>Single Responsibility (SRP)</strong> -- This enum is solely responsible
 *       for defining recipe states and their valid transitions. It does not handle
 *       recipe data, persistence, or business logic beyond state transitions.</li>
 *   <li><strong>Open/Closed Principle (OCP)</strong> -- New states can be added to the
 *       enum with their own transition rules without modifying the transition logic
 *       of existing states, since each state declares its own allowed targets.</li>
 * </ul>
 *
 * @see Recipe
 * @see AbstractRecipe
 */
public enum RecipeStatus {

    /**
     * The recipe has been jotted down but not yet attempted in the kitchen.
     * Can move to TESTING (start cooking trials) or PAUSED (waiting for an
     * ingredient, a piece of equipment, or simply more time).
     */
    DRAFT {
        @Override
        protected Set<RecipeStatus> allowedTransitions() {
            return EnumSet.of(TESTING, PAUSED);
        }
    },

    /**
     * The recipe is actively being trialled and refined in the kitchen.
     * Can move to APPROVED (the recipe is now reliable) or PAUSED (an
     * obstacle has appeared mid-trial).
     */
    TESTING {
        @Override
        protected Set<RecipeStatus> allowedTransitions() {
            return EnumSet.of(APPROVED, PAUSED);
        }
    },

    /**
     * The cook is satisfied with the recipe and considers it reliable.
     * Can move to COOKED (the recipe has been served) or back to TESTING
     * (further changes are required after a final review).
     */
    APPROVED {
        @Override
        protected Set<RecipeStatus> allowedTransitions() {
            return EnumSet.of(COOKED, TESTING);
        }
    },

    /**
     * The recipe has been served. This is a <strong>terminal state</strong>
     * with no outgoing transitions; a re-cook starts a new {@link Recipe}.
     */
    COOKED {
        @Override
        protected Set<RecipeStatus> allowedTransitions() {
            // Terminal state -- no further transitions are permitted.
            return EnumSet.noneOf(RecipeStatus.class);
        }
    },

    /**
     * The recipe is on hold (missing ingredient, broken equipment, scheduling
     * clash, etc.). Can only transition back to DRAFT once the obstacle has
     * been cleared, at which point the cook may resume trials or rework the
     * recipe from scratch.
     */
    PAUSED {
        @Override
        protected Set<RecipeStatus> allowedTransitions() {
            return EnumSet.of(DRAFT);
        }
    };

    /**
     * Returns the set of states this state is permitted to transition to.
     * Implemented per enum constant.
     *
     * @return an {@link EnumSet} of allowed target states
     */
    protected abstract Set<RecipeStatus> allowedTransitions();

    /**
     * Checks whether a transition from this state to the given {@code next}
     * state is permitted by the workflow rules.
     *
     * <p>Example usage:</p>
     * <pre>
     *   if (currentStatus.canTransitionTo(RecipeStatus.TESTING)) {
     *       recipe.setStatus(RecipeStatus.TESTING);
     *   }
     * </pre>
     *
     * @param next the target state to transition to
     * @return {@code true} if the transition is valid, {@code false} otherwise
     */
    public boolean canTransitionTo(RecipeStatus next) {
        return allowedTransitions().contains(next);
    }
}
