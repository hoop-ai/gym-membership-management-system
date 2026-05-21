/**
 * The level of physical access a membership grants inside the gym.
 *
 * <p>Tiers are ordered from most restricted ({@link #BASIC}) to least
 * restricted ({@link #PREMIUM}). Higher tiers include every privilege of the
 * lower tiers, so the natural enum ordering matches the access hierarchy.</p>
 */
public enum AccessTier {

    /**
     * Gym floor access during standard business hours (e.g., 09:00 -- 21:00),
     * weekdays only. No pool, no sauna, no group classes.
     */
    BASIC,

    /**
     * Gym floor access during extended hours (e.g., 06:00 -- 23:00), seven
     * days a week, plus group classes. No spa amenities.
     */
    STANDARD,

    /**
     * Twenty-four-hour gym access, all group classes, pool, sauna, spa, and
     * guest passes. The most permissive tier.
     */
    PREMIUM
}
