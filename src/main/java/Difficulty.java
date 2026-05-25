/**
 * Difficulty level of a fitness class.
 *
 * <p>Used by {@link FitnessClass} as a strongly-typed alternative to a free-form
 * string. The default difficulty if not specified on the Builder is
 * {@link #INTERMEDIATE}.
 */
public enum Difficulty {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED
}
