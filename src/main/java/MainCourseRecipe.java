/**
 * Represents a main-course recipe in the Recipe Management System.
 *
 * <p>A main course captures the centre-of-the-plate dish along with its
 * total cooking time (in minutes) and a one-to-ten satisfaction rating that
 * the cook has assigned after trialling the recipe. Cooks use these numbers
 * to plan dinner timing and pick winners for repeat appearances.</p>
 *
 * <h3>Design Pattern Role</h3>
 * <p>This is a <strong>Concrete Product</strong> in the <strong>Factory Method</strong>
 * pattern. A recipe factory can create {@code MainCourseRecipe} instances and
 * return them through the {@link Recipe} interface, decoupling client code from
 * this specific implementation.</p>
 *
 * <h3>SOLID Principles Demonstrated</h3>
 * <ul>
 *   <li><strong>Single Responsibility Principle (SRP)</strong> -- This class is
 *       responsible only for main-course-specific data (cooking time, satisfaction
 *       rating). All common recipe behaviour is inherited from {@link AbstractRecipe}.</li>
 *   <li><strong>Liskov Substitution Principle (LSP)</strong> -- A
 *       {@code MainCourseRecipe} can be used anywhere a {@link Recipe} is expected
 *       without altering program correctness.</li>
 *   <li><strong>Open/Closed Principle (OCP)</strong> -- This class was added as a
 *       new concrete product without modifying {@link AbstractRecipe} or
 *       {@link Recipe}.</li>
 * </ul>
 *
 * @see Recipe
 * @see AbstractRecipe
 * @see DessertRecipe
 * @see AppetizerRecipe
 */
public class MainCourseRecipe extends AbstractRecipe {

    // -----------------------------------------------------------------------
    // Main-course-specific fields
    // -----------------------------------------------------------------------

    /**
     * Total cooking time in minutes from first prep step to plating.
     * Used when scheduling a multi-course meal so the kitchen does not back up.
     */
    private final int cookingTimeMinutes;

    /**
     * Cook's satisfaction rating from 1 (would not make again) to 10 (signature
     * dish). Built up over repeated trials, this score helps pick which
     * approved recipes get promoted to the regular rotation.
     */
    private final int satisfactionRating;

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    /**
     * Creates a new main-course recipe with the specified details.
     *
     * @param title              a short name for the dish
     * @param description        a detailed description of the dish
     * @param priority           priority from 1 (low) to 5 (high)
     * @param cookingTimeMinutes total cooking time in minutes (must be non-negative)
     * @param satisfactionRating cook's score from 1 to 10
     * @throws IllegalArgumentException if {@code cookingTimeMinutes} is negative
     *                                  or {@code satisfactionRating} is outside 1-10
     */
    public MainCourseRecipe(String title, String description, int priority,
                            int cookingTimeMinutes, int satisfactionRating) {
        super(title, description, priority);

        if (cookingTimeMinutes < 0) {
            throw new IllegalArgumentException(
                    "Cooking time must be non-negative, got: " + cookingTimeMinutes);
        }
        if (satisfactionRating < 1 || satisfactionRating > 10) {
            throw new IllegalArgumentException(
                    "Satisfaction rating must be between 1 and 10, got: " + satisfactionRating);
        }

        this.cookingTimeMinutes = cookingTimeMinutes;
        this.satisfactionRating = satisfactionRating;
    }

    // -----------------------------------------------------------------------
    // Recipe interface -- type identifier
    // -----------------------------------------------------------------------

    /**
     * {@inheritDoc}
     *
     * @return {@code "MAIN_COURSE"} for all main-course recipe instances
     */
    @Override
    public String getType() {
        return "MAIN_COURSE";
    }

    // -----------------------------------------------------------------------
    // Main-course-specific accessors
    // -----------------------------------------------------------------------

    /**
     * Returns the total cooking time in minutes.
     *
     * @return the cooking time in minutes
     */
    public int getCookingTimeMinutes() {
        return cookingTimeMinutes;
    }

    /**
     * Returns the cook's satisfaction rating for this recipe.
     *
     * @return an integer from 1 (low) to 10 (high)
     */
    public int getSatisfactionRating() {
        return satisfactionRating;
    }

    // -----------------------------------------------------------------------
    // toString -- extends the template from AbstractRecipe
    // -----------------------------------------------------------------------

    /**
     * Returns a string representation that includes both the common recipe
     * fields (via {@code super.toString()}) and main-course-specific details.
     *
     * @return a formatted string with all main-course recipe information
     */
    @Override
    public String toString() {
        return super.toString()
                + String.format(" | MainCourseDetails[time=%dmin, satisfaction=%d/10]",
                        cookingTimeMinutes, satisfactionRating);
    }
}
