import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Immutable description of a gym membership plan.
 *
 * <p>A plan bundles together every commercially-relevant attribute of an
 * offering: name, duration, monthly fee, access tier, the list of group
 * classes that are included, the number of guest passes per month, and the
 * maximum number of days a member may freeze the membership per year.</p>
 *
 * <h3>Design Pattern Role -- Builder (Product)</h3>
 * <p>This class is the <strong>Product</strong> of the <strong>Builder</strong>
 * design pattern. Constructing a plan requires many parameters, many of which
 * have sensible defaults. A traditional constructor with eight positional
 * arguments would be unreadable at the call site and impossible to evolve
 * (adding a ninth attribute would break every caller). The nested
 * {@link Builder} class solves both problems:</p>
 * <ul>
 *   <li>Each attribute is set by its own named method, so the call site reads
 *       like a configuration list rather than a positional opcode.</li>
 *   <li>Optional attributes have defaults; required attributes are validated
 *       inside {@link Builder#build()}.</li>
 *   <li>The product itself is <strong>immutable</strong> -- once built, a
 *       plan never changes. Members and clients can share references without
 *       worrying about defensive copying.</li>
 * </ul>
 *
 * <h3>SOLID Principles Demonstrated</h3>
 * <ul>
 *   <li><strong>Single Responsibility Principle.</strong> This class is
 *       responsible only for holding plan data. Construction is delegated to
 *       the nested {@code Builder}.</li>
 *   <li><strong>Open/Closed Principle.</strong> Adding a new plan attribute
 *       (e.g., {@code includesNutritionConsult}) requires extending this class
 *       and the {@code Builder} -- but does not require touching the
 *       {@link Gym} or any notifier.</li>
 *   <li><strong>Interface Segregation Principle.</strong> Only the accessors
 *       relevant to every plan are public. Internal-only data is private and
 *       final.</li>
 * </ul>
 *
 * <h3>Usage</h3>
 * <pre>{@code
 *     MembershipPlan premium = new MembershipPlan.Builder("Premium Annual")
 *             .durationMonths(12)
 *             .monthlyFee(89.99)
 *             .accessTier(AccessTier.PREMIUM)
 *             .includesClass("Yoga")
 *             .includesClass("Spinning")
 *             .guestPassesPerMonth(4)
 *             .freezeDaysPerYear(60)
 *             .build();
 * }</pre>
 *
 * @see Builder
 * @see Member
 * @see Gym
 */
public final class MembershipPlan {

    // -----------------------------------------------------------------------
    // Immutable fields
    // -----------------------------------------------------------------------

    private final String        name;
    private final int           durationMonths;
    private final double        monthlyFee;
    private final AccessTier    accessTier;
    private final Set<String>   includedClasses;
    private final int           guestPassesPerMonth;
    private final int           freezeDaysPerYear;
    private final boolean       personalTrainerIncluded;

    // -----------------------------------------------------------------------
    // Private constructor -- only the Builder may call this
    // -----------------------------------------------------------------------

    /**
     * Private constructor: only the {@link Builder} can instantiate plans.
     *
     * @param b a fully-validated builder
     */
    private MembershipPlan(Builder b) {
        this.name                    = b.name;
        this.durationMonths          = b.durationMonths;
        this.monthlyFee              = b.monthlyFee;
        this.accessTier              = b.accessTier;
        this.includedClasses         = Collections.unmodifiableSet(new LinkedHashSet<>(b.includedClasses));
        this.guestPassesPerMonth     = b.guestPassesPerMonth;
        this.freezeDaysPerYear       = b.freezeDaysPerYear;
        this.personalTrainerIncluded = b.personalTrainerIncluded;
    }

    // -----------------------------------------------------------------------
    // Accessors
    // -----------------------------------------------------------------------

    public String      getName()                    { return name; }
    public int         getDurationMonths()          { return durationMonths; }
    public double      getMonthlyFee()              { return monthlyFee; }
    public AccessTier  getAccessTier()              { return accessTier; }
    public Set<String> getIncludedClasses()         { return includedClasses; }
    public int         getGuestPassesPerMonth()     { return guestPassesPerMonth; }
    public int         getFreezeDaysPerYear()       { return freezeDaysPerYear; }
    public boolean     isPersonalTrainerIncluded()  { return personalTrainerIncluded; }

    /**
     * Total cost of the plan over its full duration.
     *
     * @return the monthly fee multiplied by the duration in months
     */
    public double getTotalCost() {
        return monthlyFee * durationMonths;
    }

    @Override
    public String toString() {
        return String.format(
                "Plan[name='%s', tier=%s, months=%d, monthlyFee=%.2f, classes=%s, guestPasses=%d/mo, "
                        + "freeze=%dd/yr, PT=%s]",
                name, accessTier, durationMonths, monthlyFee, includedClasses,
                guestPassesPerMonth, freezeDaysPerYear, personalTrainerIncluded ? "yes" : "no");
    }

    // =======================================================================
    // Nested Builder -- the heart of the Builder pattern
    // =======================================================================

    /**
     * Fluent builder for {@link MembershipPlan}.
     *
     * <p>Holds a mutable working copy of every attribute, validated centrally
     * in {@link #build()} so that every {@link MembershipPlan} ever produced
     * satisfies the same invariants. The constructor accepts the only truly
     * mandatory field (the plan's display name); every other attribute has a
     * sensible default that can be overridden via a chainable setter.</p>
     *
     * <p>Calls return the builder itself so the configuration reads as a
     * vertical list at the call site, which is the main ergonomic benefit of
     * the Builder pattern over a many-argument constructor.</p>
     */
    public static final class Builder {

        // ---- working state -------------------------------------------------

        private final String name;

        private int        durationMonths          = 1;
        private double     monthlyFee              = 0.0;
        private AccessTier accessTier              = AccessTier.BASIC;
        private Set<String> includedClasses        = new LinkedHashSet<>();
        private int        guestPassesPerMonth     = 0;
        private int        freezeDaysPerYear       = 0;
        private boolean    personalTrainerIncluded = false;

        /**
         * Starts a new builder for a plan with the given display name.
         *
         * @param name a human-readable name (must not be {@code null} or blank)
         * @throws IllegalArgumentException if {@code name} is null or blank
         */
        public Builder(String name) {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Plan name must not be null or blank.");
            }
            this.name = name;
        }

        // ---- fluent setters -----------------------------------------------

        public Builder durationMonths(int months) {
            this.durationMonths = months;
            return this;
        }

        public Builder monthlyFee(double fee) {
            this.monthlyFee = fee;
            return this;
        }

        public Builder accessTier(AccessTier tier) {
            this.accessTier = tier;
            return this;
        }

        /**
         * Adds a single included group-class name. Duplicates are silently
         * dropped.
         *
         * @param className the class name (must not be {@code null} or blank)
         * @return this builder for chaining
         */
        public Builder includesClass(String className) {
            if (className == null || className.isBlank()) {
                throw new IllegalArgumentException("Class name must not be null or blank.");
            }
            this.includedClasses.add(className.trim());
            return this;
        }

        public Builder guestPassesPerMonth(int passes) {
            this.guestPassesPerMonth = passes;
            return this;
        }

        public Builder freezeDaysPerYear(int days) {
            this.freezeDaysPerYear = days;
            return this;
        }

        public Builder personalTrainerIncluded(boolean included) {
            this.personalTrainerIncluded = included;
            return this;
        }

        // ---- validation + construction ------------------------------------

        /**
         * Validates the working state and returns an immutable
         * {@link MembershipPlan}.
         *
         * <p>All invariants are checked here, not in the setters, so a builder
         * can be configured in any order before the validation runs.</p>
         *
         * @return a new, fully-validated, immutable {@code MembershipPlan}
         * @throws IllegalArgumentException if any invariant fails
         */
        public MembershipPlan build() {
            if (durationMonths <= 0) {
                throw new IllegalArgumentException(
                        "Duration must be at least 1 month, got: " + durationMonths);
            }
            if (monthlyFee < 0) {
                throw new IllegalArgumentException(
                        "Monthly fee must be non-negative, got: " + monthlyFee);
            }
            if (accessTier == null) {
                throw new IllegalArgumentException("Access tier must not be null.");
            }
            if (guestPassesPerMonth < 0) {
                throw new IllegalArgumentException(
                        "Guest passes per month must be non-negative, got: " + guestPassesPerMonth);
            }
            if (freezeDaysPerYear < 0) {
                throw new IllegalArgumentException(
                        "Freeze days per year must be non-negative, got: " + freezeDaysPerYear);
            }
            return new MembershipPlan(this);
        }
    }
}
